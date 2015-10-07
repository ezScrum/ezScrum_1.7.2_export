package ntut.csie.ezScrum.web.action.rbac;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ntut.csie.ezScrum.web.helper.AccountHelper;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class CheckAccountIDAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		String id = request.getParameter("id");
		
		// 設置Header與編碼
		response.setContentType("text/html");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");

		try {
			// 將判斷帳號是否有效結果傳給View
//			response.getWriter().write(validateAccountID(id));
			response.getWriter().write((new AccountHelper()).validateAccountID(id));
			response.getWriter().close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 判斷帳號是否已被建立, 建立則傳給View無法新增帳號的訊息 判斷帳號是否為有效的字元, 數字與英文才為有效 true則已存在,false則不存在
	 * 
	 * @param id
	 * @return
	 */
//	private String validateAccountID(String id) {
//
//		// 判斷帳號是否符合只有英文+數字的格式
//		Pattern p = Pattern.compile("[0-9a-zA-Z_]*");
//		Matcher m = p.matcher(id);
//		boolean b = m.matches();
//
//		// 若帳號可建立且ID format正確 則回傳true
//		IAccountManager am = AccountFactory.getManager();
//		if (b && !am.isAccountExist(id) && !id.isEmpty()) {			
//			return "true";
//		}
//
//		return "false";
//	}
}
