package ntut.csie.ezScrum.restful.export;

import java.util.Arrays;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.web.control.ProductBacklogHelper;
import ntut.csie.ezScrum.web.support.export.JSONEncoder;
import ntut.csie.ezScrum.web.support.export.ResourceFinder;
import ntut.csie.jcis.resource.core.IProject;

@Path("projects/{projectName}/tasks")
public class DroppedTaskRESTfulApi {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDroppedTasks(@PathParam("projectName") String projectName) {
		ResourceFinder resourceFinder = new ResourceFinder();
		IProject project = resourceFinder.findProject(projectName);

		if (project == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		ProductBacklogHelper productBacklogHelper = new ProductBacklogHelper(project, null);
		IIssue[] taskArray = productBacklogHelper.getAddableTasks();
		String entity = JSONEncoder.toTaskJSONArray(Arrays.asList(taskArray)).toString();
		return Response.status(Response.Status.OK).entity(entity).build();
	}
}
