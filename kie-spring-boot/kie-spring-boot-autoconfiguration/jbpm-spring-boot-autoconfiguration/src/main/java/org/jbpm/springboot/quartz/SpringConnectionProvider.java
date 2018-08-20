package org.jbpm.springboot.quartz;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.quartz.utils.ConnectionProvider;
import org.springframework.context.ApplicationContext;

public class SpringConnectionProvider implements ConnectionProvider {

    private static ApplicationContext applicationContext;

    private String dataSourceName;

    private Object datasource;

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (datasource instanceof XADataSource) {
            return (((XADataSource) datasource).getXAConnection().getConnection());
        } else if (datasource instanceof DataSource) {
            return ((DataSource) datasource).getConnection();
        } else {
            throw new RuntimeException("DataSource instance is not of expected type " + datasource.getClass());
        }
    }

    @Override
    public void shutdown() throws SQLException {
        // do nothing
    }

    @Override
    public void initialize() throws SQLException {
        this.datasource = applicationContext.getBean(dataSourceName);
    }

    public static void setApplicationContext(ApplicationContext appContext) {
        applicationContext = appContext;
    }
}
