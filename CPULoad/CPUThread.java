package Jobs;

public class CPUThread extends Thread {

	// Percentage to hold this thread
	private int percentageToHold;

	// Time to run for
	private long timeToRun;

	public CPUThread(int minutes, int percent) {
		this.percentageToHold = percent;
		this.timeToRun = (long) minutes * 60000;
	}

	public void run() {

		long start = System.currentTimeMillis();
		long current = System.currentTimeMillis();
		long length = current - start;

		while (length < timeToRun) {

			long time = System.currentTimeMillis();

			time += percentageToHold;

			while (System.currentTimeMillis() < time) {
				// Do nothing... just consume time;
			}
			try {
				Thread.sleep(100 - percentageToHold);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Update counters
			current = System.currentTimeMillis();
			length = current - start;
		}
		//System.out.println("Job took " + length + " milliseconds");
	}
}
