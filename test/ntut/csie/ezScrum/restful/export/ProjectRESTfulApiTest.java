package ntut.csie.ezScrum.restful.export;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;

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

import ntut.csie.ezScrum.pic.core.IUserSession;
import ntut.csie.ezScrum.test.CreateData.CopyProject;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.ezScrum.web.dataObject.ITSInformation;
import ntut.csie.ezScrum.web.dataObject.ProjectInformation;
import ntut.csie.ezScrum.web.databaseEnum.ProjectEnum;
import ntut.csie.ezScrum.web.helper.ProjectHelper;
import ntut.csie.jcis.resource.core.IProject;

public class ProjectRESTfulApiTest extends JerseyTest {
	private ezScrumInfoConfig mConfig = new ezScrumInfoConfig();
	private CreateProject mCP;
	private IUserSession mUserSession = null;
	private static IProject sProject;

	private Client mClient;
	private HttpServer mHttpServer;
	private String mBaseUrl = "http://localhost:8080/ezScrum/resource";

	@Override
	protected Application configure() {
		return new ResourceConfig(ProjectRESTfulApi.class);
	}
	
	@Before
	public void setUp() {
		// 初始化 SQL
		InitialSQL ini = new InitialSQL(mConfig);
		ini.exe();

		// Create Project
		mCP = new CreateProject(1);
		mCP.exeCreate();
		
		// Get test Project
		sProject = mCP.getProjectList().get(0);

		// Start Server
		URI baseUri = URI.create(mBaseUrl);
		ResourceConfig resourceConfig = new ResourceConfig(ProjectRESTfulApi.class);
		mHttpServer = JdkHttpServerFactory.createHttpServer(baseUri, resourceConfig, true);
		
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
		mUserSession = null;
		mHttpServer = null;
		mClient = null;
	}

	@Test
	public void testGet() throws JSONException {
		// Test data
		String projectNmae = sProject.getName();
		String projectDisplayName = sProject.getProjectDesc().getDisplayName();
		String projectComment = sProject.getProjectDesc().getComment();
		String projectManager = sProject.getProjectDesc().getProjectManager();
		String projectAttachFileSize = sProject.getProjectDesc().getAttachFileSize();
		long projectCreateTime = sProject.getProjectDesc().getCreateDate().getTime();
		
		// Call '/projects/{projectName}' API
		String response = mClient.target(mBaseUrl)
				                 .path("projects/" + projectNmae)
				                 .request()
				                 .get(String.class);
		
		JSONObject jsonResponse = new JSONObject(response);
		
		// Assert
		assertEquals(projectNmae, jsonResponse.get(ProjectEnum.NAME));
		assertEquals(projectDisplayName, jsonResponse.get(ProjectEnum.DISPLAY_NAME));
		assertEquals(projectComment, jsonResponse.get(ProjectEnum.COMMENT));
		assertEquals(projectManager, jsonResponse.get(ProjectEnum.PRODUCT_OWNER));
		assertEquals(projectAttachFileSize, jsonResponse.get(ProjectEnum.ATTATCH_MAX_SIZE));
		assertEquals(projectCreateTime, jsonResponse.get(ProjectEnum.CREATE_TIME));
	}
	
	@Test
	public void testGetList_one_project() throws JSONException {
		// Test data
		String projectNmae = sProject.getName();
		String projectDisplayName = sProject.getProjectDesc().getDisplayName();
		String projectComment = sProject.getProjectDesc().getComment();
		String projectManager = sProject.getProjectDesc().getProjectManager();
		String projectAttachFileSize = sProject.getProjectDesc().getAttachFileSize();
		long projectCreateTime = sProject.getProjectDesc().getCreateDate().getTime();
		
		// Call '/projects/{projectName}' API
		String response = mClient.target(mBaseUrl)
				                 .path("projects")
		                         .request()
		                         .get(String.class);
		
		JSONObject jsonResponse = new JSONObject(response);
		JSONArray jsonArrayResponse = jsonResponse.getJSONArray("projects");
		
		// Assert
		assertEquals(1, jsonArrayResponse.length());
		assertEquals(projectNmae, jsonArrayResponse.getJSONObject(0).get(ProjectEnum.NAME));
		assertEquals(projectDisplayName, jsonArrayResponse.getJSONObject(0).get(ProjectEnum.DISPLAY_NAME));
		assertEquals(projectComment, jsonArrayResponse.getJSONObject(0).get(ProjectEnum.COMMENT));
		assertEquals(projectManager, jsonArrayResponse.getJSONObject(0).get(ProjectEnum.PRODUCT_OWNER));
		assertEquals(projectAttachFileSize, jsonArrayResponse.getJSONObject(0).get(ProjectEnum.ATTATCH_MAX_SIZE));
		assertEquals(projectCreateTime, jsonArrayResponse.getJSONObject(0).get(ProjectEnum.CREATE_TIME));
	}
}
