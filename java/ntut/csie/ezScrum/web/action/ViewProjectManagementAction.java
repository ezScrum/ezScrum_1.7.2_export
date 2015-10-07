package ntut.csie.ezScrum.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ntut.csie.ezScrum.pic.core.IUserSession;
import ntut.csie.ezScrum.web.helper.AccountHelper;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class ViewProjectManagementAction extends Action {
//	private static Log log = LogFactory.getLog(ViewProjectManagementAction.class);

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		IUserSession session = (IUserSession) request.getSession().getAttribute("UserSession");
		AccountHelper ah = new AccountHelper();

//		IAccount Account = session.getAccount();
//		IAccountManager manager = AccountFactory.getManager();
//		IPermission permAdmin = manager.getPermission("system", "admin");
//
//		if (Boolean.valueOf(Account.checkPermission(permAdmin))) {
//			this.log.info(Account.getID() + " is an admin.");
//			return mapping.findForward("Admin_ManagementView");
//		} else {
//			this.log.info(Account.getID() + " is a user.");
//			return mapping.findForward("User_ManagementView");
//		}
		
		return mapping.findForward(ah.getManagementView(session.getAccount()));
	}

}
