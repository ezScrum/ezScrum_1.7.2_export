package ntut.csie.ezScrum.web.action.backlog.sprint;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.issue.internal.Issue;
import ntut.csie.ezScrum.iteration.core.IStory;
import ntut.csie.ezScrum.refactoring.manager.ProjectManager;
import ntut.csie.ezScrum.test.CreateData.AddStoryToSprint;
import ntut.csie.ezScrum.test.CreateData.AddTaskToStory;
import ntut.csie.ezScrum.test.CreateData.CreateProductBacklog;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.test.CreateData.DropTask;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.ezScrum.web.dataObject.TaskObject;
import ntut.csie.ezScrum.web.mapper.SprintBacklogMapper;
import ntut.csie.jcis.resource.core.IProject;
import servletunit.struts.MockStrutsTestCase;

public class AjaxAddExistedTask extends MockStrutsTestCase {
	private CreateProject CP;
	private CreateSprint CS;
	private ezScrumInfoConfig config = new ezScrumInfoConfig();
	private final String ACTION_PATH = "/addExistedTask";
	private IProject project;
	
	public AjaxAddExistedTask(String testName) {
		super(testName);
	}
	
	protected void setUp() throws Exception {
		//	刪除資料庫
		InitialSQL ini = new InitialSQL(config);
		ini.exe();
		
		this.CP = new CreateProject(1);
		this.CP.exeCreate(); // 新增一測試專案
		this.project = this.CP.getProjectList().get(0);
		this.CS = new CreateSprint(1, this.CP);
		this.CS.exe();
		
		super.setUp();
		
		// ================ set action info ========================
		setContextDirectory(new File(config.getBaseDirPath()+ "/WebContent"));
		setServletConfigFile("/WEB-INF/struts-config.xml");
		setRequestPathInfo(this.ACTION_PATH);
		
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
		this.CS = null;
	}
	
	/**
	 * 測試有一個Droped Task的情況
	 */
	public void testAddExistedTask() throws Exception {
		// 加入1個Sprint
		int sprintID = Integer.valueOf(this.CS.getSprintIDList().get(0));
		// Sprint加入1個Story
		AddStoryToSprint addStory_Sprint = new AddStoryToSprint(1, 1, sprintID, CP, CreateProductBacklog.TYPE_ESTIMATION);
		addStory_Sprint.exe();
		int storyID = (int) addStory_Sprint.getIssueList().get(0).getIssueID();
		// Story加入1個Task
		AddTaskToStory addTask_Story = new AddTaskToStory(1, 1, addStory_Sprint, CP);
		addTask_Story.exe();
		int taskID = addTask_Story.getTaskIDList().get(0).intValue();
		// drop Task from story
		DropTask dropTask = new DropTask(CP, sprintID, storyID, taskID);
		dropTask.exe();
		
		// ================ set request info ========================
		String projectName = this.project.getName();
		request.setHeader("Referer", "?PID=" + projectName);
		// 設定Session資訊
		request.getSession().setAttribute("UserSession", config.getUserSession());
		request.getSession().setAttribute("Project", project);	
		// 設定新增Task所需的資訊
		String expectedStoryID = String.valueOf(storyID);
		String expectedSprintID = String.valueOf(sprintID);
		String expectedTaskID = String.valueOf(taskID);

		addRequestParameter("sprintID", expectedSprintID);
		addRequestParameter("issueID", expectedStoryID);	// story
		addRequestParameter("selected", expectedTaskID);	// task

		// ================ 執行 action ======================
		actionPerform();

		// ================ assert ========================
		verifyNoActionErrors();
		verifyNoActionMessages();
		
		SprintBacklogMapper sprintBacklogMapper = new SprintBacklogMapper(project, config.getUserSession(), sprintID);
		IIssue[] tasks = sprintBacklogMapper.getTaskInStory(storyID);
		assertEquals(expectedTaskID, String.valueOf(tasks[0].getIssueID()));
	}
}
