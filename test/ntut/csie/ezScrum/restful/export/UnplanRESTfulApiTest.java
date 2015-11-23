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
import ntut.csie.ezScrum.issue.core.IIssueHistory;
import ntut.csie.ezScrum.issue.core.ITSEnum;
import ntut.csie.ezScrum.iteration.core.ScrumEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.AccountJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.HistoryJSONEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.UnplanJSONEnum;
import ntut.csie.ezScrum.restful.export.support.JSONEncoder;
import ntut.csie.ezScrum.test.CreateData.CopyProject;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.test.CreateData.CreateUnplannedItem;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
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
		
		assertEquals(unplans.get(0).getSummary(), jsonResponse.getJSONObject(0).getString(UnplanJSONEnum.NAME));
		assertEquals(unplans.get(0).getAssignto(), jsonResponse.getJSONObject(0).getString(UnplanJSONEnum.HANDLER));
		assertEquals(unplans.get(0).getEstimated(), jsonResponse.getJSONObject(0).getString(UnplanJSONEnum.ESTIMATE));
		assertEquals(unplans.get(0).getActualHour(), jsonResponse.getJSONObject(0).getString(UnplanJSONEnum.ACTUAL));
		assertEquals(unplans.get(0).getNotes(), jsonResponse.getJSONObject(0).getString(UnplanJSONEnum.NOTES));
		assertEquals(unplans.get(0).getStatus(), jsonResponse.getJSONObject(0).getString(UnplanJSONEnum.STATUS));
		JSONArray unplan1PartnersJSONArray = jsonResponse.getJSONObject(0).getJSONArray(UnplanJSONEnum.PARTNERS);
		assertEquals("Henry", unplan1PartnersJSONArray.getJSONObject(0).getString(AccountJSONEnum.USERNAME));
		assertEquals("Mike", unplan1PartnersJSONArray.getJSONObject(1).getString(AccountJSONEnum.USERNAME));
		assertEquals("Jonathan", unplan1PartnersJSONArray.getJSONObject(2).getString(AccountJSONEnum.USERNAME));
		assertEquals("Tony", unplan1PartnersJSONArray.getJSONObject(3).getString(AccountJSONEnum.USERNAME));
		
		assertEquals(unplans.get(1).getSummary(), jsonResponse.getJSONObject(1).getString(UnplanJSONEnum.NAME));
		assertEquals(unplans.get(1).getAssignto(), jsonResponse.getJSONObject(1).getString(UnplanJSONEnum.HANDLER));
		assertEquals(unplans.get(1).getEstimated(), jsonResponse.getJSONObject(1).getString(UnplanJSONEnum.ESTIMATE));
		assertEquals(unplans.get(1).getActualHour(), jsonResponse.getJSONObject(1).getString(UnplanJSONEnum.ACTUAL));
		assertEquals(unplans.get(1).getNotes(), jsonResponse.getJSONObject(1).getString(UnplanJSONEnum.NOTES));
		assertEquals(unplans.get(1).getStatus(), jsonResponse.getJSONObject(1).getString(UnplanJSONEnum.STATUS));
	}
	
	@Test
	public void testGetHistoriesInTask_CreateTask() throws JSONException {
		IProject project = mCP.getProjectList().get(0);
		String sprintId = mCS.getSprintIDList().get(0);
		IIssue unplan = mCU.getIssueList().get(0);
		
		// Call '/projects/{projectName}/sprints/{sprintId}/unplans/{unplanId}/histories' API
		Response response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() +
		              "/sprints/" + sprintId +
		              "/unplans/" + unplan.getIssueID() +
		              "/histories")
		        .request()
		        .get();
		
		@SuppressWarnings("deprecation")
		List<IIssueHistory> histories = unplan.getIssueHistories();
		JSONArray expectedJSONArray = JSONEncoder.toHistoryJSONArray(histories, ScrumEnum.UNPLANNEDITEM_ISSUE_TYPE);
		
		JSONArray jsonResponse = new JSONArray(response.readEntity(String.class));
		// Assert
		assertEquals(expectedJSONArray.length(), jsonResponse.length());
		// First History
		assertEquals(expectedJSONArray.getJSONObject(0).getInt(HistoryJSONEnum.HISTORY_TYPE), jsonResponse.getJSONObject(0).getInt(HistoryJSONEnum.HISTORY_TYPE));
		assertEquals(expectedJSONArray.getJSONObject(0).getString(HistoryJSONEnum.OLD_VALUE), jsonResponse.getJSONObject(0).getString(HistoryJSONEnum.OLD_VALUE));
		assertEquals(expectedJSONArray.getJSONObject(0).getString(HistoryJSONEnum.NEW_VALUE), jsonResponse.getJSONObject(0).getString(HistoryJSONEnum.NEW_VALUE));
		assertEquals(expectedJSONArray.getJSONObject(0).getLong(HistoryJSONEnum.CREATE_TIME), jsonResponse.getJSONObject(0).getLong(HistoryJSONEnum.CREATE_TIME));
		// Second History
		assertEquals(expectedJSONArray.getJSONObject(1).getInt(HistoryJSONEnum.HISTORY_TYPE), jsonResponse.getJSONObject(1).getInt(HistoryJSONEnum.HISTORY_TYPE));
		assertEquals(expectedJSONArray.getJSONObject(1).getString(HistoryJSONEnum.OLD_VALUE), jsonResponse.getJSONObject(1).getString(HistoryJSONEnum.OLD_VALUE));
		assertEquals(expectedJSONArray.getJSONObject(1).getString(HistoryJSONEnum.NEW_VALUE), jsonResponse.getJSONObject(1).getString(HistoryJSONEnum.NEW_VALUE));
		assertEquals(expectedJSONArray.getJSONObject(1).getLong(HistoryJSONEnum.CREATE_TIME), jsonResponse.getJSONObject(1).getLong(HistoryJSONEnum.CREATE_TIME));
		// Third History
		assertEquals(expectedJSONArray.getJSONObject(2).getInt(HistoryJSONEnum.HISTORY_TYPE), jsonResponse.getJSONObject(2).getInt(HistoryJSONEnum.HISTORY_TYPE));
		assertEquals(expectedJSONArray.getJSONObject(2).getString(HistoryJSONEnum.OLD_VALUE), jsonResponse.getJSONObject(2).getString(HistoryJSONEnum.OLD_VALUE));
		assertEquals(expectedJSONArray.getJSONObject(2).getString(HistoryJSONEnum.NEW_VALUE), jsonResponse.getJSONObject(2).getString(HistoryJSONEnum.NEW_VALUE));
		assertEquals(expectedJSONArray.getJSONObject(2).getLong(HistoryJSONEnum.CREATE_TIME), jsonResponse.getJSONObject(2).getLong(HistoryJSONEnum.CREATE_TIME));
	}
	
	@Test
	public void testGetHistoriesInTask_ModifyUnplanInformation() throws JSONException, Exception {
		IProject project = mCP.getProjectList().get(0);
		String sprintId = mCS.getSprintIDList().get(0);
		IIssue unplan = mCU.getIssueList().get(0);
		
		// Edit Unplan
		Thread.sleep(1000);
		UnplannedItemHelper unplannedItemHelper = new UnplannedItemHelper(project, null);
		unplannedItemHelper.modifyUnplannedItemIssue(unplan.getIssueID(), unplan.getSummary(), unplan.getAssignto(), ITSEnum.ASSIGNED,
		                                             "", "20", "20", "newNotes", "100", null);
		unplan = unplannedItemHelper.getIssue(unplan.getIssueID());
		Thread.sleep(1000);
		
		// Call '/projects/{projectName}/sprints/{sprintId}/unplans/{unplanId}/histories' API
		Response response = mClient.target(mBaseUri)
		        .path("projects/" + project.getName() +
		              "/sprints/" + sprintId +
		              "/unplans/" + unplan.getIssueID() +
		              "/histories")
		        .request()
		        .get();
		
		@SuppressWarnings("deprecation")
		List<IIssueHistory> histories = unplan.getIssueHistories();
		JSONArray expectedJSONArray = JSONEncoder.toHistoryJSONArray(histories, ScrumEnum.UNPLANNEDITEM_ISSUE_TYPE);
		
		JSONArray jsonResponse = new JSONArray(response.readEntity(String.class));
		// Assert
		assertEquals(expectedJSONArray.length(), jsonResponse.length());
		for (int i = 0; i < expectedJSONArray.length(); i++) {
			assertEquals(expectedJSONArray.getJSONObject(i).getInt(HistoryJSONEnum.HISTORY_TYPE), jsonResponse.getJSONObject(i).getInt(HistoryJSONEnum.HISTORY_TYPE));
			assertEquals(expectedJSONArray.getJSONObject(i).getString(HistoryJSONEnum.OLD_VALUE), jsonResponse.getJSONObject(i).getString(HistoryJSONEnum.OLD_VALUE));
			assertEquals(expectedJSONArray.getJSONObject(i).getString(HistoryJSONEnum.NEW_VALUE), jsonResponse.getJSONObject(i).getString(HistoryJSONEnum.NEW_VALUE));
			assertEquals(expectedJSONArray.getJSONObject(i).getLong(HistoryJSONEnum.CREATE_TIME), jsonResponse.getJSONObject(i).getLong(HistoryJSONEnum.CREATE_TIME));
		}
	}
}
