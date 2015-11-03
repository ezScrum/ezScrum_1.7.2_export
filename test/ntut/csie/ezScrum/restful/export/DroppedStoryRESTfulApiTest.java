package ntut.csie.ezScrum.restful.export;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URI;

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
import ntut.csie.ezScrum.issue.core.IIssueTag;
import ntut.csie.ezScrum.test.CreateData.AddStoryToSprint;
import ntut.csie.ezScrum.test.CreateData.AddTaskToStory;
import ntut.csie.ezScrum.test.CreateData.CopyProject;
import ntut.csie.ezScrum.test.CreateData.CreateProductBacklog;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.ezScrum.web.databaseEnum.AttachFileEnum;
import ntut.csie.ezScrum.web.databaseEnum.StoryEnum;
import ntut.csie.ezScrum.web.databaseEnum.TagEnum;
import ntut.csie.ezScrum.web.helper.ProductBacklogHelper;
import ntut.csie.ezScrum.web.logic.ProductBacklogLogic;
import ntut.csie.ezScrum.web.mapper.ProductBacklogMapper;
import ntut.csie.ezScrum.web.mapper.SprintBacklogMapper;
import ntut.csie.ezScrum.web.support.export.FileEncoder;
import ntut.csie.jcis.resource.core.IProject;

public class DroppedStoryRESTfulApiTest extends JerseyTest {
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
		mResourceConfig = new ResourceConfig(DroppedStoryRESTfulApi.class);
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
		mHttpServer = null;
		mResourceConfig = null;
		mBaseUri = null;
		mClient = null;
		mATTS = null;
	}
	
	@Test
	public void testGetDroppedStories() throws JSONException, InterruptedException {
		IProject project = mCP.getProjectList().get(0);
		IIssue story2 = mASTS.getIssueList().get(1);
		// Remove story2 from Sprint
		ProductBacklogLogic productBacklogLogic = new ProductBacklogLogic(null, project);
		//將Story自Sprint移除, 
		long story2Id = story2.getIssueID();
		// It's need some delay for manipulating file IO (add story to sprint)
		Thread.sleep(1000);
		productBacklogLogic.removeStoryFromSprint(story2Id);
		// It's need some delay for manipulating file IO (productBacklogLogic.removeStoryFromSprint)
		Thread.sleep(1000);
		
		// Call '/projects/{projectName}/stories' API
		Response response = mClient.target(mBaseUri)
		                           .path("projects/" +  project.getName() + "/stories")
		                           .request()
		                           .get();
		JSONArray jsonResponse = new JSONArray(response.readEntity(String.class));

		// Assert
		assertEquals(1, jsonResponse.length());

		assertEquals(story2.getIssueID(), jsonResponse.getJSONObject(0).get(StoryEnum.ID));
		assertEquals(story2.getSummary(), jsonResponse.getJSONObject(0).get(StoryEnum.NAME));
		assertEquals(story2.getStatus(), jsonResponse.getJSONObject(0).get(StoryEnum.STATUS));
		assertEquals(Integer.parseInt(story2.getEstimated()), jsonResponse.getJSONObject(0).get(StoryEnum.ESTIMATE));
		assertEquals(Integer.parseInt(story2.getImportance()), jsonResponse.getJSONObject(0).get(StoryEnum.IMPORTANCE));
		assertEquals(Integer.parseInt(story2.getValue()), jsonResponse.getJSONObject(0).get(StoryEnum.VALUE));
		assertEquals(story2.getNotes(), jsonResponse.getJSONObject(0).get(StoryEnum.NOTES));
		assertEquals(story2.getHowToDemo(), jsonResponse.getJSONObject(0).get(StoryEnum.HOW_TO_DEMO));
	}
	
	@Test
	public void testGetTasksInDroppedStory() throws InterruptedException, JSONException {
		IProject project = mCP.getProjectList().get(0);
		IIssue story1 = mASTS.getIssueList().get(0);
		ProductBacklogLogic productBacklogLogic = new ProductBacklogLogic(null, project);
		long story1Id = story1.getIssueID();
		// It's need some delay for manipulating file IO (add story to sprint)
		Thread.sleep(1000);
		productBacklogLogic.removeStoryFromSprint(story1Id);
		// It's need some delay for manipulating file IO (productBacklogLogic.removeStoryFromSprint)
		Thread.sleep(1000);
		
		// Call '/projects/{projectName}/stories/{storyId}/tasks' API
		Response response = mClient.target(mBaseUri)
		                           .path("projects/" +  project.getName() + "/stories/" + story1Id + "/tasks")
		                           .request()
		                           .get();
		
		JSONArray jsonResponse = new JSONArray(response.readEntity(String.class));
		// Assert
		assertEquals(2, jsonResponse.length());
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		// Call '/projects/{projectName}/stories/{storyId}/tasks' API
		IIssue story2 = mASTS.getIssueList().get(1);
		long story2Id = story2.getIssueID();
		response = mClient.target(mBaseUri)
                           .path("projects/" +  project.getName() + "/stories/" + story2Id + "/tasks")
                           .request()
                           .get();
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void testGetAttachFilesInDroppedStory() throws JSONException, InterruptedException {
		// Test Data
		String testFile1 = "./TestData/RoleBase.xml";
		String testFile2 = "./TestData/InitialData/ScrumRole.xml";
		IProject project = mCP.getProjectList().get(0);
		IIssue story = mASTS.getIssueList().get(0);

		// It's need some delay for manipulating file IO (add story to sprint)
		Thread.sleep(1000);
		ProductBacklogLogic productBacklogLogic = new ProductBacklogLogic(null, project);
		productBacklogLogic.removeStoryFromSprint(story.getIssueID());
		// It's need some delay for manipulating file IO (productBacklogLogic.removeStoryFromSprint)
		Thread.sleep(1000);
		
		// Upload Attach File
		ProductBacklogMapper productBacklogMapper = new ProductBacklogMapper(project, null);
		productBacklogMapper.addAttachFile(story.getIssueID(), testFile1);
		productBacklogMapper.addAttachFile(story.getIssueID(), testFile2);
		
		// Call '/projects/{projectName}/stories/{storyId}/attachfiles' API
		Response response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() +
		                "/stories/" + story.getIssueID()
		                + "/attachfiles")
		        .request()
		        .get();

		JSONArray jsonResponse = new JSONArray(response.readEntity(String.class));
		String expectedXmlBinary1 = FileEncoder.toBase64BinaryString(new File(testFile1));
		String expectedXmlBinary2 = FileEncoder.toBase64BinaryString(new File(testFile2));

		// Assert
		assertEquals(2, jsonResponse.length());
		assertEquals("RoleBase.xml", jsonResponse.getJSONObject(0).getString(AttachFileEnum.NAME));
		assertEquals("text/xml", jsonResponse.getJSONObject(0).getString(AttachFileEnum.CONTENT_TYPE));
		assertEquals(expectedXmlBinary1, jsonResponse.getJSONObject(0).getString(AttachFileEnum.BINARY));

		assertEquals("ScrumRole.xml", jsonResponse.getJSONObject(1).getString(AttachFileEnum.NAME));
		assertEquals("text/xml", jsonResponse.getJSONObject(1).getString(AttachFileEnum.CONTENT_TYPE));
		assertEquals(expectedXmlBinary2, jsonResponse.getJSONObject(1).getString(AttachFileEnum.BINARY));
	}
	
	@Test
	public void testGetTagsInDroppedStory() throws JSONException, InterruptedException {
		String tagName1 = "Data Migration";
		String tagName2 = "Thesis";
		IProject project = mCP.getProjectList().get(0);
		IIssue story = mASTS.getIssueList().get(0);
		
		// It's need some delay for manipulating file IO (add story to sprint)
		Thread.sleep(1000);
		ProductBacklogLogic productBacklogLogic = new ProductBacklogLogic(null, project);
		productBacklogLogic.removeStoryFromSprint(story.getIssueID());
		// It's need some delay for manipulating file IO (productBacklogLogic.removeStoryFromSprint)
		Thread.sleep(1000);
				
		ProductBacklogHelper productBacklogHelper = new ProductBacklogHelper(null, project);
		productBacklogHelper.addNewTag(tagName1);
		productBacklogHelper.addNewTag(tagName2);
		IIssueTag tag1 = productBacklogHelper.getTagByName(tagName1);
		productBacklogHelper.addStoryTag(String.valueOf(story.getIssueID()), String.valueOf(tag1.getTagId()));
		IIssueTag tag2 = productBacklogHelper.getTagByName(tagName2);
		productBacklogHelper.addStoryTag(String.valueOf(story.getIssueID()), String.valueOf(tag2.getTagId()));
		
		// Call '/projects/{projectName}/stories/{storyId}/tags' API
		Response response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() +
		                "/stories/" + story.getIssueID()
		                + "/tags")
		        .request()
		        .get();

		JSONArray jsonResponse = new JSONArray(response.readEntity(String.class));
		
		// Assert
		assertEquals(2, jsonResponse.length());
		assertEquals(tagName1, jsonResponse.getJSONObject(0).getString(TagEnum.NAME));
		assertEquals(tagName2, jsonResponse.getJSONObject(1).getString(TagEnum.NAME));
	}
	
	@Test
	public void testGetAttachFilesInTask() {
		
	}
	
	@Test
	public void testGetPartnersInTask() {
		
	}
}
