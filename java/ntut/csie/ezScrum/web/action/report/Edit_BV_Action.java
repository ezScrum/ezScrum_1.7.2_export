package ntut.csie.ezScrum.web.action.report;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ntut.csie.ezScrum.web.action.PermissionAction;
import ntut.csie.jcis.project.core.IProjectDescription;
import ntut.csie.jcis.resource.core.IProject;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

public class Edit_BV_Action extends PermissionAction{
	
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
		String BV = request.getParameter("BV");
		IProject project = (IProject) request.getSession().getAttribute("Project");
		IProjectDescription projectDescription = project.getProjectDesc();
		projectDescription.setBV( BV );
		project.setProjectDesc(projectDescription);
		project.save();
		return new StringBuilder("ok");
	}

}

