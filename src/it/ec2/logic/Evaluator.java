package it.ec2.logic;

import it.ec2.logic.Ec2Utils;
import it.ec2.model.Application;
import it.ec2.model.Machine;
import it.spark.model.SparkApplication;
import it.spark.model.SparkExecutor;
import it.spark.model.SparkEvaluator;
import it.spark.model.SparkJob;
import it.spark.model.SparkRDD;
import it.spark.model.SparkStage;
import it.spark.model.SparkTaskSummary;
import it.json.utils.JsonUtils;
import it.rest.utils.RestUtils;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;

public class Evaluator {
	
	private static final int minimumTasksCompletedThreshold = 30;
	private static final float minimumCoresToDownscaleThreshold = (float)0.85;
	private static final int minutesAfterCreationThreshold = 55;
	private static final int totalApplicationsToConsiderForHistory = 30;
	private static final int recentApplicationsToConsiderForHistory = 10;
	private static final int mediumApplicationsToConsiderForHistory = 20;
	private static final int defaultSparkConcurrentJobs = 2;
	private static final int estimationWordCountCores = 8;
	private static final int estimationSortCores = 8;
	private static final int estimationPageRankCores = 8;
	private static final int estimationKMeansCores = 6;
	private static final int minimumTasksCompletedPercThreshold = 97;
	private String historyPort = "18080";
	private String masterPort = "8080";
	private String executorPort = "4040";
	private int executorPortAsInt = 4040;
	private int [] hourlyJobsHistory;
	private int currentCores;
	private int currentMemory;
	private String IP;
	private int lastChecksNeedingUpScaling = 0;
	private ArrayList<Machine> machinesCreated;
	private static AmazonEC2 singleEC2 = null;
	
	public Evaluator()
	{
		this.currentCores = 0;
		this.currentMemory = 0;
		this.IP = "master-ip:"; // TO BE SET
		hourlyJobsHistory = new int [24];
		for(int i = 0; i < 24; i++)
		{
			hourlyJobsHistory[i] = 2;
		}
		machinesCreated = new ArrayList<Machine>();
		try
		{
			if(singleEC2 == null)
				singleEC2 = Ec2Utils.init();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

	public void begin() {

		//retrievePastApplicationMetrics();
		while(true)
		{
			checkScaling();
			try {
				TimeUnit.SECONDS.sleep(60);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	private void checkScaling()
	{
		int currentlyUsedCores = 0;
		int currentlyRequestedCores = 0;
		int currentlyUsedMemory = 0;
		int historyUsageOfCores = 0;
		ArrayList<Application> currentlyRunningApplications = new ArrayList<Application>();
		machinesCreated = Ec2Utils.checkClusterStatus(singleEC2, this);
		/*for(Machine m : machinesCreated)
		{
			System.out.println("Trovata aggiunta "+m.getID());
		}*/
        System.out.println("Spark has " + currentCores + " CPU cores available.");
        System.out.println("Spark has " + currentMemory + " GBs of memory available.");
        JsonUtils remoteRESTParser = new JsonUtils();
		//System.out.println("Requesting on "+IP+masterPort);
		SparkApplication[] appsOnMasterUI = remoteRESTParser.readApplications(IP+masterPort);
		for(SparkApplication e : appsOnMasterUI)
		{
			if((e.getAttempts()[0]!=null) && (!SparkEvaluator.checkApplicationFinished(e)))
			{
				
				//int completedTasksPerc = remoteRESTParser.readJobs(remoteRESTParser, IP+workerPort, e.getId());
				currentlyUsedCores += e.getCoresGranted();
				currentlyRequestedCores += e.getMaxCores()-e.getCoresGranted();
				currentlyUsedMemory += e.getMemoryPerExecutorMB();
				//Which port was the job started on?
				for(int i=0; i<8; i++){
					System.out.println("Requesting on "+IP+(executorPortAsInt+i));
					if(RestUtils.testEndpoint("http://"+IP+String.valueOf(executorPortAsInt+i)))
					{
						System.out.println("Testing Endpoint "+(executorPortAsInt+i)+" succeeded");
						SparkApplication[] appsOnExecutor = remoteRESTParser.readApplications(IP+String.valueOf(executorPortAsInt+i));
						if(appsOnExecutor[0].getId().equals(e.getId()))
						{
							System.out.println("Application "+e.getId()+" found on endpoint "+IP+String.valueOf(executorPortAsInt+i));
							currentlyRunningApplications.add(new Application(e.getId(),e.getName(),(executorPortAsInt+i)));
							System.out.println("Application "+e.getId()+" added to list with now "+currentlyRunningApplications.size()+" apps");
							executorPort = String.valueOf(executorPortAsInt+i);
							break;
						}
					}
				}
				SparkExecutor[] appsOnMasterUIExecutor = remoteRESTParser.readExecutors(IP+executorPort, e.getId());
				for(SparkExecutor exec : appsOnMasterUIExecutor)
				{
					for(Machine m : machinesCreated)
					{
						if(checkSparkIP(exec.getHostPort(),m.getIP()))
						{
							if(exec.getCompletedTasks() > minimumTasksCompletedThreshold)
							{
								m.setTerminateProtection(true);
								System.out.println("Executor "+m.getIP()+" is protected, "+exec.getCompletedTasks()+" tasks completed");
							}
							else
								System.out.println("Executor "+m.getIP()+" is not protected from terminations, "+exec.getCompletedTasks()+" tasks completed");
								
							m.addToUsedInApplications(e.getId());
							//System.out.println("Executor "+m.getIP()+" is running "+e.getId()+" currently");
						}
					}
				}
			}
		}
		System.out.println("Spark Applications currently uses "+currentlyUsedCores+" cores allocated and need "+currentlyRequestedCores+" more cores");
		
		//Use History to retrieve statistical usage
		SparkApplication [] appsOnHistoryUI = remoteRESTParser.readApplications(IP+historyPort);
		historyUsageOfCores = retrieveRecentHistoryRequestedCores(appsOnHistoryUI);
		System.out.println("History shows a statistical need of "+historyUsageOfCores+" cores per application");
		hourlyJobsHistory = evaluateJobsPerHour(appsOnHistoryUI);
		System.out.println("Numero di controlli precedenti che hanno richiesto scaling UP "+lastChecksNeedingUpScaling);
		
		//Do we need to scale Up?? 
		int necessaryCoresToScale = currentlyUsedCores + currentlyRequestedCores + defaultSparkConcurrentJobs*historyUsageOfCores;
		//Altrimenti, uso la predizione precedente. La disponibilità deve essere maggiore
		
		int currentHour = LocalDateTime.now().getHourOfDay();
		int currentMinute = LocalDateTime.now().getMinuteOfHour();
		int necessaryProactiveCoresToScale = hourlyJobsHistory[currentHour]*historyUsageOfCores;
		System.out.println("Dovrei allocare proattivamente "+necessaryProactiveCoresToScale+" core");
		
		for(Application app : currentlyRunningApplications)
		{
			SparkJob [] currentlyExecutingJobs = remoteRESTParser.readJobs(IP+app.getMappedPort(),app.getId());
			//System.out.println("Checking  application for jobs "+app.getType()+"");
			if(evalCompletedTasksInStagePerc(currentlyExecutingJobs) > minimumTasksCompletedPercThreshold)
			{
				necessaryCoresToScale -= coresByName(app.getType());
				System.out.println("Job has executed is almost ended, so it will not count, cores now"+necessaryCoresToScale);
			}
		}
		
		//Scrivo lo stato del sistema
		PrintWriter out = null;
		try {
		    out = new PrintWriter(new BufferedWriter(new FileWriter("writePath", true)));
		    out.println(currentHour+":"+currentMinute+","+machinesCreated.size()+","+currentCores+","+currentlyUsedCores+","+currentlyRequestedCores+
		    		","+historyUsageOfCores+","+defaultSparkConcurrentJobs+","+necessaryCoresToScale+","+necessaryProactiveCoresToScale+","+(float)necessaryCoresToScale/currentCores);
		}catch (IOException e) {
		    System.err.println(e);
		}finally{
		    if(out != null){
		        out.close();
		    }
		} 
		
		necessaryCoresToScale = Math.max(necessaryCoresToScale, necessaryProactiveCoresToScale);
		if(currentCores < necessaryCoresToScale || currentCores < necessaryProactiveCoresToScale)
		{
			necessaryCoresToScale = Math.max(necessaryCoresToScale, necessaryProactiveCoresToScale);
			System.out.println("System does NEED UPSCALE. Current Available Cores:"+currentCores+" All Necessary Cores:"+necessaryCoresToScale);
			//Which ones and how many
			int neededCores = necessaryCoresToScale-currentCores;
			System.out.println("Adding machines properly for "+neededCores+" cores");
			machinesCreated.addAll(Ec2Utils.createInstance(singleEC2,neededCores));
			lastChecksNeedingUpScaling++;
		}
		else
		{
			System.out.println("System does NOT NEED UPSCALE. Current Cores:"+currentCores+" Necessary Cores:"+necessaryCoresToScale);
			lastChecksNeedingUpScaling = 0;
			//No need to scale Up. Down?
			//Da controllare quanti task rimangono a questi esecutori , tagliare quelli con lavori iniziati da poco
			if(((float)necessaryCoresToScale/currentCores) < minimumCoresToDownscaleThreshold && !machinesCreated.isEmpty())
			{
				System.out.println("System does NEED DOWNSCALE. Current Machines:"+machinesCreated.size()+" Current Cores:"+currentCores+" Necessary Cores:"+necessaryCoresToScale);
				int surplusCores = currentCores-necessaryCoresToScale;
				System.out.println("Removing machines properly for "+surplusCores+" cores");
				ArrayList<Machine> terminationCandidates = evalNodesToTerminate(surplusCores,machinesCreated);
				System.out.println("System will try to remove a number of Machines:"+terminationCandidates.size()+" Surplus Cores:"+surplusCores+" Necessary Cores:"+necessaryCoresToScale);
				
				try 
				{
						for(Machine m : terminationCandidates)
						{
							//int completedTasks = evalCompletedTasksInStage(new JsonUtils(),IP+masterPort,applicationID,m.getIP());
							if(m.isTerminateProtection())
							{
								System.out.println("La macchina considerata è protetta "+m.getIP());
							}
							else
							{
								System.out.println("La macchina considerata NON è protetta "+m.getIP());
					    		System.out.println("Istanza che sta per essere terminata: "+ 
					    				" / Tipo: " + m.getType() + 
					    				" / IP: " + m.getIP() +
					    				" / ID: " + m.getID() +
					    				" / Gruppo: " + m.getGroupName());
								try {
									Ec2Utils.terminateInstance(m.getID(), singleEC2);
								} catch (InterruptedException e1) {
									System.out.println("Interrupted");
								}
							}
						}
				} catch (AmazonServiceException ase) {
					System.out.println("Caught Exception: " + ase.getMessage());
	                System.out.println("Reponse Status Code: " + ase.getStatusCode());
	                System.out.println("Error Code: " + ase.getErrorCode());
	                System.out.println("Request ID: " + ase.getRequestId());
				} catch (AmazonClientException cl) {
					System.out.println("Caught Exception: " + cl.getMessage());
				}
			}
			else
			{
				System.out.println("System does NOT NEED DOWNSCALE. Machines:"+machinesCreated.size()+" Current Cores:"+currentCores+" Necessary Cores:"+necessaryCoresToScale);
			}
		}
	}
	
	private boolean checkSparkIP(String sparkIP, String normalIP)
	{
		//ip-172-31-29-132.eu-central-1.compute.internal
		String parsed;
		if(sparkIP.contains("eu-central-1"))
		{
			String [] tokens = sparkIP.split("[.]+");
			String [] midRes = tokens[0].split("-");
			parsed = midRes[1]+"."+midRes[2]+"."+midRes[3]+"."+midRes[4];
		}
		else
		{
			parsed = sparkIP.split("[:]+")[0];
		}
		if(parsed.equals(normalIP))
			return true;
		else
			return false;
	}
	
	private int evalCompletedTasksInStagePerc(SparkJob [] jobs)
	{
		int totalTasks = 0;
		int completedTasks = 0;
		if(jobs == null)
			return 0;
		for(SparkJob j : jobs)
		{
			totalTasks += j.getNumTasks();
			completedTasks += j.getNumCompletedTasks();
		}
		double result = completedTasks/(double)totalTasks;
		result = Math.round(result*100.0)/100.0;
		return (int)(Math.round(result*100));
	}
	
	private ArrayList<Machine> evalNodesToTerminate(int surplusCores, ArrayList<Machine> machines)
	{
		ArrayList<Machine> toTerminate = new ArrayList<Machine>();
		for(Machine m : machines)
		{
			int remainingMinutes = (int)(System.currentTimeMillis()-m.getCreatedTime())/60000;
			if((remainingMinutes%60) > minutesAfterCreationThreshold)
			{
				System.out.println("Preparing to terminate machine with ID: "+m.getID()+" Minutes Elapsed: "+remainingMinutes);
				toTerminate.add(m);
			}
			else
				System.out.println("Not yet time to terminate machine with ID: "+m.getID()+" Minutes Elapsed: "+remainingMinutes);
		}
		return toTerminate;
	}
	
	private int [] evaluateJobsPerHour(SparkApplication [] apps)
	{
		int [] result = new int[24];
		for(int i = 0; i<24; i++)
		{
			result[i] = defaultSparkConcurrentJobs;
		}
		for(int i = 0; i< 100; i++)
		{
			LocalDateTime startJob = LocalDateTime.fromDateFields(evalTime(apps[i].getAttempts()[0].getStartTime()));
			//GMT Adjustment
			startJob.plusHours(2);
			result[startJob.getHourOfDay()]++;
		}
		for(int i = 0; i<24; i++)
		{
			result[i] = Math.max(defaultSparkConcurrentJobs,(int)Math.round(Math.sqrt((float)result[i])));
		}
		return result;
	}
	
	private int retrieveRecentHistoryRequestedCores(SparkApplication [] apps)
	{
		float cpuCores = 0;
		float candidateValue = 0;
		for(int i = 0;i<totalApplicationsToConsiderForHistory;i++)
		{
			candidateValue = coresByName(apps[i].getName());
			if(i<recentApplicationsToConsiderForHistory)
			{
				cpuCores += (candidateValue*(float)0.5);
			}
			else if(i<mediumApplicationsToConsiderForHistory)
			{
				cpuCores += (candidateValue*(float) 0.3);
			}
			else
			{
				cpuCores += (candidateValue*(float) 0.2);
			}
			//System.out.println("core aggiunti: "+cpuCores);
		}
		return Math.round(cpuCores/10);
	}
	
	private int coresByName(String type)
	{
		int candidate;
		switch(type)
		{
			case"WordCount": candidate=estimationWordCountCores;
				break;
			case"PageRank": candidate=estimationPageRankCores;
				break;
			case"Sort": candidate=estimationSortCores;
				break;
			case"SparkKMeans": candidate=estimationKMeansCores;
				break;
			case"WordCountBig": candidate=estimationWordCountCores*2;
				break;
			case"PageRankBig": candidate=estimationSortCores;
				break;
			case"SortBig": candidate=estimationPageRankCores;
				break;
			case"SparkKMeansBig": candidate=estimationKMeansCores*2;
				break;
			default: candidate=estimationWordCountCores;
				break;
		}
		return candidate;
	}

	private void retrievePastApplicationMetrics()
	{
		JsonUtils remoteRESTParser = new JsonUtils();
		SparkApplication[] apps = remoteRESTParser.readApplications(IP+historyPort);
		StringBuilder myBuilder = new StringBuilder();
		myBuilder.append("ID,name,coresGranted,memory,time,executorMinRuntime,executorFourthRuntime,"
				+ "executorThreeFourthsRuntime,executorMaxRuntime\n");
		for(int i = 0; i<500; i++)
		{
			if(apps[i].getAttempts()[0]!= null && SparkEvaluator.checkApplicationFinished(apps[i]) && 
					remoteRESTParser.readStages(IP+historyPort,apps[i].getId()).length > 0 && apps[i].getName().equals("SparkKMeans"))
			{
				myBuilder.append(apps[i].getId()+","+apps[i].getName()+","+apps[i].getCoresGranted()+
						","+apps[i].getMemoryPerExecutorMB()+",");
				myBuilder.append(evalTimeDifference(apps[i].getAttempts()[0].getStartTime(),apps[i].getAttempts()[0].getEndTime()));
				try
				{
					int theStage = 1;
					switch(apps[i].getName())
					{
					case "WordCount": theStage=0;
						break;
					case "PageRank": theStage=0;
						break;
					case "Sort": theStage=0;
						break;
					case "SparkKMeans": theStage=1;
						break;
					}
					SparkTaskSummary ts = remoteRESTParser.readMetrics(IP+historyPort,apps[i].getId(),theStage,0);
					myBuilder.append(","+toSeconds(ts.getExecutorRunTime()[0])+","+
							toSeconds(ts.getExecutorRunTime()[1])+","+
							toSeconds(ts.getExecutorRunTime()[2])+","+
							toSeconds(ts.getExecutorRunTime()[3])+"\n");
				}
				catch(RuntimeException myEc)
				{
					myBuilder.append("\n");
					//System.out.println("Skipping");
				}
			}
			System.out.println(i);
		}
		System.out.println("==============ENDED");
		try {
			PrintWriter writer = new PrintWriter("currentStats.txt", "UTF-8");
			writer.print(myBuilder);
			writer.close();
		} catch (FileNotFoundException e1) {
			System.out.println("File Not Found");
		} catch (UnsupportedEncodingException e1) {
			System.out.println("Unsupported Encoding");
		}
	}

	private String toSeconds(float inMillies)
	{
		return Float.toString(inMillies/(float)1000);
	}
	
	private Date evalTime(String begin)
	{
		Date beginDate;
		SimpleDateFormat myDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'GMT'");
		try {
			beginDate = myDate.parse(begin);
			return beginDate;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private String evalTimeDifference(String begin, String end)
	{
		// 2016-06-25T13:06:13.227GMT
		Date beginDate,endDate;
		SimpleDateFormat myDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'GMT'");
		try {
			beginDate = myDate.parse(begin);
			endDate = myDate.parse(end);
			long diffInMillies = endDate.getTime() - beginDate.getTime();
			float endRes = diffInMillies/(float)60000;
			return Float.toString(endRes);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "0";
	}


	public void setCurrentMemory(int mem)
	{
		this.currentMemory = mem;
	}

	public void setCurrentCores(int cores)
	{
		this.currentCores = cores;
	}

}
