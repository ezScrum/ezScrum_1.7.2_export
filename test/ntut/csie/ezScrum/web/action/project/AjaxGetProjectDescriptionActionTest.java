package ntut.csie.ezScrum.web.action.project;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import ntut.csie.ezScrum.pic.core.IUserSession;
import ntut.csie.ezScrum.pic.internal.UserSession;
import ntut.csie.ezScrum.refactoring.manager.ProjectManager;
import ntut.csie.ezScrum.test.CreateData.AddUserToRole;
import ntut.csie.ezScrum.test.CreateData.CreateAccount;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.jcis.account.core.IAccount;
import ntut.csie.jcis.resource.core.IProject;
import servletunit.struts.MockStrutsTestCase;

public class AjaxGetProjectDescriptionActionTest extends MockStrutsTestCase {

	private ezScrumInfoConfig config = new ezScrumInfoConfig();
	private CreateProject CP;
	private CreateAccount CA;
	private int ProjectCount = 1;
	private int AccountCount = 1;
	private final String ActionPath_GetProjectDescription = "/GetProjectDescription";
	
	public AjaxGetProjectDescriptionActionTest(String testMethod) {
        super(testMethod);
    }
	
	private void setRequestPathInformation( String actionPath ){
    	setContextDirectory(new File(config.getBaseDirPath() + "/WebContent"));		// 設定讀取的 struts-config 檔案路徑
    	setServletConfigFile("/WEB-INF/struts-config.xml");
    	setRequestPathInfo( actionPath );
	}
	
	/**
	 * clean previous action info
	 */
/*	private void cleanActionInformation(){
		clearRequestParameters();
		this.response.reset();
	}*/
	
	/**
	 * 取得一般使用者的UserSession
	 * @param account
	 * @return
	 */
    private IUserSession getUserSession(IAccount account){
    	IUserSession userSession = new UserSession(account);
    	return userSession;
    }
    
	protected void setUp() throws Exception{
		//	刪除資料庫
		InitialSQL ini = new InitialSQL(config);
		ini.exe();
		
		// 新增Project
		this.CP = new CreateProject(this.ProjectCount);
		this.CP.exeCreate();
		
		// 新增使用者
		this.CA = new CreateAccount(this.AccountCount);
		this.CA.exe();
		
		super.setUp();
		
		this.setRequestPathInformation( this.ActionPath_GetProjectDescription );
    	// ============= release ==============
    	ini = null;
	}
	
	protected void tearDown()throws Exception{
		//	刪除資料庫
		InitialSQL ini = new InitialSQL(config);
		ini.exe();
		
		//	刪除外部檔案
		ProjectManager projectManager = new ProjectManager();
		projectManager.deleteAllProject();
		projectManager.initialRoleBase( this.config.getTestDataPath() );

		super.tearDown();
		
		ini = null;
		projectManager = null;
	}
	
	/**
	 * 測試admin登入專案後，是否能取得正確的專案資訊。
	 * response text : {"ID":"0","ProjectName":"TEST_PROJECT_1","ProjectDisplayName":"TEST_PROJECT_1","Commnet":"This is Test Project - 1","ProjectManager":"Project_Manager_1","AttachFileSize":"2","ProjectCreateDate":"2013/04/15-21:55:19"}
	 */
	public void testAdminAjaxGetProjectDescriptionAction(){
		IProject project = this.CP.getProjectList().get(0);
		String projectID = project.getName();
		
    	// ================ set URL parameter ========================
		request.setHeader("Referer", "?PID=" + projectID);	// SessionManager 會對URL的參數作分析 ,未帶入此參數無法存入session
		
		// ================ set session info ========================
		request.getSession().setAttribute("UserSession", this.config.getUserSession());
		
		// ================ 執行 action ======================
		actionPerform();
		
		// ================ assert ======================
		//	assert response text
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
		String expectProjectName = "TEST_PROJECT_1";
		String expectProjectDisplayName = "TEST_PROJECT_1";
		String expectComment = "This is Test Project - 1";
		String expectProjectManager = "Project_Manager_1";
		String expectAttachFileSize = "2";
		String expectProjectCreateDate = dateFormat.format( project.getProjectDesc().getCreateDate() );
		String actualResponseText = response.getWriterBuffer().toString();
		String expectResponseText = 
		"{" +
			"\"ID\":\"0\"," +
			"\"ProjectName\":\"" + expectProjectName + "\"," +
			"\"ProjectDisplayName\":\"" + expectProjectDisplayName + "\"," +
			"\"Commnet\":\"" + expectComment + "\"," +
			"\"ProjectManager\":\"" + expectProjectManager + "\"," +
			"\"AttachFileSize\":\"" + expectAttachFileSize + "\"," +
			"\"ProjectCreateDate\":\"" + expectProjectCreateDate + "\"" +
		"}";
		assertEquals(expectResponseText, actualResponseText);
	}
	
	/**
	 * 測試一般使用者在沒有加入該專案下，是否會回傳權限不足的警告訊息。
	 * response text:{"PermissionAction":{"ActionCheck":"false", "Id":0}}
	 */
	public void testUserAjaxGetProjectDescriptionAction_NotInProject(){
		String projectID = this.CP.getProjectList().get(0).getName();
		IAccount account = this.CA.getAccountList().get(0);
		
    	// ================ set URL parameter ========================
		request.setHeader("Referer", "?PID=" + projectID);	// SessionManager 會對URL的參數作分析 ,未帶入此參數無法存入session
		
		// ================ set session info ========================
		request.getSession().setAttribute("UserSession", this.getUserSession(account));
		
		// ================ 執行 action ======================
		actionPerform();
		
		// ================ assert ======================
		//	assert response text
		String actualResponseText = response.getWriterBuffer().toString();
		String expectResponseText = 
			"{" +
				"\"PermissionAction\":" +"{\"" +
						"ActionCheck\":\"false\", " +
						"\"Id\":0" +
				"}" +
			"}";
		assertEquals(expectResponseText, actualResponseText);
	}
	
	/**
	 * 測試一般使用者登入專案後，是否能取得正確的專案資訊。
	 * response text : {"ID":"0","ProjectName":"TEST_PROJECT_1","ProjectDisplayName":"TEST_PROJECT_1","Commnet":"This is Test Project - 1","ProjectManager":"Project_Manager_1","AttachFileSize":"2","ProjectCreateDate":"2013/04/15-21:55:19"}
	 */
	public void testUserAjaxGetProjectDescriptionAction_InProject(){
		IProject project = this.CP.getProjectList().get(0);
		String projectID = project.getName();
		IAccount account = this.CA.getAccountList().get(0);
		
		AddUserToRole addUserToRole = new AddUserToRole(this.CP, this.CA);
		addUserToRole.exe_ST();
		
    	// ================ set URL parameter ========================
		request.setHeader("Referer", "?PID=" + projectID);	// SessionManager 會對URL的參數作分析 ,未帶入此參數無法存入session
		
		// ================ set session info ========================
		request.getSession().setAttribute("UserSession", this.getUserSession(account));
		
		// ================ 執行 action ======================
		actionPerform();
		
		// ================ assert ======================
		//	assert response text
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
		String expectProjectName = "TEST_PROJECT_1";
		String expectProjectDisplayName = "TEST_PROJECT_1";
		String expectComment = "This is Test Project - 1";
		String expectProjectManager = "Project_Manager_1";
		String expectAttachFileSize = "2";
		String expectProjectCreateDate = dateFormat.format( project.getProjectDesc().getCreateDate() );
		String actualResponseText = response.getWriterBuffer().toString();
		String expectResponseText = 
		"{" +
			"\"ID\":\"0\"," +
			"\"ProjectName\":\"" + expectProjectName + "\"," +
			"\"ProjectDisplayName\":\"" + expectProjectDisplayName + "\"," +
			"\"Commnet\":\"" + expectComment + "\"," +
			"\"ProjectManager\":\"" + expectProjectManager + "\"," +
			"\"AttachFileSize\":\"" + expectAttachFileSize + "\"," +
			"\"ProjectCreateDate\":\"" + expectProjectCreateDate + "\"" +
		"}";
		assertEquals(expectResponseText, actualResponseText);
	}
}
