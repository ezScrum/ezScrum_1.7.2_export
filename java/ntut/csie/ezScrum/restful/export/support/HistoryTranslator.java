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
			if (oldHistory.getFieldName().equals("Sprint")) {
				newHistory.setType(HistoryJSONEnum.TYPE_APPEND);
				newHistory.setOldValue("");
				newHistory.setNewValue(oldHistory.getNewValue());
			} else if (oldHistory.getFieldName().equals(IIssueHistory.SUMMARY)) {
				newHistory.setType(HistoryJSONEnum.TYPE_NAME);
				newHistory.setOldValue(oldHistory.getOldValue());
				newHistory.setNewValue(oldHistory.getNewValue());
			} else if (oldHistory.getFieldName().equals(IIssueHistory.STATUS_FIELD_NAME)) {
				String translatedOldValue = getStatusByIssueTypeAndValue(issueType, Integer.parseInt(oldHistory.getOldValue()));
				String translatedNewValue = getStatusByIssueTypeAndValue(issueType, Integer.parseInt(oldHistory.getNewValue()));
				newHistory.setType(HistoryJSONEnum.TYPE_STATUS);
				newHistory.setOldValue(translatedOldValue);
				newHistory.setNewValue(translatedNewValue);
			}
		} else if (oldHistory.getType() == IIssueHistory.ISSUE_NEW_TYPE) {
			newHistory.setType(HistoryJSONEnum.TYPE_CREATE);
			newHistory.setOldValue("");
			newHistory.setNewValue("");
		}
		newHistory.setModifyDate(oldHistory.getModifyDate());
		return newHistory;
	}
	
	private static String getStatusByIssueTypeAndValue(String issueType, int value) {
		String translatedValue = String.valueOf(value);
		if (issueType.equals(ScrumEnum.TASK_ISSUE_TYPE) || issueType.equals(ScrumEnum.UNPLANNEDITEM_ISSUE_TYPE)) {
			if (value == ITSEnum.NEW_STATUS) {
				translatedValue = HistoryJSONEnum.TASK_STATUS_UNCHECK;
			} else if (value == ITSEnum.ASSIGNED_STATUS) {
				translatedValue = HistoryJSONEnum.TASK_STATUS_CHECK;
			} else if (value == ITSEnum.CLOSED_STATUS) {
				translatedValue = HistoryJSONEnum.TASK_STATUS_DONE;
			}
		} else if (issueType.equals(ScrumEnum.GOOD_ISSUE_TYPE) || issueType.equals(ScrumEnum.IMPROVEMENTS_ISSUE_TYPE)) {
			if (value == ITSEnum.NEW_STATUS) {
				translatedValue = HistoryJSONEnum.RETROSPECTIVE_STATUS_NEW;
			} else if (value == ITSEnum.ASSIGNED_STATUS) {
				translatedValue = HistoryJSONEnum.RETROSPECTIVE_STATUS_ASSIGNED;
			} else if (value == ITSEnum.RESOLVED_STATUS) {
				translatedValue = HistoryJSONEnum.RETROSPECTIVE_STATUS_RESOLVED;
			} else if (value == ITSEnum.CLOSED_STATUS) {
				translatedValue = HistoryJSONEnum.RETROSPECTIVE_STATUS_CLOSED;
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
