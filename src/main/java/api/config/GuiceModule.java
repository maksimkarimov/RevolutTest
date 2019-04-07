package api.config;

import api.dao.AccountDao;
import api.dao.TransferDao;
import api.service.AccountService;
import api.service.TransferService;
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
        bind(AccountService.class).in(Singleton.class);
        bind(TransferService.class).in(Singleton.class);
    }

    @Provides
    public DataSource createDataSource() {
        final JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:revolut;MVCC=TRUE");
        dataSource.setUser("sa");
        dataSource.setPassword("sa");

        return dataSource;
    }
}
