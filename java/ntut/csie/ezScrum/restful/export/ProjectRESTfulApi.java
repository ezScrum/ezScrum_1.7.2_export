package ntut.csie.ezScrum.restful.export;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ntut.csie.ezScrum.issue.core.IIssueTag;
import ntut.csie.ezScrum.pic.core.ScrumRole;
import ntut.csie.ezScrum.restful.export.jsonEnum.ScrumRoleJSONEnum;
import ntut.csie.ezScrum.restful.export.support.JSONEncoder;
import ntut.csie.ezScrum.restful.export.support.ResourceFinder;
import ntut.csie.ezScrum.web.helper.ProductBacklogHelper;
import ntut.csie.ezScrum.web.mapper.ProjectMapper;
import ntut.csie.ezScrum.web.mapper.ScrumRoleMapper;
import ntut.csie.jcis.account.core.IAccount;
import ntut.csie.jcis.resource.core.IProject;

@Path("projects")
public class ProjectRESTfulApi {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProjects() {
		// Get projects
		List<IProject> projects = new ProjectMapper().getAllProjectList();
		// Get Projects List JSON
		String entity = JSONEncoder.toProjectJSONArray(projects).toString();
		return Response.status(Response.Status.OK).entity(entity).build();
	}
	
	@GET
	@Path("/{projectName}/tags")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTagsInProject(@PathParam("projectName") String projectName) {
		ResourceFinder resourceFinder = new ResourceFinder();
		IProject project = resourceFinder.findProject(projectName);
		
		if (project == null) {
			System.err.println("404 project null");
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		
		ProductBacklogHelper productBacklogHelper = new ProductBacklogHelper(null, project);
		IIssueTag[] tagArray = productBacklogHelper.getTagList();
		// Get Tag List JSON
		String entity = JSONEncoder.toTagJSONArray(Arrays.asList(tagArray)).toString();
		return Response.status(Response.Status.OK).entity(entity).build();
	}
	
	@GET
	@Path("/{projectName}/scrumroles")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getScrumRolesInProject(@PathParam("projectName") String projectName) {
		ResourceFinder resourceFinder = new ResourceFinder();
		IProject project = resourceFinder.findProject(projectName);
		
		if (project == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		
		ScrumRoleMapper scrumRoleMapper = new ScrumRoleMapper();
		ScrumRole productOwner = scrumRoleMapper.getPermission(projectName, ScrumRoleJSONEnum.PRODUCT_OWNER);
		ScrumRole scrumMaster = scrumRoleMapper.getPermission(projectName, ScrumRoleJSONEnum.SCRUM_MASTER);
		ScrumRole scrumTeam = scrumRoleMapper.getPermission(projectName, ScrumRoleJSONEnum.SCRUM_TEAM);
		ScrumRole stakeholder = scrumRoleMapper.getPermission(projectName, ScrumRoleJSONEnum.STAKEHOLDER);
		ScrumRole guest = scrumRoleMapper.getPermission(projectName, ScrumRoleJSONEnum.GUEST);
		
		String entity = JSONEncoder.toScrumRolesJSON(productOwner, scrumMaster, scrumTeam, stakeholder, guest).toString();
		return Response.status(Response.Status.OK).entity(entity).build();
	}
	
	@GET
	@Path("/{projectName}/projectroles")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProjectRolesInProject(@PathParam("projectName") String projectName) {
		ResourceFinder resourceFinder = new ResourceFinder();
		IProject project = resourceFinder.findProject(projectName);
		
		if (project == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		
		ProjectMapper projectMapper = new ProjectMapper();
		List<IAccount> projectRoles = projectMapper.getProjectMemberList(null, project);
		
		String entity = JSONEncoder.toProjectRoleJSONArray(projectName, projectRoles).toString();
		return Response.status(Response.Status.OK).entity(entity).build();
	}
}
