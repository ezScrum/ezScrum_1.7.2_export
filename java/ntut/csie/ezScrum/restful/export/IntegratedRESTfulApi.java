package ntut.csie.ezScrum.restful.export;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import ntut.csie.ezScrum.web.databaseEnum.ExportEnum;
import ntut.csie.ezScrum.web.databaseEnum.ProjectEnum;
import ntut.csie.ezScrum.web.databaseEnum.SprintEnum;
import ntut.csie.ezScrum.web.databaseEnum.StoryEnum;

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
			String projectName = project.getString(ProjectEnum.NAME);
			// Get Sprints
			response = mClient.target(BASE_URL)
			        .path("projects/" + projectName + "/sprints")
			        .request()
			        .get();
			JSONArray sprints = new JSONArray(response.readEntity(String.class));
			sprintsMap.put(projectName, sprints);
		}
		// Add Sprints to Project
		for (int i = 0; i < projects.length(); i++) {
			JSONObject project = projects.getJSONObject(i);
			String projectName = project.getString(ProjectEnum.NAME);
			JSONArray sprintsInProject = sprintsMap.get(projectName);
			project.put(ExportEnum.SPRINTS, sprintsInProject);
		}
		
		
		//// Get Stories ////
		for (Map.Entry<String, JSONArray> sprintMap : sprintsMap.entrySet()) {
			String projectName = sprintMap.getKey();
			JSONArray sprintArray = sprintMap.getValue();
			
			// Get Stories from Sprint and add to Sprint
			for(int i = 0; i < sprintArray.length(); i++){
				JSONObject sprintJSON = sprintArray.getJSONObject(i);
				long sprintId = sprintJSON.getLong(SprintEnum.ID);
				response = mClient.target(BASE_URL)
				        .path("projects/" + projectName +
				              "/sprints/" + sprintId + "/stories")
				        .request()
				        .get();
				JSONArray storiesArray = new JSONArray(response.readEntity(String.class));
				sprintJSON.put(ExportEnum.STORIES, storiesArray);
				
				//// Get Tasks ////
				for (int j = 0; j < storiesArray.length(); j++) {
					JSONObject story = storiesArray.getJSONObject(j);
					long storyId = story.getLong(StoryEnum.ID);
					response = mClient.target(BASE_URL)
					        .path("projects/" + projectName +
					              "/sprints/" + sprintId + 
					              "/stories/" + storyId + 
					              "/tasks")
					        .request()
					        .get();
					JSONArray tasksArray = new JSONArray(response.readEntity(String.class));
					story.put(ExportEnum.TASKS, tasksArray);
				}
			}
		}
		
		//// Get WildStories ////
		for (int i = 0; i < projects.length(); i++) {
			JSONObject project = projects.getJSONObject(i);
			String projectName = project.getString(ProjectEnum.NAME);
			// Get Sprints
			response = mClient.target(BASE_URL)
			        .path("projects/" + projectName + "/stories")
			        .request()
			        .get();
			JSONArray wildStoriesArray = new JSONArray(response.readEntity(String.class));
			project.put(ExportEnum.DROPPED_STORIES, wildStoriesArray);
			
			// Get Tasks in WildStory
			for (int j = 0; j < wildStoriesArray.length(); j++) {
				JSONObject wildStory = wildStoriesArray.getJSONObject(j);
				long wildStoryId = wildStory.getLong(StoryEnum.ID);
				response = mClient.target(BASE_URL)
				        .path("projects/" + projectName + 
				        	  "/stories/" + wildStoryId + 
				        	  "/tasks")
				        .request()
				        .get();
				JSONArray tasksArray = new JSONArray(response.readEntity(String.class));
				wildStory.put(ExportEnum.TASKS, tasksArray);
			}
		}
		
		//// Get WildTasks ////
		for (int i = 0; i < projects.length(); i++) {
			JSONObject project = projects.getJSONObject(i);
			String projectName = project.getString(ProjectEnum.NAME);
			// Get Sprints
			response = mClient.target(BASE_URL)
			        .path("projects/" + projectName + "/tasks")
			        .request()
			        .get();
			JSONArray tasksArray = new JSONArray(response.readEntity(String.class));
			project.put(ExportEnum.DROPPED_TASKS, tasksArray);
		}
		return Response.status(Status.OK).entity(exportJSON.toString()).header("Content-Disposition", "attachment; filename=\"" + getFileName() + "\"").build();
	}
	
	private String getFileName() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HHmm");
		String fileName = simpleDateFormat.format(System.currentTimeMillis()) + "_ezScrum_export.json";
		return fileName;
	}
}
