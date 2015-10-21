package ntut.csie.ezScrum.restful.export;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import ntut.csie.ezScrum.web.databaseEnum.ExportEnum;
import ntut.csie.ezScrum.web.databaseEnum.ProjectEnum;

@Path("export")
public class IntegratedRESTfulApi {
	private Client mClient;
	private static String BASE_URL = "http://localhost:8080/ezScrum/resource/";
	
	@GET
	@Path("/projects")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProjectsExportJSON() throws JSONException{
		// Get Client
		mClient = ClientBuilder.newClient();
		// Response JSONObject
		JSONObject exportJSON = new JSONObject();
		// Projects JSONArray
		JSONArray projects = null;
		// Sprints In Project
		Map<String, JSONArray> sprintsMap = new HashMap<String, JSONArray>();
		// Stories In Sprint
		Map<String, JSONArray> storiesMap = new HashMap<String, JSONArray>();
		// Tasks In Story
		Map<String, JSONArray> tasksMap = new HashMap<String, JSONArray>();
		
		//// Get Projects ////
		// Get Projects
		Response response = mClient.target(BASE_URL)
		        .path("projects")
		        .request()
		        .get();
		projects = new JSONArray(response.readEntity(String.class));
		exportJSON.put(ExportEnum.PROJECTS, projects);
		
		//// Get Sprints ////
		for (int i = 0; i < projects.length(); i++) {
			JSONObject project = projects.getJSONObject(i);
			// Get Sprints
			response = mClient.target(BASE_URL)
			        .path("projects/" + project.getString(ProjectEnum.NAME) + "/sprints")
			        .request()
			        .get();
			JSONArray sprints = new JSONArray(response.readEntity(String.class));
			sprintsMap.put(project.getString(ProjectEnum.NAME), sprints);
		}
		
		//// Get Story ////
		
		
		
		

		return null;
	}
}
