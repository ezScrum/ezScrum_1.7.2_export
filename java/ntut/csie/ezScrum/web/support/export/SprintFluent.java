package ntut.csie.ezScrum.web.support.export;

import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import ntut.csie.ezScrum.iteration.core.ISprintPlanDesc;
import ntut.csie.ezScrum.web.databaseEnum.SprintEnum;

public class SprintFluent {
	// Translate multiple sprint to JSON
	public static JSONObject toJSON(List<ISprintPlanDesc> sprints) throws JSONException {
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
}