package ntut.csie.ezScrum.web.action.backlog.product;

import java.io.File;
import java.io.IOException;

import ntut.csie.ezScrum.refactoring.manager.ProjectManager;
import ntut.csie.ezScrum.test.CreateData.CreateProductBacklog;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.jcis.resource.core.IProject;
import servletunit.struts.MockStrutsTestCase;

public class AjaxEditStoryActionTest extends MockStrutsTestCase {
	private CreateProject CP;
	private ezScrumInfoConfig config = new ezScrumInfoConfig();
	private final String ACTION_PATH = "/ajaxEditStory";
	private IProject project;
	
	public AjaxEditStoryActionTest(String testName) {
		super(testName);
	}
	
	protected void setUp() throws Exception {
		//	刪除資料庫
		InitialSQL ini = new InitialSQL(config);
		ini.exe();
		
		this.CP = new CreateProject(1);
		this.CP.exeCreate(); // 新增一測試專案
		this.project = this.CP.getProjectList().get(0);
		
		super.setUp();
		
		// ================ set action info ========================
		setContextDirectory( new File(config.getBaseDirPath()+ "/WebContent") );
		setServletConfigFile("/WEB-INF/struts-config.xml");
		setRequestPathInfo( this.ACTION_PATH );
		
		ini = null;
	}

	protected void tearDown() throws IOException, Exception {
		//	刪除資料庫
		InitialSQL ini = new InitialSQL(config);
		ini.exe();
		
		//	刪除外部檔案
		ProjectManager projectManager = new ProjectManager();
		projectManager.deleteAllProject();
		projectManager.initialRoleBase(this.config.getTestDataPath());

		super.tearDown();
		
		ini = null;
		projectManager = null;
		this.CP = null;
	}
	
	public void testEditStory() throws InterruptedException{
		int storyCount = 2;
		CreateProductBacklog CPB = new CreateProductBacklog(storyCount, this.CP);
		CPB.exe();
		
		// ================ set request info ========================
		/**
		 *	Q: 由於在story資料庫的紀錄方式為XML並且XML有一個欄位是記錄更改時間，
		 *	        在撰寫測試案例時，如果做新增完馬上做編輯的動作，
		 *	        由於時間太快可能導致此爛位的時間一模一樣，會讓取讀錯誤的資料。
		 *	Sol: 使用sleep確保時間有差距。
		 */
		Thread.sleep(1000);	
		String projectName = this.project.getName();
		request.setHeader("Referer", "?PID=" + projectName);
		String expectedStoryName = "UT for Update Story for Name";
		String expectedStoryImportance = "5";
		String expectedStoryEstimation = "5";
		String expectedStoryValue = "5";
		String expectedStoryHoewToDemo = "UT for Update Story for How to Demo";
		String expectedStoryNote = "UT for Update Story for Notes";
		String issueID = String.valueOf(CPB.getIssueIDList().get(0));
		addRequestParameter("issueID", issueID);
		addRequestParameter("Name", expectedStoryName);
		addRequestParameter("Importance", expectedStoryImportance);
		addRequestParameter("Estimation", expectedStoryEstimation);
		addRequestParameter("Value", expectedStoryValue);
		addRequestParameter("HowToDemo", expectedStoryHoewToDemo);
		addRequestParameter("Notes", expectedStoryNote);
		
		// ================ set session info ========================
		request.getSession().setAttribute("UserSession", config.getUserSession());
		
		// ================ 執行 action ======================
		actionPerform();
		
		// ================ assert ========================
		verifyNoActionErrors();
		verifyNoActionMessages();
		//	assert response text
		StringBuilder expectedResponseText = new StringBuilder();
		expectedResponseText.append("{\"success\":true,")
							.append("\"Total\":1,")
							.append("\"Stories\":[{")
							.append("\"Id\":").append(issueID).append(",")
							.append("\"Link\":\"/ezScrum/showIssueInformation.do?issueID=").append(issueID).append("\",")
							.append("\"Name\":\"").append(expectedStoryName).append("\",")
							.append("\"Value\":\"").append(expectedStoryValue).append("\",")
							.append("\"Importance\":\"").append(expectedStoryImportance).append("\",")			
							.append("\"Estimation\":\"").append(expectedStoryEstimation).append("\",")
							.append("\"Status\":\"new\",")
							.append("\"Notes\":\"").append(expectedStoryNote).append("\",")
							.append("\"HowToDemo\":\"").append(expectedStoryHoewToDemo).append("\",")
							.append("\"Release\":\"None\",")
							.append("\"Sprint\":\"None\",")
							.append("\"Tag\":\"\",")
							.append("\"FilterType\":\"DETAIL\",")
							.append("\"Attach\":\"false\",")
							.append("\"AttachFileList\":[]")
							.append("}]}");
		String actualResponseText = response.getWriterBuffer().toString();
		assertEquals(expectedResponseText.toString(), actualResponseText);
	}
}
