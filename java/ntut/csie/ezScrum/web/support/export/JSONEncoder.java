package ntut.csie.ezScrum.web.support.export;

import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.iteration.core.IReleasePlanDesc;
import ntut.csie.ezScrum.iteration.core.IScrumIssue;
import ntut.csie.ezScrum.iteration.core.ISprintPlanDesc;
import ntut.csie.ezScrum.web.databaseEnum.AccountEnum;
import ntut.csie.ezScrum.web.databaseEnum.ProjectEnum;
import ntut.csie.ezScrum.web.databaseEnum.ReleaseEnum;
import ntut.csie.ezScrum.web.databaseEnum.RetrospectiveEnum;
import ntut.csie.ezScrum.web.databaseEnum.SprintEnum;
import ntut.csie.ezScrum.web.databaseEnum.StoryEnum;
import ntut.csie.ezScrum.web.databaseEnum.TaskEnum;
import ntut.csie.jcis.account.core.IAccount;
import ntut.csie.jcis.resource.core.IProject;

public class JSONEncoder {
	// Translate multiple account to JSONArray
	public static JSONArray toAccountJSONArray(List<IAccount> accounts) {
		JSONArray accountJsonArray = new JSONArray();
		for (IAccount account : accounts) {
			accountJsonArray.put(toAccountJSON(account));
		}
		return accountJsonArray;
	}

	// Translate account to JSON
	public static JSONObject toAccountJSON(IAccount account) {
		JSONObject accountJson = new JSONObject();
		try {
			accountJson.put(AccountEnum.USERNAME, account.getID());
			accountJson.put(AccountEnum.NICK_NAME, account.getName());
			accountJson.put(AccountEnum.PASSWORD, account.getPassword());
			accountJson.put(AccountEnum.EMAIL, account.getEmail());
			accountJson.put(AccountEnum.ENABLE, account.getEnable().equalsIgnoreCase("true") ? 1 : 0);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return accountJson;
	}

	// Translate multiple retrospective to JSONArray
	public static JSONArray toRetrospectiveJSONArray(List<IScrumIssue> retrospectives) {
		JSONArray retrospectiveJsonArray = new JSONArray();
		for (IScrumIssue retrospective : retrospectives) {
			retrospectiveJsonArray.put(toRetrospectiveJSON(retrospective));
		}
		return retrospectiveJsonArray;
	}
	
	// Translate retrospective to JSON
	public static JSONObject toRetrospectiveJSON(IScrumIssue retrospective) {
		JSONObject retrospectiveJson = new JSONObject();
		try {
			retrospectiveJson.put(RetrospectiveEnum.NAME, retrospective.getName());
			retrospectiveJson.put(RetrospectiveEnum.DESCRIPTION, retrospective.getDescription());
			retrospectiveJson.put(RetrospectiveEnum.TYPE, retrospective.getCategory());
			retrospectiveJson.put(RetrospectiveEnum.STATUS, retrospective.getStatus());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return retrospectiveJson;
	}
	
	// Translate multiple sprint to JSONArray
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

	// Translate multiple story to JSONArray
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
	
	// Translate multiple task to JSONArray
	public static JSONArray toTaskJSONArray(List<IIssue> tasks) {
		JSONArray taskJsonArray = new JSONArray();
		for (IIssue task : tasks) {
			taskJsonArray.put(toTaskJSON(task));
		}
		return taskJsonArray;
	}

	// Translate task to JSON
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
	
	// Translate multiple release to JSONArray
	public static JSONArray toReleaseJSONArray(List<IReleasePlanDesc> releases) {
		JSONArray releaseJsonArray = new JSONArray();
		for (IReleasePlanDesc release : releases) {
			releaseJsonArray.put(toReleaseJSON(release));
		}
		return releaseJsonArray;
	}

	// Translate release to JSON
	public static JSONObject toReleaseJSON(IReleasePlanDesc release) {
		JSONObject releaseJson = new JSONObject();
		try {
			releaseJson.put(ReleaseEnum.NAME, release.getName())
			           .put(ReleaseEnum.DESCRIPTION, release.getDescription())
			           .put(ReleaseEnum.START_DATE, release.getStartDate())
			           .put(ReleaseEnum.DUE_DATE, release.getEndDate());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return releaseJson;
	}
}
