package ntut.csie.ezScrum.restful.export;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.iteration.core.ISprintPlanDesc;
import ntut.csie.ezScrum.restful.export.support.JSONEncoder;
import ntut.csie.ezScrum.restful.export.support.ResourceFinder;
import ntut.csie.ezScrum.web.mapper.UnplannedItemMapper;
import ntut.csie.jcis.resource.core.IProject;

@Path("projects/{projectName}/sprints/{sprintId}/unplans")
public class UnplanRESTfulApi {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUnplansInSprint(@PathParam("projectName") String projectName,
	                        @PathParam("sprintId") long sprintId) {
		ResourceFinder resourceFinder = new ResourceFinder();
		IProject project = resourceFinder.findProject(projectName);
		ISprintPlanDesc sprint = resourceFinder.findSprint(sprintId);
		
		if (project == null || sprint == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		UnplannedItemMapper unplannedItemMapper = new UnplannedItemMapper(project, null);
		List<IIssue> unplans = unplannedItemMapper.getList(String.valueOf(sprintId));
		String entity = JSONEncoder.toUnplanJSONArray(unplans).toString();
		return Response.status(Response.Status.OK).entity(entity).build();
	}
}
