package ntut.csie.ezScrum.restful.export;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ntut.csie.ezScrum.restful.export.support.JSONEncoder;
import ntut.csie.ezScrum.web.mapper.AccountMapper;
import ntut.csie.jcis.account.core.IAccount;
import ntut.csie.jcis.account.core.IActor;

@Path("accounts")
public class AccountRESTfulApi {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAccounts() {
		AccountMapper accountMapper = new AccountMapper();
		List<IActor> accountsActor = accountMapper.getAccountList();
		List<IAccount> accounts = new ArrayList<IAccount>();
		for (IActor actor : accountsActor) {
			accounts.add((IAccount) actor);
		}
		String entity = JSONEncoder.toAccountJSONArray(accounts).toString();
		return Response.status(Response.Status.OK).entity(entity).build();
	}
}
