package ntut.csie.ezScrum.restful.export;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("projects")
public class ProjectRESTfulApi {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getList() {
		return null;
	}

	@GET
	@Path("/{projectId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@PathParam("projectId") long projectId) {
		return null;
	}
}
