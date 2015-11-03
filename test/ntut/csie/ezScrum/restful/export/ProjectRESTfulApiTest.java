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
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.net.httpserver.HttpServer;

import ntut.csie.ezScrum.pic.core.ScrumRole;
import ntut.csie.ezScrum.test.CreateData.CopyProject;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.ezScrum.web.dataObject.UserInformation;
import ntut.csie.ezScrum.web.databaseEnum.AccountEnum;
import ntut.csie.ezScrum.web.databaseEnum.ScrumRoleEnum;
import ntut.csie.ezScrum.web.databaseEnum.TagEnum;
import ntut.csie.ezScrum.web.helper.AccountHelper;
import ntut.csie.ezScrum.web.helper.ProductBacklogHelper;
import ntut.csie.ezScrum.web.mapper.ProjectMapper;
import ntut.csie.ezScrum.web.mapper.ScrumRoleMapper;
import ntut.csie.ezScrum.web.support.export.JSONEncoder;
import ntut.csie.jcis.account.core.IAccount;
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
		Response response = mClient.target(BASE_URL).path("projects").request().get();

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
		Response response = mClient.target(BASE_URL).path("projects/" + project.getName() + "/tags").request().get();

		JSONArray jsonArrayResponse = new JSONArray(response.readEntity(String.class));

		// Assert
		assertEquals(2, jsonArrayResponse.length());
		assertEquals(tagName1, jsonArrayResponse.getJSONObject(0).getString(TagEnum.NAME));
		assertEquals(tagName2, jsonArrayResponse.getJSONObject(1).getString(TagEnum.NAME));
	}

	@Test
	public void testGetScrumRolesInProject() throws JSONException {
		IProject project = mCP.getProjectList().get(0);
		ScrumRoleMapper scrumRoleMapper = new ScrumRoleMapper();
		ScrumRole productOwner = new ScrumRole(project.getName(), ScrumRoleEnum.PRODUCT_OWNER);
		productOwner.setAccessProductBacklog(true);
		productOwner.setAccessSprintPlan(true);
		productOwner.setAccessTaskBoard(false);
		productOwner.setAccessSprintBacklog(true);
		productOwner.setAccessReleasePlan(true);
		productOwner.setAccessRetrospective(false);
		productOwner.setAccessUnplannedItem(false);
		productOwner.setReadReport(true);
		productOwner.setEditProject(true);
		scrumRoleMapper.update(productOwner);

		ScrumRole scrumMaster = new ScrumRole(project.getName(), ScrumRoleEnum.SCRUM_MASTER);
		scrumMaster.setAccessProductBacklog(true);
		scrumMaster.setAccessSprintPlan(true);
		scrumMaster.setAccessTaskBoard(true);
		scrumMaster.setAccessSprintBacklog(true);
		scrumMaster.setAccessReleasePlan(true);
		scrumMaster.setAccessRetrospective(true);
		scrumMaster.setAccessUnplannedItem(true);
		scrumMaster.setReadReport(true);
		scrumMaster.setEditProject(false);
		scrumRoleMapper.update(scrumMaster);

		ScrumRole scrumTeam = new ScrumRole(project.getName(), ScrumRoleEnum.SCRUM_TEAM);
		scrumTeam.setAccessProductBacklog(false);
		scrumTeam.setAccessSprintPlan(true);
		scrumTeam.setAccessTaskBoard(true);
		scrumTeam.setAccessSprintBacklog(true);
		scrumTeam.setAccessReleasePlan(true);
		scrumTeam.setAccessRetrospective(true);
		scrumTeam.setAccessUnplannedItem(true);
		scrumTeam.setReadReport(true);
		scrumTeam.setEditProject(false);
		scrumRoleMapper.update(scrumTeam);

		ScrumRole stakeholder = new ScrumRole(project.getName(), ScrumRoleEnum.STAKEHOLDER);
		stakeholder.setAccessProductBacklog(false);
		stakeholder.setAccessSprintPlan(false);
		stakeholder.setAccessTaskBoard(false);
		stakeholder.setAccessSprintBacklog(false);
		stakeholder.setAccessReleasePlan(true);
		stakeholder.setAccessRetrospective(false);
		stakeholder.setAccessUnplannedItem(false);
		stakeholder.setReadReport(true);
		stakeholder.setEditProject(false);
		scrumRoleMapper.update(stakeholder);

		ScrumRole guest = new ScrumRole(project.getName(), ScrumRoleEnum.GUEST);
		guest.setAccessProductBacklog(false);
		guest.setAccessSprintPlan(false);
		guest.setAccessTaskBoard(false);
		guest.setAccessSprintBacklog(false);
		guest.setAccessReleasePlan(true);
		guest.setAccessRetrospective(false);
		guest.setAccessUnplannedItem(false);
		guest.setReadReport(true);
		guest.setEditProject(false);
		scrumRoleMapper.update(guest);

		// Call '/projects/{projectName}/scrumroles' API
		Response response = mClient.target(BASE_URL).path("projects/" + project.getName() + "/scrumroles").request()
				.get();

		JSONObject jsonResponse = new JSONObject(response.readEntity(String.class));

		// Assert Product Owner
		JSONObject productOwnerJSON = jsonResponse.getJSONObject(ScrumRoleEnum.PRODUCT_OWNER);
		assertEquals(productOwner.getAccessProductBacklog(),
				productOwnerJSON.getBoolean(ScrumRoleEnum.ACCESS_PRODUCT_BACKLOG));
		assertEquals(productOwner.getAccessSprintPlan(), productOwnerJSON.getBoolean(ScrumRoleEnum.ACCESS_SPRINT_PLAN));
		assertEquals(productOwner.getAccessTaskBoard(), productOwnerJSON.getBoolean(ScrumRoleEnum.ACCESS_TASKBOARD));
		assertEquals(productOwner.getAccessSprintBacklog(),
				productOwnerJSON.getBoolean(ScrumRoleEnum.ACCESS_SPRINT_BACKLOG));
		assertEquals(productOwner.getAccessReleasePlan(),
				productOwnerJSON.getBoolean(ScrumRoleEnum.ACCESS_RELEASE_PLAN));
		assertEquals(productOwner.getAccessRetrospective(),
				productOwnerJSON.getBoolean(ScrumRoleEnum.ACCESS_RETROSPECTIVE));
		assertEquals(productOwner.getAccessUnplannedItem(),
				productOwnerJSON.getBoolean(ScrumRoleEnum.ACCESS_UNPLANNED));
		assertEquals(productOwner.getReadReport(), productOwnerJSON.getBoolean(ScrumRoleEnum.ACCESS_REPORT));
		assertEquals(productOwner.getEditProject(), productOwnerJSON.getBoolean(ScrumRoleEnum.ACCESS_EDIT_PROJECT));

		// Assert Scrum Master
		JSONObject scrumMasterJSON = jsonResponse.getJSONObject(ScrumRoleEnum.SCRUM_MASTER);
		assertEquals(scrumMaster.getAccessProductBacklog(),
				scrumMasterJSON.getBoolean(ScrumRoleEnum.ACCESS_PRODUCT_BACKLOG));
		assertEquals(scrumMaster.getAccessSprintPlan(), scrumMasterJSON.getBoolean(ScrumRoleEnum.ACCESS_SPRINT_PLAN));
		assertEquals(scrumMaster.getAccessTaskBoard(), scrumMasterJSON.getBoolean(ScrumRoleEnum.ACCESS_TASKBOARD));
		assertEquals(scrumMaster.getAccessSprintBacklog(),
				scrumMasterJSON.getBoolean(ScrumRoleEnum.ACCESS_SPRINT_BACKLOG));
		assertEquals(scrumMaster.getAccessReleasePlan(), scrumMasterJSON.getBoolean(ScrumRoleEnum.ACCESS_RELEASE_PLAN));
		assertEquals(scrumMaster.getAccessRetrospective(),
				scrumMasterJSON.getBoolean(ScrumRoleEnum.ACCESS_RETROSPECTIVE));
		assertEquals(scrumMaster.getAccessUnplannedItem(), scrumMasterJSON.getBoolean(ScrumRoleEnum.ACCESS_UNPLANNED));
		assertEquals(scrumMaster.getReadReport(), scrumMasterJSON.getBoolean(ScrumRoleEnum.ACCESS_REPORT));
		assertEquals(scrumMaster.getEditProject(), scrumMasterJSON.getBoolean(ScrumRoleEnum.ACCESS_EDIT_PROJECT));

		// Assert Scrum Team
		JSONObject scrumTeamJSON = jsonResponse.getJSONObject(ScrumRoleEnum.SCRUM_TEAM);
		assertEquals(scrumTeam.getAccessProductBacklog(),
				scrumTeamJSON.getBoolean(ScrumRoleEnum.ACCESS_PRODUCT_BACKLOG));
		assertEquals(scrumTeam.getAccessSprintPlan(), scrumTeamJSON.getBoolean(ScrumRoleEnum.ACCESS_SPRINT_PLAN));
		assertEquals(scrumTeam.getAccessTaskBoard(), scrumTeamJSON.getBoolean(ScrumRoleEnum.ACCESS_TASKBOARD));
		assertEquals(scrumTeam.getAccessSprintBacklog(), scrumTeamJSON.getBoolean(ScrumRoleEnum.ACCESS_SPRINT_BACKLOG));
		assertEquals(scrumTeam.getAccessReleasePlan(), scrumTeamJSON.getBoolean(ScrumRoleEnum.ACCESS_RELEASE_PLAN));
		assertEquals(scrumTeam.getAccessRetrospective(), scrumTeamJSON.getBoolean(ScrumRoleEnum.ACCESS_RETROSPECTIVE));
		assertEquals(scrumTeam.getAccessUnplannedItem(), scrumTeamJSON.getBoolean(ScrumRoleEnum.ACCESS_UNPLANNED));
		assertEquals(scrumTeam.getReadReport(), scrumTeamJSON.getBoolean(ScrumRoleEnum.ACCESS_REPORT));
		assertEquals(scrumTeam.getEditProject(), scrumTeamJSON.getBoolean(ScrumRoleEnum.ACCESS_EDIT_PROJECT));

		// Assert Stakeholder
		JSONObject stakeholderJSON = jsonResponse.getJSONObject(ScrumRoleEnum.STAKEHOLDER);
		assertEquals(stakeholder.getAccessProductBacklog(),
				stakeholderJSON.getBoolean(ScrumRoleEnum.ACCESS_PRODUCT_BACKLOG));
		assertEquals(stakeholder.getAccessSprintPlan(), stakeholderJSON.getBoolean(ScrumRoleEnum.ACCESS_SPRINT_PLAN));
		assertEquals(stakeholder.getAccessTaskBoard(), stakeholderJSON.getBoolean(ScrumRoleEnum.ACCESS_TASKBOARD));
		assertEquals(stakeholder.getAccessSprintBacklog(),
				stakeholderJSON.getBoolean(ScrumRoleEnum.ACCESS_SPRINT_BACKLOG));
		assertEquals(stakeholder.getAccessReleasePlan(), stakeholderJSON.getBoolean(ScrumRoleEnum.ACCESS_RELEASE_PLAN));
		assertEquals(stakeholder.getAccessRetrospective(),
				stakeholderJSON.getBoolean(ScrumRoleEnum.ACCESS_RETROSPECTIVE));
		assertEquals(stakeholder.getAccessUnplannedItem(), stakeholderJSON.getBoolean(ScrumRoleEnum.ACCESS_UNPLANNED));
		assertEquals(stakeholder.getReadReport(), stakeholderJSON.getBoolean(ScrumRoleEnum.ACCESS_REPORT));
		assertEquals(stakeholder.getEditProject(), stakeholderJSON.getBoolean(ScrumRoleEnum.ACCESS_EDIT_PROJECT));

		// Assert Guest
		JSONObject guestJSON = jsonResponse.getJSONObject(ScrumRoleEnum.GUEST);
		assertEquals(guest.getAccessProductBacklog(), guestJSON.getBoolean(ScrumRoleEnum.ACCESS_PRODUCT_BACKLOG));
		assertEquals(guest.getAccessSprintPlan(), guestJSON.getBoolean(ScrumRoleEnum.ACCESS_SPRINT_PLAN));
		assertEquals(guest.getAccessTaskBoard(), guestJSON.getBoolean(ScrumRoleEnum.ACCESS_TASKBOARD));
		assertEquals(guest.getAccessSprintBacklog(), guestJSON.getBoolean(ScrumRoleEnum.ACCESS_SPRINT_BACKLOG));
		assertEquals(guest.getAccessReleasePlan(), guestJSON.getBoolean(ScrumRoleEnum.ACCESS_RELEASE_PLAN));
		assertEquals(guest.getAccessRetrospective(), guestJSON.getBoolean(ScrumRoleEnum.ACCESS_RETROSPECTIVE));
		assertEquals(guest.getAccessUnplannedItem(), guestJSON.getBoolean(ScrumRoleEnum.ACCESS_UNPLANNED));
		assertEquals(guest.getReadReport(), guestJSON.getBoolean(ScrumRoleEnum.ACCESS_REPORT));
		assertEquals(guest.getEditProject(), guestJSON.getBoolean(ScrumRoleEnum.ACCESS_EDIT_PROJECT));
	}

	@Test
	public void testGetProjectRolesInProject() throws Exception {
		IProject project = mCP.getProjectList().get(0);

		// Test Data
		String userName = "TEST_USER_NAME_";
		String userRealName = "TEST_USER_REAL_NAME_";
		String password = "TEST_USER_PASSWORD_";
		String email = "TEST_USER_EMAIL_";
		String enable = "true";

		// Create Accounts
		AccountHelper accountHelper = new AccountHelper();
		// Account 1
		UserInformation userInformation = new UserInformation(userName + 1, userRealName + 1, password + 1, email + 1,
				enable);
		IAccount account1 = accountHelper.createAccount(userInformation, "user");
		// Account 2
		userInformation = new UserInformation(userName + 2, userRealName + 2, password + 2, email + 2, enable);
		IAccount account2 = accountHelper.createAccount(userInformation, "user");

		accountHelper.assignRole_add(mConfig.getUserSession(), account1.getID(),
				project.getName(), ScrumRoleEnum.PRODUCT_OWNER);
		accountHelper.assignRole_add(mConfig.getUserSession(), account2.getID(),
				project.getName(), ScrumRoleEnum.SCRUM_MASTER);
		
		// Call '/projects/{projectName}/projectroles' API
		Response response = mClient.target(BASE_URL).path("projects/" + project.getName() + "/projectroles").request()
				.get();

		JSONArray jsonResponse = new JSONArray(response.readEntity(String.class));
		
		ProjectMapper projectMapper = new ProjectMapper();
		List<IAccount> projectRoles = projectMapper.getProjectMemberList(null, project);
		
		// Assert
		assertEquals(2, jsonResponse.length());
		assertEquals(JSONEncoder.toProjectRoleJSONArray(project.getName(), projectRoles).toString(), jsonResponse.toString());
	}
}
