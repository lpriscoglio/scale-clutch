package it.spark.model;

public class SparkStage {

	private String status;
	private int stageId;
	private int attemptId;
	private int numActiveTasks;
	private int numCompleteTasks;
	private int numFailedTasks;
	private int executorRunTime;
	private long inputBytes;
	private int inputRecords;
	private long outputBytes;
	private int outputRecords;
	private long shuffleReadBytes;
	private int shuffleReadRecords;
	private long shuffleWriteBytes;
	private long shuffleWriteRecords;
	private long memoryBytesSpilled;
	private long diskBytesSpilled;
	private String name;
	private String details;
	private String schedulingPool;
	public String getStatus() {
		return status;
	}
	public int getStageId() {
		return stageId;
	}
	public int getAttemptId() {
		return attemptId;
	}
	public int getNumActiveTasks() {
		return numActiveTasks;
	}
	public int getNumCompleteTasks() {
		return numCompleteTasks;
	}
	public int getNumFailedTasks() {
		return numFailedTasks;
	}
	public int getExecutorRunTime() {
		return executorRunTime;
	}
	public long getInputBytes() {
		return inputBytes;
	}
	public int getInputRecords() {
		return inputRecords;
	}
	public long getOutputBytes() {
		return outputBytes;
	}
	public int getOutputRecords() {
		return outputRecords;
	}
	public long getShuffleReadBytes() {
		return shuffleReadBytes;
	}
	public int getShuffleReadRecords() {
		return shuffleReadRecords;
	}
	public long getShuffleWriteBytes() {
		return shuffleWriteBytes;
	}
	public long getShuffleWriteRecords() {
		return shuffleWriteRecords;
	}
	public long getMemoryBytesSpilled() {
		return memoryBytesSpilled;
	}
	public long getDiskBytesSpilled() {
		return diskBytesSpilled;
	}
	public String getName() {
		return name;
	}
	public String getDetails() {
		return details;
	}
	public String getSchedulingPool() {
		return schedulingPool;
	}
}
