package ntut.csie.ezScrum.restful.export.support;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.issue.core.IIssueHistory;
import ntut.csie.ezScrum.issue.core.IIssueTag;
import ntut.csie.ezScrum.issue.core.ITSEnum;
import ntut.csie.ezScrum.issue.internal.IssueAttachFile;
import ntut.csie.ezScrum.iteration.core.IReleasePlanDesc;
import ntut.csie.ezScrum.iteration.core.IScrumIssue;
import ntut.csie.ezScrum.iteration.core.ISprintPlanDesc;
import ntut.csie.ezScrum.iteration.core.ScrumEnum;
import ntut.csie.ezScrum.pic.core.ScrumRole;
import ntut.csie.ezScrum.restful.export.jsonEnum.AccountJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.AttachFileJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.HistoryJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.ProjectJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.ReleaseJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.RetrospectiveJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.ScrumRoleJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.SprintJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.StoryJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.TagJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.TaskJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.UnplanJSONEnum;
import ntut.csie.jcis.account.core.IAccount;
import ntut.csie.jcis.account.core.IRole;
import ntut.csie.jcis.resource.core.IProject;

public class JSONEncoder {
	// Translate multiple history to JSONArray
	public static JSONArray toHistoryJSONArray(List<IIssueHistory> histories, String issueType) {
		JSONArray historyJSONArray = new JSONArray();
		for (IIssueHistory history : histories) {
			if (isHistoryValid(history)) {
				historyJSONArray.put(toHistoryJSON(history, issueType));
			}
		}
		return historyJSONArray;
	}

	public static boolean isHistoryValid(IIssueHistory history) {
		boolean isValid = false;
		int type = history.getType();
		String filedName = history.getFieldName();
		if (type == IIssueHistory.OTHER_TYPE) {
			String[] validFieldNameArrayInOtherType = new String[] {ScrumEnum.SPRINT_TAG, ScrumEnum.SPRINT_ID, IIssueHistory.SUMMARY,
					IIssueHistory.STATUS_FIELD_NAME, ScrumEnum.VALUE, ScrumEnum.ESTIMATION, ScrumEnum.IMPORTANCE,
					ScrumEnum.ACTUALHOUR, ScrumEnum.REMAINS};
			List<String> validFieldNamesInOtherType = Arrays.asList(validFieldNameArrayInOtherType);
			isValid = validFieldNamesInOtherType.contains(filedName);
			if (filedName != null && filedName.equals(IIssueHistory.STATUS_FIELD_NAME)) {
				int oldValue = Integer.parseInt(history.getOldValue());
				int newValue = Integer.parseInt(history.getNewValue());
				isValid &= !(oldValue == ITSEnum.CONFIRMED_STATUS);
				isValid &= !(newValue == ITSEnum.CONFIRMED_STATUS);
			}
		} else if (type == IIssueHistory.ISSUE_NEW_TYPE) {
			isValid = true;
		} else if (type == IIssueHistory.RELEATIONSHIP_ADD_TYPE) {
			String oldValue = history.getOldValue();
			String[] validOldValueArrayInAddType = new String[] {IIssueHistory.PARENT_OLD_VALUE, IIssueHistory.CHILD_OLD_VALUE};
			List<String> validOldValuesInAddType = Arrays.asList(validOldValueArrayInAddType);
			isValid = validOldValuesInAddType.contains(oldValue);
		} else if (type == IIssueHistory.RELEATIONSHIP_DELETE_TYPE) {
			String oldValue = history.getOldValue();
			String[] validOldValueArrayInDeleteType = new String[] {IIssueHistory.PARENT_OLD_VALUE, IIssueHistory.CHILD_OLD_VALUE};
			List<String> validOldValuesInDeleteType = Arrays.asList(validOldValueArrayInDeleteType);
			isValid = validOldValuesInDeleteType.contains(oldValue);
		}
		return isValid;
	}

	// Translate history to JSON
	public static JSONObject toHistoryJSON(IIssueHistory oldHistory, String issueType) {
		JSONObject historyJSON = new JSONObject();
		try {
			IIssueHistory newHistory = HistoryTranslator.toNewHistory(oldHistory, issueType);
			historyJSON.put(HistoryJSONEnum.HISTORY_TYPE, HistoryJSONEnum.HistoryType.values()[newHistory.getType()].name());
			historyJSON.put(HistoryJSONEnum.OLD_VALUE, newHistory.getOldValue());
			historyJSON.put(HistoryJSONEnum.NEW_VALUE, newHistory.getNewValue());
			historyJSON.put(HistoryJSONEnum.CREATE_TIME, newHistory.getModifyDate());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return historyJSON;
	}

	// Translate multiple project role to JSONArray
	public static JSONArray toProjectRoleJSONArray(String projectName, List<IAccount> projectRoles) {
		JSONArray projectRoleJSONArray = new JSONArray();
		for (IAccount projectRole : projectRoles) {
			projectRoleJSONArray.put(toProjectRoleJSON(projectName, projectRole));
		}
		return projectRoleJSONArray;
	}

	// Translate project role to JSON
	public static JSONObject toProjectRoleJSON(String projectName, IAccount projectRole) {
		JSONObject projectRoleJSON = new JSONObject();
		try {
			projectRoleJSON.put(AccountJSONEnum.USERNAME, projectRole.getID());
			projectRoleJSON.put(ScrumRoleJSONEnum.ROLE, splitRole(projectName, projectRole.getRoles()));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return projectRoleJSON;
	}

	// refer to GetProjectMembersAction splitRole(IProject project, IRole[]
	// roles)
	private static String splitRole(String projectName, IRole[] roles) {
		String split_role = "";

		if (roles.length > 0) {
			for (IRole role : roles) {
				// 將專案的角色以切字串方式取出
				String[] token = role.getRoleId().split(projectName + "_");
				if ((token.length == 2) && (token[1].length() > 0)) {
					// 取得此專案的角色即可
					split_role = token[1];
					break;
				}
			}
		}
		return split_role;
	}

	// Translate multiple scrum role to JSON
	public static JSONObject toScrumRolesJSON(ScrumRole productOwner, ScrumRole scrumMaster, ScrumRole scrumTeam,
			ScrumRole stakeholder, ScrumRole guest) {
		JSONObject scrumRolesJSON = new JSONObject();
		try {
			// set scrum role to scrum roles JSON
			scrumRolesJSON.put(productOwner.getRoleName(), toScrumRoleJSON(productOwner));
			scrumRolesJSON.put(scrumMaster.getRoleName(), toScrumRoleJSON(scrumMaster));
			scrumRolesJSON.put(scrumTeam.getRoleName(), toScrumRoleJSON(scrumTeam));
			scrumRolesJSON.put(stakeholder.getRoleName(), toScrumRoleJSON(stakeholder));
			scrumRolesJSON.put(guest.getRoleName(), toScrumRoleJSON(guest));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return scrumRolesJSON;
	}

	// Translate scrum role to JSON
	public static JSONObject toScrumRoleJSON(ScrumRole scrumRole) {
		JSONObject scrumRoleJSON = new JSONObject();
		try {
			// set scrum role's access
			scrumRoleJSON.put(ScrumRoleJSONEnum.ACCESS_PRODUCT_BACKLOG, scrumRole.getAccessProductBacklog());
			scrumRoleJSON.put(ScrumRoleJSONEnum.ACCESS_SPRINT_PLAN, scrumRole.getAccessSprintPlan());
			scrumRoleJSON.put(ScrumRoleJSONEnum.ACCESS_TASKBOARD, scrumRole.getAccessTaskBoard());
			scrumRoleJSON.put(ScrumRoleJSONEnum.ACCESS_SPRINT_BACKLOG, scrumRole.getAccessSprintBacklog());
			scrumRoleJSON.put(ScrumRoleJSONEnum.ACCESS_RELEASE_PLAN, scrumRole.getAccessReleasePlan());
			scrumRoleJSON.put(ScrumRoleJSONEnum.ACCESS_RETROSPECTIVE, scrumRole.getAccessRetrospective());
			scrumRoleJSON.put(ScrumRoleJSONEnum.ACCESS_UNPLAN, scrumRole.getAccessUnplannedItem());
			scrumRoleJSON.put(ScrumRoleJSONEnum.ACCESS_REPORT, scrumRole.getReadReport());
			scrumRoleJSON.put(ScrumRoleJSONEnum.ACCESS_EDIT_PROJECT, scrumRole.getEditProject());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return scrumRoleJSON;
	}

	// Translate multiple tag to JSONArray
	public static JSONArray toTagJSONArray(List<IIssueTag> tags) {
		JSONArray tagJSONArray = new JSONArray();
		for (IIssueTag tag : tags) {
			tagJSONArray.put(toTagJSON(tag));
		}
		return tagJSONArray;
	}

	// Translate tag to JSON
	public static JSONObject toTagJSON(IIssueTag tag) {
		JSONObject tagJSON = new JSONObject();
		try {
			tagJSON.put(TagJSONEnum.NAME, tag.getTagName());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return tagJSON;
	}

	// Translate multiple unplan to JSONArray
	public static JSONArray toUnplanJSONArray(List<IIssue> unplans) {
		JSONArray unplanJSONArray = new JSONArray();
		for (IIssue unplan : unplans) {
			unplanJSONArray.put(toUnplanJSON(unplan));
		}
		return unplanJSONArray;
	}

	// Translate unplan to JSON
	public static JSONObject toUnplanJSON(IIssue unplan) {
		JSONObject unplanJSON = new JSONObject();
		try {
			unplanJSON.put(UnplanJSONEnum.ID, unplan.getIssueID());
			unplanJSON.put(UnplanJSONEnum.NAME, unplan.getSummary());
			unplanJSON.put(UnplanJSONEnum.HANDLER, unplan.getAssignto());
			unplanJSON.put(UnplanJSONEnum.ESTIMATE, intStringToIntValue(unplan.getEstimated()));
			unplanJSON.put(UnplanJSONEnum.ACTUAL, intStringToIntValue(unplan.getActualHour()));
			unplanJSON.put(UnplanJSONEnum.NOTES, unplan.getNotes());
			unplanJSON.put(UnplanJSONEnum.STATUS, unplan.getStatus());
			// Process Partners
			JSONArray partnerJSONArray = new JSONArray();
			String delimiters = ";";
			// analyzing the string
			@SuppressWarnings("deprecation")
			String[] partnerStringArray = unplan.getPartners().split(delimiters);
			for (String partnerString : partnerStringArray) {
				if (partnerString.equals("")) {
					continue;
				}
				JSONObject partnerJSON = new JSONObject();
				partnerJSON.put(AccountJSONEnum.USERNAME, partnerString);
				partnerJSONArray.put(partnerJSON);
			}
			unplanJSON.put(UnplanJSONEnum.PARTNERS, partnerJSONArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return unplanJSON;
	}

	// Translate multiple attach file to JSONArray
	public static JSONArray toAttachFileJSONArray(List<IssueAttachFile> attachFiles, List<File> sourceFiles) {
		JSONArray attachFileJSONArray = new JSONArray();
		for (int i = 0; i < attachFiles.size(); i++) {
			attachFileJSONArray.put(toAttachFileJSON(attachFiles.get(i), sourceFiles.get(i)));
		}
		return attachFileJSONArray;
	}

	// Translate account to JSON
	public static JSONObject toAttachFileJSON(IssueAttachFile attachFile, File sourceFile) {
		JSONObject attachFileJson = new JSONObject();
		try {
			attachFileJson.put(AttachFileJSONEnum.NAME, attachFile.getFilename());
			attachFileJson.put(AttachFileJSONEnum.CONTENT_TYPE, attachFile.getFileType());
			String base64BinaryString = FileEncoder.toBase64BinaryString(sourceFile);
			attachFileJson.put(AttachFileJSONEnum.BINARY, base64BinaryString);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return attachFileJson;
	}

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
			accountJson.put(AccountJSONEnum.USERNAME, account.getID());
			accountJson.put(AccountJSONEnum.NICK_NAME, account.getName());
			accountJson.put(AccountJSONEnum.PASSWORD, account.getPassword());
			accountJson.put(AccountJSONEnum.EMAIL, account.getEmail());
			accountJson.put(AccountJSONEnum.ENABLE, Boolean.parseBoolean(account.getEnable()));
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
			retrospectiveJson.put(RetrospectiveJSONEnum.NAME, retrospective.getName());
			retrospectiveJson.put(RetrospectiveJSONEnum.DESCRIPTION, retrospective.getDescription());
			retrospectiveJson.put(RetrospectiveJSONEnum.TYPE, retrospective.getCategory());
			retrospectiveJson.put(RetrospectiveJSONEnum.STATUS, retrospective.getStatus());
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
			sprintJson.put(SprintJSONEnum.ID, Long.parseLong(sprint.getID()))
			        .put(SprintJSONEnum.GOAL, sprint.getGoal())
					.put(SprintJSONEnum.INTERVAL, intStringToIntValue(sprint.getInterval()))
					.put(SprintJSONEnum.TEAM_SIZE, intStringToIntValue(sprint.getMemberNumber()))
					.put(SprintJSONEnum.AVAILABLE_HOURS, intStringToIntValue(sprint.getAvailableDays()))
					.put(SprintJSONEnum.FOCUS_FACTOR, intStringToIntValue(sprint.getFocusFactor()))
					.put(SprintJSONEnum.START_DATE, sprint.getStartDate())
					.put(SprintJSONEnum.END_DATE, sprint.getEndDate())
					.put(SprintJSONEnum.DEMO_DATE, sprint.getDemoDate())
					.put(SprintJSONEnum.DEMO_PLACE, sprint.getDemoPlace())
					.put(SprintJSONEnum.DAILY_INFO, sprint.getNotes());
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
			long attachFileSize = 2;
			String attachFileSizeString = project.getProjectDesc().getAttachFileSize();
			if (attachFileSizeString != null && !attachFileSizeString.isEmpty()) {
				attachFileSize = Long.parseLong(attachFileSizeString);
			}
			projectJson.put(ProjectJSONEnum.NAME, project.getName())
					.put(ProjectJSONEnum.DISPLAY_NAME, project.getProjectDesc().getDisplayName())
					.put(ProjectJSONEnum.COMMENT, project.getProjectDesc().getComment())
					.put(ProjectJSONEnum.PRODUCT_OWNER, project.getProjectDesc().getProjectManager())
					.put(ProjectJSONEnum.ATTATCH_MAX_SIZE, attachFileSize)
			        .put(ProjectJSONEnum.CREATE_TIME, project.getProjectDesc().getCreateDate().getTime());
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
			storyJson.put(StoryJSONEnum.ID, story.getIssueID())
			  		 .put(StoryJSONEnum.NAME, story.getSummary())
					 .put(StoryJSONEnum.STATUS, story.getStatus())
					 .put(StoryJSONEnum.ESTIMATE, intStringToIntValue(story.getEstimated()))
					 .put(StoryJSONEnum.IMPORTANCE, intStringToIntValue(story.getImportance()))
					 .put(StoryJSONEnum.VALUE, intStringToIntValue(story.getValue()))
					 .put(StoryJSONEnum.NOTES, story.getNotes())
					 .put(StoryJSONEnum.HOW_TO_DEMO, story.getHowToDemo());
			JSONArray tagJSONArray = new JSONArray();
			for (IIssueTag tag : story.getTag()) {
				JSONObject tagJSON = new JSONObject();
				tagJSON.put(TagJSONEnum.NAME, tag.getTagName());
				tagJSONArray.put(tagJSON);
			}
			storyJson.put(StoryJSONEnum.TAGS, tagJSONArray);
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
			taskJson.put(TaskJSONEnum.ID, task.getIssueID())
			        .put(TaskJSONEnum.NAME, task.getSummary())
					.put(TaskJSONEnum.HANDLER, task.getAssignto())
			        .put(TaskJSONEnum.ESTIMATE, intStringToIntValue(task.getEstimated()))
			        .put(TaskJSONEnum.REMAIN, intStringToIntValue(task.getRemains()))
			        .put(TaskJSONEnum.ACTUAL, intStringToIntValue(task.getActualHour()))
			        .put(TaskJSONEnum.NOTES, task.getNotes())
					.put(TaskJSONEnum.STATUS, task.getStatus());
			// Process Partners
			JSONArray partnerJSONArray = new JSONArray();
			String delimiters = ";";
			// analyzing the string
			@SuppressWarnings("deprecation")
			String[] partnerStringArray = task.getPartners().split(delimiters);
			for (String partnerString : partnerStringArray) {
				if (partnerString.equals("")) {
					continue;
				}
				JSONObject partnerJSON = new JSONObject();
				partnerJSON.put(AccountJSONEnum.USERNAME, partnerString);
				partnerJSONArray.put(partnerJSON);
			}
			taskJson.put(TaskJSONEnum.PARTNERS, partnerJSONArray);
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
			releaseJson.put(ReleaseJSONEnum.NAME, release.getName())
					.put(ReleaseJSONEnum.DESCRIPTION, release.getDescription())
					.put(ReleaseJSONEnum.START_DATE, release.getStartDate())
					.put(ReleaseJSONEnum.END_DATE, release.getEndDate());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return releaseJson;
	}
	
	private static int intStringToIntValue(String intString) {
		double intValueInDouble;
		try {
			intValueInDouble = Double.valueOf(intString);
		} catch (NumberFormatException e) {
			intValueInDouble = 0;
		}
		return (int) intValueInDouble;
	}
}
