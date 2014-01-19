/* Glazed Lists                                                      (c) 2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
/*                                                          StarLight Systems */
package com.publicobject.misc.util.concurrent;

import java.util.List;
import java.util.ArrayList;

/**
 * A simple task queue for writing multithreaded tests.
 *
 * <p>This has special logic to maintain the thread interrupted state between
 * tasks. If the thread was interrupted at the end of one task, it will stay
 * interrupted at the start of the next task.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class JobQueue implements Runnable {

    /** the tasks to invoke, in order of invocation */
    private List<Job> jobs = new ArrayList<Job>();

    @Override
    public void run() {
        boolean interrupted = false;
        while(true) {
            // find a job to run, if any
            Job job = null;
            synchronized(this) {
                if(!jobs.isEmpty()) {
                    job = jobs.remove(0);
                } else {
                    try {
                        wait();
                    } catch(InterruptedException e) {
                        // interrupt the next execution
                        interrupted = true;
                    }
                    continue;
                }
            }

            // run the job
            try {
                if(interrupted) {
                    Thread.currentThread().interrupt();
                }
                job.runnable.run();
                interrupted = Thread.interrupted();
            } catch(Throwable t) {
                synchronized(job) {
                    job.thrown = t;
                }
            } finally {
                synchronized(job) {
                    job.done = true;
                    job.notify();
                }
            }
        }
    }

    /**
     * Invoke the specified job and return immediately.
     */
    public Job invokeLater(Runnable runnable) {
        // queue the job for execution
        Job job = new Job(runnable);
        synchronized(this) {
            jobs.add(job);
            notify();
        }
        return job;
    }

    /**
     * Invoke the specified job on the other thread and wait for it to complete.
     */
    public void invokeAndWait(Runnable runnable) {
        // queue the job for execution
        Job job = new Job(runnable);
        synchronized(this) {
            jobs.add(job);
            notify();
        }

        // wait for the job to complete
        synchronized(job) {
            while(!job.done) {
                try {
                    job.wait();
                } catch(InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        // if the job threw anything, throw that
        job.rethrowIfNecessary();
    }

    public void flush() {
        // flush the queue
        invokeAndWait(new NoOpRunnable());
    }

    private class NoOpRunnable implements Runnable {
        @Override
        public void run() {
            // do nothing
        }
    }


    /**
     * A task and its result.
     */
    public static class Job {
        private final Runnable runnable;
        private boolean done = false;
        private Throwable thrown = null;

        public Job(Runnable runnable) {
            this.runnable = runnable;
        }

        public void rethrowIfNecessary() {
            if(thrown == null) return;
            if(thrown instanceof RuntimeException) throw (RuntimeException)thrown;
            if(thrown instanceof Error) throw (Error)thrown;
            throw new IllegalStateException("Unexpected throwable " + thrown.getClass());
        }

        public Throwable getThrown() {
            return thrown;
        }
    }
}
