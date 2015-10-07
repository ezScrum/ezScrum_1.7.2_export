package ntut.csie.ezScrum.web.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.iteration.core.ISprintPlanDesc;
import ntut.csie.ezScrum.iteration.iternal.SprintPlanDesc;
import ntut.csie.ezScrum.pic.core.IUserSession;
import ntut.csie.ezScrum.restful.mobile.support.ConvertSprint;
import ntut.csie.ezScrum.web.control.ProductBacklogHelper;
import ntut.csie.ezScrum.web.dataObject.SprintObject;
import ntut.csie.ezScrum.web.dataObject.StoryObject;
import ntut.csie.ezScrum.web.dataObject.TaskObject;
import ntut.csie.ezScrum.web.form.IterationPlanForm;
import ntut.csie.ezScrum.web.logic.ProductBacklogLogic;
import ntut.csie.ezScrum.web.logic.SprintBacklogLogic;
import ntut.csie.ezScrum.web.logic.SprintPlanLogic;
import ntut.csie.ezScrum.web.mapper.SprintBacklogMapper;
import ntut.csie.ezScrum.web.mapper.SprintPlanMapper;
import ntut.csie.jcis.core.util.DateUtil;
import ntut.csie.jcis.resource.core.IProject;

public class SprintPlanHelper {
	private SprintPlanMapper mSprintPlanMapper;
	private SprintPlanLogic mSprintPlanLogic;
	private SprintBacklogMapper mSprintBacklogMapper;
	
	public SprintPlanHelper(IProject project){
		this.mSprintPlanMapper = new SprintPlanMapper(project);
		this.mSprintPlanLogic = new SprintPlanLogic(project);
		this.mSprintBacklogMapper = (new SprintBacklogLogic(project, null, String.valueOf(this.getCurrentSprintID()))).getSprintBacklogMapper();
	}
	
	public List<ISprintPlanDesc> loadListPlans(){
		return this.mSprintPlanLogic.getSprintPlanListAndSortByStartDate();
	}
	
	public int getCurrentSprintID(){
		return this.mSprintPlanLogic.getCurrentSprintID();
	}
	
	//load the last plan, so perhaps the return is not the current plan.
	public ISprintPlanDesc loadCurrentPlan(){
		return this.mSprintPlanLogic.loadCurrentPlan();
	} 
	
	//get next demoDate
	public String getNextDemoDate(){
		List<ISprintPlanDesc> descs = this.mSprintPlanLogic.getSprintPlanListAndSortById();
		if (descs.size() == 0)
			return null;
		if(!String.valueOf(this.getCurrentSprintID()).equals("-1")){
			ISprintPlanDesc sprintPlanDesc = mSprintPlanMapper.getSprintPlan(String.valueOf(this.getCurrentSprintID()));
			if(sprintPlanDesc.getDemoDate().equals(""))
				return null;
			else
				return sprintPlanDesc.getDemoDate();
		}
		String demoDate=null;	
		Date current = new Date();
		// compare the demo date to find the closed date
		for (ISprintPlanDesc desc:descs){
			String descDemoDate = desc.getDemoDate(); 
			if(descDemoDate.equals(""))
				continue;
			// judge whether the descDemoDate is larger than now 
			if (DateUtil.dayFilter(descDemoDate).getTime() > current.getTime()){
				if(demoDate==null)
					demoDate = descDemoDate;
				// judge whether the demoDate is larger than descDemoDate
				else
					if (DateUtil.dayFilter(demoDate).getTime() > DateUtil.dayFilter(descDemoDate).getTime()){
						demoDate = descDemoDate;
					}
			}
		}
		return demoDate;
	}
	
	public void editIterationPlanForm(IterationPlanForm form){
		SprintPlanDesc desc = new SprintPlanDesc();
		desc.setInterval(form.getIterIterval());
		desc.setMemberNumber(form.getIterMemberNumber());
		desc.setStartDate(form.getIterStartDate());
		desc.setID(form.getID());
		desc.setFocusFactor(form.getFocusFactor());
		desc.setGoal(form.getGoal());
		desc.setAvailableDays(form.getAvailableDays());
		desc.setDemoDate(form.getDemoDate());
		desc.setNotes(form.getNotes());
		desc.setDemoPlace(form.getDemoPlace());
		mSprintPlanMapper.updateSprintPlan(desc);	
	}
			
	public void saveIterationPlanForm(IterationPlanForm form){
		SprintPlanDesc desc = new SprintPlanDesc();
		desc.setInterval(form.getIterIterval());
		desc.setMemberNumber(form.getIterMemberNumber());
		desc.setStartDate(form.getIterStartDate());
		desc.setID(form.getID());
		desc.setFocusFactor(form.getFocusFactor());
		desc.setGoal(form.getGoal());
		desc.setAvailableDays(form.getAvailableDays());
		desc.setDemoDate(form.getDemoDate());
		desc.setNotes(form.getNotes());
		desc.setDemoPlace(form.getDemoPlace());
		mSprintPlanMapper.addSprintPlan(desc);
	}
	
	/**
	 * 只取得一筆 sprint
	 * @param lastsprint
	 * @param sprintID
	 * @return 
	 */
	public ISprintPlanDesc getOneSprintInformation(String lastsprint, String sprintID){
		int SprintID = -1;
		if (lastsprint != null && Boolean.parseBoolean(lastsprint)) {
			SprintID = this.getLastSprintId();
		} else if (sprintID != null) {
			SprintID = Integer.parseInt(sprintID);
		}
		
		if (SprintID > 0) {
			return this.loadPlan(SprintID);
		} else {
			return new SprintPlanDesc();
		}
	}
	
	public Date getProjectStartDate() {
		List<ISprintPlanDesc> sprintList = this.loadListPlans();
		return  DateUtil.dayFilter(sprintList.get(0).getStartDate());
	}

	public Date getProjectEndDate() {
		List<ISprintPlanDesc> sprintList = this.loadListPlans();
		
		if (sprintList.size() > 0) {
			return  DateUtil.dayFilter(sprintList.get(sprintList.size()-1).getEndDate());
		} else {
			return null;
		}
	}

	public int getLastSprintId() {
		List<ISprintPlanDesc> descs = this.mSprintPlanLogic.getSprintPlanListAndSortById();
		if(descs.size() == 0)
			return -1;
		else
			return Integer.parseInt(descs.get(descs.size()-1).getID());
	}
	
	public int getSprintIDbyDate(Date date) {
		int sprintID = -1;
		List<ISprintPlanDesc> sprints = this.mSprintPlanLogic.getSprintPlanListAndSortByStartDate();
			
		for ( ISprintPlanDesc sp : sprints ) {
			// 此 sprint 的結束日期在 date 之後
			if ( DateUtil.dayFilter(sp.getEndDate()).getTime() >= (DateUtil.dayFilter(date)).getTime() ) {
				// 此 sprint 的開始日期在使用者設定之前
				// 兩者成立表示此使用者設定的日期在這個 sprint 區間內，回傳此 sprint ID
				if ( DateUtil.dayFilter(date).getTime() >= (DateUtil.dayFilter(sp.getStartDate())).getTime() ) {
					sprintID = Integer.parseInt(sp.getID());
				break;
				}
			}
		}
			
		return sprintID;	
	}
	
	/*
	 * from AjaxMoveSprintAction
	 */
	
	public void moveSprintPlan(IProject project, IUserSession session, int oldID, int newID) {		
		List<ISprintPlanDesc> descs = this.loadListPlans();
		this.moveSprint(oldID, newID);
				
		ProductBacklogHelper pb = new ProductBacklogHelper(project, session);
		Map<String, List<IIssue>> map = pb.getSprintHashMap();
		
		ArrayList<Integer> list = new ArrayList<Integer>();
		//取出需要修改的sprint ID 
		if(oldID > newID){
			for(int i = newID; i<= oldID;i++){
				if(this.isSprintPlan(descs, i))
					list.add(i);
			}
		}
		else{
			for(int i = 0; i<= newID - oldID;i++){
				if(this.isSprintPlan(descs, newID-i))
					list.add(newID-i);
			}
		}
		
		ProductBacklogLogic productBacklogLogic = new ProductBacklogLogic(session, project);
		//將story的中sprint id做修改
		if(list.size()!=0){
			for(int i = 0;i<list.size(); i++){
				if((i+1)!=list.size()){
					String sprintID = String.valueOf(list.get(i));
					String nextSprintID = String.valueOf(list.get(i+1));
					List<IIssue> stories = map.get(sprintID);
					if(stories!=null){
						ArrayList<Long> total = convertTolong(stories);
						productBacklogLogic.addIssueToSprint(total, nextSprintID);
					}
				}
				else{
					String sprintID = String.valueOf(list.get(i));
					String nextSprintID = String.valueOf(list.get(0));
					List<IIssue> stories = map.get(sprintID);
					if(stories!=null){
						ArrayList<Long> total = convertTolong(stories);
						productBacklogLogic.addIssueToSprint(total, nextSprintID);
					}
				}
			}
		}		
	}

	private ArrayList<Long> convertTolong(List<IIssue> stories){
		ArrayList<Long> total = new ArrayList<Long>();
		for(IIssue story:stories){
			total.add(story.getIssueID());
		}
		return total;
	}
	
	private boolean isSprintPlan(List<ISprintPlanDesc> descs, int iteration){
		String iter = String.valueOf(iteration);
		for(ISprintPlanDesc desc: descs){
			if(desc.getID().equals(iter)){
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}
	
	public ISprintPlanDesc loadPlan(String ID){
		return mSprintPlanMapper.getSprintPlan(ID);
	}
	
	/*
	 * move from Mapper
	 */
	// move the specific sprint to other sprint
	public void moveSprint(int oldID, int newID) {
		//移動iterPlan.xml的資訊
		mSprintPlanMapper.moveSprintPlan(oldID, newID);
	}
	
	public ISprintPlanDesc loadPlan(int iteration){
		return mSprintPlanMapper.getSprintPlan(Integer.toString(iteration));
	}
	
	public void editSprintPlanForActualCost(String sprintID, String actualCost) {		
		ISprintPlanDesc sprintPlan = this.loadPlan(sprintID);
		sprintPlan.setActualCost(actualCost);		
		mSprintPlanMapper.updateSprintPlanForActualCost(sprintPlan);
	}
	
	public void createSprint(SprintObject sprint) {
		mSprintPlanMapper.addSprintPlan(ConvertSprint.convertSprintObjectToDesc(sprint));
	}
	
	public void deleteSprint(String id) {
		mSprintPlanMapper.deleteSprintPlan(id);
	}
	
	public void updateSprint(SprintObject sprintObject) {
		mSprintPlanMapper.updateSprintPlan(ConvertSprint.convertSprintObjectToDesc(sprintObject));
	}
	
	public List<SprintObject> getAllSprint() {
		return ConvertSprint.convertSprintDescToObject(mSprintPlanMapper.getSprintPlanList());
	}
	
	public SprintObject getSprintWithAllItem(String sprintID) {
		SprintObject sprint = new SprintObject(loadPlan(sprintID));
		// 找出 sprint 中所有的 story
		IIssue[] storyIIssues = mSprintBacklogMapper.getStoryInSprint(Long.parseLong(sprintID));
		for (IIssue storyIssue : storyIIssues) {
			StoryObject story = new StoryObject(storyIssue);
			// 找出 story 中所有的 task
			IIssue[] taskIIssues = mSprintBacklogMapper.getTaskInStory(Long.parseLong(story.id));
			for (IIssue taskIssue : taskIIssues)
				story.addTask(new TaskObject(taskIssue));
			sprint.addStory(story);
		}
		return sprint;
	}
}
