package TwitterGatherDataFollowers.userRyersonU;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import java.awt.AWTException;
import java.awt.Robot;


public class RecommenderAgent extends Agent 
{	
	private final long serialVersionUID = 1L;
	private static final int HASH_TAGS = 1;
	private static final int RE_TWEETS = 1;
	private static final int STOP_WORDS = 1;
	public static final int COS_SIM = 0;
	public static final int K_MEANS = 1;

	private AID controllerAgent;

	private String referenceUser; 
	private String tweetNum_temp2;
	private String dc1string_temp;
	private String dc2string_temp;
	private int    hashtags_temp;

	private int    retweetedby_temp;
	private int    stopWordFlag_temp;
	private int    stemFlag_temp;
	private int    numberofAgents_temp;	  
	private int    numberofAgents_tempint;
	private int    temp=0;


	private int connectedtoTfidfservernumber_temp;	  
	private int connectedtoTfidfservernumber;
	private int connectedtoRecservernumber_temp;	  
	private int connectedtoRecservernumber;

	private AID[] allRecommenderAgents;	
	private AID[] allUserAgentConnectedtoThisServer;		
	private AID   AID_agent_name;
	private String agentName;



	private int tweetCount = 0; //Number of tweets currently received from user agents
	private int tweetsToReceive = 100; //Total number of tweets the recommender is supposed to receive from user agents

	private int numRecAgents =0;

	static String serverName = "127.0.0.1";
	static String portNumber = "3306";
	static String sid = "testmysql";

	private Connection con;
	private Statement stmt = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;

	static String user = "root";
	static String pass = "Asdf1234";


	private ArrayList<String> textprocessing_wb_or_tfidf_Data = new ArrayList<String>();		

	//My Own Variables
	String[] stopWordsArray = {"a","able","about","above","abst","accordance","according","accordingly","across","act",
			"actually","added","adj","affected","affecting","affects","after","afterwards","again","against",
			"ah","all","almost","alone","along","already","also","although","always","am",
			"among","amongst","an","and","announce","another","any","anybody","anyhow","anymore",
			"anyone","anything","anyway","anyways","anywhere","apparently","approximately","are","aren","arent",
			"arise","around","as","aside","ask","asking","at","auth","available","away",
			"awfully","b","back","be","became","because","become","becomes","becoming","been",
			"before","beforehand","begin","beginning","beginnings","begins","behind","being","believe","below",
			"beside","besides","between","beyond","biol","both","brief","briefly","but","by",
			"c","ca","came","can","cannot","cant","cause","causes","certain","certainly",
			"co","com","come","comes","contain","containing","contains","could","couldnt","d",
			"da","date","did","didnt","different","do","does","doesnt","doing","done",
			"dont","down","downwards","due","during","e","each","ed","edu","effect",
			"eg","eight","eighty","either","else","elsewhere","end","ending","enough","especially",
			"et","et-al","etc","even","ever","every","everybody","everyone","everything","everywhere",
			"ex","except","f","far","few","ff","fifth","first","five","fix",
			"followed","following","follows","for","former","formerly","forth","found","four","from",
			"further","furthermore","g","gave","get","gets","getting","give","given","gives",
			"giving","go","goes","gone","got","gotten","h","had","happens","hardly",
			"has","hasnt","have","havent","having","he","hed","hence","her","here",
			"hereafter","hereby","herein","heres","hereupon","hers","herself","hes","hi","hid",
			"him","himself","his","hither","home","how","howbeit","however","hundred","i",
			"id","ie","if","ill","im","immediate","immediately","importance","important","in",
			"inc","indeed","index","information","instead","into","invention","inward","is","isnt",
			"it","itd","itll","its","itself","ive","j","just","k","keep",
			"keeps","kept","kg","km","know","known","knows","l","largely","last",
			"lately","later","latter","latterly","least","less","lest","let","lets","like",
			"liked","likely","line","little","ll","look","looking","looks","ltd","m",
			"made","mainly","make","makes","many","may","maybe","me","mean","means",
			"meantime","meanwhile","merely","mg","might","million","miss","ml","more","moreover",
			"most","mostly","mr","mrs","much","mug","must","my","myself","n",
			"na","name","namely","nay","nd","near","nearly","necessarily","necessary","need",
			"needs","neither","never","nevertheless","new","next","nine","ninety","no","nobody",
			"non","none","nonetheless","noone","nor","normally","nos","not","noted","nothing",
			"now","nowhere","o","obtain","obtained","obviously","of","off","often","oh",
			"ok","okay","old","omitted","on","once","one","ones","only","onto",
			"or","ord","other","others","otherwise","ought","our","ours","ourselves","out",
			"outside","over","overall","owing","own","p","page","pages","part","particular",
			"particularly","past","per","perhaps","placed","please","plus","poorly","possible","possibly",
			"potentially","pp","predominantly","present","previously","primarily","probably","promptly","proud","provides",
			"put","q","que","quickly","quite","qv","r","ran","rather","rd",
			"re","readily","really","recent","recently","ref","refs","regarding","regardless","regards",
			"related","relatively","research","respectively","resulted","resulting","results","right","rt","run","s",
			"said","same","saw","say","saying","says","sec","section","see","seeing",
			"seem","seemed","seeming","seems","seen","self","selves","sent","seven","several",
			"shall","she","shed","shell","shes","should","shouldnt","show","showed","shown",
			"showns","shows","significant","significantly","similar","similarly","since","six","slightly","so",
			"some","somebody","somehow","someone","somethan","something","sometime","sometimes","somewhat","somewhere",
			"soon","sorry","specifically","specified","specify","specifying","still","stop","strongly","sub",
			"substantially","successfully","such","sufficiently","suggest","sup","sure","t","take","taken",
			"taking","tell","tends","th","than","thank","thanks","thanx","that","thatll",
			"thats","thatve","the","their","theirs","them","themselves","then","thence","there",
			"thereafter","thereby","thered","therefore","therein","therell","thereof","therere","theres","thereto",
			"thereupon","thereve","these","they","theyd","theyll","theyre","theyve","think","this",
			"those","thou","though","thoughh","thousand","throug","through","throughout","thru","thus",
			"til","tip","to","together","too","took","toward","towards","tried","tries",
			"truly","try","trying","ts","twice","two","ty","u","ull","ull",
			"un","under","unfortunately","unless","unlike","unlikely","until","unto","up","upon",
			"ups","ur","us","use","used","useful","usefully","usefulness","uses","using",
			"usually","v","value","various","ve","very","via","viz","vol","vols",
			"vs","w","want","wants","was","wasnt","way","we","wed","welcome",
			"well","went","were","werent","weve","what","whatever","whatll","whats","when",
			"whence","whenever","where","whereafter","whereas","whereby","wherein","wheres","whereupon","wherever",
			"whether","which","while","whim","whither","who","whod","whoever","whole","wholl",
			"whom","whomever","whos","whose","why","widely","willing","wish","with","within",
			"without","wont","words","world","would","wouldnt","www","x","y","yes",
			"yet","you","youd","youll","your","youre","yours","yourself","yourselves","youve",
			"z","zero"};


	private String ServerNumber ="";

	//@Jason added algorithmRec
	private int algorithmRec = 0;

	//@Jason added convId
	private String convId = "";

	//@Jason added boolean to only calculate once
	private boolean calculateAlready=false;

	//@Jason added list of completed users for recommendation
	private ArrayList<String> completedUsers = new ArrayList<String>();
	private int countUsersCosim = 0;
	private int countScores = 0;


	//Necessary @Jason
	private ArrayList<LinkedHashMap<String,Double>> userDocumentVectorsList = new ArrayList<LinkedHashMap<String,Double>>();
	private LinkedHashMap<String,ArrayList<LinkedHashMap<String,Double>>> allUserDocumentVectors = new LinkedHashMap<String,ArrayList<LinkedHashMap<String,Double>>>();
	private LinkedHashMap<String,ArrayList<String>> allUserDocuments = new LinkedHashMap<String,ArrayList<String>>();
	private LinkedHashMap<String,Integer> allTermsDocumentFreq = new LinkedHashMap<String,Integer>();
	private TreeSet<String> allUniqueDocTerms = new TreeSet<String>();
	private int totalUsers=0,totalWords=0,totalDocuments=0;

	private double startTimeTextProcessing,endTimeTextProcessing,completionTimeTextProcessing;
	private double startTimeTFIDF,endTimeTFIDF,completionTimeTFIDF;
	private double startTimeAlgorithm,endTimeAlgorithm,completionTimeAlgorithm;

	private LinkedHashMap<Long,String> tweetIdUser = new LinkedHashMap<Long,String>();
	private LinkedHashMap<Long,String> tweetIdText = new LinkedHashMap<Long,String>();
	private LinkedHashMap<String,ArrayList<Long>> usersTweetIdsList = new LinkedHashMap<String,ArrayList<Long>>();
	private LinkedHashMap<Long,LinkedHashMap<String,Double>> tweetIdDocumentVector = new LinkedHashMap<Long,LinkedHashMap<String,Double>>();
	private LinkedHashMap<Long,LinkedHashMap<String,Double>> tweetIdTFIDF = new LinkedHashMap<Long,LinkedHashMap<String,Double>>();

	private ArrayList<String> userRegisteredInRecAgent = new ArrayList<String>();
	private ArrayList<String> usersRec;

	private Map<String,TreeMap<String,Double>> allUserScores = new TreeMap<String,TreeMap<String,Double>>();

	transient protected ControllerAgentGui myGui;

	protected void setup() 
	{


		Object[] args = getArguments();
		controllerAgent = (AID) args[0];

		referenceUser = (String) args[1]; 
		tweetNum_temp2    = (String) args[2];
		dc1string_temp    = (String) args[3];
		dc2string_temp    = (String) args[4];
		hashtags_temp     = Integer.parseInt(args[5].toString());

		retweetedby_temp  = (Integer) args[7];
		stopWordFlag_temp = (Integer) args[8];


		connectedtoTfidfservernumber = (Integer) args[11];	  
		connectedtoRecservernumber   = (Integer) args[12];

		tweetsToReceive		 = (Integer) args[13];		

		System.out.println(getLocalName()+" tweetsToReceive: "+tweetsToReceive);

		numRecAgents = (Integer) args[15];

		//@Jason added algorithmRec argument
		algorithmRec = (Integer) args[16];

		//usersRec = (ArrayList<String>) args[17];

		myGui = (ControllerAgentGui) args[18];

		try {
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			ServiceDescription sd = new ServiceDescription();
			sd.setName("Distributed Recommender System");
			sd.setType("Recommender Agent");
			dfd.addServices(sd);
			DFService.register(this, dfd);
			RecommenderServiceBehaviour RecommenderServiceBehaviour = new RecommenderServiceBehaviour(this);
			addBehaviour(RecommenderServiceBehaviour);

			System.out.println(getLocalName()+" REGISTERED WITH THE DF");
		} catch (FIPAException e) {
			e.printStackTrace();
		}		
		agentName = getLocalName();
		AID_agent_name = getAID();

		//@Jason checking AID_agent_name
		System.out.println(this.getLocalName()+" AID_agent_name: "+AID_agent_name);


		ServerNumber = agentName.split("ServiceAgent", 2)[1].trim();

		System.out.println("Hello! I am " + getAID().getLocalName()+ " and is setup properly.");

		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Recommender Agent");
		template.addServices(sd);
		try {
			DFAgentDescription[] result = DFService.search(this, template);
			allRecommenderAgents = new AID[result.length];
			for (int i = 0; i < result.length; ++i) {
				allRecommenderAgents[i] = result[i].getName();
			}
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}

	}

	private class RecommenderServiceBehaviour extends CyclicBehaviour {	
		private static final long serialVersionUID = 1L;

		private LinkedHashMap <String, LinkedHashMap<Long, String>> localDatabase = new LinkedHashMap <String, LinkedHashMap<Long, String>>(); 
		private LinkedHashMap<Long, String> userTweets = new LinkedHashMap<Long, String>();
		private LinkedHashMap<String, Integer> userWords = new LinkedHashMap<String, Integer>();
		private long tweetID  = 0;
		private String strLine ="";
		private StringTokenizer tokenize = new StringTokenizer(strLine);
		private String token = "";
		private int wordcount = 0;	
		private LinkedHashMap <String, LinkedHashMap<String, Integer>> userWordfreqbag = new LinkedHashMap <String, LinkedHashMap<String, Integer>>();		                        
		private LinkedHashMap <String, LinkedHashMap<String, Integer>> userWordfreqbag_temp = new LinkedHashMap <String, LinkedHashMap<String, Integer>>();		                        
		public RecommenderServiceBehaviour(Agent a) {
			super(a);
		}

		public void action() {
			ACLMessage msg= myAgent.receive();

			if (msg!=null && msg.getOntology() == "Update Connected UserAgent List for this Rec Server" && msg.getPerformative() == ACLMessage.REQUEST)
			{
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setName("Distributed Recommender System");
				sd.setType("User-Agent");
				sd.setOwnership(ServerNumber);
				template.addServices(sd);
				try {
					DFAgentDescription[] result = DFService.search(myAgent, template);

					//@Jason checking how many connected agents to recommender agents
					System.out.println(myAgent.getLocalName()+" RESULT LENGTH: "+result.length);

					allUserAgentConnectedtoThisServer = new AID[result.length];
					for (int i = 0; i < result.length; ++i) {
						allUserAgentConnectedtoThisServer[i] = result[i].getName();
					}
				}
				catch (FIPAException fe) {
					fe.printStackTrace();
				}
			}

			if (msg!=null && msg.getOntology() == "Tweet From User Agent")
			{
				tweetCount++;

				ArrayList<String> currUserDocuments;
				ArrayList<Long> currUserTweetIdList;
				String tweetReceived;
				String tweetUserReceived;
				long tweetIdReceived;
				String tweetTextReceived;

				tweetReceived = msg.getContent();
				tweetUserReceived = tweetReceived.split(" ",3)[0];
				tweetIdReceived = Long.valueOf(tweetReceived.split(" ",3)[1]);
				tweetTextReceived = tweetReceived.split(" ",3)[2];

				if (!allUserDocuments.containsKey(tweetUserReceived))
				{
					currUserDocuments = new ArrayList<String>();
					userRegisteredInRecAgent.add(tweetUserReceived);
				}
				else
					currUserDocuments = allUserDocuments.get(tweetUserReceived);

				currUserDocuments.add(tweetTextReceived);
				allUserDocuments.put(tweetUserReceived, currUserDocuments);

				tweetIdText.put(tweetIdReceived, tweetTextReceived);
				tweetIdUser.put(tweetIdReceived, tweetUserReceived);

				if (!usersTweetIdsList.containsKey(tweetUserReceived))
					currUserTweetIdList = new ArrayList<Long>();
				else	
					currUserTweetIdList = usersTweetIdsList.get(tweetUserReceived);

				currUserTweetIdList.add(tweetIdReceived);
				usersTweetIdsList.put(tweetUserReceived, currUserTweetIdList);

				//@Jason see tweets before processing
				try {
					FileWriter writer = new FileWriter("tweetsRec.txt", true); //append

					BufferedWriter bufferedWriter = new BufferedWriter(writer);

					bufferedWriter.write(myAgent.getLocalName()+" "+msg.getContent()+" tweetCount: "+tweetCount);
					bufferedWriter.newLine();

					bufferedWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				//System.out.println(myAgent.getLocalName()+" "+msg.getContent()+" tweetCount: "+tweetCount);

				if(tweetCount == tweetsToReceive)
				{

					/*System.out.println("tweetIdText: "+tweetIdText);
					System.out.println("tweetIdUser: "+tweetIdText);
					System.out.println("usersTweetIdsList: "+usersTweetIdsList);
					 */
					ArrayList<Long> tweetIdsToRemove = new ArrayList<Long>(); //tweetIdsToRemove because no useful info

					startTimeTextProcessing = System.nanoTime();

					for (String curName: usersTweetIdsList.keySet())
					{
						userDocumentVectorsList = new ArrayList<LinkedHashMap<String,Double>>();

						for (Long currTweetId : usersTweetIdsList.get(curName))
						{
							LinkedHashMap<String,Double> userDocumentVector = new LinkedHashMap<String,Double>();
							String currentText = tweetIdText.get(currTweetId);

							//pad out spaces before and after word for parsing 
							currentText = String.format(" %s ",currentText);

							//System.out.println("Original text: "+currentText);


							//Remove Photo: tweets
							if (currentText.contains("Photo:"))
								currentText = currentText.substring(0,currentText.indexOf("Photo:"));

							//Remove Photoset: tweets
							if (currentText.contains("Photoset:"))
								currentText = currentText.substring(0,currentText.indexOf("Photoset:"));

							//Remove all retweets if flagged
							Matcher matcher; //a matcher
							if (currentText.contains("RT @") && retweetedby_temp == RE_TWEETS)
								currentText="";
							else
							{
								//Remove RT @, conserve the text from retweets
								Pattern retweet = Pattern.compile("RT @");

								matcher = retweet.matcher(currentText);
								currentText = matcher.replaceAll("RT ");
							}
							//System.out.println("After RT @: " + currentText);

							//Remove punctuations
							Pattern punctuations = Pattern.compile("[\\p{P}]");
							matcher = punctuations.matcher(currentText);
							currentText = matcher.replaceAll("");

							//System.out.println("After punctuations: "+ currentText);

							//Remove url links
							Pattern links = Pattern.compile("http[a-zA-Z0-9]*|bitly[a-zA-Z0-9]*|www[a-zA-Z0-9]*");
							matcher = links.matcher(currentText);
							currentText = matcher.replaceAll(" ");

							//System.out.println("After url links: "+ currentText);

							//Remove special characters including hash tags if flagged
							if (hashtags_temp == HASH_TAGS)
							{
								Pattern specialCharacters = Pattern.compile("[^a-zA-Z\\p{Z}]");
								matcher = specialCharacters.matcher(currentText);
								currentText = matcher.replaceAll(" ");
							}
							//System.out.println("After special characters: " + currentText);

							//Remove stop words if flagged
							if (stopWordFlag_temp == STOP_WORDS)
							{
								for (String stopWord : stopWordsArray){	
									if (currentText.toLowerCase().contains(" "+stopWord+" "))
										//System.out.println("FOUND STOPWORD: "+stopWord);
										currentText = currentText.toLowerCase().replaceAll(" "+stopWord+" "," ");
								}
							}
							//Change all text to lowercase
							currentText=currentText.toLowerCase();

							//Trim leading and ending white space
							currentText=currentText.trim();

							//System.out.println(getLocalName()+" Removed junk: "+currentText);

							//******@Begin making vectors*************************************************
							//Add processed texts to a list
							Scanner sc = new Scanner(currentText);
							List<String> list = new ArrayList<String>();
							String stringToken;
							double wordFreq = 0.0;
							int wordCount = 0;
							wordCount = currentText.split("\\s+").length;
							//If processed text is a blank line with 1 single space or less than 3 words
							if (wordCount < 3)
							{
								//System.out.println("DO NOT ADD");
								tweetIdsToRemove.add(currTweetId);
							}
							else
							{
								while (sc.hasNext()){
									stringToken = sc.next();
									list.add(stringToken);

									//Add all unique terms to allUniqueDocTerms
									allUniqueDocTerms.add(stringToken);

									//Count frequency of words in a document
									if (userDocumentVector.get(stringToken)!=null) //already exists in vector
										wordFreq = userDocumentVector.get(stringToken)+1;
									else //does not exist in vector yet
										wordFreq = 1.0;

									userDocumentVector.put(stringToken, wordFreq);
								}
								/*for (String s : list){
									System.out.print(s+" ");
								}
								System.out.println();*/
								sc.close();
								//System.out.println();
								//System.out.println("The length of string: "+currentText.length());

								tweetIdDocumentVector.put(currTweetId, userDocumentVector);
								userDocumentVectorsList.add(userDocumentVector);
								//System.out.println("currTweetId: "+currTweetId);
								//System.out.println(userDocumentVector);
							}

						} //end for (Long currTweetId : usersTweetIdsList.get(curName))
						//Case where after processing, some users may have no more useful words left in every document, only add > 0
						if (userDocumentVectorsList.size() > 0)
							allUserDocumentVectors.put(curName,userDocumentVectorsList);	

					} //end for (String curName: usersTweetIdsList.keySet())

					//System.out.println("tweetIdText.size(): "+tweetIdText.size());

					//Remove all tweetIds that are not useful							
					for (Long tweetIdToRemove : tweetIdsToRemove)
					{
						if (tweetIdDocumentVector.containsKey(tweetIdToRemove))
							tweetIdDocumentVector.remove(tweetIdToRemove);
						if (tweetIdText.containsKey(tweetIdToRemove))
							tweetIdText.remove(tweetIdToRemove);
						if (tweetIdUser.containsKey(tweetIdToRemove))
							tweetIdUser.remove(tweetIdToRemove);

						Iterator<Map.Entry<String,ArrayList<Long>>> iterator = usersTweetIdsList.entrySet().iterator();
						while(iterator.hasNext()){
							Map.Entry<String,ArrayList<Long>> entry = iterator.next();
							for (int i = 0; i < entry.getValue().size(); i++)
							{
								if (entry.getValue().get(i) == tweetIdToRemove)
								{
									entry.getValue().remove(i);
								}
							}    
							if (entry.getValue().size() == 0)
								iterator.remove();
						}


					}



					/*
					System.out.println("tweetIdDocumentVector");
					System.out.println(tweetIdDocumentVector);
					 */
					/*
					int countDb2 = 0;
   					FileWriter writer;
					try {
						writer = new FileWriter("myOwnDBText.txt", true); //append
						BufferedWriter bufferedWriter = new BufferedWriter(writer);
						bufferedWriter.write("tweetIdText.size(): "+tweetIdText.size());
						bufferedWriter.newLine();
						for (Long l: tweetIdText.keySet())
						{
							countDb2++;
							bufferedWriter.write("TweetId: "+l);
							bufferedWriter.write("\t Text: "+tweetIdText.get(l));
							bufferedWriter.newLine();
						}

						bufferedWriter.write("countDb2: "+countDb2);
						bufferedWriter.newLine();
						bufferedWriter.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					 */

					endTimeTextProcessing = System.nanoTime();
					completionTimeTextProcessing = endTimeTextProcessing - startTimeTextProcessing;
					System.out.println(getLocalName()+" completionTimeTextProcessing: "+completionTimeTextProcessing);
					System.out.println(getLocalName()+ " After processing, tweets: "+ tweetIdText.size());

					//@Jason added code to deny querying for any user who have no tweet in database after text processing and tweeting simulation is complete 					

					//System.out.println(getLocalName()+" userRegisteredInRecAgent: "+userRegisteredInRecAgent);

					ArrayList<String> usersToRemove = new ArrayList<String>();
					for(String currUser : userRegisteredInRecAgent){

						if (!usersTweetIdsList.containsKey(currUser)) {
							System.out.println(currUser+" DOES NOT EXIST IN DB");
							usersToRemove.add(currUser);

							String userAgent = currUser+"-UserAgent";
							System.out.println("userAgent: "+userAgent);
							ACLMessage stopUserQueryMsg = new ACLMessage (ACLMessage.REQUEST);
							stopUserQueryMsg.addReceiver(new AID(userAgent,AID.ISLOCALNAME));
							stopUserQueryMsg.setPerformative(ACLMessage.REQUEST);
							stopUserQueryMsg.setContent("Denied Querying");
							stopUserQueryMsg.setOntology("Denied Querying");
							send(stopUserQueryMsg);
							System.out.println(getLocalName()+" Sent out Denied Querying for "+userAgent);
						}
					}
					/*	
   					int countDb = 0;
   					FileWriter writer2;
					try {
						writer2 = new FileWriter("myOwnDB.txt", true); //append
						BufferedWriter bufferedWriter = new BufferedWriter(writer2);
						bufferedWriter.write("usersTweetIdsList.size(): "+usersTweetIdsList.size());
						bufferedWriter.write("\tusersTweetIdsList.keySet(): "+usersTweetIdsList.keySet().size());
						bufferedWriter.newLine();
						for (String curUsername: usersTweetIdsList.keySet())
						{
							bufferedWriter.write("username: "+curUsername);
							bufferedWriter.write("\t tweetIds: "+usersTweetIdsList.get(curUsername).size());
							bufferedWriter.newLine();
							for (Long l : usersTweetIdsList.get(curUsername))
							{
								bufferedWriter.write("\ttweetId: "+l);
								bufferedWriter.newLine();
								countDb++;
							}
						}

						bufferedWriter.write("countDb: "+countDb);
						bufferedWriter.newLine();
						bufferedWriter.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
					 */
					//@Jason remove users from Starter Agent's counter
					//Only send remove message if there is a user to remove

					if (usersToRemove.size() > 0)
					{

						ACLMessage removeUsersMessage = new ACLMessage( ACLMessage.INFORM );
						removeUsersMessage.addReceiver(new AID("Starter Agent",AID.ISLOCALNAME));
						removeUsersMessage.setPerformative(ACLMessage.INFORM);
						try {
							removeUsersMessage.setContentObject(usersToRemove);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						removeUsersMessage.setOntology("Remove Users From Total");



						send(removeUsersMessage);

					}
					/*if (usersToRemove.size() > 0)
					{

	   					ACLMessage removeUsersMessage = new ACLMessage( ACLMessage.INFORM );
	   					removeUsersMessage.addReceiver(new AID("Starter Agent",AID.ISLOCALNAME));
	   					removeUsersMessage.setPerformative(ACLMessage.INFORM);
	   					removeUsersMessage.setContent(String.valueOf(usersToRemove.size()));
	   					removeUsersMessage.setOntology("Remove Users From Total");



   						send(removeUsersMessage);

   					//@Jason remove users from allUserAgents list in Starter Agent
	   					removeUsersMessage.setOntology("Remove This User From List of Agents");
	   					for (String currentUser : usersToRemove){
	   						System.out.println(myAgent.getLocalName()+" send to Starter Agent to remove: "+currentUser);
	   						removeUsersMessage.setContent(currentUser+"-UserAgent");
	   						send(removeUsersMessage);
	   					}
					} */ 					


					//@Jason tell Starter Agent recommender agent is done text processing and only valid tweets available
					ACLMessage textProcessMessage = new ACLMessage( ACLMessage.INFORM );
					textProcessMessage.addReceiver(new AID("Starter Agent",AID.ISLOCALNAME));
					textProcessMessage.setPerformative(ACLMessage.INFORM);
					textProcessMessage.setContent("Text Processing Complete");
					textProcessMessage.setOntology("Text Processing Complete");
					send(textProcessMessage);

					System.out.println(getLocalName()+ " Text Processing Complete");

				}
			}

			//@Jason added new message to start recommending
			if (msg!=null && msg.getOntology()=="Start Recommend Algorithms" && msg.getPerformative()==ACLMessage.REQUEST && calculateAlready==false)
			{

				calculateAlready=true;

				System.out.println(myAgent.getLocalName()+" Starting Algorithm: "+algorithmRec);

				System.out.println(myAgent.getLocalName()+" received Start Recommend Algorithms message");


				/*//@Jason see users in local db
					int c=0;
					for(Entry<String,Double> currentUser : User_in_Server.entrySet()){
   						String currentUserID = currentUser.getKey();
   						c++;
   						System.out.println(myAgent.getLocalName()+" user "+c+": "+currentUserID);
					}
				 */

				startTimeTFIDF = System.nanoTime();

				System.out.println(getLocalName()+" Get Document Freq of Terms");
				int docFreq=0;
				//Get the document frequency of terms
				for (String term : allUniqueDocTerms)
				{
					//Initialize allTermsDocumentFreq to 0
					allTermsDocumentFreq.put(term, 0);

					for (String curName: allUserDocumentVectors.keySet())
					{
						for (LinkedHashMap<String,Double> curDoc : allUserDocumentVectors.get(curName))
						{
							for (String docTerm : curDoc.keySet())
							{
								if (term.equals(docTerm))
								{
									docFreq++;
									break;
								}

							}
						}
					}

					allTermsDocumentFreq.put(term,docFreq);
					docFreq=0;

				}

				//*******************************@CALCULATE THE TF-IDF ************************************
				//tf-idf = tf * idf
				//tf log normalization= allUserDocumentVectors
				//idf smooth = log((totalDocuments/df)+1) //adjust for zero log(1)
				//df = allTermsDocumentFreq

				//Put tf-idf weights into documents
				double tf,df,idf,tfidf;
				LinkedHashMap<String,ArrayList<LinkedHashMap<String,Double>>> allUserDocumentsTFIDF = new LinkedHashMap<String,ArrayList<LinkedHashMap<String,Double>>>();
				ArrayList<LinkedHashMap<String,Double>> userDocumentsTFIDFList = new ArrayList<LinkedHashMap<String,Double>>();
				totalDocuments = tweetIdDocumentVector.size();
				System.out.println(getLocalName()+" Calculating TF-IDF");		

				double vectorMagnitude=0.0;

				for (String curName : usersTweetIdsList.keySet())
				{

					userDocumentsTFIDFList = new ArrayList<LinkedHashMap<String,Double>>();


					for (Long currTweetId: usersTweetIdsList.get(curName))
					{
						vectorMagnitude = 0.0;
						LinkedHashMap<String,Double> tweetIdDoc = tweetIdDocumentVector.get(currTweetId);

						LinkedHashMap<String,Double> userDocumentTFIDF = new LinkedHashMap<String,Double>();
						for (String docTerm : tweetIdDoc.keySet())
						{
							tf=1+Math.log10(tweetIdDoc.get(docTerm)); //tf log normalization
							df=allTermsDocumentFreq.get(docTerm);
							idf=Math.log10((totalDocuments/df)+1); //idf smooth, adjust for zero log(1)
							tfidf=tf*idf;
							userDocumentTFIDF.put(docTerm, tfidf);
							//System.out.println("docTerm: "+docTerm+"\ttf: "+tf+"\tdf: "+df+"\tidf: "+idf+"\ttfidf: "+tfidf);					
							vectorMagnitude+=tfidf*tfidf;
						}

						vectorMagnitude = Math.sqrt(vectorMagnitude);

						//precalculate the magnitude of vectors and element-wise division of documents to the magnitude x./|x| ie. normalize the document vectors
						for (String docTerm : tweetIdDoc.keySet())
						{
							tfidf = userDocumentTFIDF.get(docTerm);
							tfidf = tfidf / vectorMagnitude;			
							userDocumentTFIDF.put(docTerm,tfidf);
						}

						userDocumentsTFIDFList.add(userDocumentTFIDF);

						tweetIdTFIDF.put(currTweetId, userDocumentTFIDF);

					} //end for (Long currTweetId: usersTweetIdsList.get(curName)) 

					allUserDocumentsTFIDF.put(curName,userDocumentsTFIDFList);
				} //end for (String curName : usersTweetIdsList.keySet())

				endTimeTFIDF = System.nanoTime();
				completionTimeTFIDF = endTimeTFIDF - startTimeTFIDF;
				System.out.println("completionTimeTFIDF: "+completionTimeTFIDF/1000000.00+"ms");

				usersRec = myGui.getUsersRec();
				System.out.println(getLocalName()+" usersRec: "+usersRec);

				//System.out.println(getLocalName()+" allUserDocumentsTFIDF: "+allUserDocumentsTFIDF);

				//@Jason added conditions to determine what algorithms to use for recommender

				//Cosine Similarity
				if (algorithmRec == COS_SIM)
				{
					//@Jason prevent sleep from windows
					/*try {
					        new Robot().mouseMove(new Random().nextInt(1920),new Random().nextInt(1080));		         
					    } catch (AWTException e) {
					        e.printStackTrace();
					    }
					 */
					//**************CALCULATE COS-SIM SCORES******************

					startTimeAlgorithm = System.nanoTime();

					//setup array of users
					String[] users = new String[allUserDocumentsTFIDF.keySet().size()];
					String[] usersForRec = new String[usersRec.size()];
					allUserDocumentsTFIDF.keySet().toArray(users);
					usersRec.toArray(usersForRec);

					allUserScores = new TreeMap<String,TreeMap<String,Double>>();
					Map<String,Double> userScore1 = new TreeMap<String,Double>();
					Map<String,Double> userScore2 = new TreeMap<String,Double>();
					double magnitudeVector1=0.0,magnitudeVector2=0.0; //magnitude of vectors
					double dpVectors=0.0; //dot product of vectors
					double score=0.0,prevScore=0.0,newScore=0.0;
					Set<String> lowerTermsVector; 
					Set<String> higherTermsVector;		
					int scoreCount=0, docTermCount=0;
					int higherTermsUserIndex, higherTermsUserDocIndex, lowerTermsUserIndex, lowerTermsUserDocIndex;

					//initialize scores to 0.0
					System.out.println(getLocalName()+ "Initialized Scores to 0.0");

					for (int i = 0; i < usersForRec.length; i++)
					{
						for (int j = 0; j < users.length; j++)
						{
							if (!usersForRec[i].equals(users[j]))
							{
								userScore1.put(users[j], 0.0);
								allUserScores.put(usersForRec[i],(TreeMap<String,Double>) userScore1);
							}
						}
						userScore1 = new TreeMap<String,Double>();
					}

					/*
						for (int i = 0; i < users.length; i++)
						{
							for (int j = 0; j < users.length; j++)
							{
								if (!users[i].equals(users[j]))
								{
									userScore1.put(users[j], 0.0);
									allUserScores.put(users[i],(TreeMap<String,Double>) userScore1);
								}
							}
							//userScore1 = new LinkedHashMap<String,Double>();
							userScore1 = new TreeMap<String,Double>();
						}
					 */

					//System.out.println(getLocalName()+" allUserScores: "+allUserScores);


					System.out.println(getLocalName()+ "CALCULATING COS-SIM SCORES");
					//System.out.println();

					for (int i = 0; i < usersForRec.length; i++)
					{
						for (int j = 0; j < users.length; j++)
						{
							if (!usersForRec[i].equals(users[j]))
							{
								int size1 = allUserDocumentsTFIDF.get(usersForRec[i]).size();
								int size2 = allUserDocumentsTFIDF.get(users[j]).size();
								ArrayList<LinkedHashMap<String, Double>> doc1 = allUserDocumentsTFIDF.get(usersForRec[i]);
								ArrayList<LinkedHashMap<String, Double>> doc2 = allUserDocumentsTFIDF.get(users[j]);

								//get documents from users[i]
								for (int k=0; k < size1; k++)
								{
									//get documents from users[j]				
									for (int l=0; l < size2; l++)
									{
										//System.out.println("COSSIM: "+allUserDocumentsTFIDF.get(users[i]).get(k)+"\t"+allUserDocumentsTFIDF.get(users[j]).get(l));

										Set<String> terms1 = doc1.get(k).keySet();
										Set<String> terms2 = doc2.get(l).keySet();
										LinkedHashMap<String,Double> docTerms1 = doc1.get(k);
										LinkedHashMap<String,Double> docTerms2 = doc2.get(l);

										for (String termUser1 : terms1)
										{
											//keeps count of when document k has gone through all its terms
											docTermCount++;
											for (String termUser2 : terms2)
											{

												if (termUser1.equals(termUser2))
												{
													//System.out.print("SAME TERMS "+termUser1+" "+termUser2+" ");
													//System.out.print("dp: "+allUserDocumentsTFIDF.get(users[j]).get(l).get(termUser2)+"*"+allUserDocumentsTFIDF.get(users[i]).get(k).get(termUser1)+"\t");
													dpVectors+=docTerms2.get(termUser2)*docTerms1.get(termUser1);
												}
											}
										}

										score=dpVectors;

										scoreCount++;
										userScore1 = allUserScores.get(usersForRec[i]);
										//userScore2 = allUserScores.get(users[j]);
										if (userScore1.containsKey(users[j]))
										{
											prevScore = userScore1.get(users[j]);
											newScore = prevScore + score;
											userScore1.put(users[j], newScore);
										}
										else if (!userScore1.containsKey(users[j]))
										{
											userScore1.put(users[j], score);
										}
										/*
											if (userScore2.containsKey(users[i]))
											{
												prevScore = userScore2.get(users[i]);
												newScore = prevScore + score;
												userScore2.put(users[i], newScore);
											}
											else if (!userScore2.containsKey(users[i]))
											{
												userScore1.put(users[i], score);
											}
										 */
										//System.out.println("score: "+score);
										dpVectors=0.0;
										docTermCount=0;
										score=0.0;
									}
									allUserScores.put(usersForRec[i],(TreeMap<String,Double>)userScore1);
									//allUserScores.put(users[j],(TreeMap<String,Double>)userScore2);
								}
							}
						} //end for users.length
					} //end for usersForRec.length

					System.out.println(getLocalName()+" After COS SIM scores: "+allUserScores);

					endTimeAlgorithm = System.nanoTime();
					completionTimeAlgorithm = endTimeAlgorithm - startTimeAlgorithm;

					//Output for cosSIM
					textprocessing_wb_or_tfidf_Data.add("CosSim=TP+TFIDF+CosSim" + "\t" + agentName + "\t" + tweetCount + "\t" + completionTimeTextProcessing + "\t" + completionTimeTFIDF    + "\t" + completionTimeAlgorithm + "\t" + System.getProperty("line.separator"));
					System.out.println(agentName+"- Total Tweets Processed: " + tweetCount + " TP:" + completionTimeTextProcessing/1000000.00 + " TFIDF:" + completionTimeTFIDF/1000000.00 + " CosSim:" + completionTimeAlgorithm/1000000.00 + " Total:" + (completionTimeTextProcessing+completionTimeTFIDF+completionTimeAlgorithm)/1000000.00);
					myGui.appendResult(agentName+"\nTotal Tweets Processed: " + tweetCount + " TP:" + round(completionTimeTextProcessing/1000000.00,2) + "ms TFIDF:" + round(completionTimeTFIDF/1000000.00,2) + "ms CosSim:" + round(completionTimeAlgorithm/1000000.00,2) + "ms Total:" + round((completionTimeTextProcessing+completionTimeTFIDF+completionTimeAlgorithm)/1000000.00,2)+"ms");
				}

				//K-Means 
				else if (algorithmRec == K_MEANS)
				{
					//@Jason prevent sleep from windows
					/*try {
					        new Robot().mouseMove(new Random().nextInt(1920),new Random().nextInt(1080));		         
					    } catch (AWTException e) {
					        e.printStackTrace();
					    }
					 */
					//**************CALCULATE K-Means SCORES******************
					int kClusters = 3; //number of k clusters
					long initialTweetId;
					boolean notDuplicateInitialTweetId = false; 
					boolean convergence = false; //documents remain in the same clusters
					int maxIterations = 10;
					ArrayList<Long> initialTweetIds = new ArrayList<Long>();
					List<Long> allTweetIds = new ArrayList<Long>(tweetIdDocumentVector.keySet());
					List<Long> usableTweetIds = new ArrayList<Long>(allTweetIds); //Tweet ids excluding initial tweet ids
					List<Cluster> allClusters = new ArrayList<Cluster>();
					LinkedHashMap<String,Double> centroidTFIDF = new LinkedHashMap<String,Double>();
					LinkedHashMap<String,Double> baseCentroidTFIDF = new LinkedHashMap<String,Double>(); //tfidf of 0.0 for all unique doc terms
					List<List<Point>> prevListPoints = new ArrayList<List<Point>>();

					if (kClusters > allTweetIds.size())
						kClusters = allTweetIds.size();
					if (kClusters < 2)
						kClusters = 2;

					for (String term : allUniqueDocTerms)
					{
						baseCentroidTFIDF.put(term, 0.0);
					}


					startTimeAlgorithm = System.nanoTime();

					System.out.println("Calculating k-means");

					for (int i = 0; i < kClusters; i++)
					{
						Collections.shuffle(allTweetIds);
						initialTweetId = allTweetIds.get(0);
						if (initialTweetIds.contains(initialTweetId))
						{
							notDuplicateInitialTweetId = true;
							while (notDuplicateInitialTweetId)
							{
								System.out.println("Duplicated");
								Collections.shuffle(allTweetIds);
								initialTweetId = allTweetIds.get(0);
								if (!initialTweetIds.contains(initialTweetId))
									notDuplicateInitialTweetId = false;
							}
						}

						initialTweetIds.add(initialTweetId);

						Cluster cluster = new Cluster(i);
						LinkedHashMap<String,Double> currTFIDF = tweetIdTFIDF.get(initialTweetId);
						Point initialPoint = new Point(initialTweetId,currTFIDF);
						initialPoint.setCluster(i);
						cluster.addPoint(initialPoint);

						centroidTFIDF = new LinkedHashMap<String,Double>(baseCentroidTFIDF);

						for (String term : currTFIDF.keySet())
						{
							centroidTFIDF.put(term,currTFIDF.get(term));
						}

						Point centroid = new Point(-1,centroidTFIDF);
						cluster.setCentroid(centroid);

						allClusters.add(cluster);

					}

					//remove initial tweet ids from remaining tweet ids
					usableTweetIds.removeAll(initialTweetIds);
					/*	
						System.out.println("allTweetIds: "+allTweetIds);
						System.out.println("Initial Tweet Ids: "+initialTweetIds);
						System.out.println("usableTweetIds: "+usableTweetIds);
					 */	
					//Create the initial clusters
					for (Cluster currCluster: allClusters)
					{
						List<Point> points = currCluster.getPoints();
						/*System.out.println(currCluster.getPoints());
							for (Point p : points)
							{
								System.out.println(p.getTweetId());
							}
							System.out.println("currCluster centroid: "+currCluster.getCentroid());
						 */
					}

					/*for (int i = 0; i < kClusters; i++) {
				    		Cluster c = allClusters.get(i);
				    		c.plotCluster();
				    	}*/


					//Assign remaining points to the closest cluster
					//assignCluster();
					double highestCosSim = 0.0; 
					int cluster = 0;                 
					double cosSim = 0.0; 

					for(Long currTweetId : usableTweetIds) {

						LinkedHashMap<String,Double> currTFIDF = tweetIdTFIDF.get(currTweetId);
						Point currPoint = new Point(currTweetId,currTFIDF);

						for(int i = 0; i < kClusters; i++) {
							Cluster c = allClusters.get(i);
							cosSim = Point.cosSimDistance(currPoint, c.getCentroid());
							if(cosSim >= highestCosSim){
								highestCosSim = cosSim;
								cluster = i;
							}
						}
						currPoint.setCluster(cluster);
						allClusters.get(cluster).addPoint(currPoint);

						highestCosSim = 0.0;
					}


					//Calculate new centroids.
					//calculateCentroids();
					for(Cluster clusterI : allClusters) {

						List<Point> listOfPoints = clusterI.getPoints();
						int numPoints = listOfPoints.size();

						Point centroid = clusterI.getCentroid();
						if(numPoints > 0) {
							LinkedHashMap<String,Double> newCentroidTFIDF = new LinkedHashMap<String,Double>(baseCentroidTFIDF);
							for (Point p : listOfPoints)
							{
								LinkedHashMap<String,Double> currTFIDF = p.getTfidf();
								for (String term : currTFIDF.keySet())
								{
									double pointTFIDFValue = 0.0;
									double centroidTFIDFValue = 0.0;
									pointTFIDFValue = currTFIDF.get(term);
									centroidTFIDFValue = newCentroidTFIDF.get(term);
									newCentroidTFIDF.put(term, pointTFIDFValue+centroidTFIDFValue);
								}
							}

							for (String term : newCentroidTFIDF.keySet())
							{
								double currTermTFIDF = 0.0;
								currTermTFIDF = newCentroidTFIDF.get(term);
								if (currTermTFIDF > 0.0)
								{
									currTermTFIDF = currTermTFIDF / numPoints;
									newCentroidTFIDF.put(term, currTermTFIDF);
								}

							}

							centroid.setTfidf(newCentroidTFIDF);
						}
					}

					System.out.println("#################");
					System.out.println("Iteration: " + 0);


					for (int i = 0; i < kClusters; i++) {
						Cluster c = allClusters.get(i);
						//c.plotCluster();
						prevListPoints.add(c.getPoints());
						//System.out.println("prevListPoints: "+prevListPoints.get(i));
					}




					//Calculate k-means calculate()
					int iteration = 0;

					//Iterate k-means ********************************************************************************
					while(!convergence && iteration < maxIterations) {

						//Clear cluster state
						//clearClusters();
						for(Cluster clusterK : allClusters) {
							clusterK.clear();
							//System.out.println("clusterK: "+clusterK.getPoints());
						}


						//getCentroids()
						List centroids = new ArrayList(kClusters);
						for(Cluster clusterH : allClusters) {
							Point currCentroid = clusterH.getCentroid();
							Point point = new Point(currCentroid.getTweetId(),currCentroid.getTfidf());
							centroids.add(point);
						}
						List<Point> lastCentroids = centroids;

						//Assign points to the closer cluster
						//assignCluster();
						highestCosSim = 0.0; 
						cluster = 0;                 
						cosSim = 0.0; 

						for(Long currTweetId : allTweetIds) {

							LinkedHashMap<String,Double> currTFIDF = tweetIdTFIDF.get(currTweetId);
							Point currPoint = new Point(currTweetId,currTFIDF);

							for(int i = 0; i < kClusters; i++) {
								Cluster c = allClusters.get(i);
								cosSim = Point.cosSimDistance(currPoint, c.getCentroid());
								if(cosSim >= highestCosSim){
									highestCosSim = cosSim;
									cluster = i;
								}
							}
							currPoint.setCluster(cluster);
							allClusters.get(cluster).addPoint(currPoint);

							highestCosSim = 0.0;
						}


						//Get the current list of points
						List<List<Point>> currListPoints = new ArrayList<List<Point>>();

						//Calculate new centroids.
						//calculateCentroids();

						for(Cluster clusterI : allClusters) {

							List<Point> listOfPoints = clusterI.getPoints();
							currListPoints.add(listOfPoints);

							int numPoints = listOfPoints.size();

							Point centroid = clusterI.getCentroid();
							if(numPoints > 0) {
								LinkedHashMap<String,Double> newCentroidTFIDF = new LinkedHashMap<String,Double>(baseCentroidTFIDF);
								for (Point p : listOfPoints)
								{
									LinkedHashMap<String,Double> currTFIDF = p.getTfidf();
									for (String term : currTFIDF.keySet())
									{
										double pointTFIDFValue = 0.0;
										double centroidTFIDFValue = 0.0;
										pointTFIDFValue = currTFIDF.get(term);
										centroidTFIDFValue = newCentroidTFIDF.get(term);
										newCentroidTFIDF.put(term, pointTFIDFValue+centroidTFIDFValue);
									}
								}

								for (String term : newCentroidTFIDF.keySet())
								{
									double currTermTFIDF = 0.0;
									currTermTFIDF = newCentroidTFIDF.get(term);
									if (currTermTFIDF > 0.0)
									{
										currTermTFIDF = currTermTFIDF / numPoints;
										newCentroidTFIDF.put(term, currTermTFIDF);
									}

								}

								centroid.setTfidf(newCentroidTFIDF);
							}
						}

						iteration++;


						//Check if convergence
						convergence = true;

						for (int i = 0; i < kClusters; i++) {
							List<Point> prevList = prevListPoints.get(i);
							List<Point> currList = currListPoints.get(i);
							//System.out.println("prevList "+prevList);
							//System.out.println("currList "+currList);
							for (Point p : prevList)
							{

								if (!currList.contains(p))
								{
									convergence = false;
									break;
								}
							}
							if (convergence == false)
								break;

						}

						if (convergence == false)
							prevListPoints = currListPoints;

						System.out.println("#################");
						System.out.println("Iteration: " + iteration);


						/*	for (int i = 0; i < kClusters; i++) {
					    		Cluster c = allClusters.get(i);
					    		c.plotCluster();
					    	}*/

					}

					String[] users = new String[allUserDocumentsTFIDF.keySet().size()];
					allUserDocumentsTFIDF.keySet().toArray(users);
					allUserScores = new TreeMap<String,TreeMap<String,Double>>();
					Map<String,Double> userScore1 = new TreeMap<String,Double>();
					Map<String,Double> userScore2 = new TreeMap<String,Double>();
					double dpVectors = 0.0, score = 0.0, prevScore = 0.0;

					System.out.println("Initialized Scores to 0.0");
					for (int i = 0; i < users.length; i++)
					{
						for (int j = 0; j < users.length; j++)
						{
							if (!users[i].equals(users[j]))
							{
								userScore1.put(users[j], 0.0);
								allUserScores.put(users[i],(TreeMap<String,Double>) userScore1);
							}
						}
						//userScore1 = new LinkedHashMap<String,Double>();
						userScore1 = new TreeMap<String,Double>();
					}


					//System.out.println("allUserScores:" +allUserScores);

					for (int i = 0; i < allClusters.size(); i++)
					{
						//Not comparing where clusters have a size of 1 or 0
						List<Point> pointsInCluster = allClusters.get(i).getPoints();
						if (pointsInCluster.size() > 1)
						{
							for (int j = 0; j < pointsInCluster.size()-1; j++)
							{
								long tweetId1 = pointsInCluster.get(j).getTweetId();
								LinkedHashMap<String,Double> tweetId1Tfidf = pointsInCluster.get(j).getTfidf();
								Set<String> terms1 = tweetId1Tfidf.keySet();

								for (int k = j+1; k < pointsInCluster.size(); k++)
								{
									long tweetId2 = pointsInCluster.get(k).getTweetId();
									LinkedHashMap<String,Double> tweetId2Tfidf = pointsInCluster.get(k).getTfidf();
									Set<String> terms2 = tweetId2Tfidf.keySet();

									String user1, user2;
									user1 = tweetIdUser.get(tweetId1);
									user2 = tweetIdUser.get(tweetId2);

									if (!user1.equals(user2))
									{
										for (String term1 : terms1)
										{
											for (String term2 : terms2)
											{

												if (term1.equals(term2))
												{
													dpVectors+=tweetId1Tfidf.get(term1)*tweetId2Tfidf.get(term2);
												}
											}
										}

										//System.out.println("user1: "+user1+" user2: "+user2);
										//Update the scores of the users
										//System.out.println(allUserScores.get(user1));
										/*prevScore = allUserScores.get(user1).get(user2);
					        				score = prevScore + dpVectors;
					        				userScore1 = allUserScores.get(user1);
					        				userScore1.put(user2, score);
					        				allUserScores.put(user1,(TreeMap<String,Double>)userScore1);
					        				userScore2 = allUserScores.get(user2);
					        				userScore2.put(user1, score);
					        				allUserScores.put(user2,(TreeMap<String,Double>)userScore2);
										 */
										prevScore = allUserScores.get(user1).get(user2);
										score = prevScore + dpVectors;
										allUserScores.get(user1).put(user2, score);
										allUserScores.get(user2).put(user1, score);

									}
									dpVectors=0.0;
									score=0.0;
								} //end for (int k = j+1; k < pointsInCluster.size(); k++)
							} //end for (int j = 0; j < pointsInCluster.size()-1; j++)
						} //end if (pointsInCluster.size() > 1)
					} //for (int i = 0; i < allClusters.size(); i++)

					/*for (String s : allUserScores.keySet())
						{
							System.out.print("user: "+s+"\t");
							System.out.println(allUserScores.get(s));
						}*/


					endTimeAlgorithm = System.nanoTime();
					completionTimeAlgorithm = endTimeAlgorithm - startTimeAlgorithm;						

					//Output for K-means	   					
					textprocessing_wb_or_tfidf_Data.add("K-means=TP+TFIDF+K-means" + "\t" + agentName + "\t" + tweetCount + "\t" + completionTimeTextProcessing + "\t" + completionTimeTFIDF    + "\t" + completionTimeAlgorithm + "\t" + System.getProperty("line.separator"));
					System.out.println(agentName+"- Total Tweets Processed: " + tweetCount + " TP:" + completionTimeTextProcessing/1000000.00 + "ms TFIDF:" + completionTimeTFIDF/1000000.00    + "ms K-means:" + completionTimeAlgorithm/1000000.00 + "ms Total:" + (completionTimeTextProcessing+completionTimeTFIDF+completionTimeAlgorithm)/1000000.00+"ms");
					myGui.appendResult(agentName+"\nTotal Tweets Processed: " + tweetCount + " TP:" + round(completionTimeTextProcessing/1000000.00,2) + "ms TFIDF:" + round(completionTimeTFIDF/1000000.00,2) + "ms K-means:" + round(completionTimeAlgorithm/1000000.00,2) + "ms Total:" + round((completionTimeTextProcessing+completionTimeTFIDF+completionTimeAlgorithm)/1000000.00,2)+"ms");

				}

				//Added in @Jason display text processing wb/tfidf kmean/cosSIM time in ms
				for (String s : textprocessing_wb_or_tfidf_Data){
					System.out.print(s);
				}

				myGui.setTPTime(completionTimeTextProcessing/1000000.00);
				myGui.setTfidfTime(completionTimeTFIDF/1000000.00);
				myGui.setAlgorithmTime(completionTimeAlgorithm/1000000.00);


				System.out.println(getLocalName()+" tweetCount: "+ tweetCount+"\ttweetreceived: "+tweetsToReceive);


				//OUTPUT TO TIMING, NEED TO EDIT

				String outputFilename = "Results/Timing/" + referenceUser + "/" + "Distributed_Server_TP_TFIDF_Algorithm" + numRecAgents + ".txt"; 
				try {
					saveToFile_array(outputFilename, textprocessing_wb_or_tfidf_Data, "append");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				textprocessing_wb_or_tfidf_Data.clear();

				ACLMessage msg7 = new ACLMessage( ACLMessage.INFORM );
				msg7.addReceiver( new AID("Starter Agent", AID.ISLOCALNAME) );
				msg7.setPerformative( ACLMessage.INFORM );
				msg7.setContent("Tweeting TFIDF Algorithm Calculation Completed");
				//@Jason took out conversationID
				msg7.setOntology("Tweets TFIDF Algorithm Calculation Done");
				send(msg7);

				ACLMessage toMergeMsg = new ACLMessage(ACLMessage.INFORM);
				toMergeMsg.addReceiver( new AID("Organizing Agent1", AID.ISLOCALNAME) );
				toMergeMsg.setPerformative(ACLMessage.INFORM);
				toMergeMsg.setOntology("Merge Lists");
				try {
					toMergeMsg.setContentObject((Serializable) allUserScores);
					send(toMergeMsg);
					System.out.println(getLocalName()+" sent toMergeMsg");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}




		protected String removeUrl(String commentstr)
		{
			String urlPattern = "((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\))+[\\w\\d:#@%/;$()''~_?\\+-=\\\\\\.&]*)";
			Pattern p = Pattern.compile(urlPattern,Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(commentstr);
			while (m.find()) {
				commentstr = commentstr.replace(m.group(),"").trim();
			}
			return commentstr;
		}


		protected String removeHashtags(String commentstr)
		{
			LinkedList<String> hashTaglist = new LinkedList<String>();

			String urlPattern = "((?:^|\\s|)(#[\\p{L}0-9-_]+))";
			Pattern p = Pattern.compile(urlPattern,Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(commentstr);
			int i=0;
			while (m.find()) {
				commentstr = commentstr.replaceAll(m.group(1),"").trim();
				i++;
			}
			return commentstr;
		}

		protected String removeUsernames(String commentstr)
		{
			LinkedList<String> hashTaglist = new LinkedList<String>();

			String urlPattern = "((?:^|\\s|)(@[\\p{L}0-9-_]+))";
			Pattern p = Pattern.compile(urlPattern,Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(commentstr);
			int i=0;
			while (m.find()) {
				commentstr = commentstr.replaceAll(m.group(1),"").trim();
				i++;
			}
			return commentstr;
		}

		protected boolean isRetweeted(String commentstr)
		{
			String urlPattern = "((RT\\s@)+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
			Pattern p = Pattern.compile(urlPattern,Pattern.CASE_INSENSITIVE);
			Matcher match = p.matcher(commentstr);
			if (match.find()) {
				return true;
			}
			return false;
		}

		protected String removeStopwords(String strLine_temp)
		{
			String strLinefinal = "";
			StringTokenizer tokenize = new StringTokenizer(strLine_temp);
			while (tokenize.hasMoreTokens()) 
			{
				boolean foundstopword = false;
				token = tokenize.nextToken();
				token = token.replaceAll("[-+.^:,'Ã¢â‚¬â„¢?;Ã¢â‚¬â€�&!()Ã¢â‚¬Å“Ã¢â‚¬ï¿½\"]","");
				token = token.toLowerCase();

				for(int k=0;  k<stopWordsArray.length; k++)
				{
					if(token.equals(stopWordsArray[k]))
					{    	
						foundstopword = true;
					}
				}
				if(foundstopword == false)
				{
					strLinefinal += token + " ";
				} 	 
			}
			return strLinefinal;
		}



		protected void saveToFile_array(String filename, ArrayList<String> result, String append) throws IOException 
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true));
			if(append=="not_append")
			{
				writer = new BufferedWriter(new FileWriter(filename, false));
			}
			for(int i=0; i<result.size(); i++)
			{
				writer.write(result.get(i));
				writer.flush();
			}
			writer.close();
			return;
		}		

	}

	//Source from: http://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	protected void takeDown() 
	{
		try {
			DFService.deregister(this);
			System.out.println(getLocalName()+" DEREGISTERED WITH THE DF");
			//doDelete();
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}

}