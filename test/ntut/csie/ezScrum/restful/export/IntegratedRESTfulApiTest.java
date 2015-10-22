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

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.iteration.core.ISprintPlanDesc;
import ntut.csie.ezScrum.test.CreateData.AddStoryToSprint;
import ntut.csie.ezScrum.test.CreateData.AddTaskToStory;
import ntut.csie.ezScrum.test.CreateData.CopyProject;
import ntut.csie.ezScrum.test.CreateData.CreateProductBacklog;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.ezScrum.web.databaseEnum.ExportEnum;
import ntut.csie.ezScrum.web.databaseEnum.ProjectEnum;
import ntut.csie.ezScrum.web.databaseEnum.SprintEnum;
import ntut.csie.ezScrum.web.databaseEnum.StoryEnum;
import ntut.csie.ezScrum.web.databaseEnum.TaskEnum;
import ntut.csie.ezScrum.web.helper.SprintBacklogHelper;
import ntut.csie.ezScrum.web.logic.ProductBacklogLogic;
import ntut.csie.ezScrum.web.support.export.ResourceFinder;
import ntut.csie.jcis.resource.core.IProject;

public class IntegratedRESTfulApiTest extends JerseyTest {
	private ezScrumInfoConfig mConfig = new ezScrumInfoConfig();
	private CreateProject mCP;
	private CreateSprint mCS;
	private AddStoryToSprint mASTS;
	private AddTaskToStory mATTS;

	private Client mClient;
	private HttpServer mHttpServer;
	private ResourceConfig mResourceConfig;
	private static String BASE_URL = "http://localhost:8080/ezScrum/resource/";
	private URI mBaseUri = URI.create(BASE_URL);

	@Override
	protected Application configure() {
		mResourceConfig = new ResourceConfig(ProjectRESTfulApi.class, SprintRESTfulApi.class, StoryRESTfulApi.class,
		                                     TaskRESTfulApi.class, WildStoryRESTfulApi.class, WildTaskRESTfulApi.class,
		                                     IntegratedRESTfulApi.class);
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

		// Add Story to project
		mASTS = new AddStoryToSprint(2, 8, mCS, mCP, CreateProductBacklog.TYPE_ESTIMATION);
		mASTS.exe();

		// Add Task to project
		mATTS = new AddTaskToStory(2, 13, mASTS, mCP);
		mATTS.exe();
				
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
		mASTS = null;
		mATTS = null;
		mHttpServer = null;
		mClient = null;
	}
	
	@Test
	public void testGetExportedJSON() throws InterruptedException, JSONException {
		// Test Data
		// Project 1
		IProject project = mCP.getProjectList().get(0);
		IIssue story1 = mASTS.getIssueList().get(0);
		IIssue story2 = mASTS.getIssueList().get(1);
		IIssue task1 = mATTS.getTaskList().get(0);
		IIssue task2 = mATTS.getTaskList().get(1);
		IIssue task3 = mATTS.getTaskList().get(2);
		IIssue task4 = mATTS.getTaskList().get(3);
		ResourceFinder resourceFinder = new ResourceFinder();
		resourceFinder.findProject(project.getName());
		ISprintPlanDesc sprint = resourceFinder.findSprint(Long.parseLong(mCS.getSprintIDList().get(0)));
		
		// Drop Story1
		ProductBacklogLogic productBacklogLogic = new ProductBacklogLogic(null, project);
		
		// It's need some delay for manipulating file IO (add story to sprint)
		Thread.sleep(1000);
		// Remove story1 from Sprint
		productBacklogLogic.removeStoryFromSprint(story1.getIssueID());
		
		// Drop task1, task3 from story
		SprintBacklogHelper sprintBacklogHelper = new SprintBacklogHelper(project, null);
		sprintBacklogHelper.removeTask(task1.getIssueID(), story1.getIssueID());
		sprintBacklogHelper.removeTask(task3.getIssueID(), story2.getIssueID());
		
		Response response = mClient.target(mBaseUri)
		        .path("export/projects")
		        .request()
		        .get();
		
		JSONObject jsonArrayResponse = new JSONObject(response.readEntity(String.class));
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		JSONArray projectJSONArray = jsonArrayResponse.getJSONArray(ExportEnum.PROJECTS);
		assertEquals(1, projectJSONArray.length());
		
		// Assert project data
		JSONObject projectJSON = projectJSONArray.getJSONObject(0);
		assertEquals(project.getName(), projectJSON.getString(ProjectEnum.NAME));
		assertEquals(project.getProjectDesc().getDisplayName(), projectJSON.getString(ProjectEnum.DISPLAY_NAME));
		assertEquals(project.getProjectDesc().getComment(), projectJSON.getString(ProjectEnum.COMMENT));
		assertEquals(project.getProjectDesc().getProjectManager(), projectJSON.getString(ProjectEnum.PRODUCT_OWNER));
		assertEquals(Long.parseLong(project.getProjectDesc().getAttachFileSize()), projectJSON.getLong(ProjectEnum.ATTATCH_MAX_SIZE));
		JSONArray sprintJSONArray = projectJSON.getJSONArray(ExportEnum.SPRINTS);
		assertEquals(1, sprintJSONArray.length());
		// end
		
		// Assert sprint data
		JSONObject sprintJSON = sprintJSONArray.getJSONObject(0);
		assertEquals(Long.parseLong(sprint.getID()), sprintJSON.getLong(SprintEnum.ID));
		assertEquals(sprint.getGoal(), sprintJSON.getString(SprintEnum.GOAL));
		assertEquals(Integer.parseInt(sprint.getInterval()), sprintJSON.getInt(SprintEnum.INTERVAL));
		assertEquals(Integer.parseInt(sprint.getMemberNumber()), sprintJSON.getInt(SprintEnum.TEAM_SIZE));
		assertEquals(Integer.parseInt(sprint.getAvailableDays()), sprintJSON.getInt(SprintEnum.AVAILABLE_HOURS));
		assertEquals(Integer.parseInt(sprint.getFocusFactor()), sprintJSON.getInt(SprintEnum.FOCUS_FACTOR));
		assertEquals(sprint.getStartDate(), sprintJSON.getString(SprintEnum.START_DATE));
		assertEquals(sprint.getEndDate(), sprintJSON.getString(SprintEnum.DUE_DATE));
		assertEquals(sprint.getDemoDate(), sprintJSON.getString(SprintEnum.DEMO_DATE));
		assertEquals(sprint.getDemoPlace(), sprintJSON.getString(SprintEnum.DEMO_PLACE));
		assertEquals(sprint.getNotes(), sprintJSON.getString(SprintEnum.DAILY_INFO));
		JSONArray storyJSONArray = sprintJSON.getJSONArray(ExportEnum.STORIES);
		assertEquals(1, storyJSONArray.length());
		// end
		
		// Assert story2 data
		JSONObject story2JSON = storyJSONArray.getJSONObject(0);
		assertEquals(story2.getIssueID(), story2JSON.getLong(StoryEnum.ID));
		assertEquals(story2.getSummary(), story2JSON.getString(StoryEnum.NAME));
		assertEquals(story2.getStatus(), story2JSON.getString(StoryEnum.STATUS));
		assertEquals(Integer.parseInt(story2.getEstimated()), story2JSON.getInt(StoryEnum.ESTIMATE));
		assertEquals(Integer.parseInt(story2.getImportance()), story2JSON.getInt(StoryEnum.IMPORTANCE));
		assertEquals(Integer.parseInt(story2.getValue()), story2JSON.getInt(StoryEnum.VALUE));
		assertEquals(story2.getNotes(), story2JSON.getString(StoryEnum.NOTES));
		assertEquals(story2.getHowToDemo(), story2JSON.getString(StoryEnum.HOW_TO_DEMO));
		JSONArray taskInStory2JSONArray = story2JSON.getJSONArray(ExportEnum.TASKS);
		assertEquals(1, taskInStory2JSONArray.length());
		// end
		
		// Assert task4 data
		JSONObject task4JSON = taskInStory2JSONArray.getJSONObject(0);
		assertEquals(task4.getSummary(), task4JSON.getString(TaskEnum.NAME));
		assertEquals(task4.getAssignto(), task4JSON.getString(TaskEnum.HANDLER));
		assertEquals(Integer.parseInt(task4.getEstimated()), task4JSON.getInt(TaskEnum.ESTIMATE));
		assertEquals(Integer.parseInt(task4.getRemains()), task4JSON.getInt(TaskEnum.REMAIN));
		assertEquals(Integer.parseInt(task4.getActualHour()), task4JSON.getInt(TaskEnum.ACTUAL));
		assertEquals(task4.getNotes(), task4JSON.getString(TaskEnum.NOTES));
		assertEquals(task4.getStatus(), task4JSON.getString(TaskEnum.STATUS));
		// end
		
		JSONArray wildStoryJSONArray = projectJSON.getJSONArray(ExportEnum.WILD_STORIES);
		assertEquals(1, wildStoryJSONArray.length());
		
		// Assert wild story1 data
		JSONObject wildStory1JSON = wildStoryJSONArray.getJSONObject(0);
		assertEquals(story1.getIssueID(), wildStory1JSON.getLong(StoryEnum.ID));
		assertEquals(story1.getSummary(), wildStory1JSON.getString(StoryEnum.NAME));
		assertEquals(story2.getStatus(), wildStory1JSON.getString(StoryEnum.STATUS));
		assertEquals(Integer.parseInt(story1.getEstimated()), wildStory1JSON.getInt(StoryEnum.ESTIMATE));
		assertEquals(Integer.parseInt(story1.getImportance()), wildStory1JSON.getInt(StoryEnum.IMPORTANCE));
		assertEquals(Integer.parseInt(story1.getValue()), wildStory1JSON.getInt(StoryEnum.VALUE));
		assertEquals(story1.getNotes(), wildStory1JSON.getString(StoryEnum.NOTES));
		assertEquals(story1.getHowToDemo(), wildStory1JSON.getString(StoryEnum.HOW_TO_DEMO));
		JSONArray taskInWildStory1JSONArray = wildStory1JSON.getJSONArray(ExportEnum.TASKS);
		assertEquals(1, taskInWildStory1JSONArray.length());
		// end
		
		// Assert task2 data
		JSONObject task2JSON = taskInWildStory1JSONArray.getJSONObject(0);
		assertEquals(task2.getSummary(), task2JSON.getString(TaskEnum.NAME));
		assertEquals(task2.getAssignto(), task2JSON.getString(TaskEnum.HANDLER));
		assertEquals(Integer.parseInt(task2.getEstimated()), task2JSON.getInt(TaskEnum.ESTIMATE));
		assertEquals(Integer.parseInt(task2.getRemains()), task2JSON.getInt(TaskEnum.REMAIN));
		assertEquals(Integer.parseInt(task2.getActualHour()), task2JSON.getInt(TaskEnum.ACTUAL));
		assertEquals(task2.getNotes(), task2JSON.getString(TaskEnum.NOTES));
		assertEquals(task2.getStatus(), task2JSON.getString(TaskEnum.STATUS));
		// end
		
		JSONArray wildTaskJSONArray = projectJSON.getJSONArray(ExportEnum.WILD_TASKS);
		assertEquals(2, wildTaskJSONArray.length());
		
		// Assert wild task1 data
		JSONObject wildTask1JSON = wildTaskJSONArray.getJSONObject(0);
		assertEquals(task1.getSummary(), wildTask1JSON.getString(TaskEnum.NAME));
		assertEquals(task1.getAssignto(), wildTask1JSON.getString(TaskEnum.HANDLER));
		assertEquals(Integer.parseInt(task1.getEstimated()), wildTask1JSON.getInt(TaskEnum.ESTIMATE));
		assertEquals(Integer.parseInt(task1.getRemains()), wildTask1JSON.getInt(TaskEnum.REMAIN));
		assertEquals(Integer.parseInt(task1.getActualHour()), wildTask1JSON.getInt(TaskEnum.ACTUAL));
		assertEquals(task1.getNotes(), wildTask1JSON.getString(TaskEnum.NOTES));
		assertEquals(task1.getStatus(), wildTask1JSON.getString(TaskEnum.STATUS));
		// end
		
		// Assert wild task3 data
		JSONObject wildTask3JSON = wildTaskJSONArray.getJSONObject(1);
		assertEquals(task3.getSummary(), wildTask3JSON.getString(TaskEnum.NAME));
		assertEquals(task3.getAssignto(), wildTask3JSON.getString(TaskEnum.HANDLER));
		assertEquals(Integer.parseInt(task3.getEstimated()), wildTask3JSON.getInt(TaskEnum.ESTIMATE));
		assertEquals(Integer.parseInt(task3.getRemains()), wildTask3JSON.getInt(TaskEnum.REMAIN));
		assertEquals(Integer.parseInt(task3.getActualHour()), wildTask3JSON.getInt(TaskEnum.ACTUAL));
		assertEquals(task3.getNotes(), wildTask3JSON.getString(TaskEnum.NOTES));
		assertEquals(task3.getStatus(), wildTask3JSON.getString(TaskEnum.STATUS));
		// end
	}
}
