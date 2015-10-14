package ntut.csie.ezScrum.web.support.export;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.test.CreateData.AddStoryToSprint;
import ntut.csie.ezScrum.test.CreateData.CopyProject;
import ntut.csie.ezScrum.test.CreateData.CreateProductBacklog;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.ezScrum.web.databaseEnum.StoryEnum;

public class JSONEncoderTest {
	private ezScrumInfoConfig mConfig = new ezScrumInfoConfig();
	private CreateProject mCP;
	private CreateSprint mCS;
	private AddStoryToSprint mASTS;

	@Before
	public void setUp() throws Exception {
		// 初始化 SQL
		InitialSQL ini = new InitialSQL(mConfig);
		ini.exe();

		// Create Project
		mCP = new CreateProject(2);
		mCP.exeCreate();

		//	 新增兩個Sprint
		this.mCS = new CreateSprint(2, mCP);
		this.mCS.exe();

		// Add Story to project
		mASTS = new AddStoryToSprint(2, 8, mCS, mCP, CreateProductBacklog.TYPE_ESTIMATION);
		mASTS.exe();
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
	public void tstToStoryJSONArray() throws JSONException {
		// Get Stories
		List<IIssue> stories = mASTS.getIssueList();
		// Convert to JSONArray
		JSONArray storyJSONArray = JSONEncoder.toStoryJSONArray(stories);

		// Assert
		IIssue story1 = stories.get(0);
		assertEquals(story1.getSummary(), storyJSONArray.getJSONObject(0).get(StoryEnum.NAME));
		assertEquals(story1.getStatus(), storyJSONArray.getJSONObject(0).get(StoryEnum.STATUS));
		assertEquals(Integer.parseInt(story1.getEstimated()), storyJSONArray.getJSONObject(0).get(StoryEnum.ESTIMATE));
		assertEquals(Integer.parseInt(story1.getImportance()), storyJSONArray.getJSONObject(0).get(StoryEnum.IMPORTANCE));
		assertEquals(Integer.parseInt(story1.getValue()), storyJSONArray.getJSONObject(0).get(StoryEnum.VALUE));
		assertEquals(story1.getNotes(), storyJSONArray.getJSONObject(0).get(StoryEnum.NOTES));
		assertEquals(story1.getHowToDemo(), storyJSONArray.getJSONObject(0).get(StoryEnum.HOW_TO_DEMO));

		IIssue story2 = stories.get(1);
		assertEquals(story2.getSummary(), storyJSONArray.getJSONObject(1).get(StoryEnum.NAME));
		assertEquals(story2.getStatus(), storyJSONArray.getJSONObject(1).get(StoryEnum.STATUS));
		assertEquals(Integer.parseInt(story2.getEstimated()), storyJSONArray.getJSONObject(1).get(StoryEnum.ESTIMATE));
		assertEquals(Integer.parseInt(story2.getImportance()), storyJSONArray.getJSONObject(1).get(StoryEnum.IMPORTANCE));
		assertEquals(Integer.parseInt(story2.getValue()), storyJSONArray.getJSONObject(1).get(StoryEnum.VALUE));
		assertEquals(story2.getNotes(), storyJSONArray.getJSONObject(1).get(StoryEnum.NOTES));
		assertEquals(story2.getHowToDemo(), storyJSONArray.getJSONObject(1).get(StoryEnum.HOW_TO_DEMO));

		IIssue story3 = stories.get(2);
		assertEquals(story3.getSummary(), storyJSONArray.getJSONObject(2).get(StoryEnum.NAME));
		assertEquals(story3.getStatus(), storyJSONArray.getJSONObject(2).get(StoryEnum.STATUS));
		assertEquals(Integer.parseInt(story3.getEstimated()), storyJSONArray.getJSONObject(2).get(StoryEnum.ESTIMATE));
		assertEquals(Integer.parseInt(story3.getImportance()), storyJSONArray.getJSONObject(2).get(StoryEnum.IMPORTANCE));
		assertEquals(Integer.parseInt(story3.getValue()), storyJSONArray.getJSONObject(2).get(StoryEnum.VALUE));
		assertEquals(story3.getNotes(), storyJSONArray.getJSONObject(2).get(StoryEnum.NOTES));
		assertEquals(story3.getHowToDemo(), storyJSONArray.getJSONObject(2).get(StoryEnum.HOW_TO_DEMO));

		IIssue story4 = stories.get(3);
		assertEquals(story4.getSummary(), storyJSONArray.getJSONObject(3).get(StoryEnum.NAME));
		assertEquals(story4.getStatus(), storyJSONArray.getJSONObject(3).get(StoryEnum.STATUS));
		assertEquals(Integer.parseInt(story4.getEstimated()), storyJSONArray.getJSONObject(3).get(StoryEnum.ESTIMATE));
		assertEquals(Integer.parseInt(story4.getImportance()), storyJSONArray.getJSONObject(3).get(StoryEnum.IMPORTANCE));
		assertEquals(Integer.parseInt(story4.getValue()), storyJSONArray.getJSONObject(3).get(StoryEnum.VALUE));
		assertEquals(story4.getNotes(), storyJSONArray.getJSONObject(3).get(StoryEnum.NOTES));
		assertEquals(story4.getHowToDemo(), storyJSONArray.getJSONObject(3).get(StoryEnum.HOW_TO_DEMO));
	}

	@Test
	public void toStoryJSON() throws JSONException {
		// Get Stories
		IIssue story = mASTS.getIssueList().get(0);
		// Convert to JSONObject
		JSONObject storyJson = JSONEncoder.toStoryJSON(story);

		// Assert
		assertEquals(story.getSummary(), storyJson.get(StoryEnum.NAME));
		assertEquals(story.getStatus(), storyJson.get(StoryEnum.STATUS));
		assertEquals(Integer.parseInt(story.getEstimated()), storyJson.get(StoryEnum.ESTIMATE));
		assertEquals(Integer.parseInt(story.getImportance()), storyJson.get(StoryEnum.IMPORTANCE));
		assertEquals(Integer.parseInt(story.getValue()), storyJson.get(StoryEnum.VALUE));
		assertEquals(story.getNotes(), storyJson.get(StoryEnum.NOTES));
		assertEquals(story.getHowToDemo(), storyJson.get(StoryEnum.HOW_TO_DEMO));
	}
}
