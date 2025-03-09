package at.bigb.planer.service.rest;

import at.bigb.planer.domain.dto.PlanDto;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/planer")
public class PlanerResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getPlaningResult() {
        PlanDto myPlan = new PlanDto();
        return myPlan.toString();
    }

}
