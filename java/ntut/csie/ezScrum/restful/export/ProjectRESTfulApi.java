package ntut.csie.ezScrum.restful.export;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;

import ntut.csie.ezScrum.web.mapper.ProjectMapper;
import ntut.csie.ezScrum.web.support.export.ProjectFluent;
import ntut.csie.jcis.resource.core.IProject;

@Path("projects")
public class ProjectRESTfulApi {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getList() throws JSONException {
		// Get projects
		List<IProject> projects = new ProjectMapper().getAllProjectList();
		// Create ProjectFluent
		ProjectFluent projectFluent = new ProjectFluent();
		// Get Projects List JSON
		String entity = projectFluent.Get(projects).toJSON().toString();
		return Response.status(Response.Status.OK).entity(entity).build();
	}

	@GET
	@Path("/{projectName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@PathParam("projectName") String projectName) throws JSONException {
		// Get projects
		List<IProject> projects = new ProjectMapper().getAllProjectList();
		// Create ProjectFluent
		ProjectFluent projectFluent = new ProjectFluent();

		for (IProject project : projects) {
			if (project.getName().equals(projectName)) {
				projectFluent.Get(project);
			}
		}
		return Response.status(Response.Status.OK).entity(projectFluent.toJSON().toString()).build();
	}
}
