package ntut.csie.ezScrum.restful.mobile.controller;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import ntut.csie.ezScrum.restful.mobile.service.ProductBacklogWebService;
import ntut.csie.ezScrum.restful.mobile.support.InformationDecoder;
import ntut.csie.jcis.account.core.LogonException;

@Path("{projectID}/product-backlog/")
public class ProductBacklogWebServiceController {
	private ProductBacklogWebService pbws;
	/**
	 * 取得專案底下所有Story Get
	 * http://IP:8080/ezScrum/web-service/{projectID}/product-backlog/storylist?userName={userName}&password={password}
	 **/
	@GET
	@Path("storylist")
	@Produces(MediaType.APPLICATION_JSON)
	public String getProductBacklogList(@QueryParam("userName") String username,
										@QueryParam("password") String password,
										@PathParam("projectID") String projectID) {
		String jsonString = "";
		try {
			InformationDecoder decodeAccount = new InformationDecoder();
			decodeAccount.decode(username, password, projectID);
			this.pbws = new ProductBacklogWebService(
					decodeAccount.getDecodeUserName(),
					decodeAccount.getDecodePwd(), 
					decodeAccount.getDecodeProjectID());
			this.pbws.readStory(null);
			jsonString = this.pbws.getRESTFulResponseString();
		} catch (LogonException e) {
			System.out.println("class: ProductBacklogWebServiceController, " +
								"method: getProductBacklogList, " +
								"exception: "+ e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("class: ProductBacklogWebServiceController, " +
								"method: decode, " +
								"exception: "+ e.toString());
			e.printStackTrace();
		}
		return jsonString;
	}
	/**	刪除Story	DELETE
	 * 	http://IP:8080/ezScrum/web-service/{projectID}/product-backlog/storylist/{storyID}?userName={userName}&password={password}
	 * **/
	@DELETE
	@Path("storylist/{storyID}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String deleteStory(@QueryParam("userName") String username,
							  @QueryParam("password") String password,
							  @PathParam("projectID") String projectID,
							  @PathParam("storyID") String storyID){
		String responseString = "";
		try{
			InformationDecoder decodeAccount = new InformationDecoder();
			decodeAccount.decode(username, password, projectID);
//			this.pbws = new ProductBacklogWebService(
//					decodeAccount.getDecodeUserName(),
//					decodeAccount.getDecodePwd(), 
//					decodeAccount.getDecodeProjectID(), null);
			this.pbws = new ProductBacklogWebService(
					decodeAccount.getDecodeUserName(),
					decodeAccount.getDecodePwd(), 
					decodeAccount.getDecodeProjectID());
			this.pbws.deleteStory(storyID);
			responseString += this.pbws.getRESTFulResponseString();
		} catch (LogonException e) {
			responseString += "LogonException";
			System.out.println("class: ProductBacklogWebServiceController, " +
								"method: deleteStory, " +
								"exception: "+ e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			responseString += "IOException";
			System.out.println("class: ProductBacklogWebServiceController, " +
								"method: deleteStory, " +
								"api:InformationDecoder, " +
								"exception: "+ e.toString());
			e.printStackTrace();
		}
		return responseString;
	}
	
	/**
	 * 取得專案底下指定Story Get
	 * http://IP:8080/ezScrum/web-service/{projectID}/product-backlog/storylist/{storyID}?userName={userName}&password={password}
	 **/
	@GET
	@Path("storylist/{storyID}")
	@Produces(MediaType.APPLICATION_JSON)
	public String retrieveStory(@QueryParam("userName") String username, 
								@QueryParam("password") String password, 
								@PathParam("projectID") String projectID, 
								@PathParam("storyID") String storyID) {
		String jsonString = "";
		try {
			InformationDecoder decodeAccount = new InformationDecoder();
			decodeAccount.decode(username, password, projectID);
			this.pbws = new ProductBacklogWebService(
					decodeAccount.getDecodeUserName(),
					decodeAccount.getDecodePwd(), 
					decodeAccount.getDecodeProjectID());
			this.pbws.readStoryByID(storyID);
			jsonString = this.pbws.getRESTFulResponseString();
		} catch (LogonException e) {
			System.out.println("class: ProductBacklogWebServiceController, " +
								"method: getProductBacklogList, " +
								"exception: "+ e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("class: ProductBacklogWebServiceController, " +
								"method: decode, " +
								"exception: "+ e.toString());
			e.printStackTrace();
		}
		return jsonString;
	}
	
	/**
	 * 取得專案底下所有的tag
	 * http://IP:8080/ezScrum/web-service/{projectID}/product-backlog/taglist?userName={userName}&password={password}
	 */
	@GET
	@Path("taglist")
	@Produces(MediaType.APPLICATION_JSON)
	public String getTagList(@PathParam("projectID") String projectID,
							 @QueryParam("userName") String userName,
							 @QueryParam("password") String password) {
		String jsonString = "";
		InformationDecoder informationDecoder = new InformationDecoder();
		try {
			informationDecoder.decode(userName, password, projectID);
			this.pbws = new ProductBacklogWebService(informationDecoder.getDecodeUserName(),
													 informationDecoder.getDecodePwd(),
													 informationDecoder.getDecodeProjectID());
			this.pbws.readAllTags();
			jsonString = this.pbws.getRESTFulResponseString();
		} catch (IOException e) {
			System.out.println("class: ProductBacklogWebServiceController, " +
								"method: decode, " +
								"exception: "+ e.toString());
			e.printStackTrace();
		} catch (LogonException e) {
			System.out.println("class: ProductBacklogWebServiceController, " +
								"method: getTagList, " +
								"exception: "+ e.toString());
			e.printStackTrace();
		}
		return jsonString;
	}
	
	/**
	 * 取得專案底下Story History
	 * http://IP:8080/ezScrum/web-service/{projectID}/product-backlog/{storyID}/history?userName={userName}&password={password}
	 */
	@GET
	@Path("{storyID}/history")
	@Produces(MediaType.APPLICATION_JSON)
	public String getStoryHistory(@PathParam("projectID") String projectID,
								  @QueryParam("userName") String userName,
								  @QueryParam("password") String password,
								  @PathParam("storyID") String storyID) {
		String storyHistoryJsonString = "";
		InformationDecoder informationDecoder = new InformationDecoder();
		try {
			informationDecoder.decode(userName, password, projectID);
			this.pbws = new ProductBacklogWebService(informationDecoder.getDecodeUserName(),
													 informationDecoder.getDecodePwd(),
													 informationDecoder.getDecodeProjectID());
			this.pbws.readStoryHistory(storyID);
			storyHistoryJsonString = this.pbws.getRESTFulResponseString();
		} catch (IOException e) {
			System.out.println("class: ProductBacklogWebServiceController, " +
								"method: decode, " +
								"exception: "+ e.toString());
			e.printStackTrace();
		} catch (LogonException e) {
			System.out.println("class: ProductBacklogWebServiceController, " +
								"method: getStoryHistory, " +
								"exception: "+ e.toString());
			e.printStackTrace();
		}
		return storyHistoryJsonString;
	}
}
