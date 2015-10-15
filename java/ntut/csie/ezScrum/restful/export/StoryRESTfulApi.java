package ntut.csie.ezScrum.restful.export;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

@Path("projects/{projectName}")
public class StoryRESTfulApi {
	@GET
	@Path("/stories")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getList(@PathParam("projectName") String projectName,
			@DefaultValue("false") @QueryParam("wild") boolean isWild) {
		IProject project = ResourceFinder.findProject(projectName);
		if (project == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		ProductBacklogMapper productBacklogMapper = new ProductBacklogMapper(project, null);
		IIssue[] storyArray = productBacklogMapper.getIssues(ScrumEnum.STORY_ISSUE_TYPE);
		List<IIssue> stories = new ArrayList<IIssue>();
		stories.addAll(Arrays.asList(storyArray));
		String entity = "";
		if (isWild) {
			List<IIssue> wildStories = new ArrayList<IIssue>();
			for (IIssue story : stories) {
				String sprintId = story.getSprintID();
				if ((sprintId == null) || (Integer.parseInt(sprintId) <= 0)) {
					wildStories.add(story);
				}
			}
			entity = JSONEncoder.toStoryJSONArray(wildStories).toString();
		} else {
			entity = JSONEncoder.toStoryJSONArray(stories).toString();
		}
		return Response.status(Response.Status.OK).entity(entity).build();
	}

	@GET
	@Path("sprints/{sprintId}/stories/{storyId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@PathParam("projectName") String projectName, @PathParam("sprintId") long sprintId,
			@PathParam("storyId") long storyId) {
		IIssue story = ResourceFinder.findStory(projectName, sprintId, storyId);
		if (story == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		String entity = JSONEncoder.toStoryJSON(story).toString();
		return Response.status(Response.Status.OK).entity(entity).build();
	}
}
