package ntut.csie.ezScrum.restful.export;

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
import ntut.csie.ezScrum.web.control.ProductBacklogHelper;
import ntut.csie.ezScrum.web.mapper.ProductBacklogMapper;
import ntut.csie.ezScrum.web.support.export.JSONEncoder;
import ntut.csie.ezScrum.web.support.export.ResourceFinder;
import ntut.csie.jcis.resource.core.IProject;

@Path("projects/{projectName}/tasks")
public class AddableTaskRESTfulApi {
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
		
		IIssue[] taskArray;
		if (mIsWild) {
			taskArray = new ProductBacklogHelper(project, null).getAddableTasks();
		} else {
			taskArray = productBacklogMapper.getIssues(ScrumEnum.TASK_ISSUE_TYPE);
		}
		String entity = JSONEncoder.toTaskJSONArray(Arrays.asList(taskArray)).toString();
		return Response.status(Response.Status.OK).entity(entity).build();
	}

	@GET
	@Path("/{taskId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@PathParam("projectName") String projectName,
	                    @PathParam("taskId") long taskId) {
		ResourceFinder resourceFinder = new ResourceFinder();
		IProject project = resourceFinder.findProject(projectName);
		IIssue task = resourceFinder.findTaskInProject(taskId);

		if (project == null || task == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		String entity = JSONEncoder.toTaskJSON(task).toString();
		return Response.status(Response.Status.OK).entity(entity).build();
	}
}
