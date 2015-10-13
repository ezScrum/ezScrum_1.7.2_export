package ntut.csie.ezScrum.web.support.export;

import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import ntut.csie.ezScrum.iteration.core.ISprintPlanDesc;
import ntut.csie.ezScrum.web.databaseEnum.SprintEnum;
import ntut.csie.ezScrum.web.databaseEnum.ProjectEnum;
import ntut.csie.jcis.resource.core.IProject;

public class JSONEncoder {
	// Translate multiple sprint to JSON
		public static JSONObject toJSONArray(List<ISprintPlanDesc> sprints) throws JSONException {
			JSONObject wholeJson = new JSONObject();
			if (sprints.isEmpty()) {
				return wholeJson;
			} else {
				JSONArray sprintJsonArray = new JSONArray();
				for (ISprintPlanDesc sprint : sprints) {
					sprintJsonArray.put(toJSON(sprint));
				}
				wholeJson.put("sprints", sprintJsonArray);
			}
			return wholeJson;
		}

		// Translate sprint to JSON
		public static JSONObject toJSON(ISprintPlanDesc sprint) throws JSONException {
			JSONObject sprintJson = new JSONObject();
			sprintJson.put(SprintEnum.GOAL, sprint.getGoal())
				      .put(SprintEnum.INTERVAL, sprint.getInterval())
					  .put(SprintEnum.TEAM_SIZE, sprint.getMemberNumber())
					  .put(SprintEnum.AVAILABLE_HOURS, sprint.getAvailableDays())
					  .put(SprintEnum.FOCUS_FACTOR, sprint.getFocusFactor())
					  .put(SprintEnum.START_DATE, sprint.getStartDate())
					  .put(SprintEnum.DUE_DATE, sprint.getEndDate())
					  .put(SprintEnum.DEMO_DATE, sprint.getDemoDate())
					  .put(SprintEnum.DEMO_PLACE, sprint.getDemoPlace())
					  .put(SprintEnum.DAILY_INFO, sprint.getNotes())
					  .put(SprintEnum.CREATE_TIME, System.currentTimeMillis())
					  .put(SprintEnum.UPDATE_TIME, System.currentTimeMillis());
			return sprintJson;
		}

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
