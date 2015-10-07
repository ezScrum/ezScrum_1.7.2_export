package ntut.csie.ezScrum.web.action;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ntut.csie.ezScrum.pic.core.IUserSession;
import ntut.csie.ezScrum.pic.core.ScrumRole;
import ntut.csie.ezScrum.web.control.TaskBoard;
import ntut.csie.ezScrum.web.form.ProjectInfoForm;
import ntut.csie.ezScrum.web.iternal.IProjectSummaryEnum;
import ntut.csie.ezScrum.web.logic.ProjectLogic;
import ntut.csie.ezScrum.web.logic.ScrumRoleLogic;
import ntut.csie.ezScrum.web.logic.SprintBacklogLogic;
import ntut.csie.ezScrum.web.mapper.ProjectMapper;
import ntut.csie.ezScrum.web.mapper.SprintBacklogMapper;
import ntut.csie.ezScrum.web.support.SessionManager;
import ntut.csie.jcis.account.core.IAccount;
import ntut.csie.jcis.resource.core.IProject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class ViewProjectSummaryAction extends Action {
	private static Log log = LogFactory.getLog(ViewProjectSummaryAction.class);
	private SessionManager m_projectSessionManager = null;

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) {
		HttpSession session = request.getSession();
		IUserSession userSession = (IUserSession) request.getSession().getAttribute("UserSession");
		String projectID = request.getParameter("PID");					//	取得Project ID
		IProject project = (IProject) request.getAttribute(projectID);	//	根據project ID 取得專案
		
		log.debug("Parameter=" + projectID);	//	project id log information
		
		ProjectLogic projectLogic = new ProjectLogic();
		
		//	比對PID是否存在
		if( !projectLogic.projectIsExistedInWorkspace(projectID) ){
			return mapping.findForward("error");
		}
		
		//	判斷session中專案是否為空，空的話則建立新的專案於session中
		if (project == null) {
			ProjectMapper projectMapper = new ProjectMapper();
			project = projectMapper.getProjectByID(projectID);
			session.setAttribute(projectID, project);
		}
		
		//	判斷該使用者是否存在於專案中
		if( !projectLogic.userIsExistedInProject(project, userSession) ){
			session.removeAttribute( projectID );
			return mapping.findForward("permissionDenied");
		}
		
		m_projectSessionManager = new SessionManager(request);
		
		//	以ProjectMapper來取得Project內的設定資料
		ProjectMapper projectMapper = new ProjectMapper();
		ProjectInfoForm projectInfo = projectMapper.getProjectInfoForm(project);
		request.setAttribute(IProjectSummaryEnum.PROJECT_INFO_FORM, projectInfo);

		//	更新session中的資料
		m_projectSessionManager.setProject(project);
		m_projectSessionManager.setProjectInfoForm(projectInfo);

		//	取得 TaskBoard資訊
//		SprintBacklogMapper backlog;
//		try {
//			backlog = new SprintBacklogMapper(project, userSession);
//		} catch (Exception e) {
//			backlog = null;
//		}
		SprintBacklogLogic sprintBacklogLogic = new SprintBacklogLogic(project, userSession, null);
		SprintBacklogMapper sprintBacklogMapper = sprintBacklogLogic.getSprintBacklogMapper();

		TaskBoard board = null;
		if (sprintBacklogMapper != null) {
			board = new TaskBoard(sprintBacklogLogic, sprintBacklogMapper);
			request.setAttribute("TaskBoard", board);
			request.setAttribute("SprintID", board.getSprintID());
		} else {
			request.setAttribute("TaskBoard", board);
			request.setAttribute("SprintID", "null");
		}
		
		// setting ScrumRole
//		ScrumRoleManager srm = new ScrumRoleManager();
//		IAccount account = userSession.getAccount();
//		
//		srm.setScrumRoles(account);//reset Project<-->ScrumRole map
//		Map<String, ScrumRole> sr_map = srm.getScrumRoles(account);
//		ScrumRole sr = sr_map.get(project.getName());
		
		// setting ScrumRole
		IAccount account = userSession.getAccount();
		ScrumRoleLogic scrumRoleLogic = new ScrumRoleLogic();
		
		scrumRoleLogic.setScrumRoles(account);//reset Project<-->ScrumRole map
		Map<String, ScrumRole> sr_map = scrumRoleLogic.getScrumRoles(account);
		ScrumRole sr = sr_map.get(project.getName());
		
		if (sr.isGuest()) {
			request.getSession().setAttribute("isGuest", "true");
			log.info(account.getID() + " is a guest, view project: " + project.getName());
			
			return mapping.findForward("GuestOnly");
		} else {
			request.getSession().setAttribute("isGuest", "false");
			log.info(account.getID() + " is not a guest, view project: " + project.getName());
		}

		return mapping.findForward("SummaryView");
	}

//	/**
//	 * 比對專案和帳號
//	 * @param project
//	 * @param userSession
//	 * @return
//	 */
//	private boolean userIsExistedInProject( IProject project, IUserSession userSession ){
//		String adminAccountID = userSession.getAccount().getID();
//		if( adminAccountID.equalsIgnoreCase("admin")||adminAccountID.equalsIgnoreCase("administrator") ){
//			return true;
//		}
//		
//		AccountMapper accountMapper = new AccountMapper();
//		List<IAccount> projectMemberList = accountMapper.getProjectMemberList(project, userSession);
//		
////		AccountHelper accountHelper = new AccountHelper(project, userSession);
//		
////		List<IAccount> projectMemberList = accountHelper.getProjectMemberList();
//		boolean existedInProject = false;
//		for( IAccount account:projectMemberList ){
//			String accountID = account.getID();
//			if( userSession.getAccount().getID().equals(accountID) ){
//				existedInProject = true;
//				break;
//			}
//		}
//		return existedInProject;
//	}
//	
//	/**
//	 * 比對專案是否存在
//	 * @param projectID
//	 * @return
//	 */
//	private boolean projectIsExistedInWorkspace( String projectID ){
//		ProjectMapper projectMapper = new ProjectMapper();
//		List<IProject> projects = projectMapper.getAllProjectList();
//		
//		for( IProject project:projects ){
//			String PID = project.getName();
//			if( PID.equals( projectID ) ){
////				System.out.println( PID );
//				return true;
//			}
//		}
//		return false;
//	}
}