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

import ntut.csie.ezScrum.test.CreateData.CopyProject;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.ezScrum.web.databaseEnum.TagEnum;
import ntut.csie.ezScrum.web.helper.ProductBacklogHelper;
import ntut.csie.ezScrum.web.mapper.ProjectMapper;
import ntut.csie.ezScrum.web.support.export.JSONEncoder;
import ntut.csie.jcis.resource.core.IProject;

public class ProjectRESTfulApiTest extends JerseyTest {
	private ezScrumInfoConfig mConfig = new ezScrumInfoConfig();
	private CreateProject mCP;
	private ResourceConfig mResourceConfig;
	private Client mClient;
	private HttpServer mHttpServer;
	private static String BASE_URL = "http://localhost:8080/ezScrum/resource/";
	private URI mBaseUri = URI.create(BASE_URL);

	@Override
	protected Application configure() {
		mResourceConfig = new ResourceConfig(ProjectRESTfulApi.class);
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
		mClient = null;
	}

	@Test
	public void testGetProjects() throws JSONException {
		// Get projects
		List<IProject> projects = new ProjectMapper().getAllProjectList();

		// Call '/projects' API
		Response response = mClient.target(BASE_URL)
		        .path("projects")
		        .request()
		        .get();

		JSONArray jsonArrayResponse = new JSONArray(response.readEntity(String.class));

		// Assert
		assertEquals(2, jsonArrayResponse.length());
		assertEquals(JSONEncoder.toProjectJSONArray(projects).toString(), jsonArrayResponse.toString());
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void testGetTagsInProject() throws JSONException {
		String tagName1 = "Data Migration";
		String tagName2 = "Thesis";
		IProject project = mCP.getProjectList().get(0);
		ProductBacklogHelper productBacklogHelper = new ProductBacklogHelper(null, project);
		productBacklogHelper.addNewTag(tagName1);
		productBacklogHelper.addNewTag(tagName2);
		
		// Call '/projects/{projectName}/tags' API
		Response response = mClient.target(BASE_URL)
		        .path("projects/" + project.getName() + "/tags")
		        .request()
		        .get();
		
		JSONArray jsonArrayResponse = new JSONArray(response.readEntity(String.class));
		
		// Assert
		assertEquals(2, jsonArrayResponse.length());
		assertEquals(tagName1, jsonArrayResponse.getJSONObject(0).getString(TagEnum.NAME));
		assertEquals(tagName2, jsonArrayResponse.getJSONObject(1).getString(TagEnum.NAME));
	}
}
