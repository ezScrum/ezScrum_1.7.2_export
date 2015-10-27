package ntut.csie.ezScrum.restful.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
import ntut.csie.ezScrum.test.CreateData.AddStoryToSprint;
import ntut.csie.ezScrum.test.CreateData.CopyProject;
import ntut.csie.ezScrum.test.CreateData.CreateProductBacklog;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.ezScrum.web.databaseEnum.StoryEnum;
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

		// Call '/projects/{projectName}/stories/{storyId}' API
		Response response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() + "/sprints/" + sprintId + "/stories/")
		        .request()
		        .get();

		JSONArray jsonResponse = new JSONArray(response.readEntity(String.class));

		// Assert
		assertEquals(story1.getIssueID(), jsonResponse.getJSONObject(0).get(StoryEnum.ID));
		assertEquals(story1.getSummary(), jsonResponse.getJSONObject(0).get(StoryEnum.NAME));
		assertEquals(story1.getStatus(), jsonResponse.getJSONObject(0).get(StoryEnum.STATUS));
		assertEquals(Integer.parseInt(story1.getEstimated()), jsonResponse.getJSONObject(0).get(StoryEnum.ESTIMATE));
		assertEquals(Integer.parseInt(story1.getImportance()), jsonResponse.getJSONObject(0).get(StoryEnum.IMPORTANCE));
		assertEquals(Integer.parseInt(story1.getValue()), jsonResponse.getJSONObject(0).get(StoryEnum.VALUE));
		assertEquals(story1.getNotes(), jsonResponse.getJSONObject(0).get(StoryEnum.NOTES));
		assertEquals(story1.getHowToDemo(), jsonResponse.getJSONObject(0).get(StoryEnum.HOW_TO_DEMO));

		assertEquals(story2.getIssueID(), jsonResponse.getJSONObject(1).get(StoryEnum.ID));
		assertEquals(story2.getSummary(), jsonResponse.getJSONObject(1).get(StoryEnum.NAME));
		assertEquals(story2.getStatus(), jsonResponse.getJSONObject(1).get(StoryEnum.STATUS));
		assertEquals(Integer.parseInt(story2.getEstimated()), jsonResponse.getJSONObject(1).get(StoryEnum.ESTIMATE));
		assertEquals(Integer.parseInt(story2.getImportance()), jsonResponse.getJSONObject(1).get(StoryEnum.IMPORTANCE));
		assertEquals(Integer.parseInt(story2.getValue()), jsonResponse.getJSONObject(1).get(StoryEnum.VALUE));
		assertEquals(story2.getNotes(), jsonResponse.getJSONObject(1).get(StoryEnum.NOTES));
		assertEquals(story2.getHowToDemo(), jsonResponse.getJSONObject(1).get(StoryEnum.HOW_TO_DEMO));

		// No other JSONObject in JSONArray
		try {
			jsonResponse.getJSONObject(2);
			assertTrue(false);
		} catch (JSONException e) {
			assertTrue(true);
		}
	}
}