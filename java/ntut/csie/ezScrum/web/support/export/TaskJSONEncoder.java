package ntut.csie.ezScrum.web.support.export;

import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.web.databaseEnum.TaskEnum;

public class TaskJSONEncoder {
	public static JSONArray toJSONArray(List<IIssue> tasks) throws JSONException {
		JSONArray taskJsonArray = new JSONArray();
		for (IIssue task : tasks) {
			taskJsonArray.put(toJSON(task));
		}
		return taskJsonArray;
	}

	public static JSONObject toJSON(IIssue task) throws JSONException {
		JSONObject taskJson = new JSONObject();
		taskJson.put(TaskEnum.ID, task.getIssueID()).put(TaskEnum.NAME, task.getSummary())
				.put(TaskEnum.HANDLER, task.getAssignto()).put(TaskEnum.ESTIMATE, task.getEstimated())
				.put(TaskEnum.REMAIN, task.getRemains()).put(TaskEnum.ACTUAL, task.getActualHour())
				.put(TaskEnum.NOTES, task.getNotes()).put(TaskEnum.STATUS, task.getStatus())
				.put(TaskEnum.CREATE_TIME, task.getSubmittedDate())
				.put(TaskEnum.UPDATE_TIME, task.getLastUpdate().getTime());
		return taskJson;
	}
}
