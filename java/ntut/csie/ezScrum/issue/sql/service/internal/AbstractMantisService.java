package ntut.csie.ezScrum.issue.sql.service.internal;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ntut.csie.ezScrum.issue.core.ITSEnum;
import ntut.csie.ezScrum.issue.sql.service.core.IQueryValueSet;
import ntut.csie.ezScrum.issue.sql.service.core.ITSPrefsStorage;
import ntut.csie.ezScrum.issue.sql.service.tool.ISQLControl;

public abstract class AbstractMantisService {

	private ISQLControl m_control;
	private ITSPrefsStorage m_prefs;
	
	protected int getUserID(String userName) {
		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName("mantis_user_table");
		valueSet.addLikeCondition("username", userName);
		String query = valueSet.getSelectQuery();
		// String query = "SELECT `id` FROM `mantis_user_table` WHERE `username`
		// LIKE '"
		// + userName + "'";
		try {
			ResultSet result = m_control.executeQuery(query);
			int userID = 0;
			if (result.next()) {
				userID = result.getInt("id");
			}
			return userID;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	protected String getUserName(int userID) {
		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName("mantis_user_table");
		valueSet.addEqualCondition("id", Integer.toString(userID));
		String query = valueSet.getSelectQuery();
		// String query = "SELECT `username` FROM `mantis_user_table` WHERE `id`
		// ="
		// + userID;
		try {
			ResultSet result = m_control.executeQuery(query);
			String userName = "";
			if (result.next()) {
				userName = result.getString("username");
			}
			return userName;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	protected void setControl(ISQLControl control){
		m_control = control;
	}
	
	protected ISQLControl getControl(){
		return m_control;
	}
	
	protected void setPrefs(ITSPrefsStorage prefs){
		m_prefs = prefs;
	}
	
	protected ITSPrefsStorage getPrefs(){
		return m_prefs;
	}
	
	protected int getProjectID(String projectName) {
		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName("mantis_project_table");
		valueSet.addLikeCondition("name", projectName);
		String query = valueSet.getSelectQuery();
		// String query = "SELECT `id` FROM `mantis_project_table` WHERE `name`
		// LIKE '"
		// + projectName + "'";
		// Statement stmt;
		try {
			// stmt = conn.createStatement();
			// ResultSet result = stmt.executeQuery(query);
			ResultSet result = getControl().executeQuery(query);
			int projectID = -1;
			if (result.next())
				projectID = result.getInt("id");
			return projectID;

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	protected String getProjectName(int projectID) {
		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName("mantis_project_table");
		valueSet.addLikeCondition("id", String.valueOf(projectID));
		String query = valueSet.getSelectQuery();
		try {
			ResultSet result = getControl().executeQuery(query);
			String projectName="";
			if (result.next())
				projectName = result.getString("name");
			return projectName;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	protected int getProjectAccessLevel(int userID, int projectID) {
		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName("mantis_project_user_list_table");
		valueSet.addEqualCondition("project_id", Integer.toString(projectID));
		valueSet.addEqualCondition("user_id", Integer.toString(userID));
		String query = valueSet.getSelectQuery();
		// String query = "SELECT `access_level` FROM
		// `mantis_project_user_list_table` WHERE `project_id` ="
		// + projectID + " AND `user_id` =" + userID;
		// Statement stmt;
		try {
			// stmt = conn.createStatement();
			// ResultSet result = stmt.executeQuery(query);
			ResultSet result = getControl().executeQuery(query);
			if (result.next()) {
				int accessLevel = result.getInt("access_level");
				return accessLevel;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (getProjectViewStatus(projectID))
			return getDefaultAccessLevel(userID);
		return 0;
	}

	protected boolean getProjectViewStatus(int projectID) {
		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName("mantis_project_table");
		valueSet.addEqualCondition("id", Integer.toString(projectID));
		String query = valueSet.getSelectQuery();

		try {
			ResultSet result = getControl().executeQuery(query);
			if (result.next()) {
				if (result.getInt("view_state") > ITSEnum.PUBLIC_VIEW_STATUS)
					return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}

	protected int getDefaultAccessLevel(int userID) {
		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName("mantis_user_table");
		valueSet.addEqualCondition("id", Integer.toString(userID));

		String query = valueSet.getSelectQuery();

		// String query = "SELECT `access_level` FROM `mantis_user_table` WHERE
		// `id` ="
		// + userID;
		// Statement stmt;
		try {
			// stmt = conn.createStatement();
			// ResultSet result = stmt.executeQuery(query);
			ResultSet result = getControl().executeQuery(query);

			int accessLevel = ITSEnum.VIEWER_ACCESS_LEVEL;

			if (result.next())
				accessLevel = result.getInt("access_level");

			return accessLevel;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ITSEnum.VIEWER_ACCESS_LEVEL;
	}

	protected List<String> getAllUsers() {
		List<String> list = new ArrayList<String>();

		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName("mantis_user_table");
		valueSet.addEqualCondition("enabled", "1");
		String query = valueSet.getSelectQuery();
		
		// String query = "SELECT `username` FROM `mantis_user_table` WHERE `enabled` = 1";
		try {
			ResultSet result = getControl().executeQuery(query);
			while (result.next()) {
				list.add(result.getString("username"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
}
