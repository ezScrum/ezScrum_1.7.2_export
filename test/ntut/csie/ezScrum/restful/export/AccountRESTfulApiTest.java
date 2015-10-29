package ntut.csie.ezScrum.restful.export;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.net.httpserver.HttpServer;

import ntut.csie.ezScrum.test.CreateData.CopyProject;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.ezScrum.web.dataObject.UserInformation;
import ntut.csie.ezScrum.web.databaseEnum.AccountEnum;
import ntut.csie.ezScrum.web.helper.AccountHelper;
import ntut.csie.jcis.account.core.IAccount;

public class AccountRESTfulApiTest extends JerseyTest {
	private ezScrumInfoConfig mConfig = new ezScrumInfoConfig();
	private CreateProject mCP;
	private ResourceConfig mResourceConfig;
	private Client mClient;
	private HttpServer mHttpServer;
	private static String BASE_URL = "http://localhost:8080/ezScrum/resource/";
	private URI mBaseUri = URI.create(BASE_URL);

	@Override
	protected Application configure() {
		mResourceConfig = new ResourceConfig(AccountRESTfulApi.class);
		return mResourceConfig;
	}

	@Before
	public void setUp() {
		// 初始化 SQL
		InitialSQL ini = new InitialSQL(mConfig);
		ini.exe();

		// Create Project
		mCP = new CreateProject(1);
		mCP.exeCreate();

		// Start Server
		mHttpServer = JdkHttpServerFactory.createHttpServer(mBaseUri, mResourceConfig, true);

		// Create Client
		mClient = ClientBuilder.newClient();
	}

	@After
	public void tearDown() {
		// 初始化 SQL
		InitialSQL ini = new InitialSQL(mConfig);
		ini.exe();

		// 刪除測試檔案
		CopyProject copyProject = new CopyProject(mCP);
		copyProject.exeDelete_Project();

		// Stop Server
		mHttpServer.stop(0);

		// ============= release ==============
		ini = null;
		copyProject = null;
		mCP = null;
		mHttpServer = null;
		mClient = null;
	}
	
	@Test
	public void testGetAccounts() throws JSONException {
		// Test Data
		String userName = "TEST_USER_NAME_";
		String userRealName = "TEST_USER_REAL_NAME_";
		String password = "TEST_USER_PASSWORD_";
		String email = "TEST_USER_EMAIL_";
		String enable = "true";
		
		// Create Accounts
		AccountHelper accountHelper = new AccountHelper();
		// Account 1
		UserInformation userInformation = new UserInformation(userName + 1, userRealName + 1, password + 1, email + 1, enable);
		IAccount account1 = accountHelper.createAccount(userInformation, "user");
		// Account 2
		userInformation = new UserInformation(userName + 2, userRealName + 2, password + 2, email + 2, enable);
		IAccount account2 = accountHelper.createAccount(userInformation, "user");

		// Api Test
		// Call '/accounts' API
		Response response = mClient.target(mBaseUri)
		        .path("accounts")
		        .request()
		        .get();
		
		JSONArray jsonResponse = new JSONArray(response.readEntity(String.class));
		
		// Assert
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		assertEquals(3, jsonResponse.length());
		
		JSONObject accountJSON1 = jsonResponse.getJSONObject(0);
		assertEquals(account1.getID(), accountJSON1.getString(AccountEnum.USERNAME));
		assertEquals(account1.getName(), accountJSON1.getString(AccountEnum.NICK_NAME));
		assertEquals(account1.getPassword(), accountJSON1.getString(AccountEnum.PASSWORD));
		assertEquals(account1.getEmail(), accountJSON1.getString(AccountEnum.EMAIL));
		assertEquals(1, accountJSON1.getInt(AccountEnum.ENABLE));
		
		JSONObject accountJSON2 = jsonResponse.getJSONObject(1);
		assertEquals("admin", accountJSON2.getString(AccountEnum.USERNAME));
		assertEquals("admin", accountJSON2.getString(AccountEnum.NICK_NAME));
		assertEquals(1, accountJSON2.getInt(AccountEnum.ENABLE));
		
		JSONObject accountJSON3 = jsonResponse.getJSONObject(2);
		assertEquals(account2.getID(), accountJSON3.getString(AccountEnum.USERNAME));
		assertEquals(account2.getName(), accountJSON3.getString(AccountEnum.NICK_NAME));
		assertEquals(account2.getPassword(), accountJSON3.getString(AccountEnum.PASSWORD));
		assertEquals(account2.getEmail(), accountJSON3.getString(AccountEnum.EMAIL));
		assertEquals(1, accountJSON3.getInt(AccountEnum.ENABLE));
	}
}
