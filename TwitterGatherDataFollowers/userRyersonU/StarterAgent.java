package TwitterGatherDataFollowers.userRyersonU;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Collections;
import java.util.TreeSet;
import java.util.Map.Entry;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;

import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.KillAgent;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.domain.mobility.MobilityOntology;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class StarterAgent extends Agent 
{
	private static final long serialVersionUID = 1L;

	private AID[] alltfidfserviceAgents;
	private AID controllerAID;
	private Location destination;
	private int numberofusers=0;
	private int numberofusers_counter=0;
	private int queryUserCounter=0;
	private int numberofusermessagesfromserver=0;
	private int userCounter=0;

	private int count_contiuneTweeting_messages   = 0;
	private int count_contiuneTweeting_messages_2 = 0;


	private int nextUser = 0;
	private int tfidfservercount=0;
	private	int totalvectorstosend = 0; //useless

	private int tweetingdelay = 0;
	private int increament_in_totalvectorstosend = 0; //useless
	private int numberofrun = 2;
	private int numberofrunset = 4;

	private int numberofrun_counter = 0;
	private int numberofrunset_counter = 0;


	private String conversationIDReceived;
	private String conversationIDInitial = "StartSim";
	private boolean alltweetsflag = false;
	private boolean alltfidfflag = false;

	//@Jason added numOfRecAgents recommender agents
	private int numOfRecAgents=0;

	//@Jason changed datastructure of allUserAgents to a list
	private ArrayList<AID> allUserAgentsList = new ArrayList<AID>();

	private ArrayList<String> usersRec;

	transient protected ControllerAgentGui myGui;

	private boolean endSimulation = false; //To determine if user that is looking for recommendation is removed after processing

	protected void setup() 
	{


		getContentManager().registerLanguage(new SLCodec());
		getContentManager().registerOntology(JADEManagementOntology.getInstance());

		Object[] args 				= getArguments();
		controllerAID				= (AID) args[0];
		destination 				= here();

		String temptweetvector    	= (String) args[5];
		numberofusers 				= (Integer) args[6];
		tweetingdelay				= (Integer) args[7]; //ACTUALLY USED
		//usersRec = (ArrayList<String>) args[10];
		myGui = (ControllerAgentGui) args[11];


		final int numberofuserparticipated = numberofusers;
		totalvectorstosend = Integer.parseInt(temptweetvector); //useless

		//@Jason checking numberofuserparticipated
		System.out.println("numberofuserparticipated: "+numberofuserparticipated);

		try {
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			ServiceDescription sd = new ServiceDescription();
			sd.setName("Distributed Recommender System");
			sd.setType("Starter Agent");
			dfd.addServices(sd);
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}		

		DFAgentDescription recAgentDFTemplate = new DFAgentDescription();
		ServiceDescription sd2 = new ServiceDescription();
		sd2.setType("Recommender Agent");
		recAgentDFTemplate.addServices(sd2);
		try {
			DFAgentDescription[] result2 = DFService.search(this, recAgentDFTemplate);
			alltfidfserviceAgents = new AID[result2.length];
			for (int i = 0; i < result2.length; ++i) {
				alltfidfserviceAgents[i] = result2[i].getName();
			}
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}

		//Set controllerAgentDFTemplate to look in DF for user agents
		DFAgentDescription controllerAgentDFTemplate = new DFAgentDescription();
		ServiceDescription sd3 = new ServiceDescription();
		sd3.setType("Controller Agent");
		controllerAgentDFTemplate.addServices(sd3);

		//@Jason get controller AID
		try{
			DFAgentDescription[] result = DFService.search(this, controllerAgentDFTemplate);
			controllerAID = result[0].getName();
		}

		catch (FIPAException fe) {
			fe.printStackTrace();
		}



		//Set userAgentDFTemplate to look in DF for user agents
		DFAgentDescription userAgentDFTemplate = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("User-Agent");
		userAgentDFTemplate.addServices(sd);

		//@Jason using allUserAgentsList
		try{
			DFAgentDescription[] result = DFService.search(this, userAgentDFTemplate);
			for (int i = 0; i < result.length; i++){
				allUserAgentsList.add(result[i].getName());
			}
		}

		catch (FIPAException fe) {
			fe.printStackTrace();
		}

		//@Jason print out all the initial user agents
		/*for (int i = 0; i < allUserAgentsList.size(); i++){
			System.out.println(this.getLocalName()+" allUserAgentsList.get("+i+"): "+allUserAgentsList.get(i));
		}*/

		ACLMessage msg2 = new ACLMessage( ACLMessage.REQUEST );
		//@Jason using allUserAgentsList
		for(int i=0; i<allUserAgentsList.size(); i++)
		{
			msg2.addReceiver(allUserAgentsList.get(i));  
		}	  	

		//@Jason added simulation start message and time
		System.out.println("Simulation has started...");
		final long startSimTime = System.currentTimeMillis();
		System.out.println("The start time: "+startSimTime);

		String result = String.valueOf(tweetingdelay);
		msg2.setContent(result);
		msg2.setConversationId(conversationIDInitial);
		msg2.setOntology("Start SIM");
		send(msg2);

		myGui.disableList();

		addBehaviour(new TickerBehaviour(this, 10) 
		{
			private static final long serialVersionUID = 1L;

			protected void onTick() 
			{
				ACLMessage msg= myAgent.receive();


				if (msg!=null && msg.getOntology() == "Tweeting Completed" && msg.getPerformative() == ACLMessage.INFORM)
				{
					//System.out.println(getLocalName()+" Tweeting Completed from: "+msg.getSender().getLocalName());
					ACLMessage reply = msg.createReply();
					reply.setPerformative( ACLMessage.REQUEST );
					reply.setContent("Stop Tweeting");
					reply.setOntology("Stop Tweeting");
					send(reply);

					numberofusers_counter++;

					//@Jason changed to numberofuserparticipated
					if(numberofusers_counter == numberofuserparticipated)
						//if(numberofusers_counter == numberofusers)
					{
						numberofusers_counter = 0;
						//@Jason changed to -1 to reuse counter in ontology: Querying Done from Organizing Agent below
						alltweetsflag = true;
						System.out.println(myAgent.getLocalName()+" TWEETING COMPLETED numberofusers: "+numberofusers);
					}

				}

				//@Jason added remove user from total number of users msg from recommenderagent
				if (msg!=null && msg.getOntology() == "Remove Users From Total" && msg.getPerformative() == ACLMessage.INFORM)
				{
					ArrayList<String> usersToRemove;
					try {
						usersToRemove = (ArrayList<String>)msg.getContentObject();
						numberofusers-=usersToRemove.size();

						for (int i = allUserAgentsList.size()-1; i >= 0; i--)
						{
							for (String nameToRemove : usersToRemove)
							{
								String suffixAgentName = "-UserAgent";
								if (allUserAgentsList.get(i).getLocalName().equals(nameToRemove+suffixAgentName))
								{
									allUserAgentsList.remove(i);
									break;
								}
							}
						}

						System.out.println(getLocalName()+" Received Remove Users From Total from: "+ msg.getSender().getLocalName());
						System.out.println(getLocalName()+" Removed User From Total Amount: "+usersToRemove.size());
						System.out.println(getLocalName()+" numberofusers: "+numberofusers);



						for (AID a : allUserAgentsList)
						{
							System.out.println(a.getLocalName());
						}


					} catch (UnreadableException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}




				}

				/*
				//@Jason added remove user from total number of users msg from recommenderagent
				if (msg!=null && msg.getOntology() == "Remove This User From List of Agents" && msg.getPerformative() == ACLMessage.INFORM)
				{
					int indexToRemove = 0;

					for (int i = 0; i < allUserAgentsList.size(); i++){
						//System.out.println("allUserAgentsList.get(i).getLocalName(): "+allUserAgentsList.get(i).getLocalName()+"\tmsg.getContent(): "+msg.getContent());
						if (allUserAgentsList.get(i).getLocalName().equals(msg.getContent())){
							indexToRemove = i;
							break;
						}
					}

					allUserAgentsList.remove(indexToRemove);




				}
				 */
				//@Jason added wait for all recommender agents to finish text processing before calculating recommendations
				if (msg!=null && msg.getOntology() == "Text Processing Complete" && msg.getPerformative() == ACLMessage.INFORM)
				{
					numOfRecAgents++;
					System.out.println(getLocalName()+" Text Processing Completed: "+numOfRecAgents);

					if (numOfRecAgents == alltfidfserviceAgents.length){

						int recUserInList = 0;
						usersRec = myGui.getUsersRec();

						for (String recUser : usersRec)
						{
							for (int i = 0; i < allUserAgentsList.size(); i++){

								String suffixAgentName = "-UserAgent";
								if (allUserAgentsList.get(i).getLocalName().equals(recUser+suffixAgentName))
								{
									recUserInList++;
									System.out.println(getLocalName()+" recUser: "+recUser+" recUserInList: "+recUserInList);
								}
							}

						}

						if (recUserInList == usersRec.size())
						{
							ACLMessage startRecMsg = new ACLMessage(ACLMessage.REQUEST);
							startRecMsg.setContent("Start Recommend Algorithms");
							startRecMsg.setOntology("Start Recommend Algorithms");

							for (int i=0; i < alltfidfserviceAgents.length; i++){
								String recAgentToSend = "Recommender-ServiceAgent"+(i+1);
								System.out.println(getLocalName()+ " Start Recommend Algorithm Msg Sent To: "+recAgentToSend);
								startRecMsg.addReceiver(new AID(recAgentToSend,AID.ISLOCALNAME));
								send(startRecMsg);
							}
						}
						else
						{
							for (String recUser : usersRec)
							{
								System.out.println("ERROR: RECOMMENDED USER " + recUser + " REMOVED FROM LIST OF USERS AFTER TEXT PROCESSING");
								myGui.appendResult("ERROR: RECOMMENDED USER " + recUser + " REMOVED FROM LIST OF USERS AFTER TEXT PROCESSING");
							}
							myGui.enableList();
						}
					}					
				}

				//Msg from rec agents
				if (msg!=null && msg.getOntology() == "Tweets TFIDF Algorithm Calculation Done" && msg.getPerformative() == ACLMessage.INFORM)
				{
					tfidfservercount++;
					System.out.println(getLocalName()+" received: Tweets TFIDF Algorithm Calculation Done");
					if(tfidfservercount == alltfidfserviceAgents.length)
					{
						tfidfservercount = 0;

					}
				}
				if (msg!=null && msg.getOntology()=="Merge Lists Completed" && msg.getPerformative() == ACLMessage.INFORM)
				{

					System.out.println(getLocalName()+" received Merge Lists Completed");

					myGui.addTiming();
					usersRec = myGui.getUsersRec();

					ACLMessage msg2 = new ACLMessage( ACLMessage.REQUEST);
					String result = "requestedBy";					
					msg2.setContent(result);
					msg2.setOntology("Start Querying");

					//Send query to only users that are supposed to get recommendations

					int queryMessageCount = 0;
					for (int i = 0; i < allUserAgentsList.size(); i++){

						String suffixAgentName = "-UserAgent";
						for (String queryUserName : usersRec)
						{
							if (allUserAgentsList.get(i).getLocalName().equals(queryUserName+suffixAgentName))
							{
								queryMessageCount++;
								System.out.println(getLocalName()+" "+queryUserName+suffixAgentName+" queryMessageCount: "+queryMessageCount);
								msg2.addReceiver(allUserAgentsList.get(i));
								send(msg2);
							}
						}
						if (queryMessageCount == usersRec.size())
							break;
					}
				}

				//When user got its recommendation list
				if (msg!=null && msg.getOntology() == "Querying Done from Organizing Agent" && msg.getPerformative() == ACLMessage.INFORM) 
				{
					queryUserCounter++;
					numberofusers_counter++;
					//@Jason checking numberofusers_counter
					//System.out.println(myAgent.getLocalName()+ " numberofusers_counter: "+numberofusers_counter + "\tnumberofusers: "+numberofusers);

					System.out.println("queryUserCounter: "+queryUserCounter+" usersRec.size(): "+ usersRec.size());
					if (queryUserCounter == usersRec.size()) 				
						//if(numberofusers_counter == numberofusers)
					{
						numberofusers_counter = 0;
						queryUserCounter = 0;

						//@Jason timing whole execution
						final long endSimTime = System.currentTimeMillis();
						System.out.println("Simulation Completed");
						System.out.println("Final Total execution time: " + (endSimTime - startSimTime) );

						myGui.enableList();

						//@Jason added exit to save CPU from running forever doing nothing and causing an early death for the CPU's lifespan
						//System.exit(0);


					}
				}				
			}
		});


	}


	protected void takeDown() {
		try {
			DFService.deregister(this);
			System.out.println(getLocalName()+" DEREGISTERED WITH THE DF");
			//doDelete();
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}



}
