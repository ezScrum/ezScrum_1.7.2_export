package ntut.csie.ezScrum.web.action.retrospective;

import java.io.File;
import java.io.IOException;
import java.util.List;

import ntut.csie.ezScrum.iteration.core.IScrumIssue;
import ntut.csie.ezScrum.test.CreateData.CopyProject;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateRetrospective;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.ezScrum.web.support.TranslateSpecialChar;
import ntut.csie.jcis.resource.core.IProject;
import servletunit.struts.MockStrutsTestCase;

public class ShowRetrospectiveActionTest extends MockStrutsTestCase {
	private CreateProject CP;
	private CreateSprint CS;
	private CreateRetrospective CR;
	private ezScrumInfoConfig config = new ezScrumInfoConfig();
	
	public ShowRetrospectiveActionTest(String testMethod) {
        super(testMethod);
    }
	
	protected void setUp() throws Exception {
		InitialSQL ini = new InitialSQL(config);
		ini.exe(); // 初始化 SQL

		this.CP = new CreateProject(1);
		this.CP.exeCreate(); // 新增一測試專案

		super.setUp();

		setContextDirectory(new File(config.getBaseDirPath() + "/WebContent")); // 設定讀取的
		// struts-config
		// 檔案路徑
		setServletConfigFile("/WEB-INF/struts-config.xml");
		setRequestPathInfo("/showRetrospective2");

		// ============= release ==============
		ini = null;
	}

	protected void tearDown() throws IOException, Exception {
		InitialSQL ini = new InitialSQL(config);
		ini.exe(); // 初始化 SQL

		CopyProject copyProject = new CopyProject(this.CP);
		copyProject.exeDelete_Project(); // 刪除測試檔案

		super.tearDown();

		// ============= release ==============
		ini = null;
		copyProject = null;
		this.CP = null;
		this.CS = null;
	}

	// case 1: no sprint
	public void testNoSprint() throws Exception {
		// ================ set initial data =======================
		IProject project = this.CP.getProjectList().get(0);
		// ================ set initial data =======================

		// ================== set parameter info ====================
		addRequestParameter("sprintID", Integer.toString(-1)); // 取得第一筆 SprintPlan
		// ================== set parameter info ====================

		// ================ set session info ========================
		request.getSession().setAttribute("UserSession", config.getUserSession());	
		request.getSession().setAttribute("Project", project);
		// ================ set session info ========================
		request.setHeader("Referer", "?PID=" + project.getName());	// SessionManager 會對URL的參數作分析 ,未帶入此參數無法存入session

		actionPerform(); // 執行 action

		// 驗證回傳 path
    	verifyForwardPath(null);
    	verifyForward(null);
    	verifyNoActionErrors();
  
    	// 比對資料是否正確(sprintID = -1)
    	String expected = "<Retrospectives><Sprint><Id>-1</Id><Name>Sprint #-1</Name></Sprint></Retrospectives>";
//    	System.out.println("response: " + response.getWriterBuffer().toString());
    	assertEquals(expected, response.getWriterBuffer().toString());   	    	
	}	
	
	// case 2: One sprint with no retrospective
	public void testOneSprint() throws Exception {	
		this.CS = new CreateSprint(1, this.CP);
		this.CS.exe(); // 新增一個 Sprint		
		
		// ================ set initial data =======================
		IProject project = this.CP.getProjectList().get(0);
		// ================ set initial data =======================

		// ================== set parameter info ====================
		// 測試不代入 sprint ID
		// ================== set parameter info ====================

		// ================ set session info ========================
		request.getSession().setAttribute("UserSession", config.getUserSession());	
		request.getSession().setAttribute("Project", project);
		// ================ set session info ========================
		request.setHeader("Referer", "?PID=" + project.getName());	// SessionManager 會對URL的參數作分析 ,未帶入此參數無法存入session

		actionPerform(); // 執行 action

		// 驗證回傳 path
    	verifyForwardPath(null);
    	verifyForward(null);
    	verifyNoActionErrors();
  
    	// 比對資料是否正確(sprintID = -1)
    	String expected = "<Retrospectives><Sprint><Id>1</Id><Name>Sprint #1</Name></Sprint></Retrospectives>";
//    	System.out.println("response: " + response.getWriterBuffer().toString());
    	assertEquals(expected, response.getWriterBuffer().toString());   	    	
	}		
	
	// case 3: One sprint with 1 Good
	public void testOneSprint1g() throws Exception {	
		this.CS = new CreateSprint(1, this.CP);
		this.CS.exe(); // 新增一個 Sprint		
		
		this.CR = new CreateRetrospective(1, 0, this.CP, this.CS);
		this.CR.exe();
		
		// ================ set initial data =======================
		IProject project = this.CP.getProjectList().get(0);
		String sprintID = this.CS.getSprintIDList().get(0);
		// ================ set initial data =======================

		// ================== set parameter info ====================
		addRequestParameter("sprintID", sprintID); // 取得第一筆 SprintPlan
		// ================== set parameter info ====================

		// ================ set session info ========================
		request.getSession().setAttribute("UserSession", config.getUserSession());	
		request.getSession().setAttribute("Project", project);
		// ================ set session info ========================
		request.setHeader("Referer", "?PID=" + project.getName());	// SessionManager 會對URL的參數作分析 ,未帶入此參數無法存入session

		actionPerform(); // 執行 action

		// 驗證回傳 path
    	verifyForwardPath(null);
    	verifyForward(null);
    	verifyNoActionErrors();
  
    	// 比對資料是否正確
    	String expected = this.genXML(sprintID);
    	assertEquals(expected, response.getWriterBuffer().toString());	   	    	
	}			
	
	// case 4: One sprint with 1 Improvement
	public void testOneSprint1i() throws Exception {	
		this.CS = new CreateSprint(1, this.CP);
		this.CS.exe(); // 新增一個 Sprint		
		
		this.CR = new CreateRetrospective(0, 1, this.CP, this.CS);
		this.CR.exe();
		
		// ================ set initial data =======================
		IProject project = this.CP.getProjectList().get(0);
		String sprintID = this.CS.getSprintIDList().get(0);
		// ================ set initial data =======================

		// ================== set parameter info ====================
		addRequestParameter("sprintID", sprintID); // 取得第一筆 SprintPlan
		// ================== set parameter info ====================

		// ================ set session info ========================
		request.getSession().setAttribute("UserSession", config.getUserSession());	
		request.getSession().setAttribute("Project", project);
		// ================ set session info ========================
		request.setHeader("Referer", "?PID=" + project.getName());	// SessionManager 會對URL的參數作分析 ,未帶入此參數無法存入session

		actionPerform(); // 執行 action

		// 驗證回傳 path
    	verifyForwardPath(null);
    	verifyForward(null);
    	verifyNoActionErrors();
  
    	// 比對資料是否正確
    	String expected = this.genXML(sprintID);
     	assertEquals(expected, response.getWriterBuffer().toString());	   	    	
	}	
	
	// case 5: One sprint with 1 Good + 1 Improvement
	public void testOneSprint1g1i() throws Exception {	
		this.CS = new CreateSprint(1, this.CP);
		this.CS.exe(); // 新增一個 Sprint		
		
		this.CR = new CreateRetrospective(1, 1, this.CP, this.CS);
		this.CR.exe();
		
		// ================ set initial data =======================
		IProject project = this.CP.getProjectList().get(0);
		String sprintID = this.CS.getSprintIDList().get(0);
		// ================ set initial data =======================

		// ================== set parameter info ====================
		addRequestParameter("sprintID", sprintID); // 取得第一筆 SprintPlan
		// ================== set parameter info ====================

		// ================ set session info ========================
		request.getSession().setAttribute("UserSession", config.getUserSession());	
		request.getSession().setAttribute("Project", project);
		// ================ set session info ========================
		request.setHeader("Referer", "?PID=" + project.getName());	// SessionManager 會對URL的參數作分析 ,未帶入此參數無法存入session

		actionPerform(); // 執行 action

		// 驗證回傳 path
    	verifyForwardPath(null);
    	verifyForward(null);
    	verifyNoActionErrors();
  
    	// 比對資料是否正確
    	String expected = this.genXML(sprintID);
    	assertEquals(expected, response.getWriterBuffer().toString());	   	    	
	}				
	
	private String genXML(String sprintID) {
    	TranslateSpecialChar tsc = new TranslateSpecialChar();				
		StringBuilder sb = new StringBuilder();
		sb.append("<Retrospectives><Sprint><Id>" + sprintID + "</Id><Name>Sprint #" + sprintID + "</Name></Sprint>");
		
		// good
    	List<IScrumIssue> goodRes = this.CR.getGoodRetrospectiveList();		
		for(int i = 0; i < goodRes.size(); i++){			
			IScrumIssue goodR = goodRes.get(i);
			if (goodR.getSprintID().compareTo(sprintID) == 0) {
				sb.append("<Retrospective>");
				sb.append("<Id>" + goodR.getIssueID() + "</Id>");
				sb.append("<Link>" + tsc.TranslateXMLChar(goodR.getIssueLink()) + "</Link>");
				sb.append("<SprintID>" + goodR.getSprintID() + "</SprintID>");
				sb.append("<Name>" + tsc.TranslateXMLChar(goodR.getName()) + "</Name>");
				sb.append("<Type>" + goodR.getCategory() + "</Type>");
				sb.append("<Description>" + tsc.TranslateXMLChar(goodR.getDescription()) + "</Description>");
				sb.append("<Status>" + goodR.getStatus() + "</Status>");
				sb.append("</Retrospective>");
			}
		}		
		
		// improve
		List<IScrumIssue> improveRes = this.CR.getImproveRetrospectiveList();		
		for(int i = 0; i < improveRes.size(); i++){
			IScrumIssue improveR = improveRes.get(i);
			if (improveR.getSprintID().compareTo(sprintID) == 0) {
				sb.append("<Retrospective>");
				sb.append("<Id>" + improveR.getIssueID() + "</Id>");
				sb.append("<Link>" + tsc.TranslateXMLChar(improveR.getIssueLink()) + "</Link>");
				sb.append("<SprintID>" + improveR.getSprintID() + "</SprintID>");
				sb.append("<Name>" + tsc.TranslateXMLChar(improveR.getName()) + "</Name>");
				sb.append("<Type>" + improveR.getCategory() + "</Type>");
				sb.append("<Description>" + tsc.TranslateXMLChar(improveR.getDescription()) + "</Description>");
				sb.append("<Status>" + improveR.getStatus() + "</Status>");
				sb.append("</Retrospective>");
			}
		}
		
		sb.append("</Retrospectives>");
		return sb.toString();
	}
}
