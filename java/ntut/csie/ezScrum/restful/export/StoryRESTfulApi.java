package ntut.csie.ezScrum.restful.export;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.issue.internal.IssueAttachFile;
import ntut.csie.ezScrum.iteration.core.ISprintPlanDesc;
import ntut.csie.ezScrum.iteration.core.ScrumEnum;
import ntut.csie.ezScrum.restful.export.support.JSONEncoder;
import ntut.csie.ezScrum.restful.export.support.ResourceFinder;
import ntut.csie.ezScrum.web.control.ProductBacklogHelper;
import ntut.csie.ezScrum.web.mapper.ProductBacklogMapper;
import ntut.csie.jcis.resource.core.IProject;

@Path("projects/{projectName}/sprints/{sprintId}/stories")
public class StoryRESTfulApi {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStoriesInSprint(@PathParam("projectName") String projectName,
			@PathParam("sprintId") long sprintId) {
		ResourceFinder resourceFinder = new ResourceFinder();
		IProject project = resourceFinder.findProject(projectName);
		ISprintPlanDesc sprint = resourceFinder.findSprint(sprintId);
		if (project == null || sprint == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		
		// Get All Stories
		ProductBacklogMapper productBacklogMapper = new ProductBacklogMapper(project, null);
		IIssue[] allStoryArray = productBacklogMapper.getIssues(ScrumEnum.STORY_ISSUE_TYPE);
		// Story List for response
		ArrayList<IIssue> storiesInSpecificSprint = new ArrayList<IIssue>();
		for (IIssue story : allStoryArray) {
			long currentSprintId = Long.parseLong(story.getSprintID());
			if (currentSprintId == sprintId) {
				storiesInSpecificSprint.add(story);
			}
		}
		JSONArray storyJSONArray = JSONEncoder.toStoryJSONArray(storiesInSpecificSprint);
		String entity = storyJSONArray.toString();
		return Response.status(Response.Status.OK).entity(entity).build();
	}

	@GET
	@Path("/{storyId}/attachfiles")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAttachFilesInStory(@PathParam("projectName") String projectName,
			@PathParam("sprintId") long sprintId, @PathParam("storyId") long storyId) {
		ResourceFinder resourceFinder = new ResourceFinder();
		IProject project = resourceFinder.findProject(projectName);
		ISprintPlanDesc sprint = resourceFinder.findSprint(sprintId);
		IIssue story = resourceFinder.findStory(storyId);

		if (project == null || sprint == null || story == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		ProductBacklogHelper productBacklogHelper = new ProductBacklogHelper(project, null);
		List<File> sourceFiles = new ArrayList<File>();
		List<IssueAttachFile> attachFiles = story.getAttachFile();
		for (IssueAttachFile attachFile : attachFiles) {
			String attachFileIdString = String.valueOf(attachFile.getAttachFileId());
			File srouceFile = productBacklogHelper.getAttachFile(attachFileIdString);
			sourceFiles.add(srouceFile);
		}
		String entity = JSONEncoder.toAttachFileJSONArray(attachFiles, sourceFiles).toString();
		return Response.status(Response.Status.OK).entity(entity).build();
	}
	
	@GET
	@Path("/{storyId}/histories")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getHistoriesInStory(@PathParam("projectName") String projectName,
			@PathParam("sprintId") long sprintId, @PathParam("storyId") long storyId) {
		ResourceFinder resourceFinder = new ResourceFinder();
		IProject project = resourceFinder.findProject(projectName);
		ISprintPlanDesc sprint = resourceFinder.findSprint(sprintId);
		IIssue story = resourceFinder.findStory(storyId);

		if (project == null || sprint == null || story == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}

		@SuppressWarnings("deprecation")
		String entity = JSONEncoder.toHistoryJSONArray(story.getIssueHistories(), story.getCategory()).toString();
		return Response.status(Response.Status.OK).entity(entity).build();
	}
}
