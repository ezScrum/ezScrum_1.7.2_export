package ntut.csie.ezScrum.web.mapper;

import java.util.ArrayList;
import java.util.List;

import ntut.csie.ezScrum.pic.core.IUserSession;
import ntut.csie.ezScrum.web.control.MantisAccountManager;
import ntut.csie.ezScrum.web.dataObject.UserInformation;
import ntut.csie.ezScrum.web.support.TranslateUtil;
import ntut.csie.jcis.account.core.AccountFactory;
import ntut.csie.jcis.account.core.IAccount;
import ntut.csie.jcis.account.core.IAccountManager;
import ntut.csie.jcis.account.core.IActor;
import ntut.csie.jcis.account.core.IPermission;
import ntut.csie.jcis.account.core.IRole;
import ntut.csie.jcis.account.core.LogonException;
import ntut.csie.jcis.resource.core.IProject;

public class AccountMapper {

	private String[] operation = { "ProductOwner", "ScrumMaster", "ScrumTeam", "Stakeholder", "Guest" };
	
	public AccountMapper() {
	}
	
	public IAccount createAccount(UserInformation userInformation, String roles) {	
		String id = userInformation.getId();
		String realName = userInformation.getName();
		String password = userInformation.getPassword();
		String email = userInformation.getEmail();
		String enable = userInformation.getEnable();
		
//		IAccountManager am = AccountFactory.getManager();
		IAccountManager am = this.getManager();
		// 建立帳號(RoleBase部分)
		IAccount account = AccountFactory.createAccount(id, realName, password, true);
		account.setEmail(email);
		
		// 預防 null 的發生 -> when?
		if ( (enable == null) || (enable.length() == 0) ) {
			enable = "true";
		}		
		account.setEnable(enable);
		
		am.addAccount(account);
		account = am.getAccount(id);
		List<String> roleList = TranslateUtil.translateRoleString(roles);
	
		// 確認已成功加入Assign roles
		if (account != null) {
			am.updateAccountRoles(id, roleList);
			am.save();
		}
		return account;
	}
	
	
	/**
	 * 進行編輯帳號的動作，並且將帳號更新角色，in 資料庫 和外部檔案資訊( RoleBase )的部分
	 * @param session
	 * @param userInformation
	 * @return
	 */
	public IAccount updateAccount(IUserSession session, UserInformation userInformation) {
//		IAccount account =  this.updateAccountInWorkspace(userInformation);
		this.updateAccountInWorkspace(userInformation);
		IAccount account = this.getAccountById( userInformation.getId() );
		this.updateAccountInDatabase(session, account);
		return account;
	}
	
	/**
	 * 進行編輯帳號的動作，並且將帳號更新角色 in mantis 資訊的部分
	 * @param session
	 * @param updateAccount
	 */
	private void updateAccountInDatabase(IUserSession session, IAccount updateAccount) {
		MantisAccountManager accountManager = new MantisAccountManager(session);
		try {
			accountManager.updateUserProfile(updateAccount);
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
	}
	
	/**
	 * 進行編輯帳號的動作，並且將帳號更新角色 in 外部檔案資訊的部分
	 * @param userInformation
	 */
	private void updateAccountInWorkspace(UserInformation userInformation) {
		String id = userInformation.getId();
		String name = userInformation.getName();
		String pwd = userInformation.getPassword();
		String mail = userInformation.getEmail();
		String enable = userInformation.getEnable();
		
//		IAccountManager am = AccountFactory.getManager();
//		IAccount updateAccount = am.getAccount(id);
		IAccountManager am = this.getManager();
		IAccount updateAccount = am.getAccount(id);
		
		// 預防 null 的發生
		if ( (enable == null) || (enable.length() == 0) ) {
			enable = updateAccount.getEnable();
		}
		
		// no password, use the default password
		if ( (pwd == null) || (pwd.length()==0) || pwd.equals("") ) {
			pwd = updateAccount.getPassword();		// get default password
			am.updateAccountData(id, name, pwd, mail, enable, false);	// false 不經過Md5編碼
		} else {
			am.updateAccountData(id, name, pwd, mail, enable, true);	// true 經過Md5編碼
		}

		am.save();
	}
	
	/**
	 * 刪除 account in "外部檔案" 和 "資料庫" 資訊的部分
	 */
	public void deleteAccount( IUserSession session, String id ) {
		this.deleteAccountInDatabase(session, id);
		this.deleteAccountInWorkspace(id);
	}
	
	/**
	 * 刪除 account in mantis 資訊的部分
	 * @param session
	 * @param id
	 */
	private void deleteAccountInDatabase(IUserSession session, String id) {
		MantisAccountManager accountManager = new MantisAccountManager(session);
		try {
			accountManager.deleteAccount(this.getManager(), id);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * 刪除 account in ezScrum local 的部分
	 * @param id
	 */
	private void deleteAccountInWorkspace(String id) {
//		IAccountManager am = AccountFactory.getManager();
		// 刪除帳號, 包含群組內與Assign的資料
		this.getManager().removeAccount(id);
		this.getManager().save();
	}
	
	/**
	 * 進行帳號更新的動作, 並且將帳號 Assign Roles, 建立完畢執行儲存檔案 in "外部檔案" 和 "資料庫"
	 */
	public void assignRole_add(IUserSession session, IAccount account, List<String> roleList, String id, String res, String op) throws Exception {
		this.updateAccountRoleInDatabase(session, account, res, op);
		this.updateAccountRoleInWorkspace(id, roleList);	
	}
	
	/**
	 * 進行帳號更新的動作, 並且將帳號 Assign Roles, 建立完畢執行儲存檔案 in "資料庫"
	 * @param session
	 * @param account
	 * @param res
	 * @param op
	 * @throws Exception
	 */
	private void updateAccountRoleInDatabase(IUserSession session, IAccount account, String res, String op) throws Exception {
		MantisAccountManager accountManager = new MantisAccountManager(session);
		accountManager.addReleation(account, res, op);		
	}
	
	/**
	 * 進行帳號更新的動作, 並且將帳號 Assign Roles, 建立完畢執行儲存檔案 in "外部檔案"
	 * @param id
	 * @param roleList
	 */
	private void updateAccountRoleInWorkspace(String id, List<String> roleList) {
//		IAccountManager am = AccountFactory.getManager();
		this.getManager().updateAccountRoles(id,roleList);
		this.getManager().save();
	}
	
	/**
	 * 進行帳號更新的動作, 並且將帳號 Remove Roles, 建立完畢執行儲存檔案 in "外部檔案" 和 "資料庫"
	 * @param session
	 * @param account
	 * @param id
	 * @param roleList
	 * @param res
	 * @throws Exception 
	 */
	public void assignRole_remove(IUserSession session, IAccount account, String id, List<String> roleList, String res) throws Exception {
		this.removeAccountRoleInDatabase(session, account, res);
		this.removeAccountRoleInWorkspace(id, roleList);		
	}
	
	/**
	 * 進行帳號更新的動作, 並且將帳號 Remove Roles, 建立完畢執行儲存檔案 in "外部檔案"
	 * @param id
	 * @param roleList
	 */
	private void removeAccountRoleInWorkspace(String id, List<String> roleList) {
//		IAccountManager am = AccountFactory.getManager();
		this.getManager().updateAccountRoles(id, roleList);
		this.getManager().save();
	}
	
	/**
	 * 進行帳號更新的動作, 並且將帳號 Remove Roles, 建立完畢執行儲存檔案 in "資料庫"
	 * @param session
	 * @param account
	 * @param res
	 * @throws Exception
	 */
	private void removeAccountRoleInDatabase(IUserSession session, IAccount account, String res) throws Exception {
		MantisAccountManager accountManager = new MantisAccountManager(session);
		accountManager.removeReleation(account, res);		
	}
	
	/**
	 * 建立 rolebase 的各專案的 permission
	 * @throws Exception 
	 */
	public void createPermission(IProject p) throws Exception {
		String resource = p.getName();
		
//		IAccountManager am = AccountFactory.getManager();
		IAccountManager am = this.getManager();
		for (String oper : operation) {
			String name = resource + "_" + oper;
//			IPermission oriPerm = AccountFactory.getManager().getPermission(name);
			IPermission oriPerm = am.getPermission(name);
			if (oriPerm == null) {
				IPermission perm = AccountFactory.createPermission(name, resource, oper);
				am.addPermission(perm);
				perm = am.getPermission(name);
				
				// 若perm為空代表沒新增成功
				if (perm == null) {
					am.referesh();
					throw new Exception("建立Permission失敗!!");
				}
			
				am.save();
			}
		}
	}
	
	public void createRole(IProject p) throws Exception {
		String resource = p.getName();
		
//		IAccountManager am = AccountFactory.getManager();
		IAccountManager am = this.getManager();
		for (String oper : operation) {
			String name = resource + "_" + oper;
			IRole oriRole = AccountFactory.getManager().getRole(name);
			if (oriRole == null) {
				IRole role = AccountFactory.createRole(name, name);
				am.addRole(role);
				List<String> permissionNameList = new ArrayList<String>();
				permissionNameList.add(name);
				// 加入成功則進行群組成員與Role的設置
				if (am.getRole(name) != null) {
					am.updateRolePermission(name, permissionNameList);
				} else {
					throw new Exception("建立Role失敗!!");
				}
				// 儲存檔案
				am.save();
			}
		}
	}
	
	//
	private IAccountManager getManager() {
		return AccountFactory.getManager();
	}
	
	public void releaseManager(){
		AccountFactory.releaseManager();
	}
	
	/**
	 * 取得角色在專案中的權限
	 * @param project
	 * @param role
	 * @return
	 */
	public IPermission getPermission(String project, String role) {
//		IAccountManager manager = AccountFactory.getManager();
		return this.getManager().getPermission(project, role);
	}
	
	public IPermission getPermission( String role ){
//		IAccountManager manager = AccountFactory.getManager();
		return  this.getManager().getPermission( role );
	}
	
	/**
	 * 若帳號可建立且ID format正確 則回傳true
	 * @param id
	 * @return
	 */
	public boolean isAccountExist(String id) {
//		IAccountManager am = AccountFactory.getManager();
		return this.getManager().isAccountExist(id);
	}
	
	public IAccount getAccountById(String id) {
//		IAccountManager am = AccountFactory.getManager();
		return this.getManager().getAccount(id);
	}
	
	public List<IActor> getAccountList() {
//		IAccountManager am = AccountFactory.getManager();
		return  this.getManager().getAccountList();		
	}
	
    public void confirmAccount(String id, String password) throws LogonException{
//    	IAccountManager manager = AccountFactory.getManager();
    	this.getManager().confirmAccount(id, password);
    }
    
}
