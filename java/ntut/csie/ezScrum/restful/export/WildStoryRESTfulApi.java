package ntut.csie.ezScrum.restful.export;

import java.util.ArrayList;
import java.util.Arrays;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.iteration.core.ScrumEnum;
import ntut.csie.ezScrum.web.helper.SprintBacklogHelper;
import ntut.csie.ezScrum.web.mapper.ProductBacklogMapper;
import ntut.csie.ezScrum.web.support.export.JSONEncoder;
import ntut.csie.ezScrum.web.support.export.ResourceFinder;
import ntut.csie.jcis.resource.core.IProject;

@Path("projects/{projectName}/stories")
public class WildStoryRESTfulApi {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getList(@PathParam("projectName") String projectName) {
		ResourceFinder resourceFinder = new ResourceFinder();
		IProject project = resourceFinder.findProject(projectName);

		if (project == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		// Get All Stories
		ProductBacklogMapper productBacklogMapper = new ProductBacklogMapper(project, null);
		IIssue[] allStoryArray = productBacklogMapper.getIssues(ScrumEnum.STORY_ISSUE_TYPE);
		// Story List for response
		ArrayList<IIssue> wildStories = new ArrayList<IIssue>();
		for (IIssue story : allStoryArray) {
			long sprintId = Long.parseLong(story.getSprintID());
			// 保留野生的Story
			if (sprintId <= 0) {
				wildStories.add(story);
			}
		}
		String entity = JSONEncoder.toStoryJSONArray(wildStories).toString();
		return Response.status(Response.Status.OK).entity(entity).build();
	}

	@GET
	@Path("/{storyId}/tasks")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@PathParam("projectName") String projectName,
	                    @PathParam("storyId") long storyId) {
		ResourceFinder resourceFinder = new ResourceFinder();
		IProject project = resourceFinder.findProject(projectName);
		IIssue story = resourceFinder.findWildStory(storyId);

		if (project == null || story == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		
		SprintBacklogHelper sprintBacklogHelper = new SprintBacklogHelper(project, null);
		// Get Tasks
		IIssue[] tasks = sprintBacklogHelper.getTaskInStory(String.valueOf(storyId));
		String entity = JSONEncoder.toTaskJSONArray(Arrays.asList(tasks)).toString();
		return Response.status(Response.Status.OK).entity(entity).build();
	}
}
