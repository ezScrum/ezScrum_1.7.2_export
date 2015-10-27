package ntut.csie.ezScrum.web.support.export;

import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.web.databaseEnum.ProjectEnum;
import ntut.csie.ezScrum.iteration.core.ISprintPlanDesc;
import ntut.csie.ezScrum.web.databaseEnum.StoryEnum;
import ntut.csie.ezScrum.web.databaseEnum.TaskEnum;
import ntut.csie.ezScrum.web.databaseEnum.SprintEnum;
import ntut.csie.jcis.resource.core.IProject;

public class JSONEncoder {
	// Translate multiple sprint to JSON
	public static JSONArray toSprintJSONArray(List<ISprintPlanDesc> sprints) {
		JSONArray sprintJsonArray = new JSONArray();
		for (ISprintPlanDesc sprint : sprints) {
			sprintJsonArray.put(toSprintJSON(sprint));
		}
		return sprintJsonArray;
	}

	// Translate sprint to JSON
	public static JSONObject toSprintJSON(ISprintPlanDesc sprint) {
		JSONObject sprintJson = new JSONObject();
		try {
			sprintJson.put(SprintEnum.ID, Long.parseLong(sprint.getID()))
			          .put(SprintEnum.GOAL, sprint.getGoal())
			          .put(SprintEnum.INTERVAL, Integer.parseInt(sprint.getInterval()))
			          .put(SprintEnum.TEAM_SIZE, Integer.parseInt(sprint.getMemberNumber()))
			          .put(SprintEnum.AVAILABLE_HOURS, Integer.parseInt(sprint.getAvailableDays()))
			          .put(SprintEnum.FOCUS_FACTOR, Integer.parseInt(sprint.getFocusFactor()))
			          .put(SprintEnum.START_DATE, sprint.getStartDate())
			          .put(SprintEnum.DUE_DATE, sprint.getEndDate())
			          .put(SprintEnum.DEMO_DATE, sprint.getDemoDate())
			          .put(SprintEnum.DEMO_PLACE, sprint.getDemoPlace())
			          .put(SprintEnum.DAILY_INFO, sprint.getNotes());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return sprintJson;
	}

	public static JSONArray toProjectJSONArray(List<IProject> projects) {
		JSONArray projectJsonArray = new JSONArray();
		for (IProject project : projects) {
			projectJsonArray.put(toProjectJSON(project));
		}
		return projectJsonArray;
	}

	// Translate project to JSON
	public static JSONObject toProjectJSON(IProject project) {
		JSONObject projectJson = new JSONObject();
		try {
			projectJson.put(ProjectEnum.NAME, project.getName())
			           .put(ProjectEnum.DISPLAY_NAME, project.getProjectDesc().getDisplayName())
			           .put(ProjectEnum.COMMENT, project.getProjectDesc().getComment())
			           .put(ProjectEnum.PRODUCT_OWNER, project.getProjectDesc().getProjectManager())
			           .put(ProjectEnum.ATTATCH_MAX_SIZE, Long.parseLong(project.getProjectDesc().getAttachFileSize()));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return projectJson;
	}

	public static JSONArray toStoryJSONArray(List<IIssue> stories) {
		JSONArray storyJsonArray = new JSONArray();
		for (IIssue story : stories) {
			storyJsonArray.put(toStoryJSON(story));
		}
		return storyJsonArray;
	}

	// Translate Story to JSON
	public static JSONObject toStoryJSON(IIssue story) {
		JSONObject storyJson = new JSONObject();
		try {
			storyJson.put(StoryEnum.ID, story.getIssueID())
			         .put(StoryEnum.NAME, story.getSummary())
			         .put(StoryEnum.STATUS, story.getStatus())
			         .put(StoryEnum.ESTIMATE, Integer.parseInt(story.getEstimated()))
			         .put(StoryEnum.IMPORTANCE, Integer.parseInt(story.getImportance()))
			         .put(StoryEnum.VALUE, Integer.parseInt(story.getValue()))
			         .put(StoryEnum.NOTES, story.getNotes())
			         .put(StoryEnum.HOW_TO_DEMO, story.getHowToDemo());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return storyJson;
	}
	
	public static JSONArray toTaskJSONArray(List<IIssue> tasks) {
		JSONArray taskJsonArray = new JSONArray();
		for (IIssue task : tasks) {
			taskJsonArray.put(toTaskJSON(task));
		}
		return taskJsonArray;
	}

	public static JSONObject toTaskJSON(IIssue task) {
		JSONObject taskJson = new JSONObject();
		try {
			taskJson.put(TaskEnum.NAME, task.getSummary())
			        .put(TaskEnum.HANDLER, task.getAssignto())
					.put(TaskEnum.ESTIMATE, Integer.parseInt(task.getEstimated()))
					.put(TaskEnum.REMAIN, Integer.parseInt(task.getRemains()))
					.put(TaskEnum.ACTUAL, Integer.parseInt(task.getActualHour()))
					.put(TaskEnum.NOTES, task.getNotes())
					.put(TaskEnum.STATUS, task.getStatus());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return taskJson;
	}
}