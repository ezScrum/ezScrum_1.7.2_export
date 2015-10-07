package ntut.csie.ezScrum.restful.export;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("tasks")
public class TaskRESTfulApi {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getList() {
		return null;
	}

	@GET
	@Path("/{taskId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@PathParam("taskId") long taskId) {
		return null;
	}
}
