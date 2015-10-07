package ntut.csie.ezScrum.web.helper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ntut.csie.ezScrum.iteration.core.ScrumEnum;
import ntut.csie.ezScrum.pic.core.IUserSession;
import ntut.csie.ezScrum.web.dataObject.UserInformation;
import ntut.csie.ezScrum.web.logic.ProjectLogic;
import ntut.csie.ezScrum.web.logic.ScrumRoleLogic;
import ntut.csie.ezScrum.web.mapper.AccountMapper;
import ntut.csie.ezScrum.web.support.TranslateUtil;
import ntut.csie.jcis.account.core.IAccount;
import ntut.csie.jcis.account.core.IActor;
import ntut.csie.jcis.account.core.IPermission;
import ntut.csie.jcis.account.core.IRole;
import ntut.csie.jcis.resource.core.IProject;

public class AccountHelper {

	// from GetAssignedProjectAction
	private final String SYSTEM = "system";
	private AccountMapper accountMapper;
	
	public AccountHelper(){
		this.accountMapper = new AccountMapper();
	}
	
	public String validateAccountID(String id) {

		// 判斷帳號是否符合只有英文+數字的格式
		Pattern p = Pattern.compile("[0-9a-zA-Z_]*");
		Matcher m = p.matcher(id);
		boolean b = m.matches();

		// 若帳號可建立且ID format正確 則回傳true
		AccountMapper am = new AccountMapper();
		if (b && !am.isAccountExist(id) && !id.isEmpty()) {			
			return "true";
		}

		return "false";
	}
		
	/**
	 * 進行帳號建立的動作, 並且將帳號 Assign Roles, 建立完畢執行儲存檔案
	 */
	public IAccount createAccount(UserInformation userInformation, String roles) {
//		AccountMapper accountMapper = new AccountMapper();
		IAccount account = this.accountMapper.createAccount(userInformation, roles);
		return account;
	}
	
	public IAccount updateAccount(IUserSession session, UserInformation userInformation) {
//		AccountMapper accountMapper = new AccountMapper();
		IAccount updateAccount = this.accountMapper.updateAccount(session, userInformation);
		return updateAccount;
	}
	
	public void deleteAccount(IUserSession session, String id) {
//		AccountMapper am = new AccountMapper();
		this.accountMapper.deleteAccount(session, id);
	}	
		
	/*
	 * Assign Role
	 */
	public String getAssignedProject(String userId) {		
//		IAccount account = (new AccountMapper()).getAccountById(userId);
		IAccount account = this.accountMapper.getAccountById(userId);
		IRole[] roleList = account.getRoles();
		List<String> assignedProject = new ArrayList<String>();
		
		StringBuilder sb = new StringBuilder();
		
		// 取得帳號的Assign資訊
		sb.append("<AssignRoleInfo>");
		sb.append("<Account>");
		// Account Info
		sb.append("<ID>" + account.getID() + "</ID>");
		sb.append("<Name>" + account.getName() + "</Name>");
		
		// Assign Roles 
		sb.append("<Roles>");
		for(IRole role: roleList){
			IPermission[] permissions = role.getPermisions();
			if(permissions!=null){
				for(IPermission permission: permissions){
					String resource = permission.getResourceName();
					String operation = permission.getOperation();
					if(resource.equals("system")&&(operation.equals("read")||operation.equals("createProject")))
						continue;
					sb.append("<Assigned>");
					sb.append("<Resource>" + resource + "</Resource>");
					sb.append("<Operation>" + operation + "</Operation>");
					sb.append("</Assigned>");
					//記錄此project為assigned
					assignedProject.add(resource);
				}
			}
		}
		sb.append("</Roles>");
		
		// 取得尚未被Assign的專案資訊
		ProjectLogic projectLogic = new ProjectLogic();
		List<IProject> projects = projectLogic.getAllProjects();
		
        for(IProject project: projects){
			String resource = project.getName();
			//如果project沒有被assigned權限，則代表為unassigned的project
	        if(!assignedProject.contains(resource))
	        	sb.append("<Unassigned><Resource>" +  resource + "</Resource></Unassigned>");
		}
        //判斷是否為administrator
        if(!assignedProject.contains(this.SYSTEM))
        	sb.append("<Unassigned><Resource>" + this.SYSTEM + "</Resource></Unassigned>");					
                
        sb.append("</Account>");
		sb.append("</AssignRoleInfo>");		
		
		return sb.toString();
	}	

	public IAccount assignRole_add(IUserSession session, String id, String res, String op) throws Exception {
		IAccount account = this.accountMapper.getAccountById(id);
		IRole[] roles = account.getRoles();
		String role = "";
		
		if(res.equals(ScrumEnum.SYSTEM))
			role = op;
		else	
			role = res + "_" + op;
		
		List<String> roleList = this.translateRoleString(roles, role);
		
		//	進行帳號更新的動作, 並且將帳號 Assign Roles
		this.accountMapper.assignRole_add(session, account, roleList, id, res, op);
		
		account = this.accountMapper.getAccountById(id);
		(new ScrumRoleLogic()).setScrumRoles( session.getAccount() );//reset Project<-->ScrumRole map
		
		return account;
	}	
		
	public IAccount assignRole_remove(IUserSession session, String id, String res, String op) throws Exception {
		IAccount account = this.accountMapper.getAccountById(id);		
		IRole[] roles = account.getRoles();
		
		String role = "";
		if(res.equals(ScrumEnum.SYSTEM))
			role = op;
		else	
			role = res + "_" + op;
		
		List<String> roleList = this.translateRoleStringWithCheck(roles, role);
		
		//	進行帳號更新的動作, 並且將帳號 Remove Roles
		this.accountMapper.assignRole_remove(session, account, id, roleList, res);
		
		account = this.accountMapper.getAccountById(id);
		(new ScrumRoleLogic()).setScrumRoles( session.getAccount() );//reset Project<-->ScrumRole map
		
		return account;
	}
	
	public String getAccountXML(IAccount account) {
		List<IActor> accountList = new LinkedList<IActor>();
		accountList.add(account);
		return this.getXmlstring(accountList);
	}
	
	public String getAccountListXML() {
		AccountMapper am = new AccountMapper();		
		List<IActor> accountList = am.getAccountList();		
		return this.getXmlstring(accountList);
	}

	public String getManagementView(IAccount account) {
		String result = "";		
//		AccountMapper am = new AccountMapper();
		IPermission permAdmin = this.accountMapper.getPermission("system", "admin");
		
		if (Boolean.valueOf(account.checkPermission(permAdmin))) {
			result = "Admin_ManagementView";
		} else {
			result = "User_ManagementView";
		}
		
		return result;
	}		
	
	/**
	 * 將Roles String轉成Role String List
	 */
	private List<String> translateRoleStringWithCheck(IRole[] roles, String role) {
		List<String> roleList = new ArrayList<String>();
		// default
		if(roles!=null){
			for (IRole irole : roles) {
				if( ! irole.getRoleId().equals(role))
					roleList.add(irole.getRoleId());
			}
		}
		return roleList;
	}	
	
	/**
	 * 將Roles String轉成Role String List
	 */
	private List<String> translateRoleString(IRole[] roles, String role) {
		List<String> roleList = new ArrayList<String>();
		// default
		if(roles!=null){
			for (IRole irole : roles) {
				roleList.add(irole.getRoleId());
			}
		}
		
		roleList.add(role);
		return roleList;
	}		
	
	private String getXmlstring(List<IActor> actors) {
		Iterator<IActor> iter = actors.iterator();
		// write projects to XML format
		StringBuilder sb = new StringBuilder();
		sb.append("<Accounts>");
		while (iter.hasNext()) {
			IAccount account = (IAccount) iter.next();
			sb.append("<Account>");
			sb.append("<ID>" + account.getID() + "</ID>");				
			sb.append("<Name>" + account.getName() + "</Name>");				
			sb.append("<Mail>" + account.getEmail() + "</Mail>");
			String mail = account.getEmail();
			sb.append("<Roles>" + TranslateUtil.getRolesString(account.getRoles()) + "</Roles>");
			sb.append("<Enable>" + account.getEnable()+ "</Enable>");
			String enable = account.getEnable();
			if (enable==null||enable.equalsIgnoreCase("true"))
				enable = "true";
			else
				enable = "false";
			if (mail == null)
				mail = "";
			sb.append("</Account>");
		}
	    sb.append("</Accounts>");
	    
		return sb.toString();
	}	
	
}
