package api.dao;

import api.models.Account;
import org.apache.commons.dbutils.QueryRunner;

import java.sql.Connection;
import java.sql.SQLException;

public class AccountDao extends BasicDao<Account> {
    public AccountDao() {
        super(Account.class);
    }


}
