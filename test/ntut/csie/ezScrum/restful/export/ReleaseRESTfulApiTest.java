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

import ntut.csie.ezScrum.iteration.core.IReleasePlanDesc;
import ntut.csie.ezScrum.restful.export.jsonEnum.ReleaseJSONEnum;
import ntut.csie.ezScrum.test.CreateData.CopyProject;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateRelease;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.ezScrum.web.helper.ReleasePlanHelper;
import ntut.csie.jcis.resource.core.IProject;

public class ReleaseRESTfulApiTest extends JerseyTest {
	private ezScrumInfoConfig mConfig = new ezScrumInfoConfig();
	private CreateProject mCP;
	private CreateRelease mCR;
	
	private ResourceConfig mResourceConfig;
	private Client mClient;
	private HttpServer mHttpServer;
	private static String BASE_URL = "http://localhost:8080/ezScrum/resource/";
	private URI mBaseUri = URI.create(BASE_URL);

	@Override
	protected Application configure() {
		mResourceConfig = new ResourceConfig(ReleaseRESTfulApi.class);
		return mResourceConfig;
	}

	@Before
	public void setUp() {
		// 初始化 SQL
		InitialSQL ini = new InitialSQL(mConfig);
		ini.exe();

		// Create Project
		mCP = new CreateProject(1);
		mCP.exeCreate();
		
		// Create Release
		mCR = new CreateRelease(2, mCP);
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
		mCR = null;
		mHttpServer = null;
		mClient = null;
	}
	
	@Test
	public void testGetReleases() throws JSONException {
		IProject project = mCP.getProjectList().get(0);
		IReleasePlanDesc release1 = mCR.getReleaseList().get(0);
		IReleasePlanDesc release2 = mCR.getReleaseList().get(1);
		
		// Api Test
		// Call '/projects/{projectName}/releases' API
		Response response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() + 
		        	  "/releases")
		        .request()
		        .get();
		
		JSONArray jsonResponse = new JSONArray(response.readEntity(String.class));

		// Assert
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		assertEquals(2, jsonResponse.length());

		JSONObject releaseJson1 = jsonResponse.getJSONObject(0);
		assertEquals(release1.getName(), releaseJson1.getString(ReleaseJSONEnum.NAME));
		assertEquals(release1.getDescription(), releaseJson1.getString(ReleaseJSONEnum.DESCRIPTION));
		assertEquals(release1.getStartDate(), releaseJson1.getString(ReleaseJSONEnum.START_DATE));
		assertEquals(release1.getEndDate(), releaseJson1.getString(ReleaseJSONEnum.DUE_DATE));
		
		JSONObject releaseJson2 = jsonResponse.getJSONObject(1);
		assertEquals(release2.getName(), releaseJson2.getString(ReleaseJSONEnum.NAME));
		assertEquals(release2.getDescription(), releaseJson2.getString(ReleaseJSONEnum.DESCRIPTION));
		assertEquals(release2.getStartDate(), releaseJson2.getString(ReleaseJSONEnum.START_DATE));
		assertEquals(release2.getEndDate(), releaseJson2.getString(ReleaseJSONEnum.DUE_DATE));
	}
	
	@Test
	public void testGetReleases_NoRelease() throws JSONException {
		IProject project = mCP.getProjectList().get(0);
		IReleasePlanDesc release1 = mCR.getReleaseList().get(0);
		IReleasePlanDesc release2 = mCR.getReleaseList().get(1);
		
		// Remove Releases
		ReleasePlanHelper releasePlanHelper = new ReleasePlanHelper(project);
		releasePlanHelper.deleteReleasePlan(release1.getID());
		releasePlanHelper.deleteReleasePlan(release2.getID());
		
		// Api Test
		// Call '/projects/{projectName}/releases' API
		Response response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() + 
		        	  "/releases")
		        .request()
		        .get();
		
		JSONArray jsonResponse = new JSONArray(response.readEntity(String.class));

		// Assert
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		assertEquals(0, jsonResponse.length());
	}
	
	@Test
	public void testGetReleases_InvalidProject() throws JSONException {
		// Api Test
		// Call '/projects/{projectName}/releases' API
		Response response = mClient.target(mBaseUri)
		        .path("projects/invalidProject/releases")
		        .request()
		        .get();

		// Assert
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
	}
}
