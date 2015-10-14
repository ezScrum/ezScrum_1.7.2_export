package ntut.csie.ezScrum.web.support.export;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ntut.csie.ezScrum.iteration.core.ISprintPlanDesc;
import ntut.csie.ezScrum.test.CreateData.CopyProject;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.ezScrum.web.databaseEnum.ProjectEnum;
import ntut.csie.ezScrum.web.databaseEnum.SprintEnum;
import ntut.csie.ezScrum.web.helper.SprintPlanHelper;
import ntut.csie.jcis.resource.core.IProject;

public class JSONEncoderTest {
	private ezScrumInfoConfig mConfig = new ezScrumInfoConfig();
	private CreateProject mCP;
	private CreateSprint mCS;
	
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
	}

	@After
	public void tearDown() {
		// 初始化 SQL
		InitialSQL ini = new InitialSQL(mConfig);
		ini.exe();

		// 刪除測試檔案
		CopyProject copyProject = new CopyProject(mCP);
		copyProject.exeDelete_Project();

		// ============= release ==============
		ini = null;
		copyProject = null;
		mCP = null;
		mCS = null;
	}
	
	@Test
	public void testToSprintJSONArray() throws JSONException {
		IProject project = mCP.getProjectList().get(0);
		SprintPlanHelper sprintPlanHelper = new SprintPlanHelper(project);
		List<ISprintPlanDesc> sprints = sprintPlanHelper.loadListPlans();
		JSONArray sprintJSONArray = JSONEncoder.toSprintJSONArray(sprints);
		
		// Assert Sprint1
		ISprintPlanDesc sprint1 = sprints.get(0);
		JSONObject sprint1JSON = sprintJSONArray.getJSONObject(0);
		assertEquals(sprint1.getGoal(), sprint1JSON.getString(SprintEnum.GOAL));
		assertEquals(sprint1.getInterval(), sprint1JSON.getString(SprintEnum.INTERVAL));
		assertEquals(sprint1.getMemberNumber(), sprint1JSON.getString(SprintEnum.TEAM_SIZE));
		assertEquals(sprint1.getAvailableDays(), sprint1JSON.getString(SprintEnum.AVAILABLE_HOURS));
		assertEquals(sprint1.getFocusFactor(), sprint1JSON.getString(SprintEnum.FOCUS_FACTOR));
		assertEquals(sprint1.getStartDate(), sprint1JSON.getString(SprintEnum.START_DATE));
		assertEquals(sprint1.getEndDate(), sprint1JSON.getString(SprintEnum.DUE_DATE));
		assertEquals(sprint1.getDemoDate(), sprint1JSON.getString(SprintEnum.DEMO_DATE));
		assertEquals(sprint1.getDemoPlace(), sprint1JSON.getString(SprintEnum.DEMO_PLACE));
		assertEquals(sprint1.getNotes(), sprint1JSON.getString(SprintEnum.DAILY_INFO));
		
		// Assert Sprint2
		ISprintPlanDesc sprint2 = sprints.get(1);
		JSONObject sprint2JSON = sprintJSONArray.getJSONObject(1);
		assertEquals(sprint2.getGoal(), sprint2JSON.getString(SprintEnum.GOAL));
		assertEquals(sprint2.getInterval(), sprint2JSON.getString(SprintEnum.INTERVAL));
		assertEquals(sprint2.getMemberNumber(), sprint2JSON.getString(SprintEnum.TEAM_SIZE));
		assertEquals(sprint2.getAvailableDays(), sprint2JSON.getString(SprintEnum.AVAILABLE_HOURS));
		assertEquals(sprint2.getFocusFactor(), sprint2JSON.getString(SprintEnum.FOCUS_FACTOR));
		assertEquals(sprint2.getStartDate(), sprint2JSON.getString(SprintEnum.START_DATE));
		assertEquals(sprint2.getEndDate(), sprint2JSON.getString(SprintEnum.DUE_DATE));
		assertEquals(sprint2.getDemoDate(), sprint2JSON.getString(SprintEnum.DEMO_DATE));
		assertEquals(sprint2.getDemoPlace(), sprint2JSON.getString(SprintEnum.DEMO_PLACE));
		assertEquals(sprint2.getNotes(), sprint2JSON.getString(SprintEnum.DAILY_INFO));
	}
	
	@Test
	public void testToSprintJSON() throws JSONException {
		String sprintId = mCS.getSprintIDList().get(0);
		IProject project = mCP.getProjectList().get(0);
		SprintPlanHelper sprintPlanHelper = new SprintPlanHelper(project);
		ISprintPlanDesc sprint = sprintPlanHelper.loadPlan(sprintId);
		JSONObject sprintJSON = JSONEncoder.toSprintJSON(sprint);
		
		// Assert
		assertEquals(sprint.getGoal(), sprintJSON.getString(SprintEnum.GOAL));
		assertEquals(sprint.getInterval(), sprintJSON.getString(SprintEnum.INTERVAL));
		assertEquals(sprint.getMemberNumber(), sprintJSON.getString(SprintEnum.TEAM_SIZE));
		assertEquals(sprint.getAvailableDays(), sprintJSON.getString(SprintEnum.AVAILABLE_HOURS));
		assertEquals(sprint.getFocusFactor(), sprintJSON.getString(SprintEnum.FOCUS_FACTOR));
		assertEquals(sprint.getStartDate(), sprintJSON.getString(SprintEnum.START_DATE));
		assertEquals(sprint.getEndDate(), sprintJSON.getString(SprintEnum.DUE_DATE));
		assertEquals(sprint.getDemoDate(), sprintJSON.getString(SprintEnum.DEMO_DATE));
		assertEquals(sprint.getDemoPlace(), sprintJSON.getString(SprintEnum.DEMO_PLACE));
		assertEquals(sprint.getNotes(), sprintJSON.getString(SprintEnum.DAILY_INFO));
	}
	
	@Test
	public void testToProjectJSONArray() throws JSONException {
		List<IProject> projects = mCP.getProjectList();
		JSONArray projectJSONArray = JSONEncoder.toProjectJSONArray(projects);
		
		// Assert Project1
		IProject project1 = projects.get(0);
		JSONObject project1JSON = projectJSONArray.getJSONObject(0);
		assertEquals(project1.getName(), project1JSON.getString(ProjectEnum.NAME));
		assertEquals(project1.getProjectDesc().getDisplayName(), project1JSON.getString(ProjectEnum.DISPLAY_NAME));
		assertEquals(project1.getProjectDesc().getComment(), project1JSON.getString(ProjectEnum.COMMENT));
		assertEquals(project1.getProjectDesc().getProjectManager(), project1JSON.getString(ProjectEnum.PRODUCT_OWNER));
		assertEquals(project1.getProjectDesc().getAttachFileSize(), project1JSON.getString(ProjectEnum.ATTATCH_MAX_SIZE));
		// Assert Project2
		IProject project2 = projects.get(1);
		JSONObject project2JSON = JSONEncoder.toProjectJSON(project2);
		assertEquals(project2.getName(), project2JSON.getString(ProjectEnum.NAME));
		assertEquals(project2.getProjectDesc().getDisplayName(), project2JSON.getString(ProjectEnum.DISPLAY_NAME));
		assertEquals(project2.getProjectDesc().getComment(), project2JSON.getString(ProjectEnum.COMMENT));
		assertEquals(project2.getProjectDesc().getProjectManager(), project2JSON.getString(ProjectEnum.PRODUCT_OWNER));
		assertEquals(project2.getProjectDesc().getAttachFileSize(), project2JSON.getString(ProjectEnum.ATTATCH_MAX_SIZE));
	}
	
	@Test
	public void testToProjectJSON() throws JSONException {
		IProject project = mCP.getProjectList().get(0);
		JSONObject projectJSON = JSONEncoder.toProjectJSON(project);
		
		// Assert
		assertEquals(project.getName(), projectJSON.getString(ProjectEnum.NAME));
		assertEquals(project.getProjectDesc().getDisplayName(), projectJSON.getString(ProjectEnum.DISPLAY_NAME));
		assertEquals(project.getProjectDesc().getComment(), projectJSON.getString(ProjectEnum.COMMENT));
		assertEquals(project.getProjectDesc().getProjectManager(), projectJSON.getString(ProjectEnum.PRODUCT_OWNER));
		assertEquals(project.getProjectDesc().getAttachFileSize(), projectJSON.getString(ProjectEnum.ATTATCH_MAX_SIZE));
	}
}
