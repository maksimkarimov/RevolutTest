package api.dao;

import com.google.inject.Inject;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

public class BasicDao<T> {

    @Inject
    protected DataSource dataSource;

    protected Class<T> entityClass;

    public BasicDao(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public T getById(Long id) throws SQLException {
        QueryRunner run = new QueryRunner();

        String sql = "SELECT * FROM " + entityClass.getSimpleName() + " where id = ?";
        return run.query(dataSource.getConnection(), sql, new BeanHandler<>(entityClass), id);
    }

    public List<T> getAll() throws SQLException {
        QueryRunner run = new QueryRunner();

        String sql = "SELECT * FROM " + entityClass.getSimpleName();
        return run.query(dataSource.getConnection(), sql, new BeanListHandler<>(entityClass));
    }
}
