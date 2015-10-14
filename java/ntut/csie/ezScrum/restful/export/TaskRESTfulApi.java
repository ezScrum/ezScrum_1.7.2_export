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

import org.codehaus.jettison.json.JSONException;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.iteration.core.ScrumEnum;
import ntut.csie.ezScrum.web.mapper.ProductBacklogMapper;
import ntut.csie.ezScrum.web.mapper.ProjectMapper;
import ntut.csie.ezScrum.web.support.export.JSONEncoder;
import ntut.csie.jcis.resource.core.IProject;


@Path("projects/{projectName}/tasks")
public class TaskRESTfulApi {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getList(@PathParam("projectName") String projectName) throws JSONException {	
		// Get project
		IProject project = new ProjectMapper().getProjectByID(projectName);
		// Create ProductbacklogMapper
		ProductBacklogMapper productBacklogMapper = new ProductBacklogMapper(project, null);
		// Get Tasks
		IIssue[] tasksArray = productBacklogMapper.getIssues(ScrumEnum.TASK_ISSUE_TYPE);
		// Add tasks to List
		List<IIssue> tasks = new ArrayList<IIssue>();
		tasks.addAll(Arrays.asList(tasksArray));

		return Response.status(Response.Status.OK).entity(JSONEncoder.toTaskJSONArray(tasks)).build();
	}

	@GET
	@Path("/{taskId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@PathParam("projectName") String projectName, @PathParam("taskId") long taskId) throws JSONException {
		// Get project
		IProject project = new ProjectMapper().getProjectByID(projectName);
		// Create ProductbacklogMapper
		ProductBacklogMapper productBacklogMapper = new ProductBacklogMapper(project, null);
		// Get Tasks
		IIssue[] tasksArray = productBacklogMapper.getIssues(ScrumEnum.TASK_ISSUE_TYPE);

		for (IIssue task : tasksArray) {
			if (task.getIssueID() == taskId) {
				return Response.status(Response.Status.OK).entity(JSONEncoder.toTaskJSON(task).toString()).build();
			}
		}
		return Response.status(Response.Status.NOT_FOUND).build();
	}
}
