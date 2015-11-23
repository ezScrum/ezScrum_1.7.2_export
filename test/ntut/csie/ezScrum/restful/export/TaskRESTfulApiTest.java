package ntut.csie.ezScrum.restful.export;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.net.httpserver.HttpServer;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.issue.core.IIssueHistory;
import ntut.csie.ezScrum.iteration.core.ScrumEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.AttachFileJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.HistoryJSONEnum;
import ntut.csie.ezScrum.restful.export.support.FileEncoder;
import ntut.csie.ezScrum.restful.export.support.JSONEncoder;
import ntut.csie.ezScrum.test.CreateData.AddStoryToSprint;
import ntut.csie.ezScrum.test.CreateData.AddTaskToStory;
import ntut.csie.ezScrum.test.CreateData.CopyProject;
import ntut.csie.ezScrum.test.CreateData.CreateAccount;
import ntut.csie.ezScrum.test.CreateData.CreateProductBacklog;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.ezScrum.web.dataObject.TaskObject;
import ntut.csie.ezScrum.web.helper.SprintBacklogHelper;
import ntut.csie.ezScrum.web.mapper.SprintBacklogMapper;
import ntut.csie.jcis.account.core.IAccount;
import ntut.csie.jcis.resource.core.IProject;

public class TaskRESTfulApiTest extends JerseyTest {
	private ezScrumInfoConfig mConfig = new ezScrumInfoConfig();
	private CreateProject mCP;
	private CreateSprint mCS;
	private AddStoryToSprint mASTS;
	private AddTaskToStory mATTS;
	private CreateAccount mCA;

	private Client mClient;
	private HttpServer mHttpServer;
	private ResourceConfig mResourceConfig;
	private static String BASE_URL = "http://localhost:8080/ezScrum/resource/";
	private URI mBaseUri = URI.create(BASE_URL);

	@Override
	protected Application configure() {
		mResourceConfig = new ResourceConfig(TaskRESTfulApi.class);
		return mResourceConfig;
	}

	@Before
	public void setUp() throws Exception {
		// 初始化 SQL
		InitialSQL ini = new InitialSQL(mConfig);
		ini.exe();

		// Create Project
		mCP = new CreateProject(2);
		mCP.exeCreate();

		// Create Sprint
		mCS = new CreateSprint(1, mCP);
		mCS.exe();

		// Add Story to project
		mASTS = new AddStoryToSprint(2, 8, mCS, mCP, CreateProductBacklog.TYPE_ESTIMATION);
		mASTS.exe();

		// Add Task to project
		mATTS = new AddTaskToStory(2, 13, mASTS, mCP);
		mATTS.exe();
		
		// Create Account
		mCA = new CreateAccount(2);
		mCA.exe();
		
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

		// Stop Server
		mHttpServer.stop(0);

		// ============= release ==============
		ini = null;
		copyProject = null;
		mCP = null;
		mHttpServer = null;
		mResourceConfig = null;
		mBaseUri = null;
		mClient = null;
	}
	
	@Test
	public void testGetTasksinStory() {
		IProject project = mCP.getProjectList().get(0);
		String sprintId = mCS.getSprintIDList().get(0);
		IIssue story = mASTS.getIssueList().get(0);
		
		SprintBacklogHelper sprintBacklogHelper = new SprintBacklogHelper(project, null,sprintId);
		// Get Tasks
		IIssue[] tasks = sprintBacklogHelper.getTaskInStory(String.valueOf(story.getIssueID()));
		
		// Create Account
		IAccount account1 = mCA.getAccountList().get(0);
		IAccount account2 = mCA.getAccountList().get(1);
		// Add Partners to Task1
		String partners = account1.getID() + ";" + account2.getID();
		sprintBacklogHelper.checkOutTask(tasks[0].getIssueID(), tasks[0].getSummary(), tasks[0].getAssignto(), partners, "", null);
		// Refresh tasks
		tasks = sprintBacklogHelper.getTaskInStory(String.valueOf(story.getIssueID()));
		
		// Call '/projects/{projectName}/sprints/{sprintId}/stories/{storyId}/tasks/{taskId}' API
		Response response = mClient.target(mBaseUri)
								   .path("projects/" + project.getName() +
										 "/sprints/" + sprintId + 
										 "/stories/" + story.getIssueID() + 
										 "/tasks/")
								   .request()
								   .get();
		assertEquals(JSONEncoder.toTaskJSONArray(Arrays.asList(tasks)).toString(), response.readEntity(String.class));
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void testGetAttachFiles() throws JSONException {
		// Test Data
		String testFile = "./TestData/RoleBase.xml";
		IProject project = mCP.getProjectList().get(0);
		String sprintId = mCS.getSprintIDList().get(0);
		IIssue story = mASTS.getIssueList().get(0);
		IIssue task = mATTS.getTaskList().get(0);

		// Upload Attach File
		SprintBacklogMapper sprintBacklogMapper = new SprintBacklogMapper(project, null);
		sprintBacklogMapper.addAttachFile(task.getIssueID(), testFile);

		// Call '/projects/{projectName}/sprints/{sprintId}/stories/{storyId}/tasks/{taskId}/attachfiles' API
		Response response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() +
		                "/sprints/" + sprintId +
		                "/stories/" + story.getIssueID() +
		                "/tasks/" + task.getIssueID() +
		                "/attachfiles")
		        .request()
		        .get();

		JSONArray jsonResponse = new JSONArray(response.readEntity(String.class));
		String expectedXmlBinary = FileEncoder.toBase64BinaryString(new File(testFile));

		// Assert
		assertEquals(1, jsonResponse.length());
		assertEquals("RoleBase.xml", jsonResponse.getJSONObject(0).getString(AttachFileJSONEnum.NAME));
		assertEquals("text/xml", jsonResponse.getJSONObject(0).getString(AttachFileJSONEnum.CONTENT_TYPE));
		assertEquals(expectedXmlBinary, jsonResponse.getJSONObject(0).getString(AttachFileJSONEnum.BINARY));
	}
	
	@Test
	public void testGetAttachFiles_MultipleFiles() throws JSONException {
		// Test Data
		String testFile1 = "./TestData/RoleBase.xml";
		String testFile2 = "./TestData/InitialData/ScrumRole.xml";
		IProject project = mCP.getProjectList().get(0);
		String sprintId = mCS.getSprintIDList().get(0);
		IIssue story = mASTS.getIssueList().get(0);
		IIssue task = mATTS.getTaskList().get(0);

		// Upload Attach File
		SprintBacklogMapper sprintBacklogMapper = new SprintBacklogMapper(project, null);
		sprintBacklogMapper.addAttachFile(task.getIssueID(), testFile1);
		sprintBacklogMapper.addAttachFile(task.getIssueID(), testFile2);

		// Call '/projects/{projectName}/sprints/{sprintId}/stories/{storyId}/tasks/{taskId}/attachfiles' API
		Response response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() +
		                "/sprints/" + sprintId +
		                "/stories/" + story.getIssueID() +
		                "/tasks/" + task.getIssueID() +
		                "/attachfiles")
		        .request()
		        .get();

		JSONArray jsonResponse = new JSONArray(response.readEntity(String.class));
		String expectedXmlBinary1 = FileEncoder.toBase64BinaryString(new File(testFile1));
		String expectedXmlBinary2 = FileEncoder.toBase64BinaryString(new File(testFile2));

		// Assert
		assertEquals(2, jsonResponse.length());
		assertEquals("RoleBase.xml", jsonResponse.getJSONObject(0).getString(AttachFileJSONEnum.NAME));
		assertEquals("text/xml", jsonResponse.getJSONObject(0).getString(AttachFileJSONEnum.CONTENT_TYPE));
		assertEquals(expectedXmlBinary1, jsonResponse.getJSONObject(0).getString(AttachFileJSONEnum.BINARY));
		
		assertEquals("ScrumRole.xml", jsonResponse.getJSONObject(1).getString(AttachFileJSONEnum.NAME));
		assertEquals("text/xml", jsonResponse.getJSONObject(1).getString(AttachFileJSONEnum.CONTENT_TYPE));
		assertEquals(expectedXmlBinary2, jsonResponse.getJSONObject(1).getString(AttachFileJSONEnum.BINARY));
	}
	
	@Test
	public void testGetHistoriesInTask_CreateTask() throws JSONException {
		IProject project = mCP.getProjectList().get(0);
		String sprintId = mCS.getSprintIDList().get(0);
		IIssue story = mASTS.getIssueList().get(0);
		IIssue task = mATTS.getTaskList().get(0);
		
		// Call '/projects/{projectName}/sprints/{sprintId}/stories/{storyId}/tasks/{taskId}/histories' API
		Response response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() +
		              "/sprints/" + sprintId +
		              "/stories/" + story.getIssueID() +
		              "/tasks/" + task.getIssueID() +
		              "/histories")
		        .request()
		        .get();
		
		@SuppressWarnings("deprecation")
		List<IIssueHistory> histories = task.getIssueHistories();
		JSONArray expectedJSONArray = JSONEncoder.toHistoryJSONArray(histories, ScrumEnum.TASK_ISSUE_TYPE);
		
		JSONArray jsonResponse = new JSONArray(response.readEntity(String.class));
		// Assert
		assertEquals(expectedJSONArray.length(), jsonResponse.length());
		// First History
		assertEquals(expectedJSONArray.getJSONObject(0).getInt(HistoryJSONEnum.HISTORY_TYPE), jsonResponse.getJSONObject(0).getInt(HistoryJSONEnum.HISTORY_TYPE));
		assertEquals(expectedJSONArray.getJSONObject(0).getString(HistoryJSONEnum.OLD_VALUE), jsonResponse.getJSONObject(0).getString(HistoryJSONEnum.OLD_VALUE));
		assertEquals(expectedJSONArray.getJSONObject(0).getString(HistoryJSONEnum.NEW_VALUE), jsonResponse.getJSONObject(0).getString(HistoryJSONEnum.NEW_VALUE));
		assertEquals(expectedJSONArray.getJSONObject(0).getLong(HistoryJSONEnum.CREATE_TIME), jsonResponse.getJSONObject(0).getLong(HistoryJSONEnum.CREATE_TIME));
		// Second History
		assertEquals(expectedJSONArray.getJSONObject(1).getInt(HistoryJSONEnum.HISTORY_TYPE), jsonResponse.getJSONObject(1).getInt(HistoryJSONEnum.HISTORY_TYPE));
		assertEquals(expectedJSONArray.getJSONObject(1).getString(HistoryJSONEnum.OLD_VALUE), jsonResponse.getJSONObject(1).getString(HistoryJSONEnum.OLD_VALUE));
		assertEquals(expectedJSONArray.getJSONObject(1).getString(HistoryJSONEnum.NEW_VALUE), jsonResponse.getJSONObject(1).getString(HistoryJSONEnum.NEW_VALUE));
		assertEquals(expectedJSONArray.getJSONObject(1).getLong(HistoryJSONEnum.CREATE_TIME), jsonResponse.getJSONObject(1).getLong(HistoryJSONEnum.CREATE_TIME));
		// Third History
		assertEquals(expectedJSONArray.getJSONObject(2).getInt(HistoryJSONEnum.HISTORY_TYPE), jsonResponse.getJSONObject(2).getInt(HistoryJSONEnum.HISTORY_TYPE));
		assertEquals(expectedJSONArray.getJSONObject(2).getString(HistoryJSONEnum.OLD_VALUE), jsonResponse.getJSONObject(2).getString(HistoryJSONEnum.OLD_VALUE));
		assertEquals(expectedJSONArray.getJSONObject(2).getString(HistoryJSONEnum.NEW_VALUE), jsonResponse.getJSONObject(2).getString(HistoryJSONEnum.NEW_VALUE));
		assertEquals(expectedJSONArray.getJSONObject(2).getLong(HistoryJSONEnum.CREATE_TIME), jsonResponse.getJSONObject(2).getLong(HistoryJSONEnum.CREATE_TIME));
		// fourth History
		assertEquals(expectedJSONArray.getJSONObject(3).getInt(HistoryJSONEnum.HISTORY_TYPE), jsonResponse.getJSONObject(3).getInt(HistoryJSONEnum.HISTORY_TYPE));
		assertEquals(expectedJSONArray.getJSONObject(3).getString(HistoryJSONEnum.OLD_VALUE), jsonResponse.getJSONObject(3).getString(HistoryJSONEnum.OLD_VALUE));
		assertEquals(expectedJSONArray.getJSONObject(3).getString(HistoryJSONEnum.NEW_VALUE), jsonResponse.getJSONObject(3).getString(HistoryJSONEnum.NEW_VALUE));
		assertEquals(expectedJSONArray.getJSONObject(3).getLong(HistoryJSONEnum.CREATE_TIME), jsonResponse.getJSONObject(3).getLong(HistoryJSONEnum.CREATE_TIME));
	}
	
	@Test
	public void testGetHistoriesInTask_ModifyTaskInformation() throws JSONException {
		IProject project = mCP.getProjectList().get(0);
		String sprintId = mCS.getSprintIDList().get(0);
		IIssue story = mASTS.getIssueList().get(0);
		IIssue task = mATTS.getTaskList().get(0);
		
		SprintBacklogHelper sprintBacklogHelper = new SprintBacklogHelper(project, null, sprintId);
		TaskObject taskInformation = new TaskObject();
		taskInformation.id = String.valueOf(task.getIssueID());
		taskInformation.estimation = "8";
		taskInformation.name = "TEST_TASK_NAME_NEW";
		taskInformation.remains = "1";
		taskInformation.actual = "20";
		sprintBacklogHelper.editTask(taskInformation);
		task = sprintBacklogHelper.getTaskById(task.getIssueID());
		
		// Call '/projects/{projectName}/sprints/{sprintId}/stories/{storyId}/tasks/{taskId}/histories' API
		Response response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() +
		              "/sprints/" + sprintId +
		              "/stories/" + story.getIssueID() +
		              "/tasks/" + task.getIssueID() +
		              "/histories")
		        .request()
		        .get();
		
		@SuppressWarnings("deprecation")
		List<IIssueHistory> histories = task.getIssueHistories();
		JSONArray expectedJSONArray = JSONEncoder.toHistoryJSONArray(histories, ScrumEnum.TASK_ISSUE_TYPE);
		
		JSONArray jsonResponse = new JSONArray(response.readEntity(String.class));
		// Assert
		assertEquals(expectedJSONArray.length(), jsonResponse.length());
		for (int i = 0; i < expectedJSONArray.length(); i++) {
			assertEquals(expectedJSONArray.getJSONObject(i).getInt(HistoryJSONEnum.HISTORY_TYPE), jsonResponse.getJSONObject(i).getInt(HistoryJSONEnum.HISTORY_TYPE));
			assertEquals(expectedJSONArray.getJSONObject(i).getString(HistoryJSONEnum.OLD_VALUE), jsonResponse.getJSONObject(i).getString(HistoryJSONEnum.OLD_VALUE));
			assertEquals(expectedJSONArray.getJSONObject(i).getString(HistoryJSONEnum.NEW_VALUE), jsonResponse.getJSONObject(i).getString(HistoryJSONEnum.NEW_VALUE));
			assertEquals(expectedJSONArray.getJSONObject(i).getLong(HistoryJSONEnum.CREATE_TIME), jsonResponse.getJSONObject(i).getLong(HistoryJSONEnum.CREATE_TIME));
		}
	}
	
	@Test
	public void testGetHistoriesInTask_ModifyTaskStatus() throws JSONException, Exception {
		IProject project = mCP.getProjectList().get(0);
		String sprintId = mCS.getSprintIDList().get(0);
		IIssue story = mASTS.getIssueList().get(0);
		IIssue task = mATTS.getTaskList().get(0);
		
		// Checkout Task
		SprintBacklogHelper sprintBacklogHelper = new SprintBacklogHelper(project, null, sprintId);
		sprintBacklogHelper.checkOutTask(task.getIssueID(), task.getSummary(), "henry", "", "", null);
		Thread.sleep(1000);
		// Done Task
		sprintBacklogHelper.doneIssue(task.getIssueID(), task.getSummary(), task.getNotes(), null, "0");
		Thread.sleep(1000);
		task = sprintBacklogHelper.getTaskById(task.getIssueID());

		// Call '/projects/{projectName}/sprints/{sprintId}/stories/{storyId}/tasks/{taskId}/histories' API
		Response response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() +
		              "/sprints/" + sprintId +
		              "/stories/" + story.getIssueID() +
		              "/tasks/" + task.getIssueID() +
		              "/histories")
		        .request()
		        .get();

		@SuppressWarnings("deprecation")
		List<IIssueHistory> histories = task.getIssueHistories();
		JSONArray expectedJSONArray = JSONEncoder.toHistoryJSONArray(histories, ScrumEnum.TASK_ISSUE_TYPE);

		JSONArray jsonResponse = new JSONArray(response.readEntity(String.class));
		// Assert
		assertEquals(expectedJSONArray.length(), jsonResponse.length());
		for (int i = 0; i < expectedJSONArray.length(); i++) {
			assertEquals(expectedJSONArray.getJSONObject(i).getInt(HistoryJSONEnum.HISTORY_TYPE), jsonResponse.getJSONObject(i).getInt(HistoryJSONEnum.HISTORY_TYPE));
			assertEquals(expectedJSONArray.getJSONObject(i).getString(HistoryJSONEnum.OLD_VALUE), jsonResponse.getJSONObject(i).getString(HistoryJSONEnum.OLD_VALUE));
			assertEquals(expectedJSONArray.getJSONObject(i).getString(HistoryJSONEnum.NEW_VALUE), jsonResponse.getJSONObject(i).getString(HistoryJSONEnum.NEW_VALUE));
			assertEquals(expectedJSONArray.getJSONObject(i).getLong(HistoryJSONEnum.CREATE_TIME), jsonResponse.getJSONObject(i).getLong(HistoryJSONEnum.CREATE_TIME));
		}
	}
}
