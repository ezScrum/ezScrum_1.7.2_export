package ntut.csie.ezScrum.restful.export.support;

import java.util.List;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.iteration.core.ISprintPlanDesc;
import ntut.csie.ezScrum.iteration.core.ScrumEnum;
import ntut.csie.ezScrum.web.control.ProductBacklogHelper;
import ntut.csie.ezScrum.web.helper.SprintBacklogHelper;
import ntut.csie.ezScrum.web.helper.SprintPlanHelper;
import ntut.csie.ezScrum.web.helper.UnplannedItemHelper;
import ntut.csie.ezScrum.web.mapper.ProductBacklogMapper;
import ntut.csie.ezScrum.web.mapper.ProjectMapper;
import ntut.csie.jcis.resource.core.IProject;

public class ResourceFinder {
	private IProject mProject;
	private ISprintPlanDesc mSprint;
	private IIssue mStory;
	

	public IProject findProject(String projectName) {
		List<IProject> projects = new ProjectMapper().getAllProjectList();
		for (IProject project : projects) {
			if (project.getName().equals(projectName)) {
				mProject = project;
				return project;
			}
		}
		return null;
	}

	public ISprintPlanDesc findSprint(long sprintId) {
		if (mProject == null) {
			return null;
		}
		SprintPlanHelper sprintPlanHelper = new SprintPlanHelper(mProject);
		ISprintPlanDesc sprint = sprintPlanHelper.loadPlan(String.valueOf(sprintId));
		if (!sprint.getGoal().isEmpty()) {
			mSprint = sprint;
			return sprint;
		}
		return null;
	}

	public IIssue findStory(long storyId) {
		if (mProject == null || mSprint == null) {
			return null;
		}
		// Create ProductbacklogMapper
		ProductBacklogMapper productBacklogMapper = new ProductBacklogMapper(mProject, null);
		// Get Story
		IIssue story = productBacklogMapper.getIssue(storyId);
		if (story != null) {
			if (mSprint.getID().equals(story.getSprintID())
			        && story.getIssueID() == storyId && story.getCategory().equals(ScrumEnum.STORY_ISSUE_TYPE)) {
				mStory = story;
				return story;
			}
		}
		return null;
	}
	
	public IIssue findTask(long taskId) {
		if (mProject == null || mSprint == null || mStory == null) {
			return null;
		}
		SprintBacklogHelper sprintBacklogHelper = new SprintBacklogHelper(mProject, null, mSprint.getID());
		IIssue[] tasks = sprintBacklogHelper.getTaskInStory(String.valueOf(mStory.getIssueID()));
		for (IIssue task : tasks) {
			if (task.getIssueID() == taskId) {
				return task;
			}
		}
		return null;
	}

	public IIssue findDroppedStory(long storyId) {
		if (mProject == null) {
			return null;
		}
		// Create ProductbacklogMapper
		ProductBacklogMapper productBacklogMapper = new ProductBacklogMapper(mProject, null);
		// Get Story
		IIssue story = productBacklogMapper.getIssue(storyId);
		if (story != null) {
			long sprintId = Long.parseLong(story.getSprintID());
			if (sprintId <= 0 && story.getIssueID() == storyId && story.getCategory().equals(ScrumEnum.STORY_ISSUE_TYPE)) {
				mStory = story;
				return story;
			}
		}
		return null;
	}

	public IIssue findUnplan(long unplanId) {
		if (mProject == null || mSprint == null) {
			return null;
		}

		// Create UnplannedItemHelper
		UnplannedItemHelper unplannedItemHelper = new UnplannedItemHelper(mProject, null);
		IIssue unplannedItem = unplannedItemHelper.getIssue(unplanId);
		return unplannedItem;
	}

	public IIssue findDroppedTask(long taskId) {
		if (mProject == null) {
			return null;
		}
		
		ProductBacklogHelper productBacklogHelper = new ProductBacklogHelper(mProject, null);
		IIssue[] taskArray = productBacklogHelper.getAddableTasks();
		for (IIssue task : taskArray) {
			if (task.getIssueID() == taskId) {
				return task;
			}
		}
		return null;
	}
	
	public IIssue findTaskInDroppedStory(long taskId) {
		if (mProject == null || mStory == null) {
			return null;
		}
		
		SprintBacklogHelper sprintBacklogHelper = new SprintBacklogHelper(mProject, null);
		IIssue[] taskArray = sprintBacklogHelper.getTaskInStory(String.valueOf(mStory.getIssueID()));
		for (IIssue task : taskArray) {
			if (task.getIssueID() == taskId) {
				return task;
			}
		}
		return null;
	}
}