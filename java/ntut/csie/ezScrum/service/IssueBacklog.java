package ntut.csie.ezScrum.service;

import java.util.List;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.issue.sql.service.core.ITSPrefsStorage;
import ntut.csie.ezScrum.iteration.core.ScrumEnum;
import ntut.csie.ezScrum.pic.core.IUserSession;
import ntut.csie.jcis.resource.core.IProject;

/**
 * ezTrack 使用
 * @author SPARK
 *
 */
public class IssueBacklog {
	private IProject m_project;
	private ITSPrefsStorage m_itsPrefs;
	private IUserSession m_userSession;
	
	public IssueBacklog(IProject project, IUserSession userSession){
		m_project = project;
		m_userSession = userSession;
		
		//	//初始ITS的設定
		m_itsPrefs = new ITSPrefsStorage(m_project, m_userSession);
	}
	
	/**
	 * 依照 Issue ID 取出 Issue
	 * @param id
	 * @return
	 */
	public IIssue getIssue(long id){
		EzTrackService etsService = new EzTrackService(m_itsPrefs);
		etsService.openConnect();
		IIssue issue = etsService.getIssue(id);
		etsService.closeConnect();
		return issue;
	}
	
	/**
	 * 回傳的 Custom issues type 存在，表示此專案為可以回報 issue 的專案
	 * @return
	 */
	public boolean isReportProject() {
		List<CustomIssueType> types = getCustomIssueType();
		if ( (types != null) && (types.size() > 0) ) {
			for (CustomIssueType type : types) {
				if (type.ispublic()) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * check whether the issue is the scrum type
	 * @param issue
	 * @return
	 */
	public boolean isScrumType(IIssue issue){
		if(issue.getCategory().equals(ScrumEnum.STORY_ISSUE_TYPE))
			return Boolean.TRUE;
		else if(issue.getCategory().equals(ScrumEnum.TASK_ISSUE_TYPE))
			return Boolean.TRUE;
		else if(issue.getCategory().equals(ScrumEnum.UNPLANNEDITEM_ISSUE_TYPE))
			return Boolean.TRUE;
		else if(issue.getCategory().equals(ScrumEnum.GOOD_ISSUE_TYPE))
			return Boolean.TRUE;
		else if	(issue.getCategory().equals(ScrumEnum.IMPROVEMENTS_ISSUE_TYPE))
			return Boolean.TRUE;
		else
			return Boolean.FALSE;
	}
	
	public List<CustomIssueType> getCustomIssueType(){
		EzTrackService etsService = new EzTrackService(m_itsPrefs);
		etsService.openConnect();
		List<CustomIssueType> list = etsService.getCustomIssueType(m_project.getName());
		etsService.closeConnect();
		return list;
	}
	
	
	/**
	 * ----- Test ------
	 */
	
	
	/**
	 * 新增一筆 Issue Type
	 */
	public CustomIssueType addIssueType(String typeName, boolean isPublic){
		EzTrackService etsService = new EzTrackService(m_itsPrefs);
		etsService.openConnect();
		// 新增資料
		CustomIssueType type = etsService.addIssueType(m_project.getName(), typeName, isPublic);
		etsService.closeConnect();
		return type;
	}
}
