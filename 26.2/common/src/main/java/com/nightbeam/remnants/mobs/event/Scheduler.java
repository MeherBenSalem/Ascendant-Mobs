package com.nightbeam.remnants.mobs.event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.IntPredicate;

/** Runs short delayed or repeating jobs on the server thread. Cleared when the server stops. */
public final class Scheduler {
    private static final List<Job> JOBS = new ArrayList<>();
    private static final List<Job> ADDED = new ArrayList<>();

    private Scheduler() {
    }

    /** Runs {@code job} every tick, passing the ticks left; it stops when they reach 0 or the job returns false. */
    public static void repeat(int ticks, IntPredicate job) {
        ADDED.add(new Job(ticks, job));
    }

    /** Runs {@code job} once after {@code ticks}. */
    public static void later(int ticks, Runnable job) {
        repeat(ticks, left -> {
            if (left == 0) {
                job.run();
            }
            return true;
        });
    }

    public static void tick() {
        JOBS.addAll(ADDED);
        ADDED.clear();
        Iterator<Job> it = JOBS.iterator();
        while (it.hasNext()) {
            Job job = it.next();
            job.left--;
            boolean keep;
            try {
                keep = job.task.test(job.left) && job.left > 0;
            } catch (RuntimeException e) {
                com.nightbeam.remnants.mobs.RemnantMobs.LOG.warn("Scheduled job failed", e);
                keep = false;
            }
            if (!keep) {
                it.remove();
            }
        }
    }

    public static void clear() {
        JOBS.clear();
        ADDED.clear();
    }

    private static final class Job {
        int left;
        final IntPredicate task;

        Job(int left, IntPredicate task) {
            this.left = left;
            this.task = task;
        }
    }
}
