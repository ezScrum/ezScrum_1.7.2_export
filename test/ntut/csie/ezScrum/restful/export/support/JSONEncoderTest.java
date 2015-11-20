package ntut.csie.ezScrum.restful.export.support;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.issue.core.IIssueHistory;
import ntut.csie.ezScrum.issue.core.IIssueTag;
import ntut.csie.ezScrum.issue.core.ITSEnum;
import ntut.csie.ezScrum.issue.internal.Issue;
import ntut.csie.ezScrum.issue.internal.IssueAttachFile;
import ntut.csie.ezScrum.issue.internal.IssueHistory;
import ntut.csie.ezScrum.issue.internal.IssueTag;
import ntut.csie.ezScrum.iteration.core.IReleasePlanDesc;
import ntut.csie.ezScrum.iteration.core.IScrumIssue;
import ntut.csie.ezScrum.iteration.core.ISprintPlanDesc;
import ntut.csie.ezScrum.iteration.iternal.ScrumIssue;
import ntut.csie.ezScrum.pic.core.ScrumRole;
import ntut.csie.ezScrum.restful.export.jsonEnum.AccountJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.AttachFileJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.ProjectJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.ReleaseJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.RetrospectiveJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.ScrumRoleJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.SprintJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.StoryJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.TagJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.TaskJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.UnplanJSONEnum;
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
import ntut.csie.ezScrum.web.control.ProductBacklogHelper;
import ntut.csie.ezScrum.web.dataObject.UserInformation;
import ntut.csie.ezScrum.web.helper.AccountHelper;
import ntut.csie.ezScrum.web.helper.SprintBacklogHelper;
import ntut.csie.ezScrum.web.helper.SprintPlanHelper;
import ntut.csie.ezScrum.web.mapper.SprintBacklogMapper;
import ntut.csie.jcis.account.core.IAccount;
import ntut.csie.jcis.resource.core.IProject;

public class JSONEncoderTest {
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
	public void testToUnplanJSONArray() throws JSONException {
		List<IIssue> unplans = mCU.getIssueList();
		JSONArray unplanJSONArray = JSONEncoder.toUnplanJSONArray(unplans);
		
		// Assert
		assertEquals(8, unplanJSONArray.length());
		
		assertEquals(unplans.get(0).getSummary(), unplanJSONArray.getJSONObject(0).getString(UnplanJSONEnum.NAME));
		assertEquals(unplans.get(0).getAssignto(), unplanJSONArray.getJSONObject(0).getString(UnplanJSONEnum.HANDLER));
		assertEquals(unplans.get(0).getEstimated(), unplanJSONArray.getJSONObject(0).getString(UnplanJSONEnum.ESTIMATE));
		assertEquals(unplans.get(0).getActualHour(), unplanJSONArray.getJSONObject(0).getString(UnplanJSONEnum.ACTUAL));
		assertEquals(unplans.get(0).getNotes(), unplanJSONArray.getJSONObject(0).getString(UnplanJSONEnum.NOTES));
		assertEquals(unplans.get(0).getStatus(), unplanJSONArray.getJSONObject(0).getString(UnplanJSONEnum.STATUS));
		
		assertEquals(unplans.get(1).getSummary(), unplanJSONArray.getJSONObject(1).getString(UnplanJSONEnum.NAME));
		assertEquals(unplans.get(1).getAssignto(), unplanJSONArray.getJSONObject(1).getString(UnplanJSONEnum.HANDLER));
		assertEquals(unplans.get(1).getEstimated(), unplanJSONArray.getJSONObject(1).getString(UnplanJSONEnum.ESTIMATE));
		assertEquals(unplans.get(1).getActualHour(), unplanJSONArray.getJSONObject(1).getString(UnplanJSONEnum.ACTUAL));
		assertEquals(unplans.get(1).getNotes(), unplanJSONArray.getJSONObject(1).getString(UnplanJSONEnum.NOTES));
		assertEquals(unplans.get(1).getStatus(), unplanJSONArray.getJSONObject(1).getString(UnplanJSONEnum.STATUS));
	}
	
	@Test
	public void testToUnplanJSON() throws JSONException {
		IIssue unplan = mCU.getIssueList().get(0);
		JSONObject unplanJSON = JSONEncoder.toUnplanJSON(unplan);
		
		// Assert
		assertEquals(unplan.getSummary(), unplanJSON.getString(UnplanJSONEnum.NAME));
		assertEquals(unplan.getAssignto(), unplanJSON.getString(UnplanJSONEnum.HANDLER));
		assertEquals(unplan.getEstimated(), unplanJSON.getString(UnplanJSONEnum.ESTIMATE));
		assertEquals(unplan.getActualHour(), unplanJSON.getString(UnplanJSONEnum.ACTUAL));
		assertEquals(unplan.getNotes(), unplanJSON.getString(UnplanJSONEnum.NOTES));
		assertEquals(unplan.getStatus(), unplanJSON.getString(UnplanJSONEnum.STATUS));
	}
	
	@Test
	public void testToAttachFileJSONArray() throws JSONException {
		// Test Data
		String testFile1 = "./TestData/RoleBase.xml";
		String testFile2 = "./TestData/InitialData/ScrumRole.xml";
		IIssue task = mATTS.getTaskList().get(0);
		IProject project = mCP.getProjectList().get(0);

		// Upload Attach File
		SprintBacklogMapper sprintBacklogMapper = new SprintBacklogMapper(project, null);
		sprintBacklogMapper.addAttachFile(task.getIssueID(), testFile1);
		sprintBacklogMapper.addAttachFile(task.getIssueID(), testFile2);
		
		// Get Task again
		SprintBacklogHelper sprintBacklogHelper = new SprintBacklogHelper(project, null);
		task = sprintBacklogHelper.getTaskById(task.getIssueID());
		
		ProductBacklogHelper productBacklogHelper = new ProductBacklogHelper(project, null);
		List<IssueAttachFile> attachFiles = task.getAttachFile();
		List<File> sourceFiles = new ArrayList<File>();
		for (IssueAttachFile attachFile : attachFiles) {
			String attachFileIdString = String.valueOf(attachFile.getAttachFileId());
			File srouceFile = productBacklogHelper.getAttachFile(attachFileIdString);
			sourceFiles.add(srouceFile);
		}
		JSONArray attachFilesJSONArray = JSONEncoder.toAttachFileJSONArray(attachFiles, sourceFiles);
		
		String expectedXmlBinary1 = FileEncoder.toBase64BinaryString(new File(testFile1));
		String expectedXmlBinary2 = FileEncoder.toBase64BinaryString(new File(testFile2));

		// Assert
		assertEquals(2, attachFilesJSONArray.length());
		assertEquals("RoleBase.xml", attachFilesJSONArray.getJSONObject(0).getString(AttachFileJSONEnum.NAME));
		assertEquals("text/xml", attachFilesJSONArray.getJSONObject(0).getString(AttachFileJSONEnum.CONTENT_TYPE));
		assertEquals(expectedXmlBinary1, attachFilesJSONArray.getJSONObject(0).getString(AttachFileJSONEnum.BINARY));

		assertEquals("ScrumRole.xml", attachFilesJSONArray.getJSONObject(1).getString(AttachFileJSONEnum.NAME));
		assertEquals("text/xml", attachFilesJSONArray.getJSONObject(1).getString(AttachFileJSONEnum.CONTENT_TYPE));
		assertEquals(expectedXmlBinary2, attachFilesJSONArray.getJSONObject(1).getString(AttachFileJSONEnum.BINARY));
	}

	@Test
	public void testToAttachFileJSON() throws JSONException {
		// Test Data
		String fileName = "RoleBase.xml";
		String fileType = "text/xml";

		// Create IssueAttachFile
		IssueAttachFile attachFile = new IssueAttachFile();
		attachFile.setFilename(fileName);
		attachFile.setFileType(fileType);

		File sourceFile = new File("./TestData/RoleBase.xml");
		JSONObject attachFileJSON = JSONEncoder.toAttachFileJSON(attachFile, sourceFile);
		String expectedFileBase64Binary = FileEncoder.toBase64BinaryString(sourceFile);

		// Assert
		assertEquals(fileName, attachFileJSON.getString(AttachFileJSONEnum.NAME));
		assertEquals(fileType, attachFileJSON.getString(AttachFileJSONEnum.CONTENT_TYPE));
		assertEquals(expectedFileBase64Binary, attachFileJSON.getString(AttachFileJSONEnum.BINARY));
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
		assertEquals(account1.getID(), accountJSON1.getString(AccountJSONEnum.USERNAME));
		assertEquals(account1.getName(), accountJSON1.getString(AccountJSONEnum.NICK_NAME));
		assertEquals(account1.getPassword(), accountJSON1.getString(AccountJSONEnum.PASSWORD));
		assertEquals(account1.getEmail(), accountJSON1.getString(AccountJSONEnum.EMAIL));
		assertEquals(1, accountJSON1.getInt(AccountJSONEnum.ENABLE));

		JSONObject accountJSON2 = accountJSONArray.getJSONObject(1);
		assertEquals(account2.getID(), accountJSON2.getString(AccountJSONEnum.USERNAME));
		assertEquals(account2.getName(), accountJSON2.getString(AccountJSONEnum.NICK_NAME));
		assertEquals(account2.getPassword(), accountJSON2.getString(AccountJSONEnum.PASSWORD));
		assertEquals(account2.getEmail(), accountJSON2.getString(AccountJSONEnum.EMAIL));
		assertEquals(1, accountJSON1.getInt(AccountJSONEnum.ENABLE));
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
		assertEquals(userName, accountJSON.getString(AccountJSONEnum.USERNAME));
		assertEquals(userRealName, accountJSON.getString(AccountJSONEnum.NICK_NAME));
		assertEquals(account.getPassword(), accountJSON.getString(AccountJSONEnum.PASSWORD));
		assertEquals(email, accountJSON.getString(AccountJSONEnum.EMAIL));
		assertEquals(1, accountJSON.getInt(AccountJSONEnum.ENABLE));
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
		
		assertEquals("Good name", retrospectiveJSONArray.getJSONObject(0).getString(RetrospectiveJSONEnum.NAME));
		assertEquals("Good description", retrospectiveJSONArray.getJSONObject(0).getString(RetrospectiveJSONEnum.DESCRIPTION));
		assertEquals("Good", retrospectiveJSONArray.getJSONObject(0).getString(RetrospectiveJSONEnum.TYPE));
		assertEquals("new", retrospectiveJSONArray.getJSONObject(0).getString(RetrospectiveJSONEnum.STATUS));
		
		assertEquals("Improvement name", retrospectiveJSONArray.getJSONObject(1).getString(RetrospectiveJSONEnum.NAME));
		assertEquals("Improvement description", retrospectiveJSONArray.getJSONObject(1).getString(RetrospectiveJSONEnum.DESCRIPTION));
		assertEquals("Improvement", retrospectiveJSONArray.getJSONObject(1).getString(RetrospectiveJSONEnum.TYPE));
		assertEquals("closed", retrospectiveJSONArray.getJSONObject(1).getString(RetrospectiveJSONEnum.STATUS));
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
		assertEquals("Retrospective name", retrospectiveJSON.getString(RetrospectiveJSONEnum.NAME));
		assertEquals("Retrospective description", retrospectiveJSON.getString(RetrospectiveJSONEnum.DESCRIPTION));
		assertEquals("Good", retrospectiveJSON.getString(RetrospectiveJSONEnum.TYPE));
		assertEquals("new", retrospectiveJSON.getString(RetrospectiveJSONEnum.STATUS));
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
		assertEquals(Long.parseLong(sprint1.getID()), sprint1JSON.getLong(SprintJSONEnum.ID));
		assertEquals(sprint1.getGoal(), sprint1JSON.getString(SprintJSONEnum.GOAL));
		assertEquals(Integer.parseInt(sprint1.getInterval()), sprint1JSON.getInt(SprintJSONEnum.INTERVAL));
		assertEquals(Integer.parseInt(sprint1.getMemberNumber()), sprint1JSON.getInt(SprintJSONEnum.TEAM_SIZE));
		assertEquals(Integer.parseInt(sprint1.getAvailableDays()), sprint1JSON.getInt(SprintJSONEnum.AVAILABLE_HOURS));
		assertEquals(Integer.parseInt(sprint1.getFocusFactor()), sprint1JSON.getInt(SprintJSONEnum.FOCUS_FACTOR));
		assertEquals(sprint1.getStartDate(), sprint1JSON.getString(SprintJSONEnum.START_DATE));
		assertEquals(sprint1.getEndDate(), sprint1JSON.getString(SprintJSONEnum.DUE_DATE));
		assertEquals(sprint1.getDemoDate(), sprint1JSON.getString(SprintJSONEnum.DEMO_DATE));
		assertEquals(sprint1.getDemoPlace(), sprint1JSON.getString(SprintJSONEnum.DEMO_PLACE));
		assertEquals(sprint1.getNotes(), sprint1JSON.getString(SprintJSONEnum.DAILY_INFO));

		// Assert Sprint2
		ISprintPlanDesc sprint2 = sprints.get(1);
		JSONObject sprint2JSON = sprintJSONArray.getJSONObject(1);
		assertEquals(Long.parseLong(sprint2.getID()), sprint2JSON.getLong(SprintJSONEnum.ID));
		assertEquals(sprint2.getGoal(), sprint2JSON.getString(SprintJSONEnum.GOAL));
		assertEquals(Integer.parseInt(sprint2.getInterval()), sprint2JSON.getInt(SprintJSONEnum.INTERVAL));
		assertEquals(Integer.parseInt(sprint2.getMemberNumber()), sprint2JSON.getInt(SprintJSONEnum.TEAM_SIZE));
		assertEquals(Integer.parseInt(sprint2.getAvailableDays()), sprint2JSON.getInt(SprintJSONEnum.AVAILABLE_HOURS));
		assertEquals(Integer.parseInt(sprint2.getFocusFactor()), sprint2JSON.getInt(SprintJSONEnum.FOCUS_FACTOR));
		assertEquals(sprint2.getStartDate(), sprint2JSON.getString(SprintJSONEnum.START_DATE));
		assertEquals(sprint2.getEndDate(), sprint2JSON.getString(SprintJSONEnum.DUE_DATE));
		assertEquals(sprint2.getDemoDate(), sprint2JSON.getString(SprintJSONEnum.DEMO_DATE));
		assertEquals(sprint2.getDemoPlace(), sprint2JSON.getString(SprintJSONEnum.DEMO_PLACE));
		assertEquals(sprint2.getNotes(), sprint2JSON.getString(SprintJSONEnum.DAILY_INFO));
	}

	@Test
	public void testToSprintJSON() throws JSONException {
		String sprintId = mCS.getSprintIDList().get(0);
		IProject project = mCP.getProjectList().get(0);
		SprintPlanHelper sprintPlanHelper = new SprintPlanHelper(project);
		ISprintPlanDesc sprint = sprintPlanHelper.loadPlan(sprintId);
		JSONObject sprintJSON = JSONEncoder.toSprintJSON(sprint);

		// Assert
		assertEquals(Long.parseLong(sprint.getID()), sprintJSON.getLong(SprintJSONEnum.ID));
		assertEquals(sprint.getGoal(), sprintJSON.getString(SprintJSONEnum.GOAL));
		assertEquals(Integer.parseInt(sprint.getInterval()), sprintJSON.getInt(SprintJSONEnum.INTERVAL));
		assertEquals(Integer.parseInt(sprint.getMemberNumber()), sprintJSON.getInt(SprintJSONEnum.TEAM_SIZE));
		assertEquals(Integer.parseInt(sprint.getAvailableDays()), sprintJSON.getInt(SprintJSONEnum.AVAILABLE_HOURS));
		assertEquals(Integer.parseInt(sprint.getFocusFactor()), sprintJSON.getInt(SprintJSONEnum.FOCUS_FACTOR));
		assertEquals(sprint.getStartDate(), sprintJSON.getString(SprintJSONEnum.START_DATE));
		assertEquals(sprint.getEndDate(), sprintJSON.getString(SprintJSONEnum.DUE_DATE));
		assertEquals(sprint.getDemoDate(), sprintJSON.getString(SprintJSONEnum.DEMO_DATE));
		assertEquals(sprint.getDemoPlace(), sprintJSON.getString(SprintJSONEnum.DEMO_PLACE));
		assertEquals(sprint.getNotes(), sprintJSON.getString(SprintJSONEnum.DAILY_INFO));
	}

	@Test
	public void testToProjectJSONArray() throws JSONException {
		List<IProject> projects = mCP.getProjectList();
		JSONArray projectJSONArray = JSONEncoder.toProjectJSONArray(projects);

		// Assert Project1
		IProject project1 = projects.get(0);
		JSONObject project1JSON = projectJSONArray.getJSONObject(0);
		assertEquals(project1.getName(), project1JSON.getString(ProjectJSONEnum.NAME));
		assertEquals(project1.getProjectDesc().getDisplayName(), project1JSON.getString(ProjectJSONEnum.DISPLAY_NAME));
		assertEquals(project1.getProjectDesc().getComment(), project1JSON.getString(ProjectJSONEnum.COMMENT));
		assertEquals(project1.getProjectDesc().getProjectManager(), project1JSON.getString(ProjectJSONEnum.PRODUCT_OWNER));
		assertEquals(Long.parseLong(project1.getProjectDesc().getAttachFileSize()), project1JSON.getLong(ProjectJSONEnum.ATTATCH_MAX_SIZE));

		// Assert Project2
		IProject project2 = projects.get(1);
		JSONObject project2JSON = JSONEncoder.toProjectJSON(project2);
		assertEquals(project2.getName(), project2JSON.getString(ProjectJSONEnum.NAME));
		assertEquals(project2.getProjectDesc().getDisplayName(), project2JSON.getString(ProjectJSONEnum.DISPLAY_NAME));
		assertEquals(project2.getProjectDesc().getComment(), project2JSON.getString(ProjectJSONEnum.COMMENT));
		assertEquals(project2.getProjectDesc().getProjectManager(), project2JSON.getString(ProjectJSONEnum.PRODUCT_OWNER));
		assertEquals(Long.parseLong(project2.getProjectDesc().getAttachFileSize()), project2JSON.getLong(ProjectJSONEnum.ATTATCH_MAX_SIZE));
	}

	@Test
	public void testToProjectJSON() throws JSONException {
		IProject project = mCP.getProjectList().get(0);
		JSONObject projectJSON = JSONEncoder.toProjectJSON(project);

		// Assert
		assertEquals(project.getName(), projectJSON.getString(ProjectJSONEnum.NAME));
		assertEquals(project.getProjectDesc().getDisplayName(), projectJSON.getString(ProjectJSONEnum.DISPLAY_NAME));
		assertEquals(project.getProjectDesc().getComment(), projectJSON.getString(ProjectJSONEnum.COMMENT));
		assertEquals(project.getProjectDesc().getProjectManager(), projectJSON.getString(ProjectJSONEnum.PRODUCT_OWNER));
		assertEquals(Long.parseLong(project.getProjectDesc().getAttachFileSize()), projectJSON.getLong(ProjectJSONEnum.ATTATCH_MAX_SIZE));
	}

	@Test
	public void testToStoryJSONArray() throws JSONException {
		// Get Stories
		List<IIssue> stories = mASTS.getIssueList();
		// Convert to JSONArray
		JSONArray storyJSONArray = JSONEncoder.toStoryJSONArray(stories);

		// Assert
		IIssue story1 = stories.get(0);
		assertEquals(story1.getIssueID(), storyJSONArray.getJSONObject(0).get(StoryJSONEnum.ID));
		assertEquals(story1.getSummary(), storyJSONArray.getJSONObject(0).get(StoryJSONEnum.NAME));
		assertEquals(story1.getStatus(), storyJSONArray.getJSONObject(0).get(StoryJSONEnum.STATUS));
		assertEquals(Integer.parseInt(story1.getEstimated()), storyJSONArray.getJSONObject(0).get(StoryJSONEnum.ESTIMATE));
		assertEquals(Integer.parseInt(story1.getImportance()), storyJSONArray.getJSONObject(0).get(StoryJSONEnum.IMPORTANCE));
		assertEquals(Integer.parseInt(story1.getValue()), storyJSONArray.getJSONObject(0).get(StoryJSONEnum.VALUE));
		assertEquals(story1.getNotes(), storyJSONArray.getJSONObject(0).get(StoryJSONEnum.NOTES));
		assertEquals(story1.getHowToDemo(), storyJSONArray.getJSONObject(0).get(StoryJSONEnum.HOW_TO_DEMO));

		IIssue story2 = stories.get(1);
		assertEquals(story2.getIssueID(), storyJSONArray.getJSONObject(1).get(StoryJSONEnum.ID));
		assertEquals(story2.getSummary(), storyJSONArray.getJSONObject(1).get(StoryJSONEnum.NAME));
		assertEquals(story2.getStatus(), storyJSONArray.getJSONObject(1).get(StoryJSONEnum.STATUS));
		assertEquals(Integer.parseInt(story2.getEstimated()), storyJSONArray.getJSONObject(1).get(StoryJSONEnum.ESTIMATE));
		assertEquals(Integer.parseInt(story2.getImportance()), storyJSONArray.getJSONObject(1).get(StoryJSONEnum.IMPORTANCE));
		assertEquals(Integer.parseInt(story2.getValue()), storyJSONArray.getJSONObject(1).get(StoryJSONEnum.VALUE));
		assertEquals(story2.getNotes(), storyJSONArray.getJSONObject(1).get(StoryJSONEnum.NOTES));
		assertEquals(story2.getHowToDemo(), storyJSONArray.getJSONObject(1).get(StoryJSONEnum.HOW_TO_DEMO));

		IIssue story3 = stories.get(2);
		assertEquals(story3.getIssueID(), storyJSONArray.getJSONObject(2).get(StoryJSONEnum.ID));
		assertEquals(story3.getSummary(), storyJSONArray.getJSONObject(2).get(StoryJSONEnum.NAME));
		assertEquals(story3.getStatus(), storyJSONArray.getJSONObject(2).get(StoryJSONEnum.STATUS));
		assertEquals(Integer.parseInt(story3.getEstimated()), storyJSONArray.getJSONObject(2).get(StoryJSONEnum.ESTIMATE));
		assertEquals(Integer.parseInt(story3.getImportance()), storyJSONArray.getJSONObject(2).get(StoryJSONEnum.IMPORTANCE));
		assertEquals(Integer.parseInt(story3.getValue()), storyJSONArray.getJSONObject(2).get(StoryJSONEnum.VALUE));
		assertEquals(story3.getNotes(), storyJSONArray.getJSONObject(2).get(StoryJSONEnum.NOTES));
		assertEquals(story3.getHowToDemo(), storyJSONArray.getJSONObject(2).get(StoryJSONEnum.HOW_TO_DEMO));

		IIssue story4 = stories.get(3);
		assertEquals(story4.getIssueID(), storyJSONArray.getJSONObject(3).get(StoryJSONEnum.ID));
		assertEquals(story4.getSummary(), storyJSONArray.getJSONObject(3).get(StoryJSONEnum.NAME));
		assertEquals(story4.getStatus(), storyJSONArray.getJSONObject(3).get(StoryJSONEnum.STATUS));
		assertEquals(Integer.parseInt(story4.getEstimated()), storyJSONArray.getJSONObject(3).get(StoryJSONEnum.ESTIMATE));
		assertEquals(Integer.parseInt(story4.getImportance()), storyJSONArray.getJSONObject(3).get(StoryJSONEnum.IMPORTANCE));
		assertEquals(Integer.parseInt(story4.getValue()), storyJSONArray.getJSONObject(3).get(StoryJSONEnum.VALUE));
		assertEquals(story4.getNotes(), storyJSONArray.getJSONObject(3).get(StoryJSONEnum.NOTES));
		assertEquals(story4.getHowToDemo(), storyJSONArray.getJSONObject(3).get(StoryJSONEnum.HOW_TO_DEMO));
	}

	@Test
	public void testToStoryJSON() throws JSONException {
		IIssue story = mASTS.getIssueList().get(0);
		// Convert to JSONObject
		JSONObject storyJson = JSONEncoder.toStoryJSON(story);

		// Assert
		assertEquals(story.getIssueID(), storyJson.get(StoryJSONEnum.ID));
		assertEquals(story.getSummary(), storyJson.get(StoryJSONEnum.NAME));
		assertEquals(story.getStatus(), storyJson.get(StoryJSONEnum.STATUS));
		assertEquals(Integer.parseInt(story.getEstimated()), storyJson.get(StoryJSONEnum.ESTIMATE));
		assertEquals(Integer.parseInt(story.getImportance()), storyJson.get(StoryJSONEnum.IMPORTANCE));
		assertEquals(Integer.parseInt(story.getValue()), storyJson.get(StoryJSONEnum.VALUE));
		assertEquals(story.getNotes(), storyJson.get(StoryJSONEnum.NOTES));
		assertEquals(story.getHowToDemo(), storyJson.get(StoryJSONEnum.HOW_TO_DEMO));
	}

	@Test
	public void testToTaskJSONArray() throws JSONException {
		List<IIssue> tasks = mCT.getTaskList();

		JSONArray taskJSONArray = JSONEncoder.toTaskJSONArray(tasks);

		// Assert Task1
		IIssue task1 = tasks.get(0);
		JSONObject task1JSON = taskJSONArray.getJSONObject(0);
		assertEquals(task1.getSummary(), task1JSON.getString(TaskJSONEnum.NAME));
		assertEquals(task1.getAssignto(), task1JSON.getString(TaskJSONEnum.HANDLER));
		assertEquals(Integer.parseInt(task1.getEstimated()), task1JSON.getInt(TaskJSONEnum.ESTIMATE));
		assertEquals(Integer.parseInt(task1.getRemains()), task1JSON.getInt(TaskJSONEnum.REMAIN));
		assertEquals(Integer.parseInt(task1.getActualHour()), task1JSON.getInt(TaskJSONEnum.ACTUAL));
		assertEquals(task1.getNotes(), task1JSON.getString(TaskJSONEnum.NOTES));
		assertEquals(task1.getStatus(), task1JSON.getString(TaskJSONEnum.STATUS));
		// Assert Task2
		IIssue task2 = tasks.get(1);
		JSONObject task2JSON = taskJSONArray.getJSONObject(1);
		assertEquals(task2.getSummary(), task2JSON.getString(TaskJSONEnum.NAME));
		assertEquals(task2.getAssignto(), task2JSON.getString(TaskJSONEnum.HANDLER));
		assertEquals(Integer.parseInt(task2.getEstimated()), task2JSON.getInt(TaskJSONEnum.ESTIMATE));
		assertEquals(Integer.parseInt(task2.getRemains()), task2JSON.getInt(TaskJSONEnum.REMAIN));
		assertEquals(Integer.parseInt(task2.getActualHour()), task2JSON.getInt(TaskJSONEnum.ACTUAL));
		assertEquals(task2.getNotes(), task2JSON.getString(TaskJSONEnum.NOTES));
		assertEquals(task2.getStatus(), task2JSON.getString(TaskJSONEnum.STATUS));
	}

	@Test
	public void testToTaskJSON() throws JSONException {
		List<IIssue> tasks = mCT.getTaskList();
		IIssue task = tasks.get(0);

		JSONObject taskJSON = JSONEncoder.toTaskJSON(task);

		// Assert
		assertEquals(task.getSummary(), taskJSON.getString(TaskJSONEnum.NAME));
		assertEquals(task.getAssignto(), taskJSON.getString(TaskJSONEnum.HANDLER));
		assertEquals(Integer.parseInt(task.getEstimated()), taskJSON.getInt(TaskJSONEnum.ESTIMATE));
		assertEquals(Integer.parseInt(task.getRemains()), taskJSON.getInt(TaskJSONEnum.REMAIN));
		assertEquals(Integer.parseInt(task.getActualHour()), taskJSON.getInt(TaskJSONEnum.ACTUAL));
		assertEquals(task.getNotes(), taskJSON.getString(TaskJSONEnum.NOTES));
		assertEquals(task.getStatus(), taskJSON.getString(TaskJSONEnum.STATUS));
	}
	
	@Test
	public void testToReleaseJSONArray() throws JSONException {
		List<IReleasePlanDesc> releases = mCR.getReleaseList();
		// Convert to JSONOArray
		JSONArray releaseJSONArray = JSONEncoder.toReleaseJSONArray(releases);

		// Assert
		JSONObject releaseJSON1 = releaseJSONArray.getJSONObject(0);
		IReleasePlanDesc release1 = releases.get(0);
		assertEquals(release1.getName(), releaseJSON1.getString(ReleaseJSONEnum.NAME));
		assertEquals(release1.getDescription(), releaseJSON1.getString(ReleaseJSONEnum.DESCRIPTION));
		assertEquals(release1.getStartDate(), releaseJSON1.getString(ReleaseJSONEnum.START_DATE));
		assertEquals(release1.getEndDate(), releaseJSON1.getString(ReleaseJSONEnum.DUE_DATE));

		JSONObject releaseJSON2 = releaseJSONArray.getJSONObject(1);
		IReleasePlanDesc release2 = releases.get(1);
		assertEquals(release2.getName(), releaseJSON2.getString(ReleaseJSONEnum.NAME));
		assertEquals(release2.getDescription(), releaseJSON2.getString(ReleaseJSONEnum.DESCRIPTION));
		assertEquals(release2.getStartDate(), releaseJSON2.getString(ReleaseJSONEnum.START_DATE));
		assertEquals(release2.getEndDate(), releaseJSON2.getString(ReleaseJSONEnum.DUE_DATE));
	}

	@Test
	public void testToReleaseJSON() throws JSONException {
		IReleasePlanDesc release = mCR.getReleaseList().get(0);
		// Convert to JSONObject
		JSONObject releaseJSON = JSONEncoder.toReleaseJSON(release);

		// Assert
		assertEquals(release.getName(), releaseJSON.getString(ReleaseJSONEnum.NAME));
		assertEquals(release.getDescription(), releaseJSON.getString(ReleaseJSONEnum.DESCRIPTION));
		assertEquals(release.getStartDate(), releaseJSON.getString(ReleaseJSONEnum.START_DATE));
		assertEquals(release.getEndDate(), releaseJSON.getString(ReleaseJSONEnum.DUE_DATE));
	}
	
	@Test
	public void testToTagJSONArray() throws JSONException {
		List<IIssueTag> tags = new ArrayList<IIssueTag>();
		IIssueTag tag1 = new IssueTag();
		tag1.setTagName("Data Migration");
		IIssueTag tag2 = new IssueTag();
		tag2.setTagName("Thesis");
		tags.add(tag1);
		tags.add(tag2);
		
		// Convert to JSONArray
		JSONArray tagJSONArray = JSONEncoder.toTagJSONArray(tags);
		
		// Assert
		assertEquals(2, tagJSONArray.length());
		assertEquals("Data Migration", tagJSONArray.getJSONObject(0).getString(TagJSONEnum.NAME));
		assertEquals("Thesis", tagJSONArray.getJSONObject(1).getString(TagJSONEnum.NAME));
	}
	
	@Test
	public void testToTagJSON() throws JSONException {
		IIssueTag tag = new IssueTag();
		tag.setTagName("Data Migration");
		
		// Convert to JSONObject
		JSONObject tagJSON = JSONEncoder.toTagJSON(tag);
		
		// Assert
		assertEquals("Data Migration", tagJSON.getString(TagJSONEnum.NAME));
	}
	
	@Test
	public void testToScrumRolesJSON() throws JSONException {
		IProject project = mCP.getProjectList().get(0);
		ScrumRole productOwner = new ScrumRole(project.getName(), ScrumRoleJSONEnum.PRODUCT_OWNER);
		productOwner.setAccessProductBacklog(true);
		productOwner.setAccessSprintPlan(true);
		productOwner.setAccessTaskBoard(false);
		productOwner.setAccessSprintBacklog(true);
		productOwner.setAccessReleasePlan(true);
		productOwner.setAccessRetrospective(false);
		productOwner.setAccessUnplannedItem(false);
		productOwner.setReadReport(true);
		productOwner.setEditProject(true);
		
		ScrumRole scrumMaster = new ScrumRole(project.getName(), ScrumRoleJSONEnum.SCRUM_MASTER);
		scrumMaster.setAccessProductBacklog(true);
		scrumMaster.setAccessSprintPlan(true);
		scrumMaster.setAccessTaskBoard(true);
		scrumMaster.setAccessSprintBacklog(true);
		scrumMaster.setAccessReleasePlan(true);
		scrumMaster.setAccessRetrospective(true);
		scrumMaster.setAccessUnplannedItem(true);
		scrumMaster.setReadReport(true);
		scrumMaster.setEditProject(false);
		
		ScrumRole scrumTeam = new ScrumRole(project.getName(), ScrumRoleJSONEnum.SCRUM_TEAM);
		scrumTeam.setAccessProductBacklog(false);
		scrumTeam.setAccessSprintPlan(true);
		scrumTeam.setAccessTaskBoard(true);
		scrumTeam.setAccessSprintBacklog(true);
		scrumTeam.setAccessReleasePlan(true);
		scrumTeam.setAccessRetrospective(true);
		scrumTeam.setAccessUnplannedItem(true);
		scrumTeam.setReadReport(true);
		scrumTeam.setEditProject(false);
		
		ScrumRole stakeholder = new ScrumRole(project.getName(), ScrumRoleJSONEnum.STAKEHOLDER);
		stakeholder.setAccessProductBacklog(false);
		stakeholder.setAccessSprintPlan(false);
		stakeholder.setAccessTaskBoard(false);
		stakeholder.setAccessSprintBacklog(false);
		stakeholder.setAccessReleasePlan(true);
		stakeholder.setAccessRetrospective(false);
		stakeholder.setAccessUnplannedItem(false);
		stakeholder.setReadReport(true);
		stakeholder.setEditProject(false);
		
		ScrumRole guest = new ScrumRole(project.getName(), ScrumRoleJSONEnum.GUEST);
		guest.setAccessProductBacklog(false);
		guest.setAccessSprintPlan(false);
		guest.setAccessTaskBoard(false);
		guest.setAccessSprintBacklog(false);
		guest.setAccessReleasePlan(true);
		guest.setAccessRetrospective(false);
		guest.setAccessUnplannedItem(false);
		guest.setReadReport(true);
		guest.setEditProject(false);
		
		JSONObject scrumRolesJSON = JSONEncoder.toScrumRolesJSON(productOwner, scrumMaster, scrumTeam, stakeholder, guest);
		
		// Assert Product Owner
		JSONObject productOwnerJSON = scrumRolesJSON.getJSONObject(ScrumRoleJSONEnum.PRODUCT_OWNER);
		assertEquals(productOwner.getAccessProductBacklog(), productOwnerJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_PRODUCT_BACKLOG));
		assertEquals(productOwner.getAccessSprintPlan(), productOwnerJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_SPRINT_PLAN));
		assertEquals(productOwner.getAccessTaskBoard(), productOwnerJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_TASKBOARD));
		assertEquals(productOwner.getAccessSprintBacklog(), productOwnerJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_SPRINT_BACKLOG));
		assertEquals(productOwner.getAccessReleasePlan(), productOwnerJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_RELEASE_PLAN));
		assertEquals(productOwner.getAccessRetrospective(), productOwnerJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_RETROSPECTIVE));
		assertEquals(productOwner.getAccessUnplannedItem(), productOwnerJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_UNPLANNED));
		assertEquals(productOwner.getReadReport(), productOwnerJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_REPORT));
		assertEquals(productOwner.getEditProject(), productOwnerJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_EDIT_PROJECT));
		
		// Assert Scrum Master
		JSONObject scrumMasterJSON = scrumRolesJSON.getJSONObject(ScrumRoleJSONEnum.SCRUM_MASTER);
		assertEquals(scrumMaster.getAccessProductBacklog(), scrumMasterJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_PRODUCT_BACKLOG));
		assertEquals(scrumMaster.getAccessSprintPlan(), scrumMasterJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_SPRINT_PLAN));
		assertEquals(scrumMaster.getAccessTaskBoard(), scrumMasterJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_TASKBOARD));
		assertEquals(scrumMaster.getAccessSprintBacklog(), scrumMasterJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_SPRINT_BACKLOG));
		assertEquals(scrumMaster.getAccessReleasePlan(), scrumMasterJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_RELEASE_PLAN));
		assertEquals(scrumMaster.getAccessRetrospective(), scrumMasterJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_RETROSPECTIVE));
		assertEquals(scrumMaster.getAccessUnplannedItem(), scrumMasterJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_UNPLANNED));
		assertEquals(scrumMaster.getReadReport(), scrumMasterJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_REPORT));
		assertEquals(scrumMaster.getEditProject(), scrumMasterJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_EDIT_PROJECT));
		
		// Assert Scrum Team
		JSONObject scrumTeamJSON = scrumRolesJSON.getJSONObject(ScrumRoleJSONEnum.SCRUM_TEAM);
		assertEquals(scrumTeam.getAccessProductBacklog(), scrumTeamJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_PRODUCT_BACKLOG));
		assertEquals(scrumTeam.getAccessSprintPlan(), scrumTeamJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_SPRINT_PLAN));
		assertEquals(scrumTeam.getAccessTaskBoard(), scrumTeamJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_TASKBOARD));
		assertEquals(scrumTeam.getAccessSprintBacklog(), scrumTeamJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_SPRINT_BACKLOG));
		assertEquals(scrumTeam.getAccessReleasePlan(), scrumTeamJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_RELEASE_PLAN));
		assertEquals(scrumTeam.getAccessRetrospective(), scrumTeamJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_RETROSPECTIVE));
		assertEquals(scrumTeam.getAccessUnplannedItem(), scrumTeamJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_UNPLANNED));
		assertEquals(scrumTeam.getReadReport(), scrumTeamJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_REPORT));
		assertEquals(scrumTeam.getEditProject(), scrumTeamJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_EDIT_PROJECT));
		
		// Assert Stakeholder
		JSONObject stakeholderJSON = scrumRolesJSON.getJSONObject(ScrumRoleJSONEnum.STAKEHOLDER);
		assertEquals(stakeholder.getAccessProductBacklog(), stakeholderJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_PRODUCT_BACKLOG));
		assertEquals(stakeholder.getAccessSprintPlan(), stakeholderJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_SPRINT_PLAN));
		assertEquals(stakeholder.getAccessTaskBoard(), stakeholderJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_TASKBOARD));
		assertEquals(stakeholder.getAccessSprintBacklog(), stakeholderJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_SPRINT_BACKLOG));
		assertEquals(stakeholder.getAccessReleasePlan(), stakeholderJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_RELEASE_PLAN));
		assertEquals(stakeholder.getAccessRetrospective(), stakeholderJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_RETROSPECTIVE));
		assertEquals(stakeholder.getAccessUnplannedItem(), stakeholderJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_UNPLANNED));
		assertEquals(stakeholder.getReadReport(), stakeholderJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_REPORT));
		assertEquals(stakeholder.getEditProject(), stakeholderJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_EDIT_PROJECT));
		
		// Assert Guest
		JSONObject guestJSON = scrumRolesJSON.getJSONObject(ScrumRoleJSONEnum.GUEST);
		assertEquals(guest.getAccessProductBacklog(), guestJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_PRODUCT_BACKLOG));
		assertEquals(guest.getAccessSprintPlan(), guestJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_SPRINT_PLAN));
		assertEquals(guest.getAccessTaskBoard(), guestJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_TASKBOARD));
		assertEquals(guest.getAccessSprintBacklog(), guestJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_SPRINT_BACKLOG));
		assertEquals(guest.getAccessReleasePlan(), guestJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_RELEASE_PLAN));
		assertEquals(guest.getAccessRetrospective(), guestJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_RETROSPECTIVE));
		assertEquals(guest.getAccessUnplannedItem(), guestJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_UNPLANNED));
		assertEquals(guest.getReadReport(), guestJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_REPORT));
		assertEquals(guest.getEditProject(), guestJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_EDIT_PROJECT));
	}
	
	@Test
	public void testToScrumRoleJSON() throws JSONException {
		IProject project = mCP.getProjectList().get(0);
		ScrumRole scrumRole = new ScrumRole(project.getName(), ScrumRoleJSONEnum.PRODUCT_OWNER);
		scrumRole.setAccessProductBacklog(true);
		scrumRole.setEditProject(true);
		
		JSONObject scrumRoleJSON = JSONEncoder.toScrumRoleJSON(scrumRole);
		
		// Assert
		assertEquals(scrumRole.getAccessProductBacklog(), scrumRoleJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_PRODUCT_BACKLOG));
		assertEquals(scrumRole.getAccessSprintPlan(), scrumRoleJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_SPRINT_PLAN));
		assertEquals(scrumRole.getAccessTaskBoard(), scrumRoleJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_TASKBOARD));
		assertEquals(scrumRole.getAccessSprintBacklog(), scrumRoleJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_SPRINT_BACKLOG));
		assertEquals(scrumRole.getAccessReleasePlan(), scrumRoleJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_RELEASE_PLAN));
		assertEquals(scrumRole.getAccessRetrospective(), scrumRoleJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_RETROSPECTIVE));
		assertEquals(scrumRole.getAccessUnplannedItem(), scrumRoleJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_UNPLANNED));
		assertEquals(scrumRole.getReadReport(), scrumRoleJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_REPORT));
		assertEquals(scrumRole.getEditProject(), scrumRoleJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_EDIT_PROJECT));
	}
	
	@Test
	public void testToProjectRoleJSONArray() throws Exception {
		IProject project = mCP.getProjectList().get(0);
		
		// Test Data
		String userName = "TEST_USER_NAME_";
		String userRealName = "TEST_USER_REAL_NAME_";
		String password = "TEST_USER_PASSWORD_";
		String email = "TEST_USER_EMAIL_";
		String enable = "true";
		
		// Create Accounts
		AccountHelper accountHelper = new AccountHelper();
		// Account 1
		UserInformation userInformation = new UserInformation(userName + 1, userRealName + 1, password + 1, email + 1, enable);
		IAccount account1 = accountHelper.createAccount(userInformation, "user");
		// Account 2
		userInformation = new UserInformation(userName + 2, userRealName + 2, password + 2, email + 2, enable);
		IAccount account2 = accountHelper.createAccount(userInformation, "user");
		
		IAccount resultAccount1 = accountHelper.assignRole_add(mConfig.getUserSession(), account1.getID(), project.getName(), ScrumRoleJSONEnum.PRODUCT_OWNER);
		IAccount resultAccount2 = accountHelper.assignRole_add(mConfig.getUserSession(), account2.getID(), project.getName(), ScrumRoleJSONEnum.SCRUM_MASTER);
		
		List<IAccount> projectRoles = new ArrayList<IAccount>();
		projectRoles.add(resultAccount1);
		projectRoles.add(resultAccount2);
		JSONArray projectRoleJSONArray = JSONEncoder.toProjectRoleJSONArray(project.getName(), projectRoles);
		
		// Assert
		assertEquals(2, projectRoleJSONArray.length());
		
		assertEquals(account1.getID(), projectRoleJSONArray.getJSONObject(0).getString(AccountJSONEnum.USERNAME));
		assertEquals(ScrumRoleJSONEnum.PRODUCT_OWNER, projectRoleJSONArray.getJSONObject(0).getString(ScrumRoleJSONEnum.ROLE));
		
		assertEquals(account2.getID(), projectRoleJSONArray.getJSONObject(1).getString(AccountJSONEnum.USERNAME));
		assertEquals(ScrumRoleJSONEnum.SCRUM_MASTER, projectRoleJSONArray.getJSONObject(1).getString(ScrumRoleJSONEnum.ROLE));
	}
	
	@Test
	public void testToProjectRoleJSON() throws Exception {
		IProject project = mCP.getProjectList().get(0);
		
		// Test Data
		String userName = "TEST_USER_NAME";
		String userRealName = "TEST_USER_REAL_NAME";
		String password = "TEST_USER_PASSWORD";
		String email = "TEST_USER_EMAIL";
		String enable = "true";
		
		// Create Accounts
		AccountHelper accountHelper = new AccountHelper();
		// Account
		UserInformation userInformation = new UserInformation(userName, userRealName, password, email, enable);
		IAccount projectRole = accountHelper.createAccount(userInformation, "user");
		
		IAccount resultAccount = accountHelper.assignRole_add(mConfig.getUserSession(), projectRole.getID(), project.getName(), ScrumRoleJSONEnum.PRODUCT_OWNER);
		JSONObject projectRoleJSON = JSONEncoder.toProjectRoleJSON(project.getName(), resultAccount);
		
		// Assert
		assertEquals(userName, projectRoleJSON.getString(AccountJSONEnum.USERNAME));
		assertEquals(ScrumRoleJSONEnum.PRODUCT_OWNER, projectRoleJSON.getString(ScrumRoleJSONEnum.ROLE));
	}
	
	@Test
	public void testToHistoryJSONArray_fillterUselessHistories() {
		IIssue story = mASTS.getIssueList().get(0);
		long modifyDate = System.currentTimeMillis();
		IssueHistory history1 = new IssueHistory();
		history1.setIssueID(story.getIssueID());
		history1.setType(IIssueHistory.OTHER_TYPE);
		history1.setFieldName(IssueHistory.STATUS_FIELD_NAME);
		history1.setOldValue(String.valueOf(ITSEnum.NEW_STATUS));
		history1.setNewValue(String.valueOf(ITSEnum.CONFIRMED_STATUS));
		history1.setModifyDate(modifyDate);
		IssueHistory history2 = new IssueHistory();
		history2.setIssueID(story.getIssueID());
		history2.setType(IIssueHistory.OTHER_TYPE);
		history2.setFieldName(IssueHistory.STATUS_FIELD_NAME);
		history2.setOldValue(String.valueOf(ITSEnum.CONFIRMED_STATUS));
		history2.setNewValue(String.valueOf(ITSEnum.CLOSED_STATUS));
		history2.setModifyDate(modifyDate);
		IssueHistory history3 = new IssueHistory();
		history3.setIssueID(story.getIssueID());
		history3.setType(IIssueHistory.OTHER_TYPE);
		history3.setFieldName(IssueHistory.HANDLER_FIELD_NAME);
		history3.setOldValue("1");
		history3.setNewValue("2");
		history3.setModifyDate(modifyDate);
		IssueHistory history4 = new IssueHistory();
		history4.setIssueID(story.getIssueID());
		history4.setType(IIssueHistory.OTHER_TYPE);
		history4.setFieldName("resolution");
		history4.setOldValue("1");
		history4.setNewValue("2");
		history4.setModifyDate(modifyDate);
		List<IIssueHistory> histories = new ArrayList<IIssueHistory>();
		histories.add(history1);
		histories.add(history2);
		histories.add(history3);
		histories.add(history4);
		assertEquals(0, JSONEncoder.toHistoryJSONArray(histories, story.getCategory()).length());
	}
}
