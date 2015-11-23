package ntut.csie.ezScrum.restful.export;

import static org.junit.Assert.assertEquals;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.net.httpserver.HttpServer;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.issue.core.IIssueTag;
import ntut.csie.ezScrum.issue.core.ITSEnum;
import ntut.csie.ezScrum.iteration.core.IReleasePlanDesc;
import ntut.csie.ezScrum.iteration.core.IScrumIssue;
import ntut.csie.ezScrum.iteration.core.ISprintPlanDesc;
import ntut.csie.ezScrum.pic.core.ScrumRole;
import ntut.csie.ezScrum.restful.export.jsonEnum.AccountJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.AttachFileJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.ExportJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.HistoryJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.ProjectJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.ReleaseJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.RetrospectiveJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.ScrumRoleJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.SprintJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.StoryJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.TagJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.TaskJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.UnplanJSONEnum;
import ntut.csie.ezScrum.restful.export.support.FileEncoder;
import ntut.csie.ezScrum.restful.export.support.JSONEncoder;
import ntut.csie.ezScrum.restful.export.support.ResourceFinder;
import ntut.csie.ezScrum.test.CreateData.AddStoryToSprint;
import ntut.csie.ezScrum.test.CreateData.AddTaskToStory;
import ntut.csie.ezScrum.test.CreateData.CopyProject;
import ntut.csie.ezScrum.test.CreateData.CreateAccount;
import ntut.csie.ezScrum.test.CreateData.CreateProductBacklog;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateRelease;
import ntut.csie.ezScrum.test.CreateData.CreateRetrospective;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.test.CreateData.CreateUnplannedItem;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.ezScrum.web.helper.AccountHelper;
import ntut.csie.ezScrum.web.helper.ProductBacklogHelper;
import ntut.csie.ezScrum.web.helper.SprintBacklogHelper;
import ntut.csie.ezScrum.web.helper.UnplannedItemHelper;
import ntut.csie.ezScrum.web.logic.ProductBacklogLogic;
import ntut.csie.ezScrum.web.logic.SprintBacklogLogic;
import ntut.csie.ezScrum.web.mapper.ProductBacklogMapper;
import ntut.csie.ezScrum.web.mapper.ProjectMapper;
import ntut.csie.ezScrum.web.mapper.ScrumRoleMapper;
import ntut.csie.ezScrum.web.mapper.SprintBacklogMapper;
import ntut.csie.jcis.account.core.IAccount;
import ntut.csie.jcis.resource.core.IProject;

public class IntegratedRESTfulApiTest extends JerseyTest {
	private ezScrumInfoConfig mConfig = new ezScrumInfoConfig();
	private CreateProject mCP;
	private CreateRelease mCR;
	private CreateSprint mCS;
	private CreateUnplannedItem mCU;
	private CreateRetrospective mCRE;
	private AddStoryToSprint mASTS;
	private AddTaskToStory mATTS;
	private CreateAccount mCA;
	
	private ScrumRole mProductOwner;
	private ScrumRole mScrumMaster;
	private ScrumRole mScrumTeam;
	private ScrumRole mStakeholder;
	private ScrumRole mGuest;
	
	// project tags
	private final static String PROJECT_TAG1 = "Data Migration";
	private final static String PROJECT_TAG2 = "Thesis";
	private final static String PROJECT_TAG3 = "Bug fix";
	
	// attach files
	private final String TEXT_FILE_TYPE = "application/octet-stream";
	private final String FILE_PATH_STORY1 = "./TestData/story1.txt";
	private final String FILE_PATH_STORY2 = "./TestData/story2.txt";
	private final String FILE_PATH_TASK1 = "./TestData/task1.txt";
	private final String FILE_PATH_TASK2 = "./TestData/task2.txt";
	private final String FILE_PATH_TASK3 = "./TestData/task3.txt";
	private final String FILE_PATH_TASK4 = "./TestData/task4.txt";
	private final String SOURCE_CONTENT_STORY1 = "TEST_TO_BASE64_BINARY_STORY1";
	private final String SOURCE_CONTENT_STORY2 = "TEST_TO_BASE64_BINARY_STORY2";
	private final String SOURCE_CONTENT_TASK1 = "TEST_TO_BASE64_BINARY_TASK1";
	private final String SOURCE_CONTENT_TASK2 = "TEST_TO_BASE64_BINARY_TASK2";
	private final String SOURCE_CONTENT_TASK3 = "TEST_TO_BASE64_BINARY_TASK3";
	private final String SOURCE_CONTENT_TASK4 = "TEST_TO_BASE64_BINARY_TASK4";
	private File mSourceFileOfStory1;
	private File mSourceFileOfStory2;
	private File mSourceFileOfTask1;
	private File mSourceFileOfTask2;
	private File mSourceFileOfTask3;
	private File mSourceFileOfTask4;
	
	
	private Client mClient;
	private HttpServer mHttpServer;
	private ResourceConfig mResourceConfig;
	private static String BASE_URL = "http://localhost:8080/ezScrum/resource/";
	private URI mBaseUri = URI.create(BASE_URL);

	@Override
	protected Application configure() {
		mResourceConfig = new ResourceConfig(ProjectRESTfulApi.class, SprintRESTfulApi.class, StoryRESTfulApi.class,
                TaskRESTfulApi.class, DroppedStoryRESTfulApi.class, DroppedTaskRESTfulApi.class,
                ReleaseRESTfulApi.class, AccountRESTfulApi.class, RetrospectiveRESTfulApi.class, UnplanRESTfulApi.class, IntegratedRESTfulApi.class);
		return mResourceConfig;
	}

	@Before
	public void setUp() throws Exception {
		// 初始化 SQL
		InitialSQL ini = new InitialSQL(mConfig);
		ini.exe();

		// Create Project
		mCP = new CreateProject(1);
		mCP.exeCreate();
		
		// Create Release
		mCR = new CreateRelease(2, mCP);
		mCR.exe();

		// Create Sprint
		mCS = new CreateSprint(1, mCP);
		mCS.exe();

		// Add Story to Sprint
		mASTS = new AddStoryToSprint(2, 8, mCS, mCP, CreateProductBacklog.TYPE_ESTIMATION);
		mASTS.exe();

		// Add Task to Story
		mATTS = new AddTaskToStory(2, 13, mASTS, mCP);
		mATTS.exe();
		
		// Create Retrospective
		mCRE = new CreateRetrospective(2, 2, mCP, mCS);
		mCRE.exe();

		// Create Unplan
		mCU = new CreateUnplannedItem(2, mCP, mCS);
		mCU.exe();
		
		// Create Account
		mCA = new CreateAccount(5);
		mCA.exe();
		
		// set up dropped story and dropped task
		setUpDroppedStoryAndDroppedTask();
		
		// set up scrum roles
		setUpScrumRoles();
		
		// set up project roles
		setUpProjectRoles();
		
		// set up project tags
		setUpProjectTags();
		
		// set up stories tag
		setUpStoriesTag();
		
		// set up attach files
		setUpAttachFiles();
		
		// set up partners
		setUpPartners();
				
		// Start Server
		mHttpServer = JdkHttpServerFactory.createHttpServer(mBaseUri, mResourceConfig, true);

		// Create Client
		mClient = ClientBuilder.newClient();
	}

	@After
	public void tearDown() {
		// 初始化 SQL
		InitialSQL ini = new InitialSQL(mConfig);
		ini.exe();

		// 刪除測試檔案
		CopyProject copyProject = new CopyProject(mCP);
		copyProject.exeDelete_Project();
		
		// Clear source file
		mSourceFileOfStory1.delete();
		mSourceFileOfStory2.delete();
		mSourceFileOfTask1.delete();
		mSourceFileOfTask2.delete();
		mSourceFileOfTask3.delete();
		mSourceFileOfTask4.delete();

		// Stop Server
		mHttpServer.stop(0);

		// ============= release ==============
		ini = null;
		copyProject = null;
		mCP = null;
		mCS = null;
		mCU = null;
		mCR = null;
		mCRE = null;
		mASTS = null;
		mATTS = null;
		mHttpServer = null;
		mClient = null;
	}
	
	private void setUpPartners() {
		IProject project = mCP.getProjectList().get(0);
		IIssue task1 = mATTS.getTaskList().get(0);
		IIssue task2 = mATTS.getTaskList().get(1);
		IIssue task3 = mATTS.getTaskList().get(2);
		IIssue task4 = mATTS.getTaskList().get(3);

		IAccount account1 = mCA.getAccountList().get(0);
		IAccount account2 = mCA.getAccountList().get(1);
		IAccount account3 = mCA.getAccountList().get(2);
		
		List<IIssue> unplans = mCU.getIssueList();
		IIssue unplan1 = unplans.get(0);
		IIssue unplan2 = unplans.get(1);
		
		ResourceFinder resourceFinder = new ResourceFinder();
		resourceFinder.findProject(project.getName());
		ISprintPlanDesc sprint = resourceFinder.findSprint(Long.parseLong(mCS.getSprintIDList().get(0)));
		
		SprintBacklogLogic sprintBacklogLogic = new SprintBacklogLogic(project, mConfig.getUserSession(), sprint.getID());
		// assign account1 as handler for task1
		sprintBacklogLogic.editTask(task1.getIssueID(), task1.getSummary(), task1.getEstimated(), task1.getRemains(), account1.getID(), "", task1.getActualHour(), task1.getNotes(), null);
		// assign account2 as handler for task2
		sprintBacklogLogic.editTask(task2.getIssueID(), task2.getSummary(), task2.getEstimated(), task2.getRemains(), account2.getID(), "", task2.getActualHour(), task2.getNotes(), null);
		// assign account1 as handler for task3, and assign account2 as partner for task3
		sprintBacklogLogic.editTask(task3.getIssueID(), task3.getSummary(), task3.getEstimated(), task3.getRemains(), account1.getID(), account2.getID(), task3.getActualHour(), task3.getNotes(), null);
		// assign account1 as handler for task4, and assign account2, account3 as partners for task4
		sprintBacklogLogic.editTask(task4.getIssueID(), task4.getSummary(), task4.getEstimated(), task4.getRemains(), account1.getID(), account2.getID() + ";" + account3.getID(), task4.getActualHour(), task4.getNotes(), null);
	
		UnplannedItemHelper unplannedItemHelper = new UnplannedItemHelper(project, mConfig.getUserSession());
		// assign account1 as handler for unplan1
		unplannedItemHelper.modifyUnplannedItemIssue(unplan1.getIssueID(), unplan1.getSummary(), account1.getID(), ITSEnum.ASSIGNED, "", unplan1.getEstimated(), unplan1.getActualHour(), unplan1.getNotes(), sprint.getID(), null);
		// assign account1 as handler, and assign account2, account3 as partners for unplan2
		unplannedItemHelper.modifyUnplannedItemIssue(unplan2.getIssueID(), unplan2.getSummary(), account1.getID(), ITSEnum.ASSIGNED, account2.getID() + ";" + account3.getID(), unplan2.getEstimated(), unplan2.getActualHour(), unplan1.getNotes(), sprint.getID(), null);
	}
	
	private void setUpAttachFiles() throws IOException {
		IProject project = mCP.getProjectList().get(0);
		IIssue story1 = mASTS.getIssueList().get(0);
		IIssue story2 = mASTS.getIssueList().get(1);
		IIssue task1 = mATTS.getTaskList().get(0);
		IIssue task2 = mATTS.getTaskList().get(1);
		IIssue task3 = mATTS.getTaskList().get(2);
		IIssue task4 = mATTS.getTaskList().get(3);
		SprintBacklogMapper sprintBacklogMapper = new SprintBacklogMapper(project, null);
		mSourceFileOfStory1 = new File(FILE_PATH_STORY1);
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(FILE_PATH_STORY1));
			writer.write(SOURCE_CONTENT_STORY1);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}
		sprintBacklogMapper.addAttachFile(story1.getIssueID(), FILE_PATH_STORY1);
		
		mSourceFileOfStory2 = new File(FILE_PATH_STORY2);
		try {
			writer = new BufferedWriter(new FileWriter(FILE_PATH_STORY2));
			writer.write(SOURCE_CONTENT_STORY2);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}
		sprintBacklogMapper.addAttachFile(story2.getIssueID(), FILE_PATH_STORY2);
		
		mSourceFileOfTask1 = new File(FILE_PATH_TASK1);
		try {
			writer = new BufferedWriter(new FileWriter(FILE_PATH_TASK1));
			writer.write(SOURCE_CONTENT_TASK1);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}
		sprintBacklogMapper.addAttachFile(task1.getIssueID(), FILE_PATH_TASK1);
		
		mSourceFileOfTask2 = new File(FILE_PATH_TASK2);
		try {
			writer = new BufferedWriter(new FileWriter(FILE_PATH_TASK2));
			writer.write(SOURCE_CONTENT_TASK2);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}
		sprintBacklogMapper.addAttachFile(task2.getIssueID(), FILE_PATH_TASK2);
		
		mSourceFileOfTask3 = new File(FILE_PATH_TASK3);
		try {
			writer = new BufferedWriter(new FileWriter(FILE_PATH_TASK3));
			writer.write(SOURCE_CONTENT_TASK3);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}
		sprintBacklogMapper.addAttachFile(task3.getIssueID(), FILE_PATH_TASK3);
		
		mSourceFileOfTask4 = new File(FILE_PATH_TASK4);
		try {
			writer = new BufferedWriter(new FileWriter(FILE_PATH_TASK4));
			writer.write(SOURCE_CONTENT_TASK4);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}
		sprintBacklogMapper.addAttachFile(task4.getIssueID(), FILE_PATH_TASK4);
	}
	
	private void setUpStoriesTag() {
		IProject project = mCP.getProjectList().get(0);
		IIssue story1 = mASTS.getIssueList().get(0);
		IIssue story2 = mASTS.getIssueList().get(1);
		ProductBacklogHelper productBacklogHelper = new ProductBacklogHelper(null, project);
		// set tag1 to story1
		IIssueTag tag1 = productBacklogHelper.getTagByName(PROJECT_TAG1);
		productBacklogHelper.addStoryTag(String.valueOf(story1.getIssueID()), String.valueOf(tag1.getTagId()));
		// set tag2 to story2
		IIssueTag tag2 = productBacklogHelper.getTagByName(PROJECT_TAG2);
		productBacklogHelper.addStoryTag(String.valueOf(story2.getIssueID()), String.valueOf(tag2.getTagId()));
	}
	
	private void setUpProjectTags() {
		String tagName1 = PROJECT_TAG1;
		String tagName2 = PROJECT_TAG2;
		String tagName3 = PROJECT_TAG3;
		IProject project = mCP.getProjectList().get(0);
		ProductBacklogHelper productBacklogHelper = new ProductBacklogHelper(null, project);
		productBacklogHelper.addNewTag(tagName1);
		productBacklogHelper.addNewTag(tagName2);
		productBacklogHelper.addNewTag(tagName3);
	}
	
	private void setUpProjectRoles() throws Exception {
		IProject project = mCP.getProjectList().get(0);
		IAccount account1 = mCA.getAccountList().get(0);
		IAccount account2 = mCA.getAccountList().get(1);
		IAccount account3 = mCA.getAccountList().get(2);
		
		AccountHelper accountHelper = new AccountHelper();
		accountHelper.assignRole_add(mConfig.getUserSession(), account1.getID(),
				project.getName(), ScrumRoleJSONEnum.SCRUM_MASTER);
		accountHelper.assignRole_add(mConfig.getUserSession(), account2.getID(),
				project.getName(), ScrumRoleJSONEnum.SCRUM_TEAM);
		accountHelper.assignRole_add(mConfig.getUserSession(), account3.getID(),
				project.getName(), ScrumRoleJSONEnum.SCRUM_TEAM);
	}
	
	private void setUpDroppedStoryAndDroppedTask() throws InterruptedException {
		IProject project = mCP.getProjectList().get(0);
		IIssue story1 = mASTS.getIssueList().get(0);
		IIssue story2 = mASTS.getIssueList().get(1);
		IIssue task1 = mATTS.getTaskList().get(0);
		IIssue task3 = mATTS.getTaskList().get(2);
		ResourceFinder resourceFinder = new ResourceFinder();
		resourceFinder.findProject(project.getName());
		
		// Drop Story1
		ProductBacklogLogic productBacklogLogic = new ProductBacklogLogic(null, project);
		
		// It's need some delay for manipulating file IO (add story to sprint)
		Thread.sleep(1000);
		// Remove story1 from Sprint
		productBacklogLogic.removeStoryFromSprint(story1.getIssueID());
		
		// Drop task1, task3 from story
		SprintBacklogHelper sprintBacklogHelper = new SprintBacklogHelper(project, null);
		sprintBacklogHelper.removeTask(task1.getIssueID(), story1.getIssueID());
		sprintBacklogHelper.removeTask(task3.getIssueID(), story2.getIssueID());
	}
	
	private void setUpScrumRoles() {
		IProject project = mCP.getProjectList().get(0);
		ScrumRoleMapper scrumRoleMapper = new ScrumRoleMapper();
		mProductOwner = new ScrumRole(project.getName(), ScrumRoleJSONEnum.PRODUCT_OWNER);
		mProductOwner.setAccessProductBacklog(true);
		mProductOwner.setAccessSprintPlan(true);
		mProductOwner.setAccessTaskBoard(false);
		mProductOwner.setAccessSprintBacklog(true);
		mProductOwner.setAccessReleasePlan(true);
		mProductOwner.setAccessRetrospective(false);
		mProductOwner.setAccessUnplannedItem(false);
		mProductOwner.setReadReport(true);
		mProductOwner.setEditProject(true);
		scrumRoleMapper.update(mProductOwner);

		mScrumMaster = new ScrumRole(project.getName(), ScrumRoleJSONEnum.SCRUM_MASTER);
		mScrumMaster.setAccessProductBacklog(true);
		mScrumMaster.setAccessSprintPlan(true);
		mScrumMaster.setAccessTaskBoard(true);
		mScrumMaster.setAccessSprintBacklog(true);
		mScrumMaster.setAccessReleasePlan(true);
		mScrumMaster.setAccessRetrospective(true);
		mScrumMaster.setAccessUnplannedItem(true);
		mScrumMaster.setReadReport(true);
		mScrumMaster.setEditProject(false);
		scrumRoleMapper.update(mScrumMaster);

		mScrumTeam = new ScrumRole(project.getName(), ScrumRoleJSONEnum.SCRUM_TEAM);
		mScrumTeam.setAccessProductBacklog(false);
		mScrumTeam.setAccessSprintPlan(true);
		mScrumTeam.setAccessTaskBoard(true);
		mScrumTeam.setAccessSprintBacklog(true);
		mScrumTeam.setAccessReleasePlan(true);
		mScrumTeam.setAccessRetrospective(true);
		mScrumTeam.setAccessUnplannedItem(true);
		mScrumTeam.setReadReport(true);
		mScrumTeam.setEditProject(false);
		scrumRoleMapper.update(mScrumTeam);

		mStakeholder = new ScrumRole(project.getName(), ScrumRoleJSONEnum.STAKEHOLDER);
		mStakeholder.setAccessProductBacklog(false);
		mStakeholder.setAccessSprintPlan(false);
		mStakeholder.setAccessTaskBoard(false);
		mStakeholder.setAccessSprintBacklog(false);
		mStakeholder.setAccessReleasePlan(true);
		mStakeholder.setAccessRetrospective(false);
		mStakeholder.setAccessUnplannedItem(false);
		mStakeholder.setReadReport(true);
		mStakeholder.setEditProject(false);
		scrumRoleMapper.update(mStakeholder);

		mGuest = new ScrumRole(project.getName(), ScrumRoleJSONEnum.GUEST);
		mGuest.setAccessProductBacklog(false);
		mGuest.setAccessSprintPlan(false);
		mGuest.setAccessTaskBoard(false);
		mGuest.setAccessSprintBacklog(false);
		mGuest.setAccessReleasePlan(true);
		mGuest.setAccessRetrospective(false);
		mGuest.setAccessUnplannedItem(false);
		mGuest.setReadReport(true);
		mGuest.setEditProject(false);
		scrumRoleMapper.update(mGuest);
	}
	
	@Test
	public void testGetExportedJSON() throws InterruptedException, JSONException {
		IProject project = mCP.getProjectList().get(0);
		ProductBacklogMapper productBacklogMapper = new ProductBacklogMapper(project, null);
		IIssue story1 = mASTS.getIssueList().get(0);
		story1 = productBacklogMapper.getIssue(story1.getIssueID());
		IIssue story2 = mASTS.getIssueList().get(1);
		story2 = productBacklogMapper.getIssue(story2.getIssueID());
		IIssue task1 = mATTS.getTaskList().get(0);
		task1 = productBacklogMapper.getIssue(task1.getIssueID());
		IIssue task2 = mATTS.getTaskList().get(1);
		task2 = productBacklogMapper.getIssue(task2.getIssueID());
		IIssue task3 = mATTS.getTaskList().get(2);
		task3 = productBacklogMapper.getIssue(task3.getIssueID());
		IIssue task4 = mATTS.getTaskList().get(3);
		task4 = productBacklogMapper.getIssue(task4.getIssueID());
		IIssue unplan1 = mCU.getIssueList().get(0);
		unplan1 = productBacklogMapper.getIssue(unplan1.getIssueID());
		IIssue unplan2 = mCU.getIssueList().get(1);
		unplan2 = productBacklogMapper.getIssue(unplan2.getIssueID());
		ResourceFinder resourceFinder = new ResourceFinder();
		resourceFinder.findProject(project.getName());
		ISprintPlanDesc sprint = resourceFinder.findSprint(Long.parseLong(mCS.getSprintIDList().get(0)));
		
		IAccount account1 = mCA.getAccountList().get(0);
		IAccount account2 = mCA.getAccountList().get(1);
		IAccount account3 = mCA.getAccountList().get(2);
		IAccount account4 = mCA.getAccountList().get(3);
		IAccount account5 = mCA.getAccountList().get(4);
		
		Response response = mClient.target(mBaseUri)
		        .path("export/projects")
		        .request()
		        .get();
		
		JSONObject jsonArrayResponse = new JSONObject(response.readEntity(String.class));
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		JSONArray projectJSONArray = jsonArrayResponse.getJSONArray(ExportJSONEnum.PROJECTS);
		assertEquals(1, projectJSONArray.length());
		
		// Assert Account data
		JSONArray accountJSONArray = jsonArrayResponse.getJSONArray(ExportJSONEnum.ACCOUNTS);
		assertEquals(6, accountJSONArray.length());
		assertEquals(account5.getID(), accountJSONArray.getJSONObject(0).getString(AccountJSONEnum.USERNAME));
		assertEquals("admin", accountJSONArray.getJSONObject(1).getString(AccountJSONEnum.USERNAME));
		assertEquals(account3.getID(), accountJSONArray.getJSONObject(2).getString(AccountJSONEnum.USERNAME));
		assertEquals(account4.getID(), accountJSONArray.getJSONObject(3).getString(AccountJSONEnum.USERNAME));
		assertEquals(account1.getID(), accountJSONArray.getJSONObject(4).getString(AccountJSONEnum.USERNAME));
		assertEquals(account2.getID(), accountJSONArray.getJSONObject(5).getString(AccountJSONEnum.USERNAME));
		// end
		
		// Assert project data
		JSONObject projectJSON = projectJSONArray.getJSONObject(0);
		assertEquals(project.getName(), projectJSON.getString(ProjectJSONEnum.NAME));
		assertEquals(project.getProjectDesc().getDisplayName(), projectJSON.getString(ProjectJSONEnum.DISPLAY_NAME));
		assertEquals(project.getProjectDesc().getComment(), projectJSON.getString(ProjectJSONEnum.COMMENT));
		assertEquals(project.getProjectDesc().getProjectManager(), projectJSON.getString(ProjectJSONEnum.PRODUCT_OWNER));
		assertEquals(Long.parseLong(project.getProjectDesc().getAttachFileSize()), projectJSON.getLong(ProjectJSONEnum.ATTATCH_MAX_SIZE));
		JSONArray sprintJSONArray = projectJSON.getJSONArray(ProjectJSONEnum.SPRINTS);
		assertEquals(1, sprintJSONArray.length());
		// end
		
		// Assert scrum roles in project
		JSONObject scrumRolesJSON = projectJSON.getJSONObject(ProjectJSONEnum.SCRUM_ROLES);
		// Assert Product Owner
		JSONObject productOwnerJSON = scrumRolesJSON.getJSONObject(ScrumRoleJSONEnum.PRODUCT_OWNER);
		assertEquals(mProductOwner.getAccessProductBacklog(),
				productOwnerJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_PRODUCT_BACKLOG));
		assertEquals(mProductOwner.getAccessSprintPlan(), productOwnerJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_SPRINT_PLAN));
		assertEquals(mProductOwner.getAccessTaskBoard(), productOwnerJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_TASKBOARD));
		assertEquals(mProductOwner.getAccessSprintBacklog(),
				productOwnerJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_SPRINT_BACKLOG));
		assertEquals(mProductOwner.getAccessReleasePlan(),
				productOwnerJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_RELEASE_PLAN));
		assertEquals(mProductOwner.getAccessRetrospective(),
				productOwnerJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_RETROSPECTIVE));
		assertEquals(mProductOwner.getAccessUnplannedItem(),
				productOwnerJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_UNPLANNED));
		assertEquals(mProductOwner.getReadReport(), productOwnerJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_REPORT));
		assertEquals(mProductOwner.getEditProject(), productOwnerJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_EDIT_PROJECT));

		// Assert Scrum Master
		JSONObject scrumMasterJSON = scrumRolesJSON.getJSONObject(ScrumRoleJSONEnum.SCRUM_MASTER);
		assertEquals(mScrumMaster.getAccessProductBacklog(),
				scrumMasterJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_PRODUCT_BACKLOG));
		assertEquals(mScrumMaster.getAccessSprintPlan(), scrumMasterJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_SPRINT_PLAN));
		assertEquals(mScrumMaster.getAccessTaskBoard(), scrumMasterJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_TASKBOARD));
		assertEquals(mScrumMaster.getAccessSprintBacklog(),
				scrumMasterJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_SPRINT_BACKLOG));
		assertEquals(mScrumMaster.getAccessReleasePlan(), scrumMasterJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_RELEASE_PLAN));
		assertEquals(mScrumMaster.getAccessRetrospective(),
				scrumMasterJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_RETROSPECTIVE));
		assertEquals(mScrumMaster.getAccessUnplannedItem(), scrumMasterJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_UNPLANNED));
		assertEquals(mScrumMaster.getReadReport(), scrumMasterJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_REPORT));
		assertEquals(mScrumMaster.getEditProject(), scrumMasterJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_EDIT_PROJECT));

		// Assert Scrum Team
		JSONObject scrumTeamJSON = scrumRolesJSON.getJSONObject(ScrumRoleJSONEnum.SCRUM_TEAM);
		assertEquals(mScrumTeam.getAccessProductBacklog(),
				scrumTeamJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_PRODUCT_BACKLOG));
		assertEquals(mScrumTeam.getAccessSprintPlan(), scrumTeamJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_SPRINT_PLAN));
		assertEquals(mScrumTeam.getAccessTaskBoard(), scrumTeamJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_TASKBOARD));
		assertEquals(mScrumTeam.getAccessSprintBacklog(), scrumTeamJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_SPRINT_BACKLOG));
		assertEquals(mScrumTeam.getAccessReleasePlan(), scrumTeamJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_RELEASE_PLAN));
		assertEquals(mScrumTeam.getAccessRetrospective(), scrumTeamJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_RETROSPECTIVE));
		assertEquals(mScrumTeam.getAccessUnplannedItem(), scrumTeamJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_UNPLANNED));
		assertEquals(mScrumTeam.getReadReport(), scrumTeamJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_REPORT));
		assertEquals(mScrumTeam.getEditProject(), scrumTeamJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_EDIT_PROJECT));

		// Assert Stakeholder
		JSONObject stakeholderJSON = scrumRolesJSON.getJSONObject(ScrumRoleJSONEnum.STAKEHOLDER);
		assertEquals(mStakeholder.getAccessProductBacklog(),
				stakeholderJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_PRODUCT_BACKLOG));
		assertEquals(mStakeholder.getAccessSprintPlan(), stakeholderJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_SPRINT_PLAN));
		assertEquals(mStakeholder.getAccessTaskBoard(), stakeholderJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_TASKBOARD));
		assertEquals(mStakeholder.getAccessSprintBacklog(),
				stakeholderJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_SPRINT_BACKLOG));
		assertEquals(mStakeholder.getAccessReleasePlan(), stakeholderJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_RELEASE_PLAN));
		assertEquals(mStakeholder.getAccessRetrospective(),
				stakeholderJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_RETROSPECTIVE));
		assertEquals(mStakeholder.getAccessUnplannedItem(), stakeholderJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_UNPLANNED));
		assertEquals(mStakeholder.getReadReport(), stakeholderJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_REPORT));
		assertEquals(mStakeholder.getEditProject(), stakeholderJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_EDIT_PROJECT));

		// Assert Guest
		JSONObject guestJSON = scrumRolesJSON.getJSONObject(ScrumRoleJSONEnum.GUEST);
		assertEquals(mGuest.getAccessProductBacklog(), guestJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_PRODUCT_BACKLOG));
		assertEquals(mGuest.getAccessSprintPlan(), guestJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_SPRINT_PLAN));
		assertEquals(mGuest.getAccessTaskBoard(), guestJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_TASKBOARD));
		assertEquals(mGuest.getAccessSprintBacklog(), guestJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_SPRINT_BACKLOG));
		assertEquals(mGuest.getAccessReleasePlan(), guestJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_RELEASE_PLAN));
		assertEquals(mGuest.getAccessRetrospective(), guestJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_RETROSPECTIVE));
		assertEquals(mGuest.getAccessUnplannedItem(), guestJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_UNPLANNED));
		assertEquals(mGuest.getReadReport(), guestJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_REPORT));
		assertEquals(mGuest.getEditProject(), guestJSON.getBoolean(ScrumRoleJSONEnum.ACCESS_EDIT_PROJECT));
		// end
		
		// Assert project roles in project
		JSONArray projectRoleJSONArray = projectJSON.getJSONArray(ProjectJSONEnum.PROJECT_ROLES);
		ProjectMapper projectMapper = new ProjectMapper();
		List<IAccount> projectRoles = projectMapper.getProjectMemberList(null, project);
		assertEquals(3, projectRoleJSONArray.length());
		assertEquals(JSONEncoder.toProjectRoleJSONArray(project.getName(), projectRoles).toString(), projectRoleJSONArray.toString());
		// end
		
		// Assert project's tags in project
		JSONArray tagJSONArrayInProject = projectJSON.getJSONArray(ProjectJSONEnum.TAGS);
		assertEquals(3, tagJSONArrayInProject.length());
		assertEquals(PROJECT_TAG1, tagJSONArrayInProject.getJSONObject(0).getString(TagJSONEnum.NAME));
		assertEquals(PROJECT_TAG2, tagJSONArrayInProject.getJSONObject(1).getString(TagJSONEnum.NAME));
		assertEquals(PROJECT_TAG3, tagJSONArrayInProject.getJSONObject(2).getString(TagJSONEnum.NAME));
		// end
		
		// Assert releases in project
		IReleasePlanDesc release1 = mCR.getReleaseList().get(0);
		IReleasePlanDesc release2 = mCR.getReleaseList().get(1);
		JSONArray releaseJSONArrayInProject = projectJSON.getJSONArray(ProjectJSONEnum.RELEASES);
		JSONObject releaseJson1 = releaseJSONArrayInProject.getJSONObject(0);
		assertEquals(release1.getName(), releaseJson1.getString(ReleaseJSONEnum.NAME));
		assertEquals(release1.getDescription(), releaseJson1.getString(ReleaseJSONEnum.DESCRIPTION));
		assertEquals(release1.getStartDate(), releaseJson1.getString(ReleaseJSONEnum.START_DATE));
		assertEquals(release1.getEndDate(), releaseJson1.getString(ReleaseJSONEnum.DUE_DATE));
		
		JSONObject releaseJson2 = releaseJSONArrayInProject.getJSONObject(1);
		assertEquals(release2.getName(), releaseJson2.getString(ReleaseJSONEnum.NAME));
		assertEquals(release2.getDescription(), releaseJson2.getString(ReleaseJSONEnum.DESCRIPTION));
		assertEquals(release2.getStartDate(), releaseJson2.getString(ReleaseJSONEnum.START_DATE));
		assertEquals(release2.getEndDate(), releaseJson2.getString(ReleaseJSONEnum.DUE_DATE));
		// end
		
		// Assert sprint data in project
		JSONObject sprintJSON = sprintJSONArray.getJSONObject(0);
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
		JSONArray storyJSONArray = sprintJSON.getJSONArray(SprintJSONEnum.STORIES);
		assertEquals(1, storyJSONArray.length());
		// end
		
		// Assert story2 data in sprint
		JSONObject story2JSON = storyJSONArray.getJSONObject(0);
		assertEquals(story2.getIssueID(), story2JSON.getLong(StoryJSONEnum.ID));
		assertEquals(story2.getSummary(), story2JSON.getString(StoryJSONEnum.NAME));
		assertEquals(story2.getStatus(), story2JSON.getString(StoryJSONEnum.STATUS));
		assertEquals(Integer.parseInt(story2.getEstimated()), story2JSON.getInt(StoryJSONEnum.ESTIMATE));
		assertEquals(Integer.parseInt(story2.getImportance()), story2JSON.getInt(StoryJSONEnum.IMPORTANCE));
		assertEquals(Integer.parseInt(story2.getValue()), story2JSON.getInt(StoryJSONEnum.VALUE));
		assertEquals(story2.getNotes(), story2JSON.getString(StoryJSONEnum.NOTES));
		assertEquals(story2.getHowToDemo(), story2JSON.getString(StoryJSONEnum.HOW_TO_DEMO));
		JSONArray taskInStory2JSONArray = story2JSON.getJSONArray(StoryJSONEnum.TASKS);
		assertEquals(1, taskInStory2JSONArray.length());
		JSONArray historyJSONArrayInStory2 = story2JSON.getJSONArray(StoryJSONEnum.HISTORIES);
		@SuppressWarnings("deprecation")
		JSONArray expectedHistoryJSONArrayInStory2 = JSONEncoder.toHistoryJSONArray(story2.getIssueHistories(), story2.getCategory());
		assertEquals(expectedHistoryJSONArrayInStory2.length(), historyJSONArrayInStory2.length());
		for (int i = 0; i < expectedHistoryJSONArrayInStory2.length(); i++) {
			assertEquals(expectedHistoryJSONArrayInStory2.getJSONObject(i).getInt(HistoryJSONEnum.HISTORY_TYPE),
					historyJSONArrayInStory2.getJSONObject(i).getInt(HistoryJSONEnum.HISTORY_TYPE));
			assertEquals(expectedHistoryJSONArrayInStory2.getJSONObject(i).getString(HistoryJSONEnum.OLD_VALUE),
					historyJSONArrayInStory2.getJSONObject(i).getString(HistoryJSONEnum.OLD_VALUE));
			assertEquals(expectedHistoryJSONArrayInStory2.getJSONObject(i).getString(HistoryJSONEnum.NEW_VALUE),
					historyJSONArrayInStory2.getJSONObject(i).getString(HistoryJSONEnum.NEW_VALUE));
			assertEquals(expectedHistoryJSONArrayInStory2.getJSONObject(i).getLong(HistoryJSONEnum.CREATE_TIME),
					historyJSONArrayInStory2.getJSONObject(i).getLong(HistoryJSONEnum.CREATE_TIME));
		}
		// end
		
		// Assert tags in story2
		JSONArray tagJSONArrayInStory2 = story2JSON.getJSONArray(StoryJSONEnum.TAGS);
		assertEquals(1, tagJSONArrayInStory2.length());
		assertEquals(PROJECT_TAG2, tagJSONArrayInStory2.getJSONObject(0).getString(TagJSONEnum.NAME));
		// end
		
		// Assert attach files in story2
		JSONArray attachFileJSONArrayInStory2 = story2JSON.getJSONArray(StoryJSONEnum.ATTACH_FILES);
		assertEquals(1, attachFileJSONArrayInStory2.length());
		assertEquals("story2.txt", attachFileJSONArrayInStory2.getJSONObject(0).getString(AttachFileJSONEnum.NAME));
		assertEquals(TEXT_FILE_TYPE, attachFileJSONArrayInStory2.getJSONObject(0).getString(AttachFileJSONEnum.CONTENT_TYPE));
		assertEquals(FileEncoder.toBase64BinaryString(mSourceFileOfStory2), attachFileJSONArrayInStory2.getJSONObject(0).getString(AttachFileJSONEnum.BINARY));
		// end
		
		// Assert task4 data in story2
		JSONObject task4JSON = taskInStory2JSONArray.getJSONObject(0);
		assertEquals(task4.getSummary(), task4JSON.getString(TaskJSONEnum.NAME));
		assertEquals(task4.getAssignto(), task4JSON.getString(TaskJSONEnum.HANDLER));
		assertEquals(Integer.parseInt(task4.getEstimated()), task4JSON.getInt(TaskJSONEnum.ESTIMATE));
		assertEquals(Integer.parseInt(task4.getRemains()), task4JSON.getInt(TaskJSONEnum.REMAIN));
		assertEquals(Integer.parseInt(task4.getActualHour()), task4JSON.getInt(TaskJSONEnum.ACTUAL));
		assertEquals(task4.getNotes(), task4JSON.getString(TaskJSONEnum.NOTES));
		assertEquals(task4.getStatus(), task4JSON.getString(TaskJSONEnum.STATUS));
		JSONArray historyJSONArrayInTask4 = task4JSON.getJSONArray(TaskJSONEnum.HISTORIES);
		@SuppressWarnings("deprecation")
		JSONArray expectedHistoryJSONArrayInTask4 = JSONEncoder.toHistoryJSONArray(task4.getIssueHistories(), task4.getCategory());
		assertEquals(expectedHistoryJSONArrayInTask4.length(), historyJSONArrayInTask4.length());
		for (int i = 0; i < expectedHistoryJSONArrayInTask4.length(); i++) {
			assertEquals(expectedHistoryJSONArrayInTask4.getJSONObject(i).getInt(HistoryJSONEnum.HISTORY_TYPE),
					historyJSONArrayInTask4.getJSONObject(i).getInt(HistoryJSONEnum.HISTORY_TYPE));
			assertEquals(expectedHistoryJSONArrayInTask4.getJSONObject(i).getString(HistoryJSONEnum.OLD_VALUE),
					historyJSONArrayInTask4.getJSONObject(i).getString(HistoryJSONEnum.OLD_VALUE));
			assertEquals(expectedHistoryJSONArrayInTask4.getJSONObject(i).getString(HistoryJSONEnum.NEW_VALUE),
					historyJSONArrayInTask4.getJSONObject(i).getString(HistoryJSONEnum.NEW_VALUE));
			assertEquals(expectedHistoryJSONArrayInTask4.getJSONObject(i).getLong(HistoryJSONEnum.CREATE_TIME),
					historyJSONArrayInTask4.getJSONObject(i).getLong(HistoryJSONEnum.CREATE_TIME));
		}
		// end
		
		// Assert partners in task4
		JSONArray parterJSONArrayInTask4 = task4JSON.getJSONArray(TaskJSONEnum.PARTNERS);
		assertEquals(2, parterJSONArrayInTask4.length());
		assertEquals(account2.getID(), parterJSONArrayInTask4.getJSONObject(0).getString(AccountJSONEnum.USERNAME));
		assertEquals(account3.getID(), parterJSONArrayInTask4.getJSONObject(1).getString(AccountJSONEnum.USERNAME));
		// end
		
		// Assert attach files in task4
		JSONArray attachFileJSONArrayInTask4 = task4JSON.getJSONArray(StoryJSONEnum.ATTACH_FILES);
		assertEquals(1, attachFileJSONArrayInTask4.length());
		assertEquals("task4.txt", attachFileJSONArrayInTask4.getJSONObject(0).getString(AttachFileJSONEnum.NAME));
		assertEquals(TEXT_FILE_TYPE, attachFileJSONArrayInTask4.getJSONObject(0).getString(AttachFileJSONEnum.CONTENT_TYPE));
		assertEquals(FileEncoder.toBase64BinaryString(mSourceFileOfTask4), attachFileJSONArrayInTask4.getJSONObject(0).getString(AttachFileJSONEnum.BINARY));
		// end
		
		// Assert retrospectives in sprint
		JSONArray retrospectiveJSONArrayInSprint = sprintJSON.getJSONArray(SprintJSONEnum.RETROSPECTIVES);
		List<IScrumIssue> goods = mCRE.getGoodRetrospectiveList();
		List<IScrumIssue> improvements = mCRE.getImproveRetrospectiveList();
		assertEquals(4, retrospectiveJSONArrayInSprint.length());
		assertEquals(goods.get(0).getName(), retrospectiveJSONArrayInSprint.getJSONObject(0).getString(RetrospectiveJSONEnum.NAME));
		assertEquals(goods.get(0).getDescription(), retrospectiveJSONArrayInSprint.getJSONObject(0).getString(RetrospectiveJSONEnum.DESCRIPTION));
		assertEquals(goods.get(0).getCategory(), retrospectiveJSONArrayInSprint.getJSONObject(0).getString(RetrospectiveJSONEnum.TYPE));
		assertEquals(goods.get(0).getStatus(), retrospectiveJSONArrayInSprint.getJSONObject(0).getString(RetrospectiveJSONEnum.STATUS));
		
		assertEquals(goods.get(1).getName(), retrospectiveJSONArrayInSprint.getJSONObject(1).getString(RetrospectiveJSONEnum.NAME));
		assertEquals(goods.get(1).getDescription(), retrospectiveJSONArrayInSprint.getJSONObject(1).getString(RetrospectiveJSONEnum.DESCRIPTION));
		assertEquals(goods.get(1).getCategory(), retrospectiveJSONArrayInSprint.getJSONObject(1).getString(RetrospectiveJSONEnum.TYPE));
		assertEquals(goods.get(1).getStatus(), retrospectiveJSONArrayInSprint.getJSONObject(1).getString(RetrospectiveJSONEnum.STATUS));
		
		assertEquals(improvements.get(0).getName(), retrospectiveJSONArrayInSprint.getJSONObject(2).getString(RetrospectiveJSONEnum.NAME));
		assertEquals(improvements.get(0).getDescription(), retrospectiveJSONArrayInSprint.getJSONObject(2).getString(RetrospectiveJSONEnum.DESCRIPTION));
		assertEquals(improvements.get(0).getCategory(), retrospectiveJSONArrayInSprint.getJSONObject(2).getString(RetrospectiveJSONEnum.TYPE));
		assertEquals(improvements.get(0).getStatus(), retrospectiveJSONArrayInSprint.getJSONObject(2).getString(RetrospectiveJSONEnum.STATUS));
		
		assertEquals(improvements.get(1).getName(), retrospectiveJSONArrayInSprint.getJSONObject(3).getString(RetrospectiveJSONEnum.NAME));
		assertEquals(improvements.get(1).getDescription(), retrospectiveJSONArrayInSprint.getJSONObject(3).getString(RetrospectiveJSONEnum.DESCRIPTION));
		assertEquals(improvements.get(1).getCategory(), retrospectiveJSONArrayInSprint.getJSONObject(3).getString(RetrospectiveJSONEnum.TYPE));
		assertEquals(improvements.get(1).getStatus(), retrospectiveJSONArrayInSprint.getJSONObject(3).getString(RetrospectiveJSONEnum.STATUS));
		// end
		
		// Assert unplans in sprint
		JSONArray unplanJSONArrayInSprint = sprintJSON.getJSONArray(SprintJSONEnum.UNPLANS);
		assertEquals(2, unplanJSONArrayInSprint.length());
		assertEquals(unplan1.getSummary(), unplanJSONArrayInSprint.getJSONObject(0).getString(UnplanJSONEnum.NAME));
		assertEquals(unplan1.getAssignto(), unplanJSONArrayInSprint.getJSONObject(0).getString(UnplanJSONEnum.HANDLER));
		assertEquals(unplan1.getEstimated(), unplanJSONArrayInSprint.getJSONObject(0).getString(UnplanJSONEnum.ESTIMATE));
		assertEquals(unplan1.getActualHour(), unplanJSONArrayInSprint.getJSONObject(0).getString(UnplanJSONEnum.ACTUAL));
		assertEquals(unplan1.getNotes(), unplanJSONArrayInSprint.getJSONObject(0).getString(UnplanJSONEnum.NOTES));
		assertEquals(unplan1.getStatus(), unplanJSONArrayInSprint.getJSONObject(0).getString(UnplanJSONEnum.STATUS));
		
		assertEquals(unplan2.getSummary(), unplanJSONArrayInSprint.getJSONObject(1).getString(UnplanJSONEnum.NAME));
		assertEquals(unplan2.getAssignto(), unplanJSONArrayInSprint.getJSONObject(1).getString(UnplanJSONEnum.HANDLER));
		assertEquals(unplan2.getEstimated(), unplanJSONArrayInSprint.getJSONObject(1).getString(UnplanJSONEnum.ESTIMATE));
		assertEquals(unplan2.getActualHour(), unplanJSONArrayInSprint.getJSONObject(1).getString(UnplanJSONEnum.ACTUAL));
		assertEquals(unplan2.getNotes(), unplanJSONArrayInSprint.getJSONObject(1).getString(UnplanJSONEnum.NOTES));
		assertEquals(unplan2.getStatus(), unplanJSONArrayInSprint.getJSONObject(1).getString(UnplanJSONEnum.STATUS));
		// end
		
		// Assert partners in unplan1
		JSONObject unplan1JSON = unplanJSONArrayInSprint.getJSONObject(0);
		JSONArray partnersInUnplan1 = unplan1JSON.getJSONArray(UnplanJSONEnum.PARTNERS);
		assertEquals(0, partnersInUnplan1.length());
		JSONArray historyJSONArrayInUnplan1 = unplan1JSON.getJSONArray(UnplanJSONEnum.HISTORIES);
		@SuppressWarnings("deprecation")
		JSONArray expectedHistoryJSONArrayInUnplan1 = JSONEncoder.toHistoryJSONArray(unplan1.getIssueHistories(), unplan1.getCategory());
		assertEquals(expectedHistoryJSONArrayInUnplan1.length(), historyJSONArrayInUnplan1.length());
		for (int i = 0; i < expectedHistoryJSONArrayInUnplan1.length(); i++) {
			assertEquals(expectedHistoryJSONArrayInUnplan1.getJSONObject(i).getInt(HistoryJSONEnum.HISTORY_TYPE),
					historyJSONArrayInUnplan1.getJSONObject(i).getInt(HistoryJSONEnum.HISTORY_TYPE));
			assertEquals(expectedHistoryJSONArrayInUnplan1.getJSONObject(i).getString(HistoryJSONEnum.OLD_VALUE),
					historyJSONArrayInUnplan1.getJSONObject(i).getString(HistoryJSONEnum.OLD_VALUE));
			assertEquals(expectedHistoryJSONArrayInUnplan1.getJSONObject(i).getString(HistoryJSONEnum.NEW_VALUE),
					historyJSONArrayInUnplan1.getJSONObject(i).getString(HistoryJSONEnum.NEW_VALUE));
			assertEquals(expectedHistoryJSONArrayInUnplan1.getJSONObject(i).getLong(HistoryJSONEnum.CREATE_TIME),
					historyJSONArrayInUnplan1.getJSONObject(i).getLong(HistoryJSONEnum.CREATE_TIME));
		}
		// end
		
		// Assert partners in unplan2
		JSONObject unplan2JSON = unplanJSONArrayInSprint.getJSONObject(1);
		JSONArray partnersInUnplan2 = unplan2JSON.getJSONArray(UnplanJSONEnum.PARTNERS);
		assertEquals(2, partnersInUnplan2.length());
		assertEquals(account2.getID(), partnersInUnplan2.getJSONObject(0).getString(AccountJSONEnum.USERNAME));
		assertEquals(account3.getID(), partnersInUnplan2.getJSONObject(1).getString(AccountJSONEnum.USERNAME));
		JSONArray historyJSONArrayInUnplan2 = unplan2JSON.getJSONArray(UnplanJSONEnum.HISTORIES);
		@SuppressWarnings("deprecation")
		JSONArray expectedHistoryJSONArrayInUnplan2 = JSONEncoder.toHistoryJSONArray(unplan2.getIssueHistories(), unplan2.getCategory());
		assertEquals(expectedHistoryJSONArrayInUnplan2.length(), historyJSONArrayInUnplan2.length());
		for (int i = 0; i < expectedHistoryJSONArrayInUnplan1.length(); i++) {
			assertEquals(expectedHistoryJSONArrayInUnplan2.getJSONObject(i).getInt(HistoryJSONEnum.HISTORY_TYPE),
					historyJSONArrayInUnplan2.getJSONObject(i).getInt(HistoryJSONEnum.HISTORY_TYPE));
			assertEquals(expectedHistoryJSONArrayInUnplan2.getJSONObject(i).getString(HistoryJSONEnum.OLD_VALUE),
					historyJSONArrayInUnplan2.getJSONObject(i).getString(HistoryJSONEnum.OLD_VALUE));
			assertEquals(expectedHistoryJSONArrayInUnplan2.getJSONObject(i).getString(HistoryJSONEnum.NEW_VALUE),
					historyJSONArrayInUnplan2.getJSONObject(i).getString(HistoryJSONEnum.NEW_VALUE));
			assertEquals(expectedHistoryJSONArrayInUnplan2.getJSONObject(i).getLong(HistoryJSONEnum.CREATE_TIME),
					historyJSONArrayInUnplan2.getJSONObject(i).getLong(HistoryJSONEnum.CREATE_TIME));
		}
		// end
		
		JSONArray droppedStoryJSONArray = projectJSON.getJSONArray(ExportJSONEnum.DROPPED_STORIES);
		assertEquals(1, droppedStoryJSONArray.length());
		
		// Assert dropped story1 data in project
		JSONObject droppedStory1JSON = droppedStoryJSONArray.getJSONObject(0);
		assertEquals(story1.getIssueID(), droppedStory1JSON.getLong(StoryJSONEnum.ID));
		assertEquals(story1.getSummary(), droppedStory1JSON.getString(StoryJSONEnum.NAME));
		assertEquals(story2.getStatus(), droppedStory1JSON.getString(StoryJSONEnum.STATUS));
		assertEquals(Integer.parseInt(story1.getEstimated()), droppedStory1JSON.getInt(StoryJSONEnum.ESTIMATE));
		assertEquals(Integer.parseInt(story1.getImportance()), droppedStory1JSON.getInt(StoryJSONEnum.IMPORTANCE));
		assertEquals(Integer.parseInt(story1.getValue()), droppedStory1JSON.getInt(StoryJSONEnum.VALUE));
		assertEquals(story1.getNotes(), droppedStory1JSON.getString(StoryJSONEnum.NOTES));
		assertEquals(story1.getHowToDemo(), droppedStory1JSON.getString(StoryJSONEnum.HOW_TO_DEMO));
		JSONArray taskInDroppedStory1JSONArray = droppedStory1JSON.getJSONArray(StoryJSONEnum.TASKS);
		assertEquals(1, taskInDroppedStory1JSONArray.length());
		JSONArray historyJSONArrayInStory1 = droppedStory1JSON.getJSONArray(StoryJSONEnum.HISTORIES);
		@SuppressWarnings("deprecation")
		JSONArray expectedHistoryJSONArrayInStory1 = JSONEncoder.toHistoryJSONArray(story1.getIssueHistories(), story1.getCategory());
		assertEquals(expectedHistoryJSONArrayInStory1.length(), historyJSONArrayInStory1.length());
		for (int i = 0; i < expectedHistoryJSONArrayInStory1.length(); i++) {
			assertEquals(expectedHistoryJSONArrayInStory1.getJSONObject(i).getInt(HistoryJSONEnum.HISTORY_TYPE),
					historyJSONArrayInStory1.getJSONObject(i).getInt(HistoryJSONEnum.HISTORY_TYPE));
			assertEquals(expectedHistoryJSONArrayInStory1.getJSONObject(i).getString(HistoryJSONEnum.OLD_VALUE),
					historyJSONArrayInStory1.getJSONObject(i).getString(HistoryJSONEnum.OLD_VALUE));
			assertEquals(expectedHistoryJSONArrayInStory1.getJSONObject(i).getString(HistoryJSONEnum.NEW_VALUE),
					historyJSONArrayInStory1.getJSONObject(i).getString(HistoryJSONEnum.NEW_VALUE));
			assertEquals(expectedHistoryJSONArrayInStory1.getJSONObject(i).getLong(HistoryJSONEnum.CREATE_TIME),
					historyJSONArrayInStory1.getJSONObject(i).getLong(HistoryJSONEnum.CREATE_TIME));
		}
		// end
		
		// Assert attach files in dropped story1
		JSONArray attachFileJSONArrayInStory1 = droppedStory1JSON.getJSONArray(StoryJSONEnum.ATTACH_FILES);
		assertEquals(1, attachFileJSONArrayInStory1.length());
		assertEquals("story1.txt", attachFileJSONArrayInStory1.getJSONObject(0).getString(AttachFileJSONEnum.NAME));
		assertEquals(TEXT_FILE_TYPE, attachFileJSONArrayInStory1.getJSONObject(0).getString(AttachFileJSONEnum.CONTENT_TYPE));
		assertEquals(FileEncoder.toBase64BinaryString(mSourceFileOfStory1), attachFileJSONArrayInStory1.getJSONObject(0).getString(AttachFileJSONEnum.BINARY));
		// end
		
		// Assert tags in dropped story1
		JSONArray tagJSONArrayInStory1 = droppedStory1JSON.getJSONArray(StoryJSONEnum.TAGS);
		assertEquals(1, tagJSONArrayInStory1.length());
		assertEquals(PROJECT_TAG1, tagJSONArrayInStory1.getJSONObject(0).getString(TagJSONEnum.NAME));
		// end
		
		// Assert task2 data in dropped story1
		JSONObject task2JSON = taskInDroppedStory1JSONArray.getJSONObject(0);
		assertEquals(task2.getSummary(), task2JSON.getString(TaskJSONEnum.NAME));
		assertEquals(task2.getAssignto(), task2JSON.getString(TaskJSONEnum.HANDLER));
		assertEquals(Integer.parseInt(task2.getEstimated()), task2JSON.getInt(TaskJSONEnum.ESTIMATE));
		assertEquals(Integer.parseInt(task2.getRemains()), task2JSON.getInt(TaskJSONEnum.REMAIN));
		assertEquals(Integer.parseInt(task2.getActualHour()), task2JSON.getInt(TaskJSONEnum.ACTUAL));
		assertEquals(task2.getNotes(), task2JSON.getString(TaskJSONEnum.NOTES));
		assertEquals(task2.getStatus(), task2JSON.getString(TaskJSONEnum.STATUS));
		JSONArray historyJSONArrayInTask2 = task2JSON.getJSONArray(TaskJSONEnum.HISTORIES);
		@SuppressWarnings("deprecation")
		JSONArray expectedHistoryJSONArrayInTask2 = JSONEncoder.toHistoryJSONArray(task2.getIssueHistories(), task2.getCategory());
		assertEquals(expectedHistoryJSONArrayInTask2.length(), historyJSONArrayInTask2.length());
		for (int i = 0; i < expectedHistoryJSONArrayInTask2.length(); i++) {
			assertEquals(expectedHistoryJSONArrayInTask2.getJSONObject(i).getInt(HistoryJSONEnum.HISTORY_TYPE),
					historyJSONArrayInTask2.getJSONObject(i).getInt(HistoryJSONEnum.HISTORY_TYPE));
			assertEquals(expectedHistoryJSONArrayInTask2.getJSONObject(i).getString(HistoryJSONEnum.OLD_VALUE),
					historyJSONArrayInTask2.getJSONObject(i).getString(HistoryJSONEnum.OLD_VALUE));
			assertEquals(expectedHistoryJSONArrayInTask2.getJSONObject(i).getString(HistoryJSONEnum.NEW_VALUE),
					historyJSONArrayInTask2.getJSONObject(i).getString(HistoryJSONEnum.NEW_VALUE));
			assertEquals(expectedHistoryJSONArrayInTask2.getJSONObject(i).getLong(HistoryJSONEnum.CREATE_TIME),
					historyJSONArrayInTask2.getJSONObject(i).getLong(HistoryJSONEnum.CREATE_TIME));
		}
		// end
		
		// Assert partners in task2
		JSONArray partnerJSONArrayInTask2 = task2JSON.getJSONArray(TaskJSONEnum.PARTNERS);
		assertEquals(0, partnerJSONArrayInTask2.length());
		// end
		
		// Assert attach files in task2
		JSONArray attachFileJSONArrayInTask2 = task2JSON.getJSONArray(TaskJSONEnum.ATTACH_FILES);
		assertEquals(1, attachFileJSONArrayInTask2.length());
		assertEquals("task2.txt", attachFileJSONArrayInTask2.getJSONObject(0).getString(AttachFileJSONEnum.NAME));
		assertEquals(TEXT_FILE_TYPE, attachFileJSONArrayInTask2.getJSONObject(0).getString(AttachFileJSONEnum.CONTENT_TYPE));
		assertEquals(FileEncoder.toBase64BinaryString(mSourceFileOfTask2), attachFileJSONArrayInTask2.getJSONObject(0).getString(AttachFileJSONEnum.BINARY));
		// end
		
		JSONArray droppedTaskJSONArray = projectJSON.getJSONArray(ExportJSONEnum.DROPPED_TASKS);
		assertEquals(2, droppedTaskJSONArray.length());
		
		// Assert dropped task1 data
		JSONObject droppedTask1JSON = droppedTaskJSONArray.getJSONObject(0);
		assertEquals(task1.getSummary(), droppedTask1JSON.getString(TaskJSONEnum.NAME));
		assertEquals(task1.getAssignto(), droppedTask1JSON.getString(TaskJSONEnum.HANDLER));
		assertEquals(Integer.parseInt(task1.getEstimated()), droppedTask1JSON.getInt(TaskJSONEnum.ESTIMATE));
		assertEquals(Integer.parseInt(task1.getRemains()), droppedTask1JSON.getInt(TaskJSONEnum.REMAIN));
		assertEquals(Integer.parseInt(task1.getActualHour()), droppedTask1JSON.getInt(TaskJSONEnum.ACTUAL));
		assertEquals(task1.getNotes(), droppedTask1JSON.getString(TaskJSONEnum.NOTES));
		assertEquals(task1.getStatus(), droppedTask1JSON.getString(TaskJSONEnum.STATUS));
		JSONArray historyJSONArrayInTask1 = droppedTask1JSON.getJSONArray(TaskJSONEnum.HISTORIES);
		@SuppressWarnings("deprecation")
		JSONArray expectedHistoryJSONArrayInTask1 = JSONEncoder.toHistoryJSONArray(task1.getIssueHistories(), task1.getCategory());
		assertEquals(expectedHistoryJSONArrayInTask1.length(), historyJSONArrayInTask1.length());
		for (int i = 0; i < expectedHistoryJSONArrayInTask1.length(); i++) {
			assertEquals(expectedHistoryJSONArrayInTask1.getJSONObject(i).getInt(HistoryJSONEnum.HISTORY_TYPE),
					historyJSONArrayInTask1.getJSONObject(i).getInt(HistoryJSONEnum.HISTORY_TYPE));
			assertEquals(expectedHistoryJSONArrayInTask1.getJSONObject(i).getString(HistoryJSONEnum.OLD_VALUE),
					historyJSONArrayInTask1.getJSONObject(i).getString(HistoryJSONEnum.OLD_VALUE));
			assertEquals(expectedHistoryJSONArrayInTask1.getJSONObject(i).getString(HistoryJSONEnum.NEW_VALUE),
					historyJSONArrayInTask1.getJSONObject(i).getString(HistoryJSONEnum.NEW_VALUE));
			assertEquals(expectedHistoryJSONArrayInTask1.getJSONObject(i).getLong(HistoryJSONEnum.CREATE_TIME),
					historyJSONArrayInTask1.getJSONObject(i).getLong(HistoryJSONEnum.CREATE_TIME));
		}
		// end
		
		// Assert partners in dropped task1
		JSONArray partnerJSONArrayInTask1 = droppedTask1JSON.getJSONArray(TaskJSONEnum.PARTNERS);
		assertEquals(0, partnerJSONArrayInTask1.length());
		// end
		
		// Assert attach files in dropped task1
		JSONArray attachFileJSONArrayInTask1 = droppedTask1JSON.getJSONArray(TaskJSONEnum.ATTACH_FILES);
		assertEquals(1, attachFileJSONArrayInTask1.length());
		assertEquals("task1.txt", attachFileJSONArrayInTask1.getJSONObject(0).getString(AttachFileJSONEnum.NAME));
		assertEquals(TEXT_FILE_TYPE, attachFileJSONArrayInTask1.getJSONObject(0).getString(AttachFileJSONEnum.CONTENT_TYPE));
		assertEquals(FileEncoder.toBase64BinaryString(mSourceFileOfTask1), attachFileJSONArrayInTask1.getJSONObject(0).getString(AttachFileJSONEnum.BINARY));
		// end
		
		// Assert dropped task3 data
		JSONObject droppedTask3JSON = droppedTaskJSONArray.getJSONObject(1);
		assertEquals(task3.getSummary(), droppedTask3JSON.getString(TaskJSONEnum.NAME));
		assertEquals(task3.getAssignto(), droppedTask3JSON.getString(TaskJSONEnum.HANDLER));
		assertEquals(Integer.parseInt(task3.getEstimated()), droppedTask3JSON.getInt(TaskJSONEnum.ESTIMATE));
		assertEquals(Integer.parseInt(task3.getRemains()), droppedTask3JSON.getInt(TaskJSONEnum.REMAIN));
		assertEquals(Integer.parseInt(task3.getActualHour()), droppedTask3JSON.getInt(TaskJSONEnum.ACTUAL));
		assertEquals(task3.getNotes(), droppedTask3JSON.getString(TaskJSONEnum.NOTES));
		assertEquals(task3.getStatus(), droppedTask3JSON.getString(TaskJSONEnum.STATUS));
		JSONArray historyJSONArrayInTask3 = droppedTask3JSON.getJSONArray(TaskJSONEnum.HISTORIES);
		@SuppressWarnings("deprecation")
		JSONArray expectedHistoryJSONArrayInTask3 = JSONEncoder.toHistoryJSONArray(task3.getIssueHistories(), task3.getCategory());
		assertEquals(expectedHistoryJSONArrayInTask3.length(), historyJSONArrayInTask3.length());
		for (int i = 0; i < expectedHistoryJSONArrayInTask3.length(); i++) {
			assertEquals(expectedHistoryJSONArrayInTask3.getJSONObject(i).getInt(HistoryJSONEnum.HISTORY_TYPE),
					historyJSONArrayInTask3.getJSONObject(i).getInt(HistoryJSONEnum.HISTORY_TYPE));
			assertEquals(expectedHistoryJSONArrayInTask3.getJSONObject(i).getString(HistoryJSONEnum.OLD_VALUE),
					historyJSONArrayInTask3.getJSONObject(i).getString(HistoryJSONEnum.OLD_VALUE));
			assertEquals(expectedHistoryJSONArrayInTask3.getJSONObject(i).getString(HistoryJSONEnum.NEW_VALUE),
					historyJSONArrayInTask3.getJSONObject(i).getString(HistoryJSONEnum.NEW_VALUE));
			assertEquals(expectedHistoryJSONArrayInTask3.getJSONObject(i).getLong(HistoryJSONEnum.CREATE_TIME),
					historyJSONArrayInTask3.getJSONObject(i).getLong(HistoryJSONEnum.CREATE_TIME));
		}
		// end
		
		// Assert partners in task3
		JSONArray partnerJSONArrayInTask3 = droppedTask3JSON.getJSONArray(TaskJSONEnum.PARTNERS);
		assertEquals(1, partnerJSONArrayInTask3.length());
		assertEquals(account2.getID(), partnerJSONArrayInTask3.getJSONObject(0).getString(AccountJSONEnum.USERNAME));
		// end
		
		// Assert attach files in task3
		JSONArray attachFileJSONArrayInTask3 = droppedTask3JSON.getJSONArray(TaskJSONEnum.ATTACH_FILES);
		assertEquals(1, attachFileJSONArrayInTask3.length());
		assertEquals("task3.txt", attachFileJSONArrayInTask3.getJSONObject(0).getString(AttachFileJSONEnum.NAME));
		assertEquals(TEXT_FILE_TYPE, attachFileJSONArrayInTask3.getJSONObject(0).getString(AttachFileJSONEnum.CONTENT_TYPE));
		assertEquals(FileEncoder.toBase64BinaryString(mSourceFileOfTask3), attachFileJSONArrayInTask3.getJSONObject(0).getString(AttachFileJSONEnum.BINARY));
		// end
	}
}
