package ntut.csie.ezScrum.restful.export;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

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
import ntut.csie.ezScrum.test.CreateData.CreateRelease;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.ezScrum.web.helper.SprintBacklogHelper;
import ntut.csie.ezScrum.web.logic.ProductBacklogLogic;
import ntut.csie.jcis.resource.core.IProject;

public class RESTfulApiPathConflictTest extends JerseyTest {
	private ezScrumInfoConfig mConfig = new ezScrumInfoConfig();
	private CreateProject mCP;
	private CreateRelease mCR;
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
		mResourceConfig = new ResourceConfig(ProjectRESTfulApi.class, SprintRESTfulApi.class, StoryRESTfulApi.class,
		                                     TaskRESTfulApi.class, DroppedStoryRESTfulApi.class, DroppedTaskRESTfulApi.class,
		                                     ReleaseRESTfulApi.class, AccountRESTfulApi.class);
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
		mCS = new CreateSprint(2, mCP);
		mCS.exe();

		// Add Story to project
		mASTS = new AddStoryToSprint(2, 8, mCS, mCP, CreateProductBacklog.TYPE_ESTIMATION);
		mASTS.exe();

		// Add Task to project
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
		mASTS = null;
		mATTS = null;
		mHttpServer = null;
		mClient = null;
	}
	
	@Test
	public void testIsPathConflict() throws InterruptedException {
		// Test Data
		// Project 1
		IProject project1 = mCP.getProjectList().get(0);
		String sprintId1 = mCS.getSprintIDList().get(0);
		IIssue story1 = mASTS.getIssueList().get(0);
		IIssue story2 = mASTS.getIssueList().get(1);
		IIssue task1 = mATTS.getTaskList().get(0);
		IIssue task3 = mATTS.getTaskList().get(2);
		
		// Drop Story1
		ProductBacklogLogic productBacklogLogic = new ProductBacklogLogic(null, project1);
		
		// It's need some delay for manipulating file IO (add story to sprint)
		Thread.sleep(1000);
		// Remove story1 from Sprint
		productBacklogLogic.removeStoryFromSprint(story1.getIssueID());
		
		// Drop task1, task3 from story
		SprintBacklogHelper sprintBacklogHelper = new SprintBacklogHelper(project1, null);
		sprintBacklogHelper.removeTask(task1.getIssueID(), story1.getIssueID());
		sprintBacklogHelper.removeTask(task3.getIssueID(), story2.getIssueID());
		
		//{projectName}/sprints/{sprintId}/stories/{storyId}/tasks/{taskId}
		// Api Test
		// Call '/projects' API
		Response response = mClient.target(mBaseUri)
		        .path("projects")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		// Call '/projects/{projectName}/sprints' API
		response = mClient.target(mBaseUri)
		        .path("projects/" + project1.getName() + "/sprints")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		// Call '/projects/{projectName}/sprints/{sprintId}/stories' API
		response = mClient.target(mBaseUri)
		        .path("projects/" + project1.getName() +
		              "/sprints/" + sprintId1 +
		              "/stories")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		// Call '/projects/{projectName}/sprints/{sprintId}/stories/{storyId}/tasks' API
		response = mClient.target(mBaseUri)
		        .path("projects/" + project1.getName() +
		              "/sprints/" + sprintId1 +
		              "/stories/" + story2.getIssueID() +
		              "/tasks")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		// Call '/projects/{projectName}/stories' API
		response = mClient.target(mBaseUri)
		        .path("projects/" + project1.getName() +
		              "/stories")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		// Call '/projects/{projectName}/stories' API
		response = mClient.target(mBaseUri)
		        .path("projects/" + project1.getName() +
		              "/tasks")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		// It's need some delay for manipulating file IO (productBacklogLogic.removeStoryFromSprint)
		Thread.sleep(1000);
		// Call '/projects/{projectName}/stories/{storyId}/tasks' API
		response = mClient.target(mBaseUri)
		        .path("projects/" + project1.getName() +
		        	  "/stories/" + story1.getIssueID() + 
		        	  "/tasks")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		// Call '/projects/{projectName}/releases' API
		response = mClient.target(mBaseUri)
		        .path("projects/" + project1.getName() +
		              "/releases")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		// Call '/accounts' API
		response = mClient.target(mBaseUri)
		        .path("accounts")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
	}
}
