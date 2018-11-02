package org.kie.spring.jbpm.tools;

import java.util.Properties;

import org.kie.test.util.db.DataSourceFactory;
import org.kie.test.util.db.PoolingDataSourceWrapper;

public class SpringDataSourceFactory {

    private String xaClassName;
    private String uniqueName;
    private String driverClassName;
    private Properties driverProperties;

    public PoolingDataSourceWrapper setupPoolingDataSource() {
        driverProperties.put("driverClassName", driverClassName);
        driverProperties.put("className", xaClassName);

        return DataSourceFactory.setupPoolingDataSource(uniqueName, driverProperties);
    }

    public void setXaClassName(final String xaClassName) {
        this.xaClassName = xaClassName;
    }

    public void setUniqueName(final String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public void setDriverClassName(final String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public void setDriverProperties(final Properties driverProperties) {
        this.driverProperties = driverProperties;
    }
}
