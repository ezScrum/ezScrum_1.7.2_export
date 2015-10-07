package ntut.csie.ezScrum.web.action.report;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.iteration.support.TranslateSpecialChar;
import ntut.csie.ezScrum.pic.core.IUserSession;
import ntut.csie.ezScrum.web.action.PermissionAction;
import ntut.csie.ezScrum.web.control.ProductBacklogHelper;
import ntut.csie.ezScrum.web.support.SessionManager;
import ntut.csie.jcis.resource.core.IProject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

public class ShowCheckOutIssueAction extends PermissionAction {
	private static Log log = LogFactory.getLog(ShowCheckOutIssueAction.class);

	@Override
	public boolean isValidAction() {
		return super.getScrumRole().getAccessSprintBacklog();
	}

	@Override
	public boolean isXML() {
		// html
		return false;
	}

	@Override
	public StringBuilder getResponse(ActionMapping mapping, ActionForm form,
	        HttpServletRequest request, HttpServletResponse response) {

		// get project from session or DB
		IProject project = (IProject) SessionManager.getProject(request);
		IUserSession session = (IUserSession) request.getSession().getAttribute("UserSession");

		StringBuilder result = new StringBuilder("");
		ProductBacklogHelper productBacklogHelper = new ProductBacklogHelper(project, session);

		String defaultHandler = session.getAccount().getID();
		try {
			long issueID = Long.parseLong(request.getParameter("issueID"));
			IIssue item = productBacklogHelper.getIssue(issueID);
			if (item != null) {
				result.append(getJsonString(item, defaultHandler));
			} else {
				result.append(getJsonString(null, defaultHandler));
			}
		} catch (Exception e) {
			result.append(getJsonString(null, defaultHandler));
			this.log.debug("class : ShowCheckOutTaskAction, method : execute, exception : " + e.toString());
		}
		return result;
	}

	private StringBuilder getJsonString(IIssue issue, String handler) {
		StringBuilder result = new StringBuilder();
		TranslateSpecialChar translate = new TranslateSpecialChar();
		if (issue != null) {
			result.append("{\"Task\":{")
			        .append("\"Id\":\"").append(issue.getIssueID()).append("\",")
			        .append("\"Name\":\"").append(translate.TranslateJSONChar(issue.getSummary())).append("\",")
			        .append("\"Partners\":\"").append(issue.getPartners()).append("\",")
			        .append("\"Notes\":\"").append(translate.TranslateJSONChar(issue.getNotes())).append("\",")
			        .append("\"Handler\":\"").append(handler).append("\"")
			        .append("},")
			        .append("\"success\":true,")
			        .append("\"Total\":1")
			        .append("}");
		} else {
			result.append("");
		}
		return result;
	}
}