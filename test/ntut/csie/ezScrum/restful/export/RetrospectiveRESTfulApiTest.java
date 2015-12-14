package ntut.csie.ezScrum.restful.export;

import static org.junit.Assert.assertEquals;

import java.net.URI;
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

import ntut.csie.ezScrum.iteration.core.IScrumIssue;
import ntut.csie.ezScrum.restful.export.jsonEnum.RetrospectiveJSONEnum;
import ntut.csie.ezScrum.test.CreateData.CopyProject;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateRetrospective;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.jcis.resource.core.IProject;

public class RetrospectiveRESTfulApiTest extends JerseyTest {
	private ezScrumInfoConfig mConfig = new ezScrumInfoConfig();
	private CreateProject mCP;
	private CreateSprint mCS;
	private CreateRetrospective mCR;

	private Client mClient;
	private HttpServer mHttpServer;
	private ResourceConfig mResourceConfig;
	private static String BASE_URL = "http://localhost:8080/ezScrum/resource/";
	private URI mBaseUri = URI.create(BASE_URL);

	@Override
	protected Application configure() {
		mResourceConfig = new ResourceConfig(RetrospectiveRESTfulApi.class);
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
		
		// Create Retrospective
		mCR = new CreateRetrospective(2, 2, mCP, mCS);
		mCR.exe();
		
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
	public void testGetRetrospectivesInSprint() throws JSONException {
		IProject project = mCP.getProjectList().get(0);
		String sprintId = mCS.getSprintIDList().get(0);
		List<IScrumIssue> goods = mCR.getGoodRetrospectiveList();
		List<IScrumIssue> improvements = mCR.getImproveRetrospectiveList();
		
		// Call '/projects/{projectName}/sprints/{sprintId}/retrospectives}' API
		Response response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() + "/sprints/" + sprintId + "/retrospectives/")
		        .request()
		        .get();
		
		JSONArray jsonResponse = new JSONArray(response.readEntity(String.class));
		
		// Assert
		assertEquals(4, jsonResponse.length());
		
		assertEquals(goods.get(0).getName(), jsonResponse.getJSONObject(0).getString(RetrospectiveJSONEnum.NAME));
		assertEquals(goods.get(0).getDescription(), jsonResponse.getJSONObject(0).getString(RetrospectiveJSONEnum.DESCRIPTION));
		assertEquals(goods.get(0).getCategory(), jsonResponse.getJSONObject(0).getString(RetrospectiveJSONEnum.TYPE));
		assertEquals(goods.get(0).getStatus(), jsonResponse.getJSONObject(0).getString(RetrospectiveJSONEnum.STATUS));
		
		assertEquals(goods.get(1).getName(), jsonResponse.getJSONObject(1).getString(RetrospectiveJSONEnum.NAME));
		assertEquals(goods.get(1).getDescription(), jsonResponse.getJSONObject(1).getString(RetrospectiveJSONEnum.DESCRIPTION));
		assertEquals(goods.get(1).getCategory(), jsonResponse.getJSONObject(1).getString(RetrospectiveJSONEnum.TYPE));
		assertEquals(goods.get(1).getStatus(), jsonResponse.getJSONObject(1).getString(RetrospectiveJSONEnum.STATUS));
		
		assertEquals(improvements.get(0).getName(), jsonResponse.getJSONObject(2).getString(RetrospectiveJSONEnum.NAME));
		assertEquals(improvements.get(0).getDescription(), jsonResponse.getJSONObject(2).getString(RetrospectiveJSONEnum.DESCRIPTION));
		assertEquals(improvements.get(0).getCategory(), jsonResponse.getJSONObject(2).getString(RetrospectiveJSONEnum.TYPE));
		assertEquals(improvements.get(0).getStatus(), jsonResponse.getJSONObject(2).getString(RetrospectiveJSONEnum.STATUS));
		
		assertEquals(improvements.get(1).getName(), jsonResponse.getJSONObject(3).getString(RetrospectiveJSONEnum.NAME));
		assertEquals(improvements.get(1).getDescription(), jsonResponse.getJSONObject(3).getString(RetrospectiveJSONEnum.DESCRIPTION));
		assertEquals(improvements.get(1).getCategory(), jsonResponse.getJSONObject(3).getString(RetrospectiveJSONEnum.TYPE));
		assertEquals(improvements.get(1).getStatus(), jsonResponse.getJSONObject(3).getString(RetrospectiveJSONEnum.STATUS));
	}
}