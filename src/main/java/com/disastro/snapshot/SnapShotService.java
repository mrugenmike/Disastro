package com.disastro.snapshot;

import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

import java.rmi.RemoteException;
import java.util.Date;

public class SnapShotService {
    private String SNAPHOT_PREFIX="snap-";

    public void createSnapshot(VirtualMachine vm) throws RemoteException, InterruptedException {
        String vmName = vm.getName().intern();
        System.out.println("Creating Snapshot of VM with Name "+vmName);
        String description = String.format("Took snapshot of VM with name %s at %s", vmName, new Date());
        boolean memorySnapshotsSupported = vm.getCapability().memorySnapshotsSupported;
        Task snapshot_task = vm.createSnapshot_Task(getSnapShotName(vm), description, memorySnapshotsSupported, true);
        snapshot_task.waitForTask();
    }

    public String getSnapShotName(VirtualMachine virtualMachine){
        return new StringBuilder().append(SNAPHOT_PREFIX).append(virtualMachine.getName()).toString();
    }
}
