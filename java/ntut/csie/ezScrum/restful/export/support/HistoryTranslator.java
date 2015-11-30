package ntut.csie.ezScrum.restful.export.support;

import ntut.csie.ezScrum.issue.core.IIssueHistory;
import ntut.csie.ezScrum.issue.core.ITSEnum;
import ntut.csie.ezScrum.issue.internal.IssueHistory;
import ntut.csie.ezScrum.iteration.core.ScrumEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.HistoryJSONEnum;

public class HistoryTranslator {
	public static IIssueHistory toNewHistory(IIssueHistory oldHistory, String issueType) {
		IssueHistory newHistory = new IssueHistory();
		if (oldHistory.getType() == IIssueHistory.OTHER_TYPE) {
			if (oldHistory.getFieldName().equals(ScrumEnum.SPRINT_ID) || oldHistory.getFieldName().equals(ScrumEnum.SPRINT_TAG)) {
				if (issueType.equals(ScrumEnum.UNPLANNEDITEM_ISSUE_TYPE)) {
					newHistory.setType(HistoryJSONEnum.HistoryType.UNPLAN_CHANGE_SPRINT_ID.ordinal());
					newHistory.setOldValue(oldHistory.getOldValue());
					newHistory.setNewValue(oldHistory.getNewValue());
				} else {
					int oldSprintId = Integer.parseInt(oldHistory.getOldValue());
					int newSprintId = Integer.parseInt(oldHistory.getNewValue());
					if (oldSprintId <= 0) { // Append to sprint
						newHistory.setType(HistoryJSONEnum.HistoryType.APPEND_TO_SPRINT.ordinal());
						newHistory.setOldValue("");
						newHistory.setNewValue(oldHistory.getNewValue());
					} else if (newSprintId <= 0) { // Remove from sprint
						newHistory.setType(HistoryJSONEnum.HistoryType.REMOVE_FROM_SPRINT.ordinal());
						newHistory.setOldValue("");
						newHistory.setNewValue(oldHistory.getOldValue());
					}
				}
			} else if (oldHistory.getFieldName().equals(IIssueHistory.SUMMARY)) {
				newHistory.setType(HistoryJSONEnum.HistoryType.NAME.ordinal());
				newHistory.setOldValue(oldHistory.getOldValue());
				newHistory.setNewValue(oldHistory.getNewValue());
			} else if (oldHistory.getFieldName().equals(IIssueHistory.STATUS_FIELD_NAME)) {
				String translatedOldValue = getStatusByIssueTypeAndValue(issueType, Integer.parseInt(oldHistory.getOldValue()));
				String translatedNewValue = getStatusByIssueTypeAndValue(issueType, Integer.parseInt(oldHistory.getNewValue()));
				newHistory.setType(HistoryJSONEnum.HistoryType.STATUS.ordinal());
				newHistory.setOldValue(translatedOldValue);
				newHistory.setNewValue(translatedNewValue);
			} else if (oldHistory.getFieldName().equals(ScrumEnum.VALUE)) {
				newHistory.setType(HistoryJSONEnum.HistoryType.VALUE.ordinal());
				newHistory.setOldValue(oldHistory.getOldValue());
				newHistory.setNewValue(oldHistory.getNewValue());
			} else if (oldHistory.getFieldName().equals(ScrumEnum.ESTIMATION)) {
				newHistory.setType(HistoryJSONEnum.HistoryType.ESTIMATE.ordinal());
				newHistory.setOldValue(oldHistory.getOldValue());
				newHistory.setNewValue(oldHistory.getNewValue());
			} else if (oldHistory.getFieldName().equals(ScrumEnum.IMPORTANCE)) {
				newHistory.setType(HistoryJSONEnum.HistoryType.IMPORTANCE.ordinal());
				newHistory.setOldValue(oldHistory.getOldValue());
				newHistory.setNewValue(oldHistory.getNewValue());
			} else if (oldHistory.getFieldName().equals(ScrumEnum.ACTUALHOUR)) {
				newHistory.setType(HistoryJSONEnum.HistoryType.ACTUAL.ordinal());
				newHistory.setOldValue(oldHistory.getOldValue());
				newHistory.setNewValue(oldHistory.getNewValue());
			} else if (oldHistory.getFieldName().equals(ScrumEnum.REMAINS)) {
				newHistory.setType(HistoryJSONEnum.HistoryType.REMAINS.ordinal());
				newHistory.setOldValue(oldHistory.getOldValue());
				newHistory.setNewValue(oldHistory.getNewValue());
			}
		} else if (oldHistory.getType() == IIssueHistory.ISSUE_NEW_TYPE) {
			newHistory.setType(HistoryJSONEnum.HistoryType.CREATE.ordinal());
			newHistory.setOldValue("");
			newHistory.setNewValue("");
		} else if (oldHistory.getType() == IIssueHistory.RELEATIONSHIP_ADD_TYPE) {
			if (oldHistory.getOldValue().equals(IIssueHistory.PARENT_OLD_VALUE)){ // Add Task
				newHistory.setType(HistoryJSONEnum.HistoryType.ADD_TASK.ordinal());
				newHistory.setOldValue("");
				newHistory.setNewValue(oldHistory.getNewValue());
			} else if (oldHistory.getOldValue().equals(IIssueHistory.CHILD_OLD_VALUE)){ // Append to Story
				newHistory.setType(HistoryJSONEnum.HistoryType.APPEND_TO_STORY.ordinal());
				newHistory.setOldValue("");
				newHistory.setNewValue(oldHistory.getNewValue());
			}
		} else if(oldHistory.getType() == IIssueHistory.RELEATIONSHIP_DELETE_TYPE){
			if (oldHistory.getOldValue().equals(IIssueHistory.PARENT_OLD_VALUE)){ // Drop Task
				newHistory.setType(HistoryJSONEnum.HistoryType.DROP_TASK.ordinal());
				newHistory.setOldValue("");
				newHistory.setNewValue(oldHistory.getNewValue());
			} else if (oldHistory.getOldValue().equals(IIssueHistory.CHILD_OLD_VALUE)){ // Remove from Story
				newHistory.setType(HistoryJSONEnum.HistoryType.REMOVE_FROM_STORY.ordinal());
				newHistory.setOldValue("");
				newHistory.setNewValue(oldHistory.getNewValue());
			}
		}
		newHistory.setModifyDate(oldHistory.getModifyDate());
		return newHistory;
	}
	
	private static String getStatusByIssueTypeAndValue(String issueType, int value) {
		String translatedValue = String.valueOf(value);
		if (issueType.equals(ScrumEnum.TASK_ISSUE_TYPE) || issueType.equals(ScrumEnum.UNPLANNEDITEM_ISSUE_TYPE)) {
			if (value == ITSEnum.NEW_STATUS) {
				translatedValue = HistoryJSONEnum.TASK_UNPLAN_STATUS_UNCHECK;
			} else if (value == ITSEnum.ASSIGNED_STATUS) {
				translatedValue = HistoryJSONEnum.TASK_UNPLAN_STATUS_CHECK;
			} else if (value == ITSEnum.CLOSED_STATUS) {
				translatedValue = HistoryJSONEnum.TASK_UNPLAN_STATUS_DONE;
			}
		} else if (issueType.equals(ScrumEnum.STORY_ISSUE_TYPE)) {
			if (value == ITSEnum.NEW_STATUS) {
				translatedValue = HistoryJSONEnum.STORY_STATUS_UNCHECK;
			} else if (value == ITSEnum.CLOSED_STATUS) {
				translatedValue = HistoryJSONEnum.STORY_STATUS_DONE;
			}
		}
		return translatedValue;
	}
}
