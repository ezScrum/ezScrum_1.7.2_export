package ntut.csie.ezScrum.restful.export;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.iteration.core.ScrumEnum;
import ntut.csie.ezScrum.web.mapper.ProductBacklogMapper;
import ntut.csie.ezScrum.web.mapper.ProjectMapper;
import ntut.csie.ezScrum.web.support.export.JSONEncoder;
import ntut.csie.jcis.resource.core.IProject;

@Path("projects/{projectName}/stories")
public class StoryRESTfulApi {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getList(@PathParam("projectName") String projectName) {
		// Get projects
		IProject project = new ProjectMapper().getProjectByID(projectName);
		// Create ProductbacklogMapper
		ProductBacklogMapper productBacklogMapper = new ProductBacklogMapper(project, null);
		// Get Stories
		IIssue[] storyArray = productBacklogMapper.getIssues(ScrumEnum.STORY_ISSUE_TYPE);
		List<IIssue> stories = new ArrayList<IIssue>();
		stories.addAll(Arrays.asList(storyArray));
		String entity = JSONEncoder.toStoryJSONArray(stories).toString();
		return Response.status(Response.Status.OK).entity(entity).build();
	}

	@GET
	@Path("/{storyId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@PathParam("projectName") String projectName, @PathParam("storyId") long storyId) {
		// Get projects
		IProject project = new ProjectMapper().getProjectByID(projectName);
		// Create ProductbacklogMapper
		ProductBacklogMapper productBacklogMapper = new ProductBacklogMapper(project, null);
		// Get Stories
		IIssue[] storyArray = productBacklogMapper.getIssues(ScrumEnum.STORY_ISSUE_TYPE);
		List<IIssue> stories = new ArrayList<IIssue>();
		stories.addAll(Arrays.asList(storyArray));

		for (IIssue story : stories) {
			if (story.getIssueID() == storyId) {
				String entity = JSONEncoder.toStoryJSON(story).toString();
				return Response.status(Response.Status.OK).entity(entity).build();
			}
		}
		return Response.status(Response.Status.NOT_FOUND).build();
	}
}
