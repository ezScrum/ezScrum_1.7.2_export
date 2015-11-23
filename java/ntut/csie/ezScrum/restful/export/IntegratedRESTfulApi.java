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

import ntut.csie.ezScrum.restful.export.jsonEnum.ExportJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.ProjectJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.SprintJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.StoryJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.TaskJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.UnplanJSONEnum;

@Path("export")
public class IntegratedRESTfulApi {
	private Client mClient;
	private static String BASE_URL = "http://localhost:8080/ezScrum/resource/";

	@GET
	@Path("/projects")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProjectsExportJSON() throws JSONException {
		// Get Client
		mClient = ClientBuilder.newClient();
		// Response JSONObject
		JSONObject exportJSON = new JSONObject();
		// Projects JSONArray
		JSONArray projectJSONArray = null;
		// Sprints In Project
		Map<String, JSONArray> sprintsMap = new HashMap<String, JSONArray>();

		//// Get Accounts ////
		Response response = mClient.target(BASE_URL)
		        .path("accounts")
		        .request()
		        .get();
		JSONArray accountJSONArray = new JSONArray(response.readEntity(String.class));
		exportJSON.put(ExportJSONEnum.ACCOUNTS, accountJSONArray);

		//// Get Projects ////
		// Get Projects
		response = mClient.target(BASE_URL)
		        .path("projects")
		        .request()
		        .get();
		projectJSONArray = new JSONArray(response.readEntity(String.class));
		exportJSON.put(ExportJSONEnum.PROJECTS, projectJSONArray);

		for (int i = 0; i < projectJSONArray.length(); i++) {
			JSONObject projectJSON = projectJSONArray.getJSONObject(i);
			String projectName = projectJSON.getString(ProjectJSONEnum.NAME);
			/** Get scrum_roles in Project */
			response = mClient.target(BASE_URL)
			        .path("projects/" + projectName + "/scrumroles")
			        .request()
			        .get();
			JSONObject scrumRoleJSON = new JSONObject(response.readEntity(String.class));
			projectJSON.put(ProjectJSONEnum.SCRUM_ROLES, scrumRoleJSON);

			/** Get project_roles in Project */
			response = mClient.target(BASE_URL)
			        .path("projects/" + projectName + "/projectroles")
			        .request()
			        .get();
			JSONArray projectRoleJSONArray = new JSONArray(response.readEntity(String.class));
			projectJSON.put(ProjectJSONEnum.PROJECT_ROLES, projectRoleJSONArray);

			/** Get tags in Project */
			response = mClient.target(BASE_URL)
			        .path("projects/" + projectName + "/tags")
			        .request()
			        .get();
			JSONArray tagJSONArray = new JSONArray(response.readEntity(String.class));
			projectJSON.put(ProjectJSONEnum.TAGS, tagJSONArray);
		}

		//// Get Release ////
		for (int i = 0; i < projectJSONArray.length(); i++) {
			JSONObject projectJSON = projectJSONArray.getJSONObject(i);
			String projectName = projectJSON.getString(ProjectJSONEnum.NAME);
			// Get Sprints
			response = mClient.target(BASE_URL)
			        .path("projects/" + projectName + "/releases")
			        .request()
			        .get();
			JSONArray releaseJSONArray = new JSONArray(response.readEntity(String.class));
			projectJSON.put(ProjectJSONEnum.RELEASES, releaseJSONArray);
		}

		//// Get Sprints ////
		for (int i = 0; i < projectJSONArray.length(); i++) {
			JSONObject projectJSON = projectJSONArray.getJSONObject(i);
			String projectName = projectJSON.getString(ProjectJSONEnum.NAME);
			// Get Sprints
			response = mClient.target(BASE_URL)
			        .path("projects/" + projectName + "/sprints")
			        .request()
			        .get();
			JSONArray sprintJSONArray = new JSONArray(response.readEntity(String.class));
			sprintsMap.put(projectName, sprintJSONArray);
		}
		// Add Sprints to Project
		for (int i = 0; i < projectJSONArray.length(); i++) {
			JSONObject projectJSON = projectJSONArray.getJSONObject(i);
			String projectName = projectJSON.getString(ProjectJSONEnum.NAME);
			JSONArray sprintInProjectJSONArray = sprintsMap.get(projectName);
			projectJSON.put(ProjectJSONEnum.SPRINTS, sprintInProjectJSONArray);
		}

		//// Get Stories In Sprint ////
		for (Map.Entry<String, JSONArray> sprintMap : sprintsMap.entrySet()) {
			String projectName = sprintMap.getKey();
			JSONArray sprintJSONArray = sprintMap.getValue();

			// Get Stories from Sprint and add to Sprint
			for (int i = 0; i < sprintJSONArray.length(); i++) {
				JSONObject sprintJSON = sprintJSONArray.getJSONObject(i);
				long sprintId = sprintJSON.getLong(SprintJSONEnum.ID);
				response = mClient.target(BASE_URL)
				        .path("projects/" + projectName +
				                "/sprints/" + sprintId + "/stories")
				        .request()
				        .get();
				JSONArray storyJSONArray = new JSONArray(response.readEntity(String.class));
				sprintJSON.put(SprintJSONEnum.STORIES, storyJSONArray);

				//// Get histories in Story ////
				for (int j = 0; j < storyJSONArray.length(); j++) {
					JSONObject storyJSON = storyJSONArray.getJSONObject(j);
					long storyId = storyJSON.getLong(StoryJSONEnum.ID);
					response = mClient.target(BASE_URL)
					        .path("projects/" + projectName +
					                "/sprints/" + sprintId +
					                "/stories/" + storyId +
					                "/histories")
					        .request()
					        .get();
					JSONArray historyJSONArray = new JSONArray(response.readEntity(String.class));
					storyJSON.put(StoryJSONEnum.HISTORIES, historyJSONArray);
				}

				//// Get attach_files in Story ////
				for (int j = 0; j < storyJSONArray.length(); j++) {
					JSONObject storyJSON = storyJSONArray.getJSONObject(j);
					long storyId = storyJSON.getLong(StoryJSONEnum.ID);
					response = mClient.target(BASE_URL)
					        .path("projects/" + projectName +
					                "/sprints/" + sprintId +
					                "/stories/" + storyId +
					                "/attachfiles")
					        .request()
					        .get();
					JSONArray atachfileJSONArray = new JSONArray(response.readEntity(String.class));
					storyJSON.put(StoryJSONEnum.ATTACH_FILES, atachfileJSONArray);
				}

				//// Get Tasks ////
				for (int j = 0; j < storyJSONArray.length(); j++) {
					JSONObject storyJSON = storyJSONArray.getJSONObject(j);
					long storyId = storyJSON.getLong(StoryJSONEnum.ID);
					response = mClient.target(BASE_URL)
					        .path("projects/" + projectName +
					                "/sprints/" + sprintId +
					                "/stories/" + storyId +
					                "/tasks")
					        .request()
					        .get();
					JSONArray taskJSONArray = new JSONArray(response.readEntity(String.class));
					storyJSON.put(StoryJSONEnum.TASKS, taskJSONArray);
					
					// Get histories in Task
					for (int k = 0; k < taskJSONArray.length(); k++) {
						JSONObject taskJSON = taskJSONArray.getJSONObject(k);
						long taskId = taskJSON.getLong(TaskJSONEnum.ID);
						response = mClient.target(BASE_URL)
						        .path("projects/" + projectName +
						                "/sprints/" + sprintId +
						                "/stories/" + storyId +
						                "/tasks/" + taskId +
						                "/histories")
						        .request()
						        .get();
						JSONArray historyJSONArray = new JSONArray(response.readEntity(String.class));
						taskJSON.put(TaskJSONEnum.HISTORIES, historyJSONArray);
					}

					// Get attach_files in Task
					for (int k = 0; k < taskJSONArray.length(); k++) {
						JSONObject taskJSON = taskJSONArray.getJSONObject(k);
						long taskId = taskJSON.getLong(TaskJSONEnum.ID);
						response = mClient.target(BASE_URL)
						        .path("projects/" + projectName +
						                "/sprints/" + sprintId +
						                "/stories/" + storyId +
						                "/tasks/" + taskId +
						                "/attachfiles")
						        .request()
						        .get();
						JSONArray atachfileJSONArray = new JSONArray(response.readEntity(String.class));
						taskJSON.put(TaskJSONEnum.ATTACH_FILES, atachfileJSONArray);
					}
				}
			}
		}

		//// Get Retrospectives & Unplans In Sprint ////
		for (Map.Entry<String, JSONArray> sprintMap : sprintsMap.entrySet()) {
			String projectName = sprintMap.getKey();
			JSONArray sprintArray = sprintMap.getValue();

			for (int i = 0; i < sprintArray.length(); i++) {
				JSONObject sprintJSON = sprintArray.getJSONObject(i);
				long sprintId = sprintJSON.getLong(SprintJSONEnum.ID);
				// Get Retrospectives from Sprint and add to Sprint
				response = mClient.target(BASE_URL)
				        .path("projects/" + projectName +
				                "/sprints/" + sprintId +
				                "/retrospectives")
				        .request()
				        .get();
				JSONArray retrospectiveJSONArray = new JSONArray(response.readEntity(String.class));
				sprintJSON.put(SprintJSONEnum.RETROSPECTIVES, retrospectiveJSONArray);

				// Get Unplans from Sprint and add to Sprint
				response = mClient.target(BASE_URL)
				        .path("projects/" + projectName +
				                "/sprints/" + sprintId +
				                "/unplans")
				        .request()
				        .get();
				JSONArray unplanJSONArray = new JSONArray(response.readEntity(String.class));
				sprintJSON.put(SprintJSONEnum.UNPLANS, unplanJSONArray);
				
				// Get histories in Unplan
				for (int j = 0; j < unplanJSONArray.length(); j++) {
					JSONObject unplanJSON = unplanJSONArray.getJSONObject(j);
					long unplanId = unplanJSON.getLong(TaskJSONEnum.ID);
					response = mClient.target(BASE_URL)
					        .path("projects/" + projectName +
					                "/sprints/" + sprintId +
					                "/unplans/" + unplanId +
					                "/histories")
					        .request()
					        .get();
					JSONArray historyJSONArray = new JSONArray(response.readEntity(String.class));
					unplanJSON.put(UnplanJSONEnum.HISTORIES, historyJSONArray);
				}
			}
		}

		//// Get DroppedStories ////
		for (int i = 0; i < projectJSONArray.length(); i++) {
			JSONObject projectJSON = projectJSONArray.getJSONObject(i);
			String projectName = projectJSON.getString(ProjectJSONEnum.NAME);
			// Get stories
			response = mClient.target(BASE_URL)
			        .path("projects/" + projectName + "/stories")
			        .request()
			        .get();
			JSONArray droppedStoryJSONArray = new JSONArray(response.readEntity(String.class));
			projectJSON.put(ExportJSONEnum.DROPPED_STORIES, droppedStoryJSONArray);

			// Get Story
			for (int j = 0; j < droppedStoryJSONArray.length(); j++) {
				JSONObject droppedStoryJSON = droppedStoryJSONArray.getJSONObject(j);
				long droppedStoryId = droppedStoryJSON.getLong(StoryJSONEnum.ID);
				// Get histories
				response = mClient.target(BASE_URL)
				        .path("projects/" + projectName +
				                "/stories/" + droppedStoryId +
				                "/histories")
				        .request()
				        .get();
				JSONArray historyJSONArray = new JSONArray(response.readEntity(String.class));
				droppedStoryJSON.put(StoryJSONEnum.HISTORIES, historyJSONArray);
				
				// Get attach_files
				response = mClient.target(BASE_URL)
				        .path("projects/" + projectName +
				                "/stories/" + droppedStoryId +
				                "/attachfiles")
				        .request()
				        .get();
				JSONArray attachfileJSONArray = new JSONArray(response.readEntity(String.class));
				droppedStoryJSON.put(StoryJSONEnum.ATTACH_FILES, attachfileJSONArray);
			}

			// Get Tasks in DroppedStory
			for (int j = 0; j < droppedStoryJSONArray.length(); j++) {
				JSONObject droppedStoryJSON = droppedStoryJSONArray.getJSONObject(j);
				long droppedStoryId = droppedStoryJSON.getLong(StoryJSONEnum.ID);
				response = mClient.target(BASE_URL)
				        .path("projects/" + projectName +
				                "/stories/" + droppedStoryId +
				                "/tasks")
				        .request()
				        .get();
				JSONArray taskJSONArray = new JSONArray(response.readEntity(String.class));
				droppedStoryJSON.put(StoryJSONEnum.TASKS, taskJSONArray);

				// Get Task
				for (int k = 0; k < taskJSONArray.length(); k++) {
					JSONObject taskJSON = taskJSONArray.getJSONObject(k);
					long taskId = taskJSON.getLong(TaskJSONEnum.ID);
					// Get histories
					response = mClient.target(BASE_URL)
					        .path("projects/" + projectName +
					                "/stories/" + droppedStoryId +
					                "/tasks/" + taskId +
					                "/histories")
					        .request()
					        .get();
					JSONArray historyJSONArray = new JSONArray(response.readEntity(String.class));
					taskJSON.put(TaskJSONEnum.HISTORIES, historyJSONArray);
					
					// Get attach_files
					response = mClient.target(BASE_URL)
					        .path("projects/" + projectName +
					                "/stories/" + droppedStoryId +
					                "/tasks/" + taskId +
					                "/attachfiles")
					        .request()
					        .get();
					JSONArray attachfileJSONArray = new JSONArray(response.readEntity(String.class));
					taskJSON.put(TaskJSONEnum.ATTACH_FILES, attachfileJSONArray);
				}
			}
		}

		//// Get DroppedTasks ////
		for (int i = 0; i < projectJSONArray.length(); i++) {
			JSONObject projectJSON = projectJSONArray.getJSONObject(i);
			String projectName = projectJSON.getString(ProjectJSONEnum.NAME);
			// Get Sprints
			response = mClient.target(BASE_URL)
			        .path("projects/" + projectName + 
			        	  "/tasks")
			        .request()
			        .get();
			JSONArray taskJSONArray = new JSONArray(response.readEntity(String.class));
			projectJSON.put(ExportJSONEnum.DROPPED_TASKS, taskJSONArray);

			// Get Task
			for (int k = 0; k < taskJSONArray.length(); k++) {
				JSONObject taskJSON = taskJSONArray.getJSONObject(k);
				long taskId = taskJSON.getLong(TaskJSONEnum.ID);
				// Get histories
				response = mClient.target(BASE_URL)
				        .path("projects/" + projectName +
				                "/tasks/" + taskId +
				                "/histories")
				        .request()
				        .get();
				JSONArray historyJSONArray = new JSONArray(response.readEntity(String.class));
				taskJSON.put(TaskJSONEnum.HISTORIES, historyJSONArray);
				
				// Get attach_files
				response = mClient.target(BASE_URL)
				        .path("projects/" + projectName +
				                "/tasks/" + taskId +
				                "/attachfiles")
				        .request()
				        .get();
				JSONArray attachfileJSONArray = new JSONArray(response.readEntity(String.class));
				taskJSON.put(TaskJSONEnum.ATTACH_FILES, attachfileJSONArray);
			}
		}
		return Response.status(Status.OK).entity(exportJSON.toString()).header("Content-Disposition", "attachment; filename=\"" + getFileName() + "\"").build();
	}

	private String getFileName() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HHmm");
		String fileName = simpleDateFormat.format(System.currentTimeMillis()) + "_ezScrum_export.json";
		return fileName;
	}
}
