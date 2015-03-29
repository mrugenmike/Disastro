package com.disastro;
import com.disastro.recovery.RecoverySystem;
import com.disastro.recovery.ServiceInstanceFactory;
import com.disastro.snapshot.SnapshotManager;
import com.vmware.vim25.mo.*;
import org.quartz.*;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import scala.concurrent.duration.Duration;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DisasterRecoveryManager {

    public static void main(String[] args) throws MalformedURLException, RemoteException {
        /*ActorSystem alarmSetterSystem = ActorSystem.create("AlarmSetter");
        alarmSetterSystem.scheduler().scheduleOnce(Duration.apply(0, TimeUnit.SECONDS), new AlarmSetter(), alarmSetterSystem.dispatcher());*/

        try {
            //snapshot job
            JobDetail snapShotJob = JobBuilder.newJob(SnapshotManager.class).withDescription("Snapshot Job").build();
            final Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, 120);
            final Date startTime = calendar.getTime();
            SimpleTrigger snapShotTrigger = TriggerBuilder.newTrigger().withDescription("Trigger for SnapShot").withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(10).repeatForever()).startAt(startTime).build();

            //recovery job
            JobDetail recoveryJob = JobBuilder.newJob(RecoverySystem.class).withDescription("Recovery Job").build();
            SimpleTrigger recoveryTrigger = TriggerBuilder.newTrigger().withDescription("Trigger for Recovery Job").withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(1).repeatForever()).build();

            //schedule jobs
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            scheduler.scheduleJob(snapShotJob, snapShotTrigger);
            scheduler.scheduleJob(recoveryJob, recoveryTrigger);

        } catch (SchedulerException se) {
            se.printStackTrace();
        }
        //snapshotSystem.scheduler().schedule(Duration.apply(0, TimeUnit.MINUTES), Duration.apply(5, TimeUnit.MINUTES), snapshotManager, snapshotSystem.dispatcher());

      /*  ActorSystem recoverySystem = ActorSystem.create("RecoverySystem");
        recoverySystem.scheduler().schedule(Duration.apply(0,TimeUnit.SECONDS),Duration.apply(10,TimeUnit.SECONDS),new RecoverySystem(),recoverySystem.dispatcher());*/
    }
}
