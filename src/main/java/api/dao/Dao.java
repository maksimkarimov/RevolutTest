package api.dao;

import com.sun.deploy.util.ReflectionUtil;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class Dao<T> {

    @Inject
    protected DataSource dataSource;

    public T getById(Long id) throws SQLException {
        Statement statement = dataSource.getConnection().createStatement();
        statement.executeQuery("select * from "+this.getClass().getGenericSuperclass().getTypeName()+ " where id = " + id);

        return statement.getResultSet().getObject()
    }
}
