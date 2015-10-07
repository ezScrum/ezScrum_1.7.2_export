package ntut.csie.ezScrum.web.action.other;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ntut.csie.ezScrum.pic.core.IUserSession;
import ntut.csie.ezScrum.service.CustomIssueType;
import ntut.csie.ezScrum.service.IssueBacklog;
import ntut.csie.ezScrum.web.support.SessionManager;
import ntut.csie.jcis.resource.core.IProject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class AjaxGetCustomIssueTypeAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		
//		IProject project = (IProject) request.getSession().getAttribute("Project");
		IProject project = SessionManager.getProject(request);
		IUserSession session = (IUserSession) request.getSession().getAttribute("UserSession");
		
		IssueBacklog backlog = new IssueBacklog(project, session);
		List<CustomIssueType> typeList = backlog.getCustomIssueType();
		String result = "";
		/**
		 * 設定issue type 成xml的格式
		 * <Root>
		 *		<Result>Success</Result>
		 *		<IssueType>
		 *			<TypeId>1</TypeId>
		 *			<TypeName>Risk</TypeName>
		 *          <IsPublic>true</IsPublic>
		 *		</IssueType>
		 *		<IssueType>
		 *			<TypeId>2</TypeId>
		 *			<TypeName>Bug</TypeName>
		 *          <IsPublic>false</IsPublic>
		 *        	</IssueType>
		 *		<IssueType>
		 *			...
		 *		</IssueType>
		 *	</Root>
		 */
		try{
			StringBuilder sb = new StringBuilder();
			sb.append("<Root>");
			sb.append("<Result>Success</Result>");
			for(CustomIssueType type : typeList){
				sb.append("<IssueType>");
				sb.append("<TypeId>" + type.getTypeId() + "</TypeId>");
				sb.append("<TypeName>" + type.getTypeName() + "</TypeName>");
				sb.append("<IsPublic>" + type.ispublic() + "</IsPublic>");
				sb.append("</IssueType>");
			}
			sb.append("</Root>");
			result = sb.toString();
		} catch(Exception e){
			e.printStackTrace();
		}
		/**
		 * 設定回傳資料與型態
		 */
		try {
			response.setContentType("text/xml; charset=utf-8");
			response.getWriter().write(result);
			response.getWriter().close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
