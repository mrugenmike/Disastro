package com.disastro.recovery;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

public class SystemConfiguration {

    private final Properties properties;

    public SystemConfiguration(Properties properties) {
        this.properties = properties;
    }

    public String getVcenterIp(){
        return properties.getProperty("vcenter.url");
    }

    public String getUserName() {
        return properties.getProperty("vcenter.username");
    }

    public String getPassword() {
        return properties.getProperty("vcenter.password");
    }
    public String getVmName() {
        return properties.getProperty("vm.name");
    }
    public String getUserSwitchOffAlarmName(){return properties.getProperty("vm.alarm.user.userswitchoff");}

    public boolean isProduction() {
        return Boolean.parseBoolean(properties.getProperty("env.production"));
    }

    public String getTargetVM() {
        return properties.getProperty("vm.target.name");
    }

    public int getVMPingRetryAttempts() {
        return Integer.parseInt(properties.getProperty("vm.recovery.attempts"));
    }

    public long vmRetryIntervalInMillis() {
        return Long.valueOf(properties.getProperty("vm.recovery.retry.interval"));
    }
}
