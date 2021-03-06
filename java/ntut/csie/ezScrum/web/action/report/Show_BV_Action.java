package ntut.csie.ezScrum.web.action.report;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ntut.csie.ezScrum.web.action.PermissionAction;
import ntut.csie.ezScrum.web.mapper.ProjectMapper;
import ntut.csie.jcis.resource.core.IProject;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

public class Show_BV_Action extends PermissionAction{
	
	
	@Override
	public boolean isValidAction() {
		return true; //TODO
	}

	@Override
	public boolean isXML() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public StringBuilder getResponse(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		// TODO Auto-generated method stub
		// get session info
		
		IProject project = null;
		
		if (request.getParameter("ProjectID") != null) {
			// 從專案列表登入專案的狀態
			ProjectMapper projectMapper = new ProjectMapper();
			project = projectMapper.getProjectByID(request.getParameter("ProjectID"));
			
			// 設定 session
			request.getSession().setAttribute("Project", project);
		} else {
			// 從 Modify Config 查看專案的狀態
			project = (IProject) request.getSession().getAttribute("Project");
		}
		String bv = project.getProjectDesc().getBV();
		
		StringBuilder sb = new StringBuilder();
		//假如BV抓出的值為null或空字串預設為0
		if( ( bv == null || bv.isEmpty() ) ){
			sb.append( 0 );
		}else{
			sb.append( bv );
		}
		
		return sb;
	}

}
