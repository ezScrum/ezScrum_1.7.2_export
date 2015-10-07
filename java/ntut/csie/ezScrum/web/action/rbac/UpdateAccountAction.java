package ntut.csie.ezScrum.web.action.rbac;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ntut.csie.ezScrum.pic.core.IUserSession;
import ntut.csie.ezScrum.web.dataObject.UserInformation;
import ntut.csie.ezScrum.web.helper.AccountHelper;
import ntut.csie.ezScrum.web.support.SessionManager;
import ntut.csie.jcis.account.core.IAccount;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ch.ethz.ssh2.crypto.Base64;

public class UpdateAccountAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {

		IUserSession session = (IUserSession) request.getSession().getAttribute("UserSession");
		
		String id = request.getParameter("id");
		String password = request.getParameter("passwd");
		String email = request.getParameter("mail");
		String name = request.getParameter("name");
		String enable = request.getParameter("enable");
		
		UserInformation userInformation = new UserInformation(id, name, password, email, enable);
		
		AccountHelper ah = new AccountHelper();
		IAccount newAccInfo = ah.updateAccount(session, userInformation);
		
		//	no password, use the default password
		if ( (password == null) || (password.length()==0) || password.equals("") ) {
			password = newAccInfo.getPassword();
		}
		
		//	如果更新的是登入者的密碼則更新session中屬於插件使用的密碼
		String userName = session.getAccount().getName();
		if( userName.equals( id ) ){
			String encodedPassword = new String( Base64.encode( password.getBytes() ) );
			request.getSession().setAttribute("passwordForPlugin", encodedPassword);
		}
		
		
		//	目前改了密碼之後並未強制使用者登出,可改良以避免一些問題
		IAccount sessionAccount = session.getAccount();
		
		sessionAccount.setName(newAccInfo.getName());		
		sessionAccount.setEmail(newAccInfo.getEmail());
//		sessionAccount.setEnable(enable);	// 只有admin可以設定
		sessionAccount.setPassword(newAccInfo.getPassword());	// 應該是下次登入才生效,但存取專案資料是比對新的密碼
		
		try {
			response.setContentType("text/xml; charset=utf-8");
			response.getWriter().write(ah.getAccountXML(newAccInfo));
			response.getWriter().close();
		} catch (IOException e) {
			System.out.println("class : UpdateAccountAction, method : execute, exception : " + e.toString());
			e.printStackTrace();
		}

		return null;
	}
}
