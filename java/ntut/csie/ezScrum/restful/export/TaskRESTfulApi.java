package ntut.csie.ezScrum.restful.export;

import java.io.File;
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
import ntut.csie.ezScrum.issue.internal.IssueAttachFile;
import ntut.csie.ezScrum.iteration.core.ISprintPlanDesc;
import ntut.csie.ezScrum.web.control.ProductBacklogHelper;
import ntut.csie.ezScrum.web.helper.SprintBacklogHelper;
import ntut.csie.ezScrum.web.support.export.JSONEncoder;
import ntut.csie.ezScrum.web.support.export.ResourceFinder;
import ntut.csie.jcis.resource.core.IProject;


@Path("projects/{projectName}/sprints/{sprintId}/stories/{storyId}/tasks")
public class TaskRESTfulApi {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTasksinStory(@PathParam("projectName") String projectName, 
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
	
	@GET
	@Path("/{taskId}/attachfiles")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAttachFiles(@PathParam("projectName") String projectName,
			                       @PathParam("sprintId") long sprintId,
                                   @PathParam("storyId") long storyId,
	        				       @PathParam("taskId") long taskId) {
		ResourceFinder resourceFinder = new ResourceFinder();
		IProject project = resourceFinder.findProject(projectName);
		ISprintPlanDesc sprint = resourceFinder.findSprint(sprintId);
		IIssue story = resourceFinder.findStory(storyId);
		IIssue task = resourceFinder.findTask(taskId);

		if (project == null || sprint == null ||
		        story == null || task == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		ProductBacklogHelper productBacklogHelper = new ProductBacklogHelper(project, null);
		List<File> sourceFiles = new ArrayList<File>();
		List<IssueAttachFile> attachFiles = task.getAttachFile();
		for (IssueAttachFile attachFile : attachFiles) {
			String attachFileIdString = String.valueOf(attachFile.getAttachFileId());
			File srouceFile = productBacklogHelper.getAttachFile(attachFileIdString);
			sourceFiles.add(srouceFile);
		}
		String entity = JSONEncoder.toAttachFileJSONArray(attachFiles, sourceFiles).toString();
		return Response.status(Response.Status.OK).entity(entity).build();
	}

	@GET
	@Path("/{taskId}/partners")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPartners(@PathParam("projectName") String projectName,
	        					@PathParam("sprintId") long sprintId,
	        					@PathParam("storyId") long storyId,
	        					@PathParam("taskId") long taskId) {
		ResourceFinder resourceFinder = new ResourceFinder();
		IProject project = resourceFinder.findProject(projectName);
		ISprintPlanDesc sprint = resourceFinder.findSprint(sprintId);
		IIssue story = resourceFinder.findStory(storyId);
		IIssue task = resourceFinder.findTask(taskId);

		if (project == null || sprint == null ||
		        story == null || task == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		@SuppressWarnings("deprecation")
		String partnersString = task.getPartners();
		String entity = JSONEncoder.toPartnerJSONArray(partnersString).toString();
		return Response.status(Response.Status.OK).entity(entity).build();
	}
}
