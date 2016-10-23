package it.spark.model;

public class SparkTaskSummary {
	private float [] quantiles;
	private float [] executorDeserializeTime;
	private float [] executorRunTime;
	private float [] resultSize;
	private float [] jvmGcTime;
	private float [] resultSerializationTime;
	private float [] memoryBytesSpilled;
	private float [] diskBytesSpilled;
	public float[] getQuantiles() {
		return quantiles;
	}
	public float[] getExecutorDeserializeTime() {
		return executorDeserializeTime;
	}
	public float[] getExecutorRunTime() {
		return executorRunTime;
	}
	public float[] getResultSize() {
		return resultSize;
	}
	public float[] getJvmGcTime() {
		return jvmGcTime;
	}
	public float[] getResultSerializationTime() {
		return resultSerializationTime;
	}
	public float[] getMemoryBytesSpilled() {
		return memoryBytesSpilled;
	}
	public float[] getDiskBytesSpilled() {
		return diskBytesSpilled;
	}
	
	/* Missing metrics */
}
