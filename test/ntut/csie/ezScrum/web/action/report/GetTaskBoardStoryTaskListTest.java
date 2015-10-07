package ntut.csie.ezScrum.web.action.report;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.issue.core.ITSEnum;
import ntut.csie.ezScrum.refactoring.manager.ProjectManager;
import ntut.csie.ezScrum.test.CreateData.AddStoryToSprint;
import ntut.csie.ezScrum.test.CreateData.AddTaskToStory;
import ntut.csie.ezScrum.test.CreateData.AddUserToRole;
import ntut.csie.ezScrum.test.CreateData.CreateAccount;
import ntut.csie.ezScrum.test.CreateData.CreateProductBacklog;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.ezScrum.web.logic.SprintBacklogLogic;
import ntut.csie.jcis.resource.core.IProject;
import servletunit.struts.MockStrutsTestCase;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedHashTreeMap;

public class GetTaskBoardStoryTaskListTest extends MockStrutsTestCase {
	private CreateProject CP;
	private CreateSprint CS;
	private IProject project;
	private Gson gson;
	private ezScrumInfoConfig config = new ezScrumInfoConfig();

	public GetTaskBoardStoryTaskListTest(String testMethod) {
		super(testMethod);
	}

	protected void setUp() throws Exception {
		InitialSQL ini = new InitialSQL(config);
		ini.exe(); // 初始化 SQL

		// 新增一測試專案
		this.CP = new CreateProject(1);
		this.CP.exeCreate();
		this.project = this.CP.getProjectList().get(0);

		// 新增1筆Sprint Plan
		this.CS = new CreateSprint(1, this.CP);
		this.CS.exe();

		gson = new Gson();
		super.setUp();

		setContextDirectory(new File(config.getBaseDirPath() + "/WebContent"));	// 設定讀取的struts-config檔案路徑
		setServletConfigFile("/WEB-INF/struts-config.xml");
		setRequestPathInfo("/getTaskBoardStoryTaskList");

		// ============= release ==============
		ini = null;
	}

	protected void tearDown() throws IOException, Exception {
		InitialSQL ini = new InitialSQL(config);
		ini.exe(); 	// 初始化 SQL

		// 刪除外部檔案
		ProjectManager projectManager = new ProjectManager();
		projectManager.deleteAllProject();
		projectManager.initialRoleBase(this.config.getTestDataPath());

		// ============= release ==============
		ini = null;
		this.CP = null;
		this.CS = null;
		this.gson = null;
		super.tearDown();
	}

	/**
	 * 1個story，沒有task
	 * filter條件
	 * - 第1個sprint
	 * - 所有人 ALL
	 */
	public void testGetTaskBoardStoryTaskList_1() throws Exception {
		// Sprint加入1個Story
		final int STORY_COUNT = 1;
		AddStoryToSprint addStoryToSprint = new AddStoryToSprint(STORY_COUNT, 1, CS, CP, CreateProductBacklog.TYPE_ESTIMATION);
		addStoryToSprint.exe();
		IIssue story = addStoryToSprint.getIssueList().get(0);
		String stroyID = String.valueOf(story.getIssueID());

		// ================ set request info ========================
		String projectName = this.project.getName();
		request.setHeader("Referer", "?PID=" + projectName);
		addRequestParameter("UserID", "ALL");
		addRequestParameter("sprintID", "1");

		// ================ set session info ========================
		request.getSession().setAttribute("UserSession", config.getUserSession());

		// ================ 執行 action ======================
		actionPerform();

		// ================ assert ======================
		verifyNoActionErrors();
		verifyNoActionMessages();
		String result = response.getWriterBuffer().toString();
		StringBuilder expectedResponseText = new StringBuilder();
		expectedResponseText
		        .append("{")
		        .append("\"Stories\":[{")
		        .append("\"Id\":\"").append(stroyID).append("\",")
		        .append("\"Name\":\"").append(story.getSummary()).append("\",")
		        .append("\"Value\":\"").append(story.getValue()).append("\",")
		        .append("\"Estimate\":\"").append(story.getEstimated()).append("\",")
		        .append("\"Importance\":\"").append(story.getImportance()).append("\",")
		        .append("\"Tag\":\"\",")
		        .append("\"Status\":\"").append(story.getStatus()).append("\",")
		        .append("\"Notes\":\"").append(story.getNotes()).append("\",")
		        .append("\"HowToDemo\":\"").append(story.getHowToDemo()).append("\",")
		        .append("\"Link\":\"/ezScrum/showIssueInformation.do?issueID\\u003d").append(stroyID).append("\",")
		        .append("\"Release\":\"").append(story.getReleaseID()).append("\",")
		        .append("\"Sprint\":\"").append(CS.getSprintIDList().get(0)).append("\",")
		        .append("\"Attach\":\"false\",")
		        .append("\"AttachFileList\":[],")
		        .append("\"Tasks\":[]}],")
		        .append("\"success\":\"true\",\"Total\":1}");
		assertEquals(expectedResponseText.toString(), result);
	}

	/**
	 * 10個story，沒有task
	 * filter條件
	 * - 第1個sprint
	 * - 所有人 ALL
	 */
	public void testGetTaskBoardStoryTaskList_2() throws Exception {
		// Sprint加入10個Story
		final int STORY_COUNT = 10;
		AddStoryToSprint addStoryToSprint = new AddStoryToSprint(STORY_COUNT, 1, CS, CP, CreateProductBacklog.TYPE_ESTIMATION);
		addStoryToSprint.exe();

		// ================ set request info ========================
		String projectName = this.project.getName();
		request.setHeader("Referer", "?PID=" + projectName);
		addRequestParameter("UserID", "ALL");
		addRequestParameter("sprintID", "1");

		// ================ set session info ========================
		request.getSession().setAttribute("UserSession", config.getUserSession());

		// ================ 執行 action ======================
		actionPerform();

		// ================ assert ======================
		verifyNoActionErrors();
		verifyNoActionMessages();
		String result = response.getWriterBuffer().toString();
		HashMap<String, Object> resultMap = gson.fromJson(result, HashMap.class);
		ArrayList<LinkedHashTreeMap<String, Object>> storyList = (ArrayList<LinkedHashTreeMap<String, Object>>) resultMap.get("Stories");
		Number total = (Number) resultMap.get("Total");
		List<IIssue> expectedStories = addStoryToSprint.getIssueList();
		IIssue expectedStory;

		assertEquals("true", resultMap.get("success"));
		assertEquals(STORY_COUNT, total.intValue());
		for (int i = 0; i < STORY_COUNT; i++) {
			expectedStory = expectedStories.get(i);
			assertEquals(String.valueOf(expectedStory.getIssueID()), storyList.get(i).get("Id"));
			assertEquals(expectedStory.getSummary(), storyList.get(i).get("Name"));
			assertEquals(expectedStory.getValue(), storyList.get(i).get("Value"));
			assertEquals(expectedStory.getEstimated(), storyList.get(i).get("Estimate"));
			assertEquals(expectedStory.getImportance(), storyList.get(i).get("Importance"));
			assertEquals("", storyList.get(i).get("Tag"));
			assertEquals(expectedStory.getStatus(), storyList.get(i).get("Status"));
			assertEquals(expectedStory.getNotes(), storyList.get(i).get("Notes"));
			assertEquals(expectedStory.getHowToDemo(), storyList.get(i).get("HowToDemo"));
			assertEquals(expectedStory.getIssueLink(), storyList.get(i).get("Link"));
			assertEquals(expectedStory.getReleaseID(), storyList.get(i).get("Release"));
			assertEquals(CS.getSprintIDList().get(0), storyList.get(i).get("Sprint"));
			assertEquals("false", storyList.get(i).get("Attach"));
			assertEquals(expectedStory.getAttachFile(), storyList.get(i).get("AttachFileList"));
			assertEquals(new ArrayList<LinkedHashTreeMap>(), storyList.get(i).get("Tasks"));
		}
	}

	/**
	 * 1個story，1個task
	 * filter條件
	 * - 第1個sprint
	 * - 所有人 ALL
	 */
	public void testGetTaskBoardStoryTaskList_3() throws Exception {
		final int STORY_COUNT = 1;
		final int TASK_COUNT = 1;
		// Sprint加入1個Story
		AddStoryToSprint addStoryToSprint = new AddStoryToSprint(STORY_COUNT, 1, CS, CP, CreateProductBacklog.TYPE_ESTIMATION);
		addStoryToSprint.exe();
		IIssue story = addStoryToSprint.getIssueList().get(0);
		String stroyID = String.valueOf(story.getIssueID());

		// 每個Story加入1個task
		AddTaskToStory addTaskToStory = new AddTaskToStory(TASK_COUNT, 1, addStoryToSprint, CP);
		addTaskToStory.exe();
		IIssue task = addTaskToStory.getTaskList().get(0);
		String taskID = String.valueOf(task.getIssueID());

		// ================ set request info ========================
		String projectName = this.project.getName();
		request.setHeader("Referer", "?PID=" + projectName);
		addRequestParameter("UserID", "ALL");
		addRequestParameter("sprintID", "");

		// ================ set session info ========================
		request.getSession().setAttribute("UserSession", config.getUserSession());

		// ================ 執行 action ======================
		actionPerform();

		// ================ assert ======================
		verifyNoActionErrors();
		verifyNoActionMessages();
		String result = response.getWriterBuffer().toString();
		StringBuilder expectedResponseText = new StringBuilder();
		expectedResponseText
		        .append("{")
		        .append("\"Stories\":[{")
		        .append("\"Id\":\"").append(stroyID).append("\",")
		        .append("\"Name\":\"").append(story.getSummary()).append("\",")
		        .append("\"Value\":\"").append(story.getValue()).append("\",")
		        .append("\"Estimate\":\"").append(story.getEstimated()).append("\",")
		        .append("\"Importance\":\"").append(story.getImportance()).append("\",")
		        .append("\"Tag\":\"\",")
		        .append("\"Status\":\"").append(story.getStatus()).append("\",")
		        .append("\"Notes\":\"").append(story.getNotes()).append("\",")
		        .append("\"HowToDemo\":\"").append(story.getHowToDemo()).append("\",")
		        .append("\"Link\":\"/ezScrum/showIssueInformation.do?issueID\\u003d").append(stroyID).append("\",")
		        .append("\"Release\":\"").append(story.getReleaseID()).append("\",")
		        .append("\"Sprint\":\"").append(CS.getSprintIDList().get(0)).append("\",")
		        .append("\"Attach\":\"false\",")
		        .append("\"AttachFileList\":[],")
		        .append("\"Tasks\":[{")
		        .append("\"Id\":\"").append(taskID).append("\",")
		        .append("\"Name\":\"").append(task.getSummary()).append("\",")
		        .append("\"Estimate\":\"").append(task.getEstimated()).append("\",")
		        .append("\"RemainHours\":\"").append(task.getRemains()).append("\",")
		        .append("\"Handler\":\"\",")
		        .append("\"Notes\":\"").append(task.getNotes()).append("\",")
		        .append("\"AttachFileList\":[],")
		        .append("\"Attach\":\"false\",")
		        .append("\"Status\":\"").append(task.getStatus()).append("\",")
		        .append("\"Partners\":\"\",")
		        .append("\"Link\":\"/ezScrum/showIssueInformation.do?issueID\\u003d").append(taskID).append("\",")
		        .append("\"Actual\":\"").append(task.getActualHour()).append("\"")
		        .append("}]}],")
		        .append("\"success\":\"true\",\"Total\":1}");
		assertEquals(expectedResponseText.toString(), result);
	}

	/**
	 * 5個story，1個task
	 * filter條件
	 * - 第1個sprint
	 * - TEST_ACCOUNT_ID_1
	 */
	public void testGetTaskBoardStoryTaskList_4() throws Exception {
		final int STORY_COUNT = 5;
		final int TASK_COUNT = 1;
		// Sprint加入5個Story
		AddStoryToSprint addStoryToSprint = new AddStoryToSprint(STORY_COUNT, 1, CS, CP, CreateProductBacklog.TYPE_ESTIMATION);
		addStoryToSprint.exe();

		// 每個Story加入1個task
		AddTaskToStory addTaskToStory = new AddTaskToStory(TASK_COUNT, 1, addStoryToSprint, CP);
		addTaskToStory.exe();

		// 新建一個帳號並給於專案權限
		CreateAccount CA = new CreateAccount(1);
		CA.exe();
		AddUserToRole addUserToRole = new AddUserToRole(CP, CA);
		addUserToRole.exe_ST();

		SprintBacklogLogic sprintBacklogLogic = new SprintBacklogLogic(project, config.getUserSession(), null);
		// 將第一個story跟task全都拉到done, 用TEST_ACCOUNT_ID_1 checkout task
		List<IIssue> tasks = addTaskToStory.getTaskList();
		List<IIssue> stories = addStoryToSprint.getIssueList();
		sprintBacklogLogic.checkOutTask(tasks.get(0).getIssueID(), tasks.get(0).getSummary(), CA.getAccount_ID(1), "", tasks.get(0).getNotes(), "");
		Thread.sleep(1000);
		sprintBacklogLogic.doneIssue(tasks.get(0).getIssueID(), tasks.get(0).getSummary(), tasks.get(0).getNotes(), null, null);
		sprintBacklogLogic.doneIssue(stories.get(0).getIssueID(), stories.get(0).getSummary(), stories.get(0).getNotes(), null, null);
		// 將第三個task check out, 用TEST_ACCOUNT_ID_1 checkout task
		sprintBacklogLogic.checkOutTask(tasks.get(2).getIssueID(), tasks.get(2).getSummary(), CA.getAccount_ID(1), "", tasks.get(2).getNotes(), "");

		// ================ set request info ========================
		String projectName = this.project.getName();
		request.setHeader("Referer", "?PID=" + projectName);
		addRequestParameter("UserID", CA.getAccount_ID(1));
		addRequestParameter("sprintID", "1");

		// ================ set session info ========================
		request.getSession().setAttribute("UserSession", config.getUserSession());

		// ================ 執行 action ======================
		actionPerform();

		// ================ assert ======================
		verifyNoActionErrors();
		verifyNoActionMessages();
		String result = response.getWriterBuffer().toString();
		HashMap<String, Object> resultMap = gson.fromJson(result, HashMap.class);
		ArrayList<LinkedHashTreeMap<String, Object>> storyList = (ArrayList<LinkedHashTreeMap<String, Object>>) resultMap.get("Stories");
		ArrayList<LinkedHashTreeMap<String, Object>> taskList;
		Number total = (Number) resultMap.get("Total");
		List<IIssue> expectedStories = addStoryToSprint.getIssueList();
		List<IIssue> expectedTasks = addTaskToStory.getTaskList();
		IIssue expectedStory, expectedTask;

		/**
		 * 一個Story and task 被TEST_ACCOUNT_ID_1 拉到 Done
		 * 一個task 被 TEST_ACCOUNT_ID_1 拉到 Done
		 * 所以有兩個Story會被Filter出來(與其底下的task)
		 */
		assertEquals("true", resultMap.get("success"));
		assertEquals(2, total.intValue());
		for (int i = 0; i < storyList.size(); i++) {
			expectedStory = expectedStories.get(i * 2);
			expectedTask = expectedTasks.get(i * 2);
			taskList = (ArrayList<LinkedHashTreeMap<String, Object>>) storyList.get(i).get("Tasks");
			assertEquals(String.valueOf(expectedStory.getIssueID()), storyList.get(i).get("Id"));
			assertEquals(expectedStory.getSummary(), storyList.get(i).get("Name"));
			assertEquals(expectedStory.getValue(), storyList.get(i).get("Value"));
			assertEquals(expectedStory.getEstimated(), storyList.get(i).get("Estimate"));
			assertEquals(expectedStory.getImportance(), storyList.get(i).get("Importance"));
			assertEquals("", storyList.get(i).get("Tag"));
			if (i == 0) {
				assertEquals(ITSEnum.CLOSED, storyList.get(i).get("Status"));
			} else {
				assertEquals(ITSEnum.S_NEW_STATUS, storyList.get(i).get("Status"));
			}
			assertEquals(expectedStory.getNotes(), storyList.get(i).get("Notes"));
			assertEquals(expectedStory.getHowToDemo(), storyList.get(i).get("HowToDemo"));
			assertEquals(expectedStory.getIssueLink(), storyList.get(i).get("Link"));
			assertEquals(expectedStory.getReleaseID(), storyList.get(i).get("Release"));
			assertEquals(CS.getSprintIDList().get(0), storyList.get(i).get("Sprint"));
			assertEquals("false", storyList.get(i).get("Attach"));
			assertEquals(expectedStory.getAttachFile(), storyList.get(i).get("AttachFileList"));
			for (int j = 0; j < taskList.size(); j++) {
				assertEquals(String.valueOf(expectedTask.getIssueID()), taskList.get(j).get("Id"));
				assertEquals(expectedTask.getSummary(), taskList.get(j).get("Name"));
				assertEquals(expectedTask.getEstimated(), taskList.get(j).get("Estimate"));
				assertEquals(CA.getAccount_ID(1), taskList.get(j).get("Handler"));
				assertEquals(expectedTask.getNotes(), taskList.get(j).get("Notes"));
				assertEquals(expectedTask.getAttachFile(), taskList.get(j).get("AttachFileList"));
				assertEquals("false", taskList.get(j).get("Attach"));
				if (i == 0) {
					assertEquals(ITSEnum.CLOSED, taskList.get(j).get("Status"));
					assertEquals("0", taskList.get(j).get("RemainHours"));
				} else {
					assertEquals(ITSEnum.ASSIGNED, taskList.get(j).get("Status"));
					assertEquals(expectedTask.getRemains(), taskList.get(j).get("RemainHours"));
				}
				assertEquals(expectedTask.getPartners(), taskList.get(j).get("Partners"));
				assertEquals(expectedTask.getIssueLink(), taskList.get(j).get("Link"));
				assertEquals(expectedTask.getActualHour(), taskList.get(j).get("Actual"));
			}
		}
	}
}
