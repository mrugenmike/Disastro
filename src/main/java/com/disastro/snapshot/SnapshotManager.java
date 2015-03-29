package com.disastro.snapshot;

import com.disastro.recovery.PingService;
import com.disastro.recovery.ServiceInstanceFactory;
import com.disastro.recovery.SystemConfiguration;
import com.disastro.recovery.SystemConfigurationFactory;
import com.vmware.vim25.mo.*;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@DisallowConcurrentExecution
public class SnapshotManager implements Job {
    final SnapShotService snapShotService = SnapShotServiceFactory.instance();
    final ServiceInstance serviceInstance = ServiceInstanceFactory.instance();
    final SystemConfiguration conf = SystemConfigurationFactory.instance();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Folder rootFolder = serviceInstance.getRootFolder();
        InventoryNavigator navigator = new InventoryNavigator(rootFolder);
        PingService pingService = new PingService();

        try {
            ManagedEntity[] virtualMachines = navigator.searchManagedEntities("VirtualMachine");
            if(virtualMachines!=null){
                List<VirtualMachine> vms = Arrays.asList(virtualMachines).stream().map(vm -> (VirtualMachine) vm).collect(Collectors.toList());
                vms.forEach(vm->{
                    boolean reachable = pingService.ping(vm.getGuest().getIpAddress());
                    if(reachable){
                        try {
                            boolean production = conf.isProduction();
                            if(production){
                                snapShotService.createSnapshot(vm);
                            }else{
                                if(vm.getName().equals(conf.getTargetVM())){
                                    snapShotService.createSnapshot(vm);
                                }
                            }
                            //snapshot_task.waitForTask();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
