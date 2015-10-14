package ntut.csie.ezScrum.restful.export;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.net.httpserver.HttpServer;

import ntut.csie.ezScrum.iteration.core.ISprintPlanDesc;
import ntut.csie.ezScrum.test.CreateData.CopyProject;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.ezScrum.web.helper.SprintPlanHelper;
import ntut.csie.ezScrum.web.support.export.JSONEncoder;
import ntut.csie.jcis.resource.core.IProject;

public class SprintRESTfulApiTest extends JerseyTest {
	private ezScrumInfoConfig mConfig = new ezScrumInfoConfig();
	private CreateProject mCP;
	private CreateSprint mCS;
	private ResourceConfig mResourceConfig;
	private Client mClient;
	private HttpServer mHttpServer;
	private static String BASE_URL = "http://localhost:8080/ezScrum/resource/";
	private URI mBaseUri = URI.create(BASE_URL);

	@Override
	protected Application configure() {
		mResourceConfig = new ResourceConfig(SprintRESTfulApi.class);
		return mResourceConfig;
	}
	
	@Before
	public void setUp() {
		// 初始化 SQL
		InitialSQL ini = new InitialSQL(mConfig);
		ini.exe();
		
		// Create Project
		mCP = new CreateProject(2);
		mCP.exeCreate();
		
		//	 新增兩個Sprint
    	this.mCS = new CreateSprint(2, mCP);
    	this.mCS.exe();
    	
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
		mHttpServer = null;
		mClient = null;
	}

	@Test
	public void testGet_First() throws JSONException {
		String firstSprintId = mCS.getSprintIDList().get(0);
		IProject project = mCP.getProjectList().get(0);
		SprintPlanHelper sprintPlanHelper = new SprintPlanHelper(project);
		ISprintPlanDesc firstSprint = sprintPlanHelper.loadPlan(firstSprintId);
		String projectName = project.getName();
		// Call '/projects/{projectName}/sprints' API
		Response response = mClient.target(BASE_URL)
				                 .path("projects/" + projectName + "/sprints/" + firstSprintId)
				                 .request()
				                 .get();
		
		JSONObject jsonResponse = new JSONObject(response.readEntity(String.class));
		// Assert
		assertEquals(JSONEncoder.toSprintJSON(firstSprint).toString(), jsonResponse.toString());
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void testGet_Second() throws JSONException {
//		IProject secondProject = mCP.getProjectList().get(1);
//		
//		// Test data
//		String projectNmae = secondProject.getName();
//		String projectDisplayName = secondProject.getProjectDesc().getDisplayName();
//		String projectComment = secondProject.getProjectDesc().getComment();
//		String projectManager = secondProject.getProjectDesc().getProjectManager();
//		String projectAttachFileSize = secondProject.getProjectDesc().getAttachFileSize();
//		long projectCreateTime = secondProject.getProjectDesc().getCreateDate().getTime();
//		
//		// Call '/projects/{projectName}' API
//		Response response = mClient.target(mBaseUrl)
//				                 .path("projects/" + projectNmae)
//				                 .request()
//				                 .get();
//		
//		JSONObject jsonResponse = new JSONObject(response.readEntity(String.class));
//		
//		// Assert
//		assertEquals(projectNmae, jsonResponse.get(ProjectEnum.NAME));
//		assertEquals(projectDisplayName, jsonResponse.get(ProjectEnum.DISPLAY_NAME));
//		assertEquals(projectComment, jsonResponse.get(ProjectEnum.COMMENT));
//		assertEquals(projectManager, jsonResponse.get(ProjectEnum.PRODUCT_OWNER));
//		assertEquals(projectAttachFileSize, jsonResponse.get(ProjectEnum.ATTATCH_MAX_SIZE));
//		assertEquals(projectCreateTime, jsonResponse.get(ProjectEnum.CREATE_TIME));
//		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void testGetList_MultipleProjects() throws JSONException {
//		IProject firstProject = mCP.getProjectList().get(0);
//		IProject secondProject = mCP.getProjectList().get(1);
//		
//		// Test data
//		String projectNmae = firstProject.getName();
//		String projectDisplayName = firstProject.getProjectDesc().getDisplayName();
//		String projectComment = firstProject.getProjectDesc().getComment();
//		String projectManager = firstProject.getProjectDesc().getProjectManager();
//		String projectAttachFileSize = firstProject.getProjectDesc().getAttachFileSize();
//		long projectCreateTime = firstProject.getProjectDesc().getCreateDate().getTime();
//		
//		String projectNmae2 = secondProject.getName();
//		String projectDisplayName2 = secondProject.getProjectDesc().getDisplayName();
//		String projectComment2 = secondProject.getProjectDesc().getComment();
//		String projectManager2 = secondProject.getProjectDesc().getProjectManager();
//		String projectAttachFileSize2 = secondProject.getProjectDesc().getAttachFileSize();
//		long projectCreateTime2 = secondProject.getProjectDesc().getCreateDate().getTime();
//		
//		// Call '/projects' API
//		Response response = mClient.target(mBaseUrl)
//				                 .path("projects")
//		                         .request()
//		                         .get();
//		
//		JSONArray jsonArrayResponse = new JSONArray(response.readEntity(String.class));
//		
//		// Assert
//		assertEquals(2, jsonArrayResponse.length());
//		assertEquals(projectNmae, jsonArrayResponse.getJSONObject(0).get(ProjectEnum.NAME));
//		assertEquals(projectDisplayName, jsonArrayResponse.getJSONObject(0).get(ProjectEnum.DISPLAY_NAME));
//		assertEquals(projectComment, jsonArrayResponse.getJSONObject(0).get(ProjectEnum.COMMENT));
//		assertEquals(projectManager, jsonArrayResponse.getJSONObject(0).get(ProjectEnum.PRODUCT_OWNER));
//		assertEquals(projectAttachFileSize, jsonArrayResponse.getJSONObject(0).get(ProjectEnum.ATTATCH_MAX_SIZE));
//		assertEquals(projectCreateTime, jsonArrayResponse.getJSONObject(0).get(ProjectEnum.CREATE_TIME));
//		
//		assertEquals(projectNmae2, jsonArrayResponse.getJSONObject(1).get(ProjectEnum.NAME));
//		assertEquals(projectDisplayName2, jsonArrayResponse.getJSONObject(1).get(ProjectEnum.DISPLAY_NAME));
//		assertEquals(projectComment2, jsonArrayResponse.getJSONObject(1).get(ProjectEnum.COMMENT));
//		assertEquals(projectManager2, jsonArrayResponse.getJSONObject(1).get(ProjectEnum.PRODUCT_OWNER));
//		assertEquals(projectAttachFileSize2, jsonArrayResponse.getJSONObject(1).get(ProjectEnum.ATTATCH_MAX_SIZE));
//		assertEquals(projectCreateTime2, jsonArrayResponse.getJSONObject(1).get(ProjectEnum.CREATE_TIME));
	}
}
