package ntut.csie.ezScrum.web.action.report;

import java.io.File;
import java.io.IOException;

import ntut.csie.ezScrum.refactoring.manager.ProjectManager;
import ntut.csie.ezScrum.test.CreateData.AddStoryToSprint;
import ntut.csie.ezScrum.test.CreateData.AddTaskToStory;
import ntut.csie.ezScrum.test.CreateData.CreateProductBacklog;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.SprintInfoUIObject;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.ezScrum.web.logic.SprintBacklogLogic;
import ntut.csie.jcis.resource.core.IProject;
import servletunit.struts.MockStrutsTestCase;

import com.google.gson.Gson;

public class GetSprintInfoForTaskBoardTest extends MockStrutsTestCase {
	private CreateProject CP;
	private CreateSprint CS;
	private IProject project;
	private Gson gson;
	private ezScrumInfoConfig config = new ezScrumInfoConfig();

	public GetSprintInfoForTaskBoardTest(String testMethod) {
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
		setRequestPathInfo("/GetSprintInfoForTaskBoard");

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
	 * 完全沒有動story與task的sprint info
	 */
	public void testGetSprintInfoForTaskBoard_1() throws Exception {
		final int STORY_COUNT = 2, TASK_COUNT = 2, STORY_EST = 5, TASK_EST = 5;
		// Sprint加入2個Story
		AddStoryToSprint addStoryToSprint = new AddStoryToSprint(STORY_COUNT, STORY_EST, CS, CP, CreateProductBacklog.TYPE_ESTIMATION);
		addStoryToSprint.exe();

		// 每個Story加入2個task
		AddTaskToStory addTaskToStory = new AddTaskToStory(TASK_COUNT, TASK_EST, addStoryToSprint, CP);
		addTaskToStory.exe();

		// ================ set request info ========================
		request.setHeader("Referer", "?PID=" + project.getName());
		addRequestParameter("sprintID", CS.getSprintIDList().get(0));

		// ================ set session info ========================
		request.getSession().setAttribute("UserSession", config.getUserSession());

		// ================ 執行 action ======================
		actionPerform();

		// ================ assert ======================
		verifyNoActionErrors();
		verifyNoActionMessages();
		String result = response.getWriterBuffer().toString();
		SprintInfoUIObject sprintInfo = gson.fromJson(result, SprintInfoUIObject.class);
		Double storyPoint = sprintInfo.CurrentStoryPoint;
		Double taskPoint = sprintInfo.CurrentTaskPoint;
		assertEquals(CS.getSprintIDList().get(0), String.valueOf(sprintInfo.ID));
		assertEquals(CS.TEST_SPRINT_GOAL + CS.getSprintIDList().get(0), sprintInfo.SprintGoal);
		assertEquals(STORY_COUNT * STORY_EST, storyPoint.intValue());
		assertEquals(STORY_COUNT * TASK_COUNT * TASK_EST, taskPoint.intValue());
		assertEquals("Release #0", sprintInfo.ReleaseID);
		assertEquals(true, sprintInfo.isCurrentSprint);
	}

	/**
	 * 一個story完成與兩個task完成的sprint info
	 * 總共2 story, 4 task
	 */
	public void testGetSprintInfoForTaskBoard_2() throws Exception {
		final int STORY_COUNT = 2, TASK_COUNT = 2, STORY_EST = 5, TASK_EST = 5;
		// Sprint加入2個Story
		AddStoryToSprint addStoryToSprint = new AddStoryToSprint(STORY_COUNT, STORY_EST, CS, CP, CreateProductBacklog.TYPE_ESTIMATION);
		addStoryToSprint.exe();

		// 每個Story加入2個task
		AddTaskToStory addTaskToStory = new AddTaskToStory(TASK_COUNT, TASK_EST, addStoryToSprint, CP);
		addTaskToStory.exe();

		SprintBacklogLogic sprintBacklogLogic = new SprintBacklogLogic(project, config.getUserSession(), null);
		// 將第1個story跟task全都拉到done
		sprintBacklogLogic.doneIssue(addTaskToStory.getTaskList().get(0).getIssueID(), addTaskToStory.getTaskList().get(0).getSummary(), "", null, null);
		sprintBacklogLogic.doneIssue(addTaskToStory.getTaskList().get(1).getIssueID(), addTaskToStory.getTaskList().get(1).getSummary(), "", null, null);
		sprintBacklogLogic.doneIssue(addStoryToSprint.getIssueList().get(0).getIssueID(), addStoryToSprint.getIssueList().get(0).getSummary(), "", null, null);

		// ================ set request info ========================
		String projectName = this.project.getName();
		request.setHeader("Referer", "?PID=" + projectName);
		addRequestParameter("sprintID", "1");

		// ================ set session info ========================
		request.getSession().setAttribute("UserSession", config.getUserSession());

		// ================ 執行 action ======================
		actionPerform();

		// ================ assert ======================
		verifyNoActionErrors();
		verifyNoActionMessages();
		String result = response.getWriterBuffer().toString();
		System.out.println(result);
		SprintInfoUIObject sprintInfo = gson.fromJson(result, SprintInfoUIObject.class);
		Double storyPoint = sprintInfo.CurrentStoryPoint;
		Double taskPoint = sprintInfo.CurrentTaskPoint;
		assertEquals(CS.getSprintIDList().get(0), String.valueOf(sprintInfo.ID));
		assertEquals(CS.TEST_SPRINT_GOAL + CS.getSprintIDList().get(0), sprintInfo.SprintGoal);
		assertEquals((STORY_COUNT - 1) * STORY_EST, storyPoint.intValue());				// done 1個story
		assertEquals((STORY_COUNT * TASK_COUNT - 2) * TASK_EST, taskPoint.intValue());	// done 2個task
		assertEquals("Release #0", sprintInfo.ReleaseID);
		assertEquals(true, sprintInfo.isCurrentSprint);
	}
}
