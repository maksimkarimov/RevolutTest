package api.config;

import api.dao.AccountDao;
import api.dao.TransferDao;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;

public class GuiceModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AccountDao.class).in(Singleton.class);
        bind(TransferDao.class).in(Singleton.class);
    }

    @Provides
   public DataSource createDataSource() {
        DataSource dataSource = new JdbcDataSource();
        ((JdbcDataSource) dataSource).setURL("jdbc:h2:mem:revolut;MVCC=TRUE");
        ((JdbcDataSource) dataSource).setUser("sa");
        ((JdbcDataSource) dataSource).setPassword("sa");


        return dataSource;
    }
}
