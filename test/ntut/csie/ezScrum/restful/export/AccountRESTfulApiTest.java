package ntut.csie.ezScrum.restful.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

import ntut.csie.ezScrum.restful.export.jsonEnum.AccountJSONEnum;
import ntut.csie.ezScrum.test.CreateData.CreateAccount;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.ezScrumInfoConfig;
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
		assertEquals(2, jsonResponse.length());
		
		JSONObject accountJSON1 = jsonResponse.getJSONObject(1);
		IAccount account1 = mCA.getAccountList().get(1);
		assertEquals(account1.getID(), accountJSON1.getString(AccountJSONEnum.USERNAME));
		assertEquals(account1.getName(), accountJSON1.getString(AccountJSONEnum.NICK_NAME));
		assertEquals(account1.getPassword(), accountJSON1.getString(AccountJSONEnum.PASSWORD));
		assertEquals(account1.getEmail(), accountJSON1.getString(AccountJSONEnum.EMAIL));
		assertTrue(accountJSON1.getBoolean(AccountJSONEnum.ENABLE));
		
		JSONObject accountJSON2 = jsonResponse.getJSONObject(0);
		IAccount account2 = mCA.getAccountList().get(0);
		assertEquals(account2.getID(), accountJSON2.getString(AccountJSONEnum.USERNAME));
		assertEquals(account2.getName(), accountJSON2.getString(AccountJSONEnum.NICK_NAME));
		assertEquals(account2.getPassword(), accountJSON2.getString(AccountJSONEnum.PASSWORD));
		assertEquals(account2.getEmail(), accountJSON2.getString(AccountJSONEnum.EMAIL));
		assertTrue(accountJSON2.getBoolean(AccountJSONEnum.ENABLE));
	}
}
