package Jobs;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import Utilities.DatabaseConnection;

import DataTypes.Task;

public class CPULoad extends Thread {
	
	public static void main(String[] args){
	
		System.out.println("Starting HDD CPULoad\nCreating phony Task...");
		Task t = new Task();
	
		t.map.put("time", args[0]);
		//t.map.put("host", "00:22:19:6D:26:02"); //NODE23 
		t.map.put("host", "00:22:19:6D:07:B3"); //NODE20
		t.map.put("name", "load");
		t.map.put("percentage", "75");
	
		System.out.println("Launching task");
		Thread th1 = new CPULoad(t);	
	}

	private Task task;
	private DatabaseConnection db;

	public CPULoad(Task job) {
		
		this.task = job;
		this.db = new DatabaseConnection();
		
		// Put task details in DB.
		UUID uuid = UUID.randomUUID();
		this.db.startTask(uuid, task.map.get("name"), task.map.get("host"));
		
		long start = System.currentTimeMillis();
	
		
		System.out.println("Creating a new CPULoad task ");
		
		if(job.map.containsKey("time")){
			
			int time = Integer.parseInt(job.map.get("time"));
			System.out.println("Should run for " + time + " minutes...");
			
			if(job.map.containsKey("percentage")){
				
				int percent = Integer.parseInt(job.map.get("percentage"));
				System.out.println("...at " + percent + " percent");
				
				// RUN JOB		
				int n = 8; // Number of threads
				System.out.println("Start " + n + " threads for " + time + " minute at " + percent + "%");
				System.out.println("Start time: " + now());

				CPUThread[] threads = new CPUThread[n];

				for (int i = 0; i < n; i++) {
					// Create threads and run
					threads[i] = new CPUThread(time, percent);
					threads[i].start();
				}

				// Wait for completion
				
				try {
					threads[n - 1].join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				long end = System.currentTimeMillis();
				long length = end - start;
				System.out.println("Job completed in " + length + "ms");
				this.db.endTask(uuid);
			}
			else{
				System.out.println("No percentage...");
			}
		}
		else{
			System.out.println("No time...");
		}
	}

	public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

	public static String now() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());

	}
}