package ntut.csie.ezScrum.web.support.export;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.iteration.core.IReleasePlanDesc;
import ntut.csie.ezScrum.iteration.core.ISprintPlanDesc;
import ntut.csie.ezScrum.test.CreateData.AddStoryToSprint;
import ntut.csie.ezScrum.test.CreateData.CopyProject;
import ntut.csie.ezScrum.test.CreateData.CreateProductBacklog;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateRelease;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.test.CreateData.CreateTask;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.ezScrum.web.databaseEnum.ProjectEnum;
import ntut.csie.ezScrum.web.databaseEnum.ReleaseEnum;
import ntut.csie.ezScrum.web.databaseEnum.SprintEnum;
import ntut.csie.ezScrum.web.databaseEnum.StoryEnum;
import ntut.csie.ezScrum.web.databaseEnum.TaskEnum;
import ntut.csie.ezScrum.web.helper.SprintPlanHelper;
import ntut.csie.jcis.resource.core.IProject;

public class JSONEncoderTest {
	private ezScrumInfoConfig mConfig = new ezScrumInfoConfig();
	private CreateProject mCP;
	private CreateRelease mCR;
	private CreateSprint mCS;
	private AddStoryToSprint mASTS;
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

		mASTS = new AddStoryToSprint(2, 8, mCS, mCP, CreateProductBacklog.TYPE_ESTIMATION);
		mASTS.exe();

		// Create Task
		mCT = new CreateTask(2, mCP);
		mCT.exe();
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
		mCS = null;
	}

	@Test
	public void testToSprintJSONArray() throws JSONException {
		IProject project = mCP.getProjectList().get(0);
		SprintPlanHelper sprintPlanHelper = new SprintPlanHelper(project);
		List<ISprintPlanDesc> sprints = sprintPlanHelper.loadListPlans();
		JSONArray sprintJSONArray = JSONEncoder.toSprintJSONArray(sprints);

		// Assert Sprint1
		ISprintPlanDesc sprint1 = sprints.get(0);
		JSONObject sprint1JSON = sprintJSONArray.getJSONObject(0);
		assertEquals(Long.parseLong(sprint1.getID()), sprint1JSON.getLong(SprintEnum.ID));
		assertEquals(sprint1.getGoal(), sprint1JSON.getString(SprintEnum.GOAL));
		assertEquals(Integer.parseInt(sprint1.getInterval()), sprint1JSON.getInt(SprintEnum.INTERVAL));
		assertEquals(Integer.parseInt(sprint1.getMemberNumber()), sprint1JSON.getInt(SprintEnum.TEAM_SIZE));
		assertEquals(Integer.parseInt(sprint1.getAvailableDays()), sprint1JSON.getInt(SprintEnum.AVAILABLE_HOURS));
		assertEquals(Integer.parseInt(sprint1.getFocusFactor()), sprint1JSON.getInt(SprintEnum.FOCUS_FACTOR));
		assertEquals(sprint1.getStartDate(), sprint1JSON.getString(SprintEnum.START_DATE));
		assertEquals(sprint1.getEndDate(), sprint1JSON.getString(SprintEnum.DUE_DATE));
		assertEquals(sprint1.getDemoDate(), sprint1JSON.getString(SprintEnum.DEMO_DATE));
		assertEquals(sprint1.getDemoPlace(), sprint1JSON.getString(SprintEnum.DEMO_PLACE));
		assertEquals(sprint1.getNotes(), sprint1JSON.getString(SprintEnum.DAILY_INFO));

		// Assert Sprint2
		ISprintPlanDesc sprint2 = sprints.get(1);
		JSONObject sprint2JSON = sprintJSONArray.getJSONObject(1);
		assertEquals(Long.parseLong(sprint2.getID()), sprint2JSON.getLong(SprintEnum.ID));
		assertEquals(sprint2.getGoal(), sprint2JSON.getString(SprintEnum.GOAL));
		assertEquals(Integer.parseInt(sprint2.getInterval()), sprint2JSON.getInt(SprintEnum.INTERVAL));
		assertEquals(Integer.parseInt(sprint2.getMemberNumber()), sprint2JSON.getInt(SprintEnum.TEAM_SIZE));
		assertEquals(Integer.parseInt(sprint2.getAvailableDays()), sprint2JSON.getInt(SprintEnum.AVAILABLE_HOURS));
		assertEquals(Integer.parseInt(sprint2.getFocusFactor()), sprint2JSON.getInt(SprintEnum.FOCUS_FACTOR));
		assertEquals(sprint2.getStartDate(), sprint2JSON.getString(SprintEnum.START_DATE));
		assertEquals(sprint2.getEndDate(), sprint2JSON.getString(SprintEnum.DUE_DATE));
		assertEquals(sprint2.getDemoDate(), sprint2JSON.getString(SprintEnum.DEMO_DATE));
		assertEquals(sprint2.getDemoPlace(), sprint2JSON.getString(SprintEnum.DEMO_PLACE));
		assertEquals(sprint2.getNotes(), sprint2JSON.getString(SprintEnum.DAILY_INFO));
	}

	@Test
	public void testToSprintJSON() throws JSONException {
		String sprintId = mCS.getSprintIDList().get(0);
		IProject project = mCP.getProjectList().get(0);
		SprintPlanHelper sprintPlanHelper = new SprintPlanHelper(project);
		ISprintPlanDesc sprint = sprintPlanHelper.loadPlan(sprintId);
		JSONObject sprintJSON = JSONEncoder.toSprintJSON(sprint);

		// Assert
		assertEquals(Long.parseLong(sprint.getID()), sprintJSON.getLong(SprintEnum.ID));
		assertEquals(sprint.getGoal(), sprintJSON.getString(SprintEnum.GOAL));
		assertEquals(Integer.parseInt(sprint.getInterval()), sprintJSON.getInt(SprintEnum.INTERVAL));
		assertEquals(Integer.parseInt(sprint.getMemberNumber()), sprintJSON.getInt(SprintEnum.TEAM_SIZE));
		assertEquals(Integer.parseInt(sprint.getAvailableDays()), sprintJSON.getInt(SprintEnum.AVAILABLE_HOURS));
		assertEquals(Integer.parseInt(sprint.getFocusFactor()), sprintJSON.getInt(SprintEnum.FOCUS_FACTOR));
		assertEquals(sprint.getStartDate(), sprintJSON.getString(SprintEnum.START_DATE));
		assertEquals(sprint.getEndDate(), sprintJSON.getString(SprintEnum.DUE_DATE));
		assertEquals(sprint.getDemoDate(), sprintJSON.getString(SprintEnum.DEMO_DATE));
		assertEquals(sprint.getDemoPlace(), sprintJSON.getString(SprintEnum.DEMO_PLACE));
		assertEquals(sprint.getNotes(), sprintJSON.getString(SprintEnum.DAILY_INFO));
	}

	@Test
	public void testToProjectJSONArray() throws JSONException {
		List<IProject> projects = mCP.getProjectList();
		JSONArray projectJSONArray = JSONEncoder.toProjectJSONArray(projects);

		// Assert Project1
		IProject project1 = projects.get(0);
		JSONObject project1JSON = projectJSONArray.getJSONObject(0);
		assertEquals(project1.getName(), project1JSON.getString(ProjectEnum.NAME));
		assertEquals(project1.getProjectDesc().getDisplayName(), project1JSON.getString(ProjectEnum.DISPLAY_NAME));
		assertEquals(project1.getProjectDesc().getComment(), project1JSON.getString(ProjectEnum.COMMENT));
		assertEquals(project1.getProjectDesc().getProjectManager(), project1JSON.getString(ProjectEnum.PRODUCT_OWNER));
		assertEquals(Long.parseLong(project1.getProjectDesc().getAttachFileSize()), project1JSON.getLong(ProjectEnum.ATTATCH_MAX_SIZE));

		// Assert Project2
		IProject project2 = projects.get(1);
		JSONObject project2JSON = JSONEncoder.toProjectJSON(project2);
		assertEquals(project2.getName(), project2JSON.getString(ProjectEnum.NAME));
		assertEquals(project2.getProjectDesc().getDisplayName(), project2JSON.getString(ProjectEnum.DISPLAY_NAME));
		assertEquals(project2.getProjectDesc().getComment(), project2JSON.getString(ProjectEnum.COMMENT));
		assertEquals(project2.getProjectDesc().getProjectManager(), project2JSON.getString(ProjectEnum.PRODUCT_OWNER));
		assertEquals(Long.parseLong(project2.getProjectDesc().getAttachFileSize()), project2JSON.getLong(ProjectEnum.ATTATCH_MAX_SIZE));
	}

	@Test
	public void testToProjectJSON() throws JSONException {
		IProject project = mCP.getProjectList().get(0);
		JSONObject projectJSON = JSONEncoder.toProjectJSON(project);

		// Assert
		assertEquals(project.getName(), projectJSON.getString(ProjectEnum.NAME));
		assertEquals(project.getProjectDesc().getDisplayName(), projectJSON.getString(ProjectEnum.DISPLAY_NAME));
		assertEquals(project.getProjectDesc().getComment(), projectJSON.getString(ProjectEnum.COMMENT));
		assertEquals(project.getProjectDesc().getProjectManager(), projectJSON.getString(ProjectEnum.PRODUCT_OWNER));
		assertEquals(Long.parseLong(project.getProjectDesc().getAttachFileSize()), projectJSON.getLong(ProjectEnum.ATTATCH_MAX_SIZE));
	}

	@Test
	public void testToStoryJSONArray() throws JSONException {
		// Get Stories
		List<IIssue> stories = mASTS.getIssueList();
		// Convert to JSONArray
		JSONArray storyJSONArray = JSONEncoder.toStoryJSONArray(stories);

		// Assert
		IIssue story1 = stories.get(0);
		assertEquals(story1.getIssueID(), storyJSONArray.getJSONObject(0).get(StoryEnum.ID));
		assertEquals(story1.getSummary(), storyJSONArray.getJSONObject(0).get(StoryEnum.NAME));
		assertEquals(story1.getStatus(), storyJSONArray.getJSONObject(0).get(StoryEnum.STATUS));
		assertEquals(Integer.parseInt(story1.getEstimated()), storyJSONArray.getJSONObject(0).get(StoryEnum.ESTIMATE));
		assertEquals(Integer.parseInt(story1.getImportance()), storyJSONArray.getJSONObject(0).get(StoryEnum.IMPORTANCE));
		assertEquals(Integer.parseInt(story1.getValue()), storyJSONArray.getJSONObject(0).get(StoryEnum.VALUE));
		assertEquals(story1.getNotes(), storyJSONArray.getJSONObject(0).get(StoryEnum.NOTES));
		assertEquals(story1.getHowToDemo(), storyJSONArray.getJSONObject(0).get(StoryEnum.HOW_TO_DEMO));

		IIssue story2 = stories.get(1);
		assertEquals(story2.getIssueID(), storyJSONArray.getJSONObject(1).get(StoryEnum.ID));
		assertEquals(story2.getSummary(), storyJSONArray.getJSONObject(1).get(StoryEnum.NAME));
		assertEquals(story2.getStatus(), storyJSONArray.getJSONObject(1).get(StoryEnum.STATUS));
		assertEquals(Integer.parseInt(story2.getEstimated()), storyJSONArray.getJSONObject(1).get(StoryEnum.ESTIMATE));
		assertEquals(Integer.parseInt(story2.getImportance()), storyJSONArray.getJSONObject(1).get(StoryEnum.IMPORTANCE));
		assertEquals(Integer.parseInt(story2.getValue()), storyJSONArray.getJSONObject(1).get(StoryEnum.VALUE));
		assertEquals(story2.getNotes(), storyJSONArray.getJSONObject(1).get(StoryEnum.NOTES));
		assertEquals(story2.getHowToDemo(), storyJSONArray.getJSONObject(1).get(StoryEnum.HOW_TO_DEMO));

		IIssue story3 = stories.get(2);
		assertEquals(story3.getIssueID(), storyJSONArray.getJSONObject(2).get(StoryEnum.ID));
		assertEquals(story3.getSummary(), storyJSONArray.getJSONObject(2).get(StoryEnum.NAME));
		assertEquals(story3.getStatus(), storyJSONArray.getJSONObject(2).get(StoryEnum.STATUS));
		assertEquals(Integer.parseInt(story3.getEstimated()), storyJSONArray.getJSONObject(2).get(StoryEnum.ESTIMATE));
		assertEquals(Integer.parseInt(story3.getImportance()), storyJSONArray.getJSONObject(2).get(StoryEnum.IMPORTANCE));
		assertEquals(Integer.parseInt(story3.getValue()), storyJSONArray.getJSONObject(2).get(StoryEnum.VALUE));
		assertEquals(story3.getNotes(), storyJSONArray.getJSONObject(2).get(StoryEnum.NOTES));
		assertEquals(story3.getHowToDemo(), storyJSONArray.getJSONObject(2).get(StoryEnum.HOW_TO_DEMO));

		IIssue story4 = stories.get(3);
		assertEquals(story4.getIssueID(), storyJSONArray.getJSONObject(3).get(StoryEnum.ID));
		assertEquals(story4.getSummary(), storyJSONArray.getJSONObject(3).get(StoryEnum.NAME));
		assertEquals(story4.getStatus(), storyJSONArray.getJSONObject(3).get(StoryEnum.STATUS));
		assertEquals(Integer.parseInt(story4.getEstimated()), storyJSONArray.getJSONObject(3).get(StoryEnum.ESTIMATE));
		assertEquals(Integer.parseInt(story4.getImportance()), storyJSONArray.getJSONObject(3).get(StoryEnum.IMPORTANCE));
		assertEquals(Integer.parseInt(story4.getValue()), storyJSONArray.getJSONObject(3).get(StoryEnum.VALUE));
		assertEquals(story4.getNotes(), storyJSONArray.getJSONObject(3).get(StoryEnum.NOTES));
		assertEquals(story4.getHowToDemo(), storyJSONArray.getJSONObject(3).get(StoryEnum.HOW_TO_DEMO));
	}

	@Test
	public void testToStoryJSON() throws JSONException {
		IIssue story = mASTS.getIssueList().get(0);
		// Convert to JSONObject
		JSONObject storyJson = JSONEncoder.toStoryJSON(story);

		// Assert
		assertEquals(story.getIssueID(), storyJson.get(StoryEnum.ID));
		assertEquals(story.getSummary(), storyJson.get(StoryEnum.NAME));
		assertEquals(story.getStatus(), storyJson.get(StoryEnum.STATUS));
		assertEquals(Integer.parseInt(story.getEstimated()), storyJson.get(StoryEnum.ESTIMATE));
		assertEquals(Integer.parseInt(story.getImportance()), storyJson.get(StoryEnum.IMPORTANCE));
		assertEquals(Integer.parseInt(story.getValue()), storyJson.get(StoryEnum.VALUE));
		assertEquals(story.getNotes(), storyJson.get(StoryEnum.NOTES));
		assertEquals(story.getHowToDemo(), storyJson.get(StoryEnum.HOW_TO_DEMO));
	}

	@Test
	public void testToTaskJSONArray() throws JSONException {
		List<IIssue> tasks = mCT.getTaskList();

		JSONArray taskJSONArray = JSONEncoder.toTaskJSONArray(tasks);

		// Assert Task1
		IIssue task1 = tasks.get(0);
		JSONObject task1JSON = taskJSONArray.getJSONObject(0);
		assertEquals(task1.getSummary(), task1JSON.getString(TaskEnum.NAME));
		assertEquals(task1.getAssignto(), task1JSON.getString(TaskEnum.HANDLER));
		assertEquals(Integer.parseInt(task1.getEstimated()), task1JSON.getInt(TaskEnum.ESTIMATE));
		assertEquals(Integer.parseInt(task1.getRemains()), task1JSON.getInt(TaskEnum.REMAIN));
		assertEquals(Integer.parseInt(task1.getActualHour()), task1JSON.getInt(TaskEnum.ACTUAL));
		assertEquals(task1.getNotes(), task1JSON.getString(TaskEnum.NOTES));
		assertEquals(task1.getStatus(), task1JSON.getString(TaskEnum.STATUS));
		// Assert Task2
		IIssue task2 = tasks.get(1);
		JSONObject task2JSON = taskJSONArray.getJSONObject(1);
		assertEquals(task2.getSummary(), task2JSON.getString(TaskEnum.NAME));
		assertEquals(task2.getAssignto(), task2JSON.getString(TaskEnum.HANDLER));
		assertEquals(Integer.parseInt(task2.getEstimated()), task2JSON.getInt(TaskEnum.ESTIMATE));
		assertEquals(Integer.parseInt(task2.getRemains()), task2JSON.getInt(TaskEnum.REMAIN));
		assertEquals(Integer.parseInt(task2.getActualHour()), task2JSON.getInt(TaskEnum.ACTUAL));
		assertEquals(task2.getNotes(), task2JSON.getString(TaskEnum.NOTES));
		assertEquals(task2.getStatus(), task2JSON.getString(TaskEnum.STATUS));
	}

	@Test
	public void testToTaskJSON() throws JSONException {
		List<IIssue> tasks = mCT.getTaskList();
		IIssue task = tasks.get(0);

		JSONObject taskJSON = JSONEncoder.toTaskJSON(task);

		// Assert
		assertEquals(task.getSummary(), taskJSON.getString(TaskEnum.NAME));
		assertEquals(task.getAssignto(), taskJSON.getString(TaskEnum.HANDLER));
		assertEquals(Integer.parseInt(task.getEstimated()), taskJSON.getInt(TaskEnum.ESTIMATE));
		assertEquals(Integer.parseInt(task.getRemains()), taskJSON.getInt(TaskEnum.REMAIN));
		assertEquals(Integer.parseInt(task.getActualHour()), taskJSON.getInt(TaskEnum.ACTUAL));
		assertEquals(task.getNotes(), taskJSON.getString(TaskEnum.NOTES));
		assertEquals(task.getStatus(), taskJSON.getString(TaskEnum.STATUS));
	}
	
	@Test
	public void testToReleaseJSONArray() throws JSONException {
		List<IReleasePlanDesc> releases = mCR.getReleaseList();
		// Convert to JSONOArray
		JSONArray releaseJSONArray = JSONEncoder.toReleaseJSONArray(releases);

		// Assert
		JSONObject releaseJSON1 = releaseJSONArray.getJSONObject(0);
		IReleasePlanDesc release1 = releases.get(0);
		assertEquals(release1.getName(), releaseJSON1.getString(ReleaseEnum.NAME));
		assertEquals(release1.getDescription(), releaseJSON1.getString(ReleaseEnum.DESCRIPTION));
		assertEquals(release1.getStartDate(), releaseJSON1.getString(ReleaseEnum.START_DATE));
		assertEquals(release1.getEndDate(), releaseJSON1.getString(ReleaseEnum.DUE_DATE));

		JSONObject releaseJSON2 = releaseJSONArray.getJSONObject(1);
		IReleasePlanDesc release2 = releases.get(1);
		assertEquals(release2.getName(), releaseJSON2.getString(ReleaseEnum.NAME));
		assertEquals(release2.getDescription(), releaseJSON2.getString(ReleaseEnum.DESCRIPTION));
		assertEquals(release2.getStartDate(), releaseJSON2.getString(ReleaseEnum.START_DATE));
		assertEquals(release2.getEndDate(), releaseJSON2.getString(ReleaseEnum.DUE_DATE));
	}

	@Test
	public void testToReleaseJSON() throws JSONException {
		IReleasePlanDesc release = mCR.getReleaseList().get(0);
		// Convert to JSONObject
		JSONObject releaseJSON = JSONEncoder.toReleaseJSON(release);

		// Assert
		assertEquals(release.getName(), releaseJSON.getString(ReleaseEnum.NAME));
		assertEquals(release.getDescription(), releaseJSON.getString(ReleaseEnum.DESCRIPTION));
		assertEquals(release.getStartDate(), releaseJSON.getString(ReleaseEnum.START_DATE));
		assertEquals(release.getEndDate(), releaseJSON.getString(ReleaseEnum.DUE_DATE));
	}
}
