package net.IneiTsuki.regen.magic.core.scheduler;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TickScheduler {

    private static final Logger LOGGER = Logger.getLogger(TickScheduler.class.getName());

    private static class ScheduledTask {
        int ticksLeft;
        final Runnable task;

        ScheduledTask(int ticksLeft, Runnable task) {
            this.ticksLeft = ticksLeft;
            this.task = task;
        }
    }

    private static final List<ScheduledTask> scheduledTasks = new LinkedList<>();

    /**
     * Call this once every server tick to update and execute delayed tasks.
     */
    public static void tick() {
        if (scheduledTasks.isEmpty()) {
            return;
        }

        for (int i = scheduledTasks.size() - 1; i >= 0; i--) {
            ScheduledTask scheduledTask = scheduledTasks.get(i);
            scheduledTask.ticksLeft--;

            if (scheduledTask.ticksLeft <= 0) {
                LOGGER.info("Executing scheduled task...");
                try {
                    scheduledTask.task.run();
                    LOGGER.info("Scheduled task executed successfully.");
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error executing scheduled task", e);
                }
                scheduledTasks.remove(i);
                LOGGER.info("Scheduled task removed from queue.");
            }
        }
    }

    /**
     * Schedules a task to run after a delay in ticks.
     */
    public static void schedule(int delayTicks, Runnable task) {
        if (delayTicks <= 0) {
            LOGGER.info("Executing immediate task...");
            try {
                task.run();
                LOGGER.info("Immediate task executed successfully.");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error executing immediate task", e);
            }
        } else {
            scheduledTasks.add(new ScheduledTask(delayTicks, task));
            LOGGER.info("Scheduled new task to run in " + delayTicks + " ticks. Total scheduled: " + scheduledTasks.size());
        }
    }

    /**
     * Returns the number of currently scheduled tasks.
     */
    public static int getScheduledTaskCount() {
        return scheduledTasks.size();
    }

    /**
     * Clears all scheduled tasks. Use with caution!
     */
    public static void clearAllTasks() {
        LOGGER.warning("Clearing all scheduled tasks! Count: " + scheduledTasks.size());
        scheduledTasks.clear();
    }
}
