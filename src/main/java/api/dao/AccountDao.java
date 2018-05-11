package api.dao;

import api.models.Account;

public class AccountDao extends BasicDao<Account> {
    public AccountDao() {
        super(Account.class);
    }
}
