package ntut.csie.ezScrum.web.action.backlog.product;

import java.io.File;
import java.io.IOException;
import java.util.List;

import ntut.csie.ezScrum.issue.core.IIssueTag;
import ntut.csie.ezScrum.refactoring.manager.ProjectManager;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.test.CreateData.CreateTag;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.jcis.resource.core.IProject;
import servletunit.struts.MockStrutsTestCase;

public class AjaxAddNewStoryActionTest extends MockStrutsTestCase {

	private CreateProject CP;
	private ezScrumInfoConfig config = new ezScrumInfoConfig();
	private final String ACTION_PATH = "/ajaxAddNewStory";
	private IProject project;
	
	public AjaxAddNewStoryActionTest(String testName) {
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
	
	/**
	 * 新增的Story
	 * 沒有加上 Tag = "" and sprintID = null
	 */
	public void testAddNewStory_1(){
		// ================ set request info ========================
		String projectName = this.project.getName();
		request.setHeader("Referer", "?PID=" + projectName);
		
		String expectedStoryName = "UT for Add New Story for Name";
		String expectedStoryImportance = "0";
		String expectedStoryEstimation= "0";
		String expectedStoryValue= "0";
		String expectedSprintId= null;
		String expectedTagIDs = "";
		String expectedStoryHoewToDemo = "UT for Add New Story for How to Demo";
		String expectedStoryNote = "UT for Add New Story for Notes";
		addRequestParameter("Name", expectedStoryName);
		addRequestParameter("Importance", expectedStoryImportance);
		addRequestParameter("Estimation", expectedStoryEstimation);
		addRequestParameter("Value", expectedStoryValue);
		addRequestParameter("HowToDemo", expectedStoryHoewToDemo);
		addRequestParameter("Notes", expectedStoryNote);
		addRequestParameter("sprintId", expectedSprintId);
		addRequestParameter("TagIDs", expectedTagIDs);
		
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
							.append("\"Id\":1,")
							.append("\"Link\":\"/ezScrum/showIssueInformation.do?issueID=1\",")
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
							.append("\"FilterType\":\"BACKLOG\",")
							.append("\"Attach\":\"false\",")
							.append("\"AttachFileList\":[]")
							.append("}]}");
		String actualResponseText = response.getWriterBuffer().toString();
		assertEquals(expectedResponseText.toString(), actualResponseText);
	}
	
	/**
	 * 新增的Story
	 * 沒有加上 Tag = null and sprintID = ""
	 */
	public void testAddNewStory_2(){
		// ================ set request info ========================
		
		String projectName = this.project.getName();
		request.setHeader("Referer", "?PID=" + projectName);
		String expectedStoryName = "UT for Add New Story for Name";
		String expectedStoryImportance = "0";
		String expectedStoryEstimation= "0";
		String expectedStoryValue= "0";
		String expectedSprintId= "";
		String expectedTagIDs = null;
		String expectedStoryHoewToDemo = "UT for Add New Story for How to Demo";
		String expectedStoryNote = "UT for Add New Story for Notes";
		addRequestParameter("Name", expectedStoryName);
		addRequestParameter("Importance", expectedStoryImportance);
		addRequestParameter("Estimation", expectedStoryEstimation);
		addRequestParameter("Value", expectedStoryValue);
		addRequestParameter("HowToDemo", expectedStoryHoewToDemo);
		addRequestParameter("Notes", expectedStoryNote);
		addRequestParameter("sprintId", expectedSprintId);
		addRequestParameter("TagIDs", expectedTagIDs);
		
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
							.append("\"Id\":1,")
							.append("\"Link\":\"/ezScrum/showIssueInformation.do?issueID=1\",")
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
							.append("\"FilterType\":\"BACKLOG\",")
							.append("\"Attach\":\"false\",")
							.append("\"AttachFileList\":[]")
							.append("}]}");
		String actualResponseText = response.getWriterBuffer().toString();
		assertEquals(expectedResponseText.toString(), actualResponseText);
	}
	
	/**
	 * 新增的Story加上 TagIDs and sprintID = ""
	 */
	public void testAddNewStory_3(){
		int tagCount = 2;
		CreateTag createTag = new CreateTag(tagCount, this.CP);
		createTag.exe();
		List<IIssueTag> tagList = createTag.getTagList();
		
		// ================ set request info ========================
		String projectName = this.project.getName();
		request.setHeader("Referer", "?PID=" + projectName);
		String expectedStoryName = "UT for Add New Story for Name";
		String expectedStoryImportance = "0";
		String expectedStoryEstimation = "0";
		String expectedStoryValue = "0";
		String expectedSprintId = "";
		String expectedTagIDs = tagList.get(0).getTagId() + "," + tagList.get(1).getTagId();
		String expectedTagNames = tagList.get(0).getTagName() + "," + tagList.get(1).getTagName();
		String expectedStoryHoewToDemo = "UT for Add New Story for How to Demo";
		String expectedStoryNote = "UT for Add New Story for Notes";
		addRequestParameter("Name", expectedStoryName);
		addRequestParameter("Importance", expectedStoryImportance);
		addRequestParameter("Estimation", expectedStoryEstimation);
		addRequestParameter("Value", expectedStoryValue);
		addRequestParameter("HowToDemo", expectedStoryHoewToDemo);
		addRequestParameter("Notes", expectedStoryNote);
		addRequestParameter("sprintId", expectedSprintId);
		addRequestParameter("TagIDs", expectedTagIDs);
		
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
							.append("\"Id\":1,")
							.append("\"Link\":\"/ezScrum/showIssueInformation.do?issueID=1\",")
							.append("\"Name\":\"").append(expectedStoryName).append("\",")
							.append("\"Value\":\"").append(expectedStoryValue).append("\",")
							.append("\"Importance\":\"").append(expectedStoryImportance).append("\",")			
							.append("\"Estimation\":\"").append(expectedStoryEstimation).append("\",")
							.append("\"Status\":\"new\",")
							.append("\"Notes\":\"").append(expectedStoryNote).append("\",")
							.append("\"HowToDemo\":\"").append(expectedStoryHoewToDemo).append("\",")
							.append("\"Release\":\"None\",")
							.append("\"Sprint\":\"None\",")
							.append("\"Tag\":\"").append(expectedTagNames).append("\",")
							.append("\"FilterType\":\"BACKLOG\",")
							.append("\"Attach\":\"false\",")
							.append("\"AttachFileList\":[]")
							.append("}]}");
		String actualResponseText = response.getWriterBuffer().toString();
		assertEquals(expectedResponseText.toString(), actualResponseText);
	}
	
	/**
	 * 新增的Story加上 TagIDs and sprintID = "1"
	 */
	public void testAddNewStory_4(){
		int tagCount = 2;
		CreateTag createTag = new CreateTag(tagCount, this.CP);
		createTag.exe();
		List<IIssueTag> tagList = createTag.getTagList();
		
		CreateSprint createSprint = new CreateSprint(1, this.CP);
		createSprint.exe();
		List<String> sprintIDList = createSprint.getSprintIDList();
		
		// ================ set request info ========================
		String projectName = this.project.getName();
		request.setHeader("Referer", "?PID=" + projectName);
		String expectedStoryName = "UT for Add New Story for Name";
		String expectedStoryImportance = "0";
		String expectedStoryEstimation= "0";
		String expectedStoryValue= "0";
		String expectedSprintId= sprintIDList.get(0);
		String expectedTagIDs = tagList.get(0).getTagId() + "," + tagList.get(1).getTagId();
		String expectedTagNames = tagList.get(0).getTagName() + "," + tagList.get(1).getTagName();
		String expectedStoryHoewToDemo = "UT for Add New Story for How to Demo";
		String expectedStoryNote = "UT for Add New Story for Notes";
		addRequestParameter("Name", expectedStoryName);
		addRequestParameter("Importance", expectedStoryImportance);
		addRequestParameter("Estimation", expectedStoryEstimation);
		addRequestParameter("Value", expectedStoryValue);
		addRequestParameter("HowToDemo", expectedStoryHoewToDemo);
		addRequestParameter("Notes", expectedStoryNote);
		addRequestParameter("sprintId", expectedSprintId);
		addRequestParameter("TagIDs", expectedTagIDs);
		
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
							.append("\"Id\":1,")
							.append("\"Link\":\"/ezScrum/showIssueInformation.do?issueID=1\",")
							.append("\"Name\":\"").append(expectedStoryName).append("\",")
							.append("\"Value\":\"").append(expectedStoryValue).append("\",")
							.append("\"Importance\":\"").append(expectedStoryImportance).append("\",")			
							.append("\"Estimation\":\"").append(expectedStoryEstimation).append("\",")
							.append("\"Status\":\"new\",")
							.append("\"Notes\":\"").append(expectedStoryNote).append("\",")
							.append("\"HowToDemo\":\"").append(expectedStoryHoewToDemo).append("\",")
							.append("\"Release\":\"None\",")
							.append("\"Sprint\":\"").append(expectedSprintId).append("\",")
							.append("\"Tag\":\"").append(expectedTagNames).append("\",")
							.append("\"FilterType\":\"BACKLOG\",")
							.append("\"Attach\":\"false\",")
							.append("\"AttachFileList\":[]")
							.append("}]}");
		String actualResponseText = response.getWriterBuffer().toString();
		assertEquals(expectedResponseText.toString(), actualResponseText);
	}
}
