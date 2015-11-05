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
		JSONArray projects = null;
		// Sprints In Project
		Map<String, JSONArray> sprintsMap = new HashMap<String, JSONArray>();

		//// Get Accounts ////
		Response response = mClient.target(BASE_URL)
		        .path("accounts")
		        .request()
		        .get();
		JSONArray accounts = new JSONArray(response.readEntity(String.class));
		exportJSON.put(ExportJSONEnum.ACCOUNTS, accounts);

		//// Get Projects ////
		// Get Projects
		response = mClient.target(BASE_URL)
		        .path("projects")
		        .request()
		        .get();
		projects = new JSONArray(response.readEntity(String.class));
		exportJSON.put(ExportJSONEnum.PROJECTS, projects);

		for (int i = 0; i < projects.length(); i++) {
			JSONObject project = projects.getJSONObject(i);
			String projectName = project.getString(ProjectJSONEnum.NAME);
			/** Get scrum_roles in Project */
			response = mClient.target(BASE_URL)
			        .path("projects/" + projectName + "/scrumroles")
			        .request()
			        .get();
			JSONObject scrumRoles = new JSONObject(response.readEntity(String.class));
			project.put(ProjectJSONEnum.SCRUM_ROLES, scrumRoles);

			/** Get project_roles in Project */
			response = mClient.target(BASE_URL)
			        .path("projects/" + projectName + "/projectroles")
			        .request()
			        .get();
			JSONArray projectRoles = new JSONArray(response.readEntity(String.class));
			project.put(ProjectJSONEnum.PROJECT_ROLES, projectRoles);

			/** Get tags in Project */
			response = mClient.target(BASE_URL)
			        .path("projects/" + projectName + "/tags")
			        .request()
			        .get();
			JSONArray tags = new JSONArray(response.readEntity(String.class));
			project.put(ProjectJSONEnum.TAGS, tags);
		}

		//// Get Release ////
		for (int i = 0; i < projects.length(); i++) {
			JSONObject project = projects.getJSONObject(i);
			String projectName = project.getString(ProjectJSONEnum.NAME);
			// Get Sprints
			response = mClient.target(BASE_URL)
			        .path("projects/" + projectName + "/releases")
			        .request()
			        .get();
			JSONArray releases = new JSONArray(response.readEntity(String.class));
			project.put(ProjectJSONEnum.RELEASES, releases);
		}

		//// Get Sprints ////
		for (int i = 0; i < projects.length(); i++) {
			JSONObject project = projects.getJSONObject(i);
			String projectName = project.getString(ProjectJSONEnum.NAME);
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
			String projectName = project.getString(ProjectJSONEnum.NAME);
			JSONArray sprintsInProject = sprintsMap.get(projectName);
			project.put(ProjectJSONEnum.SPRINTS, sprintsInProject);
		}

		//// Get Stories In Sprint ////
		for (Map.Entry<String, JSONArray> sprintMap : sprintsMap.entrySet()) {
			String projectName = sprintMap.getKey();
			JSONArray sprintArray = sprintMap.getValue();

			// Get Stories from Sprint and add to Sprint
			for (int i = 0; i < sprintArray.length(); i++) {
				JSONObject sprintJSON = sprintArray.getJSONObject(i);
				long sprintId = sprintJSON.getLong(SprintJSONEnum.ID);
				response = mClient.target(BASE_URL)
				        .path("projects/" + projectName +
				                "/sprints/" + sprintId + "/stories")
				        .request()
				        .get();
				JSONArray storiesArray = new JSONArray(response.readEntity(String.class));
				sprintJSON.put(SprintJSONEnum.STORIES, storiesArray);

				//// Get attach_files in Story ////
				for (int j = 0; j < storiesArray.length(); j++) {
					JSONObject story = storiesArray.getJSONObject(j);
					long storyId = story.getLong(StoryJSONEnum.ID);
					response = mClient.target(BASE_URL)
					        .path("projects/" + projectName +
					                "/sprints/" + sprintId +
					                "/stories/" + storyId +
					                "/attachfiles")
					        .request()
					        .get();
					JSONArray atachfilesArray = new JSONArray(response.readEntity(String.class));
					story.put(StoryJSONEnum.ATTACH_FILES, atachfilesArray);
				}

				//// Get Tasks ////
				for (int j = 0; j < storiesArray.length(); j++) {
					JSONObject story = storiesArray.getJSONObject(j);
					long storyId = story.getLong(StoryJSONEnum.ID);
					response = mClient.target(BASE_URL)
					        .path("projects/" + projectName +
					                "/sprints/" + sprintId +
					                "/stories/" + storyId +
					                "/tasks")
					        .request()
					        .get();
					JSONArray tasksArray = new JSONArray(response.readEntity(String.class));
					story.put(StoryJSONEnum.TASKS, tasksArray);

					// Get attach_files in Task
					for (int k = 0; k < tasksArray.length(); k++) {
						JSONObject task = tasksArray.getJSONObject(k);
						long taskId = task.getLong(TaskJSONEnum.ID);
						response = mClient.target(BASE_URL)
						        .path("projects/" + projectName +
						                "/sprints/" + sprintId +
						                "/stories/" + storyId +
						                "/tasks/" + taskId +
						                "/attachfiles")
						        .request()
						        .get();
						JSONArray atachfilesArray = new JSONArray(response.readEntity(String.class));
						task.put(TaskJSONEnum.ATTACH_FILES, atachfilesArray);
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
				JSONArray retrospectivesArray = new JSONArray(response.readEntity(String.class));
				sprintJSON.put(SprintJSONEnum.RETROSPECTIVES, retrospectivesArray);

				// Get Unplans from Sprint and add to Sprint
				response = mClient.target(BASE_URL)
				        .path("projects/" + projectName +
				                "/sprints/" + sprintId +
				                "/unplans")
				        .request()
				        .get();
				JSONArray unplansArray = new JSONArray(response.readEntity(String.class));
				sprintJSON.put(SprintJSONEnum.UNPLANS, unplansArray);
			}
		}

		//// Get DroppedStories ////
		for (int i = 0; i < projects.length(); i++) {
			JSONObject project = projects.getJSONObject(i);
			String projectName = project.getString(ProjectJSONEnum.NAME);
			// Get stories
			response = mClient.target(BASE_URL)
			        .path("projects/" + projectName + "/stories")
			        .request()
			        .get();
			JSONArray droppedStoriesArray = new JSONArray(response.readEntity(String.class));
			project.put(ExportJSONEnum.DROPPED_STORIES, droppedStoriesArray);

			// Get Story
			for (int j = 0; j < droppedStoriesArray.length(); j++) {
				JSONObject droppedStory = droppedStoriesArray.getJSONObject(j);
				long droppedStoryId = droppedStory.getLong(StoryJSONEnum.ID);
				// Get attach_files
				response = mClient.target(BASE_URL)
				        .path("projects/" + projectName +
				                "/stories/" + droppedStoryId +
				                "/attachfiles")
				        .request()
				        .get();
				JSONArray attachfilesArray = new JSONArray(response.readEntity(String.class));
				droppedStory.put(StoryJSONEnum.ATTACH_FILES, attachfilesArray);
			}

			// Get Tasks in DroppedStory
			for (int j = 0; j < droppedStoriesArray.length(); j++) {
				JSONObject droppedStory = droppedStoriesArray.getJSONObject(j);
				long droppedStoryId = droppedStory.getLong(StoryJSONEnum.ID);
				response = mClient.target(BASE_URL)
				        .path("projects/" + projectName +
				                "/stories/" + droppedStoryId +
				                "/tasks")
				        .request()
				        .get();
				JSONArray tasksArray = new JSONArray(response.readEntity(String.class));
				droppedStory.put(StoryJSONEnum.TASKS, tasksArray);

				// Get Task
				for (int k = 0; k < tasksArray.length(); k++) {
					JSONObject task = tasksArray.getJSONObject(k);
					long taskId = task.getLong(TaskJSONEnum.ID);
					// Get attach_files
					response = mClient.target(BASE_URL)
					        .path("projects/" + projectName +
					                "/stories/" + droppedStoryId +
					                "/tasks/" + taskId +
					                "/attachfiles")
					        .request()
					        .get();
					JSONArray attachfilesArray = new JSONArray(response.readEntity(String.class));
					task.put(TaskJSONEnum.ATTACH_FILES, attachfilesArray);
				}
			}
		}

		//// Get DroppedTasks ////
		for (int i = 0; i < projects.length(); i++) {
			JSONObject project = projects.getJSONObject(i);
			String projectName = project.getString(ProjectJSONEnum.NAME);
			// Get Sprints
			response = mClient.target(BASE_URL)
			        .path("projects/" + projectName + "/tasks")
			        .request()
			        .get();
			JSONArray tasksArray = new JSONArray(response.readEntity(String.class));
			project.put(ExportJSONEnum.DROPPED_TASKS, tasksArray);

			// Get Task
			for (int k = 0; k < tasksArray.length(); k++) {
				JSONObject task = tasksArray.getJSONObject(k);
				long taskId = task.getLong(TaskJSONEnum.ID);
				// Get attach_files
				response = mClient.target(BASE_URL)
				        .path("projects/" + projectName +
				                "/tasks/" + taskId +
				                "/attachfiles")
				        .request()
				        .get();
				JSONArray attachfilesArray = new JSONArray(response.readEntity(String.class));
				task.put(TaskJSONEnum.ATTACH_FILES, attachfilesArray);
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
