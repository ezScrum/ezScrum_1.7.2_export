package ntut.csie.ezScrum.restful.export;

import java.util.Arrays;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.iteration.core.ISprintPlanDesc;
import ntut.csie.ezScrum.web.helper.SprintBacklogHelper;
import ntut.csie.ezScrum.web.support.export.JSONEncoder;
import ntut.csie.ezScrum.web.support.export.ResourceFinder;
import ntut.csie.jcis.resource.core.IProject;


@Path("projects/{projectName}/sprints/{sprintId}/stories/{storyId}/tasks")
public class TaskRESTfulApi {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getList(@PathParam("projectName") String projectName, 
			                @PathParam("sprintId") long sprintId,
			                @PathParam("storyId") long storyId) {
		ResourceFinder resourceFinder = new ResourceFinder();
		IProject project = resourceFinder.findProject(projectName);
		ISprintPlanDesc sprint = resourceFinder.findSprint(sprintId);
		IIssue story = resourceFinder.findStory(storyId);
		
		if (project == null || sprint == null || story == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		
		// Create SprintBacklogHelper
		SprintBacklogHelper sprintBacklogHelper = new SprintBacklogHelper(project, null, sprint.getID());
		// Get Tasks
		IIssue[] tasks = sprintBacklogHelper.getTaskInStory(String.valueOf(storyId));
		String entity = JSONEncoder.toTaskJSONArray(Arrays.asList(tasks)).toString();
		return Response.status(Response.Status.OK).entity(entity).build();
	}
}
