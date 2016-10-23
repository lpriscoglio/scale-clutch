package it.spark.model;

public class SparkJob {
	private String jobId;
	private String name;
	private String submissionTime;
	private int [] stageIds;
	private String status;
	private int numTasks;
	private int numActiveTasks;
	private int numCompletedTasks;
	private int numSkippedTasks;
	private int numFailedTasks;
	private int numActiveStages;
	private int numCompletedStages;
	private int numSkippedStages;
	private int numFailedStages;
	public String getJobId() {
		return jobId;
	}
	public String getName() {
		return name;
	}
	public String getSubmissionTime() {
		return submissionTime;
	}
	public int[] getStageIds() {
		return stageIds;
	}
	public String getStatus() {
		return status;
	}
	public int getNumTasks() {
		return numTasks;
	}
	public int getNumActiveTasks() {
		return numActiveTasks;
	}
	public int getNumCompletedTasks() {
		return numCompletedTasks;
	}
	public int getNumSkippedTasks() {
		return numSkippedTasks;
	}
	public int getNumFailedTasks() {
		return numFailedTasks;
	}
	public int getNumActiveStages() {
		return numActiveStages;
	}
	public int getNumCompletedStages() {
		return numCompletedStages;
	}
	public int getNumSkippedStages() {
		return numSkippedStages;
	}
	public int getNumFailedStages() {
		return numFailedStages;
	}
}
