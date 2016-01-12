package ntut.csie.ezScrum.restful.export;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ntut.csie.ezScrum.iteration.core.IScrumIssue;
import ntut.csie.ezScrum.iteration.core.ISprintPlanDesc;
import ntut.csie.ezScrum.iteration.core.ScrumEnum;
import ntut.csie.ezScrum.restful.export.support.JSONEncoder;
import ntut.csie.ezScrum.restful.export.support.ResourceFinder;
import ntut.csie.ezScrum.web.mapper.RetrospectiveMapper;
import ntut.csie.jcis.resource.core.IProject;

@Path("projects/{projectName}/sprints/{sprintId}/retrospectives")
public class RetrospectiveRESTfulApi {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRetrospectivesInSprint(@PathParam("projectName") String projectName,
            @PathParam("sprintId") long sprintId) {
		ResourceFinder resourceFinder = new ResourceFinder();
		IProject project = resourceFinder.findProject(projectName);
		ISprintPlanDesc sprint = resourceFinder.findSprint(sprintId);
		
		if (project == null || sprint == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		
		RetrospectiveMapper retrospectiveMapper = new RetrospectiveMapper(project, null);
		List<IScrumIssue> retrospectives = getRetrospectives(sprintId, retrospectiveMapper);
		String entity = JSONEncoder.toRetrospectiveJSONArray(retrospectives).toString();
		return Response.status(Response.Status.OK).entity(entity).build();
	}
	
	private List<IScrumIssue> getRetrospectives(long sprintId, RetrospectiveMapper retrospectiveMapper) {
		List<IScrumIssue> retrospectives = new ArrayList<IScrumIssue>();
		List<IScrumIssue> goods = retrospectiveMapper.getList(ScrumEnum.GOOD_ISSUE_TYPE);
		for (IScrumIssue good : goods) {
			try {
				long sprintIdInGood = Long.parseLong(good.getSprintID());
				if (sprintIdInGood == sprintId) {
					retrospectives.add(good);
				}
			} catch (NumberFormatException e) {
				// when sprint id is invalid will be filtered
				continue;
			}
		}
		List<IScrumIssue> improvements = retrospectiveMapper.getList(ScrumEnum.IMPROVEMENTS_ISSUE_TYPE);
		for (IScrumIssue improvement : improvements) {
			try {
				long sprintIdInImprovement = Long.parseLong(improvement.getSprintID());
				if (sprintIdInImprovement == sprintId) {
					retrospectives.add(improvement);
				}
			} catch (NumberFormatException e) {
				// when sprint id is invalid will be filtered
				continue;
			}
		}
		return retrospectives;
	}
}
