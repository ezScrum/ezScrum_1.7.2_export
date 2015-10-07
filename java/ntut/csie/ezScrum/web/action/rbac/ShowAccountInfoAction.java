package ntut.csie.ezScrum.web.action.rbac;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ntut.csie.ezScrum.web.mapper.AccountMapper;
import ntut.csie.jcis.account.core.AccountFactory;
import ntut.csie.jcis.account.core.IAccount;
import ntut.csie.jcis.account.core.IAccountManager;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class ShowAccountInfoAction extends Action {
	private String id = "";

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		// 取得要取得帳號資訊的id
		this.id = request.getParameter("id");

//		IAccountManager am = AccountFactory.getManager();
//		IAccount account = am.getAccount(this.id);
		IAccount account = (new AccountMapper()).getAccountById(this.id);
		
		// write account to XML format
		StringBuilder sb = new StringBuilder();
		sb.append("<Accounts>");
		sb.append("<Account>");
		sb.append("<ID>" + this.id + "</ID>");				
		sb.append("<Name>" + account.getName() + "</Name>");				
		sb.append("<Mail>" + account.getEmail() + "</Mail>");
		sb.append("<Enable>" + account.getEnable()+ "</Enable>");
		sb.append("</Account>");
	    sb.append("</Accounts>");
	    
	    try {
			response.setContentType("text/xml; charset=utf-8");
			response.getWriter().write(sb.toString());
			response.getWriter().close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}
