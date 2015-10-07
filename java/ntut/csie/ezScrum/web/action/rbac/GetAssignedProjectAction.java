package ntut.csie.ezScrum.web.action.rbac;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ntut.csie.ezScrum.web.helper.AccountHelper;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class GetAssignedProjectAction extends Action {
//	private final String SYSTEM = "system";

	/**
	 * 利用Ajax將資料傳給View
	 */
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		String id = request.getParameter("accountID");
		AccountHelper ah = new AccountHelper();
		
//		IAccountManager am = AccountFactory.getManager();
//		IAccount account = am.getAccount(id);
//		IRole[] roleList = account.getRoles();
//		List<String> assignedProject = new ArrayList<String>();
//		
//		StringBuilder sb = new StringBuilder();
//		
//		// 取得帳號的Assign資訊
//		sb.append("<AssignRoleInfo>");
//		sb.append("<Account>");
//		// Account Info
//		sb.append("<ID>" + account.getID() + "</ID>");
//		sb.append("<Name>" + account.getName() + "</Name>");
//		
//		// Assign Roles 
//		sb.append("<Roles>");
//		for(IRole role: roleList){
//			IPermission[] permissions = role.getPermisions();
//			if(permissions!=null){
//				for(IPermission permission: permissions){
//					String resource = permission.getResourceName();
//					String operation = permission.getOperation();
//					if(resource.equals("system")&&(operation.equals("read")||operation.equals("createProject")))
//						continue;
//					sb.append("<Assigned>");
//					sb.append("<Resource>" + resource + "</Resource>");
//					sb.append("<Operation>" + operation + "</Operation>");
//					sb.append("</Assigned>");
//					//記錄此project為assigned
//					assignedProject.add(resource);
//				}
//			}
//		}
//		sb.append("</Roles>");
//		
//		// 取得尚未被Assign的專案資訊
//		ProjectLogic helper = new ProjectLogic();
//		List<IProject> projects = helper.getAllProjects();
//		
//        for(IProject project: projects){
//			String resource = project.getName();
//			//如果project沒有被assigned權限，則代表為unassigned的project
//	        if(!assignedProject.contains(resource))
//	        	sb.append("<Unassigned><Resource>" +  resource + "</Resource></Unassigned>");
//		}
//        //判斷是否為administrator
//        if(!assignedProject.contains(this.SYSTEM))
//        	sb.append("<Unassigned><Resource>" + this.SYSTEM + "</Resource></Unassigned>");					
//        
//        
//        sb.append("</Account>");
//		sb.append("</AssignRoleInfo>");		

		try {
			response.setContentType("text/xml; charset=utf-8");
//			response.getWriter().write(sb.toString());
			response.getWriter().write(ah.getAssignedProject(id));
			response.getWriter().close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
		
	}
}
	
