package ntut.csie.ezScrum.issue.sql.service.core;

import java.util.Map;

import ntut.csie.ezScrum.pic.core.IUserSession;
import ntut.csie.jcis.core.exception.PrefsUtilException;
import ntut.csie.jcis.core.util.PrefsUtil;
import ntut.csie.jcis.resource.core.IProject;

public class ITSPrefsStorage {

	private final String PerfsFileName = "its_config.xml";
	private final String SERVERURL = "ServerUrl";
	private final String SERVICEPATH = "ServicePath";
	private final String ACCOUNT = "Account";
	private final String PASSWORD = "Password";
	private final String DATABASETYPE = "DatabaseType";
	private final String DATABASENAME = "DatabaseName";
	
	private IProject m_project;
	private PrefsUtil prefsUtil;
	private String m_projectPrefsPath;
	private String m_sysPrefsPath;

	private Map<String, String> prefsMap;
	private IUserSession m_userSession;

//	public ITSPrefsStorage(IProject project) {
//		m_project = project;
//		prefsUtil = new PrefsUtil(PrefsUtil.ITS_PROJECT_PACKAGE);
//
//		m_projectPrefsPath = m_project.getFolder(IProject.METADATA)
//				.getFullPath()
//				+ "/" + PerfsFileName;
//		m_sysPrefsPath = m_project.getWorkspaceRoot().getFolder(
//				IProject.METADATA).getFullPath()
//				+ "/" + PerfsFileName;
//
//		init();
//	}
	/**
	 * 若不需要對ITS做修改新增的動作(add issue,add bug note)
	 * 而僅做收集資料動作的話(像在builder中要收集its的資料),
	 * userSession可使用null來使用
	 * 在回傳account及password將都會回傳DB的設定
	 */
	public ITSPrefsStorage(IProject project, IUserSession userSession) {
		m_project = project;
		prefsUtil = new PrefsUtil(PrefsUtil.ITS_PROJECT_PACKAGE);

		m_projectPrefsPath = m_project.getFolder(IProject.METADATA)
				.getFullPath()
				+ "/" + PerfsFileName;
		m_sysPrefsPath = m_project.getWorkspaceRoot().getFolder(
				IProject.METADATA).getFullPath()
				+ "/" + PerfsFileName;
		
		m_userSession = userSession;
		
		init();
	}
	
	public String getProjectName()
	{
		return m_project.getName();
	}

	@SuppressWarnings("unchecked")
	private void init() {
		try {
			
			prefsMap = prefsUtil.getPrefs(m_project.getName(), m_sysPrefsPath,
					m_projectPrefsPath, "");

		} catch (PrefsUtilException e) {
			e.printStackTrace();
		}
	}

	public void save() {
		try {
			prefsUtil.saveUsrDefined(m_project.getName(), prefsMap,
					m_projectPrefsPath);
		} catch (PrefsUtilException e) {
			e.printStackTrace();
		}
	}

	public String getServerUrl() {
		return prefsMap.get(SERVERURL);
	}

	public String getWebServicePath() {
		return prefsMap.get(SERVICEPATH);
	}

	public String getAccount(){
		if (m_userSession==null)
			return getDBAccount();
		return m_userSession.getAccount().getID();
	}
	

	
	public String getDBAccount() {
		return prefsMap.get(ACCOUNT);
	}

	public String getDBPassword() {
		return prefsMap.get(PASSWORD);
	}

	public String getDBType() {
		return prefsMap.get(DATABASETYPE);
	}

	public String getDBName() {
		return prefsMap.get(DATABASENAME);
	}
	
	public void setServerUrl(String serverUrl) {
		prefsMap.put(SERVERURL, serverUrl);
	}

	public void setServicePath(String servicePath) {
		prefsMap.put(SERVICEPATH, servicePath);
	}

	public void setDBAccount(String account) {
		prefsMap.put(ACCOUNT, account);
	}

	public void setDBPassword(String password) {
		prefsMap.put(PASSWORD, password);
	}
	
	public void setDBType(String type) {
		prefsMap.put(DATABASETYPE, type);
	}

	public void setDBName(String name) {
		prefsMap.put(DATABASENAME, name);
	}
	
	public Map<String, String> getPerfsMap(){
		return prefsMap;
	}

}
