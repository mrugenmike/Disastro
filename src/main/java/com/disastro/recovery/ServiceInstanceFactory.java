package com.disastro.recovery;

import com.vmware.vim25.mo.ServiceInstance;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

public class ServiceInstanceFactory {
    final static SystemConfiguration configuration = SystemConfigurationFactory.instance();
    public static ServiceInstance instance(){
        final String userName = configuration.getUserName();
        final String password = configuration.getPassword();
        final String vcenterIp = configuration.getVcenterIp();
        try {
            return new ServiceInstance(new URL(vcenterIp), userName, password,true);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
