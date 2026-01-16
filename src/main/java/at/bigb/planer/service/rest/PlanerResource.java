package at.bigb.planer.service.rest;

import at.bigb.planer.domain.ScheduleConfig;
import at.bigb.planer.domain.dto.PairingDto;
import at.bigb.planer.domain.dto.PlanDto;
import at.bigb.planer.domain.dto.ScheduleConfigDto;
import at.bigb.planer.domain.dto.ScheduleStatsDto;
import at.bigb.planer.service.ScheduleGenerationService;
import at.bigb.planer.service.ScheduleMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API endpoint for schedule planning operations
 */
@Path("/planer")
@Tag(name = "Schedule Planning", description = "API endpoints for generating and managing player schedules")
@Slf4j
public class PlanerResource {

    private final ScheduleGenerationService scheduleService;

    public PlanerResource() {
        this.scheduleService = new ScheduleGenerationService();
    }

    /**
     * Health check endpoint
     */
    @GET
    @Path("/health")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Health check", description = "Returns the health status of the API")
    @APIResponse(responseCode = "200", description = "Service is healthy")
    public String getHealth() {
        return "{\"status\":\"OK\"}";
    }

    /**
     * Generates a schedule based on player names and number of rounds
     *
     * @param configDto Configuration containing player names and rounds
     * @return Generated plan with all rounds and player selections
     */
    @POST
    @Path("/generate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Generate schedule", description = "Generates an optimized schedule with minimal player combination repetition")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Schedule generated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlanDto.class))),
        @APIResponse(responseCode = "400", description = "Invalid configuration (e.g., less than 4 players, 0 rounds, duplicate names)"),
        @APIResponse(responseCode = "500", description = "Internal server error during schedule generation")
    })
    public PlanDto generateSchedule(ScheduleConfigDto configDto) {
        try {
            log.info("Received schedule generation request with {} players and {} rounds",
                    configDto.getPlayerNames().size(), configDto.getNumberOfRounds());

            // Convert DTO to domain model
            ScheduleConfig config = ScheduleMapper.mapDtoToScheduleConfig(configDto);

            // Generate schedule
            var plan = scheduleService.generateSchedule(config);

            // Convert back to DTO and return
            return ScheduleMapper.mapPlanToDto(plan);
        } catch (IllegalArgumentException e) {
            log.error("Invalid configuration: {}", e.getMessage());
            throw new BadRequestException("Invalid configuration: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error generating schedule", e);
            throw new InternalServerErrorException("Error generating schedule: " + e.getMessage());
        }
    }

    /**
     * Gets statistics about the current pairings
     *
     * @return Statistics including unique pairings, frequencies, etc.
     */
    @GET
    @Path("/statistics")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get pairing statistics", description = "Returns statistics about player pairings including frequencies and distribution")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Statistics retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ScheduleStatsDto.class))),
        @APIResponse(responseCode = "500", description = "Internal server error retrieving statistics")
    })
    public ScheduleStatsDto getStatistics() {
        try {
            log.info("Retrieving pairing statistics");
            var stats = scheduleService.getPairingStatistics();
            return ScheduleMapper.mapStatsToDto(stats);
        } catch (Exception e) {
            log.error("Error retrieving statistics", e);
            throw new InternalServerErrorException("Error retrieving statistics: " + e.getMessage());
        }
    }

    /**
     * Gets all pairings sorted by frequency
     *
     * @return List of pairings with their frequencies
     */
    @GET
    @Path("/pairings")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all pairings", description = "Returns a list of all player pairings sorted by frequency (most frequent first)")
    @APIResponse(responseCode = "200", description = "Pairings retrieved successfully")
    @APIResponse(responseCode = "500", description = "Internal server error retrieving pairings")
    public java.util.List<PairingDto> getAllPairings() {
        try {
            log.info("Retrieving all pairings");
            return scheduleService.getAllPairingsSorted();
        } catch (Exception e) {
            log.error("Error retrieving pairings", e);
            throw new InternalServerErrorException("Error retrieving pairings: " + e.getMessage());
        }
    }

    /**
     * Gets player usage statistics
     *
     * @return Map of player names to the number of times they have been scheduled
     */
    @GET
    @Path("/player-usage")
    @Produces("application/json")
    @Operation(summary = "Get player usage statistics", description = "Returns statistics about how many times players have been scheduled")
    @APIResponse(responseCode = "200", description = "Player usage statistics retrieved successfully")
    @APIResponse(responseCode = "500", description = "Internal server error retrieving player usage statistics")
    public Map<String, Integer> getPlayerUsage() {
        try {
            log.info("Retrieving player usage statistics");
            return scheduleService.getPlayerUsageStatistics();
        } catch (Exception e) {
            log.error("Error retrieving player usage statistics", e);
            throw new InternalServerErrorException("Error retrieving player usage statistics: " + e.getMessage());
        }
    }

    /**
     * Downloads the last generated plan as an XLS-compatible CSV file.
     * Saves the CSV under a local tmp/ directory and returns it as an attachment.
     * Returns 404 if no plan was generated yet.
     */
    @GET
    @Path("/download")
    @Produces("application/vnd.ms-excel")
    @Operation(summary = "Download last plan as XLS-compatible CSV", description = "Returns the most recently generated plan as a CSV file that can be opened in Excel")
    @APIResponse(responseCode = "200", description = "CSV file returned successfully")
    @APIResponse(responseCode = "404", description = "No previously generated plan available")
    public Response downloadLastPlan() {
        var plan = scheduleService.getLastGeneratedPlan();
        if (plan == null) {
            throw new NotFoundException("No generated plan available for download");
        }

        // Build CSV content: header + rounds. Simple layout: RoundNo,Date,Player1,Player2,Player3,Player4
        String csv = plan.getRounds().stream().map(r -> {
            String players = r.getSelectedPlayers().stream()
                    .map(p -> escapeCsv(p.getName()))
                    .collect(Collectors.joining(","));
            return String.format("%d,%s,%s", r.getRoundNo(), r.getRoundDate(), players);
        }).collect(Collectors.joining("\n"));

        String header = "RoundNo,Date,Player1,Player2,Player3,Player4\n";
        String content = header + csv;

        try {
            // allow explicit override of project root via environment variable
            String explicitRoot = System.getenv("BIGB_PLANER_PROJECT_ROOT");
            java.nio.file.Path projectRoot;
            if (explicitRoot != null && !explicitRoot.isBlank()) {
                projectRoot = Paths.get(explicitRoot).toAbsolutePath();
            } else {
                // determine project root (walk up until we find gradlew.bat, build.gradle or settings.gradle)
                projectRoot = findProjectRoot();
            }
             java.nio.file.Path tmpDir = projectRoot.resolve("build").resolve("tmp");
             if (!Files.exists(tmpDir)) {
                 Files.createDirectories(tmpDir);
             }
             String filename = String.format("plan-%d.csv", System.currentTimeMillis());
             java.nio.file.Path file = tmpDir.resolve(filename);
             Files.writeString(file, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

             return Response.ok(content, "application/vnd.ms-excel")
                     .header("Content-Disposition", "attachment; filename=" + filename)
                     .header("X-Server-File", file.toAbsolutePath().toString())
                     .build();
         } catch (IOException e) {
             log.error("Error writing CSV to tmp directory", e);
             throw new InternalServerErrorException("Could not write CSV file: " + e.getMessage());
         }
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\n") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // Walk up the directory tree to find the project root by repository folder name or common Gradle files
    private java.nio.file.Path findProjectRoot() {
        java.nio.file.Path cur = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        while (cur != null) {
            // first, check if this directory itself is the repository root by name (case-insensitive)
            java.nio.file.Path name = cur.getFileName();
            if (name != null && "bigb-planer-project".equalsIgnoreCase(name.toString())) {
                return cur;
            }
            // next, prefer Gradle build files in case the folder name differs
            if (Files.exists(cur.resolve("gradlew.bat")) || Files.exists(cur.resolve("build.gradle")) || Files.exists(cur.resolve("settings.gradle"))) {
                return cur;
            }
            cur = cur.getParent();
        }

        // If not found yet, attempt to find a sibling 'bigb-planer-project' by walking parents
        java.nio.file.Path start = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        java.nio.file.Path p = start;
        while (p != null) {
            java.nio.file.Path candidate = p.resolve("bigb-planer-project");
            if (Files.exists(candidate)) {
                return candidate.toAbsolutePath();
            }
            p = p.getParent();
        }

        // fallback to user.dir absolute path
        return Paths.get(System.getProperty("user.dir")).toAbsolutePath();
    }

}
