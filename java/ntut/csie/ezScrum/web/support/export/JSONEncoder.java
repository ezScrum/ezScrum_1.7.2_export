package ntut.csie.ezScrum.web.support.export;

import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import ntut.csie.ezScrum.iteration.core.IStory;
import ntut.csie.ezScrum.web.databaseEnum.ProjectEnum;
import ntut.csie.ezScrum.web.databaseEnum.StoryEnum;
import ntut.csie.jcis.resource.core.IProject;

public class JSONEncoder {
	public static JSONArray toJSONArray(List<IProject> projects) throws JSONException {
		JSONArray projectJsonArray = new JSONArray();
		for (IProject project : projects) {
			projectJsonArray.put(toJSON(project));
		}
		return projectJsonArray;
	}

	// Translate project to JSON
	public static JSONObject toJSON(IProject project) throws JSONException {
		JSONObject projectJson = new JSONObject();
		projectJson.put(ProjectEnum.NAME, project.getProjectDesc().getName())
		        .put(ProjectEnum.DISPLAY_NAME, project.getProjectDesc().getDisplayName())
		        .put(ProjectEnum.COMMENT, project.getProjectDesc().getComment())
		        .put(ProjectEnum.PRODUCT_OWNER, project.getProjectDesc().getProjectManager())
		        .put(ProjectEnum.ATTATCH_MAX_SIZE, project.getProjectDesc().getAttachFileSize())
		        .put(ProjectEnum.CREATE_TIME, project.getProjectDesc().getCreateDate().getTime())
		        .put(ProjectEnum.UPDATE_TIME, System.currentTimeMillis());
		return projectJson;
	}
	
	public static JSONArray toStoryJSONArray(List<IStory> stories) throws JSONException {
		JSONArray storyJsonArray = new JSONArray();
		for (IStory story : stories) {
			storyJsonArray.put(toStoryJSON(story));
		}
		return storyJsonArray;
	}

	// Translate Story to JSON
	public static JSONObject toStoryJSON(IStory story) throws JSONException {
		JSONObject storyJson = new JSONObject();
		storyJson.put(StoryEnum.NAME, story.getName())
		         .put(StoryEnum.STATUS, story.getStatus())
		         .put(StoryEnum.ESTIMATE, Integer.parseInt(story.getEstimated()))
		         .put(StoryEnum.IMPORTANCE, Integer.parseInt(story.getImportance()))
		         .put(StoryEnum.VALUE, Integer.parseInt(story.getValue()))
		         .put(StoryEnum.NOTES, story.getNotes())
		         .put(StoryEnum.HOW_TO_DEMO, story.getHowToDemo());
		return storyJson;
	}
}
