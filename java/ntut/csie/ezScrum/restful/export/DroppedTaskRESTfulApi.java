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
import ntut.csie.ezScrum.restful.export.support.JSONEncoder;
import ntut.csie.ezScrum.restful.export.support.ResourceFinder;
import ntut.csie.ezScrum.web.control.ProductBacklogHelper;
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
	
	@GET
	@Path("/{taskId}/attachfiles")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAttachFiles(@PathParam("projectName") String projectName,
			                       @PathParam("taskId") long taskId) {
		ResourceFinder resourceFinder = new ResourceFinder();
		IProject project = resourceFinder.findProject(projectName);
		IIssue droppedTask = resourceFinder.findDroppedTask(taskId);

		if (project == null || droppedTask == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		ProductBacklogHelper productBacklogHelper = new ProductBacklogHelper(project, null);
		List<File> sourceFiles = new ArrayList<File>();
		List<IssueAttachFile> attachFiles = droppedTask.getAttachFile();
		for (IssueAttachFile attachFile : attachFiles) {
			String attachFileIdString = String.valueOf(attachFile.getAttachFileId());
			File srouceFile = productBacklogHelper.getAttachFile(attachFileIdString);
			sourceFiles.add(srouceFile);
		}
		String entity = JSONEncoder.toAttachFileJSONArray(attachFiles, sourceFiles).toString();
		return Response.status(Response.Status.OK).entity(entity).build();
	}
}
