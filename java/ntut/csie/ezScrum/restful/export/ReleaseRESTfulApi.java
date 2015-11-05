package ntut.csie.ezScrum.restful.export;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ntut.csie.ezScrum.iteration.core.IReleasePlanDesc;
import ntut.csie.ezScrum.restful.export.support.JSONEncoder;
import ntut.csie.ezScrum.restful.export.support.ResourceFinder;
import ntut.csie.ezScrum.web.helper.ReleasePlanHelper;
import ntut.csie.jcis.resource.core.IProject;

@Path("projects/{projectName}/releases")
public class ReleaseRESTfulApi {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getReleases(@PathParam("projectName") String projectName) {
		ResourceFinder resourceFinder = new ResourceFinder();
		IProject project = resourceFinder.findProject(projectName);
		
		if (project == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		ReleasePlanHelper releasePlanHelper = new ReleasePlanHelper(project);
		List<IReleasePlanDesc> releases = releasePlanHelper.loadReleasePlansList();
		String entity = JSONEncoder.toReleaseJSONArray(releases).toString();
		return Response.status(Response.Status.OK).entity(entity).build();
	}
}
