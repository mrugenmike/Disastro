package com.disastro.recovery;


import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.mo.*;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@DisallowConcurrentExecution
public class RecoverySystem implements Job {

    final PingService pinger = new PingService();
    final SystemConfiguration conf = SystemConfigurationFactory.instance();
    final AlarmService alarmService = new AlarmService();
    private VmRecoveryStrategy vmRecoveryStrategy = new VmRecoveryStrategy();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        //step1: find all Ips of the VM
        ServiceInstance serviceInstance = null;
        try {
            serviceInstance = new ServiceInstance(new URL(conf.getVcenterIp()), conf.getUserName(), conf.getPassword(), true);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Folder rootFolder = serviceInstance.getRootFolder();
        InventoryNavigator inventoryNavigator = new InventoryNavigator(rootFolder);
        ManagedEntity[] hostSystems = null;
        try {
            hostSystems = inventoryNavigator.searchManagedEntities("HostSystem");
            if (hostSystems != null && hostSystems.length > 0) {
                List<HostSystem> hosts = Arrays.asList(hostSystems).stream().map(hs -> (HostSystem) hs).collect(Collectors.toList());
                hosts.forEach(host -> {
                    //check given host is alive
                    String hostIp = host.getName();
                    boolean hostAlive = pinger.ping(hostIp);
                    try {
                        //check each VM for liveliness
                        if(hostAlive){
                            VirtualMachine[] virtualMachinesEntities = host.getVms();
                            if(virtualMachinesEntities!=null && virtualMachinesEntities.length>0){
                                List<VirtualMachine> virtualMachines = Arrays.asList(virtualMachinesEntities).stream().map(me -> ((VirtualMachine) me)).collect(Collectors.toList());
                                virtualMachines.forEach(vm -> {
                                    String vmIP = vm.getGuest().getIpAddress();
                                    System.out.println(String.format("Checking VM with IP %s and Vhost with IP %s", vmIP, hostIp));
                                    boolean vmreachable = pinger.ping(vmIP);
                                    if (!vmreachable) {
                                        // check if alarm is triggered and user has switched the VM off
                                        System.out.println("VM with name: "+vm.getName()+" found to be  not reachable retrying now");
                                        boolean userAlarmTriggered = alarmService.checkIfUserSwitchOffAlarmDecalredISet(vm, conf.getUserSwitchOffAlarmName());
                                        if (userAlarmTriggered) {
                                            System.out.println("User Switch off Event triggered for VM for VM with name: " + vm.getGuest().getHostName());
                                        } else {
                                            // restart VM if VM is found dead after configurable amount of attempts and retry interval
                                            int retryAttempts = conf.getVMPingRetryAttempts();
                                            boolean isVMReachable = false;
                                            while(retryAttempts>0){
                                                try {
                                                    Thread.sleep(conf.vmRetryIntervalInMillis());
                                                    isVMReachable = pinger.ping(vmIP);
                                                    if(!isVMReachable){
                                                        retryAttempts-=1;
                                                    }
                                                    else{
                                                        break;
                                                    }
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            if(retryAttempts==0 && !isVMReachable){
                                                vmRecoveryStrategy.recoverVM(host,vm);
                                            }
                                        }
                                    }
                                });
                            }

                        }else{
                            System.out.println("Host with IP "+hostIp+" found to be not reachable");
                            //do something
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (InvalidProperty invalidProperty) {
            invalidProperty.printStackTrace();
        } catch (RuntimeFault runtimeFault) {
            runtimeFault.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}