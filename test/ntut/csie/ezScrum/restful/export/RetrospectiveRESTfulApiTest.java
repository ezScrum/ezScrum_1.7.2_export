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
import ntut.csie.ezScrum.test.CreateData.CopyProject;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateRetrospective;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.ezScrum.web.databaseEnum.RetrospectiveEnum;
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
		
		assertEquals(jsonResponse.getJSONObject(0).getString(RetrospectiveEnum.NAME), goods.get(0).getName());
		assertEquals(jsonResponse.getJSONObject(0).getString(RetrospectiveEnum.DESCRIPTION), goods.get(0).getDescription());
		assertEquals(jsonResponse.getJSONObject(0).getString(RetrospectiveEnum.TYPE), goods.get(0).getCategory());
		assertEquals(jsonResponse.getJSONObject(0).getString(RetrospectiveEnum.STATUS), goods.get(0).getStatus());
		
		assertEquals(jsonResponse.getJSONObject(1).getString(RetrospectiveEnum.NAME), goods.get(1).getName());
		assertEquals(jsonResponse.getJSONObject(1).getString(RetrospectiveEnum.DESCRIPTION), goods.get(1).getDescription());
		assertEquals(jsonResponse.getJSONObject(1).getString(RetrospectiveEnum.TYPE), goods.get(1).getCategory());
		assertEquals(jsonResponse.getJSONObject(1).getString(RetrospectiveEnum.STATUS), goods.get(1).getStatus());
		
		assertEquals(jsonResponse.getJSONObject(2).getString(RetrospectiveEnum.NAME), improvements.get(0).getName());
		assertEquals(jsonResponse.getJSONObject(2).getString(RetrospectiveEnum.DESCRIPTION), improvements.get(0).getDescription());
		assertEquals(jsonResponse.getJSONObject(2).getString(RetrospectiveEnum.TYPE), improvements.get(0).getCategory());
		assertEquals(jsonResponse.getJSONObject(2).getString(RetrospectiveEnum.STATUS), improvements.get(0).getStatus());
		
		assertEquals(jsonResponse.getJSONObject(3).getString(RetrospectiveEnum.NAME), improvements.get(1).getName());
		assertEquals(jsonResponse.getJSONObject(3).getString(RetrospectiveEnum.DESCRIPTION), improvements.get(1).getDescription());
		assertEquals(jsonResponse.getJSONObject(3).getString(RetrospectiveEnum.TYPE), improvements.get(1).getCategory());
		assertEquals(jsonResponse.getJSONObject(3).getString(RetrospectiveEnum.STATUS), improvements.get(1).getStatus());
	}
}
