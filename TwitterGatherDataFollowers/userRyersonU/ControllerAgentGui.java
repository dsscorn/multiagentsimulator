package TwitterGatherDataFollowers.userRyersonU;


import java.text.DecimalFormat;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.UIManager.*;

import jade.gui.*;


public class ControllerAgentGui extends JFrame implements ActionListener {

	private ControllerAgent myAgent;

	private static final int FRAME_WIDTH = 1575;
	private static final int FRAME_HEIGHT = 670;	
	private static final int AREA_ROWS = 15;
	private static final int AREA_COLUMNS = 40;
	private static final int DEFAULT_K_REC = 3;
	private static final int HASH_TAGS = 1;
	private static final int RE_TWEETS = 1;
	private static final int STOP_WORDS = 1;
	private static final String BEGIN_DATE = "2007-01-01";
	private static final String END_DATE = "2017-01-01";
	private static final String DEFAULT_DATASET = "Ryerson";
	public static final int COS_SIM = 0;
	public static final int K_MEANS = 1;
	public static final int FROM_DB = 1;
	public static final int FROM_TEXT = 0;


	private JComboBox algorithmSelectionBox;
	private JLabel enterDatasetLabel;
	private JLabel numServersLabel;
	private JLabel tweetLimitLabel;
	private JLabel beginDateLabel;
	private JLabel endDateLabel;
	private JLabel recommendationLabel;
	private JLabel algorithmLabel;
	private JTextField enterDatasetField;
	private JTextField numServersField;
	private JTextField tweetLimitField;
	private JTextField beginDateField;
	private JTextField endDateField;
	private JTextField recommendationField;
	private JButton initializeButton;
	private JButton quitButton;
	private JButton startButton;
	private JButton getUsersButton;
	private JTextArea recommendationArea;
	private JTextArea resultArea;
	private JTextArea previousResultArea;
	private TitledBorder agentsListTitle;
	private TitledBorder resultsTitle;
	private TitledBorder previousResultsTitle;
	private TitledBorder initializationTitle;
	private TitledBorder commandsTitle;
	private TitledBorder chooseDatasetTitle;
	private TitledBorder textProcessingTitle;
	private TitledBorder recommendationTitle;
	private JCheckBox removeHashTags;
	private JCheckBox removeRetweets;
	private JCheckBox removeStopWords;
	private DefaultListModel<String> agentsList;
	private JList showAgentsList;
	private Border blackBorder;
	private JMenuItem fromTextMenu;
	private JMenuItem fromDbMenu;

	private ArrayList<Timings> timings;
	public Timings currentTiming;

	private String referenceUser;
	private String beginDate;
	private String endDate;
	private int numServers;
	private int kRecommend;
	private int hashTags;
	private int retweets;
	private int stopWords;
	private int simulationIteration;
	private int indexToRecommend;
	private int tweetLimit;
	private int algorithmRec;

	private int countRecServersTP;
	private int countRecServersTfidf;
	private int countRecServersAlgorithm;
	private double currentMaxTPTime;
	private double currentMaxTfidfTime;
	private double currentMaxAlgorithmTime;


	public ControllerAgentGui(ControllerAgent controller) {
		super("Multi-Agent System Simulator for Distributed Recommender System");

		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			// If Nimbus is not available, you can set the GUI to another look and feel.
		}

		myAgent = controller;
		simulationIteration = 0;
		agentsList = new DefaultListModel();
		blackBorder = BorderFactory.createLineBorder(Color.BLACK, 1);
		numServers = 1;
		tweetLimit = 1000;
		beginDate = BEGIN_DATE;
		endDate = END_DATE;
		kRecommend = DEFAULT_K_REC;
		referenceUser = DEFAULT_DATASET;
		hashTags = HASH_TAGS;
		retweets = RE_TWEETS;
		stopWords = STOP_WORDS;
		algorithmRec = COS_SIM;

		indexToRecommend = 0;
		timings = new ArrayList<Timings>();
		countRecServersTP = 0;
		countRecServersTfidf = 0;
		countRecServersAlgorithm = 0;
		currentMaxTPTime = 0;
		currentMaxTfidfTime = 0;
		currentMaxAlgorithmTime = 0;

		createTextAreas();
		createTextFields();
		createInitializationBoxes();
		createButtons();
		createList();
		createPanels();

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				shutDown();
			}
		});

		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		this.setResizable(false);
	}

	private void createTextAreas()
	{
		resultArea = new JTextArea(AREA_ROWS, AREA_COLUMNS);
		resultArea.setBorder(blackBorder);
		resultArea.setText("");
		resultArea.setEditable(false);

		previousResultArea = new JTextArea(AREA_ROWS,AREA_COLUMNS);
		previousResultArea.setBorder(blackBorder);
		previousResultArea.setText("");
		previousResultArea.setEditable(false);

		recommendationArea = new JTextArea(10,AREA_COLUMNS);
		recommendationArea.setBorder(blackBorder);
		recommendationArea.setText("");
		recommendationArea.setEditable(false);
		recommendationArea.setFont(new Font("Arial",Font.BOLD,12));
	}

	private void createTextFields()
	{
		final int FIELD_WIDTH = 10;

		enterDatasetLabel = new JLabel("Enter dataset: ");
		enterDatasetLabel.setForeground(Color.WHITE);
		enterDatasetLabel.setFont(new Font("Arial",Font.BOLD,12));
		enterDatasetField = new JTextField(FIELD_WIDTH);
		enterDatasetField.setText(DEFAULT_DATASET);
		enterDatasetField.setHorizontalAlignment(JTextField.CENTER);
		enterDatasetField.addActionListener(new GetReferenceListener());

		numServersLabel = new JLabel("Number of Recommender Systems: ");
		numServersLabel.setHorizontalAlignment(JLabel.RIGHT);
		numServersLabel.setForeground(Color.WHITE);
		numServersLabel.setFont(new Font("Arial",Font.BOLD,12));
		numServersField = new JTextField(FIELD_WIDTH);
		numServersField.setText("1");
		numServersField.setHorizontalAlignment(JTextField.CENTER);
		numServersField.addActionListener(new NumServersListener());

		tweetLimitLabel = new JLabel("Number of Latest Tweets: ");
		tweetLimitLabel.setHorizontalAlignment(JLabel.RIGHT);
		tweetLimitLabel.setForeground(Color.WHITE);
		tweetLimitLabel.setFont(new Font("Arial",Font.BOLD,12));
		tweetLimitField = new JTextField(FIELD_WIDTH);
		tweetLimitField.setText("1000");
		tweetLimitField.setHorizontalAlignment(JTextField.CENTER);
		tweetLimitField.addActionListener(new TweetLimitListener());

		beginDateLabel = new JLabel("Begin date: ");
		beginDateLabel.setHorizontalAlignment(JLabel.RIGHT);
		beginDateLabel.setForeground(Color.WHITE);
		beginDateLabel.setFont(new Font("Arial",Font.BOLD,12));
		beginDateField = new JTextField(FIELD_WIDTH);
		beginDateField.setText(BEGIN_DATE);
		beginDateField.setHorizontalAlignment(JTextField.CENTER);
		beginDateField.addActionListener(new BeginDateListener());

		endDateLabel = new JLabel("End date: ");
		endDateLabel.setHorizontalAlignment(JLabel.RIGHT);
		endDateLabel.setForeground(Color.WHITE);
		endDateLabel.setFont(new Font("Arial",Font.BOLD,12));
		endDateField = new JTextField(FIELD_WIDTH);
		endDateField.setText(END_DATE);
		endDateField.setHorizontalAlignment(JTextField.CENTER);
		endDateField.addActionListener(new EndDateListener());

		recommendationLabel = new JLabel("Top Recommendations: ");
		recommendationLabel.setHorizontalAlignment(JLabel.RIGHT);
		recommendationLabel.setForeground(Color.WHITE);
		recommendationLabel.setFont(new Font("Arial",Font.BOLD,12));
		recommendationField = new JTextField(FIELD_WIDTH);
		recommendationField.setText("3");
		recommendationField.setHorizontalAlignment(JTextField.CENTER);
		recommendationField.addActionListener(new RecommendationListener());

		algorithmLabel = new JLabel("Algorithm: ");
		algorithmLabel.setForeground(Color.WHITE);
		algorithmLabel.setHorizontalAlignment(JLabel.RIGHT);
		algorithmLabel.setFont(new Font("Arial",Font.BOLD,12));

		String[] algorithmsSelection = {"Cos Sim","K-means"};
		algorithmSelectionBox = new JComboBox(algorithmsSelection);
		((JLabel)algorithmSelectionBox.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		algorithmSelectionBox.addActionListener(new AlgorithmSelectionListener());

	}

	class AlgorithmSelectionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			System.out.println("algorithmSelectionBox.getSelectedIndex(): "+ algorithmSelectionBox.getSelectedIndex());
			algorithmRec = algorithmSelectionBox.getSelectedIndex();
		}
	}

	class GetReferenceListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			String enteredData = enterDatasetField.getText();
			if (!enteredData.equals(""))
			{
				referenceUser = enteredData;
				System.out.println("referenceUser: "+referenceUser);
			}
		}
	}

	class NumServersListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			if (numServersField.getText().equals("") || Integer.parseInt(numServersField.getText()) < 1)
				numServers = 1;
			else
				numServers = Integer.parseInt(numServersField.getText());

			System.out.println("numServers: "+numServers);

		}
	}

	class TweetLimitListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			if (tweetLimitField.getText().equals(""))
				tweetLimit = 1000;
			else
				tweetLimit = Integer.parseInt(tweetLimitField.getText());

			System.out.println("tweetLimit: "+tweetLimit);

		}
	}

	class BeginDateListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			if (beginDateField.getText().equals(""))
				beginDate = BEGIN_DATE;
			else
				beginDate = beginDateField.getText();

			System.out.println("beginDate: "+beginDate);

		}
	}

	class EndDateListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			if (endDateField.getText().equals(""))
				endDate = END_DATE;
			else
				endDate = endDateField.getText();

			System.out.println("endDate: "+endDate);

		}
	}

	class RecommendationListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			if (recommendationField.getText().equals("") || Integer.parseInt(recommendationField.getText()) < DEFAULT_K_REC)
				kRecommend = DEFAULT_K_REC;
			else
				kRecommend = Integer.parseInt(recommendationField.getText());

			System.out.println("kRecommend: "+kRecommend);

		}
	}

	private void createInitializationBoxes()
	{
		removeHashTags = new JCheckBox("Remove #'s");
		removeHashTags.setForeground(Color.WHITE);
		removeHashTags.setFont(new Font("Arial",Font.BOLD,12));
		removeHashTags.setSelected(true);
		removeHashTags.addItemListener(new RemoveBoxListener());

		removeRetweets = new JCheckBox("Remove RT's");
		removeRetweets.setForeground(Color.WHITE);
		removeRetweets.setFont(new Font("Arial",Font.BOLD,12));
		removeRetweets.setSelected(true);
		removeRetweets.addItemListener(new RemoveBoxListener());

		removeStopWords = new JCheckBox("Remove Stop Words");
		removeStopWords.setForeground(Color.WHITE);
		removeStopWords.setFont(new Font("Arial",Font.BOLD,12));
		removeStopWords.setSelected(true);
		removeStopWords.addItemListener(new RemoveBoxListener());
	}

	class RemoveBoxListener implements ItemListener
	{
		public void itemStateChanged(ItemEvent event)
		{
			int index = 0;
			Object source = event.getItemSelectable();
			if (source == removeHashTags)
			{
				index = 0;
				hashTags = HASH_TAGS;
			}
			else if (source == removeRetweets)
			{
				index = 1;
				retweets = RE_TWEETS;
			}
			else if (source == removeStopWords)
			{
				index = 2;
				stopWords = STOP_WORDS;
			}

			if (event.getStateChange() == ItemEvent.DESELECTED){
				if (index == 0)
					hashTags = 0;
				else if (index == 1)
					retweets = 0;
				else if (index == 2)
					stopWords = 0;

			}

			System.out.println("hashTags: "+hashTags+" retweets: "+retweets+ "stopWords: "+stopWords);
		}
	}


	private void createButtons()
	{

		initializeButton = new JButton("Initialize");
		initializeButton.setFont(new Font("Arial",Font.BOLD,12));
		initializeButton.addActionListener(this);

		startButton = new JButton("Run Simulation");
		startButton.setFont(new Font("Arial",Font.BOLD,12));
		startButton.setEnabled(false);
		startButton.addActionListener(this);

		quitButton = new JButton("Quit");
		quitButton.setFont(new Font("Arial",Font.BOLD,12));
		quitButton.addActionListener(this);

		getUsersButton = new JButton("Get Users");
		getUsersButton.setFont(new Font("Arial",Font.BOLD,12));
		getUsersButton.addActionListener(this);

	}

	private void createList()
	{
		showAgentsList = new JList(agentsList);
		showAgentsList.setVisibleRowCount(28);
		showAgentsList.setPrototypeCellValue(String.format("%80s", ""));
		showAgentsList.setLayoutOrientation(JList.VERTICAL);
		//Only allow one user to be selected for recommendation
		showAgentsList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		showAgentsList.addListSelectionListener(new AddListListener());
		showAgentsList.setBorder(blackBorder);

	}

	class AddListListener implements ListSelectionListener
	{
		public void valueChanged(ListSelectionEvent event)
		{
			if (event.getValueIsAdjusting() == false) 
			{
				if (showAgentsList.getSelectedIndex() == -1) 
				{
					//No selection, disable start button.
					startButton.setEnabled(false);
				} 
				else 
				{
					//Selection, enable the start button.
					indexToRecommend = showAgentsList.getSelectedIndex();
				}
			}
		}
	}

	//Creates all the panels in the GUI and add to the frame
	private void createPanels()
	{
		/*setLayout(new BorderLayout());
	    //setContentPane(new JLabel(new ImageIcon("background_image_mas2.jpg")));
		setContentPane(new JLabel(new ImageIcon("ryerson_wallpaper_blue.jpg")));
	    setLayout(new FlowLayout());
		 */
		getContentPane().setBackground(new Color(0,168,239));
		JLabel logo = (new JLabel(new ImageIcon("ryerson_logo_resized.png")));

		Border empty = BorderFactory.createEmptyBorder();

		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		this.setJMenuBar(menuBar);
		fromTextMenu = new JMenuItem("Dataset From Text",KeyEvent.VK_T);
		fromTextMenu.addActionListener(this);
		fromDbMenu = new JMenuItem("Dataset From Database",KeyEvent.VK_D);
		fromDbMenu.addActionListener(this);
		fileMenu.add(fromTextMenu);
		fileMenu.add(fromDbMenu);

		JPanel mainPanel = new JPanel();
		mainPanel = new JPanel();
		mainPanel.setOpaque(false);
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.setOpaque(false);
		JPanel resultsPanel = new JPanel();
		resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
		resultsPanel.setOpaque(false);
		JPanel inputDbPanel = new JPanel();
		inputDbPanel.setOpaque(false);
		JPanel commandsPanel = new JPanel(new GridLayout(2,2));
		commandsPanel.setOpaque(false);
		JPanel initializationsPanel = new JPanel(new GridLayout(3,2));
		initializationsPanel.setOpaque(false);
		JPanel textProcessingPanel = new JPanel();
		textProcessingPanel.setOpaque(false);
		JScrollPane resultScrollPane = new JScrollPane(resultArea);
		resultScrollPane.setOpaque(false);
		//resultScrollPane.setBackground(new Color(0,0,0,0));
		JScrollPane previousResultScrollPane = new JScrollPane(previousResultArea);
		previousResultScrollPane.setOpaque(false);
		//previousResultScrollPane.setBackground(new Color(0,0,0,0));
		JScrollPane dataPane = new JScrollPane(showAgentsList);
		dataPane.setOpaque(false);
		//dataPane.setBackground(new Color(0,0,0,0));
		JScrollPane recommendationPane = new JScrollPane(recommendationArea);
		recommendationPane.setPreferredSize(new Dimension(300,125));
		recommendationPane.setMaximumSize(new Dimension(500,150));
		recommendationPane.setOpaque(false);

		agentsListTitle = BorderFactory.createTitledBorder(empty,"List of Users");
		agentsListTitle.setTitleJustification(TitledBorder.CENTER);
		agentsListTitle.setTitleFont(new Font("Arial",Font.BOLD,20));
		agentsListTitle.setTitleColor(Color.WHITE);
		chooseDatasetTitle = BorderFactory.createTitledBorder(empty,"Dataset Chooser");
		chooseDatasetTitle.setTitleJustification(TitledBorder.CENTER);
		chooseDatasetTitle.setTitleFont(new Font("Arial",Font.BOLD,20));
		chooseDatasetTitle.setTitleColor(Color.WHITE);
		initializationTitle = BorderFactory.createTitledBorder(empty,"Recommender System Initialization Parameters");
		initializationTitle.setTitleJustification(TitledBorder.CENTER);
		initializationTitle.setTitleFont(new Font("Arial",Font.BOLD,20));
		initializationTitle.setTitleColor(Color.WHITE);
		commandsTitle = BorderFactory.createTitledBorder(empty,"Commands");
		commandsTitle.setTitleJustification(TitledBorder.CENTER);
		commandsTitle.setTitleFont(new Font("Arial",Font.BOLD,20));
		commandsTitle.setTitleColor(Color.WHITE);
		resultsTitle = BorderFactory.createTitledBorder(empty,"Timing Results");
		resultsTitle.setTitleJustification(TitledBorder.CENTER);
		resultsTitle.setTitleFont(new Font("Arial",Font.BOLD,20));
		resultsTitle.setTitleColor(Color.WHITE);
		previousResultsTitle = BorderFactory.createTitledBorder(empty,"Previous Timing Results");
		previousResultsTitle.setTitleJustification(TitledBorder.CENTER);
		previousResultsTitle.setTitleFont(new Font("Arial",Font.BOLD,20));
		previousResultsTitle.setTitleColor(Color.WHITE);
		textProcessingTitle = BorderFactory.createTitledBorder(empty,"Text Processing Parameters"); 
		textProcessingTitle.setTitleJustification(TitledBorder.CENTER);
		textProcessingTitle.setTitleFont(new Font("Arial",Font.BOLD,20));
		textProcessingTitle.setTitleColor(Color.WHITE);
		recommendationTitle = BorderFactory.createTitledBorder(empty,"Recommendations for User");
		recommendationTitle.setTitleJustification(TitledBorder.CENTER);
		recommendationTitle.setTitleFont(new Font("Arial",Font.BOLD,20));
		recommendationTitle.setTitleColor(Color.WHITE);

		dataPane.setBorder(agentsListTitle);
		resultScrollPane.setBorder(resultsTitle);
		previousResultScrollPane.setBorder(previousResultsTitle);
		textProcessingPanel.setBorder(textProcessingTitle);
		recommendationPane.setBorder(recommendationTitle);

		/*
		inputDbPanel.add(enterDatasetLabel);
		inputDbPanel.add(enterDatasetField);
		inputDbPanel.setBorder(chooseDatasetTitle);
		*/
		inputDbPanel.add(logo);

		initializationsPanel.add(numServersLabel);
		initializationsPanel.add(numServersField);
		initializationsPanel.add(tweetLimitLabel);
		initializationsPanel.add(tweetLimitField);
		initializationsPanel.add(beginDateLabel);
		initializationsPanel.add(beginDateField);
		initializationsPanel.add(endDateLabel);
		initializationsPanel.add(endDateField);
		initializationsPanel.add(recommendationLabel);
		initializationsPanel.add(recommendationField);
		initializationsPanel.add(algorithmLabel);
		initializationsPanel.add(algorithmSelectionBox);
		initializationsPanel.setBorder(initializationTitle);

		textProcessingPanel.add(removeHashTags);
		textProcessingPanel.add(removeRetweets);
		textProcessingPanel.add(removeStopWords);

		commandsPanel.add(getUsersButton);
		commandsPanel.add(initializeButton);
		commandsPanel.add(startButton);
		commandsPanel.add(quitButton);
		commandsPanel.setBorder(commandsTitle);

		centerPanel.add(inputDbPanel);
		Dimension maxSize = new Dimension(Short.MAX_VALUE, 100);

		centerPanel.add(new Box.Filler(new Dimension(10,10), new Dimension(10,10), maxSize));
		centerPanel.add(initializationsPanel);
		centerPanel.add(new Box.Filler(new Dimension(10,10), new Dimension(10,10), maxSize));
		centerPanel.add(textProcessingPanel);
		centerPanel.add(new Box.Filler(new Dimension(10,20), new Dimension(10,20), new Dimension(30,20)));
		centerPanel.add(recommendationPane);
		centerPanel.add(new Box.Filler(new Dimension(10,20), new Dimension(10,20), maxSize));
		centerPanel.add(commandsPanel);

		resultsPanel.add(resultScrollPane);
		resultsPanel.add(previousResultScrollPane);

		mainPanel.add(dataPane);
		mainPanel.add(centerPanel);
		mainPanel.add(resultsPanel);

		add(mainPanel);

	}

	public void actionPerformed(ActionEvent event) {

		if (event.getSource() == quitButton)
		{
			shutDown();
		}
		else if (event.getSource() == getUsersButton)
		{
			getUsers();
		}
		else if (event.getSource() == initializeButton)
		{
			initializeAgents();
		}
		else if (event.getSource() == startButton)
		{
			startSimulation();
		}
		else if (event.getSource() == fromTextMenu)
		{
			selectFile();
			System.out.println("fromTextMenu");
		}
		else if (event.getSource() == fromDbMenu)
		{
			myAgent.setReadFrom(FROM_DB);
			System.out.println("fromDbMenu");
		}
	}

	public void selectFile()
	{
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
		int result = fileChooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			myAgent.setFile(selectedFile);
			System.out.println("Selected file: " + selectedFile.getAbsolutePath());    
		}
		myAgent.setReadFrom(FROM_TEXT);

	}

	public void getUsers()
	{
		String enteredData = enterDatasetField.getText();
		if (!enteredData.equals(""))
		{
			referenceUser = enteredData;
			System.out.println("referenceUser: "+referenceUser);
		}
		else
		{
			referenceUser = DEFAULT_DATASET;
		}

		if (tweetLimitField.getText().equals(""))
			tweetLimit = 1000;
		else
			tweetLimit = Integer.parseInt(tweetLimitField.getText());

		if (beginDateField.getText().equals(""))
			beginDate = BEGIN_DATE;
		else
			beginDate = beginDateField.getText();

		if (endDateField.getText().equals(""))
			endDate = END_DATE;
		else
			endDate = endDateField.getText();

		GuiEvent ge = new GuiEvent(this,myAgent.GET_USERS);
		ge.addParameter(referenceUser);
		ge.addParameter(tweetLimit);
		ge.addParameter(beginDate);
		ge.addParameter(endDate);		
		myAgent.postGuiEvent(ge);

	}

	public void initializeAgents()
	{
		recommendationArea.setText("");

		if (numServersField.getText().equals("") || Integer.parseInt(numServersField.getText()) < 1)
			numServers = 1;
		else
			numServers = Integer.parseInt(numServersField.getText());

		if (tweetLimitField.getText().equals(""))
			tweetLimit = 1000;
		else
			tweetLimit = Integer.parseInt(tweetLimitField.getText());

		if (beginDateField.getText().equals(""))
			beginDate = BEGIN_DATE;
		else
			beginDate = beginDateField.getText();

		if (endDateField.getText().equals(""))
			endDate = END_DATE;
		else
			endDate = endDateField.getText();

		if (recommendationField.getText().equals("") || Integer.parseInt(recommendationField.getText()) < DEFAULT_K_REC)
			kRecommend = DEFAULT_K_REC;
		else
			kRecommend = Integer.parseInt(recommendationField.getText());

		String enteredData = enterDatasetField.getText();
		if (!enteredData.equals(""))
		{
			referenceUser = enteredData;
			System.out.println("referenceUser: "+referenceUser);
		}

		GuiEvent ge = new GuiEvent(this,myAgent.INITIALIZE);
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
		myAgent.postGuiEvent(ge);

	}

	public void startSimulation()
	{
		GuiEvent ge = new GuiEvent(this,myAgent.START_SIM);
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
		myAgent.postGuiEvent(ge);

		simulationIteration++;
		if (simulationIteration > 1)
		{
			previousResultArea.append(resultArea.getText());
			resultArea.setText("");
		}

		resultArea.append("====================== Simulation Iteration "+ simulationIteration+" =======================\n");

		countRecServersTP = 0;
		countRecServersTfidf = 0;
		countRecServersAlgorithm = 0;
		currentMaxTPTime = 0;
		currentMaxTfidfTime = 0;
		currentMaxAlgorithmTime = 0;

		if (algorithmRec == COS_SIM)
			currentTiming = new Timings(currentMaxTPTime,currentMaxTfidfTime,currentMaxAlgorithmTime,"CosSim");
		else if (algorithmRec == K_MEANS)
			currentTiming = new Timings(currentMaxTPTime,currentMaxTfidfTime,currentMaxAlgorithmTime,"K-means");

	}

	public ArrayList<String> getUsersRec()
	{
		String userToRecommend;
		System.out.println("indexToRecommend: "+indexToRecommend);
		userToRecommend = agentsList.getElementAt(indexToRecommend);
		userToRecommend = userToRecommend.split("-",2)[0];
		ArrayList<String> usersRec = new ArrayList<String>();
		usersRec.add(userToRecommend);
		System.out.println("controllerGUI usersRec: "+usersRec);
		return usersRec;
	}

	public void shutDown() 
	{
		GuiEvent ge = new GuiEvent(this, myAgent.QUIT);
		myAgent.postGuiEvent(ge);
	}

	public void appendResult(String resultText)
	{
		resultArea.append(resultText+"\n");
	}

	public void appendPreviousResult(String resultText)
	{
		previousResultArea.append(resultText+"\n");
	}

	public void appendRecommendation(String recommendationText)
	{
		recommendationArea.append(recommendationText);
	}

	public void disableStartButton()
	{
		startButton.setEnabled(false);
	}

	public void enableStartButton()
	{
		startButton.setEnabled(true);
	}

	public void disableList()
	{
		showAgentsList.setEnabled(false);
	}

	public void enableList()
	{
		showAgentsList.setEnabled(true);
	}

	public void updateList(ArrayList<String> listOfAgents) 
	{
		agentsList.clear();
		System.out.println("listOfAgents.size(): "+listOfAgents.size());
		for (int i = 0; i < listOfAgents.size(); i++){
			agentsList.addElement(listOfAgents.get(i));
		}
	}

	public void setTPTime(double tpTime)
	{
		countRecServersTP++;
		if (countRecServersTP <= numServers)
			currentMaxTPTime = getMax(currentMaxTPTime,tpTime);
		currentTiming.setTPTime(currentMaxTPTime);
	}

	public void setTfidfTime(double tfidfTime)
	{
		countRecServersTfidf++;
		if (countRecServersTfidf <= numServers)
			currentMaxTfidfTime = getMax(currentMaxTfidfTime,tfidfTime);
		currentTiming.setTFIDFTime(currentMaxTfidfTime);
	}

	public void setAlgorithmTime(double algorithmTime)
	{
		countRecServersAlgorithm++;
		if (countRecServersAlgorithm <= numServers)
			currentMaxAlgorithmTime = getMax(currentMaxAlgorithmTime,algorithmTime);
		currentTiming.setAlgorithmTime(currentMaxAlgorithmTime);

	}

	public double getMax(double time1, double time2)
	{
		if (time1 < time2)
			return time2;
		else
			return time1;
	}

	public void addTiming()
	{
		timings.add(currentTiming);
		if (simulationIteration > 1)
		{
			double differenceTP, differenceTfidf, differenceAlgorithm;
			double currentTP, currentTfidf, currentAlgorithm;
			double previousTP, previousTfidf, previousAlgorithm;
			DecimalFormat df = new DecimalFormat("#.##");

			currentTP = timings.get(timings.size()-1).getTPTime();
			currentTfidf = timings.get(timings.size()-1).getTFIDFTime();
			currentAlgorithm = timings.get(timings.size()-1).getAlgorithmTime();
			previousTP = timings.get(timings.size()-2).getTPTime();
			previousTfidf = timings.get(timings.size()-2).getTFIDFTime();
			previousAlgorithm = timings.get(timings.size()-2).getAlgorithmTime();

			System.out.println("currentTP: "+currentTP + " currentTfidf: "+currentTfidf + " currentAlgorithm: "+currentAlgorithm);
			System.out.println("previousTP: "+previousTP+" previousTfidf: "+previousTfidf+ " previousAlgorithm: "+previousAlgorithm);
			System.out.println("(currentTP - previousTP): "+(currentTP - previousTP));
			System.out.println("(currentTfidf - previousTfidf): "+(currentTfidf - previousTfidf));
			System.out.println("(currentAlgorithm - previousAlgorithm): "+(currentAlgorithm - previousAlgorithm));

			differenceTP = ((currentTP - previousTP) / previousTP)*100*-1;
			differenceTfidf = ((currentTfidf - previousTfidf) / previousTfidf)*100*-1;
			differenceAlgorithm = ((currentAlgorithm - previousAlgorithm) / previousAlgorithm)*100*-1;

			if (differenceTP == -0)
				differenceTP = 0.0;
			if (differenceTfidf == -0)
				differenceTfidf = 0.0;
			if (differenceAlgorithm == -0)
				differenceAlgorithm = 0.0;

			resultArea.append("Improvements - TP: "+df.format(differenceTP)+"% TFIDF: "+df.format(differenceTfidf)+"% Algorithm: "+df.format(differenceAlgorithm)+"%\n");
		}
	}


}

