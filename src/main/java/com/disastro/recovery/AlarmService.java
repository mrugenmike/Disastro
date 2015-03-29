package com.disastro.recovery;

import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.mo.AlarmManager;
import com.vmware.vim25.mo.VirtualMachine;

import java.util.Arrays;

public class AlarmService {
    public boolean checkIfUserSwitchOffAlarmDecalredISet(VirtualMachine vm, String alarmName) {
        VirtualMachineRuntimeInfo runtime = vm.getRuntime();
        if (runtime.getPowerState().equals(VirtualMachinePowerState.poweredOff)) {
            // Assuming At least 1 alarm is declared
            long userSwitchedOffAlarmCount = Arrays.asList(vm.getDeclaredAlarmState()).stream().filter(alarmState -> alarmState.getKey().equalsIgnoreCase(alarmName) && alarmState.overallStatus.equals("red")).count();
            if (userSwitchedOffAlarmCount > 0) {
                return true;
            }
        }
        return false;
    }
}
