package ntut.csie.ezScrum.web.control;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import ntut.csie.ezScrum.iteration.core.ScrumEnum;
import ntut.csie.ezScrum.pic.core.IUserSession;
import ntut.csie.ezScrum.pic.core.ScrumRole;
import ntut.csie.ezScrum.test.CreateData.AddUserToRole;
import ntut.csie.ezScrum.test.CreateData.CopyProject;
import ntut.csie.ezScrum.test.CreateData.CreateAccount;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.ezScrum.web.mapper.ProjectMapper;
import ntut.csie.ezScrum.web.mapper.ScrumRoleMapper;
import ntut.csie.jcis.resource.core.IProject;

public class AccountHelperTest extends TestCase {
	private CreateProject CP;
	private int ProjectCount = 1;
//	private MantisAccountMapper helper = null;
	private ProjectMapper projectMapper = null;
	private ezScrumInfoConfig config = new ezScrumInfoConfig();
	private IUserSession userSession = null;
	private IProject project = null;
	
	public AccountHelperTest(String testMethod) {
        super(testMethod);
    }
	
	protected void setUp() throws Exception {
		InitialSQL ini = new InitialSQL(config);
		ini.exe();											// 初始化 SQL
		
		// 新增Project
		this.CP = new CreateProject(this.ProjectCount);
		this.CP.exeCreate();
		
		// 建構 helper
		//this.helper = new MantisAccountMapper(this.CP.getProjectList().get(0), config.getUserSession());
		this.projectMapper = new ProjectMapper();
		this.userSession = config.getUserSession();
		this.project = this.CP.getProjectList().get(0);
		
		super.setUp();
		
		// release
		ini = null;
    }

    protected void tearDown() throws IOException, Exception {
		InitialSQL ini = new InitialSQL(config);
		ini.exe();											// 初始化 SQL
		
		CopyProject copyProject = new CopyProject(this.CP);
//    	copyProject.exeDelete_Project();					// 刪除測試檔案
    	copyProject.exeCopy_Delete_Project();
    	
    	super.tearDown();
    	
    	// release
    	ini = null;
    	copyProject = null;
    	this.CP = null;
//    	this.helper = null;
    	this.projectMapper = null;
    	this.config = null;
    }
    
    public void testgetScrumWorkerList() {
    	CreateAccount ca = new CreateAccount(4);		// create 4 accounts
    	ca.exe();
//    	IAccount ac_Stakeholder = ca.getAccountList().get(0);
//    	IAccount ac_ProductOwner = ca.getAccountList().get(1);
//    	IAccount ac_ScrumMaster = ca.getAccountList().get(2);
//    	IAccount ac_ScrumTeam = ca.getAccountList().get(3);
    	
    	List<String> accountIDList = this.projectMapper.getProjectScrumWorkerList(this.userSession, this.project);
    	assertEquals(1, accountIDList.size());		// 多一個是因為空白
    	
    	AddUserToRole autr = new AddUserToRole(this.CP, ca);
    	autr.setAccountIndex(0);
    	autr.exe_Sh();
    	autr.setAccountIndex(1);
    	autr.exe_PO();
    	autr.setAccountIndex(2);
    	autr.exe_SM();
    	autr.setAccountIndex(3);
    	autr.exe_ST();
    	
    	IProject p = this.CP.getProjectList().get(0);
    	updatePermission(p.getName(), "Stakeholder", false);	// 將 Stakeholder 角色設定成不能存取 TaskBoard
    	updatePermission(p.getName(), "ProductOwner", false);	// 將 PO 角色設定成不能存取 TaskBoard
    	
    	accountIDList = this.projectMapper.getProjectScrumWorkerList(this.userSession, this.project);
    	assertEquals(3, accountIDList.size());		// 可以領取工作的角色剩下兩個，多一個是因為多一個空白
    	assertTrue(accountIDList.contains(ca.getAccount_ID(3)));
    	assertTrue(accountIDList.contains(ca.getAccount_ID(4)));
    	
    	
    	updatePermission(p.getName(), "ProductOwner", true);	// 將 PO 角色設定成能存取 TaskBoard
    	accountIDList = this.projectMapper.getProjectScrumWorkerList(this.userSession, this.project);
    	assertEquals(4, accountIDList.size());		// 可以領取工作的角色剩下四個，多一個是因為多一個空白
    	assertTrue(accountIDList.contains(ca.getAccount_ID(2)));
    	assertTrue(accountIDList.contains(ca.getAccount_ID(3)));
    	assertTrue(accountIDList.contains(ca.getAccount_ID(4)));
    }
    
	private void updatePermission(String res, String role, boolean accessTaskBoard) {
		List<String> permissionsList = new LinkedList<String>();
		permissionsList.add(ScrumEnum.ACCESS_PRODUCTBACKLOG);
		permissionsList.add(ScrumEnum.ACCESS_RELEASEPLAN);
		permissionsList.add(ScrumEnum.ACCESS_SPRINTPLAN);
		permissionsList.add(ScrumEnum.ACCESS_SPRINTBACKLOG);
		
		if (accessTaskBoard) {
			permissionsList.add(ScrumEnum.ACCESS_TASKBOARD);
		}
		
		permissionsList.add(ScrumEnum.ACCESS_UNPLANNED);
		permissionsList.add(ScrumEnum.ACCESS_RETROSPECTIVE);
		permissionsList.add(ScrumEnum.ACCESS_REPORT);
		permissionsList.add(ScrumEnum.ACCESS_EDITPROJECT);
		
		ScrumRole scrumrole = new ScrumRole(res, role);
		scrumrole = setAttribute(scrumrole, permissionsList);
//		ScrumRoleManager manager = new ScrumRoleManager();
//		try {
//			manager.update(scrumrole);
//		} catch (Exception e) {
//			System.out.println("class: AccountHelperTest, method: updatePermission, exception: " + e.toString());
//			e.printStackTrace();
//		}
		ScrumRoleMapper scrumRoleMapper = new ScrumRoleMapper();
		try {
			scrumRoleMapper.update(scrumrole);
		} catch (Exception e) {
			System.out.println("class: AccountHelperTest, method: updatePermission, exception: " + e.toString());
			e.printStackTrace();
		}
	}
	
	private ScrumRole setAttribute(ScrumRole role, List<String> attributeList){
		for(String attribute: attributeList){
			if(attribute.equals(ScrumEnum.ACCESS_PRODUCTBACKLOG))
				role.setAccessProductBacklog(Boolean.TRUE);
			else if(attribute.equals(ScrumEnum.ACCESS_RELEASEPLAN))
				role.setAccessReleasePlan(Boolean.TRUE);
			else if(attribute.equals(ScrumEnum.ACCESS_SPRINTPLAN))
				role.setAccessSprintPlan(Boolean.TRUE);
			else if(attribute.equals(ScrumEnum.ACCESS_SPRINTBACKLOG))
				role.setAccessSprintBacklog(Boolean.TRUE);
			else if(attribute.equals(ScrumEnum.ACCESS_TASKBOARD))
				role.setAccessTaskBoard(Boolean.TRUE);
			else if(attribute.equals(ScrumEnum.ACCESS_UNPLANNED))
				role.setAccessUnplannedItem(Boolean.TRUE);
			else if(attribute.equals(ScrumEnum.ACCESS_RETROSPECTIVE))
				role.setAccessRetrospective(Boolean.TRUE);
			else if(attribute.equals(ScrumEnum.ACCESS_REPORT))
				role.setReadReport(Boolean.TRUE);
			else if(attribute.equals(ScrumEnum.ACCESS_EDITPROJECT))
				role.setEditProject(Boolean.TRUE);
		}
		return role;
	}
}
