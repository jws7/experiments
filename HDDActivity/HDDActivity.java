package Jobs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Random;
import java.util.UUID;

import Utilities.DatabaseConnection;

import DataTypes.Task;

/**
  * This code will write an random intriguer to a random place in one XX
  * files continuously.
  * 
  * Files should be greater than both the CPU cache size and the HDD cache.
  * In the case of Node20 - 12MB and 16MB respectively.
  * 
  * For this experiment files are 18MB in size. // Created using DD
  * @author jws7 - James Smith
  *
  */
public class HDDActivity extends Thread {

	private int NUM_OF_FILES = 10;
	private int SIZE_OF_FILES = 18; //MB
	private String FILE_LOCATION = "/home/jws7/DummyNode/";

	private long TIME = 1;
	
	private Task task;
	
	// array to hold the files
	private RandomAccessFile[] fileCollection;

	private DatabaseConnection db;
	
	public static void main(String[] args){
		
		System.out.println("Starting HDD ActivityTest\nCreating phony Task...");
		Task t = new Task();
		t.map.put("time", args[0]);
		//t.map.put("host", "00:22:19:6D:26:02"); //NODE23 
		t.map.put("host", "00:22:19:6D:07:B3"); //NODE20
		t.map.put("name", "disk");
	
		
		
		System.out.println("Launching task");
		Thread th1 = new Thread(new HDDActivity(t));
		th1.start();
		//Thread th2 = new Thread(new HDDActivity(t));
		//th2.start();
		// and wait for it to complete
		System.out.println("Waiting for it to join");
		try {
			th1.join();
			//th2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	// Constructor
	public HDDActivity(Task job) {
		
		this.task = job;
		this.db = new DatabaseConnection();

		System.out.println("Extracting parameters");
		// Store parameters
		if(job.map.containsKey("time"))
			TIME = (long) (Integer.parseInt(job.map.get("time")) * 60000);
		if(job.map.containsKey("files"))
			NUM_OF_FILES = Integer.parseInt(job.map.get("files"));
		if(job.map.containsKey("filesize"))
			SIZE_OF_FILES = Integer.parseInt(job.map.get("filesize"));
		if(job.map.containsKey("filelocation"))
			FILE_LOCATION = job.map.get("filelocation");
		
		fileCollection = new RandomAccessFile[NUM_OF_FILES];

		// Open the files (pre-created using DD)
		for (int i = 0; i < NUM_OF_FILES; i++) {
			System.out.println("Creating new file at: " + FILE_LOCATION 
					+ "file" + i + ".out");
			try {
				fileCollection[i] = new RandomAccessFile(FILE_LOCATION 
						+ "file" + i + ".out",
						"rw");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}	
	}

	public void run() {

		// Put task details in DB.
		UUID uuid = UUID.randomUUID();
		this.db.startTask(uuid, task.map.get("name"), task.map.get("host"));
		
		Random randomNumberGenerator = new Random();
		long start = System.currentTimeMillis();
		long current = System.currentTimeMillis();
		long length = current - start;
		
		while (length < TIME) {

			// Select random file
			int selc = randomNumberGenerator
					.nextInt(this.fileCollection.length);
			
			RandomAccessFile file = this.fileCollection[selc];

			try {
				if(file == null){
					
					System.err.println("File is null");
					System.exit(0);
					
				}
				// Jump to random place
				long range = file.length();
				long place = (long) (randomNumberGenerator.nextDouble()*range);
				file.seek(place);

				// write a random int.
				file.writeInt(randomNumberGenerator.nextInt());

				// Perform fsync !! CRITICAL - won't write changes to disk
				// otherwise
				file.getFD().sync();
				
				// Update counters
				current = System.currentTimeMillis();
				length = current - start;
				
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
		System.out.println("Job completed in " + length + "ms");
		this.db.endTask(uuid);
	}

}
