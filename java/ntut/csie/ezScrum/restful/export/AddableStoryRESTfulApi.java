package ntut.csie.ezScrum.restful.export;

import java.util.ArrayList;
import java.util.Arrays;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.iteration.core.ScrumEnum;
import ntut.csie.ezScrum.web.mapper.ProductBacklogMapper;
import ntut.csie.ezScrum.web.support.export.JSONEncoder;
import ntut.csie.ezScrum.web.support.export.ResourceFinder;
import ntut.csie.jcis.resource.core.IProject;

@Path("projects/{projectName}/stories")
public class AddableStoryRESTfulApi {
	@QueryParam("isWild")
	@DefaultValue("false")
	private boolean mIsWild;
	
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
		IIssue[] storyArray = productBacklogMapper.getIssues(ScrumEnum.STORY_ISSUE_TYPE);
		// Story List for response
		ArrayList<IIssue> stories = new ArrayList<IIssue>(Arrays.asList(storyArray));

		if (mIsWild) {
			for (IIssue story : stories) {
				long sprintId = Long.parseLong(story.getSprintID());
				// 保留野生的Story
				if (sprintId > 0) {
					stories.remove(stories.indexOf(story));
				}
			}
		}
		String entity = JSONEncoder.toStoryJSONArray(stories).toString();
		return Response.status(Response.Status.OK).entity(entity).build();
	}

	@GET
	@Path("/{storyId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@PathParam("projectName") String projectName,
	                    @PathParam("storyId") long storyId) {
		ResourceFinder resourceFinder = new ResourceFinder();
		IProject project = resourceFinder.findProject(projectName);
		IIssue story = resourceFinder.findStory(storyId);

		if (project == null || story == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		String entity = JSONEncoder.toStoryJSON(story).toString();
		return Response.status(Response.Status.OK).entity(entity).build();
	}
}
