package it.spark.model;

public class SparkExecutor {
	private String id;
	private String hostPort;
	private int rddBlocks;
	private int memoryUsed;
	private int diskUsed;
	private int activeTasks;
	private int failedTasks;
	private int completedTasks;
	private int totalTasks;
	private int totalDuration;
	private long totalInputBytes;
	private long totalShuffleRead;
	private long totalShuffleWrite;
	private long maxMemory;
	
	public String getId()
	{ return this.id; }
	
	public String getHostPort()
	{ return this.hostPort; }

	public int getRddBlocks() {
		return rddBlocks;
	}

	public int getMemoryUsed() {
		return memoryUsed;
	}

	public int getDiskUsed() {
		return diskUsed;
	}

	public int getActiveTasks() {
		return activeTasks;
	}

	public int getFailedTasks() {
		return failedTasks;
	}

	public int getCompletedTasks() {
		return completedTasks;
	}

	public int getTotalTasks() {
		return totalTasks;
	}

	public int getTotalDuration() {
		return totalDuration;
	}

	public long getTotalInputBytes() {
		return totalInputBytes;
	}

	public long getTotalShuffleRead() {
		return totalShuffleRead;
	}

	public long getTotalShuffleWrite() {
		return totalShuffleWrite;
	}

	public long getMaxMemory() {
		return maxMemory;
	}
}
