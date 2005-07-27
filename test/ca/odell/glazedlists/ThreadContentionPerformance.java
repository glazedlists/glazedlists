/*
 * Copyright(c) 2005, NEXVU Technologies
 * All rights reserved.
 */
package ca.odell.glazedlists;

import ca.odell.glazedlists.util.concurrent.LockFactory;

import java.util.Random;


/**
 * This application tests thread contention issues with GlazedLists. It's intended to be
 * used for benchmarking the cores locks rather than being an exhaustive test of each
 * list.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public class ThreadContentionPerformance {
	public static void main(String[] args) {
		if (args.length != 1 && args.length != 6) {
			printUsage();
			return;
		}

		int writer_threads;
		long writer_min_pause_ms;
		long writer_max_pause_ms;
		int reader_threads;
		long reader_min_pause_ms;
		long reader_max_pause_ms;

		// Default settings
		if (args.length == 1) {
			if (!args[0].equalsIgnoreCase("default")) {
				printUsage();
				return;
			}

			writer_threads = 2;
			writer_min_pause_ms = 0;
			writer_max_pause_ms = 10;

			reader_threads = 20;
			reader_min_pause_ms = 0;
			reader_max_pause_ms = 0;
		} else {
			writer_threads = Integer.parseInt(args[0]);
			writer_min_pause_ms = Long.parseLong(args[1]);
			writer_max_pause_ms = Long.parseLong(args[2]);
			reader_threads = Integer.parseInt(args[3]);
			reader_min_pause_ms = Long.parseLong(args[4]);
			reader_max_pause_ms = Long.parseLong(args[5]);
		}

		// Setup
		EventList list = new BasicEventList();
		list.add("one");
		list.add("two");
		list.add("three");

		System.out.println("ReadWriteLock class: " +
			LockFactory.createReadWriteLock().getClass().getName());

		System.out.print("Starting threads...");

		final WriterThread[] writers = new WriterThread[ writer_threads ];
		for (int i = 0; i < writers.length; i++) {
			writers[i] = new WriterThread(list, writer_min_pause_ms, writer_max_pause_ms);
			writers[i].start();
		}

		final ReaderThread[] readers = new ReaderThread[ reader_threads ];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = new ReaderThread(list, reader_min_pause_ms, reader_max_pause_ms);
			readers[i].start();
		}
		System.out.println("done.");

		System.out.print("Waiting one minute...");
		try {
			Thread.sleep(60000);
		}
		catch (InterruptedException ex) {
		}
		System.out.println("done.");

		System.out.print("Stopping threads...");
		// Stop everything
		for (int i = 0; i < writers.length; i++)
			writers[i].interrupt();
		for (int i = 0; i < readers.length; i++)
			readers[i].interrupt();

		// Wait for them to stop
		try {
			for (int i = 0; i < writers.length; i++)
				writers[i].join();
			for (int i = 0; i < readers.length; i++)
				readers[i].join();
		}
		catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		System.out.println("done.");
		System.out.println("");

		long total_num_ops = 0;
		long total_time = 0;
		for (int i = 0; i < writers.length; i++) {
			total_num_ops += writers[i].getNumOps();
			total_time += writers[i].getTotalTime();
		}
		System.out.println("Total writes: " + total_num_ops);
		System.out.println("Total write time: " + total_time);


		total_num_ops = 0;
		total_time = 0;
		for (int i = 0; i < readers.length; i++) {
			total_num_ops += readers[i].getNumOps();
			total_time += readers[i].getTotalTime();
		}
		System.out.println("Total reads: " + total_num_ops);
		System.out.println("Total read time: " + total_time);

	}


	private static void printUsage() {
		System.out.println("Usage: ThreadContentionPerformance <num_writer_threads> " +
			"<writer_min_wait_ms> <writer_max_wait_ms> <num_reader_threads> " +
			"<reader_min_wait_ms> <reader_max_wait_ms>");
		System.out.println("   or  ThreadContentionPerformance default");
	}


	private static class ReaderThread extends Thread {
		private final EventList list;
		private final Random random;

		private final int wait_diff;
		private final long min_wait;

		private long total_time = 0;
		private long num_ops = 0;

		ReaderThread(EventList list, long min_wait, long max_wait) {
			super("ReaderThread");

			this.list = list;

			this.wait_diff = (int) (max_wait - min_wait);
			this.min_wait = min_wait;

			this.random = new Random();
		}


		synchronized long getTotalTime() {
			return total_time;
		}

		synchronized long getNumOps() {
			return num_ops;
		}

		public void run() {
			while (!isInterrupted()) {
				// Wait
				long sleep = min_wait + wait_diff == 0 ? 0 : random.nextInt(wait_diff);
				try {
					Thread.sleep(sleep);
				}
				catch (InterruptedException ex) {
					return;
				}

				// Read something
				long time = System.currentTimeMillis();
//				long time = System.nanoTime();
				list.getReadWriteLock().readLock().lock();
				try {
					int size = list.size();

					// Get a random object
					Object obj = list.get(random.nextInt(size));
				}
				finally {
					list.getReadWriteLock().readLock().unlock();

					total_time += System.currentTimeMillis() - time;
//					total_time += ( System.nanoTime() - time );
					num_ops++;
				}
			}
		}
	}


	private static class WriterThread extends Thread {
		private final EventList list;
		private final Random random;

		private final int wait_diff;
		private final long min_wait;

		private long total_time = 0;
		private long num_ops = 0;

		WriterThread(EventList list, long min_wait, long max_wait) {
			super("WriterThread");

			setPriority(Thread.NORM_PRIORITY + 2);

			this.list = list;

			this.wait_diff = (int) (max_wait - min_wait);
			this.min_wait = min_wait;

			this.random = new Random();
		}


		synchronized long getTotalTime() {
			return total_time;
		}

		synchronized long getNumOps() {
			return num_ops;
		}


		public void run() {
			while (!isInterrupted()) {
				// Wait
				long sleep = min_wait + wait_diff == 0 ? 0 : random.nextInt(wait_diff);

				try {
					Thread.sleep(sleep);
				}
				catch (InterruptedException ex) {
					return;
				}

				// Write something
				long time = System.currentTimeMillis();
//				long time = System.nanoTime();
				list.getReadWriteLock().writeLock().lock();
				try {
					list.add(new Long(System.currentTimeMillis()));
				}
				finally {
					list.getReadWriteLock().writeLock().unlock();

					total_time += System.currentTimeMillis() - time;
//					total_time += ( System.nanoTime() - time );
					num_ops++;
				}
			}
		}
	}
}
