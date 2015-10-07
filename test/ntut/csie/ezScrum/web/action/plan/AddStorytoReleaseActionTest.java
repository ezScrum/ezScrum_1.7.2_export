package ntut.csie.ezScrum.web.action.plan;

import java.io.File;
import java.io.IOException;

import ntut.csie.ezScrum.test.CreateData.CopyProject;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateRelease;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import servletunit.struts.MockStrutsTestCase;

public class AddStorytoReleaseActionTest extends MockStrutsTestCase {
	private CreateProject CP;
	private CreateRelease CR;
	private CreateSprint CS;
	private ezScrumInfoConfig config = new ezScrumInfoConfig();
	
	public AddStorytoReleaseActionTest(String testMethod) {
        super(testMethod);
    }
	
	protected void setUp() throws Exception {
		InitialSQL ini = new InitialSQL(config);
		ini.exe();											// 初始化 SQL
		
    	this.CP = new CreateProject(1);
    	this.CP.exeCreate();								// 新增一測試專案
    	
    	this.CR = new CreateRelease(1, this.CP);
    	this.CR.exe();										// 新增一筆Release Plan
    	
    	this.CS = new CreateSprint(2, this.CP);
    	this.CS.exe();										// 新增二筆Sprint Plan
    	
    	super.setUp();
    	
    	setContextDirectory(new File(config.getBaseDirPath() + "/WebContent"));		// 設定讀取的 struts-config 檔案路徑
    	setServletConfigFile("/WEB-INF/struts-config.xml");
    	
    	// ============= release ==============
    	ini = null;
    }
	
    protected void tearDown() throws IOException, Exception {
		InitialSQL ini = new InitialSQL(config);
		ini.exe();											// 初始化 SQL
		
    	CopyProject copyProject = new CopyProject(this.CP);
    	copyProject.exeDelete_Project();					// 刪除測試檔案
    	
    	
    	// ============= release ==============
    	ini = null;
    	copyProject = null;
    	this.CP = null;
    	this.CR = null;
    	this.CS = null;
    	
    	super.tearDown();
    }

    public void testexecute() throws Exception {
    	// ===================== 此 action 似乎尚未被使用 ===========================    	
    }
}
