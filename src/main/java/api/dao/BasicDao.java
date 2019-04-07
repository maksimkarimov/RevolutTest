package api.dao;

import com.google.inject.Inject;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BasicDao<T> {

    private static final String FIND_BY_CLASS_AND_ID = "SELECT * FROM %s where id = ?";
    private static final String FIND_ALL = "SELECT * FROM %s";
    private static final String DELETE_BY_ID = "DELETE FROM %s where id = ?";
    private static final String SELECT_FROM_BY_ID_FOR_UPDATE = "select * from %s where id = ? FOR UPDATE";


    @Inject
    protected DataSource dataSource;

    private final Class<T> entityClass;

    public BasicDao(final Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public T getById(final Long id) throws SQLException {
        final QueryRunner run = new QueryRunner();

        final String sql = String.format(FIND_BY_CLASS_AND_ID, entityClass.getSimpleName());
        return run.query(dataSource.getConnection(), sql, new BeanHandler<>(entityClass), id);
    }

    public List<T> getAll() throws SQLException {
        final QueryRunner run = new QueryRunner();

        final String sql = String.format(FIND_ALL, entityClass.getSimpleName());
        return run.query(dataSource.getConnection(), sql, new BeanListHandler<>(entityClass));
    }

    abstract Long save(T entity) throws SQLException;

    abstract T update(T entity) throws SQLException;

    public Integer delete(Long id) throws SQLException {
        final QueryRunner run = new QueryRunner();

        final String sql = String.format(DELETE_BY_ID, entityClass.getSimpleName());
        return run.execute(dataSource.getConnection(), sql, id);
    }

    protected String getSelectFromByIdForUpdateQuery() {
        return String.format(SELECT_FROM_BY_ID_FOR_UPDATE, entityClass.getSimpleName());
    }

    protected String getSelectFromByIdForUpdateQuery(Class clazz) {
        return String.format(SELECT_FROM_BY_ID_FOR_UPDATE, clazz.getSimpleName());
    }


/*    private String createInsertQuery(T entity) {
        Class<?> aClass = entity.getClass();
        final Field[] fields = aClass.getDeclaredFields();
        Map<String, Object> filedNameValue = Stream.of(fields)
                .peek(f -> f.setAccessible(true))
                .filter(f -> !"id".equalsIgnoreCase(f.getName()))
                .collect(Collectors.toMap(Field::getName, f -> {
                    try {
                        return f.get(entity);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    return null;
                }));

        filedNameValue.keySet().stream().reduce()


        return String.format("INSERT INTO %d(name, balance) VALUES(%d, %d)");
    }*/
}
