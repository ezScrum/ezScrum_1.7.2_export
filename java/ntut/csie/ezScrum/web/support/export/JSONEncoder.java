package ntut.csie.ezScrum.web.support.export;

import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import ntut.csie.ezScrum.web.databaseEnum.ProjectEnum;
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
}
