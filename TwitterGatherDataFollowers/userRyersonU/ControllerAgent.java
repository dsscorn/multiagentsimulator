package TwitterGatherDataFollowers.userRyersonU;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;

import jade.lang.acl.*;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import jade.content.*;
import jade.content.onto.basic.*;
import jade.content.lang.*;
import jade.content.lang.sl.*;
import jade.core.*;
import jade.core.Runtime;
import jade.core.behaviours.*;
import jade.domain.*;
import jade.domain.mobility.*;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.JADEAgentManagement.*;
import jade.gui.*;
import jade.domain.FIPAAgentManagement.*;

public class ControllerAgent extends GuiAgent {
	//public class ControllerAgent extends Agent {

	private static final long serialVersionUID = 1L;
	private AgentContainer workContainer;

	private AID[] allRecAgents;
	private int tweetDelay;

	private int totalUsers = 0;
	private ArrayList<String> usersRec = new ArrayList<String>(); //Users looking for recommendation
	private ArrayList<String> listOfUsers = new ArrayList<String>(); //List of users from dataset
	private ArrayList<String> listOfAgents = new ArrayList<String>(); //List of all agents created
	private ArrayList<AgentController> listOfAgentControllers = new ArrayList<AgentController>(); //List of all agents AID created

	private static String serverName = "127.0.0.1";
	private static String portNumber = "3306";
	private static String sid = "testmysql";	 

	private Connection con;
	private Statement stmt = null;
	private ResultSet resultSet = null;
	private static String user = "root";
	private static String pass = "Asdf1234";

	public static final int QUIT = 0;
	public static final int START_SIM = 1;
	public static final int INITIALIZE = 2;
	public static final int GET_USERS = 3;
	public static final int FROM_DB = 1;
	public static final int FROM_TEXT = 0;
	public static final int COS_SIM = 0;
	public static final int K_MEANS = 1;
	private static final int HASH_TAGS = 1;
	private static final int RE_TWEETS = 1;
	private static final int STOP_WORDS = 1;

	private AID agentNameAID;
	private String agentName;

	private int algorithmRec;
	private int readFrom;
	private int totalTweetLimit;

	private String referenceUser;
	private String beginDate;
	private String endDate;	

	private Runtime runTime;

	transient protected ControllerAgentGui myGui;

	AgentController agentController = null;

	private boolean initialized;
	private boolean firstRun;

	private InMemoryDb localDb;
	private InMemoryDb availableDb;

	private File textFile;

	protected void setup() {

		//Get access to current jade runtime system
		runTime = jade.core.Runtime.instance();

		getContentManager().registerLanguage(new SLCodec(),FIPANames.ContentLanguage.FIPA_SL0);
		getContentManager().registerOntology(MobilityOntology.getInstance());

		setUpWorkContainer();

		myGui = new ControllerAgentGui(this);
		myGui.setVisible(true);

		agentName = getLocalName();
		agentNameAID = getAID();

		initialized = false;
		firstRun = true;
		readFrom = FROM_TEXT;
		textFile = null;
		//readFrom = FROM_DB;
		localDb = new InMemoryDb();
		availableDb = new InMemoryDb();

		System.out.println("ControllerAGENT MADE agentName:" + agentName + " agentNameAID: "+agentNameAID);


		try {
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			ServiceDescription sd = new ServiceDescription();
			sd.setName("Distributed Recommender System");
			sd.setType("Controller Agent");
			dfd.addServices(sd);
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}


	}





	protected void onGuiEvent(GuiEvent ev) {
		int actionToPerform;
		actionToPerform = ev.getType();
		System.out.println("actionToPerform: "+actionToPerform);
		if (actionToPerform == QUIT)
		{
			System.exit(0);
		}
		else if (actionToPerform == GET_USERS)
		{
			loadUsers(ev);
		}
		else if (actionToPerform == INITIALIZE)
		{
			if (initialized)
			{
				killContainer();
				setUpWorkContainer();
			}
			initializeSetup(ev);
		}
		else if (actionToPerform == START_SIM)
		{
			startSimulation();
		}
	}

	private void setUpWorkContainer()
	{
		System.out.println("Set up work container");
		try {
			Profile mainProfile = new ProfileImpl();
			//mainProfile.setParameter(CONTAINER_NAME, "workContainer");
			workContainer = runTime.createAgentContainer(mainProfile);

		}
		catch (Exception e) { e.printStackTrace(); }
	}

	public void loadUsers(GuiEvent ev)
	{
		localDb.clearDb();
		availableDb.clearDb();

		referenceUser = (String) ev.getParameter(0);
		totalTweetLimit = (Integer)ev.getParameter(1); //DO NOT SET < 300	
		beginDate = (String) ev.getParameter(2);
		endDate = (String) ev.getParameter(3);	

		if (readFrom == FROM_TEXT)
		{
			try
			{
				RandomAccessFile file = new RandomAccessFile(textFile, "r");
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Date beginDateFormat = null;
				try {
					beginDateFormat = sdf.parse(beginDate);
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Date endDateFormat = null;
				try {
					endDateFormat = sdf.parse(endDate);
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}			
				String line = "";
				String[] info;
				long seekPosition = 0;
				boolean canAddTweet = false;
				ArrayList<String> tweetParts = new ArrayList<String>();
				Date currentDateFormat = null;
				long tweetId = 0;
				String tweetText = "";
				String currentUserName = "";


				line = file.readLine();
				seekPosition = file.getFilePointer();

				while(line != null)
				{
					tweetParts.clear();
					int tabsCount = 0;

					info = line.split("\t",6);
					/*for (int i = 0; i < info.length; i++)
				{
					System.out.print(i+": "+info[i]+" ");
				}
				System.println();
					 */
					tweetId = Long.valueOf(info[1]);
					currentDateFormat = sdf.parse(info[2]);
					currentUserName = info[4];
					tweetText = info[5];

					if (beginDateFormat.before(currentDateFormat) && endDateFormat.after(currentDateFormat))
						canAddTweet = true;
					else
						canAddTweet = false;

					//Check if nextline is proper format or part of previous tweet with newline(s) in it
					line = file.readLine();

					//If the nextline is null, add the current line to the db
					if (line == null)
					{
						//System.out.println(tweetText);
						Tweet currentTweet = new Tweet(tweetText,tweetId,currentDateFormat,currentUserName);
						if (canAddTweet)
							localDb.addTweet(currentTweet);
						
						break;
					}


					tabsCount = countTabs(line.toCharArray());

					//Proper format with tabs in the tweet text
					if(tabsCount >= 5)
					{
						file.seek(seekPosition);
					}
					//Not proper format so it is text from the previous tweet with newlines in it
					else
					{
						do
						{
							seekPosition = file.getFilePointer();
							//System.out.println("seekPosition: "+seekPosition + " line: "+line);
							tweetParts.add(line);
							line = file.readLine();
							if (line != null)
								tabsCount = countTabs(line.toCharArray());
							else
								break;
						} while (tabsCount < 5);

						for (String partText : tweetParts)
						{
							tweetText = tweetText + partText;
						}

						file.seek(seekPosition);
					}

					Tweet currentTweet = new Tweet(tweetText,tweetId,currentDateFormat,currentUserName);

					if (canAddTweet)
						localDb.addTweet(currentTweet);

					//System.out.println("tabs: "+tabsCount+" position: "+ file.getFilePointer()+" text: "+tweetText);
					//System.out.println(tweetText);
					line=file.readLine();
					seekPosition = file.getFilePointer();
				}

				file.close();

			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (ParseException e)
			{
				e.printStackTrace();
			}		

			ArrayList<Tweet> tweetsFromDb = localDb.getTweets();
			ArrayList<Tweet> datasetAvailable = new ArrayList<Tweet>();

			Collections.sort(tweetsFromDb);

			int tweetToLimitCount = 0;
			String prevUserName = "";
			listOfUsers.clear();

			for (int i = tweetsFromDb.size()-1; i >= 0; i--)
			{
				tweetToLimitCount++;
				//System.out.println("tweetToLimitCount: "+tweetToLimitCount+" totalTweetLimit: "+totalTweetLimit);
				String currentUser;
				Tweet currentTweet = tweetsFromDb.get(i);
				//System.out.println(currentTweet.getTweetId()+" "+currentTweet.getUser()+" "+currentTweet.getTweetText());


				datasetAvailable.add(currentTweet);
				currentUser = currentTweet.getUser();

				if (!currentUser.equals(prevUserName))
				{
					if (!listOfUsers.contains(currentUser))
						listOfUsers.add(currentUser);
					prevUserName = currentUser;
				}
				if (tweetToLimitCount == totalTweetLimit)
					break;
			}

			availableDb.setTweets(datasetAvailable);

			myGui.updateList(listOfUsers);

		}

		if (readFrom == FROM_DB)
		{				 
			listOfUsers.clear();

			try {

				String url = "jdbc:mysql://" + serverName + ":" + portNumber + "/" + sid + "?useSSL=false";
				con = DriverManager.getConnection(url, user, pass);

				String query;

				if (totalTweetLimit > 0)
					query = "select screen_name,referenceUser,count(*) from (select * from usertweet where referenceUser='"+referenceUser+"' AND CAST(created_at AS DATE) BETWEEN '" + beginDate + "' AND '" + endDate + "' order by tweetid DESC LIMIT "+ totalTweetLimit +") as T1 group by screen_name order by count(*) DESC";				  
				else
					query = "select screen_name,referenceUser,count(*) from (select * from usertweet where referenceUser='"+referenceUser+"' AND CAST(created_at AS DATE) BETWEEN '" + beginDate + "' AND '" + endDate + "') as T1 group by screen_name order by count(*) DESC";

				//String queryst = "select * from ryersontop41users";

				stmt = con.createStatement();
				resultSet = stmt.executeQuery(query);

				String currentTwitterUser;

				resultSet.beforeFirst();
				while(!resultSet.isLast())
				{
					resultSet.next();
					currentTwitterUser = resultSet.getString(1);
					listOfUsers.add(currentTwitterUser);
				}

				resultSet.close();
				stmt.close();
				con.close();

			} catch (SQLException e1) {
				e1.printStackTrace();
			}

			myGui.updateList(listOfUsers);

		}

	}

	public void startSimulation()
	{
		try{
			Object[] starterAgentArgs = new Object[12];							
			starterAgentArgs[0] = getAID();										
			starterAgentArgs[1] = referenceUser;		//referenceUser						
			starterAgentArgs[2] = "1"; //1
			starterAgentArgs[5] = "0"; //0
			starterAgentArgs[6] = totalUsers; //1
			starterAgentArgs[7] = tweetDelay; //10ms
			starterAgentArgs[8] = "1"; //1
			starterAgentArgs[9] = 1;	//1	
			//starterAgentArgs[10] = usersRec;
			starterAgentArgs[11] = myGui;

			String starterAgentName = "Starter Agent";
			agentController = workContainer.createNewAgent(starterAgentName, StarterAgent.class.getName(), starterAgentArgs);
			listOfAgentControllers.add(agentController);
			agentController.start();
			listOfAgents.add(starterAgentName);
			System.out.println("Made agent: "+starterAgentName);
			System.out.println(getLocalName()+" getAID(): "+getAID());

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}

		myGui.disableStartButton();
		firstRun = false;
	}

	public void initializeSetup(GuiEvent ev)
	{
		String currentTwitterUser = "Ryerson";
		/*referenceUser = "Ryerson";
		String beginDate = "2007-04-04";
		String endDate = "2015-05-24";	
		int kRecommend = 3;
		int hashtags_temp = 1; //1 to remove hash tags	
		int retweetedby_temp = 1; //1 if it is retweeted	
		int stopWordFlag_temp = 0; //remove stop words	

		int readFrom = 1;	//1 to read FROM_DB	
		//Modify for servers @Jason	
		int numOrgAgents = 1;	//number of recommender servers (RecommenderServiceAgents) # Organizing Agents
		int numRecAgents = 4; //numRecAgentss OrganizingAgent, numRecAgentss RecommenderServiceAgent 	 #RecommenderAgents
		//@Jason change limit of overall tweets  
		//  int totalTweetLimit = 7446;
		int totalTweetLimit = 300; //DO NOT SET < 300
		//  int totalTweetLimit = 2000;


		//algorithmRec = COS_SIM;
		  algorithmRec=K_MEANS;
		 */

		/*
		ge.addParameter(numServers);
		ge.addParameter(tweetLimit);
		ge.addParameter(beginDate);
		ge.addParameter(endDate);
		ge.addParameter(kRecommend);
		ge.addParameter(referenceUser);
		ge.addParameter(hashTags);
		ge.addParameter(retweets);
		ge.addParameter(stopWords);
		ge.addParameter(algorithmRec);
		 */
		//readFrom = FROM_DB;	//1 to read FROM_DB	

		referenceUser = (String) ev.getParameter(5);
		String beginDate = (String) ev.getParameter(2);
		String endDate = (String) ev.getParameter(3);	
		int kRecommend = (Integer) ev.getParameter(4);
		int hashtags_temp = (Integer) ev.getParameter(6); //1 to remove hash tags	
		int retweetedby_temp = (Integer) ev.getParameter(7); //1 if it is retweeted	
		int stopWordFlag_temp = (Integer) ev.getParameter(8); //remove stop words			
		//Modify for servers @Jason	
		int numOrgAgents = 1;	//number of recommender servers (RecommenderServiceAgents) # Organizing Agents
		int numRecAgents = (Integer)ev.getParameter(0); //numRecAgentss OrganizingAgent, numRecAgentss RecommenderServiceAgent 	 #RecommenderAgents
		//@Jason change limit of overall tweets  
		int totalTweetLimit = (Integer)ev.getParameter(1); //DO NOT SET < 300

		algorithmRec=(Integer) ev.getParameter(9);

		tweetDelay = 10;

		initialized = true;
		usersRec = myGui.getUsersRec();

		//usersRec.add("Juliebr");
		//usersRec.add("keileek");



		String directoryName = "Results/Timing/" + referenceUser; 
		createDir(directoryName);
		directoryName = "Results/Recommendations/"+referenceUser;
		createDir(directoryName);

		//@Jason
		//IGNORE this part. Go to readFrom == 1
		if (readFrom == FROM_TEXT)
		{
			localDb.clearDb();
			availableDb.clearDb();

			try
			{
				RandomAccessFile file = new RandomAccessFile(textFile, "r");
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Date beginDateFormat = null;
				try {
					beginDateFormat = sdf.parse(beginDate);
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Date endDateFormat = null;
				try {
					endDateFormat = sdf.parse(endDate);
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}			
				String line = "";
				String[] info;
				long seekPosition = 0;
				boolean canAddTweet = false;
				ArrayList<String> tweetParts = new ArrayList<String>();
				Date currentDateFormat = null;
				long tweetId = 0;
				String tweetText = "";
				String currentUserName = "";


				line = file.readLine();
				seekPosition = file.getFilePointer();

				while(line != null)
				{
					tweetParts.clear();
					int tabsCount = 0;

					info = line.split("\t",6);
					/*for (int i = 0; i < info.length; i++)
				{
					System.out.print(i+": "+info[i]+" ");
				}
				System.println();
					 */
					tweetId = Long.valueOf(info[1]);
					currentDateFormat = sdf.parse(info[2]);
					currentUserName = info[4];
					tweetText = info[5];

					if (beginDateFormat.before(currentDateFormat) && endDateFormat.after(currentDateFormat))
						canAddTweet = true;
					else
						canAddTweet = false;

					//Check if nextline is proper format or part of previous tweet with newline(s) in it
					line = file.readLine();

					//If the nextline is null, add the current line to the db
					if (line == null)
					{
						//System.out.println(tweetText);
						Tweet currentTweet = new Tweet(tweetText,tweetId,currentDateFormat,currentUserName);
						if (canAddTweet)
							localDb.addTweet(currentTweet);
						
						break;
					}


					tabsCount = countTabs(line.toCharArray());

					//Proper format with tabs in the tweet text
					if(tabsCount >= 5)
					{
						file.seek(seekPosition);
					}
					//Not proper format so it is text from the previous tweet with newlines in it
					else
					{
						do
						{
							seekPosition = file.getFilePointer();
							//System.out.println("seekPosition: "+seekPosition + " line: "+line);
							tweetParts.add(line);
							line = file.readLine();
							if (line != null)
								tabsCount = countTabs(line.toCharArray());
							else
								break;
						} while (tabsCount < 5);

						for (String partText : tweetParts)
						{
							tweetText = tweetText + partText;
						}

						file.seek(seekPosition);
					}

					Tweet currentTweet = new Tweet(tweetText,tweetId,currentDateFormat,currentUserName);

					if (canAddTweet)
						localDb.addTweet(currentTweet);

					//System.out.println("tabs: "+tabsCount+" position: "+ file.getFilePointer()+" text: "+tweetText);
					//System.out.println(tweetText);
					line=file.readLine();
					seekPosition = file.getFilePointer();
				}

				file.close();

			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (ParseException e)
			{
				e.printStackTrace();
			}		

			ArrayList<Tweet> tweetsFromDb = localDb.getTweets();
			ArrayList<Tweet> datasetAvailable = new ArrayList<Tweet>();

			Collections.sort(tweetsFromDb);

			int tweetToLimitCount = 0;
			String prevUserName = "";
			listOfUsers.clear();

			for (int i = tweetsFromDb.size()-1; i >= 0; i--)
			{
				tweetToLimitCount++;
				//System.out.println("tweetToLimitCount: "+tweetToLimitCount+" totalTweetLimit: "+totalTweetLimit);
				String currentUser;
				Tweet currentTweet = tweetsFromDb.get(i);
				datasetAvailable.add(currentTweet);
				currentUser = currentTweet.getUser();

				if (!currentUser.equals(prevUserName))
				{
					if (!listOfUsers.contains(currentUser))
						listOfUsers.add(currentUser);
					prevUserName = currentUser;
				}
				if (tweetToLimitCount == totalTweetLimit)
					break;
			}

			availableDb.setTweets(datasetAvailable);			

			//Making agents

			try{

				//Create bins for recommender agents
				ArrayList<Integer> bins = new ArrayList<Integer>();
				for(int i=0; i<numRecAgents; i++)
				{
					bins.add(0);
				}

				int currentTweetCount;
				int totalRecBin = 0;
				int smallestBinIndex;

				for (String user : listOfUsers)
				{
					totalUsers++;

					currentTweetCount = availableDb.getTweetCountFromUser(user);
					smallestBinIndex = findSmallestBin(bins);
					totalRecBin = bins.get(smallestBinIndex);
					totalRecBin += currentTweetCount;
					bins.set(smallestBinIndex, totalRecBin);
					smallestBinIndex++;

					ArrayList<Integer> recServers = new ArrayList<Integer>();
					if (usersRec.contains(user))
					{	
						for (int i = 0; i < numRecAgents; i++)
						{
							if (smallestBinIndex-1 != i)
							{
								totalRecBin = bins.get(i);
								totalRecBin += currentTweetCount;
								bins.set(i,totalRecBin);
							}
							recServers.add(i+1);
						}
					}
					else
						recServers.add(smallestBinIndex);

					//Object[] userAgentArgs = new Object[15];							
					Object[] userAgentArgs = new Object[22];
					userAgentArgs[0] = getAID();										
					userAgentArgs[1] = referenceUser;							

					userAgentArgs[3] = beginDate;
					userAgentArgs[4] = endDate;
					userAgentArgs[5] = hashtags_temp;

					userAgentArgs[7] = retweetedby_temp;
					userAgentArgs[8] = stopWordFlag_temp;

					userAgentArgs[10] = recServers;

					userAgentArgs[11] = smallestBinIndex;				

					userAgentArgs[12] = 1;
					userAgentArgs[13] = 1;	
					userAgentArgs[14] = tweetDelay;

					//@Jason added limit and referenceUser;
					userAgentArgs[15]=totalTweetLimit;
					userAgentArgs[16]=referenceUser;
					userAgentArgs[17] = kRecommend;
					userAgentArgs[18] = algorithmRec;
					userAgentArgs[19] = myGui;
					userAgentArgs[20] = readFrom;
					userAgentArgs[21] = availableDb.getTweetsFromUser(user);

					String userAgentName = user + "-UserAgent";
					agentController = workContainer.createNewAgent(userAgentName, MobileAgent.class.getName(), userAgentArgs);   
					agentController.start();
					listOfAgents.add(userAgentName);
					listOfAgentControllers.add(agentController);
					System.out.println("Made agent: "+userAgentName);
					System.out.println("totalUsers: "+totalUsers);

					//++totalUsers;		
				}

				int totalnumberoftweets =0;
				for(int k=0; k<bins.size();k++)
				{
					//totalnumberoftweets += bins.get(k);
					System.out.println("bin "+k+": "+bins.get(k));
				}



				int j =0;
				for(int i=1; i<numRecAgents+1; i++)
				{

					Object[] recAgentArgs = new Object[19];
					recAgentArgs[0] = getAID();										
					recAgentArgs[1] = referenceUser;								

					recAgentArgs[3] = beginDate;
					recAgentArgs[4] = endDate;
					recAgentArgs[5] = hashtags_temp;

					recAgentArgs[7] = retweetedby_temp;
					recAgentArgs[8] = stopWordFlag_temp;



					recAgentArgs[11] = 1;
					recAgentArgs[12] = 1;
					recAgentArgs[13] = bins.get(j);

					recAgentArgs[15] = numRecAgents;

					//@Jason added argument for algorithm to use
					recAgentArgs[16] = algorithmRec;
					//recAgentArgs[17] = usersRec;
					recAgentArgs[18] = myGui;

					String recAgentName = "Recommender-ServiceAgent" + i;
					agentController = workContainer.createNewAgent(recAgentName, RecommenderAgent.class.getName(), recAgentArgs);
					agentController.start();
					listOfAgents.add(recAgentName);
					listOfAgentControllers.add(agentController);
					System.out.println("Made agent: "+recAgentName);
					j++;
				}

				//@Jason added algorithmRec arg
				//Object[] orgAgentArgs = new Object[16];
				Object[] orgAgentArgs = new Object[19];
				orgAgentArgs[0] = getAID();										
				orgAgentArgs[1] = referenceUser;								

				orgAgentArgs[3] = beginDate;
				orgAgentArgs[4] = endDate;
				orgAgentArgs[5] = hashtags_temp;

				orgAgentArgs[7] = retweetedby_temp;
				orgAgentArgs[8] = stopWordFlag_temp;



				orgAgentArgs[11] = 1;
				orgAgentArgs[12] = 1;
				orgAgentArgs[13] = totalUsers;
				orgAgentArgs[14] = 1;		
				orgAgentArgs[15] = numRecAgents;

				//@Jason added algorithmRec argument
				orgAgentArgs[16] = algorithmRec;

				orgAgentArgs[18] = myGui;

				for(int i=1; i<numOrgAgents+1; i++)
				{
					String orgAgentName = "Organizing Agent" + i;
					agentController = workContainer.createNewAgent(orgAgentName, OrganizingAgent.class.getName(), orgAgentArgs);
					agentController.start();
					listOfAgents.add(orgAgentName);
					listOfAgentControllers.add(agentController);
					System.out.println("Made agent: "+orgAgentName);

				}
			}catch (Exception e){
				System.err.println("Error: " + e.getMessage());
			}  

		}


		if (readFrom == FROM_DB)
		{	

			listOfUsers.clear();

			try{


				try {

					String url = "jdbc:mysql://" + serverName + ":" + portNumber + "/" + sid + "?useSSL=false";
					con = DriverManager.getConnection(url, user, pass);


					//@Jason Change the top* tables queryst

					String query;

					if (totalTweetLimit > 0)
						query = "select screen_name,referenceUser,count(*) from (select * from usertweet where referenceUser='"+referenceUser+"' AND CAST(created_at AS DATE) BETWEEN '" + beginDate + "' AND '" + endDate + "' order by tweetid DESC LIMIT "+ totalTweetLimit +") as T1 group by screen_name order by count(*) DESC";				  
					else
						query = "select screen_name,referenceUser,count(*) from (select * from usertweet where referenceUser='"+referenceUser+"' AND CAST(created_at AS DATE) BETWEEN '" + beginDate + "' AND '" + endDate + "') as T1 group by screen_name order by count(*) DESC";
					stmt = con.createStatement();
					resultSet = stmt.executeQuery(query);


				} catch (SQLException e1) {
					e1.printStackTrace();
				}

				resultSet.beforeFirst();
				while(!resultSet.isLast())
				{
					resultSet.next();
					currentTwitterUser = resultSet.getString(1);
					listOfUsers.add(currentTwitterUser);
				}

				//Create bins for recommender agents
				ArrayList<Integer> bins = new ArrayList<Integer>();
				for(int i=0; i<numRecAgents; i++)
				{
					bins.add(0);
				}

				//adds referenceUser tweets to first recommender agent
				//bins.set(0, referenceUserCount);    
				//totalUsers++;

				int currentTweetCount;
				int totalRecBin = 0;
				int smallestBinIndex;

				resultSet.beforeFirst();
				while(!resultSet.isLast())
				{

					resultSet.next();
					currentTwitterUser = resultSet.getString(1);
					//if(!currentTwitterUser.equalsIgnoreCase(referenceUser))			
					//{
					totalUsers++;		

					currentTweetCount  = resultSet.getInt(3);

					smallestBinIndex = findSmallestBin(bins);
					/*System.out.println("smallestBinIndex: "+smallestBinIndex);
					  		for (int i = 0; i < bins.size(); i++){
					  			System.out.println("bin "+i+": "+bins.get(i));
					  		}*/
					totalRecBin = bins.get(smallestBinIndex);
					totalRecBin += currentTweetCount;
					bins.set(smallestBinIndex, totalRecBin);

					smallestBinIndex++;

					//Add list of recommender servers and increase bins accordingly if distributed
					//ie. add the tweet count to all recommenders if there are 2 for the user looking for recommendation
					ArrayList<Integer> recServers = new ArrayList<Integer>();
					if (usersRec.contains(currentTwitterUser))
					{	
						for (int i = 0; i < numRecAgents; i++)
						{
							if (smallestBinIndex-1 != i)
							{
								totalRecBin = bins.get(i);
								totalRecBin += currentTweetCount;
								bins.set(i,totalRecBin);
							}
							recServers.add(i+1);
						}
					}
					else
						recServers.add(smallestBinIndex);
					//Object[] userAgentArgs = new Object[15];							
					Object[] userAgentArgs = new Object[22];
					userAgentArgs[0] = getAID();										
					userAgentArgs[1] = referenceUser;							

					userAgentArgs[3] = beginDate;
					userAgentArgs[4] = endDate;
					userAgentArgs[5] = hashtags_temp;

					userAgentArgs[7] = retweetedby_temp;
					userAgentArgs[8] = stopWordFlag_temp;

					userAgentArgs[10] = recServers;

					userAgentArgs[11] = smallestBinIndex;				

					userAgentArgs[12] = 1;
					userAgentArgs[13] = 1;	
					userAgentArgs[14] = tweetDelay;

					//@Jason added limit and referenceUser;
					userAgentArgs[15]=totalTweetLimit;
					userAgentArgs[16]=referenceUser;
					userAgentArgs[17] = kRecommend;
					userAgentArgs[18] = algorithmRec;
					userAgentArgs[19] = myGui;
					userAgentArgs[20] = readFrom;


					String userAgentName = currentTwitterUser + "-UserAgent";
					agentController = workContainer.createNewAgent(userAgentName, MobileAgent.class.getName(), userAgentArgs);   
					agentController.start();
					listOfAgents.add(userAgentName);
					listOfAgentControllers.add(agentController);
					System.out.println("Made agent: "+userAgentName);
					System.out.println("totalUsers: "+totalUsers);

					//++totalUsers;		
				}
				// }
				resultSet.close();
				stmt.close();
				con.close();


				int totalnumberoftweets =0;
				for(int k=0; k<bins.size();k++)
				{
					//totalnumberoftweets += bins.get(k);
					System.out.println("bin "+k+": "+bins.get(k));
				}



				int j =0;
				for(int i=1; i<numRecAgents+1; i++)
				{

					Object[] recAgentArgs = new Object[19];
					recAgentArgs[0] = getAID();										
					recAgentArgs[1] = referenceUser;								

					recAgentArgs[3] = beginDate;
					recAgentArgs[4] = endDate;
					recAgentArgs[5] = hashtags_temp;

					recAgentArgs[7] = retweetedby_temp;
					recAgentArgs[8] = stopWordFlag_temp;



					recAgentArgs[11] = 1;
					recAgentArgs[12] = 1;
					recAgentArgs[13] = bins.get(j);

					recAgentArgs[15] = numRecAgents;

					//@Jason added argument for algorithm to use
					recAgentArgs[16] = algorithmRec;
					//recAgentArgs[17] = usersRec;
					recAgentArgs[18] = myGui;

					String recAgentName = "Recommender-ServiceAgent" + i;
					agentController = workContainer.createNewAgent(recAgentName, RecommenderAgent.class.getName(), recAgentArgs);
					agentController.start();
					listOfAgents.add(recAgentName);
					listOfAgentControllers.add(agentController);
					System.out.println("Made agent: "+recAgentName);
					j++;
				}

				//@Jason added algorithmRec arg
				//Object[] orgAgentArgs = new Object[16];
				Object[] orgAgentArgs = new Object[19];
				orgAgentArgs[0] = getAID();										
				orgAgentArgs[1] = referenceUser;								

				orgAgentArgs[3] = beginDate;
				orgAgentArgs[4] = endDate;
				orgAgentArgs[5] = hashtags_temp;

				orgAgentArgs[7] = retweetedby_temp;
				orgAgentArgs[8] = stopWordFlag_temp;



				orgAgentArgs[11] = 1;
				orgAgentArgs[12] = 1;
				orgAgentArgs[13] = totalUsers;
				orgAgentArgs[14] = 1;		
				orgAgentArgs[15] = numRecAgents;

				//@Jason added algorithmRec argument
				orgAgentArgs[16] = algorithmRec;

				orgAgentArgs[18] = myGui;

				for(int i=1; i<numOrgAgents+1; i++)
				{
					String orgAgentName = "Organizing Agent" + i;
					agentController = workContainer.createNewAgent(orgAgentName, OrganizingAgent.class.getName(), orgAgentArgs);
					agentController.start();
					listOfAgents.add(orgAgentName);
					listOfAgentControllers.add(agentController);
					System.out.println("Made agent: "+orgAgentName);

				}
			}catch (Exception e){
				System.err.println("Error: " + e.getMessage());
			}

			
		}    

		myGui.updateList(listOfUsers); //update for db or text


	}

	public void killContainer()
	{
		try {
			for (AgentController ac : listOfAgentControllers)
			{
				ac.kill();
			}
			workContainer.kill();
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		listOfAgentControllers.clear();
		listOfAgents.clear();
		totalUsers=0;
		usersRec.clear();

		System.out.println("Killed container");

	}

	//Finds the smallest bin to fill in when using distributed system
	public int findSmallestBin(ArrayList<Integer> numbers) { 
		int smallestIndex = 0;
		int smallest = numbers.get(0); 
		for(int i = 0; i < numbers.size(); i++) { 
			if(numbers.get(i) < smallest) { 
				smallest = numbers.get(i); 
				smallestIndex = i;
			} 
		} 
		return smallestIndex;
	}

	//Count the tabs in the line read
	public int countTabs(char[] charArray)
	{
		int tabs = 0;
		for (int i = 0; i < charArray.length; i++)
		{
			if (charArray[i] == '\t')
				tabs++;
		}
		return tabs;
	}

	//Creates a directory if it does not exist
	public void createDir(String dirName) {
		File dir = new File(dirName);
		if (!dir.exists())
			dir.mkdirs();
	}

	public void setFile(File textFile)
	{
		this.textFile = textFile;
		System.out.println("textFile.getName(): "+textFile.getName());
	}

	public void setReadFrom(int readFrom)
	{
		this.readFrom = readFrom;
	}

}
