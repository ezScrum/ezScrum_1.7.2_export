package ntut.csie.ezScrum.web.action;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.google.gson.Gson;

public class GetProjectLeftTreeItem extends PermissionAction {

	@Override
	public boolean isValidAction() {
		return true;
	}

	@Override
	public boolean isXML() {
		// html
		return false;
	}

	@Override
	public StringBuilder getResponse(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		
		List<ParentSideUI> psuiList = new LinkedList<ParentSideUI>();
		appendChildren(psuiList);
		
		Gson gson = new Gson();
		
		return new StringBuilder(gson.toJson(psuiList));
	}
	
	private void appendChildren(List<ParentSideUI> list) {
		appendProjectConfig(list);
		appendProjectManagement(list);
//		appendPracticeGuide(list); 不對外開放
//		appendezTrackInformation(list); 不對外開放
		appendITSConfig(list);
		appendPluginConfig(list);
	}
	
	
	// Project Configuration Side
	private final String Project_Configuration_ID = "ProjectConfig";
	private final String Project_Configuration_Text = "Project Configuration";
	private final String Project_Configuration_Summary_ID = "SummaryUrl";
	private final String Project_Configuration_Summary_Text = "Summary";
	private final String Project_Configuration_ModifyConfig_ID = "ModifyConfigUrl";
	private final String Project_Configuration_ModifyConfig_Text = "Modify Config";
	private final String Project_Configuration_Members_ID = "MembersUrl";
	private final String Project_Configuration_Members_Text = "Members";
	private void appendProjectConfig(List<ParentSideUI> list) {
		ParentSideUI ProjectConfig = new ParentSideUI(this.Project_Configuration_ID, this.Project_Configuration_Text);

		// summary page
		ProjectConfig.appendChild(new ChildrenSiidUI(this.Project_Configuration_Summary_ID, this.Project_Configuration_Summary_Text));
		// members page
		ProjectConfig.appendChild(new ChildrenSiidUI(this.Project_Configuration_Members_ID, this.Project_Configuration_Members_Text));
		
		if ( super.getScrumRole().getEditProject() || super.getScrumRole().isAdmin() ) {
			// project config page
			ProjectConfig.appendChild(new ChildrenSiidUI(this.Project_Configuration_ModifyConfig_ID, this.Project_Configuration_ModifyConfig_Text));
		}
		
		list.add(ProjectConfig);
	}
	// Project Configuration Side
	
	
	// Project Management Side
	private final String Project_Management_ID = "ProjectMgt";
	private final String Project_Management_Text = "Project Management";
	private final String Project_Management_ProductBacklog_ID = "ProductBacklogUrl";
	private final String Project_Management_ProductBacklog_Text = "Product Backlog";
	private final String Project_Management_ReleasePlan_ID = "ReleasePlanUrl";
	private final String Project_Management_ReleasePlan_Text = "Release Plan";
	private final String Project_Management_SprintPlan_ID = "SprintPlanUrl";
	private final String Project_Management_SprintPlan_Text = "Sprint Plan";
	private final String Project_Management_SprintBacklog_ID = "SprintBaclogUrl";
	private final String Project_Management_SprintBacklog_Text = "Sprint Backlog";
	private final String Project_Management_TaskBoard_ID = "TaskBoardUrl";
	private final String Project_Management_TaskBoard_Text = "TaskBoard";
	private final String Project_Management_Retrospective_ID = "RetrospectiveUrl";
	private final String Project_Management_Retrospective_Text = "Retrospective";
	private final String Project_Management_Unplanned_ID = "UnplannedUrl";
	private final String Project_Management_Unplanned_Text = "Unplanned";
	private final String Project_Management_ScrumReport_ID = "ScrumReportUrl";
	private final String Project_Management_ScrumReport_Text = "Scrum Report";
	private void appendProjectManagement(List<ParentSideUI> list) {
		ParentSideUI ProjectManagement = new ParentSideUI(this.Project_Management_ID, this.Project_Management_Text);
		
		// product backlog page
		if ( super.getScrumRole().getAccessProductBacklog() || super.getScrumRole().isAdmin()) {
			ProjectManagement.appendChild(new ChildrenSiidUI(this.Project_Management_ProductBacklog_ID, this.Project_Management_ProductBacklog_Text));
		}
		
		// release plan page
		if ( super.getScrumRole().getAccessReleasePlan() || super.getScrumRole().isAdmin()) {
			ProjectManagement.appendChild(new ChildrenSiidUI(this.Project_Management_ReleasePlan_ID, this.Project_Management_ReleasePlan_Text));
		}
		
		// sprint plan page
		if ( super.getScrumRole().getAccessSprintPlan() || super.getScrumRole().isAdmin()) {
			ProjectManagement.appendChild(new ChildrenSiidUI(this.Project_Management_SprintPlan_ID, this.Project_Management_SprintPlan_Text));
		}
		
		// sprint backlog page
		if ( super.getScrumRole().getAccessSprintBacklog() || super.getScrumRole().isAdmin()) {
			ProjectManagement.appendChild(new ChildrenSiidUI(this.Project_Management_SprintBacklog_ID, this.Project_Management_SprintBacklog_Text));
		}
		
		// task board page
		if ( super.getScrumRole().getAccessTaskBoard() || super.getScrumRole().isAdmin()) {
			ProjectManagement.appendChild(new ChildrenSiidUI(this.Project_Management_TaskBoard_ID, this.Project_Management_TaskBoard_Text));
		}
		
		// retrospective page
		if ( super.getScrumRole().getAccessRetrospective() || super.getScrumRole().isAdmin()) {
			ProjectManagement.appendChild(new ChildrenSiidUI(this.Project_Management_Retrospective_ID, this.Project_Management_Retrospective_Text));
		}
		
		// unplanned page
		if ( super.getScrumRole().getAccessUnplannedItem() || super.getScrumRole().isAdmin()) {
			ProjectManagement.appendChild(new ChildrenSiidUI(this.Project_Management_Unplanned_ID, this.Project_Management_Unplanned_Text));
		}
		
		// scrum report page
		if ( super.getScrumRole().getReadReport() || super.getScrumRole().isAdmin()) {
			ProjectManagement.appendChild(new ChildrenSiidUI(this.Project_Management_ScrumReport_ID, this.Project_Management_ScrumReport_Text));
		}

		list.add(ProjectManagement);
	}
	// Project Management Side
	
	
	// ITS configuration Side
	private final String Project_ITSConfiguration_ID = "itsconfig";
	private final String Project_ITSConfiguration_Text = "ITS configuration";
	private final String Project_ITSConfiguration_ITSConfig_ID = "ITSmodifyUrl";
	private final String Project_ITSConfiguration_ITSConfig_Text = "ITS configuration";
	private void appendITSConfig(List<ParentSideUI> list) {
		if ( super.getScrumRole().getEditProject() || super.getScrumRole().isAdmin() ) {
			// ITS config page
			ParentSideUI ITSConfigurration = new ParentSideUI(this.Project_ITSConfiguration_ID, this.Project_ITSConfiguration_Text);
			ITSConfigurration.appendChild(new ChildrenSiidUI(this.Project_ITSConfiguration_ITSConfig_ID, this.Project_ITSConfiguration_ITSConfig_Text));
			
			list.add(ITSConfigurration);
		}
	}
	
	//plugin configuration
	private final String Project_PluginConfigration_ID = "pluginConfig";
	private final String Project_PluginConfiguration_Text = "Plugin Configuration";
	private final String Project_PluginConfiguration_ITSConfig_ID = "PluginUrl";
	private final String Project_PluginConfiguration_ITSConfig_Text = "Plugin configuration";
	private void appendPluginConfig(List<ParentSideUI> list){
		ParentSideUI ITSConfigurration = new ParentSideUI(this.Project_PluginConfigration_ID, this.Project_PluginConfiguration_Text);
		ITSConfigurration.appendChild(new ChildrenSiidUI(this.Project_PluginConfiguration_ITSConfig_ID, this.Project_PluginConfiguration_ITSConfig_Text));
		
		list.add(ITSConfigurration);
		
	}
	// ITS configuration Side
	
	/* 不對外開放
	// Practice Guide Side
	private final String Project_PracticeGuide_ID = "PracticeGuide";
	private final String Project_PracticeGuide_Text = "Practice Guide";
	private final String Project_PracticeGuide_DOD_ID = "DoDUrl";
	private final String Project_PracticeGuide_DOD_Text = "DoD Info";
	private final String Project_PracticeGuide_CodeReview_ID = "CodeReviewUrl";
	private final String Project_PracticeGuide_CodeReview_Text = "Code Review";
	private void appendPracticeGuide(List<ParentSideUI> list) {
		// Practice Guide page
		ParentSideUI PracticeGuide = new ParentSideUI(this.Project_PracticeGuide_ID, this.Project_PracticeGuide_Text);
		PracticeGuide.appendChild(new ChildrenSiidUI(this.Project_PracticeGuide_DOD_ID, this.Project_PracticeGuide_DOD_Text));
		PracticeGuide.appendChild(new ChildrenSiidUI(this.Project_PracticeGuide_CodeReview_ID, this.Project_PracticeGuide_CodeReview_Text));
		
		list.add(PracticeGuide);
	}	
	// Practice Guide Side
	*/
	
	/* 不對外開放
	// ezTrack Side
	private final String Project_ezTrack_ID = "ezTrackInfo";
	private final String Project_ezTrack_Text = "ezTrack Information";
	private final String Project_ezTrack_ViewIssue_ID = "ViewIssuesUrl";
	private final String Project_ezTrack_ViewIssue_Text = "View Issues";
	private final String Project_ezTrack_ManageIssueType_ID = "ManageIssueTypeUrl";
	private final String Project_ezTrack_ManageIssueType_Text = "Manage Issue Type";
	private final String Project_ezTrack_Report_ID = "ezTrackReportUrl";
	private final String Project_ezTrack_Report_Text = "ezTrack Report";
	private void appendezTrackInformation(List<ParentSideUI> list) {
		// ezTrack page
		ParentSideUI ezTrackInformation = new ParentSideUI(this.Project_ezTrack_ID, this.Project_ezTrack_Text);
		ezTrackInformation.appendChild(new ChildrenSiidUI(this.Project_ezTrack_ViewIssue_ID, this.Project_ezTrack_ViewIssue_Text));
		ezTrackInformation.appendChild(new ChildrenSiidUI(this.Project_ezTrack_ManageIssueType_ID, this.Project_ezTrack_ManageIssueType_Text));
		ezTrackInformation.appendChild(new ChildrenSiidUI(this.Project_ezTrack_Report_ID, this.Project_ezTrack_Report_Text));
		
		list.add(ezTrackInformation);
	}	
	// ezTrack Side
	*/

	// Parent Side
	private class ParentSideUI {
		private String id = "";			// 一定要設定給頁面對應的 ID
		private String text = "";		// 一定要設定 Text
		private String iconCls = "None";
    	private String cls = "treepanel-parent";
    	private boolean expanded = true;
    	private List<ChildrenSiidUI> children = new LinkedList<ChildrenSiidUI>();
    	
    	public ParentSideUI(String id, String text) {
    		this.id = id;
    		this.text = text;
    	}
    	
    	public void appendChild(ChildrenSiidUI csui) {
    		this.children.add(csui);
    	}
	}
	
	
	// Children Side
	private class ChildrenSiidUI {
		private String id = "";		// 一定要設定給頁面對應的 ID
		private String text = "";	// 一定要設定 Text
    	private String cls = "treepanel-leaf-line";
    	private String iconCls = "leaf-icon";
		private boolean leaf = true;
		
		private ChildrenSiidUI(String id, String text) {
			this.id = id;
			this.text = text;
		}
	}
}