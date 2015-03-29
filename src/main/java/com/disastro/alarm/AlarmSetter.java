package com.disastro.alarm;

import com.disastro.recovery.PingService;
import com.disastro.recovery.ServiceInstanceFactory;
import com.disastro.recovery.SystemConfiguration;
import com.disastro.recovery.SystemConfigurationFactory;
import com.vmware.vim25.*;
import com.vmware.vim25.mo.*;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AlarmSetter implements Runnable {
    private SystemConfiguration conf = SystemConfigurationFactory.instance();
    PingService pinger = new PingService();
    @Override
    public void run() {
        try {
            ServiceInstance serviceInstance = ServiceInstanceFactory.instance();
            String userSwitchOffAlarmName = conf.getUserSwitchOffAlarmName();
            AlarmManager alarmManager = serviceInstance.getAlarmManager();
            Folder rootFolder = serviceInstance.getRootFolder();
            InventoryNavigator inventoryNavigator = new InventoryNavigator(rootFolder);

            VirtualMachine virtualMachine =(VirtualMachine) inventoryNavigator.searchManagedEntity("VirtualMachine", "T09-VM01-Ubu-Mru");
                    VirtualMachineRuntimeInfo runtime = virtualMachine.getRuntime();
                    String ipAddress = virtualMachine.getGuest().getIpAddress();
                    if(pinger.ping(ipAddress)){
                        AlarmSpec alarmSpec = new AlarmSpec();
                        AlarmExpression expression = getAlarmExpression();
                        AlarmSetting as = new AlarmSetting();
                        as.setReportingFrequency(0); //as often as possible
                        as.setToleranceRange(0);

                        try {
                            alarmSpec.setSetting(as);
                            alarmSpec.setEnabled(true);
                            alarmSpec.setExpression(expression);
                            alarmSpec.setName(conf.getUserSwitchOffAlarmName());
                            alarmManager.createAlarm(virtualMachine,alarmSpec);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private AlarmTriggeringAction getAlarmTriggerAction(Action action){
        AlarmTriggeringAction alarmAction =
                new AlarmTriggeringAction();
        alarmAction.setYellow2red(true);
        alarmAction.setAction(action);
        return alarmAction;
    }
    private AlarmExpression getAlarmExpression() {
        StateAlarmExpression expression = new StateAlarmExpression();
        expression.setType("VirtualMachine");
        expression.setStatePath("runtime.powerState");
        expression.setOperator(StateAlarmOperator.isEqual);
        expression.setRed("poweredOff");
        return expression;
    }

    private MethodAction powerOnAction(){
        MethodAction action = new MethodAction();
        action.setName("PowerOnVM_Task");
        MethodActionArgument argument = new MethodActionArgument();
        argument.setValue(null);
        action.setArgument(new MethodActionArgument[] { argument });
        return action;
    }
}
