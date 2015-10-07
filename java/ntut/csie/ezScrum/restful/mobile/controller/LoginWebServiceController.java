package ntut.csie.ezScrum.restful.mobile.controller;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import ntut.csie.ezScrum.restful.mobile.service.LoginWebService;
import ntut.csie.ezScrum.restful.mobile.support.InformationDecoder;
import ntut.csie.jcis.account.core.IAccount;
import ntut.csie.jcis.account.core.LogonException;

import com.google.gson.Gson;

@Path("/user")
public class LoginWebServiceController {
	private LoginWebService service = null;
	
	/***
	 * 取得帳號是否存在資訊
	 * http://IP:8080/ezScrum/web-service/user/login?userName={userName}&password={password}
	 * */
	@GET
	@Path("login")
	@Produces(MediaType.APPLICATION_JSON)
	public String login(@QueryParam("userName") String userName, @QueryParam("password") String password) {
		String response = "";
		try {
			InformationDecoder decodeInfo = new InformationDecoder();
			decodeInfo.decode(userName, password);
			this.service = new LoginWebService(decodeInfo.getDecodeUserName(), decodeInfo.getDecodePwd());
			IAccount theAccount = service.getAccount();
			Gson gson = new Gson();
			if (theAccount != null) {
				response = gson.toJson(Boolean.TRUE);
			} else {
				response = gson.toJson(Boolean.FALSE);
			}
		} catch (LogonException e) {
			System.out.println("class: LoginWebServiceController, method: login, exception: " + e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("class: LoginWebServiceController, method: login, exception: " + e.toString());
			e.printStackTrace();
		}
		return response;
	}
}
