package ntut.csie.ezScrum.restful.export;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;

import ntut.csie.ezScrum.iteration.core.ISprintPlanDesc;
import ntut.csie.ezScrum.web.helper.SprintPlanHelper;
import ntut.csie.ezScrum.web.mapper.ProjectMapper;
import ntut.csie.ezScrum.web.support.export.JSONEncoder;
import ntut.csie.jcis.resource.core.IProject;

@Path("projects/{projectName}/sprints")
public class SprintRESTfulApi {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getList(@PathParam("projectName") String projectName) throws JSONException {
		// Get project By name
		IProject project = new ProjectMapper().getProjectByID(projectName);
		// Get sprints
		SprintPlanHelper sprintPlanHelper = new SprintPlanHelper(project);
		List<ISprintPlanDesc> sprints = sprintPlanHelper.loadListPlans();
		// Get Sprints JSONString
		String entity = JSONEncoder.toJSONArray(sprints).toString();
		return Response.status(Response.Status.OK).entity(entity).build();
	}

	@GET
	@Path("/{sprintId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@PathParam("projectName") String projectName, @PathParam("sprintId") long sprintId) throws JSONException {
		// Get project By name
		IProject project = new ProjectMapper().getProjectByID(projectName);
		// Get sprint
		SprintPlanHelper sprintPlanHelper = new SprintPlanHelper(project);
		ISprintPlanDesc sprint = sprintPlanHelper.loadPlan(String.valueOf(sprintId));
		// Get Sprint JSONString
		String entity = JSONEncoder.toJSON(sprint).toString();
		return Response.status(Response.Status.OK).entity(entity).build();
	}
}