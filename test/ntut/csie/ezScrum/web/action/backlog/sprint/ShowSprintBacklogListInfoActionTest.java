package ntut.csie.ezScrum.web.action.backlog.sprint;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntut.csie.ezScrum.refactoring.manager.ProjectManager;
import ntut.csie.ezScrum.test.TestTool;
import ntut.csie.ezScrum.test.CreateData.AddStoryToSprint;
import ntut.csie.ezScrum.test.CreateData.AddTaskToStory;
import ntut.csie.ezScrum.test.CreateData.CreateProductBacklog;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.jcis.resource.core.IProject;
import servletunit.struts.MockStrutsTestCase;

public class ShowSprintBacklogListInfoActionTest extends MockStrutsTestCase{
	
	private CreateProject CP;
	private CreateSprint CS;
	private ezScrumInfoConfig config = new ezScrumInfoConfig();
	private final String ACTION_PATH = "/showSprintBacklogTreeListInfo";
	private IProject project;
	
	public ShowSprintBacklogListInfoActionTest(String testName) {
		super(testName);
	}
	
	protected void setUp() throws Exception {
		//	刪除資料庫
		InitialSQL ini = new InitialSQL(config);
		ini.exe();
		
		this.CP = new CreateProject(1);
		this.CP.exeCreate(); // 新增一測試專案
		this.project = this.CP.getProjectList().get(0);
		
		this.CS = new CreateSprint(2, this.CP);
		this.CS.exe();
		
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
	 * 沒有stories and tasks
	 */
	public void testShowSprintBacklogListInfo_1(){
		List<String> idList = this.CS.getSprintIDList();
		
		// ================ set request info ========================
		String projectName = this.project.getName();
		request.setHeader("Referer", "?PID=" + projectName);
		addRequestParameter("sprintID", idList.get(0));
		
		// ================ set session info ========================
		request.getSession().setAttribute("UserSession", config.getUserSession());
		
		// ================ 執行 action ======================
		actionPerform();
		
		// ================ assert ========================
		verifyNoActionErrors();
		verifyNoActionMessages();
		//	assert response text
		String expectedResponseText = "[]";
		String actualResponseText = response.getWriterBuffer().toString();
		assertEquals(expectedResponseText, actualResponseText);
		
	}
	
	/**
	 * sprint有stories and tasks
	 * @throws Exception 
	 */
	public void testShowSprintBacklogListInfo_2() throws Exception{
		List<String> idList = this.CS.getSprintIDList();
		int sprintID = Integer.parseInt(idList.get(0));
		int storyCount = 1;
		int storyEst = 5;
		AddStoryToSprint addStoryToSprint = new AddStoryToSprint(storyCount, storyEst, sprintID, this.CP, CreateProductBacklog.TYPE_ESTIMATION);
		addStoryToSprint.exe();
		
		int taskCount = 1;
		int taskEstValue = 2;
		AddTaskToStory addTaskToStory = new AddTaskToStory(taskCount, taskEstValue, addStoryToSprint, this.CP);
		addTaskToStory.exe();
		
		// ================ set request info ========================
		String projectName = this.project.getName();
		request.setHeader("Referer", "?PID=" + projectName);
		addRequestParameter("sprintID", idList.get(0));
		
		// ================ set session info ========================
		request.getSession().setAttribute("UserSession", config.getUserSession());
		
		// ================ 執行 action ======================
		actionPerform();
		
		// ================ assert ========================
		verifyNoActionErrors();
		verifyNoActionMessages();
		//	assert response text
		String actualResponseText = response.getWriterBuffer().toString();
//		System.out.println("actualResponseText = " + actualResponseText);
		//	story
		String storyType = "\"Type\":\"Story\"";
		String storyName = "\"Name\":\"TEST_STORY_1\"";
		String storyImportance = "\"Importance\":\"100\"";
		String storyValue = "\"Value\":\"50\"";
		String storyEstimatation = "\"Estimate\":\"5\"";
		String storyNotes = "\"Notes\":\"TEST_STORY_NOTE_1\"";
		assertTrue(actualResponseText.contains(storyType));
		assertTrue(actualResponseText.contains(storyName));
		assertTrue(actualResponseText.contains(storyImportance));
		assertTrue(actualResponseText.contains(storyValue));
		assertTrue(actualResponseText.contains(storyEstimatation));
		assertTrue(actualResponseText.contains(storyNotes));
		
		//	task
		String taskType = "\"Type\":\"Task\"";
		String taskName = "\"Name\":\"TEST_TASK_1\"";
		String taskValue = "\"Value\":\"\"";
		String taskEstimatation = "\"Estimate\":\"2\"";
		String taskNotes = "\"Notes\":\"TEST_TASK_NOTES_1\"";
		assertTrue(actualResponseText.contains(taskType));
		assertTrue(actualResponseText.contains(taskName));
		assertTrue(actualResponseText.contains(taskValue));
		assertTrue(actualResponseText.contains(taskEstimatation));
		assertTrue(actualResponseText.contains(taskNotes));
	}
	
	public void testShowSprintBacklogListInfo_3() throws Exception {
		List<String> idList = this.CS.getSprintIDList();
		int sprintID = Integer.parseInt(idList.get(0));
		int storyCount = 1;
		int storyEst = 2;
		AddStoryToSprint addStoryToSprint = new AddStoryToSprint(storyCount, storyEst, sprintID, this.CP, CreateProductBacklog.TYPE_ESTIMATION);
		addStoryToSprint.exe();
		
		int taskCount = 1;
		int taskEst = 2;
		AddTaskToStory addTaskToStory = new AddTaskToStory(taskCount, taskEst, addStoryToSprint, this.CP);
		addTaskToStory.exe();
		
		// ================ set request info ========================
		String projectName = this.project.getName();
		request.setHeader("Referer", "?PID=" + projectName);
		addRequestParameter("sprintID", idList.get(0));
		// ================ set session info ========================
		request.getSession().setAttribute("UserSession", config.getUserSession());
		// ================  執行 action ==============================
		actionPerform();
		// ================    assert    =============================
		verifyNoActionErrors();
		verifyNoActionMessages();
		//	assert response text
		//  Story
		String StoryType = addStoryToSprint.getIssueList().get(0).getCategory();
		String StoryID = String.valueOf(addStoryToSprint.getIssueList().get(0).getIssueID());
		String StoryName = addStoryToSprint.getIssueList().get(0).getSummary();
		String StoryValue = addStoryToSprint.getIssueList().get(0).getValue();		
		String StoryEstimate = addStoryToSprint.getIssueList().get(0).getEstimated();
		String StoryImportance = addStoryToSprint.getIssueList().get(0).getImportance();
		String StoryStatus = addStoryToSprint.getIssueList().get(0).getStatus();
		String StoryNotes = addStoryToSprint.getIssueList().get(0).getNotes();
		String StoryLink = "\"/ezScrum/showIssueInformation.do?issueID\\u003d1\"";
		String ReleaseID = addStoryToSprint.getIssueList().get(0).getReleaseID();
		//  取得Story日期
		List<String> StoryDate = new ArrayList<String>();
		Date currentDate = new Date();
		TestTool getStoryDate = new TestTool();
		StoryDate = getStoryDate.getDateList(currentDate, 10);
		
		//  Task
		String TaskType = addTaskToStory.getTaskList().get(0).getCategory();
		String TaskID = String.valueOf(addTaskToStory.getTaskList().get(0).getIssueID());
		String TaskName = addTaskToStory.getTaskList().get(0).getSummary();
		String TaskEstimate = addTaskToStory.getTaskList().get(0).getEstimated();
		String TaskStatus = addTaskToStory.getTaskList().get(0).getStatus();
		String TaskNotes = addTaskToStory.getTaskList().get(0).getNotes();
		String TaskLink = "\"/ezScrum/showIssueInformation.do?issueID\\u003d2\"";
	//  取得Task日期
		List<String> TaskDate = new ArrayList<String>();
		TaskDate = getStoryDate.getDateList(currentDate, 10);
		
		StringBuilder expectedResponseTest = new StringBuilder();
		expectedResponseTest.append("[{\"Type\":\"" + StoryType + "\"")
							.append(",\"ID\":\"" + StoryID + "\"")
							.append(",\"Tag\":\"\"")
							.append(",\"Name\":\"" + StoryName + "\"")
							.append(",\"Handler\":\" \"")
							.append(",\"Value\":\"" + StoryValue + "\"")
							.append(",\"Estimate\":\"" + StoryEstimate + "\"")
							.append(",\"Importance\":\"" + StoryImportance + "\"")
							.append(",\"Status\":\"" + StoryStatus + "\"")
							.append(",\"Notes\":\"" + StoryNotes + "\"")
							.append(",\"Link\":" + StoryLink)
							.append(",\"SprintID\":\"" + idList.get(0) + "\"")
							.append(",\"ReleaseID\":\"" + ReleaseID + "\"")
							.append(",\"dateList\":[");
		for(int i = 0; i < StoryDate.size(); i++){
			if(i != StoryDate.size() - 1) {
				expectedResponseTest.append("\"" + StoryDate.get(i) + " 12:00:00 AM\",");
			}else {
				expectedResponseTest.append("\"" + StoryDate.get(i) + " 12:00:00 AM\"]");
			}
		}
		expectedResponseTest.append(",\"leaf\":false")
							.append(",\"expanded\":false")
							.append(",\"id\":\"1\"")
							.append(",\"cls\":\"folder\"")
							.append(",\"children\":[{\"Type\":\"" + TaskType + "\"")
							.append(",\"ID\":\"" + TaskID + "\"")
							.append(",\"Tag\":\"\"")
							.append(",\"Name\":\"" + TaskName + "\"")
							.append(",\"Handler\":\"\"")
							.append(",\"Value\":\"\"")
							.append(",\"Estimate\":\"" + TaskEstimate + "\"")
							.append(",\"Importance\":\"\"")
							.append(",\"Status\":\"" + TaskStatus + "\"")
							.append(",\"Notes\":\"" + TaskNotes + "\"")
							.append(",\"Link\":" + TaskLink)
							.append(",\"SprintID\":\"\"")
							.append(",\"ReleaseID\":\"\"")
							.append(",\"dateList\":[");
		for(int i = 0; i < TaskDate.size(); i++){
			if(i != TaskDate.size() - 1) {
				expectedResponseTest.append("\"" + TaskDate.get(i) + " 12:00:00 AM\",");
			}else {
				expectedResponseTest.append("\"" + TaskDate.get(i) + " 12:00:00 AM\"]");
			}
		}
		expectedResponseTest.append(",\"Date_9\":\"\"")
							.append(",\"Date_10\":\"\"")
							.append(",\"Date_1\":\"2.0\"")
							.append(",\"Date_2\":\"\"")
							.append(",\"Date_3\":\"\"")
							.append(",\"Date_4\":\"\"")
							.append(",\"Date_5\":\"\"")
							.append(",\"Date_6\":\"\"")
							.append(",\"Date_7\":\"\"")
							.append(",\"Date_8\":\"\"")
							.append(",\"leaf\":true")
							.append(",\"expanded\":false")
							.append(",\"id\":\"2\"")
							.append(",\"cls\":\"file\"}]}]");
		
		String actualResponseText = response.getWriterBuffer().toString();
//		System.out.println("result = " + actualResponseText);
		System.out.println("expect = " + expectedResponseTest.toString());
		assertEquals(expectedResponseTest.toString(), actualResponseText);
	}
}
