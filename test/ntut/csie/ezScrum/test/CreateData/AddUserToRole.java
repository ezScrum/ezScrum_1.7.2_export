package ntut.csie.ezScrum.test.CreateData;

import java.util.ArrayList;
import java.util.List;

import ntut.csie.ezScrum.iteration.core.ScrumEnum;
import ntut.csie.ezScrum.web.control.MantisAccountManager;
import ntut.csie.ezScrum.web.dataObject.UserInformation;
import ntut.csie.ezScrum.web.helper.AccountHelper;
import ntut.csie.jcis.account.core.AccountFactory;
import ntut.csie.jcis.account.core.IAccount;
import ntut.csie.jcis.account.core.IAccountManager;
import ntut.csie.jcis.account.core.IRole;
import ntut.csie.jcis.account.core.LogonException;
import ntut.csie.jcis.resource.core.IProject;

public class AddUserToRole {
	private CreateProject CP;
	private CreateAccount CA;
	
	private IAccount theAccount = null;
	private IProject theProject = null;
	
	private ezScrumInfoConfig config = new ezScrumInfoConfig();
	
	public AddUserToRole(CreateProject cp, CreateAccount ca) {
		this.CP = cp;
		this.CA = ca;
		
		this.theAccount = this.CA.getAccountList().get(0);
		this.theProject = this.CP.getProjectList().get(0);
	}
	
	/**
	 * 指定目前要新增的專案 Index
	 */
	public void setProjectIndex(int index) {
		if (index < this.CP.getProjectList().size()) {
			this.theProject = this.CP.getProjectList().get(index);
		}
	}
	
	/**
	 * 指定目前要新增的Account Index
	 */
	public void setAccountIndex(int index) {
		if (index < this.CA.getAccountCount()) {
			this.theAccount = this.CA.getAccountList().get(index);
		}
	}
	
	/**
	 * 取得目前指定的 Project
	 */
	public IProject getNowProject() {
		return this.theProject;
	}
	
	/**
	 * 取得目前指定的 Account
	 */
	public IAccount getNoeAccount() {
		return this.theAccount;
	}
	
	/**
	 * 將目前指定的 Account 加入 Admin 角色
	 */
	public void exe_System() {
		String res = ScrumEnum.SYSTEM;
		String op = ScrumEnum.SCRUMROLE_ADMIN;
		updateAccount(res, op);
	}
	
	/**
	 * 將目前指定的 Account 加入 Product Owner 角色
	 */
	public void exe_PO() {
		String res = this.theProject.getName();
		String op = ScrumEnum.SCRUMROLE_PRODUCTOWNER;
		updateAccount(res, op);
	}
	
	/**
	 * 將目前指定的 Account 加入 Scrum Team 角色
	 */
	public void exe_ST() {
		String res = this.theProject.getName();
		String op = ScrumEnum.SCRUMROLE_SCRUMTEAM;
		updateAccount(res, op);
	}
	
	/**
	 * 將目前指定的 Account 加入 Scrum Master 角色
	 */
	public void exe_SM() {
		String res = this.theProject.getName();
		String op = ScrumEnum.SCRUMROLE_SCRUMMASTER;
		updateAccount(res, op);
	}
	
	/**
	 * 將目前指定的 Account 加入 Stakeholder 角色
	 */
	public void exe_Sh() {
		String res = this.theProject.getName();
		String op = ScrumEnum.SCRUMROLE_STAKEHOLDER;
		updateAccount(res, op);
	}
	
	/**
	 * 將目前指定的 Account 加入 Guest 角色
	 */
	public void exe_Guest() {
		String res = this.theProject.getName();
		String op = ScrumEnum.SCRUMROLE_GUEST;
		updateAccount(res, op);
	}
	
	/**
	 * 將目前 Account 指定為系統管理員
	 */
	public void setNowAccountIsSystem() {
		IAccountManager am = AccountFactory.getManager();
		IAccount account = am.getAccount(ScrumEnum.SCRUMROLE_ADMIN);
	
		this.theAccount = account;
	}
	
	/**
	 * 將目前 Account 指定為disable
	 */
	public void setEnable(CreateAccount CA, int index, boolean isEnable) {
		MantisAccountManager accountManager;
		try {
			accountManager = new MantisAccountManager(config.getUserSession());
			IAccount account = CA.getAccountList().get(index);
			account.setEnable(String.valueOf(isEnable));
			accountManager.updateUserProfile(account);
		} catch(Exception ex) {}
	}
	
	private void updateAccount(String res, String op) {
		IAccountManager am = AccountFactory.getManager();
		IRole[] roles = this.theAccount.getRoles();
		String role = "";
		if(res.equals(ScrumEnum.SYSTEM))
			role = op;
		else	
			role = res + "_" + op;
		
		List<String> roleList = translateRoleString(roles, role);
		
		// 更新 mantis 資訊的部分
		MantisAccountManager accountManager;
		try {
			accountManager = new MantisAccountManager(config.getUserSession());
			accountManager.addReleation(this.theAccount, res, op);
		} catch (LogonException e) {
			e.printStackTrace();
			System.out.println("class: AddUserToRole, method: updateAccount, Logon_exception: " + e.toString());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("class: AddUserToRole, method: updateAccount, exception: " + e.toString());
		}
		// 更新 ezScrum local 的部分
		am.updateAccountRoles(this.theAccount.getID(), roleList);
		am.save();
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
}
