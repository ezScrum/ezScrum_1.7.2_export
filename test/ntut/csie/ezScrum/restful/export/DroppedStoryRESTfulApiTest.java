package ntut.csie.ezScrum.restful.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
import ntut.csie.ezScrum.restful.export.jsonEnum.AccountJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.AttachFileJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.HistoryJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.StoryJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.TagJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.TaskJSONEnum;
import ntut.csie.ezScrum.restful.export.support.FileEncoder;
import ntut.csie.ezScrum.test.CreateData.AddStoryToSprint;
import ntut.csie.ezScrum.test.CreateData.AddTaskToStory;
import ntut.csie.ezScrum.test.CreateData.CopyProject;
import ntut.csie.ezScrum.test.CreateData.CreateAccount;
import ntut.csie.ezScrum.test.CreateData.CreateProductBacklog;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.test.CreateData.CreateTask;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.ezScrum.web.dataObject.TaskObject;
import ntut.csie.ezScrum.web.helper.ProductBacklogHelper;
import ntut.csie.ezScrum.web.helper.SprintBacklogHelper;
import ntut.csie.ezScrum.web.logic.ProductBacklogLogic;
import ntut.csie.ezScrum.web.mapper.ProductBacklogMapper;
import ntut.csie.ezScrum.web.mapper.SprintBacklogMapper;
import ntut.csie.jcis.resource.core.IProject;

public class DroppedStoryRESTfulApiTest extends JerseyTest {
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
		mATTS = null;
	}
	
	@Test
	public void testGetDroppedStories() throws JSONException, InterruptedException {
		IProject project = mCP.getProjectList().get(0);
		IIssue story2 = mASTS.getIssueList().get(1);
		String tagName1 = "Data Migration";
		String tagName2 = "Thesis";
		
		// Add Story Tag
		ProductBacklogHelper productBacklogHelper = new ProductBacklogHelper(null, project);
		productBacklogHelper.addNewTag(tagName1);
		productBacklogHelper.addNewTag(tagName2);
		IIssueTag tag1 = productBacklogHelper.getTagByName(tagName1);
		productBacklogHelper.addStoryTag(String.valueOf(story2.getIssueID()), String.valueOf(tag1.getTagId()));
		IIssueTag tag2 = productBacklogHelper.getTagByName(tagName2);
		productBacklogHelper.addStoryTag(String.valueOf(story2.getIssueID()), String.valueOf(tag2.getTagId()));
		
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

		assertEquals(story2.getIssueID(), jsonResponse.getJSONObject(0).get(StoryJSONEnum.ID));
		assertEquals(story2.getSummary(), jsonResponse.getJSONObject(0).get(StoryJSONEnum.NAME));
		assertEquals(story2.getStatus(), jsonResponse.getJSONObject(0).get(StoryJSONEnum.STATUS));
		assertEquals(Integer.parseInt(story2.getEstimated()), jsonResponse.getJSONObject(0).get(StoryJSONEnum.ESTIMATE));
		assertEquals(Integer.parseInt(story2.getImportance()), jsonResponse.getJSONObject(0).get(StoryJSONEnum.IMPORTANCE));
		assertEquals(Integer.parseInt(story2.getValue()), jsonResponse.getJSONObject(0).get(StoryJSONEnum.VALUE));
		assertEquals(story2.getNotes(), jsonResponse.getJSONObject(0).get(StoryJSONEnum.NOTES));
		assertEquals(story2.getHowToDemo(), jsonResponse.getJSONObject(0).get(StoryJSONEnum.HOW_TO_DEMO));
		
		JSONArray tagsJSONArray = jsonResponse.getJSONObject(0).getJSONArray(StoryJSONEnum.TAGS);
		assertEquals(2, tagsJSONArray.length());
		assertEquals(tagName1, tagsJSONArray.getJSONObject(0).getString(TagJSONEnum.NAME));
		assertEquals(tagName2, tagsJSONArray.getJSONObject(1).getString(TagJSONEnum.NAME));
		
	}
	
	@Test
	public void testGetTasksInDroppedStory() throws InterruptedException, JSONException {
		IProject project = mCP.getProjectList().get(0);
		IIssue story1 = mASTS.getIssueList().get(0);
		IIssue task1 = mATTS.getTaskList().get(0);
		ProductBacklogLogic productBacklogLogic = new ProductBacklogLogic(null, project);
		long story1Id = story1.getIssueID();
		
		// Add Partners to Task
		String partners = mCA.getAccountList().get(0).getName() + ";" + mCA.getAccountList().get(1).getName();
		SprintBacklogHelper sprintBacklogHelper = new SprintBacklogHelper(project, null);
		sprintBacklogHelper.checkOutTask(task1.getIssueID(), task1.getSummary(), task1.getAssignto(), partners, "", null);
		task1 = sprintBacklogHelper.getIssue(task1.getIssueID());
		
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
		JSONObject task1JSON = jsonResponse.getJSONObject(0);
		assertEquals(task1.getSummary(), task1JSON.getString(TaskJSONEnum.NAME));
		assertEquals(task1.getAssignto(), task1JSON.getString(TaskJSONEnum.HANDLER));
		assertEquals(Integer.parseInt(task1.getEstimated()), task1JSON.getInt(TaskJSONEnum.ESTIMATE));
		assertEquals(Integer.parseInt(task1.getRemains()), task1JSON.getInt(TaskJSONEnum.REMAIN));
		assertEquals(Integer.parseInt(task1.getActualHour()), task1JSON.getInt(TaskJSONEnum.ACTUAL));
		assertEquals(task1.getNotes(), task1JSON.getString(TaskJSONEnum.NOTES));
		assertEquals(task1.getStatus(), task1JSON.getString(TaskJSONEnum.STATUS));
		JSONArray task1PartnersJSONArray = task1JSON.getJSONArray(TaskJSONEnum.PARTNERS);
		assertEquals(mCA.getAccountList().get(0).getName(), task1PartnersJSONArray.getJSONObject(0).getString(AccountJSONEnum.USERNAME));
		assertEquals(mCA.getAccountList().get(1).getName(), task1PartnersJSONArray.getJSONObject(1).getString(AccountJSONEnum.USERNAME));
		
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
		assertEquals("RoleBase.xml", jsonResponse.getJSONObject(0).getString(AttachFileJSONEnum.NAME));
		assertEquals("text/xml", jsonResponse.getJSONObject(0).getString(AttachFileJSONEnum.CONTENT_TYPE));
		assertEquals(expectedXmlBinary1, jsonResponse.getJSONObject(0).getString(AttachFileJSONEnum.BINARY));

		assertEquals("ScrumRole.xml", jsonResponse.getJSONObject(1).getString(AttachFileJSONEnum.NAME));
		assertEquals("text/xml", jsonResponse.getJSONObject(1).getString(AttachFileJSONEnum.CONTENT_TYPE));
		assertEquals(expectedXmlBinary2, jsonResponse.getJSONObject(1).getString(AttachFileJSONEnum.BINARY));
	}
	
	@Test
	public void testGetHistoriesInDroppedStory_CreateStory() throws JSONException, InterruptedException {
		// Test Data
		IProject project = mCP.getProjectList().get(0);
		IIssue story = mASTS.getIssueList().get(0);

		// It's need some delay for manipulating file IO (add story to sprint)
		Thread.sleep(1000);
		ProductBacklogLogic productBacklogLogic = new ProductBacklogLogic(null, project);
		productBacklogLogic.removeStoryFromSprint(story.getIssueID());
		// It's need some delay for manipulating file IO (productBacklogLogic.removeStoryFromSprint)
		Thread.sleep(1000);
		
		// Call '/projects/{projectName}/stories/{storyId}/histories' API
		Response response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() +
		                "/stories/" + story.getIssueID()
		                + "/histories")
		        .request()
		        .get();

		JSONArray jsonResponse = new JSONArray(response.readEntity(String.class));

		// Assert
		assertEquals(9, jsonResponse.length());
		Integer[] expectedTypeArray = new Integer[] {HistoryJSONEnum.TYPE_APPEND, HistoryJSONEnum.TYPE_CREATE, HistoryJSONEnum.TYPE_VALUE, HistoryJSONEnum.TYPE_IMPORTANCE, HistoryJSONEnum.TYPE_ESTIMATE, HistoryJSONEnum.TYPE_REMOVE, HistoryJSONEnum.TYPE_ADD};
		List<Integer> expectedTypes = Arrays.asList(expectedTypeArray);
		for (int i = 0; i < jsonResponse.length(); i++) {
			JSONObject json = jsonResponse.getJSONObject(i);
			assertTrue(expectedTypes.contains(json.getInt(HistoryJSONEnum.HISTORY_TYPE)));
		}
	}
	
	@Test
	public void testGetHistoriesInDroppedStory_ModifyStoryInformation() throws JSONException, InterruptedException {
		// Test Data
		IProject project = mCP.getProjectList().get(0);
		IIssue story = mASTS.getIssueList().get(0);
		
		ProductBacklogHelper productBacklogHelper = new ProductBacklogHelper(null, project);
		String Name = "Edited Name";
		String Value = "33";
		String Importance = "44";
		String Estimated = "55";
		productBacklogHelper.editStory(story.getIssueID(), Name, story.getValue(), story.getImportance(), story.getEstimated(), story.getHowToDemo(), story.getNotes());
		Thread.sleep(1000);
		productBacklogHelper.editStory(story.getIssueID(), Name, Value, story.getImportance(), story.getEstimated(), story.getHowToDemo(), story.getNotes());
		Thread.sleep(1000);
		productBacklogHelper.editStory(story.getIssueID(), Name, Value, Importance, story.getEstimated(), story.getHowToDemo(), story.getNotes());
		Thread.sleep(1000);
		productBacklogHelper.editStory(story.getIssueID(), Name, Value, Importance, Estimated, story.getHowToDemo(), story.getNotes());

		// It's need some delay for manipulating file IO (add story to sprint)
		Thread.sleep(1000);
		ProductBacklogLogic productBacklogLogic = new ProductBacklogLogic(null, project);
		productBacklogLogic.removeStoryFromSprint(story.getIssueID());
		// It's need some delay for manipulating file IO (productBacklogLogic.removeStoryFromSprint)
		Thread.sleep(1000);
		
		// Call '/projects/{projectName}/stories/{storyId}/histories' API
		Response response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() +
		                "/stories/" + story.getIssueID()
		                + "/histories")
		        .request()
		        .get();

		JSONArray jsonResponse = new JSONArray(response.readEntity(String.class));

		// Assert
		assertEquals(13, jsonResponse.length());
		Integer[] expectedTypeArray = new Integer[] {HistoryJSONEnum.TYPE_APPEND, HistoryJSONEnum.TYPE_CREATE, HistoryJSONEnum.TYPE_VALUE, HistoryJSONEnum.TYPE_IMPORTANCE, HistoryJSONEnum.TYPE_ESTIMATE, HistoryJSONEnum.TYPE_REMOVE, HistoryJSONEnum.TYPE_ADD, HistoryJSONEnum.TYPE_NAME};
		List<Integer> expectedTypes = Arrays.asList(expectedTypeArray);
		for (int i = 0; i < jsonResponse.length(); i++) {
			JSONObject json = jsonResponse.getJSONObject(i);
			assertTrue(expectedTypes.contains(json.getInt(HistoryJSONEnum.HISTORY_TYPE)));
		}
	}
	
	@Test
	public void testGetHistoriesInDroppedStory_ModifyRelation() throws Exception {
		// Test Data
		IProject project = mCP.getProjectList().get(0);
		IIssue story = mASTS.getIssueList().get(0);

		CreateTask CT = new CreateTask(1, mCP);
		CT.exe();
		SprintBacklogHelper sprintBacklogHelper = new SprintBacklogHelper(project, null);
		String taskId = String.valueOf(CT.getTaskIDList().get(0));
		sprintBacklogHelper.addExistedTask(String.valueOf(story.getIssueID()), new String[] {taskId});
		Thread.sleep(1000);
		sprintBacklogHelper.removeTask(CT.getTaskIDList().get(0), story.getIssueID());
		Thread.sleep(1000);
		
		// It's need some delay for manipulating file IO (add story to sprint)
		Thread.sleep(1000);
		ProductBacklogLogic productBacklogLogic = new ProductBacklogLogic(null, project);
		productBacklogLogic.removeStoryFromSprint(story.getIssueID());
		// It's need some delay for manipulating file IO (productBacklogLogic.removeStoryFromSprint)
		Thread.sleep(1000);
		
		// Call '/projects/{projectName}/stories/{storyId}/histories' API
		Response response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() +
		                "/stories/" + story.getIssueID()
		                + "/histories")
		        .request()
		        .get();

		JSONArray jsonResponse = new JSONArray(response.readEntity(String.class));

		// Assert histories
		Integer[] expectedTypeArray = new Integer[] {HistoryJSONEnum.TYPE_APPEND, HistoryJSONEnum.TYPE_CREATE, HistoryJSONEnum.TYPE_VALUE, HistoryJSONEnum.TYPE_IMPORTANCE, HistoryJSONEnum.TYPE_ESTIMATE, HistoryJSONEnum.TYPE_ADD, HistoryJSONEnum.TYPE_REMOVE};
		List<Integer> expectedTypes = Arrays.asList(expectedTypeArray);
		for (int i = 0; i < jsonResponse.length(); i++) {
			JSONObject json = jsonResponse.getJSONObject(i);
			assertTrue(expectedTypes.contains(json.getInt(HistoryJSONEnum.HISTORY_TYPE)));
		}
	}
	
	@Test
	public void testGetAttachFilesInTask() throws InterruptedException, JSONException {
		// Test Data
		String testFile1 = "./TestData/RoleBase.xml";
		String testFile2 = "./TestData/InitialData/ScrumRole.xml";
		IProject project = mCP.getProjectList().get(0);
		IIssue story = mASTS.getIssueList().get(0);
		IIssue task = mATTS.getTaskList().get(0);

		// Upload Attach File
		SprintBacklogMapper sprintBacklogMapper = new SprintBacklogMapper(project, null);
		sprintBacklogMapper.addAttachFile(task.getIssueID(), testFile1);
		sprintBacklogMapper.addAttachFile(task.getIssueID(), testFile2);
		
		// Remove story from Sprint
		ProductBacklogLogic productBacklogLogic = new ProductBacklogLogic(null, project);
		// It's need some delay for manipulating file IO (add story to sprint)
		Thread.sleep(1000);
		productBacklogLogic.removeStoryFromSprint(story.getIssueID());
		// It's need some delay for manipulating file IO (productBacklogLogic.removeStoryFromSprint)
		Thread.sleep(1000);

		// Call '/projects/{projectName}/stories/{storyId}/tasks/{taskId}/attachfiles' API
		Response response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() +
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
	public void testGetHistoriesInTask_CreateTask() throws InterruptedException, JSONException {
		// Test Data
		IProject project = mCP.getProjectList().get(0);
		IIssue story = mASTS.getIssueList().get(0);
		IIssue task = mATTS.getTaskList().get(0);
		
		SprintBacklogHelper sprintBacklogHelper = new SprintBacklogHelper(project, null);
		TaskObject taskObject = new TaskObject();
		taskObject.name = "new name";
		taskObject.actual = "11";
		taskObject.estimation = "22";
		taskObject.remains = "33";
		taskObject.id = String.valueOf(task.getIssueID());
		sprintBacklogHelper.editTask(taskObject);
		Thread.sleep(1000);
		
		// Remove story from Sprint
		ProductBacklogLogic productBacklogLogic = new ProductBacklogLogic(null, project);
		// It's need some delay for manipulating file IO (add story to sprint)
		Thread.sleep(1000);
		productBacklogLogic.removeStoryFromSprint(story.getIssueID());
		// It's need some delay for manipulating file IO (productBacklogLogic.removeStoryFromSprint)
		Thread.sleep(1000);

		// Call '/projects/{projectName}/stories/{storyId}/tasks/{taskId}/histories' API
		Response response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() +
		                "/stories/" + story.getIssueID() +
		                "/tasks/" + task.getIssueID() +
		                "/histories")
		        .request()
		        .get();

		JSONArray jsonResponse = new JSONArray(response.readEntity(String.class));

		// Assert
		assertEquals(8, jsonResponse.length());
		Integer[] expectedTypeArray = new Integer[] {HistoryJSONEnum.TYPE_APPEND, HistoryJSONEnum.TYPE_CREATE, HistoryJSONEnum.TYPE_ESTIMATE, HistoryJSONEnum.TYPE_REMAIMS, HistoryJSONEnum.TYPE_ACTUAL, HistoryJSONEnum.TYPE_NAME};
		List<Integer> expectedTypes = Arrays.asList(expectedTypeArray);
		for (int i = 0; i < jsonResponse.length(); i++) {
			JSONObject json = jsonResponse.getJSONObject(i);
			assertTrue(expectedTypes.contains(json.getInt(HistoryJSONEnum.HISTORY_TYPE)));
		}
	}
}
