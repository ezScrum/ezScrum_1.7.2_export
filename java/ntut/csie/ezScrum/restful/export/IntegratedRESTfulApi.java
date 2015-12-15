package ntut.csie.ezScrum.restful.export;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.POST;
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

	@POST
	@Path("/projects")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProjectsExportJSON(String entity) throws JSONException {
		// Get Client
		mClient = ClientBuilder.newClient();
		// Selected Project JSONArray
		JSONArray selectedProjectJSONArray = new JSONArray();
		// Response JSONObject
		JSONObject exportJSON = new JSONObject();
		// Projects JSONArray
		JSONArray projectJSONArray = new JSONArray();
		// Sprints In Project
		Map<String, JSONArray> sprintsMap = new HashMap<String, JSONArray>();

		try {
			// Get Selected Project
			selectedProjectJSONArray = new JSONArray(entity);
			//// Get Accounts ////
			Response response = mClient.target(BASE_URL)
			        .path("accounts")
			        .request()
			        .get();
			JSONArray accountJSONArray = new JSONArray();
			if (getStatus(response)) {
				String responseString = response.readEntity(String.class);
				accountJSONArray = new JSONArray(responseString);
			}
			exportJSON.put(ExportJSONEnum.ACCOUNTS, accountJSONArray);

			//// Get Projects ////
			// Get Projects
			response = mClient.target(BASE_URL)
			        .path("projects")
			        .request()
			        .get();
			JSONArray tempALLProjectJSONArray = new JSONArray();
			if (getStatus(response)) {
				String responseString = response.readEntity(String.class);
				tempALLProjectJSONArray = new JSONArray(responseString);
			}
			for (int i = 0; i < tempALLProjectJSONArray.length(); i++) {
				JSONObject projectJSON = tempALLProjectJSONArray.getJSONObject(i);
				String projectName = projectJSON.getString(ProjectJSONEnum.NAME);
				for (int j = 0; j < selectedProjectJSONArray.length(); j++) {
					JSONObject selectedProjectJSON = selectedProjectJSONArray.getJSONObject(j);
					String selectedProjectName = selectedProjectJSON.getString(ProjectJSONEnum.NAME);
					if (selectedProjectName.equals(projectName)) {
						projectJSONArray.put(projectJSON);
					}
				}
			}
			exportJSON.put(ExportJSONEnum.PROJECTS, projectJSONArray);

			for (int i = 0; i < projectJSONArray.length(); i++) {
				JSONObject projectJSON = projectJSONArray.getJSONObject(i);
				String projectName = projectJSON.getString(ProjectJSONEnum.NAME);
				/** Get scrum_roles in Project */
				response = mClient.target(BASE_URL)
				        .path("projects/" + projectName + "/scrumroles")
				        .request()
				        .get();
				JSONObject scrumRoleJSON = null;
				if (getStatus(response)) {
					String responseString = response.readEntity(String.class);
					scrumRoleJSON = new JSONObject(responseString);
					projectJSON.put(ProjectJSONEnum.SCRUM_ROLES, scrumRoleJSON);
				}

				/** Get project_roles in Project */
				response = mClient.target(BASE_URL)
				        .path("projects/" + projectName + "/projectroles")
				        .request()
				        .get();
				JSONArray projectRoleJSONArray = new JSONArray();
				if (getStatus(response)) {
					String responseString = response.readEntity(String.class);
					projectRoleJSONArray = new JSONArray(responseString);
				}
				projectJSON.put(ProjectJSONEnum.PROJECT_ROLES, projectRoleJSONArray);

				/** Get tags in Project */
				response = mClient.target(BASE_URL)
				        .path("projects/" + projectName + "/tags")
				        .request()
				        .get();
				JSONArray tagJSONArray = new JSONArray();
				if (getStatus(response)) {
					String responseString = response.readEntity(String.class);
				    tagJSONArray = new JSONArray(responseString);
				}
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
				JSONArray releaseJSONArray = new JSONArray();
				if (getStatus(response)) {
					String responseString = response.readEntity(String.class);
					releaseJSONArray = new JSONArray(responseString);
				}
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
				JSONArray sprintJSONArray = new JSONArray();
				if (getStatus(response)) {
					String responseString = response.readEntity(String.class);
					sprintJSONArray = new JSONArray(responseString);
					sprintsMap.put(projectName, sprintJSONArray);
				}
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
					JSONArray storyJSONArray = new JSONArray();
					if (getStatus(response)) {
						String responseString = response.readEntity(String.class);
						storyJSONArray = new JSONArray(responseString);
					}
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
						JSONArray historyJSONArray = new JSONArray();
						if (getStatus(response)) {
							String responseString = response.readEntity(String.class);
							historyJSONArray = new JSONArray(responseString);
						}
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
						JSONArray atachfileJSONArray = new JSONArray();
						if (getStatus(response)) {
							String responseString = response.readEntity(String.class);
							atachfileJSONArray = new JSONArray(responseString);
						}
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
						JSONArray taskJSONArray = new JSONArray();
						if (getStatus(response)) {
							String responseString = response.readEntity(String.class);
							taskJSONArray = new JSONArray(responseString);
						}
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
							JSONArray historyJSONArray = new JSONArray();
							if (getStatus(response)) {
								String responseString = response.readEntity(String.class);
								historyJSONArray = new JSONArray(responseString);
							}
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
							JSONArray atachfileJSONArray = new JSONArray();
							if (getStatus(response)) {
								String responseString = response.readEntity(String.class);
								atachfileJSONArray = new JSONArray(responseString);
							}
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
					JSONArray retrospectiveJSONArray = new JSONArray();
					if (getStatus(response)) {
						String responseString = response.readEntity(String.class);
						retrospectiveJSONArray = new JSONArray(responseString);
					}
					sprintJSON.put(SprintJSONEnum.RETROSPECTIVES, retrospectiveJSONArray);

					// Get Unplans from Sprint and add to Sprint
					response = mClient.target(BASE_URL)
					        .path("projects/" + projectName +
					                "/sprints/" + sprintId +
					                "/unplans")
					        .request()
					        .get();
					JSONArray unplanJSONArray = new JSONArray();
					if (getStatus(response)) {
						String responseString = response.readEntity(String.class);
						 unplanJSONArray = new JSONArray(responseString);
					}
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
						JSONArray historyJSONArray = new JSONArray();
						if (getStatus(response)) {
							String responseString = response.readEntity(String.class);
							historyJSONArray = new JSONArray(responseString);
						}
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
				JSONArray droppedStoryJSONArray = new JSONArray();
				if (getStatus(response)) {
					String responseString = response.readEntity(String.class);
					droppedStoryJSONArray = new JSONArray(responseString);
				}
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
					JSONArray historyJSONArray = new JSONArray();
					if (getStatus(response)) {
						String responseString = response.readEntity(String.class);
						historyJSONArray = new JSONArray(responseString);
					}
					droppedStoryJSON.put(StoryJSONEnum.HISTORIES, historyJSONArray);

					// Get attach_files
					response = mClient.target(BASE_URL)
					        .path("projects/" + projectName +
					                "/stories/" + droppedStoryId +
					                "/attachfiles")
					        .request()
					        .get();
					JSONArray attachfileJSONArray = new JSONArray();
					if (getStatus(response)) {
						String responseString = response.readEntity(String.class);
						attachfileJSONArray = new JSONArray(responseString);
					}
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
					JSONArray taskJSONArray = new JSONArray();
					if (getStatus(response)) {
						String responseString = response.readEntity(String.class);
						taskJSONArray = new JSONArray(responseString);
					}
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
						JSONArray historyJSONArray = new JSONArray();
						if (getStatus(response)) {
							String responseString = response.readEntity(String.class);
							historyJSONArray = new JSONArray(responseString);
						}
						taskJSON.put(TaskJSONEnum.HISTORIES, historyJSONArray);

						// Get attach_files
						response = mClient.target(BASE_URL)
						        .path("projects/" + projectName +
						                "/stories/" + droppedStoryId +
						                "/tasks/" + taskId +
						                "/attachfiles")
						        .request()
						        .get();
						JSONArray attachfileJSONArray = new JSONArray();
						if (getStatus(response)) {
							String responseString = response.readEntity(String.class);
							attachfileJSONArray = new JSONArray(responseString);
						}
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
				JSONArray taskJSONArray = new JSONArray();
				if (getStatus(response)) {
					String responseString = response.readEntity(String.class);
					taskJSONArray = new JSONArray(responseString);
				}
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
					JSONArray historyJSONArray = new JSONArray();
					if (getStatus(response)) {
						String responseString = response.readEntity(String.class);
						historyJSONArray = new JSONArray(responseString);
					}
					taskJSON.put(TaskJSONEnum.HISTORIES, historyJSONArray);

					// Get attach_files
					response = mClient.target(BASE_URL)
					        .path("projects/" + projectName +
					                "/tasks/" + taskId +
					                "/attachfiles")
					        .request()
					        .get();
					JSONArray attachfileJSONArray = new JSONArray();
					if (getStatus(response)) {
						String responseString = response.readEntity(String.class);
						attachfileJSONArray = new JSONArray(responseString);
					}
					taskJSON.put(TaskJSONEnum.ATTACH_FILES, attachfileJSONArray);
				}
			}
		} catch (JSONException e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		return Response.status(Status.OK).entity(exportJSON.toString()).header("Content-Disposition", "attachment; filename=\"" + getFileName() + "\"").build();
	}

	private String getFileName() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HHmm");
		String fileName = simpleDateFormat.format(System.currentTimeMillis()) + "_ezScrum_export.json";
		return fileName;
	}
	
	private boolean getStatus(Response response) {
		if (response.getStatus() == Response.Status.OK.getStatusCode()) {
			return true;
		}
		return false;
	}
}
