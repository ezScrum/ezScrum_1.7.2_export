package ntut.csie.ezScrum.web.helper;

import java.io.IOException;
import java.net.ConnectException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import ntut.csie.ezScrum.issue.sql.service.internal.TestConnectException;
import ntut.csie.ezScrum.pic.core.IUserSession;
import ntut.csie.ezScrum.web.dataObject.ITSInformation;
import ntut.csie.ezScrum.web.dataObject.ProjectInformation;
import ntut.csie.ezScrum.web.form.ProjectInfoForm;
import ntut.csie.ezScrum.web.logic.ProjectLogic;
import ntut.csie.ezScrum.web.mapper.AccountMapper;
import ntut.csie.ezScrum.web.mapper.ProjectMapper;
import ntut.csie.ezScrum.web.support.AccessPermissionManager;
import ntut.csie.ezScrum.web.support.SessionManager;
import ntut.csie.ezScrum.web.support.TranslateSpecialChar;
import ntut.csie.jcis.account.core.IAccount;
import ntut.csie.jcis.resource.core.IProject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProjectHelper {
	
	private static Log log = LogFactory.getLog(ProjectHelper.class);
	private ProjectMapper mProjectMapper;
	
	public ProjectHelper() {
		mProjectMapper = new ProjectMapper();
	}
	
	public ProjectInfoForm getProjectInfoForm(IProject project) {
		return mProjectMapper.getProjectInfoForm(project);
	}
	
	public String getProjectListXML(IAccount account) {
		log.info(" handle project list xml format");
		
        // get all projects
        ProjectLogic projectLogic = new ProjectLogic();
        List<IProject> projects = projectLogic.getAllProjects();
		
        // get the user and projects permission mapping
        Map<String, Boolean> map = projectLogic.getProjectPermissionMap( account );
        
        // get the demo date
        HashMap<String, String> hm = new HashMap<String, String>();
        for (IProject project : projects) {
			SprintPlanHelper SPhelper = new SprintPlanHelper(project);
			String demoDate = SPhelper.getNextDemoDate();
			if(demoDate==null)
				hm.put(project.getName(), "No Plan!");
			else
				hm.put(project.getName(), demoDate);
        }
        
        // write projects to XML format
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		StringBuilder sb = new StringBuilder();
		sb.append("<Projects>");
		TranslateSpecialChar tsc = new TranslateSpecialChar();
		for( IProject project:projects ){
			if ( map.get(project.getName()) == Boolean.TRUE)
        	{
				sb.append("<Project>");
				sb.append("<ID>" + tsc.TranslateXMLChar(project.getProjectDesc().getName()) + "</ID>");
				sb.append("<Name>" + tsc.TranslateXMLChar(project.getProjectDesc().getDisplayName()) + "</Name>");
				sb.append("<Comment>" + tsc.TranslateXMLChar(project.getProjectDesc().getComment()) + "</Comment>");
				sb.append("<ProjectManager>" + tsc.TranslateXMLChar(project.getProjectDesc().getProjectManager()) + "</ProjectManager>");
				sb.append("<CreateDate>" + dateFormat.format(project.getProjectDesc().getCreateDate()) + "</CreateDate>");
				sb.append("<DemoDate>" + hm.get(project.getName()) + "</DemoDate>");
				sb.append("</Project>");
        	}
		}
		sb.append("</Projects>");
		
		return sb.toString();
		
	}

	/**
	 * 回傳此專案的Scrum Team 內可以存取 TaskBoard 權限的人，代表可以領取工作者
	 * @param userSession 
	 * @param project 
	 */
	public List<String> getProjectScrumWorkerList(IUserSession userSession, IProject project) {
		ProjectMapper projectMapper = new ProjectMapper();
		return projectMapper.getProjectScrumWorkerList(userSession, project);
	}
	
	public String getCreateProjectXML(HttpServletRequest request, IUserSession userSession, String fromPage, ITSInformation itsInformation, ProjectInformation projectInformation) {
		StringBuilder sb = new StringBuilder();
		ProjectMapper projectMapper = new ProjectMapper();
		try {
			sb.append("<Root>");

			// create project
			if (fromPage != null) {
				if (fromPage.equals("createProject")) {
					sb.append("<CreateProjectResult>");

					IProject project = null;
					try {
						//	轉換格式
						ProjectInfoForm projectInfoForm = this.convertProjectInfo( projectInformation );
						
						project = projectMapper.createProject( userSession, itsInformation, projectInfoForm );
						
						// 重新設定權限, 當專案被建立時, 重新讀取此User的權限資訊
						SessionManager projectSessionManager = new SessionManager(request);
						projectSessionManager.setProject(project);
						AccessPermissionManager.setupPermission(request, userSession);
						
						//	建立專案角色和權限的外部檔案
						AccountMapper accountMapper = new AccountMapper();
						accountMapper.createPermission(project);
						accountMapper.createRole(project);

						sb.append("<Result>Success</Result>");
						sb.append("<ID>" + projectInformation.getName() + "</ID>");

					} catch (TestConnectException e) {
						// 連線失敗，告知使用者連線已失敗
						if(e.getType().equals(TestConnectException.TABLE_ERROR)) {
							//如果是Table Error的話，那還是先存入
							sb.append("<Result>InitDatabase</Result>");
							sb.append("<IP>" + itsInformation.getServerURL() + "</IP>");
							sb.append("<DBName>" + itsInformation.getDbName() + "</DBName>");
						}
						else if(e.getType().equals(TestConnectException.DATABASE_ERROR)) {
							sb.append("<Result>CreateDatabase</Result>");
							sb.append("<IP>" + itsInformation.getServerURL() + "</IP>");
							sb.append("<DBName>" + itsInformation.getDbName() + "</DBName>");
						}
						else if(e.getType().equals(TestConnectException.CONNECT_ERROR)) {
							sb.append("<Result>Connect_Error</Result>");
						}
							
						else {
							sb.append("<Result>Failure</Result>");
						}
						// 如果project Create失敗，就把目前產生到一半的Project檔案刪除
						projectMapper.deleteProject( projectInformation.getName() );
					}
					
					sb.append("</CreateProjectResult>");
				}

			}

			sb.append("</Root>");
			return sb.toString();
		} catch (ConnectException e) {
			log.info( "TestConnect" );
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			log.info( "SaveWorkspace" );
			e.printStackTrace();
		}
		return sb.toString(); 
	}
	
	public List<IAccount> getProjectMemberList(IUserSession userSession, IProject project) {
		return mProjectMapper.getProjectMemberList(userSession, project);
	}
	
	private ProjectInfoForm convertProjectInfo( ProjectInformation projectInformation) {
		String name = projectInformation.getName();
		String displayName = projectInformation.getDisplayName();
		String comment = projectInformation.getComment();
		String manager = projectInformation.getManager(); 
		String attachFileSize = projectInformation.getAttachFileSize();
		
		ProjectInfoForm saveProjectInfoForm = new ProjectInfoForm();
		// 塞入假資料
		saveProjectInfoForm.setServerType("SVN");
		saveProjectInfoForm.setCvsConnectionType("pserver");
		saveProjectInfoForm.setSvnHook("Close");
		saveProjectInfoForm.setOutputPath("/");
		saveProjectInfoForm.setSourcePathString("/");
		// 塞入使用者輸入的資料
		saveProjectInfoForm.setName(name);
		saveProjectInfoForm.setDisplayName(displayName);
		saveProjectInfoForm.setComment(comment);
		saveProjectInfoForm.setProjectManager(manager);
		saveProjectInfoForm.setAttachFileSize(attachFileSize);
		// log info
		log.info("saveProjectInfoForm.getOutputPath()=" + saveProjectInfoForm.getOutputPath());
		log.info("saveProjectInfoForm.getSourcePaths().length=" + saveProjectInfoForm.getSourcePaths().length);

		return saveProjectInfoForm;
	}
}
