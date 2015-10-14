package ntut.csie.ezScrum.restful.export;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;

import ntut.csie.ezScrum.iteration.core.IStory;
import ntut.csie.ezScrum.web.mapper.ProductBacklogMapper;
import ntut.csie.ezScrum.web.mapper.ProjectMapper;
import ntut.csie.ezScrum.web.support.export.JSONEncoder;
import ntut.csie.jcis.resource.core.IProject;

@Path("projects/{projectName}/stories")
public class StoryRESTfulApi {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getList(@PathParam("projectName") String projectName) throws JSONException {
		// Get projects
		IProject project = new ProjectMapper().getProjectByID(projectName);
		// Create ProductbacklogMapper
		ProductBacklogMapper productBacklogMapper = new ProductBacklogMapper(project, null);
		// Get Stories
		List<IStory> stories = productBacklogMapper.getAllStoriesByProjectName();
		return Response.status(Response.Status.OK).entity(JSONEncoder.toStoryJSONArray(stories).toString()).build();
	}

	@GET
	@Path("/{storyId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@PathParam("projectName") String projectName, @PathParam("storyId") long storyId) throws JSONException {
		// Get projects
		IProject project = new ProjectMapper().getProjectByID(projectName);
		// Create ProductbacklogMapper
		ProductBacklogMapper productBacklogMapper = new ProductBacklogMapper(project, null);
		// Get Stories
		List<IStory> stories = productBacklogMapper.getAllStoriesByProjectName();

		for (IStory story : stories) {
			if (story.getIssueID() == storyId) {
				return Response.status(Response.Status.OK).entity(JSONEncoder.toStoryJSON(story).toString()).build();
			}
		}
		return Response.status(Response.Status.NOT_FOUND).build();
	}
}
