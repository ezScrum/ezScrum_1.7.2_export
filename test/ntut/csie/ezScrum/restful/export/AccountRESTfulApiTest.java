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

import ntut.csie.ezScrum.test.CreateData.CreateAccount;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
import ntut.csie.ezScrum.web.databaseEnum.AccountEnum;
import ntut.csie.jcis.account.core.IAccount;

public class AccountRESTfulApiTest extends JerseyTest {
	private ezScrumInfoConfig mConfig = new ezScrumInfoConfig();
	private ResourceConfig mResourceConfig;
	private Client mClient;
	private CreateAccount mCA;
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

		// Create Account
		mCA = new CreateAccount(2);
		mCA.exe();
		
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

		// Stop Server
		mHttpServer.stop(0);

		// ============= release ==============
		ini = null;
		mCA = null;
		mHttpServer = null;
		mClient = null;
	}
	
	@Test
	public void testGetAccounts() throws JSONException {
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
		assertEquals("admin", accountJSON1.getString(AccountEnum.USERNAME));
		assertEquals("admin", accountJSON1.getString(AccountEnum.NICK_NAME));
		assertEquals(1, accountJSON1.getInt(AccountEnum.ENABLE));
		
		JSONObject accountJSON2 = jsonResponse.getJSONObject(1);
		IAccount account1 = mCA.getAccountList().get(0);
		assertEquals(account1.getID(), accountJSON2.getString(AccountEnum.USERNAME));
		assertEquals(account1.getName(), accountJSON2.getString(AccountEnum.NICK_NAME));
		assertEquals(account1.getPassword(), accountJSON2.getString(AccountEnum.PASSWORD));
		assertEquals(account1.getEmail(), accountJSON2.getString(AccountEnum.EMAIL));
		assertEquals(1, accountJSON2.getInt(AccountEnum.ENABLE));
		
		JSONObject accountJSON3 = jsonResponse.getJSONObject(2);
		IAccount account2 = mCA.getAccountList().get(1);
		assertEquals(account2.getID(), accountJSON3.getString(AccountEnum.USERNAME));
		assertEquals(account2.getName(), accountJSON3.getString(AccountEnum.NICK_NAME));
		assertEquals(account2.getPassword(), accountJSON3.getString(AccountEnum.PASSWORD));
		assertEquals(account2.getEmail(), accountJSON3.getString(AccountEnum.EMAIL));
		assertEquals(1, accountJSON3.getInt(AccountEnum.ENABLE));
	}
}
