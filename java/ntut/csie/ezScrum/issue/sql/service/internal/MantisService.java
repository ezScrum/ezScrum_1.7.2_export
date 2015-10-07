package ntut.csie.ezScrum.issue.sql.service.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.issue.core.IIssueHistory;
import ntut.csie.ezScrum.issue.core.IIssueNote;
import ntut.csie.ezScrum.issue.core.IIssueTag;
import ntut.csie.ezScrum.issue.core.ITSEnum;
import ntut.csie.ezScrum.issue.internal.IssueNote;
import ntut.csie.ezScrum.issue.sql.service.core.IITSService;
import ntut.csie.ezScrum.issue.sql.service.core.IQueryValueSet;
import ntut.csie.ezScrum.issue.sql.service.core.ITSPrefsStorage;
import ntut.csie.ezScrum.issue.sql.service.tool.ISQLControl;
import ntut.csie.ezScrum.issue.sql.service.tool.internal.HSQLControl;
import ntut.csie.ezScrum.issue.sql.service.tool.internal.MySQLControl;
import ntut.csie.ezScrum.iteration.core.IStory;
import ntut.csie.ezScrum.iteration.core.ScrumEnum;
import ntut.csie.ezScrum.iteration.support.TranslateSpecialChar;
import ntut.csie.jcis.account.core.AccountEnum;
import ntut.csie.jcis.core.util.DateUtil;
import ntut.csie.jcis.core.util.StringUtil;
import ntut.csie.jcis.core.util.XmlFileUtil;
import ntut.csie.jcis.resource.core.IProject;
import ntut.csie.jcis.resource.core.ResourceFacade;
import ntut.csie.jcis.resource.core.internal.Workspace;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

public class MantisService extends AbstractMantisService implements IITSService {
	private static Log log = LogFactory.getLog(MantisService.class);
	private static String INITIATE_SQL_FILE = "initial_bk.sql";
	final public static String ROOT_TAG = "root";
	
	final private String m_id = "Mantis";
	final private String PORT_SERVICE_MYSQL = "3306";
	private String MANTIS_TABLE_TYPE = "Default";
	private String MANTIS_DB_NAME = "bugtracker";

	private MantisNoteService m_noteService;
	private MantisHistoryService m_historyService;
	private MantisIssueService m_issueService;
	private MantisAttachFileService m_attachFileService;
	private MantisTagService m_tagService;

	public MantisService(ITSPrefsStorage prefs) {
		setPrefs(prefs);

		if (!getPrefs().getDBType().equals(""))
			MANTIS_TABLE_TYPE = getPrefs().getDBType();

		if (!getPrefs().getDBName().equals(""))
			MANTIS_DB_NAME = getPrefs().getDBName();

		// =========設定要使用的SQLControl============
		ISQLControl control = null;
		if (MANTIS_TABLE_TYPE.equalsIgnoreCase("MySQL")) {
			control = new MySQLControl(prefs.getServerUrl(), PORT_SERVICE_MYSQL, MANTIS_DB_NAME);

		} else {
			/*-----------------------------------------------------------
			 *	如果是要使用Default SQL的設定，
			 *	那麼ServerUrl的路徑要指到Workspace裡面
			 *	所以要先取得Workspace的路徑
			-------------------------------------------------------------*/
			// 因為Default DB的檔案名稱預設就是ProjectName
			String projectName = prefs.getProjectName();

			// 如果是Default SQL的話，那麼DB路徑就會被設定為Project底下的資料夾+Project檔案名稱
			// ex. WorkspacePath/ProjectName/ProjectName
			String DBRootPath = new Workspace().getRoot().getProject(projectName).getFullPath().append(projectName)
					.getPathString();

			// 然後剩下的路徑啥就不會管他了
			// ex. ProjectName.h2.db , 所以MANTIS_TABLE_NAME會被完全忽略 ....
			control = new HSQLControl(DBRootPath, PORT_SERVICE_MYSQL, projectName);
		}

		control.setUser(prefs.getDBAccount());
		control.setPassword(prefs.getDBPassword());
		setControl(control);

	}

	
	/**
	 * 利用透過MantisConnect及直接access資料庫的方式來實作 因此提供的pm帳號必需要能在Mantis及MySQL上使用
	 */
	public void openConnect() {
		getControl().connection();

		m_noteService = new MantisNoteService(getControl(), getPrefs());
		m_historyService = new MantisHistoryService(getControl(), getPrefs());
		m_issueService = new MantisIssueService(getControl(), getPrefs());
		m_attachFileService = new MantisAttachFileService(getControl(), getPrefs());
		m_tagService = new MantisTagService(getControl(), getPrefs());
	}

	/************************************************************
	 * 執行SQL Script清空資料庫並且重新建立Table
	 *************************************************************/
	public boolean initiateDB() throws SQLException {
		getControl().connection();
		Connection connection = getControl().getconnection();

		String defaultFile = ResourceFacade.getWorkspace().getRoot().getFolder(IProject.METADATA).getFullPath() + "/"
				+ INITIATE_SQL_FILE;
		try {
			TableCreater.importSQL(connection, new FileInputStream(defaultFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} finally {
			getControl().close();
		}
		return true;
	}
	/**
	 * 在SQL Server上建立一個資料庫
	 */
	public boolean createDB()
	{
		ISQLControl controller = getControl();
		controller.connectionToServer();
		try
		{
			String sql = "CREATE DATABASE "+MANTIS_DB_NAME;
			return controller.execute(sql);
		}
		finally
		{
			controller.close();
		}
	}
	/**
	 * 檢查連線的順序為:
	 * 
	 * 1.測試hostname+port+dbname這完整路徑是否可以正確連線
	 * 2.測試hostname+port 是否可以正確連線
	 * 3.測試表格內容是否正確
	 * 
	 */
	public void TestConnect() throws Exception {
		getControl().connection();
		Connection connection = getControl().getconnection();
		try
		{
			//檢查是否能正確連線
			if (connection == null) {
				//如果不能連線的話，那麼先確認Server是否可以連線
				//如果Server可以連線，那麼就嘗試建立資料庫
				if(testServerConnect())
				{
					throw new TestConnectException(TestConnectException.DATABASE_ERROR);
				}
				else
				{
					throw new TestConnectException(TestConnectException.CONNECT_ERROR);
				}
			} 
			//檢查表格是否正確
			else if (!isAllTableExist()) {
				throw new TestConnectException(TestConnectException.TABLE_ERROR);
			} 
		}
		finally
		{
			if(connection != null)
				getControl().close();
		}
	}
	public boolean isAllTableExist() {
		/*-----------------------------------------------------------
		 *	如果是MySql需要檢查Table是否存在
		-------------------------------------------------------------*/
		if (MANTIS_TABLE_TYPE.equalsIgnoreCase("MySQL")) {
			return TableCreater.isAllTableExist(getControl());

		}
		return true;
	}
	private boolean testServerConnect() throws SQLException 
	{
		getControl().connectionToServer();
		Connection connection =getControl().getconnection();
		try
		{
			if(connection == null)
				return false;
			else
				return true;
		}
		finally
		{
			if(connection!=null)
				connection.close();
		}
	}

	/**
	 * 關閉連線,主要是關閉SQL的連線 在使用完成一定要關閉連線
	 */
	public void closeConnect() {
		try {
			getControl().close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public long newIssue(IIssue issue) {
		long issueID = m_issueService.newIssue(issue);
		if (issueID > 0) {
			m_historyService.addMantisActionHistory(issueID, IIssueHistory.EMPTY_FIELD_NAME,
					IIssueHistory.ZERO_OLD_VALUE, IIssueHistory.ZERO_NEW_VALUE, IIssueHistory.ISSUE_NEW_TYPE,
					IIssueHistory.NOW_MODIFY_DATE);
		}
		return issueID;
	}
	
	/************************************************************
	 * 
	 * 可以針對releaseID與SprintID來搜尋Story與Task，並且只取得最新的資料
	 * 
	 *************************************************************/
	public IIssue[] getIssues(String projectName, String category, String releaseID, String sprintID) {
		return getIssues(projectName, category, releaseID, sprintID, null);
	}

	/************************************************************
	 * 找出期間限定的Issue
	 *************************************************************/
	public IIssue[] getIssues(String projectName, String category, String releaseID, String sprintID, Date startDate,
			Date endDate) {

		IIssue[] issues = m_issueService.getIssues(projectName, category, releaseID, sprintID, startDate, endDate);

		for (IIssue issue : issues) {
			// 建立歷史記錄
			m_historyService.initHistory(issue);
			// 設定在mantis上tag的資料
			setIssueNote(issue);
			// 建立bug note
			issue.setIssueNotes(this.m_noteService.getIssueNotes(issue));

			m_attachFileService.initAttachFile(issue);
		}

		return issues;
	}

	/************************************************************
	 * 
	 * 針對傳入的時間範圍取出這時間的Issue狀態
	 * 
	 *************************************************************/
	public IIssue[] getIssues(String projectName, String category, String releaseID, String sprintID, Date date) {
		IIssue[] issues = m_issueService.getIssues(projectName, category, releaseID, sprintID, date);

		for (IIssue issue : issues) {
			// 建立歷史記錄
			m_historyService.initHistory(issue);
			// 設定在mantis上tag的資料
			setIssueNote(issue);
			// 建立bug note
			issue.setIssueNotes(this.m_noteService.getIssueNotes(issue));

			m_attachFileService.initAttachFile(issue);
			// 建立tag資料
			m_tagService.initTag(issue);
		}

		return issues;
	}

	// 當有更新時,若沒有重新建立Service的話,連續取得同一個專案會造成只能取到舊資料
	public IIssue[] getIssues(String projectName) {
		IIssue[] issues = m_issueService.getIssues(projectName);

		for (IIssue issue : issues) {
			// 建立歷史記錄
			m_historyService.initHistory(issue);

			// 設定在mantis上tag的資料
			setIssueNote(issue);

			// 建立bug note
			issue.setIssueNotes(this.m_noteService.getIssueNotes(issue));

			m_attachFileService.initAttachFile(issue);
		}

		return issues;
	}

	public IIssue[] getIssues(String projectName, String category) {
		IIssue[] issues = m_issueService.getIssues(projectName, category);

		for (IIssue issue : issues) {
			// 建立歷史記錄
			m_historyService.initHistory(issue);
			// 設定在mantis上note的資料
			setIssueNote(issue);
			// 建立bug note
			issue.setIssueNotes(this.m_noteService.getIssueNotes(issue));
			m_attachFileService.initAttachFile(issue);
			// 建立tag資料
			m_tagService.initTag(issue);
		}
		return issues;
	}

	public IIssue getIssue(long issueID) {
		IIssue issue = m_issueService.getIssue(issueID);

		if (issue != null) {
			// 建立歷史記錄
			m_historyService.initHistory(issue);

			// 設定在mantis上tag的資料
			setIssueNote(issue);

			// 設定child issue id
			// setRelationShipID(issue);

			// 建立bug note
			issue.setIssueNotes(this.m_noteService.getIssueNotes(issue));
			m_attachFileService.initAttachFile(issue);
			m_tagService.initTag(issue);
		}
		return issue;
	}

	// =====================有新增標籤時,在這必需要存入Issue Tag中============
	private void setIssueNote(IIssue issue) {
		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName("mantis_bugnote_table");
		valueSet.addTableName("mantis_bugnote_text_table");
		valueSet.addFieldEqualCondition("mantis_bugnote_text_table.id", "mantis_bugnote_table.bugnote_text_id");
		valueSet.addFieldEqualCondition("mantis_bugnote_table.bug_id", Long.toString(issue.getIssueID()));
		String query = valueSet.getSelectQuery();
		// String query = "SELECT date_submitted, note,
		// mantis_bugnote_text_table.id FROM `mantis_bugnote_table` ,
		// `mantis_bugnote_text_table` WHERE mantis_bugnote_text_table.id =
		// mantis_bugnote_table.bugnote_text_id AND mantis_bugnote_table.bug_id
		// ="
		// + issue.getIssueID();
		// Statement stmt;
		try {
			// stmt = conn.createStatement();
			// ResultSet result = stmt.executeQuery(query);
			ResultSet result = getControl().executeQuery(query);
			while (result.next()) {
				String note = result.getString("note");

				// 只要note中有包含JCIS的tag就算是記錄的地方
				if (note.contains("<JCIS") && !note.contains("<JCIS:")) {
					note = "<" + ROOT_TAG + ">" + note + "</" + ROOT_TAG + ">";
					Document doc;
					try {
						doc = XmlFileUtil.LoadXmlString(note);
						issue.setTagContent(doc.getRootElement());
					} catch (JDOMException e) {
						log.error(e);
						continue;
					} catch (IOException e) {
						log.error(e);
						continue;
					}
				}

				/***************************************************************
				 * 使用Issue Note記錄全部的note
				 **************************************************************/
				IssueNote issueNote = new IssueNote();
				issueNote.setIssueID(issue.getIssueID());
				issueNote.setText(result.getString("note"));
				issueNote.setHandler(this.getUserName(result.getInt("reporter_id")));
				issueNote.setNoteID(result.getLong("id"));
				issueNote.setSubmittedDate(result.getTimestamp("date_submitted").getTime());
				issueNote.setModifiedDate(result.getTimestamp("last_modified").getTime());

				issue.addIssueNote(issueNote);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public String[] getActors(String projectName, int accessLevel) {
		int projectID = getProjectID(projectName);
		List<String> allUserList = getAllUsers();

		List<String> userList = new ArrayList<String>();
		for (String userName : allUserList) {
			if (getProjectAccessLevel(getUserID(userName), projectID) >= accessLevel)
				userList.add(userName);
		}

		return userList.toArray(new String[userList.size()]);

	}

	public String[] getCategories(String projectName) {
		List<String> categories = new ArrayList<String>();
		int projectID = getProjectID(projectName);

		if (projectID > 0) {
			IQueryValueSet valueSet = new MySQLQuerySet();
			valueSet.addTableName("mantis_project_category_table");
			valueSet.addEqualCondition("project_id", Integer.toString(projectID));

			String query = valueSet.getSelectQuery();

			try {
				ResultSet result = getControl().executeQuery(query);
				while (result.next()) {
					categories.add(result.getString("category"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return categories.toArray(new String[categories.size()]);
	}

	public String getServiceID() {
		return m_id;
	}

	public int count(String projectName, String type, Date date) {
		IIssue[] issues = getIssues(projectName);
		if (type.equals(ITSEnum.CLOSED))
			return countDone(issues, date);
		if (type.equals(ITSEnum.RESOLVED))
			return countAssigned(issues, date);
		if (type.equals(ITSEnum.WORKING))
			return countNonAssign(issues, date);
		if (type.equals(ITSEnum.TOTAL))
			return countTotal(issues, date);
		return 0;

	}

	private int countDone(IIssue[] issues, Date date) {
		int count = 0;
		for (IIssue issue : issues) {
			if (issue.getStatusUpdated(ITSEnum.CLOSED_STATUS) == null)
				continue;
			// 一般而言,date都會包含當天,所以在計算時要多加一天
			if (issue.getStatusUpdated(ITSEnum.CLOSED_STATUS).getTime() <= (date.getTime() + 24 * 3600 * 1000)) {

				count++;
			}
		}
		return count;
	}

	private int countAssigned(IIssue[] issues, Date date) {
		int count = 0;
		for (IIssue issue : issues) {
			if (issue.getStatusUpdated(ITSEnum.ASSIGNED_STATUS) == null)
				continue;
			// 一般而言,date都會包含當天,所以在計算時要多加一天
			if (issue.getStatusUpdated(ITSEnum.ASSIGNED_STATUS).getTime() <= (date.getTime() + 24 * 3600 * 1000))
				count++;
		}
		return count;
	}

	private int countNonAssign(IIssue[] issues, Date date) {
		int count = 0;
		for (IIssue issue : issues) {
			if (issue.getWorkingUpdated() == 0)
				continue;
			// 一般而言,date都會包含當天,所以在計算時要多加一天
			if (issue.getWorkingUpdated() <= (date.getTime() + 24 * 3600 * 1000))
				count++;
		}
		return count;
	}

	private int countTotal(IIssue[] issues, Date date) {
		return issues.length;
	}

	@Override
	public void updateBugNote(IIssue issue) {
		m_noteService.updateBugNote(issue);
	}

	@Override
	public void updateIssueNote(IIssue issue, IIssueNote note) {
		m_noteService.updateIssueNote(issue, note);
	}

	public void addHistory(long issueID, String typeName, String oldValue, String newValue) {
		m_historyService.addMantisActionHistory(issueID, typeName, oldValue, newValue, IIssueHistory.OTHER_TYPE, IIssueHistory.NOW_MODIFY_DATE);
	}

	@Override
	public void addRelationship(long sourceID, long targetID, int type, Date date) {
		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName("mantis_bug_relationship_table");
		valueSet.addInsertValue("source_bug_id", Long.toString(sourceID));
		valueSet.addInsertValue("destination_bug_id", Long.toString(targetID));
		valueSet.addInsertValue("relationship_type", Integer.toString(type));

		String query = valueSet.getInsertQuery();
		getControl().execute(query);

		if (type == ITSEnum.PARENT_RELATIONSHIP) {
			m_historyService.addMantisActionHistory(
						sourceID, 
						IIssueHistory.EMPTY_FIELD_NAME, 
						IIssueHistory.PARENT_OLD_VALUE,	
						Long.toString(targetID), 
						IIssueHistory.RELEATIONSHIP_ADD_TYPE, 
						date);
			
			m_historyService.addMantisActionHistory(
						targetID, 
						IIssueHistory.EMPTY_FIELD_NAME,
						IIssueHistory.CHILD_OLD_VALUE, 
						Long.toString(sourceID), 
						IIssueHistory.RELEATIONSHIP_ADD_TYPE, 
						date);
		}
	}

	@Override
	public void removeRelationship(long sourceID, long targetID, int type) {
		if (type == ITSEnum.PARENT_RELATIONSHIP) {
			IQueryValueSet valueSet = new MySQLQuerySet();
			valueSet.addTableName("mantis_bug_relationship_table");
			valueSet.addEqualCondition("source_bug_id", Long.toString(sourceID));
			valueSet.addEqualCondition("destination_bug_id", Long.toString(targetID));
			String query = valueSet.getDeleteQuery();
			// String query = "DELETE FROM `mantis_bug_relationship_table` WHERE
			// source_bug_id = "
			// + sourceID + " AND destination_bug_id = " + targetID;
			getControl().execute(query);
			Date removeTime = new Date();
			m_historyService.addMantisActionHistory(sourceID, IIssueHistory.EMPTY_FIELD_NAME,
					IIssueHistory.PARENT_OLD_VALUE, Long.toString(targetID), IIssueHistory.RELEATIONSHIP_DELETE_TYPE,
					removeTime);
			m_historyService.addMantisActionHistory(targetID, IIssueHistory.EMPTY_FIELD_NAME,
					IIssueHistory.CHILD_OLD_VALUE, Long.toString(sourceID), IIssueHistory.RELEATIONSHIP_DELETE_TYPE,
					removeTime);
		}

	}

	public void updateHandler(IIssue issue, String handler, Date modifyDate) {
		// 變更人員
		int oldActor = getUserID(issue.getAssignto());

		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName("mantis_bug_table");
		valueSet.addInsertValue("handler_id", Integer.toString(getUserID(handler)));

		// 若狀態不為assigned,則改變狀態為assigned
		int oldStatus = ITSEnum.getStatus(issue.getStatus());
		if (oldStatus != ITSEnum.ASSIGNED_STATUS) {
			valueSet.addInsertValue("status", Integer.toString(ITSEnum.ASSIGNED_STATUS));
		}

		valueSet.addEqualCondition("id", Long.toString(issue.getIssueID()));

		String updateQuery = valueSet.getUpdateQuery();

		getControl().execute(updateQuery);

		// 新增歷史記錄
		m_historyService.addMantisActionHistory(issue.getIssueID(), IIssueHistory.HANDLER_FIELD_NAME, oldActor,
				getUserID(handler), IIssueHistory.OTHER_TYPE, IIssueHistory.NOW_MODIFY_DATE);

		if (oldStatus != ITSEnum.ASSIGNED_STATUS)
			m_historyService.addMantisActionHistory(issue.getIssueID(), IIssueHistory.STATUS_FIELD_NAME, oldStatus,
					ITSEnum.ASSIGNED_STATUS, IIssueHistory.OTHER_TYPE, modifyDate);

	}

	@Override
	public void updateName(IIssue issue, String name, Date modifyDate) {

		TranslateSpecialChar translateChar = new TranslateSpecialChar();

		// 變更SUMMARY
		String oldSummary = issue.getSummary();
		oldSummary = translateChar.TranslateDBChar(oldSummary);
		name = translateChar.TranslateDBChar(name);

		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName("mantis_bug_table");
		valueSet.addInsertValue("summary", name);
		valueSet.addEqualCondition("id", Long.toString(issue.getIssueID()));

		String updateQuery = valueSet.getUpdateQuery();

		getControl().execute(updateQuery);

		// 新增歷史記錄
		m_historyService.addMantisActionHistory(issue.getIssueID(), IIssueHistory.SUMMARY, oldSummary, name,
				IIssueHistory.OTHER_TYPE, IIssueHistory.NOW_MODIFY_DATE);
	}

	@Override
	public void changeStatusToClosed(long issueID, int resolution, String bugNote, Date closeDate) {
		IIssue issue = getIssue(issueID);
		int oldStatus = issue.getStatusValue();

		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName("mantis_bug_table");
		valueSet.addInsertValue("status", Integer.toString(ITSEnum.CLOSED_STATUS));
		valueSet.addInsertValue("resolution", Integer.toString(resolution));
		valueSet.addEqualCondition("id", Long.toString(issueID));

		String query = valueSet.getUpdateQuery();

		getControl().execute(query);

		// 新增歷史記錄,還有一個resolution的history,因為不是很重要,就暫時沒加入
		m_historyService.addMantisActionHistory(issueID, IIssueHistory.STATUS_FIELD_NAME, oldStatus,
				ITSEnum.CLOSED_STATUS, IIssueHistory.OTHER_TYPE, closeDate);

		if (bugNote != null && !bugNote.equals("")) {
			issue.addIssueNote(bugNote);
			updateBugNote(issue);
		}
	}

	public void insertBugNote(long issueID, String note) {
		long bugNoteID = m_noteService.insertBugNote(issueID, note);

		// 新增歷史記錄
		m_historyService.addMantisActionHistory(issueID, IIssueHistory.EMPTY_FIELD_NAME, Long.toString(bugNoteID),
				IIssueHistory.ZERO_NEW_VALUE, IIssueHistory.BUGNOTE_ADD_TYPE, IIssueHistory.NOW_MODIFY_DATE);
	}

	@Override
	public void reopenStatusToAssigned(long issueID, String name, String bugNote, Date reopenDate) {
		IIssue issue = getIssue(issueID);
		int oldStatus = issue.getStatusValue();

		// String updateQuery = "UPDATE `mantis_bug_table` SET `status` = '"
		// + ITSEnum.ASSIGNED_STATUS + "', `resolution` = '"
		// + ITSEnum.OPEN_RESOLUTION
		// + "' WHERE `mantis_bug_table`.`id` =" + issueID;

		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName("mantis_bug_table");
		valueSet.addInsertValue("summary", name);
		valueSet.addInsertValue("status", Integer.toString(ITSEnum.ASSIGNED_STATUS));
		valueSet.addInsertValue("resolution", Integer.toString(ITSEnum.OPEN_RESOLUTION));
		valueSet.addEqualCondition("id", Long.toString(issueID));
		String query = valueSet.getUpdateQuery();

		getControl().execute(query);
		// 新增歷史記錄,還有一個resolution的history,因為不是很重要,就暫時沒加入
		m_historyService.addMantisActionHistory(issueID, IIssueHistory.STATUS_FIELD_NAME, oldStatus,
				ITSEnum.ASSIGNED_STATUS, IIssueHistory.OTHER_TYPE, reopenDate);

		if (bugNote != null && !bugNote.equals("")) {
			Element history = new Element(ScrumEnum.HISTORY_TAG);
			history.setAttribute(ScrumEnum.ID_HISTORY_ATTR, DateUtil.format(
					new Date(), DateUtil._16DIGIT_DATE_TIME_2));

			Element notesElem = new Element(ScrumEnum.NOTES);
			notesElem.setText(bugNote);
			history.addContent(notesElem);

			if (history.getChildren().size() > 0) {
				issue.addTagValue(history);
				// 最後將修改的結果更新至DB
				updateBugNote(issue);
			}
		}

	}

	@Override
	public void resetStatusToNew(long issueID, String name, String bugNote, Date resetDate) {
		IIssue issue = getIssue(issueID);
		int oldStatus = issue.getStatusValue();

		// String updateQuery = "UPDATE `mantis_bug_table` SET `status` = '"
		// + ITSEnum.NEW_STATUS + "', `resolution` = '"
		// + ITSEnum.OPEN_RESOLUTION
		// + "', `handler_id` = '0' WHERE `mantis_bug_table`.`id` ="
		// + issueID;
		TranslateSpecialChar translateChar = new TranslateSpecialChar();//accept special char ex: / '
		name = translateChar.TranslateDBChar(name);
		
		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName("mantis_bug_table");
		valueSet.addInsertValue("summary", name);
		valueSet.addInsertValue("status", Integer.toString(ITSEnum.NEW_STATUS));
		valueSet.addInsertValue("resolution", Integer.toString(ITSEnum.OPEN_RESOLUTION));
		valueSet.addInsertValue("handler_id", "0");
		valueSet.addEqualCondition("id", Long.toString(issueID));
		String query = valueSet.getUpdateQuery();

		getControl().execute(query);

		// 新增歷史記錄,還有一個resolution的history,因為不是很重要,就暫時沒加入
		m_historyService.addMantisActionHistory(issueID, IIssueHistory.STATUS_FIELD_NAME, oldStatus,
				ITSEnum.NEW_STATUS, IIssueHistory.OTHER_TYPE, resetDate);

		if (bugNote != null && !bugNote.equals("")) {
			Element history = new Element(ScrumEnum.HISTORY_TAG);
			history.setAttribute(ScrumEnum.ID_HISTORY_ATTR, DateUtil.format(
					new Date(), DateUtil._16DIGIT_DATE_TIME_2));

			Element notesElem = new Element(ScrumEnum.NOTES);
			notesElem.setText(bugNote);
			history.addContent(notesElem);

			if (history.getChildren().size() > 0) {
				issue.addTagValue(history);
				// 最後將修改的結果更新至DB
				updateBugNote(issue);
			}
		}
	}

	public void updateHistoryModifiedDate(long issueID, long historyID, Date date) {
		if (historyID < Long.parseLong("20000000000000")) {
			m_historyService.updateHistoryModifiedDate(historyID, date);
		} else {
			m_noteService.updateHistoryModifiedDate(this.getIssue(issueID), historyID, date);
		}
	}

	@Override
	public void updateIssueContent(IIssue modifiedIssue) {
		m_issueService.updateIssueContent(modifiedIssue);
		// m_noteService.insertBugNote(modifiedIssue.getIssueID(), modifiedIssue
		// .getIssueNotes().get(0).getText());
		// 未修改與修改後的issue比較,更新history
	}

	public void removeIssue(String ID) {
		// 刪除retrospective issue，分別砍掉issue與note的資料檔
		m_issueService.removeIssue(ID);
		m_noteService.removeNote(ID);
		m_historyService.removeHistory(ID);

		// 刪除此Story與其他Sprint or Release的關係
		deleteStoryRelationTable(ID);
	}

	// 刪除story
	public void deleteStory(String ID) {
		IIssue issue = getIssue(Long.parseLong(ID));
		if (issue.getCategory().compareTo(ScrumEnum.STORY_ISSUE_TYPE) == 0) {
			IQueryValueSet valueSet = new MySQLQuerySet();
			valueSet.addTableName("mantis_bug_table");
			valueSet.addEqualCondition("id", ID);
			String query = valueSet.getDeleteQuery();
			getControl().execute(query);

			// 清除StoryRelationTable中有關Stroy的資料
			valueSet.clear();
			valueSet.addTableName("ezscrum_story_relation");
			valueSet.addEqualCondition("storyID", ID);
			query = valueSet.getDeleteQuery();
			getControl().execute(query);

			// 刪除跟此issue有關的tag
			m_tagService.removeStoryTag(ID, "-1");
		}
	}
	
	// 刪除 task
	public void deleteTask(long taskID) {
			// delete task，分別砍掉issue與history的資料檔
			String bug_text_id = m_issueService.getBugTextId(taskID);
			IQueryValueSet valueSet = new MySQLQuerySet();
			valueSet.addTableName("mantis_bug_text_table");
			valueSet.addEqualCondition("id", bug_text_id);
			String query = valueSet.getDeleteQuery();
			getControl().execute(query);
			m_issueService.removeIssue(Long.toString(taskID));
			m_historyService.removeHistory(Long.toString(taskID));
	}

	// 刪除 story 和 task 的關係
	public void deleteRelationship(long storyID, long taskID) {
		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName("mantis_bug_relationship_table");
		valueSet.addEqualCondition("source_bug_id", Long.toString(storyID));
		valueSet.addEqualCondition("destination_bug_id", Long.toString(taskID));
		String query = valueSet.getDeleteQuery();
		getControl().execute(query);
		Date removeTime = new Date();
		// delete task 的訊息記錄在 story 裡面
		m_historyService.addMantisActionHistory(storyID, IIssueHistory.EMPTY_FIELD_NAME,
				IIssueHistory.PARENT_OLD_VALUE, Long.toString(taskID), IIssueHistory.TASK_DELETE_TYPE,
				removeTime);
	}

	public void addAttachFile(long issueID, File attachFile) {
		try {
			PreparedStatement pstmt = getControl().getconnection().prepareStatement(
					"INSERT INTO `mantis_bug_file_table` VALUES(?,?,?, ?,?,?, ?,?,?,?,?)");
			InputStream fin = new FileInputStream(attachFile);
			//pstmt.setInt(1, 0);
			pstmt.setNull(1, java.sql.Types.INTEGER);
			pstmt.setLong(2, issueID);

			pstmt.setString(3, "");
			pstmt.setString(4, "");
			// 將檔案路徑轉為md5,因為mantis要判斷是否有重覆的檔案
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(attachFile.getPath().getBytes());
			pstmt.setString(5, StringUtil.toHexString(md.digest()));

			pstmt.setString(6, attachFile.getName());
			pstmt.setString(7, "");
			pstmt.setInt(8, (int) attachFile.length());
			String Type = "";
			String fileType = attachFile.getName().substring(attachFile.getName().indexOf(".") + 1);
			if (fileType.compareToIgnoreCase("jpg") == 0)
				Type = "image/jpeg";
			else if (fileType.compareToIgnoreCase("xml") == 0)
				Type = "text/xml";
			else
				Type = "application/octet-stream";
			pstmt.setString(9, Type);
			pstmt.setString(10, DateUtil.getCurrentTimeInMySqlTime());
			pstmt.setBinaryStream(11, fin, (int) attachFile.length());
			pstmt.executeUpdate();
			fin.close();
			
			pstmt.clearParameters();
			pstmt.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}finally{
//			getControl().close();
		}

	}

	// ezScrum上面新增帳號，新增進mySQL裡
	public void addUser(String name, String password, String email, String realName, String access_Level,
			String cookie_string, String createDate, String lastVisitDate) throws Exception {
		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName("mantis_user_table");
		valueSet.addLikeCondition("username", name);
		try {
			String query = valueSet.getSelectQuery();
			ResultSet resultSet = getControl().executeQuery(query);
			valueSet.clear();
			// resultSet.first 如果回傳false代表set內是空的
			if (!resultSet.first()) {
				valueSet.addTableName("mantis_user_table");
				valueSet.addInsertValue(AccountEnum.ACCOUNT_NAME, name);
				valueSet.addInsertValue(AccountEnum.ACCOUNT_EMAIL, email);
				valueSet.addInsertValue(AccountEnum.ACCOUNT_PAASSWORD, password);
				valueSet.addInsertValue(AccountEnum.ACCOUNT_REALNAME, realName);
				valueSet.addInsertValue(AccountEnum.ACCOUNT_ACCESS_LEVEL, access_Level);
				valueSet.addInsertValue(AccountEnum.ACCOUNT_COOKIE_STRING, cookie_string);
				valueSet.addInsertValue(AccountEnum.ACCOUNT_DATE_CREATED, createDate);
				valueSet.addInsertValue(AccountEnum.ACCOUNT_LAST_VISIT, lastVisitDate);

				query = valueSet.getInsertQuery();
				getControl().execute(query);
			}

		} catch (Exception e) {
			throw e;
		}
	}

	// 是否存在這個使用者
	public boolean existUser(String name) {
		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName("mantis_user_table");
		valueSet.addLikeCondition("username", name);
		valueSet.addLikeCondition("enabled", "1");

		try {
			String query = valueSet.getSelectQuery();
			ResultSet resultSet = getControl().executeQuery(query);
			// resultSet.first 如果回傳false代表set內是空的
			if (!resultSet.first()) {
				return Boolean.FALSE;
			} else {
				return Boolean.TRUE;
			}
		} catch (Exception e) {
			return Boolean.FALSE;
		}
	}

	// 新增User和Project之間的關係，新增到MySql裡
	public void addUserProjectRelation(String projectName, String name, String access_Level) throws Exception {
		IQueryValueSet valueSet = new MySQLQuerySet();

		try {
			// 取得User的ID
			valueSet.addTableName("mantis_user_table");
			valueSet.addLikeCondition("username", name);
			String query = valueSet.getSelectQuery();
			ResultSet resultSet = getControl().executeQuery(query);
			resultSet.first();
			String userID = resultSet.getString(AccountEnum.ACCOUNT_ID);
			resultSet.close();

			// 取得Project的ID
			valueSet.clear();
			valueSet.addTableName("mantis_project_table");
			valueSet.addLikeCondition("name", projectName);
			query = valueSet.getSelectQuery();
			resultSet = getControl().executeQuery(query);
			resultSet.first();
			String projectID = resultSet.getString(AccountEnum.ACCOUNT_ID);

			// 新增使用者跟Project的Relation
			valueSet.clear();
			valueSet.addTableName("mantis_project_user_list_table");
			valueSet.addInsertValue("project_id", projectID);
			valueSet.addInsertValue("user_id", userID);
			valueSet.addInsertValue("access_level", access_Level);
			query = valueSet.getInsertQuery();
			getControl().execute(query);
		} catch (Exception e) {
			throw e;
		}
	}

	/*
	 * Jcis上面刪除帳號，刪除mySQL裡的資料
	 * 
	 * 警告:欲刪除使用者帳號 請先刪除User和Project之間的關係 否則會造成找不到User_id而無法砍除User和Project之間的關係
	 */
	public void deleteUser(String userid) throws Exception {
		IQueryValueSet valueSet = new MySQLQuerySet();
		try {
			valueSet.addTableName("mantis_user_table");
			valueSet.addLikeCondition("username", userid);
			String query = valueSet.getDeleteQuery();
			getControl().execute(query);

		} catch (Exception e) {
			throw e;
		}
	}

	// 刪除User和Project之間的關係，刪除MySql裡的資料
	public void deleteUserProjectRelation(String userName, String projectName) throws Exception {
		IQueryValueSet valueSet = new MySQLQuerySet();

		try {
			String projectId = "";
			String userId = "";

			// 取得userId
			valueSet.addTableName("mantis_user_table");
			valueSet.addLikeCondition("username", userName);
			String query = valueSet.getSelectQuery();
			ResultSet resultSet = getControl().executeQuery(query);
			if (resultSet.first())
				userId = resultSet.getString(AccountEnum.ACCOUNT_ID);

			// 取得projectId
			valueSet.clear();
			valueSet.addTableName("mantis_project_table");
			valueSet.addLikeCondition("name", projectName);
			query = valueSet.getSelectQuery();
			resultSet = getControl().executeQuery(query);
			if (resultSet.first())
				projectId = resultSet.getString(AccountEnum.ACCOUNT_ID);

			// 刪除user跟project之間的關係
			valueSet.clear();
			valueSet.addTableName("mantis_project_user_list_table");
			valueSet.addLikeCondition("project_id", projectId);
			valueSet.addLikeCondition("user_id", userId);
			query = valueSet.getDeleteQuery();
			getControl().execute(query);

		} catch (Exception e) {
			throw e;
		}
	}

	// 刪除Project所有屬於access_level的使用者
	public void deleteUserProjectRelationByAccessLevel(String projectName, String access_level) throws Exception {
		IQueryValueSet valueSet = new MySQLQuerySet();

		try {
			String projectId = "";

			// 取得projectId
			valueSet.clear();
			valueSet.addTableName("mantis_project_table");
			valueSet.addLikeCondition("name", projectName);
			String query = valueSet.getSelectQuery();
			ResultSet resultSet = getControl().executeQuery(query);

			if (resultSet.first())
				projectId = resultSet.getString(AccountEnum.ACCOUNT_ID);

			// 刪除user跟project之間的關係
			valueSet.clear();
			valueSet.addTableName("mantis_project_user_list_table");
			valueSet.addLikeCondition("project_id", projectId);
			// access_level不同，代表的值也不同
			if (access_level.compareTo(AccountEnum.ACCOUNT_ACTOR_ADMIN) == 0)
				valueSet.addLikeCondition("access_level", AccountEnum.ACCESS_LEVEL_MANAGER);
			else
				valueSet.addLikeCondition("access_level", AccountEnum.ACCESS_LEVEL_VIEWER);

			query = valueSet.getDeleteQuery();
			getControl().execute(query);

		} catch (Exception e) {
			throw new Exception("刪除Mantis權限失敗!");
		}
	}

	// 回傳是否User和任何一個Project有關聯
	public boolean isUserHasRelationByAnyProject(String userName) throws Exception {
		IQueryValueSet valueSet = new MySQLQuerySet();

		try {
			// 取得User的ID
			valueSet.addTableName("mantis_user_table");
			valueSet.addLikeCondition("username", userName);
			String query = valueSet.getSelectQuery();
			ResultSet resultSet = getControl().executeQuery(query);
			resultSet.first();
			String userID = resultSet.getString(AccountEnum.ACCOUNT_ID);
			resultSet.close();

			// 新增使用者跟Project的Relation
			valueSet.clear();
			valueSet.addTableName("mantis_project_user_list_table");
			valueSet.addEqualCondition("user_id", userID);
			query = valueSet.getSelectQuery();
			resultSet = getControl().executeQuery(query);
			if (resultSet.first())
				return true;
		} catch (Exception e) {
			throw e;
		}

		return false;
	}

	// 更新user的資料
	public void updateUserProfile(String userID, String realName, String password, String email, String enable)
			throws Exception {
		try {
			IQueryValueSet valueSet = new MySQLQuerySet();
			valueSet.addTableName("mantis_user_table");
			valueSet.addLikeCondition("username", userID);

			valueSet.addInsertValue(AccountEnum.ACCOUNT_EMAIL, email);
			valueSet.addInsertValue(AccountEnum.ACCOUNT_PAASSWORD, password);
			valueSet.addInsertValue(AccountEnum.ACCOUNT_REALNAME, realName);
			valueSet.addInsertValue(AccountEnum.ACCOUNT_ENABLED, enable);
			String query = valueSet.getUpdateQuery();
			getControl().execute(query);

		} catch (Exception e) {
			throw new Exception("更新Mantis資料發生錯誤");
		}
	}

	/**
	 * 移除附件
	 * 
	 * @param fileId
	 */
	public void deleteAttachFile(long fileId) {
		// 根據attach id移除附件
		try {
			PreparedStatement pstmt = getControl().getconnection().prepareStatement(
					"DELETE FROM `mantis_bug_file_table` WHERE `mantis_bug_file_table`.`id` = ?");
			pstmt.setLong(1, fileId);
			pstmt.executeUpdate();
			pstmt.clearParameters();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void createProject(String ProjectName) throws Exception {
		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName("mantis_project_table");

		valueSet.addInsertValue("name", ProjectName);
		valueSet.addInsertValue("status", ScrumEnum.PROJECT_STATUS);
		valueSet.addInsertValue("enabled", ScrumEnum.PROJECT_ENABLED);
		valueSet.addInsertValue("view_state", ScrumEnum.PROJECT_VIEW_STATE);
		valueSet.addInsertValue("access_min", ScrumEnum.PROJECT_ACCESS_MIN);
		valueSet.addInsertValue("file_path", "");
		valueSet.addInsertValue("description", "");

		String query = valueSet.getInsertQuery();
		try {
			getControl().execute(query);
		} catch (Exception e) {
			throw e;
		}
	}

	public void updateStoryRelationTable(String storyID, String projectName, String releaseID, String sprintID,
			String estimation, String importance, Date date) {

		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName("ezscrum_story_relation");

		valueSet.addInsertValue("storyID", storyID);
		int projectID = getProjectID(projectName);

		valueSet.addInsertValue("projectID", Integer.toString(projectID));

		if (releaseID != null)
			valueSet.addInsertValue("releaseID", releaseID);

		if (sprintID != null)
			valueSet.addInsertValue("sprintID", sprintID);

		if (estimation != null)
			valueSet.addInsertValue("estimation", estimation);

		if (importance != null)
			valueSet.addInsertValue("importance", estimation);

		// 取得時間
		Timestamp now = new Timestamp(new Date().getTime());

		valueSet.addInsertValue("updateDate", now.toString());
		String query = valueSet.getInsertQuery();

		getControl().execute(query);
	}

	public void deleteStoryRelationTable(String storyID) {
		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName("ezscrum_story_relation");
		valueSet.addEqualCondition("storyID", storyID);
		String query = valueSet.getDeleteQuery();
		getControl().execute(query);
	}

	// 新增自訂分類標籤
	public void addNewTag(String name, String projectName) {
		m_tagService.addNewTag(name, projectName);
	}

	// 刪除自訂分類標籤
	public void deleteTag(String id, String projectName) {
		m_tagService.deleteTag(id, projectName);
	}

	// 取得自訂分類標籤列表
	public IIssueTag[] getTagList(String projectName) {
		return m_tagService.getTagList(projectName);
	}

	// 對Story設定自訂分類標籤
	public void addStoryTag(String storyID, String tagID) {
		m_tagService.addStoryTag(storyID, tagID);
	}

	// 移除Story的自訂分類標籤
	public void removeStoryTag(String storyID, String tagID) {
		m_tagService.removeStoryTag(storyID, tagID);
	}

	@Override
	public List<IStory> getStorys(String projectName) {
		List<IStory> result = new ArrayList<IStory>();
		result = m_issueService.getStorys(projectName);

		for (IStory story : result) {
			// 建立歷史記錄
			// m_historyService.initHistory(issue);
			// 設定在mantis上note的資料
			setIssueNote(story);
			// 建立bug note
			// story.setIssueNotes(this.m_noteService.getIssueNotes(story));
			m_attachFileService.initAttachFile(story);
			// 建立tag資料
			m_tagService.initTag(story);
		}

		return result;
	}

	@Override
	public void updateTag(String tagId, String tagName, String projectName) {
		m_tagService.updateTag(tagId, tagName, projectName);
	}

	@Override
	public IIssueTag getTagByName(String name, String projectName) {
		return m_tagService.getTagByName(name, projectName);
	}

	@Override
	public boolean isTagExist(String name, String projectName) {
		return m_tagService.isTagExist(name, projectName);
	}

	/**
	 * 抓取attach file 不透過 mantis
	 */
	public File getAttachFile(String fileID) {
		return m_attachFileService.getAttachFile(fileID);
	}
	
	/**
	 * 抓取attach file 不透過 mantis，透過檔名
	 */
	public File getAttachFileByName(String fileName) {
		return m_attachFileService.getAttachFileByName(fileName);
	}
}
