package ntut.csie.ezScrum.web.action.rbac;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ntut.csie.ezScrum.pic.core.IUserSession;
import ntut.csie.ezScrum.web.helper.AccountHelper;
import ntut.csie.ezScrum.web.support.SessionManager;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class DeleteAccountAction extends Action {
//	private static Log log = LogFactory.getLog(DeleteAccountAction.class);
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		//取得session
		IUserSession userSession = (IUserSession) request.getSession().getAttribute("UserSession");
		
		// 取得要刪除的id
		String id = request.getParameter("id");

		// 設置Header與編碼
		response.setContentType("text/html");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
	
		AccountHelper ah = new AccountHelper();
		ah.deleteAccount(userSession, id);
//		deleteAccount(userSession, id);
		
		try {
			response.getWriter().write("true");
			response.getWriter().close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 刪除Session中關於該使用者的所有專案權限。
		SessionManager.removeScrumRolesMap(request, userSession.getAccount());
		
		return null;
	}

	/**
	 * 刪除帳號, 刪除完儲存檔案
	 * 
	 * @throws Exception
	 */
//	private void deleteAccount(IUserSession session, String id) {
//		IAccountManager am = AccountFactory.getManager();
//		//mantis
//		MantisAccountManager accountManager = new MantisAccountManager(session);
//		try {
//			accountManager.deleteAccount(am, id);
//		} catch (Exception e) {
//			this.log.info("class: DeleteAccountAction, method: deleteAccount, exception: " + e.toString());
//			e.printStackTrace();
//		}
//		
//		// 刪除帳號, 包含群組內與Assign的資料
//		am.removeAccount(id);
//		am.save();
//	}
}
