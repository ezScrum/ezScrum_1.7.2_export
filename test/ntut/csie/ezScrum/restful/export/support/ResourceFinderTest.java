package ntut.csie.ezScrum.restful.export.support;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.restful.export.support.ResourceFinder;
import ntut.csie.ezScrum.test.CreateData.AddStoryToSprint;
import ntut.csie.ezScrum.test.CreateData.AddTaskToStory;
import ntut.csie.ezScrum.test.CreateData.CopyProject;
import ntut.csie.ezScrum.test.CreateData.CreateProductBacklog;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateRelease;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.test.CreateData.CreateTask;
import ntut.csie.ezScrum.test.CreateData.CreateUnplannedItem;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.ezScrum.web.helper.SprintBacklogHelper;
import ntut.csie.ezScrum.web.logic.ProductBacklogLogic;
import ntut.csie.jcis.resource.core.IProject;

public class ResourceFinderTest {
	private ezScrumInfoConfig mConfig = new ezScrumInfoConfig();
	private CreateProject mCP;
	private CreateRelease mCR;
	private CreateSprint mCS;
	private CreateUnplannedItem mCU;
	private AddStoryToSprint mASTS;
	private AddTaskToStory mATTS;
	private CreateTask mCT;

	@Before
	public void setUp() throws Exception {
		// 初始化 SQL
		InitialSQL ini = new InitialSQL(mConfig);
		ini.exe();

		// Create Project
		mCP = new CreateProject(2);
		mCP.exeCreate();

		// Create Release
		mCR = new CreateRelease(1, mCP);
		mCR.exe();

		// Create Sprint
		mCS = new CreateSprint(2, mCP);
		mCS.exe();

		// Add Story to Sprint
		mASTS = new AddStoryToSprint(2, 8, mCS, mCP, CreateProductBacklog.TYPE_ESTIMATION);
		mASTS.exe();

		// Add Task to Story
		mATTS = new AddTaskToStory(2, 13, mASTS, mCP);
		mATTS.exe();

		// Create Dropped Task
		mCT = new CreateTask(2, mCP);
		mCT.exe();

		// Create Unplan
		mCU = new CreateUnplannedItem(2, mCP, mCS);
		mCU.exe();
	}

	@After
	public void tearDown() {
		// 初始化 SQL
		InitialSQL ini = new InitialSQL(mConfig);
		ini.exe();

		// 刪除測試檔案
		CopyProject copyProject = new CopyProject(mCP);
		copyProject.exeDelete_Project();

		// ============= release ==============
		ini = null;
		copyProject = null;
		mCP = null;
		mCR = null;
		mCS = null;
		mCU = null;
		mASTS = null;
		mCT = null;
	}
	
	@Test
	public void testFindProject() {
		IProject project = mCP.getProjectList().get(0);
		ResourceFinder resourceFinder = new ResourceFinder();
		assertNull(resourceFinder.findProject("not exist project"));
		assertNotNull(resourceFinder.findProject(project.getName()));
	}
	
	@Test
	public void testFindSprint() {
		IProject project = mCP.getProjectList().get(0);
		String sprintId = mCS.getSprintIDList().get(0);
		ResourceFinder resourceFinder = new ResourceFinder();
		assertNull(resourceFinder.findSprint(Long.parseLong(sprintId)));
		resourceFinder.findProject(project.getName());
		assertNotNull(resourceFinder.findSprint(Long.parseLong(sprintId)));
	}
	
	@Test
	public void testFindStory() {
		IProject project = mCP.getProjectList().get(0);
		String sprintId = mCS.getSprintIDList().get(0);
		IIssue story = mASTS.getIssueList().get(0);
		ResourceFinder resourceFinder = new ResourceFinder();
		assertNull(resourceFinder.findStory(story.getIssueID()));
		resourceFinder.findProject(project.getName());
		resourceFinder.findSprint(Long.parseLong(sprintId));
		assertNotNull(resourceFinder.findStory(story.getIssueID()));
	}
	
	@Test
	public void testFindTask() {
		IProject project = mCP.getProjectList().get(0);
		String sprintId = mCS.getSprintIDList().get(0);
		IIssue story = mASTS.getIssueList().get(0);
		IIssue task = mATTS.getTaskList().get(0);
		ResourceFinder resourceFinder = new ResourceFinder();
		assertNull(resourceFinder.findTask(task.getIssueID()));
		resourceFinder.findProject(project.getName());
		resourceFinder.findSprint(Long.parseLong(sprintId));
		resourceFinder.findStory(story.getIssueID());
		assertNotNull(resourceFinder.findTask(task.getIssueID()));
	}
	
	@Test
	public void testFindDroppedStory() throws InterruptedException {
		IProject project = mCP.getProjectList().get(0);
		IIssue story = mASTS.getIssueList().get(0);
		ProductBacklogLogic productBacklogLogic = new ProductBacklogLogic(null, project);
		// It's need some delay for manipulating file IO (add story to sprint)
		Thread.sleep(1000);
		productBacklogLogic.removeStoryFromSprint(story.getIssueID());
		// It's need some delay for manipulating file IO (productBacklogLogic.removeStoryFromSprint)
		Thread.sleep(1000);
		ResourceFinder resourceFinder = new ResourceFinder();
		assertNull(resourceFinder.findDroppedStory(story.getIssueID()));
		resourceFinder.findProject(project.getName());
		assertNotNull(resourceFinder.findDroppedStory(story.getIssueID()));
	}
	
	@Test
	public void testFindUnplan() {
		IProject project = mCP.getProjectList().get(0);
		IIssue unplan = mCU.getIssueList().get(0);
		ResourceFinder resourceFinder = new ResourceFinder();
		assertNull(resourceFinder.findUnplan(unplan.getIssueID()));
		resourceFinder.findProject(project.getName());
		resourceFinder.findSprint(Long.parseLong(unplan.getSprintID()));
		assertNotNull(resourceFinder.findUnplan(unplan.getIssueID()));
	}
	
	@Test
	public void testFindDroppedTask() {
		IProject project = mCP.getProjectList().get(0);
		IIssue story = mASTS.getIssueList().get(0);
		IIssue task = mATTS.getTaskList().get(0);
		// Remove task from story
		SprintBacklogHelper sprintBacklogHelper = new SprintBacklogHelper(project, null);
		sprintBacklogHelper.removeTask(task.getIssueID(), story.getIssueID());
		ResourceFinder resourceFinder = new ResourceFinder();
		assertNull(resourceFinder.findDroppedTask(task.getIssueID()));
		resourceFinder.findProject(project.getName());
		assertNotNull(resourceFinder.findDroppedTask(task.getIssueID()));
	}
	
	@Test
	public void testFindTaskInDroppedStory() throws InterruptedException {
		IProject project = mCP.getProjectList().get(0);
		IIssue story = mASTS.getIssueList().get(0);
		IIssue task = mATTS.getTaskList().get(0);
		ProductBacklogLogic productBacklogLogic = new ProductBacklogLogic(null, project);
		// It's need some delay for manipulating file IO (add story to sprint)
		Thread.sleep(1000);
		productBacklogLogic.removeStoryFromSprint(story.getIssueID());
		// It's need some delay for manipulating file IO (productBacklogLogic.removeStoryFromSprint)
		Thread.sleep(1000);
		ResourceFinder resourceFinder = new ResourceFinder();
		assertNull(resourceFinder.findTaskInDroppedStory(task.getIssueID()));
		resourceFinder.findProject(project.getName());
		resourceFinder.findDroppedStory(story.getIssueID());
		assertNotNull(resourceFinder.findTaskInDroppedStory(task.getIssueID()));
	}
}
