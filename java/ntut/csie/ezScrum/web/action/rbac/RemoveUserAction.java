package ntut.csie.ezScrum.web.action.rbac;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ntut.csie.ezScrum.pic.core.IUserSession;
import ntut.csie.ezScrum.web.helper.AccountHelper;
import ntut.csie.ezScrum.web.support.SessionManager;
import ntut.csie.jcis.account.core.IAccount;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class RemoveUserAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String id = request.getParameter("id");;
		String resource = request.getParameter("resource");
		String operation = request.getParameter("operation");
		
		IUserSession session = (IUserSession) request.getSession().getAttribute("UserSession");
		AccountHelper ah = new AccountHelper();
		
		if (id != null && resource != null && operation != null) {
			try {
//				IAccount account = updateAccount(session, id, resource, operation);
				IAccount account = ah.assignRole_remove(session, id, resource, operation);
				
				// 刪除Session中關於該使用者的所有專案權限。
				SessionManager.removeScrumRolesMap(request, account);
				
				// 將判斷帳號是否有效結果傳給View
//				List<IActor> accountList = new LinkedList<IActor>();
//				accountList.add(account);
//				AccountXmlTranslation tr = new AccountXmlTranslation(accountList);
				
				response.setContentType("text/xml; charset=utf-8");
//				response.getWriter().write(tr.getXmlstring());
				response.getWriter().write(ah.getAccountXML(account));				
				response.getWriter().close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}

	/**
	 * 進行帳號更新的動作, 並且將帳號Assign Roles, 建立完畢執行儲存檔案
	 * @throws Exception
	 */
//	private IAccount updateAccount(IUserSession session, String id, String res, String op) throws Exception {
//		IAccountManager am = AccountFactory.getManager();
//		IAccount account = am.getAccount(id);
//		IRole[] roles = account.getRoles();
//		
//		String role = "";
//		if(res.equals(ScrumEnum.SYSTEM))
//			role = op;
//		else	
//			role = res + "_" + op;
//		
//		List<String> roleList = translateRoleString(roles, role);
//		
//		//更新mantis資訊的部分
//		MantisAccountManager accountManager = new MantisAccountManager(session);
//		accountManager.removeReleation(account, res);
//		
//		//更新ezScrum local的部分
//		am.updateAccountRoles(id, roleList);
//		am.save();
//		
//		account = am.getAccount(id);
//		
//		ScrumRoleManager scrumRoleManager = new ScrumRoleManager();
//		scrumRoleManager.setScrumRoles( session.getAccount() );//reset Project<-->ScrumRole map
//		
//		return account;
//	}

	/**
	 * 將Roles String轉成Role String List
	 */
//	private List<String> translateRoleString(IRole[] roles, String role) {
//		List<String> roleList = new ArrayList<String>();
//		// default
//		if(roles!=null){
//			for (IRole irole : roles) {
//				if( ! irole.getRoleId().equals(role))
//					roleList.add(irole.getRoleId());
//			}
//		}
//		return roleList;
//	}
}
