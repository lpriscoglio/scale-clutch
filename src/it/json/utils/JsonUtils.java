package it.json.utils;

import com.google.gson.Gson;

import it.rest.utils.RestUtils;
import it.spark.model.SparkApplication;
import it.spark.model.SparkExecutor;
import it.spark.model.SparkJob;
import it.spark.model.SparkRDD;
import it.spark.model.SparkStage;
import it.spark.model.SparkTaskSummary;

public class JsonUtils {
	Gson g;
	
	public JsonUtils()
	{
		g = new Gson();
	}
	
	public SparkExecutor[] readExecutors(String host, String app)
	{
		String input = RestUtils.restRequest("http://"+host+"/api/v1/applications/"+app+"/executors");
		return g.fromJson(input, SparkExecutor[].class);
	}
	
	public SparkApplication[] readApplications(String host)
	{
		SparkApplication[] result;
		String input = RestUtils.restRequest("http://"+host+"/api/v1/applications/");
		if(!input.contains("NotFoundOnPort"))
			 result = g.fromJson(input, SparkApplication[].class);
		else 
			result = null;
		return result;
	}
	
	public SparkJob[] readJobs(String host, String app)
	{
		String input = RestUtils.restRequest("http://"+host+"/api/v1/applications/"+app+"/jobs");
		return g.fromJson(input, SparkJob[].class);
	}
	
	public SparkRDD[] readRDD(String host, String app)
	{
		String input = RestUtils.restRequest("http://"+host+"/api/v1/applications/"+app+"/storage/rdd");
		SparkRDD[] result = g.fromJson(input, SparkRDD[].class);
		return result;
	}
	
	public SparkStage[] readStages(String host, String app)
	{
		String input = RestUtils.restRequest("http://"+host+"/api/v1/applications/"+app+"/stages");
		SparkStage[] result = g.fromJson(input, SparkStage[].class);
		return result;
	}
	
	public SparkTaskSummary readMetrics(String host, String app, int stageID, int stageAttemptID)
	{
		String input = RestUtils.restRequest("http://"+host+"/api/v1/applications/"+app+"/stages/"
				+stageID+"/"+stageAttemptID+"/taskSummary");
		SparkTaskSummary result = g.fromJson(input, SparkTaskSummary.class);
		return result;
	}
}
