package ntut.csie.ezScrum.restful.export;

import static org.junit.Assert.assertEquals;

import java.net.URI;

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
import ntut.csie.ezScrum.test.CreateData.AddStoryToSprint;
import ntut.csie.ezScrum.test.CreateData.AddTaskToStory;
import ntut.csie.ezScrum.test.CreateData.CopyProject;
import ntut.csie.ezScrum.test.CreateData.CreateProductBacklog;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.ezScrum.web.databaseEnum.TaskEnum;
import ntut.csie.ezScrum.web.helper.SprintBacklogHelper;
import ntut.csie.ezScrum.web.support.export.JSONEncoder;
import ntut.csie.jcis.resource.core.IProject;

public class WildTaskRESTfulApiTest extends JerseyTest {
	private ezScrumInfoConfig mConfig = new ezScrumInfoConfig();
	private CreateProject mCP;
	private CreateSprint mCS;
	private AddStoryToSprint mASTS;
	private AddTaskToStory mATTS;

	private Client mClient;
	private HttpServer mHttpServer;
	private ResourceConfig mResourceConfig;
	private static String BASE_URL = "http://localhost:8080/ezScrum/resource/";
	private URI mBaseUri = URI.create(BASE_URL);

	@Override
	protected Application configure() {
		mResourceConfig = new ResourceConfig(WildTaskRESTfulApi.class);
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

		// Create Sprint
		mCS = new CreateSprint(1, mCP);
		mCS.exe();

		// Add Story to Sprint
		mASTS = new AddStoryToSprint(2, 8, mCS, mCP, CreateProductBacklog.TYPE_ESTIMATION);
		mASTS.exe();
		
		// Add Task to Story
		mATTS = new AddTaskToStory(2, 13, mASTS, mCP);
		mATTS.exe();

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
		mCS = null;
		mASTS = null;
		mATTS = null;
		mHttpServer = null;
		mResourceConfig = null;
		mBaseUri = null;
		mClient = null;
	}
	
	@Test
	public void testGetList() throws JSONException {
		IProject project = mCP.getProjectList().get(0);
		
		// Call '/projects/{projectName}/tasks' API
		Response response = mClient.target(mBaseUri)
		                           .path("projects/" +  project.getName() + "/tasks")
		                           .request()
		                           .get();
		
		JSONArray jsonResponse = new JSONArray(response.readEntity(String.class));

		// Assert
		assertEquals(4, jsonResponse.length());
		assertEquals(JSONEncoder.toTaskJSONArray(mATTS.getTaskList()).toString(), jsonResponse.toString());
	}
	
	@Test
	public void testGetList_WithWildQuery() throws JSONException {
		IProject project = mCP.getProjectList().get(0);
		IIssue story = mASTS.getIssueList().get(0);
		IIssue task1 = mATTS.getTaskList().get(0);
		IIssue task2 = mATTS.getTaskList().get(1);
		
		// Remove task from story
		SprintBacklogHelper sprintBacklogHelper = new SprintBacklogHelper(project, null);
		sprintBacklogHelper.removeTask(task1.getIssueID(), story.getIssueID());
		sprintBacklogHelper.removeTask(task2.getIssueID(), story.getIssueID());
		
		// Call '/projects/{projectName}/tasks?isWild=true' API
		Response response = mClient.target(mBaseUri)
		                           .path("projects/" + project.getName() + "/tasks")
		                           .queryParam("isWild", true)
		                           .request()
		                           .get();

		JSONArray jsonResponse = new JSONArray(response.readEntity(String.class));
		
		// Assert
		assertEquals(2, jsonResponse.length());
		assertEquals(task1.getSummary(), jsonResponse.getJSONObject(0).get(TaskEnum.NAME));
		assertEquals(task1.getAssignto(), jsonResponse.getJSONObject(0).get(TaskEnum.HANDLER));
		assertEquals(Integer.parseInt(task1.getEstimated()), jsonResponse.getJSONObject(0).get(TaskEnum.ESTIMATE));
		assertEquals(Integer.parseInt(task1.getRemains()), jsonResponse.getJSONObject(0).get(TaskEnum.REMAIN));
		assertEquals(Integer.parseInt(task1.getActualHour()), jsonResponse.getJSONObject(0).get(TaskEnum.ACTUAL));
		assertEquals(task1.getNotes(), jsonResponse.getJSONObject(0).get(TaskEnum.NOTES));
		assertEquals(task1.getStatus(), jsonResponse.getJSONObject(0).get(TaskEnum.STATUS));
		
		assertEquals(task2.getSummary(), jsonResponse.getJSONObject(1).get(TaskEnum.NAME));
		assertEquals(task2.getAssignto(), jsonResponse.getJSONObject(1).get(TaskEnum.HANDLER));
		assertEquals(Integer.parseInt(task2.getEstimated()), jsonResponse.getJSONObject(1).get(TaskEnum.ESTIMATE));
		assertEquals(Integer.parseInt(task2.getRemains()), jsonResponse.getJSONObject(1).get(TaskEnum.REMAIN));
		assertEquals(Integer.parseInt(task2.getActualHour()), jsonResponse.getJSONObject(1).get(TaskEnum.ACTUAL));
		assertEquals(task2.getNotes(), jsonResponse.getJSONObject(1).get(TaskEnum.NOTES));
		assertEquals(task2.getStatus(), jsonResponse.getJSONObject(1).get(TaskEnum.STATUS));
	}
	
	@Test
	public void testGet() throws JSONException {
		IProject project = mCP.getProjectList().get(0);
		IIssue task = mATTS.getTaskList().get(0);
		
		// Call '/projects/{projectName}/tasks' API
		Response response = mClient.target(mBaseUri)
		                           .path("projects/" + project.getName() + "/tasks/" + task.getIssueID())
		                           .request()
		                           .get();
		
		JSONObject jsonResponse = new JSONObject(response.readEntity(String.class));

		// Assert
		assertEquals(task.getSummary(), jsonResponse.get(TaskEnum.NAME));
		assertEquals(task.getAssignto(), jsonResponse.get(TaskEnum.HANDLER));
		assertEquals(Integer.parseInt(task.getEstimated()), jsonResponse.get(TaskEnum.ESTIMATE));
		assertEquals(Integer.parseInt(task.getRemains()), jsonResponse.get(TaskEnum.REMAIN));
		assertEquals(Integer.parseInt(task.getActualHour()), jsonResponse.get(TaskEnum.ACTUAL));
		assertEquals(task.getNotes(), jsonResponse.get(TaskEnum.NOTES));
		assertEquals(task.getStatus(), jsonResponse.get(TaskEnum.STATUS));
	}
	
	@Test
	public void testGet_Wild() throws JSONException {
		IProject project = mCP.getProjectList().get(0);
		IIssue story = mASTS.getIssueList().get(0);
		IIssue task = mATTS.getTaskList().get(0);
		
		// Remove task from story
		SprintBacklogHelper sprintBacklogHelper = new SprintBacklogHelper(project, null);
		sprintBacklogHelper.removeTask(task.getIssueID(), story.getIssueID());
		
		// Call '/projects/{projectName}/tasks' API
		Response response = mClient.target(mBaseUri)
		                           .path("projects/" + project.getName() + "/tasks/" + task.getIssueID())
		                           .request()
		                           .get();
		
		JSONObject jsonResponse = new JSONObject(response.readEntity(String.class));

		// Assert
		assertEquals(task.getSummary(), jsonResponse.get(TaskEnum.NAME));
		assertEquals(task.getAssignto(), jsonResponse.get(TaskEnum.HANDLER));
		assertEquals(Integer.parseInt(task.getEstimated()), jsonResponse.get(TaskEnum.ESTIMATE));
		assertEquals(Integer.parseInt(task.getRemains()), jsonResponse.get(TaskEnum.REMAIN));
		assertEquals(Integer.parseInt(task.getActualHour()), jsonResponse.get(TaskEnum.ACTUAL));
		assertEquals(task.getNotes(), jsonResponse.get(TaskEnum.NOTES));
		assertEquals(task.getStatus(), jsonResponse.get(TaskEnum.STATUS));
	}
}
