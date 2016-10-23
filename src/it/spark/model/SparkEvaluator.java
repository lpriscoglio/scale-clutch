package it.spark.model;

import it.json.utils.JsonUtils;

public class SparkEvaluator {

	   public static boolean checkApplicationFinished(SparkApplication app)
	   {
		   return app.getAttempts()[0].isCompleted();
	   }
	
	   public static void testSparkFunctionality(String IP, String port)
	   {
		   
			//Ec2Utils.createInstance(singleEC2);
			try
			{
				//Ec2Utils.terminateInstance("i-044beba7263fe8807",singleEC2);
			}
			catch(Exception e)
			{
				System.out.println(e.getMessage());
			}
			JsonUtils remoteRESTParser = new JsonUtils();
			SparkApplication[] apps = remoteRESTParser.readApplications(IP+port);
			System.out.println("==== APPLICATIONS ACTIVE ===");
			for(SparkApplication e : apps)
			{
				System.out.println("ID: "+e.getId());
			}
			SparkExecutor[] ex = remoteRESTParser.readExecutors(IP+port,apps[0].getId());
			System.out.println("==== EXECUTORS ACTIVE ===");
			for(SparkExecutor e : ex)
			{
				System.out.println("ID: "+e.getId());
			}
			SparkJob[] jobs = remoteRESTParser.readJobs(IP+port,apps[0].getId());
			System.out.println("==== JOBS ACTIVE ===");
			for(SparkJob e : jobs)
			{
				System.out.println("ID: "+e.getJobId());
			}
			SparkStage[] stages = remoteRESTParser.readStages(IP+port,apps[0].getId());
			System.out.println("==== STAGES ACTIVE ===");
			for(SparkStage e : stages)
			{
				System.out.println("ID: "+e.getStageId());
			}
			SparkRDD[] rdds = remoteRESTParser.readRDD(IP+port,apps[0].getId());
			System.out.println("==== STAGES ACTIVE ===");
			for(SparkRDD e : rdds)
			{
				System.out.println("ID: "+e.getId());
			}
			SparkTaskSummary ts = remoteRESTParser.readMetrics(IP+port,apps[0].getId(),0,0);
			System.out.println("==== METRICS ACTIVE ===");
			System.out.println("First Quantile: "+ts.getQuantiles()[0]);
	   }
	   
	   
}
