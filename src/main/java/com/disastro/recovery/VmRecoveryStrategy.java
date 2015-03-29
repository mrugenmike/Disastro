package com.disastro.recovery;

import com.disastro.snapshot.SnapShotNotFoundException;
import com.disastro.snapshot.SnapShotService;
import com.disastro.snapshot.SnapShotServiceFactory;
import com.vmware.vim25.*;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.VirtualMachine;
import com.vmware.vim25.mo.VirtualMachineSnapshot;

import java.rmi.RemoteException;

public class VmRecoveryStrategy {
    final SnapShotService snapShotService = SnapShotServiceFactory.instance();

    public void recoverVM(HostSystem host, VirtualMachine vm) {
        try {
            final VirtualMachineSnapshot snapShot = getSnapshot(vm, snapShotService.getSnapShotName(vm));
            if(snapShot!=null){
                snapShot.revertToSnapshot_Task(host);
            }else{
                System.out.println(String.format("No snapshot found for VM with name %s"+vm.getName()));
            }
        } catch (SnapShotNotFoundException e) {
            e.printStackTrace();
        } catch (FileFault fileFault) {
            fileFault.printStackTrace();
        } catch (TaskInProgress taskInProgress) {
            taskInProgress.printStackTrace();
        } catch (RuntimeFault runtimeFault) {
            runtimeFault.printStackTrace();
        } catch (VmConfigFault vmConfigFault) {
            vmConfigFault.printStackTrace();
        } catch (InsufficientResourcesFault insufficientResourcesFault) {
            insufficientResourcesFault.printStackTrace();
        } catch (InvalidState invalidState) {
            invalidState.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private VirtualMachineSnapshot getSnapshot(VirtualMachine vm,
                                       String snapshotname) throws SnapShotNotFoundException {
        if (vm == null || snapshotname == null)
        {
            return null;
        }
        final VirtualMachineSnapshotInfo snapshot = vm.getSnapshot();
        VirtualMachineSnapshotTree[] snapTree = null;
        if(snapshot!=null)
            snapTree= snapshot.getRootSnapshotList();
        
        if(snapTree!=null)
        {
            ManagedObjectReference mor = findSnapshotInTree(
                    snapTree, snapshotname);
            if(mor!=null)
            {
                return new VirtualMachineSnapshot(
                        vm.getServerConnection(), mor);
            }
        }
        throw new SnapShotNotFoundException("Snapshot with name "+snapshotname+" not found");
    }

    private  ManagedObjectReference findSnapshotInTree(
            VirtualMachineSnapshotTree[] snapTree, String snapshotname) {
        for(int i=0; i <snapTree.length; i++)
        {
            VirtualMachineSnapshotTree node = snapTree[i];
            if(snapshotname.equals(node.getName()))
            {
                return node.getSnapshot();
            }
            else
            {
                VirtualMachineSnapshotTree[] childTree =
                        node.getChildSnapshotList();
                if(childTree!=null)
                {
                    ManagedObjectReference mor = findSnapshotInTree(
                            childTree, snapshotname);
                    if(mor!=null)
                    {
                        return mor;
                    }
                }
            }
        }
        return null;

    }

}
