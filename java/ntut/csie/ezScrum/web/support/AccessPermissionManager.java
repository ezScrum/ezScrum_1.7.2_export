package ntut.csie.ezScrum.web.support;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import ntut.csie.ezScrum.pic.core.IUserSession;
import ntut.csie.ezScrum.pic.core.ScrumRole;
import ntut.csie.ezScrum.web.logic.ProjectLogic;
import ntut.csie.ezScrum.web.logic.ScrumRoleLogic;
import ntut.csie.ezScrum.web.mapper.AccountMapper;
import ntut.csie.jcis.account.core.IAccount;
import ntut.csie.jcis.account.core.IPermission;

public class AccessPermissionManager {
	
	static public void setupPermission(HttpServletRequest request, IUserSession userSession){
        //check create project permission
		IAccount account = userSession.getAccount();

//        IAccountManager manager = AccountFactory.getManager();
//        IPermission permCreateProject = manager.getPermission("system_createProject");
//      IPermission permAdmin = manager.getPermission("system", "admin");
        
		AccountMapper accountMapper = new AccountMapper();
        IPermission permCreateProject = accountMapper.getPermission("system_createProject");
        IPermission permAdmin = accountMapper.getPermission("system", "admin");

        //設定使用者是否擁有建立專案的權限
        request.getSession().setAttribute("CreateProject", Boolean.valueOf(account.checkPermission(permCreateProject)));

        //設定使用者是否為系統管理員的權限
        request.getSession().setAttribute("Administration", Boolean.valueOf(account.checkPermission(permAdmin)));
		
        //判斷對於project而言,使用者能使用的功能權限
        ProjectLogic projectLogic = new ProjectLogic();
        request.getSession().setAttribute("FunctionAccess", projectLogic.getProjectPermissionMap(account));
        
//		//get the role of scrum *author: py2k*
//        ScrumRoleManager sr_manager = new ScrumRoleManager();
//        //透過專案資訊得到對應的權限
//        Map<String, ScrumRole> scrumRoles = sr_manager.getScrumRoles(account);
        
		//get the role of scrum *author: py2k*
        //透過專案資訊得到對應的權限
        Map<String, ScrumRole> scrumRoles = (new ScrumRoleLogic()).getScrumRoles(account);
        
        //設定User role Session 
        request.getSession().setAttribute("ScrumRoles", scrumRoles);
	}
}
