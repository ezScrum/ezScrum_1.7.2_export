package ntut.csie.ezScrum.restful.export;

import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.iteration.core.ISprintPlanDesc;
import ntut.csie.ezScrum.iteration.core.ScrumEnum;
import ntut.csie.ezScrum.web.mapper.ProductBacklogMapper;
import ntut.csie.ezScrum.web.support.export.JSONEncoder;
import ntut.csie.ezScrum.web.support.export.ResourceFinder;
import ntut.csie.jcis.resource.core.IProject;

@Path("projects/{projectName}/sprints/{sprintId}/stories")
public class StoryRESTfulApi {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getList(@PathParam("projectName") String projectName,
	                        @PathParam("sprintId") long sprintId) {
		ResourceFinder resourceFinder = new ResourceFinder();
		IProject project = resourceFinder.findProject(projectName);
		ISprintPlanDesc sprint = resourceFinder.findSprint(sprintId);
		
		if (project == null || sprint == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		ProductBacklogMapper productBacklogMapper = new ProductBacklogMapper(project, null);
		IIssue[] allStoryArray = productBacklogMapper.getIssues(ScrumEnum.STORY_ISSUE_TYPE);
		ArrayList<IIssue> storiesInSprint = new ArrayList<IIssue>();
		for (IIssue story : allStoryArray) {
			if (sprint.getID().equals(story.getSprintID())) {
				storiesInSprint.add(story);
			}
		}
		String entity = JSONEncoder.toStoryJSONArray(storiesInSprint).toString();
		return Response.status(Response.Status.OK).entity(entity).build();
	}
}
