package ntut.csie.ezScrum.web.action.backlog.product;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ntut.csie.ezScrum.issue.core.IIssueTag;
import ntut.csie.ezScrum.refactoring.manager.ProjectManager;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.ezScrum.web.helper.ProductBacklogHelper;
import ntut.csie.ezScrum.web.support.TranslateSpecialChar;
import ntut.csie.jcis.resource.core.IProject;
import servletunit.struts.MockStrutsTestCase;

public class AjaxAddNewTagActionTest extends MockStrutsTestCase {
	private CreateProject CP;
	private int ProjectCount = 1;
	private ezScrumInfoConfig config = new ezScrumInfoConfig();
	private final String ACTION_PATH = "/AjaxAddNewTag";
	
	public AjaxAddNewTagActionTest(String testMethod) {
        super(testMethod);
    }
	
	protected void setUp() throws Exception {
		InitialSQL ini = new InitialSQL(config);
		ini.exe();											// 初始化 SQL
		
		// 新增Project
		this.CP = new CreateProject(this.ProjectCount);
		this.CP.exeCreate();
		
		super.setUp();
		
		// 設定讀取的struts-config檔案路徑
		setContextDirectory(new File(config.getBaseDirPath() + "/WebContent")); 
		setServletConfigFile("/WEB-INF/struts-config.xml");
		setRequestPathInfo(this.ACTION_PATH);
		
		// ============= release ==============
		ini = null;
	}
	
	protected void tearDown() throws IOException, Exception {
		//	刪除資料庫
		InitialSQL ini = new InitialSQL(config);
		ini.exe();
		
		//	刪除外部檔案
		ProjectManager projectManager = new ProjectManager();
		projectManager.deleteAllProject();
		projectManager.initialRoleBase(this.config.getTestDataPath());

		super.tearDown();
		
		ini = null;
		projectManager = null;
		this.CP = null;
	}
	
	//測試 tag 名稱中含 "," ，會不會顯示 not allowed 的訊息
	public void testAddComma() throws Exception{//comma = ","
		IProject project = this.CP.getProjectList().get(0);
		String tag = ",";
		String compareMsg = "<Message>TagName: \",\" is not allowed</Message>";
		
		//設定Session資訊
		request.getSession().setAttribute("UserSession", config.getUserSession());
		request.getSession().setAttribute("Project", project);
		request.setHeader("Referer", "?PID=" + project.getName());	// SessionManager 會對URL的參數作分析 ,未帶入此參數無法存入session
		
		addRequestParameter("newTagName", tag);	
		
		actionPerform();
		
		assertTrue(getMockResponse().getWriterBuffer().toString().contains(compareMsg));
	}
	
	//測試加入 DB 中已經存在的tag 名稱，並檢視request訊息要包含XML的轉換
	public void testAddExistTag() throws Exception{
		IProject project = this.CP.getProjectList().get(0);

		String tagDB = "&  /  <  >  \\\\  \\\'  \"";//add tag in DB
		String tag = "&  /  <  >  \\  \'  \"";//key in new tag
		String compareMsg = "&amp;  /  &lt;  &gt;  \\  &apos;  &quot;";
		
		//設定Session資訊
		request.getSession().setAttribute("UserSession", config.getUserSession());
		request.getSession().setAttribute("Project", project);
		request.setHeader("Referer", "?PID=" + project.getName());	// SessionManager 會對URL的參數作分析 ,未帶入此參數無法存入session
		
//		ProductBacklogHelper PBHelper = new ProductBacklogHelper(project,config.getUserSession());
//		PBHelper.addNewTag(tagDB);//先在DB中加入tag
		
		(new ProductBacklogHelper(config.getUserSession(), project)).addNewTag(tagDB);
		
		addRequestParameter("newTagName", tag);//增加新的tag
		
		actionPerform(); // 執行 action
		verifyNoActionErrors();
		
		//抓取Tag Name already exist的訊息, 並比對字串
		assertTrue(getMockResponse().getWriterBuffer().toString().contains(compareMsg));
	}
	
	//測試新增 tag ，並從 DB 中取出比對
	public void testAddNewTag() throws Exception{
		IProject project = this.CP.getProjectList().get(0);

		List<String> tagList = new ArrayList<String>();
		//tagList.add("&/<>\'\"");
		tagList.add("&");
		tagList.add("/");
		tagList.add("<");
		tagList.add(">");
		tagList.add("\\");
		tagList.add("\'");
		tagList.add("\"");
		
		//設定Session資訊
		request.getSession().setAttribute("UserSession", config.getUserSession());
		request.getSession().setAttribute("Project", project);
		request.setHeader("Referer", "?PID=" + project.getName());	// SessionManager 會對URL的參數作分析 ,未帶入此參數無法存入session
		
//		ProductBacklogHelper PBHelper = new ProductBacklogHelper(project,config.getUserSession());
		
		String expectedResponseText = "";
		String actualResponseText = "";
		
		for(int i = 0; i < tagList.size(); i++) {
			addRequestParameter("newTagName", tagList.get(i));
			actionPerform(); // 執行 action
			verifyNoActionErrors();
			
			expectedResponseText = 
					"<Tags><Result>true</Result>" + 
					"<IssueTag>" + 
					"<Id>" + (i+1) + "</Id>" + 
					"<Name>" + new TranslateSpecialChar().TranslateXMLChar(tagList.get(i)) + "</Name>" + 
					"</IssueTag>" + 
					"</Tags>";
			
			actualResponseText = response.getWriterBuffer().toString();
			assertEquals(expectedResponseText, actualResponseText);
			this.response.reset();
		}
		
//		IIssueTag[] tags = PBHelper.getTagList();
		IIssueTag[] tags = (new ntut.csie.ezScrum.web.helper.ProductBacklogHelper( config.getUserSession(), project)).getTagList();
		
		assertEquals(tags.length, tagList.size());
		for(int i = 0; i < tags.length; i++){
			assertEquals(tags[i].getTagName(), tagList.get(i));
		}
		
	}
}
