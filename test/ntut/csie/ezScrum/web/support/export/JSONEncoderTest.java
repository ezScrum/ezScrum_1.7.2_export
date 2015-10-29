package ntut.csie.ezScrum.web.support.export;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.issue.internal.Issue;
import ntut.csie.ezScrum.iteration.core.IReleasePlanDesc;
import ntut.csie.ezScrum.iteration.core.IScrumIssue;
import ntut.csie.ezScrum.iteration.core.ISprintPlanDesc;
import ntut.csie.ezScrum.iteration.iternal.ScrumIssue;
import ntut.csie.ezScrum.test.CreateData.AddStoryToSprint;
import ntut.csie.ezScrum.test.CreateData.CopyProject;
import ntut.csie.ezScrum.test.CreateData.CreateProductBacklog;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateRelease;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.test.CreateData.CreateTask;
import ntut.csie.ezScrum.test.CreateData.CreateUnplannedItem;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.ezScrum.web.dataObject.UserInformation;
import ntut.csie.ezScrum.web.databaseEnum.AccountEnum;
import ntut.csie.ezScrum.web.databaseEnum.ProjectEnum;
import ntut.csie.ezScrum.web.databaseEnum.ReleaseEnum;
import ntut.csie.ezScrum.web.databaseEnum.RetrospectiveEnum;
import ntut.csie.ezScrum.web.databaseEnum.SprintEnum;
import ntut.csie.ezScrum.web.databaseEnum.StoryEnum;
import ntut.csie.ezScrum.web.databaseEnum.TaskEnum;
import ntut.csie.ezScrum.web.databaseEnum.UnplanEnum;
import ntut.csie.ezScrum.web.helper.AccountHelper;
import ntut.csie.ezScrum.web.helper.SprintPlanHelper;
import ntut.csie.jcis.account.core.IAccount;
import ntut.csie.jcis.resource.core.IProject;

public class JSONEncoderTest {
	private ezScrumInfoConfig mConfig = new ezScrumInfoConfig();
	private CreateProject mCP;
	private CreateRelease mCR;
	private CreateSprint mCS;
	private CreateUnplannedItem mCU;
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
		
		// Create Unplan
		mCU = new CreateUnplannedItem(2, mCP, mCS);
		mCU.exe();

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
		mCR = null;
		mCS = null;
		mCU = null;
		mASTS = null;
		mCT = null;
	}
	
	@Test
	public void testToPartnerJSONArray() throws JSONException {
		String partnersString1 = "";
		String partnersString2 = "Jay";
		String partnersString3 = "Jay;";
		String partnersString4 = "Jay;Mike";
		String partnersString5 = "Jay;Mike;";
		String partnersString6 = "Jay;Henry;Mike;Jonathan;Tony";
		
		// Assert
		JSONArray partnerJSONArray1 = JSONEncoder.toPartnerJSONArray(partnersString1);
		assertEquals(0, partnerJSONArray1.length());
		
		JSONArray partnerJSONArray2 = JSONEncoder.toPartnerJSONArray(partnersString2);
		assertEquals(1, partnerJSONArray2.length());
		assertEquals("Jay", partnerJSONArray2.getJSONObject(0).getString(AccountEnum.USERNAME));
		
		JSONArray partnerJSONArray3 = JSONEncoder.toPartnerJSONArray(partnersString3);
		assertEquals(1, partnerJSONArray3.length());
		assertEquals("Jay", partnerJSONArray3.getJSONObject(0).getString(AccountEnum.USERNAME));
		
		JSONArray partnerJSONArray4 = JSONEncoder.toPartnerJSONArray(partnersString4);
		assertEquals(2, partnerJSONArray4.length());
		assertEquals("Jay", partnerJSONArray4.getJSONObject(0).getString(AccountEnum.USERNAME));
		assertEquals("Mike", partnerJSONArray4.getJSONObject(1).getString(AccountEnum.USERNAME));
		
		JSONArray partnerJSONArray5 = JSONEncoder.toPartnerJSONArray(partnersString5);
		assertEquals(2, partnerJSONArray5.length());
		assertEquals("Jay", partnerJSONArray5.getJSONObject(0).getString(AccountEnum.USERNAME));
		assertEquals("Mike", partnerJSONArray5.getJSONObject(1).getString(AccountEnum.USERNAME));
		
		JSONArray partnerJSONArray6 = JSONEncoder.toPartnerJSONArray(partnersString6);
		assertEquals(5, partnerJSONArray6.length());
		assertEquals("Jay", partnerJSONArray6.getJSONObject(0).getString(AccountEnum.USERNAME));
		assertEquals("Henry", partnerJSONArray6.getJSONObject(1).getString(AccountEnum.USERNAME));
		assertEquals("Mike", partnerJSONArray6.getJSONObject(2).getString(AccountEnum.USERNAME));
		assertEquals("Jonathan", partnerJSONArray6.getJSONObject(3).getString(AccountEnum.USERNAME));
		assertEquals("Tony", partnerJSONArray6.getJSONObject(4).getString(AccountEnum.USERNAME));
	}
	
	@Test
	public void testToUnplanJSONArray() throws JSONException {
		List<IIssue> unplans = mCU.getIssueList();
		JSONArray unplanJSONArray = JSONEncoder.toUnplanJSONArray(unplans);
		
		// Assert
		assertEquals(8, unplanJSONArray.length());
		
		assertEquals(unplans.get(0).getSummary(), unplanJSONArray.getJSONObject(0).getString(UnplanEnum.NAME));
		assertEquals(unplans.get(0).getAssignto(), unplanJSONArray.getJSONObject(0).getString(UnplanEnum.HANDLER));
		assertEquals(unplans.get(0).getEstimated(), unplanJSONArray.getJSONObject(0).getString(UnplanEnum.ESTIMATE));
		assertEquals(unplans.get(0).getActualHour(), unplanJSONArray.getJSONObject(0).getString(UnplanEnum.ACTUAL));
		assertEquals(unplans.get(0).getNotes(), unplanJSONArray.getJSONObject(0).getString(UnplanEnum.NOTES));
		assertEquals(unplans.get(0).getStatus(), unplanJSONArray.getJSONObject(0).getString(UnplanEnum.STATUS));
		
		assertEquals(unplans.get(1).getSummary(), unplanJSONArray.getJSONObject(1).getString(UnplanEnum.NAME));
		assertEquals(unplans.get(1).getAssignto(), unplanJSONArray.getJSONObject(1).getString(UnplanEnum.HANDLER));
		assertEquals(unplans.get(1).getEstimated(), unplanJSONArray.getJSONObject(1).getString(UnplanEnum.ESTIMATE));
		assertEquals(unplans.get(1).getActualHour(), unplanJSONArray.getJSONObject(1).getString(UnplanEnum.ACTUAL));
		assertEquals(unplans.get(1).getNotes(), unplanJSONArray.getJSONObject(1).getString(UnplanEnum.NOTES));
		assertEquals(unplans.get(1).getStatus(), unplanJSONArray.getJSONObject(1).getString(UnplanEnum.STATUS));
	}
	
	@Test
	public void testToUnplanJSON() throws JSONException {
		IIssue unplan = mCU.getIssueList().get(0);
		JSONObject unplanJSON = JSONEncoder.toUnplanJSON(unplan);
		
		// Assert
		assertEquals(unplan.getSummary(), unplanJSON.getString(UnplanEnum.NAME));
		assertEquals(unplan.getAssignto(), unplanJSON.getString(UnplanEnum.HANDLER));
		assertEquals(unplan.getEstimated(), unplanJSON.getString(UnplanEnum.ESTIMATE));
		assertEquals(unplan.getActualHour(), unplanJSON.getString(UnplanEnum.ACTUAL));
		assertEquals(unplan.getNotes(), unplanJSON.getString(UnplanEnum.NOTES));
		assertEquals(unplan.getStatus(), unplanJSON.getString(UnplanEnum.STATUS));
	}
	
	@Test
	public void testToAccountJSONArray() throws JSONException {
		// Test Data
		String userName = "TEST_USER_NAME_";
		String userRealName = "TEST_USER_REAL_NAME_";
		String password = "TEST_USER_PASSWORD_";
		String email = "TEST_USER_EMAIL_";
		String enable = "true";

		// Create Account
		AccountHelper accountHelper = new AccountHelper();
		// Account 1
		UserInformation userInformation = new UserInformation(userName + 1, userRealName + 1, password + 1, email + 1, enable);
		IAccount account1 = accountHelper.createAccount(userInformation, "user");
		// Account 2
		userInformation = new UserInformation(userName + 2, userRealName + 2, password + 2, email + 2, enable);
		IAccount account2 = accountHelper.createAccount(userInformation, "user");

		// Add accounts to List
		List<IAccount> accounts = new ArrayList<IAccount>();
		accounts.add(account1);
		accounts.add(account2);

		JSONArray accountJSONArray = JSONEncoder.toAccountJSONArray(accounts);

		// Assert
		assertEquals(2, accountJSONArray.length());

		JSONObject accountJSON1 = accountJSONArray.getJSONObject(0);
		assertEquals(account1.getID(), accountJSON1.getString(AccountEnum.USERNAME));
		assertEquals(account1.getName(), accountJSON1.getString(AccountEnum.NICK_NAME));
		assertEquals(account1.getPassword(), accountJSON1.getString(AccountEnum.PASSWORD));
		assertEquals(account1.getEmail(), accountJSON1.getString(AccountEnum.EMAIL));
		assertEquals(1, accountJSON1.getInt(AccountEnum.ENABLE));

		JSONObject accountJSON2 = accountJSONArray.getJSONObject(1);
		assertEquals(account2.getID(), accountJSON2.getString(AccountEnum.USERNAME));
		assertEquals(account2.getName(), accountJSON2.getString(AccountEnum.NICK_NAME));
		assertEquals(account2.getPassword(), accountJSON2.getString(AccountEnum.PASSWORD));
		assertEquals(account2.getEmail(), accountJSON2.getString(AccountEnum.EMAIL));
		assertEquals(1, accountJSON1.getInt(AccountEnum.ENABLE));
	}
	
	@Test
	public void testToAccountJSON() throws JSONException {
		// Test Data
		String userName = "TEST_USER_NAME";
		String userRealName = "TEST_USER_REAL_NAME";
		String password = "TEST_USER_PASSWORD";
		String email = "TEST_USER_EMAIL";
		String enable = "true";

		// Create Account
		AccountHelper accountHelper = new AccountHelper();
		UserInformation userInformation = new UserInformation(userName, userRealName, password, email, enable);
		IAccount account = accountHelper.createAccount(userInformation, "user");
		JSONObject accountJSON = JSONEncoder.toAccountJSON(account);

		// Assert
		assertEquals(userName, accountJSON.getString(AccountEnum.USERNAME));
		assertEquals(userRealName, accountJSON.getString(AccountEnum.NICK_NAME));
		assertEquals(account.getPassword(), accountJSON.getString(AccountEnum.PASSWORD));
		assertEquals(email, accountJSON.getString(AccountEnum.EMAIL));
		assertEquals(1, accountJSON.getInt(AccountEnum.ENABLE));
	}
	
	@Test
	public void testToRetrospectiveJSONArray() throws JSONException {
		IIssue issue1 = new Issue();
		issue1.setSummary("Good name");
		issue1.setDescription("Good description");
		issue1.setCategory("Good");
		issue1.setStatus("new");
		IScrumIssue good = new ScrumIssue(issue1);
		IIssue issue2 = new Issue();
		issue2.setSummary("Improvement name");
		issue2.setDescription("Improvement description");
		issue2.setCategory("Improvement");
		issue2.setStatus("closed");
		IScrumIssue improvement = new ScrumIssue(issue2);
		List<IScrumIssue> retrospectives = new ArrayList<IScrumIssue>();
		retrospectives.add(good);
		retrospectives.add(improvement);
		
		JSONArray retrospectiveJSONArray = JSONEncoder.toRetrospectiveJSONArray(retrospectives);
		assertEquals(2, retrospectiveJSONArray.length());
		
		assertEquals("Good name", retrospectiveJSONArray.getJSONObject(0).getString(RetrospectiveEnum.NAME));
		assertEquals("Good description", retrospectiveJSONArray.getJSONObject(0).getString(RetrospectiveEnum.DESCRIPTION));
		assertEquals("Good", retrospectiveJSONArray.getJSONObject(0).getString(RetrospectiveEnum.TYPE));
		assertEquals("new", retrospectiveJSONArray.getJSONObject(0).getString(RetrospectiveEnum.STATUS));
		
		assertEquals("Improvement name", retrospectiveJSONArray.getJSONObject(1).getString(RetrospectiveEnum.NAME));
		assertEquals("Improvement description", retrospectiveJSONArray.getJSONObject(1).getString(RetrospectiveEnum.DESCRIPTION));
		assertEquals("Improvement", retrospectiveJSONArray.getJSONObject(1).getString(RetrospectiveEnum.TYPE));
		assertEquals("closed", retrospectiveJSONArray.getJSONObject(1).getString(RetrospectiveEnum.STATUS));
	}
	
	@Test
	public void testToRetrospectiveJSON() throws JSONException {
		IIssue issue = new Issue();
		issue.setSummary("Retrospective name");
		issue.setDescription("Retrospective description");
		issue.setCategory("Good");
		issue.setStatus("new");
		IScrumIssue retrospective = new ScrumIssue(issue);
		JSONObject retrospectiveJSON = JSONEncoder.toRetrospectiveJSON(retrospective);
		assertEquals("Retrospective name", retrospectiveJSON.getString(RetrospectiveEnum.NAME));
		assertEquals("Retrospective description", retrospectiveJSON.getString(RetrospectiveEnum.DESCRIPTION));
		assertEquals("Good", retrospectiveJSON.getString(RetrospectiveEnum.TYPE));
		assertEquals("new", retrospectiveJSON.getString(RetrospectiveEnum.STATUS));
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
