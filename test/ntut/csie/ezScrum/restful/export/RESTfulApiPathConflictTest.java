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
import ntut.csie.ezScrum.test.CreateData.CreateRetrospective;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.test.CreateData.CreateUnplannedItem;
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
	private CreateUnplannedItem mCU;
	private CreateRetrospective mCRE;
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
		                                     ReleaseRESTfulApi.class, AccountRESTfulApi.class, RetrospectiveRESTfulApi.class, UnplanRESTfulApi.class);
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
		
		// Add Story to sprint
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
		mCU = null;
		mCR = null;
		mCRE = null;
		mASTS = null;
		mATTS = null;
		mHttpServer = null;
		mClient = null;
	}
	
	@Test
	public void testIsPathConflict() throws InterruptedException {
		// Test Data
		// Project 1
		IProject project = mCP.getProjectList().get(0);
		String sprintId1 = mCS.getSprintIDList().get(0);
		IIssue story1 = mASTS.getIssueList().get(0);
		IIssue story2 = mASTS.getIssueList().get(1);
		IIssue task1 = mATTS.getTaskList().get(0);
		IIssue task2 = mATTS.getTaskList().get(1);
		IIssue task3 = mATTS.getTaskList().get(2);
		IIssue task4 = mATTS.getTaskList().get(3);
		IIssue unplan1 = mCU.getIssueList().get(0);
		
		// Api Test
		// Call '/accounts' API
		Response response = mClient.target(mBaseUri)
		        .path("accounts")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		// Call '/projects' API
		response = mClient.target(mBaseUri)
		        .path("projects")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		// Call '/projects/{projectName}/tags' API
		response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() + "/tags")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		// Call '/projects/{projectName}/scrumroles' API
		response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() + "/scrumroles")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		// Call '/projects/{projectName}/projectroles' API
		response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() + "/projectroles")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		// Call '/projects/{projectName}/sprints' API
		response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() + "/sprints")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		// Call '/projects/{projectName}/sprints/{sprintId}/stories' API
		response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() +
		              "/sprints/" + sprintId1 +
		              "/stories")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		// Call '/projects/{projectName}/sprints/{sprintId}/stories/{storyId}/histories' API
		response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() +
		                "/sprints/" + sprintId1 +
		                "/stories/" + story2.getIssueID() +
		                "/histories")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		// Call '/projects/{projectName}/sprints/{sprintId}/stories/{storyId}/attachfiles' API
		response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() +
		              "/sprints/" + sprintId1 +
		              "/stories/" + story2.getIssueID() +
		              "/attachfiles")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		// Call '/projects/{projectName}/sprints/{sprintId}/stories/{storyId}/tasks' API
		response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() +
		              "/sprints/" + sprintId1 +
		              "/stories/" + story2.getIssueID() +
		              "/tasks")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		// Call '/projects/{projectName}/sprints/{sprintId}/stories/{storyId}/tasks/{taskId}/histories' API
		response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() +
		               "/sprints/" + sprintId1 +
		               "/stories/" + story2.getIssueID() +
		               "/tasks/" + task4.getIssueID() +
		               "/histories")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		// Call '/projects/{projectName}/sprints/{sprintId}/stories/{storyId}/tasks/{taskId}/attachfiles' API
		response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() +
		                "/sprints/" + sprintId1 +
		                "/stories/" + story2.getIssueID() +
		                "/tasks/" + task4.getIssueID() +
		                "/attachfiles")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		// Call '/projects/{projectName}/releases' API
		response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() +
		              "/releases")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		// Call '/projects/{projectName}/sprints/{sprintId}/retrospectives' API
		response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() + "/sprints/" + sprintId1 + "/retrospectives")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		//// Unplan Resource Test
		// Call '/projects/{projectName}/sprints/{sprintId}/unplans' API
		response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() +
		                "/sprints/" + unplan1.getSprintID() +
		                "/unplans")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		// Call '/projects/{projectName}/sprints/{sprintId}/unplans/{unplanId}/histories' API
		response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() +
		        	  "/sprints/" + sprintId1 + 
		              "/unplans/" + unplan1.getIssueID() +
		              "/histories")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		//// Dropped Resource Test
		// Drop Story1
		ProductBacklogLogic productBacklogLogic = new ProductBacklogLogic(null, project);

		// It's need some delay for manipulating file IO (add story to sprint)
		Thread.sleep(1000);
		// Remove story1 from Sprint
		productBacklogLogic.removeStoryFromSprint(story1.getIssueID());
		// It's need some delay for manipulating file IO (productBacklogLogic.removeStoryFromSprint)
		Thread.sleep(1000);

		// Drop task1, task3 from story
		SprintBacklogHelper sprintBacklogHelper = new SprintBacklogHelper(project, null);
		sprintBacklogHelper.removeTask(task1.getIssueID(), story1.getIssueID());
		sprintBacklogHelper.removeTask(task3.getIssueID(), story2.getIssueID());
		
		// Call '/projects/{projectName}/stories' API
		response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() +
		                "/stories")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		// Call '/projects/{projectName}/stories/{storyId}/tasks' API
		response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() +
		              "/stories/" + story1.getIssueID() +
		              "/tasks")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		// Call '/projects/{projectName}/stories/{storyId}/histories' API
		response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() +
		               "/stories/" + story1.getIssueID() +
		               "/histories")
		        .request()
		        .get();
	    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		// Call '/projects/{projectName}/stories/{storyId}/attachfiles' API
		response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() +
		              "/stories/" + story1.getIssueID() + 
		              "/attachfiles")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		// Call '/projects/{projectName}/stories/{storyId}/tasks/{taskId}/histories' API
		response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() +
		                "/stories/" + story1.getIssueID() +
		                "/tasks/" + task2.getIssueID() +
		                "/histories")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

		// Call '/projects/{projectName}/stories/{storyId}/tasks/{taskId}/attachfiles' API
		response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() +
		              "/stories/" + story1.getIssueID() +
		              "/tasks/" + task2.getIssueID() +
		              "/attachfiles")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

		// Call '/projects/{projectName}/tasks' API
		response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() +
		              "/tasks")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		// Call '/projects/{projectName}/tasks/{taskId}/histories' API
		response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() +
		              "/tasks/" + task1.getIssueID() +
		              "/histories")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
				
		// Call '/projects/{projectName}/tasks/{taskId}/attachfiles' API
		response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() +
		              "/tasks/" + task1.getIssueID() +
		              "/attachfiles")
		        .request()
		        .get();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
	}
}
