package ntut.csie.ezScrum.restful.export.support;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.issue.core.IIssueHistory;
import ntut.csie.ezScrum.issue.core.ITSEnum;
import ntut.csie.ezScrum.issue.internal.IssueHistory;
import ntut.csie.ezScrum.iteration.core.ScrumEnum;
import ntut.csie.ezScrum.restful.export.jsonEnum.HistoryJSONEnum;
import ntut.csie.ezScrum.test.CreateData.AddStoryToSprint;
import ntut.csie.ezScrum.test.CreateData.AddTaskToStory;
import ntut.csie.ezScrum.test.CreateData.CopyProject;
import ntut.csie.ezScrum.test.CreateData.CreateProductBacklog;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateRelease;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.test.CreateData.CreateTask;
import ntut.csie.ezScrum.test.CreateData.CreateUnplannedItem;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;

public class HistoryTranslatorTest {
	private ezScrumInfoConfig mConfig = new ezScrumInfoConfig();
	private CreateProject mCP;
	private CreateRelease mCR;
	private CreateSprint mCS;
	private CreateUnplannedItem mCU;
	private AddStoryToSprint mASTS;
	private AddTaskToStory mATTS;
	private CreateTask mCT;

	@Before
	public void setUp() throws Exception {
		// 初始化 SQL
		InitialSQL ini = new InitialSQL(mConfig);
		ini.exe();

		// Create Project
		mCP = new CreateProject(2);
		mCP.exeCreate();
		
		// Create Release
		mCR = new CreateRelease(1, mCP);
		mCR.exe();

		// Create Sprint
		mCS = new CreateSprint(2, mCP);
		mCS.exe();

		// Add Story to Sprint
		mASTS = new AddStoryToSprint(2, 8, mCS, mCP, CreateProductBacklog.TYPE_ESTIMATION);
		mASTS.exe();
		
		// Add Task to Story
		mATTS = new AddTaskToStory(2, 13, mASTS, mCP);
		mATTS.exe();

		// Create Dropped Task
		mCT = new CreateTask(2, mCP);
		mCT.exe();
		
		// Create Unplan
		mCU = new CreateUnplannedItem(2, mCP, mCS);
		mCU.exe();
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
		mCR = null;
		mCS = null;
		mCU = null;
		mASTS = null;
		mCT = null;
	}
	
	@Test
	public void testToNewHistory_CreateStoryInSprint() {
		IssueHistory oldHistory = new IssueHistory();
		long modifyDate = System.currentTimeMillis();
		oldHistory.setType(IIssueHistory.OTHER_TYPE);
		oldHistory.setFieldName("Sprint");
		oldHistory.setOldValue("-1");
		oldHistory.setNewValue("1");
		oldHistory.setModifyDate(modifyDate);
		
		IIssueHistory newHistory = HistoryTranslator.toNewHistory(oldHistory, ScrumEnum.STORY_ISSUE_TYPE);
		assertEquals(HistoryJSONEnum.TYPE_APPEND, newHistory.getType());
		assertEquals("", newHistory.getOldValue());
		assertEquals("1", newHistory.getNewValue());
		assertEquals(modifyDate, newHistory.getModifyDate());
	}
	
	@Test
	public void testToNewHistory_ModifyName() {
		IssueHistory oldHistory = new IssueHistory();
		long modifyDate = System.currentTimeMillis();
		oldHistory.setType(IIssueHistory.OTHER_TYPE);
		oldHistory.setFieldName(IIssueHistory.SUMMARY);
		oldHistory.setOldValue("Old Story Name");
		oldHistory.setNewValue("New Story Name");
		oldHistory.setModifyDate(modifyDate);
		
		IIssueHistory newHistory = HistoryTranslator.toNewHistory(oldHistory, ScrumEnum.STORY_ISSUE_TYPE);
		assertEquals(HistoryJSONEnum.TYPE_NAME, newHistory.getType());
		assertEquals("Old Story Name", newHistory.getOldValue());
		assertEquals("New Story Name", newHistory.getNewValue());
		assertEquals(modifyDate, newHistory.getModifyDate());
	}
	
	@Test
	public void testToNewHistory_ModifyTaskStatus_NewToAssigned() {
		IIssue task = mATTS.getTaskList().get(0);
		IssueHistory oldHistory = new IssueHistory();
		long modifyDate = System.currentTimeMillis();
		oldHistory.setIssueID(task.getIssueID());
		oldHistory.setType(IIssueHistory.OTHER_TYPE);
		oldHistory.setFieldName(IssueHistory.STATUS_FIELD_NAME);
		oldHistory.setOldValue(String.valueOf(ITSEnum.NEW_STATUS));
		oldHistory.setNewValue(String.valueOf(ITSEnum.ASSIGNED_STATUS));
		oldHistory.setModifyDate(modifyDate);
		
		IIssueHistory newHistory = HistoryTranslator.toNewHistory(oldHistory, task.getCategory());
		assertEquals(HistoryJSONEnum.TYPE_STATUS, newHistory.getType());
		assertEquals(HistoryJSONEnum.TASK_STATUS_UNCHECK, newHistory.getOldValue());
		assertEquals(HistoryJSONEnum.TASK_STATUS_CHECK, newHistory.getNewValue());
		assertEquals(modifyDate, newHistory.getModifyDate());
	}
	
	@Test
	public void testToNewHistory_ModifyTaskStatus_AssignedToClosed() {
		IIssue task = mATTS.getTaskList().get(0);
		IssueHistory oldHistory = new IssueHistory();
		long modifyDate = System.currentTimeMillis();
		oldHistory.setIssueID(task.getIssueID());
		oldHistory.setType(IIssueHistory.OTHER_TYPE);
		oldHistory.setFieldName(IssueHistory.STATUS_FIELD_NAME);
		oldHistory.setOldValue(String.valueOf(ITSEnum.ASSIGNED_STATUS));
		oldHistory.setNewValue(String.valueOf(ITSEnum.CLOSED_STATUS));
		oldHistory.setModifyDate(modifyDate);
		
		IIssueHistory newHistory = HistoryTranslator.toNewHistory(oldHistory, task.getCategory());
		assertEquals(HistoryJSONEnum.TYPE_STATUS, newHistory.getType());
		assertEquals(HistoryJSONEnum.TASK_STATUS_CHECK, newHistory.getOldValue());
		assertEquals(HistoryJSONEnum.TASK_STATUS_DONE, newHistory.getNewValue());
		assertEquals(modifyDate, newHistory.getModifyDate());
	}
	
	@Test
	public void testToNewHistory_ModifyUnplanStatus_NewToAssigned() {
		IIssue unplan = mCU.getIssueList().get(0);
		IssueHistory oldHistory = new IssueHistory();
		long modifyDate = System.currentTimeMillis();
		oldHistory.setIssueID(unplan.getIssueID());
		oldHistory.setType(IIssueHistory.OTHER_TYPE);
		oldHistory.setFieldName(IssueHistory.STATUS_FIELD_NAME);
		oldHistory.setOldValue(String.valueOf(ITSEnum.NEW_STATUS));
		oldHistory.setNewValue(String.valueOf(ITSEnum.ASSIGNED_STATUS));
		oldHistory.setModifyDate(modifyDate);
		
		IIssueHistory newHistory = HistoryTranslator.toNewHistory(oldHistory, unplan.getCategory());
		assertEquals(HistoryJSONEnum.TYPE_STATUS, newHistory.getType());
		assertEquals(HistoryJSONEnum.TASK_STATUS_UNCHECK, newHistory.getOldValue());
		assertEquals(HistoryJSONEnum.TASK_STATUS_CHECK, newHistory.getNewValue());
		assertEquals(modifyDate, newHistory.getModifyDate());
	}
	
	@Test
	public void testToNewHistory_ModifyUnplanStatus_AssignedToClosed() {
		IIssue unplan = mCU.getIssueList().get(0);
		IssueHistory oldHistory = new IssueHistory();
		long modifyDate = System.currentTimeMillis();
		oldHistory.setIssueID(unplan.getIssueID());
		oldHistory.setType(IIssueHistory.OTHER_TYPE);
		oldHistory.setFieldName(IssueHistory.STATUS_FIELD_NAME);
		oldHistory.setOldValue(String.valueOf(ITSEnum.ASSIGNED_STATUS));
		oldHistory.setNewValue(String.valueOf(ITSEnum.CLOSED_STATUS));
		oldHistory.setModifyDate(modifyDate);
		
		IIssueHistory newHistory = HistoryTranslator.toNewHistory(oldHistory, unplan.getCategory());
		assertEquals(HistoryJSONEnum.TYPE_STATUS, newHistory.getType());
		assertEquals(HistoryJSONEnum.TASK_STATUS_CHECK, newHistory.getOldValue());
		assertEquals(HistoryJSONEnum.TASK_STATUS_DONE, newHistory.getNewValue());
		assertEquals(modifyDate, newHistory.getModifyDate());
	}
	
	@Test
	public void testToNewHistory_ModifyStoryStatus_NewToClosed() {
		IIssue story = mASTS.getIssueList().get(0);
		IssueHistory oldHistory = new IssueHistory();
		long modifyDate = System.currentTimeMillis();
		oldHistory.setIssueID(story.getIssueID());
		oldHistory.setType(IIssueHistory.OTHER_TYPE);
		oldHistory.setFieldName(IssueHistory.STATUS_FIELD_NAME);
		oldHistory.setOldValue(String.valueOf(ITSEnum.NEW_STATUS));
		oldHistory.setNewValue(String.valueOf(ITSEnum.CLOSED_STATUS));
		oldHistory.setModifyDate(modifyDate);
		
		IIssueHistory newHistory = HistoryTranslator.toNewHistory(oldHistory, story.getCategory());
		assertEquals(HistoryJSONEnum.TYPE_STATUS, newHistory.getType());
		assertEquals(HistoryJSONEnum.STORY_STATUS_UNCHECK, newHistory.getOldValue());
		assertEquals(HistoryJSONEnum.STORY_STATUS_DONE, newHistory.getNewValue());
		assertEquals(modifyDate, newHistory.getModifyDate());
	}
}
