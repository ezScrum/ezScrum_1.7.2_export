package ntut.csie.ezScrum.web.dataObject;

import ntut.csie.ezScrum.issue.core.IIssue;

public class TaskObject {
	public String id = "";
	public String name = "";
	public String estimation = "";
	public String status = "";
	public String handler = "";
	public String partners = "";
	public String remains = "";
	public String actual = "";
	public String notes = "";
	public String specificTime = "";
	
	public TaskObject() {}
	
	public TaskObject(IIssue task) {
		id = Long.toString(task.getIssueID());
		name = task.getSummary();
		estimation = task.getEstimated();
		status = task.getStatus();
		// issue 沒有 handler 可以抓
		partners = task.getPartners();
		remains = task.getRemains();
		actual = task.getActualHour();
		notes = task.getNotes();
	}
	
	public String toString() {
		String task = 
				"id :" + id + 
				", name :" + name +
				", estimation :" + estimation +
				", status :" + status +
				", handler :" + handler +
				", partners :" + partners +
				", remains :" + remains +
				", actual :" + actual +
				", notes :" + notes;
		return task;
	}
}
