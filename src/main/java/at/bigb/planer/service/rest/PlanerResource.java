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
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Map;

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

}
