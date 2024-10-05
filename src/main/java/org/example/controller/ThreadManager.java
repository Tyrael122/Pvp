package org.example.controller;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.*;

@Slf4j
public class ThreadManager {

    private static ScheduledFuture<?> endMatchTask;

    public static void scheduleNewThreadToEndMatch(Runnable runnable, LocalDateTime nextMatchEndTime) {
        if (endMatchTask != null && !endMatchTask.isDone()) {
            endMatchTask.cancel(true);
        }

        try (ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1)) {
            Runnable task = wrapRunnableWithTryCatch(runnable, "Ending matches automatically");

            endMatchTask = scheduler.schedule(task, getDelay(nextMatchEndTime), TimeUnit.SECONDS);
        }
    }

    public static void scheduleNewThreadToStartMatchPeriodically(Runnable runnable) {
        log.info("Scheduling new thread to start matches periodically.");

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable task = wrapRunnableWithTryCatch(runnable, "Trying to form matches automatically");

        scheduler.scheduleWithFixedDelay(task, 10L, 3L, TimeUnit.SECONDS);
    }

    private static Runnable wrapRunnableWithTryCatch(Runnable runnable, String taskDescription) {
        return () -> {
            try {
                log.info("{} at {}.", taskDescription, LocalDateTime.now());

                runnable.run();

                log.info("{} finished at {}.", taskDescription, LocalDateTime.now());
            } catch (Exception e) {
                log.error("Error while {} at {}.", taskDescription, LocalDateTime.now());
                log.error("Error details: ", e);
            }
        };
    }

    private static long getDelay(LocalDateTime nextMatchEndTime) {
        ZonedDateTime zdt = nextMatchEndTime.atZone(ZoneId.systemDefault());
        return zdt.toEpochSecond() - ZonedDateTime.now().toEpochSecond();
    }
}
