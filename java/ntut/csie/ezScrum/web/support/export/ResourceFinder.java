package ntut.csie.ezScrum.web.support.export;

import java.util.List;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.iteration.core.ISprintPlanDesc;
import ntut.csie.ezScrum.iteration.core.ScrumEnum;
import ntut.csie.ezScrum.web.helper.SprintPlanHelper;
import ntut.csie.ezScrum.web.mapper.ProductBacklogMapper;
import ntut.csie.ezScrum.web.mapper.ProjectMapper;
import ntut.csie.jcis.resource.core.IProject;

public class ResourceFinder {
	private IProject mProject;
	private ISprintPlanDesc mSprint;

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
		List<ISprintPlanDesc> sprints = sprintPlanHelper.loadListPlans();
		for (ISprintPlanDesc sprint : sprints) {
			if (sprint.getID().equals(String.valueOf(sprintId))) {
				mSprint = sprint;
				return sprint;
			}
		}
		return null;
	}

	public IIssue findStory(long storyId) {
		if (mProject == null || mSprint == null) {
			return null;
		}
		// Create ProductbacklogMapper
		ProductBacklogMapper productBacklogMapper = new ProductBacklogMapper(mProject, null);
		// Get Stories
		IIssue[] storyArray = productBacklogMapper.getIssues(ScrumEnum.STORY_ISSUE_TYPE);

		for (IIssue story : storyArray) {
			if (mSprint.getID().equals(story.getSprintID())
			   && story.getIssueID() == storyId) {
				return story;
			}
		}
		return null;
	}
	
	public IIssue findWildStory(long storyId) {
		if (mProject == null) {
			return null;
		}
		// Create ProductbacklogMapper
		ProductBacklogMapper productBacklogMapper = new ProductBacklogMapper(mProject, null);
		// Get Stories
		IIssue[] storyArray = productBacklogMapper.getIssues(ScrumEnum.STORY_ISSUE_TYPE);

		for (IIssue story : storyArray) {
			if (story.getIssueID() == storyId) {
				return story;
			}
		}
		return null;
	}
}