package ntut.csie.ezScrum.web.support.export;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.web.databaseEnum.TaskEnum;

public class TaskFluent {
	
	// IIssue = Task or Story
	private ArrayList<IIssue> mTasks;
	
	public TaskFluent(){
		mTasks = new ArrayList<IIssue>();
	}
	
	// Get a Task
	public TaskFluent Get(IIssue task){
		mTasks.add(task);
		return this;
	}
	
	// Get Tasks
	public TaskFluent Get(List<IIssue> tasks){
		mTasks.addAll(tasks);
		return this;
	}
	
	// Translate multiple tasks to JSON
	public JSONObject toJSON(IIssue task) throws JSONException {
		JSONObject taskJson = new JSONObject();
		taskJson.put(TaskEnum.NAME, task.getSummary())
				.put(TaskEnum.ID, task.getIssueID())
				.put(TaskEnum.HANDLER, task.getAssignto())
				.put(TaskEnum.ESTIMATE, task.getEstimated())
				.put(TaskEnum.REMAIN, task.getRemains())
				.put(TaskEnum.ACTUAL, task.getActualHour())
				.put(TaskEnum.NOTES, task.getNotes())
				.put(TaskEnum.STATUS, task.getStatus())
				.put(TaskEnum.PROJECT_ID, task.getProjectID())
				.put(TaskEnum.CREATE_TIME, task.getSubmittedDate())
				.put(TaskEnum.UPDATE_TIME, task.getLastUpdate().getTime());
		return taskJson;
	}
}
