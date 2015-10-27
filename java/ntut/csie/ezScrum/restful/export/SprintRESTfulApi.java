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
import ntut.csie.ezScrum.web.support.export.JSONEncoder;
import ntut.csie.ezScrum.web.support.export.ResourceFinder;
import ntut.csie.jcis.resource.core.IProject;

@Path("projects/{projectName}/sprints")
public class SprintRESTfulApi {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSprints(@PathParam("projectName") String projectName) throws JSONException {
		// Create ResourceFinder
		ResourceFinder resourceFinder = new ResourceFinder();
		// Get project By name
		IProject project = resourceFinder.findProject(projectName);
		if (project == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		// Get sprints
		SprintPlanHelper sprintPlanHelper = new SprintPlanHelper(project);
		List<ISprintPlanDesc> sprints = sprintPlanHelper.loadListPlans();
		// Get Sprints JSONString
		String entity = JSONEncoder.toSprintJSONArray(sprints).toString();
		return Response.status(Response.Status.OK).entity(entity).build();
	}
}