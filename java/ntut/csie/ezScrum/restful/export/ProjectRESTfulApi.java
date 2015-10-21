package ntut.csie.ezScrum.restful.export;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;

import ntut.csie.ezScrum.web.mapper.ProjectMapper;
import ntut.csie.ezScrum.web.support.export.JSONEncoder;
import ntut.csie.jcis.resource.core.IProject;

@Path("projects")
public class ProjectRESTfulApi {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProjects() throws JSONException {
		// Get projects
		List<IProject> projects = new ProjectMapper().getAllProjectList();
		// Get Projects List JSON
		String entity = JSONEncoder.toProjectJSONArray(projects).toString();
		return Response.status(Response.Status.OK).entity(entity).build();
	}
}
