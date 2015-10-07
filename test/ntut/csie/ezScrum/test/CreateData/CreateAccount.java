package ntut.csie.ezScrum.test.CreateData;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ntut.csie.ezScrum.web.support.TranslateUtil;
import ntut.csie.jcis.account.core.AccountFactory;
import ntut.csie.jcis.account.core.IAccount;
import ntut.csie.jcis.account.core.IAccountManager;

public class CreateAccount {
	private static Log log = LogFactory.getLog(CreateRelease.class);

	private int AccountCount = 0;
	private String Account_ID = "TEST_ACCOUNT_ID_";
	private String Account_NAME = "TEST_ACCOUNT_REALNAME_";
	private String Account_PWD = "TEST_ACCOUNT_PWD_";
	private String Account_Mail = "TEST_ACCOUNT_MAIL_";

	private List<IAccount> AccountList;

	// private ezScrumInfoConfig ezScrumInfo = new ezScrumInfoConfig();

	public CreateAccount(int ACcount) {
		this.AccountCount = ACcount;
		this.AccountList = new ArrayList<IAccount>();
	}

	/**
	 * 自動產生建構時給的 count 個數
	 */
	public void exe() {
		IAccountManager am = AccountFactory.getManager();
		String roles = "user";
		for (int i = 0; i < this.AccountCount; i++) {
			String ID = Integer.toString(i + 1);
			String Acc_ID = this.Account_ID + ID;
			String Acc_RLNAME = this.Account_NAME + ID;
			String Acc_PWD = this.Account_PWD + ID;
			String Acc_Mail = this.Account_Mail + ID;

			IAccount account = AccountFactory.createAccount(Acc_ID, Acc_RLNAME, Acc_PWD, true);
			account.setEmail(Acc_Mail);

			am.addAccount(account);
			account = am.getAccount(Acc_ID);
			List<String> roleList = TranslateUtil.translateRoleString(roles);

			// 確認已成功加入Assign roles
			if (account != null) {
				am.updateAccountRoles(Acc_ID, roleList);
				am.save();
			}

			this.AccountList.add(account);
			log.info("Create " + this.AccountCount + " accounts success.");
		}
	}

	/**
	 * return ID = TEST_ACCOUNT_ID_X
	 */
	public String getAccount_ID(int i) {
		return (this.Account_ID + Integer.toString(i));
	}

	/**
	 * return Name = TEST_ACCOUNT_NAME_X
	 */
	public String getAccount_RealName(int i) {
		return (this.Account_NAME + Integer.toString(i));
	}

	/**
	 * return PWD = TEST_ACCOUNT_PWD_X
	 */
	public String getAccount_PWD(int i) {
		return (this.Account_PWD + Integer.toString(i));
	}

	/**
	 * return MAIL = TEST_ACCOUNT_MAIL_
	 */
	public String getAccount_Mail(int i) {
		return (this.Account_Mail + Integer.toString(i));
	}

	/**
	 * return the added account Object
	 */
	public List<IAccount> getAccountList() {
		return this.AccountList;
	}

	/**
	 * return Account counts
	 */
	public int getAccountCount() {
		return this.AccountCount;
	}

	/**
	 * reset Account Name through accountId
	 * @param accountId
	 */
	public void setAccount_RealName(int accountId) {
		this.AccountList.get(accountId - 1).setName(this.Account_NAME + "NEW_" + Integer.toString(accountId));
	}
}
