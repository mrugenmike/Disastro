package com.disastro.recovery;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

public class SystemConfigurationFactory {
    private static AtomicReference<SystemConfiguration> instance = new AtomicReference<SystemConfiguration>();
    static{
        InputStream resourceAsStream = SystemConfiguration.class.getResourceAsStream("/vm.properties");
        Properties properties = new Properties();
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        instance.compareAndSet(null,new SystemConfiguration(properties));
    }
    public static synchronized SystemConfiguration instance() {
        return instance.get();
    }
}
