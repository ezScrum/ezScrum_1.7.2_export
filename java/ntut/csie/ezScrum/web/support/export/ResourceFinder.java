package ntut.csie.ezScrum.web.support.export;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.iteration.core.ISprintPlanDesc;
import ntut.csie.ezScrum.iteration.core.ScrumEnum;
import ntut.csie.ezScrum.web.helper.SprintPlanHelper;
import ntut.csie.ezScrum.web.mapper.ProductBacklogMapper;
import ntut.csie.ezScrum.web.mapper.ProjectMapper;
import ntut.csie.jcis.resource.core.IProject;

public class ResourceFinder {
	public static IProject findProject(String projectName) {
		List<IProject> projects = new ProjectMapper().getAllProjectList();
		for (IProject project : projects) {
			if (project.getName().equals(projectName)) {
				return project;
			}
		}
		return null;
	}
	
	public static ISprintPlanDesc findSprint(String projectName, long sprintId) {
		IProject project = findProject(projectName);
		if (project == null) {
			return null;
		}
		SprintPlanHelper sprintPlanHelper = new SprintPlanHelper(project);
		List<ISprintPlanDesc> sprints = sprintPlanHelper.loadListPlans();
		for (ISprintPlanDesc sprint : sprints) {
			if (sprint.getID().equals(String.valueOf(sprintId))) {
				return sprint;
			}
		}
		return null;
	}
	
	public static IIssue findStory(String projectName, long sprintId, long storyId) {
		IProject project = findProject(projectName);
		ISprintPlanDesc sprint = findSprint(projectName, sprintId);
		if (project == null || sprint == null) {
			return null;
		}
		// Create ProductbacklogMapper
		ProductBacklogMapper productBacklogMapper = new ProductBacklogMapper(project, null);
		// Get Stories
		IIssue[] storyArray = productBacklogMapper.getIssues(ScrumEnum.STORY_ISSUE_TYPE);
		List<IIssue> stories = new ArrayList<IIssue>();
		stories.addAll(Arrays.asList(storyArray));

		for (IIssue story : stories) {
			if (story.getIssueID() == storyId) {
				return story;
			}
		}
		return null;
	}
}
