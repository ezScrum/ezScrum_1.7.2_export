package ntut.csie.ezScrum.web.support.export;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import ntut.csie.ezScrum.web.databaseEnum.ProjectEnum;
import ntut.csie.jcis.resource.core.IProject;

public class ProjectFluent {
	private ArrayList<IProject> mProjects;

	public ProjectFluent() {
		mProjects = new ArrayList<IProject>();
	}

	// Get a Project
	public ProjectFluent Get(IProject project) {
		mProjects.add(project);
		return this;
	}

	// Get Projects
	public ProjectFluent Get(List<IProject> projects) {
		mProjects.addAll(projects);
		return this;
	}

	// Translate multiple project to JSON
	public JSONObject toJSON() throws JSONException {
		JSONObject projectsJson = new JSONObject();

		if (mProjects.isEmpty()) {
			return projectsJson;
		} else if (mProjects.size() == 1) {
			projectsJson = toJSON(mProjects.get(0));
		} else {
			JSONArray projectsJsonArray = new JSONArray();
			for (IProject project : mProjects) {
				projectsJsonArray.put(toJSON(project));
			}
			projectsJson.put("projects", projectsJsonArray);
		}
		return projectsJson;
	}

	// Translate project to JSON
	private JSONObject toJSON(IProject project) throws JSONException {
		JSONObject projectJson = new JSONObject();
		projectJson.put(ProjectEnum.NAME, project.getProjectDesc().getName())
		        .put(ProjectEnum.DISPLAY_NAME, project.getProjectDesc().getDisplayName())
		        .put(ProjectEnum.COMMENT, project.getProjectDesc().getComment())
		        .put(ProjectEnum.PRODUCT_OWNER, project.getProjectDesc().getProjectManager())
		        .put(ProjectEnum.ATTATCH_MAX_SIZE, project.getProjectDesc().getAttachFileSize())
		        .put(ProjectEnum.CREATE_TIME, project.getProjectDesc().getCreateDate().getTime())
		        .put(ProjectEnum.UPDATE_TIME, System.currentTimeMillis());
		return projectJson;
	}
}
