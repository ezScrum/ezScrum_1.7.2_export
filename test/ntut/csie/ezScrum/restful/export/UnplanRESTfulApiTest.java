package ntut.csie.ezScrum.restful.export;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.Date;
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

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.test.CreateData.CopyProject;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.test.CreateData.CreateUnplannedItem;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.ezScrum.web.databaseEnum.AccountEnum;
import ntut.csie.ezScrum.web.databaseEnum.UnplanEnum;
import ntut.csie.ezScrum.web.helper.UnplannedItemHelper;
import ntut.csie.jcis.resource.core.IProject;

public class UnplanRESTfulApiTest extends JerseyTest {
	private ezScrumInfoConfig mConfig = new ezScrumInfoConfig();
	private CreateProject mCP;
	private CreateSprint mCS;
	private CreateUnplannedItem mCU;
	
	private Client mClient;
	private HttpServer mHttpServer;
	private ResourceConfig mResourceConfig;
	private static String BASE_URL = "http://localhost:8080/ezScrum/resource/";
	private URI mBaseUri = URI.create(BASE_URL);

	@Override
	protected Application configure() {
		mResourceConfig = new ResourceConfig(UnplanRESTfulApi.class);
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
		
		// Create Unplan
		mCU = new CreateUnplannedItem(2, mCP, mCS);
		mCU.exe();
		
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
		mCU = null;
		mHttpServer = null;
		mResourceConfig = null;
		mBaseUri = null;
		mClient = null;
	}
	
	@Test
	public void testGetUnplansInSprint() throws JSONException {
		IProject project = mCP.getProjectList().get(0);
		String sprintId = mCS.getSprintIDList().get(0);
		List<IIssue> unplans = mCU.getIssueList();
		IIssue unplan1 = unplans.get(0);
		String name = unplan1.getSummary();
		String estimated = unplan1.getEstimated();
		String handler = unplan1.getAssignto();
		String partners = "Henry;Mike;Jonathan;Tony";
		String notes = unplan1.getNotes();
		String status = unplan1.getStatus();
		String actualHour = unplan1.getActualHour();
		
		UnplannedItemHelper unplannedItemHelper = new UnplannedItemHelper(project, mConfig.getUserSession());
		unplannedItemHelper.modifyUnplannedItemIssue(unplan1.getIssueID(), name, handler, status, partners, estimated, actualHour, notes, sprintId, new Date());
		unplan1 = unplannedItemHelper.getIssue(unplan1.getIssueID());
		
		// Call '/projects/{projectName}/sprints/{sprintId}/unplans' API
		Response response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() + "/sprints/" + sprintId + "/unplans")
		        .request()
		        .get();

		JSONArray jsonResponse = new JSONArray(response.readEntity(String.class));
		
		// Assert
		assertEquals(2, jsonResponse.length());
		
		assertEquals(unplans.get(0).getSummary(), jsonResponse.getJSONObject(0).getString(UnplanEnum.NAME));
		assertEquals(unplans.get(0).getAssignto(), jsonResponse.getJSONObject(0).getString(UnplanEnum.HANDLER));
		assertEquals(unplans.get(0).getEstimated(), jsonResponse.getJSONObject(0).getString(UnplanEnum.ESTIMATE));
		assertEquals(unplans.get(0).getActualHour(), jsonResponse.getJSONObject(0).getString(UnplanEnum.ACTUAL));
		assertEquals(unplans.get(0).getNotes(), jsonResponse.getJSONObject(0).getString(UnplanEnum.NOTES));
		assertEquals(unplans.get(0).getStatus(), jsonResponse.getJSONObject(0).getString(UnplanEnum.STATUS));
		JSONArray unplan1PartnersJSONArray = jsonResponse.getJSONObject(0).getJSONArray(UnplanEnum.PARTNERS);
		assertEquals("Henry", unplan1PartnersJSONArray.getJSONObject(0).getString(AccountEnum.USERNAME));
		assertEquals("Mike", unplan1PartnersJSONArray.getJSONObject(1).getString(AccountEnum.USERNAME));
		assertEquals("Jonathan", unplan1PartnersJSONArray.getJSONObject(2).getString(AccountEnum.USERNAME));
		assertEquals("Tony", unplan1PartnersJSONArray.getJSONObject(3).getString(AccountEnum.USERNAME));
		
		assertEquals(unplans.get(1).getSummary(), jsonResponse.getJSONObject(1).getString(UnplanEnum.NAME));
		assertEquals(unplans.get(1).getAssignto(), jsonResponse.getJSONObject(1).getString(UnplanEnum.HANDLER));
		assertEquals(unplans.get(1).getEstimated(), jsonResponse.getJSONObject(1).getString(UnplanEnum.ESTIMATE));
		assertEquals(unplans.get(1).getActualHour(), jsonResponse.getJSONObject(1).getString(UnplanEnum.ACTUAL));
		assertEquals(unplans.get(1).getNotes(), jsonResponse.getJSONObject(1).getString(UnplanEnum.NOTES));
		assertEquals(unplans.get(1).getStatus(), jsonResponse.getJSONObject(1).getString(UnplanEnum.STATUS));
	}
}
