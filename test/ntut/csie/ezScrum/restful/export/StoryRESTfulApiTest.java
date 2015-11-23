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
import ntut.csie.ezScrum.restful.export.jsonEnum.AttachFileJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.HistoryJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.StoryJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.TagJSONEnum;
import ntut.csie.ezScrum.restful.export.support.FileEncoder;
import ntut.csie.ezScrum.restful.export.support.JSONEncoder;
import ntut.csie.ezScrum.test.CreateData.AddStoryToSprint;
import ntut.csie.ezScrum.test.CreateData.CopyProject;
import ntut.csie.ezScrum.test.CreateData.CreateProductBacklog;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.ezScrum.web.dataObject.TaskObject;
import ntut.csie.ezScrum.web.helper.ProductBacklogHelper;
import ntut.csie.ezScrum.web.helper.SprintBacklogHelper;
import ntut.csie.ezScrum.web.mapper.SprintBacklogMapper;
import ntut.csie.jcis.resource.core.IProject;

public class StoryRESTfulApiTest extends JerseyTest {
	private ezScrumInfoConfig mConfig = new ezScrumInfoConfig();
	private CreateProject mCP;
	private CreateSprint mCS;
	private AddStoryToSprint mASTS;

	private Client mClient;
	private HttpServer mHttpServer;
	private ResourceConfig mResourceConfig;
	private static String BASE_URL = "http://localhost:8080/ezScrum/resource/";
	private URI mBaseUri = URI.create(BASE_URL);

	@Override
	protected Application configure() {
		mResourceConfig = new ResourceConfig(StoryRESTfulApi.class);
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
	public void testGetStoriesInSprint() throws JSONException {
		IProject project = mCP.getProjectList().get(0);
		String sprintId = mCS.getSprintIDList().get(0);
		IIssue story1 = mASTS.getIssueList().get(0);
		IIssue story2 = mASTS.getIssueList().get(1);
		String tagName1 = "Data Migration";
		String tagName2 = "Thesis";

		// Add Tags to Story1
		ProductBacklogHelper productBacklogHelper = new ProductBacklogHelper(null, project);
		productBacklogHelper.addNewTag(tagName1);
		productBacklogHelper.addNewTag(tagName2);
		IIssueTag tag1 = productBacklogHelper.getTagByName(tagName1);
		productBacklogHelper.addStoryTag(String.valueOf(story1.getIssueID()), String.valueOf(tag1.getTagId()));
		IIssueTag tag2 = productBacklogHelper.getTagByName(tagName2);
		productBacklogHelper.addStoryTag(String.valueOf(story1.getIssueID()), String.valueOf(tag2.getTagId()));

		// Call '/projects/{projectName}/sprints/{sprintId}/stories' API
		Response response = mClient.target(mBaseUri)
				.path("projects/" + project.getName() + "/sprints/" + sprintId + "/stories/").request().get();

		JSONArray jsonResponse = new JSONArray(response.readEntity(String.class));

		// Assert
		assertEquals(2, jsonResponse.length());
		assertEquals(story1.getIssueID(), jsonResponse.getJSONObject(0).get(StoryJSONEnum.ID));
		assertEquals(story1.getSummary(), jsonResponse.getJSONObject(0).get(StoryJSONEnum.NAME));
		assertEquals(story1.getStatus(), jsonResponse.getJSONObject(0).get(StoryJSONEnum.STATUS));
		assertEquals(Integer.parseInt(story1.getEstimated()),
				jsonResponse.getJSONObject(0).get(StoryJSONEnum.ESTIMATE));
		assertEquals(Integer.parseInt(story1.getImportance()),
				jsonResponse.getJSONObject(0).get(StoryJSONEnum.IMPORTANCE));
		assertEquals(Integer.parseInt(story1.getValue()), jsonResponse.getJSONObject(0).get(StoryJSONEnum.VALUE));
		assertEquals(story1.getNotes(), jsonResponse.getJSONObject(0).get(StoryJSONEnum.NOTES));
		assertEquals(story1.getHowToDemo(), jsonResponse.getJSONObject(0).get(StoryJSONEnum.HOW_TO_DEMO));

		JSONArray tagsJSONArray = jsonResponse.getJSONObject(0).getJSONArray(StoryJSONEnum.TAGS);
		assertEquals(2, tagsJSONArray.length());
		assertEquals(tagName1, tagsJSONArray.getJSONObject(0).getString(TagJSONEnum.NAME));
		assertEquals(tagName2, tagsJSONArray.getJSONObject(1).getString(TagJSONEnum.NAME));

		assertEquals(story2.getIssueID(), jsonResponse.getJSONObject(1).get(StoryJSONEnum.ID));
		assertEquals(story2.getSummary(), jsonResponse.getJSONObject(1).get(StoryJSONEnum.NAME));
		assertEquals(story2.getStatus(), jsonResponse.getJSONObject(1).get(StoryJSONEnum.STATUS));
		assertEquals(Integer.parseInt(story2.getEstimated()),
				jsonResponse.getJSONObject(1).get(StoryJSONEnum.ESTIMATE));
		assertEquals(Integer.parseInt(story2.getImportance()),
				jsonResponse.getJSONObject(1).get(StoryJSONEnum.IMPORTANCE));
		assertEquals(Integer.parseInt(story2.getValue()), jsonResponse.getJSONObject(1).get(StoryJSONEnum.VALUE));
		assertEquals(story2.getNotes(), jsonResponse.getJSONObject(1).get(StoryJSONEnum.NOTES));
		assertEquals(story2.getHowToDemo(), jsonResponse.getJSONObject(1).get(StoryJSONEnum.HOW_TO_DEMO));
	}

	@Test
	public void testGetDroppedTaskAttachFiles() throws JSONException {
		// Test Data
		String testFile = "./TestData/RoleBase.xml";
		IProject project = mCP.getProjectList().get(0);
		String sprintId = mCS.getSprintIDList().get(0);
		IIssue story = mASTS.getIssueList().get(0);

		// Upload Attach File
		SprintBacklogMapper sprintBacklogMapper = new SprintBacklogMapper(project, null);
		sprintBacklogMapper.addAttachFile(story.getIssueID(), testFile);

		// Call
		// '/projects/{projectName}/sprints/{sprintId}/stories/{storyId}/attachfiles'
		// API
		Response response = mClient.target(mBaseUri).path("projects/" + project.getName() + "/sprints/" + sprintId
				+ "/stories/" + story.getIssueID() + "/attachfiles").request().get();

		JSONArray jsonResponse = new JSONArray(response.readEntity(String.class));
		String expectedXmlBinary = FileEncoder.toBase64BinaryString(new File(testFile));

		// Assert
		assertEquals(1, jsonResponse.length());
		assertEquals("RoleBase.xml", jsonResponse.getJSONObject(0).getString(AttachFileJSONEnum.NAME));
		assertEquals("text/xml", jsonResponse.getJSONObject(0).getString(AttachFileJSONEnum.CONTENT_TYPE));
		assertEquals(expectedXmlBinary, jsonResponse.getJSONObject(0).getString(AttachFileJSONEnum.BINARY));
	}

	@Test
	public void testGetDroppedTaskAttachFiles_MultipleFiles() throws JSONException {
		// Test Data
		String testFile1 = "./TestData/RoleBase.xml";
		String testFile2 = "./TestData/InitialData/ScrumRole.xml";
		IProject project = mCP.getProjectList().get(0);
		String sprintId = mCS.getSprintIDList().get(0);
		IIssue story = mASTS.getIssueList().get(0);

		// Upload Attach File
		SprintBacklogMapper sprintBacklogMapper = new SprintBacklogMapper(project, null);
		sprintBacklogMapper.addAttachFile(story.getIssueID(), testFile1);
		sprintBacklogMapper.addAttachFile(story.getIssueID(), testFile2);

		// Call
		// '/projects/{projectName}/sprints/{sprintId}/stories/{storyId}/attachfiles'
		// API
		Response response = mClient.target(mBaseUri).path("projects/" + project.getName() + "/sprints/" + sprintId
				+ "/stories/" + story.getIssueID() + "/attachfiles").request().get();

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
	public void testGetHistoriesInStory_CreateStory() throws JSONException {
		IProject project = mCP.getProjectList().get(0);
		String sprintId = mCS.getSprintIDList().get(0);
		IIssue story = mASTS.getIssueList().get(0);

		// Call
		// '/projects/{projectName}/sprints/{sprintId}/stories/{storyId}/histories'
		// API
		Response response = mClient.target(mBaseUri).path("projects/" + project.getName() + "/sprints/" + sprintId
				+ "/stories/" + story.getIssueID() + "/histories").request().get();

		JSONArray jsonResponse = new JSONArray(response.readEntity(String.class));

		SprintBacklogMapper sprintBacklogMapper = new SprintBacklogMapper(project, null);
		story = sprintBacklogMapper.getIssue(story.getIssueID());
		@SuppressWarnings("deprecation")
		JSONArray expectedResponse = JSONEncoder.toHistoryJSONArray(story.getIssueHistories(), story.getCategory());

		// Assert histories
		assertEquals(expectedResponse.length(), jsonResponse.length());
		for (int i = 0; i < expectedResponse.length(); i++) {
			assertEquals(expectedResponse.getJSONObject(i).getInt(HistoryJSONEnum.HISTORY_TYPE),
					jsonResponse.getJSONObject(i).getInt(HistoryJSONEnum.HISTORY_TYPE));
			assertEquals(expectedResponse.getJSONObject(i).getString(HistoryJSONEnum.OLD_VALUE),
					jsonResponse.getJSONObject(i).getString(HistoryJSONEnum.OLD_VALUE));
			assertEquals(expectedResponse.getJSONObject(i).getString(HistoryJSONEnum.NEW_VALUE),
					jsonResponse.getJSONObject(i).getString(HistoryJSONEnum.NEW_VALUE));
			assertEquals(expectedResponse.getJSONObject(i).getLong(HistoryJSONEnum.CREATE_TIME),
					jsonResponse.getJSONObject(i).getLong(HistoryJSONEnum.CREATE_TIME));
		}
	}

	@Test
	public void testGetHistoriesInStory_ModifyStoryInformation() throws JSONException, InterruptedException {
		IProject project = mCP.getProjectList().get(0);
		String sprintId = mCS.getSprintIDList().get(0);
		IIssue story = mASTS.getIssueList().get(0);

		ProductBacklogHelper productBacklogHelper = new ProductBacklogHelper(null, project);
		String Name = "Edited Name";
		String Value = "33";
		String Importance = "44";
		String Estimated = "55";
		Thread.sleep(1000);
		productBacklogHelper.editStory(story.getIssueID(), Name, Value, Importance, Estimated, story.getHowToDemo(),
				story.getNotes());

		// Call
		// '/projects/{projectName}/sprints/{sprintId}/stories/{storyId}/histories'
		// API
		Response response = mClient.target(mBaseUri).path("projects/" + project.getName() + "/sprints/" + sprintId
				+ "/stories/" + story.getIssueID() + "/histories").request().get();

		JSONArray jsonResponse = new JSONArray(response.readEntity(String.class));

		SprintBacklogMapper sprintBacklogMapper = new SprintBacklogMapper(project, null);
		story = sprintBacklogMapper.getIssue(story.getIssueID());
		@SuppressWarnings("deprecation")
		JSONArray expectedResponse = JSONEncoder.toHistoryJSONArray(story.getIssueHistories(), story.getCategory());

		// Assert histories
		assertEquals(expectedResponse.length(), jsonResponse.length());
		for (int i = 0; i < expectedResponse.length(); i++) {
			assertEquals(expectedResponse.getJSONObject(i).getInt(HistoryJSONEnum.HISTORY_TYPE),
					jsonResponse.getJSONObject(i).getInt(HistoryJSONEnum.HISTORY_TYPE));
			assertEquals(expectedResponse.getJSONObject(i).getString(HistoryJSONEnum.OLD_VALUE),
					jsonResponse.getJSONObject(i).getString(HistoryJSONEnum.OLD_VALUE));
			assertEquals(expectedResponse.getJSONObject(i).getString(HistoryJSONEnum.NEW_VALUE),
					jsonResponse.getJSONObject(i).getString(HistoryJSONEnum.NEW_VALUE));
			assertEquals(expectedResponse.getJSONObject(i).getLong(HistoryJSONEnum.CREATE_TIME),
					jsonResponse.getJSONObject(i).getLong(HistoryJSONEnum.CREATE_TIME));
		}
	}

	@Test
	public void testGetHistoriesInStory_ModifyStoryAndTaskRelation() throws Exception {
		IProject project = mCP.getProjectList().get(0);
		String sprintId = mCS.getSprintIDList().get(0);
		IIssue story = mASTS.getIssueList().get(0);

		SprintBacklogHelper sprintBacklogHelper = new SprintBacklogHelper(project, null, sprintId);
		TaskObject taskInfomation = new TaskObject();
		taskInfomation.name = "Task name";
		taskInfomation.estimation = "1";
		taskInfomation.notes = "task notes";
		IIssue task = sprintBacklogHelper.createTaskInStory(String.valueOf(story.getIssueID()), taskInfomation);
		Thread.sleep(1000);
		sprintBacklogHelper.removeTask(task.getIssueID(), story.getIssueID());
		Thread.sleep(1000);
		// Call
		// '/projects/{projectName}/sprints/{sprintId}/stories/{storyId}/histories'
		// API
		Response response = mClient.target(mBaseUri).path("projects/" + project.getName() + "/sprints/" + sprintId
				+ "/stories/" + story.getIssueID() + "/histories").request().get();

		JSONArray jsonResponse = new JSONArray(response.readEntity(String.class));

		SprintBacklogMapper sprintBacklogMapper = new SprintBacklogMapper(project, null);
		story = sprintBacklogMapper.getIssue(story.getIssueID());
		@SuppressWarnings("deprecation")
		JSONArray expectedResponse = JSONEncoder.toHistoryJSONArray(story.getIssueHistories(), story.getCategory());

		// Assert histories
		assertEquals(expectedResponse.length(), jsonResponse.length());
		for (int i = 0; i < expectedResponse.length(); i++) {
			assertEquals(expectedResponse.getJSONObject(i).getInt(HistoryJSONEnum.HISTORY_TYPE),
					jsonResponse.getJSONObject(i).getInt(HistoryJSONEnum.HISTORY_TYPE));
			assertEquals(expectedResponse.getJSONObject(i).getString(HistoryJSONEnum.OLD_VALUE),
					jsonResponse.getJSONObject(i).getString(HistoryJSONEnum.OLD_VALUE));
			assertEquals(expectedResponse.getJSONObject(i).getString(HistoryJSONEnum.NEW_VALUE),
					jsonResponse.getJSONObject(i).getString(HistoryJSONEnum.NEW_VALUE));
			assertEquals(expectedResponse.getJSONObject(i).getLong(HistoryJSONEnum.CREATE_TIME),
					jsonResponse.getJSONObject(i).getLong(HistoryJSONEnum.CREATE_TIME));
		}
	}
	
	@Test
	public void testGetHistoriesInStory_ModifyStoryStatus() throws Exception {
		IProject project = mCP.getProjectList().get(0);
		String sprintId = mCS.getSprintIDList().get(0);
		IIssue story = mASTS.getIssueList().get(0);
		
		SprintBacklogHelper sprintBacklogHelper = new SprintBacklogHelper(project, null, sprintId);
		sprintBacklogHelper.doneIssue(story.getIssueID(), story.getSummary(), story.getNotes(), "", "0");
		Thread.sleep(1000);
		sprintBacklogHelper.reopenIssue(story.getIssueID(), story.getSummary(), story.getNotes(), "");
		Thread.sleep(1000);
		
		// Call
		// '/projects/{projectName}/sprints/{sprintId}/stories/{storyId}/histories'
		// API
		Response response = mClient.target(mBaseUri).path("projects/" + project.getName() + "/sprints/" + sprintId
				+ "/stories/" + story.getIssueID() + "/histories").request().get();

		JSONArray jsonResponse = new JSONArray(response.readEntity(String.class));

		SprintBacklogMapper sprintBacklogMapper = new SprintBacklogMapper(project, null);
		story = sprintBacklogMapper.getIssue(story.getIssueID());
		@SuppressWarnings("deprecation")
		JSONArray expectedResponse = JSONEncoder.toHistoryJSONArray(story.getIssueHistories(), story.getCategory());

		// Assert histories
		assertEquals(expectedResponse.length(), jsonResponse.length());
		for (int i = 0; i < expectedResponse.length(); i++) {
			assertEquals(expectedResponse.getJSONObject(i).getInt(HistoryJSONEnum.HISTORY_TYPE),
					jsonResponse.getJSONObject(i).getInt(HistoryJSONEnum.HISTORY_TYPE));
			assertEquals(expectedResponse.getJSONObject(i).getString(HistoryJSONEnum.OLD_VALUE),
					jsonResponse.getJSONObject(i).getString(HistoryJSONEnum.OLD_VALUE));
			assertEquals(expectedResponse.getJSONObject(i).getString(HistoryJSONEnum.NEW_VALUE),
					jsonResponse.getJSONObject(i).getString(HistoryJSONEnum.NEW_VALUE));
			assertEquals(expectedResponse.getJSONObject(i).getLong(HistoryJSONEnum.CREATE_TIME),
					jsonResponse.getJSONObject(i).getLong(HistoryJSONEnum.CREATE_TIME));
		}
	}
}
