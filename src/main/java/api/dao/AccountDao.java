package api.dao;

import api.models.Account;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AccountDao {

    @Inject
    private DataSource dataSource;

    public Account getById(Long id) throws SQLException {
        Statement statement = dataSource.getConnection().createStatement();

        ResultSet resultSet = statement.executeQuery("select * from ACCOUNT where id = " + id);

        return resultSet.
    }
}
