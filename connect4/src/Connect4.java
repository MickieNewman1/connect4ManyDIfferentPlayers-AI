/* *****************************************************************************
 * Title:            Connect4
 * Files:            Connect4.java, Connect4Game.java, Connect4Board.java,
 * 					 Arbitrator.java, TimeUpException.java, PlayerThread.java, Player.java
 * 					 HumanPlayer.java, RandomOlayer.java, [OtherPlayers].java
 *
 * 
 * Author:           Daniel Szafir, daniel.szafir@colorado.edu
 * 
 * Description:		 An application that allows humans and AI agents to play Connect 4
 * 					 and a Connect 4 variant where the game goes on until the board is
 * 				     completely full and the winner is the player with the most Connect 4s.
 * 
 * Written:       	 4/20/2020
 * 
 * Credits:          Based on code originally by David Martin, 2004
 **************************************************************************** */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.List;
import java.util.Optional;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;


// TODO:
// - The whole codebase should be refactored as the logic between Connect4.java and Connect4Game.java is extremely
//   out of date and confusing. Right now, Connect4Game.java runs Connect4.java since Connect4.java now owns the GUI,
//   but we should update all of this.
// - Add a log window to the game that shows game results (DQ,score,etc.)
// - Stop the match when the losing player can't catch up
// - Allow an odd number of seed moves

/**
 * Connect 4 main application.
 * 
 * @author Daniel Szafir
 *
 */
public class Connect4 extends Application
{	
	// Application width and height, don't modify
	public static final int WIDTH = 800, HEIGHT = 600;

	// Status labels
	private Label statusLabel, moveLabel, gameLabel, turnLabel;
	private Circle p1Circle, p2Circle;

	// Options
	private CheckBox announceWinnerCheckBox, matchPlayCheckBox;
	private Spinner<Integer> numGamesSpinner, numSeedMovesSpinner, maxMoveTimeSpinner;
	private int numGames, seedMoves, moveTime;

	// Game buttons
	private Button newMatchButton, stepButton, pauseButton, autoplayButton;

	// Drawing canvas to show the game
	private Canvas canvas;

	// Choose player controls
	private Button changePlayer1Button, changePlayer2Button;
	private Label p1Label, p2Label;

	// Scoreboard labels
	private Label p1GamesLabel, p1ScoreLabel, p2GamesLabel, p2ScoreLabel;
	
	// Speed slider
	Slider speedSlider;

	// Reference to the underlying game object that keeps track of game logic
	private Connect4Game c4Game;

	private Connect4Board board;
	
	private int mouseHoveringCol;
	private boolean isMouseHovering;

	// Print program usage and exit.
	private static void usage() {
		System.err.println();
		System.err.println("usage: Connect4 [options]");
		System.err.println();
		System.err.println("    r - Number of rows, >= 4.");
		System.err.println("    c - Number of columns, >= 4.");
		System.err.println("    n - Number of games per match.");
		System.err.println("    t - Time allowed per move (msec).");
		System.err.println("   p1 - Class name for player #1.");
		System.err.println("   p2 - Class name for player #2.");
		System.err.println("   mp - Match play?  One of {on,off}.");
		System.err.println("   nr - Number of random moves, even, >=0, <=rows*cols.");
		System.err.println(" game - Which game to play:");
		System.err.println("        first - First Connect-4 wins.");
		System.err.println("         most - Most Connect-4s wins.");
		System.err.println();
		System.err.println("The player classes must be in CLASSPATH.");
		System.err.println();
		System.exit(1);
	}

	private Connect4Game parseArgs() throws Exception
	{
		int rows = 6;
		int cols = 7;
		String game = "most";
		int msec = 250;//1000;
		int ngames = 1;
		int numRandMoves = 0;
		boolean matchPlay = false;
		String p1 = null;
		String p2 = null;

		List<String> args = getParameters().getRaw();

		for (int i=0; i<args.size(); i++) {
			if (args.get(i).equals("-r")) {
				if (++i == args.size()) usage();
				rows = Integer.parseInt(args.get(i));
				if (rows < 4) usage();
				continue;
			}
			if (args.get(i).equals("-c")) {
				if (++i == args.size()) usage();
				cols = Integer.parseInt(args.get(i));
				if (cols < 4) usage();
				continue;
			}
			if (args.get(i).equals("-nr")) {
				if (++i == args.size()) usage();
				numRandMoves = Integer.parseInt(args.get(i));
				if (numRandMoves < 0) usage();
				if ((numRandMoves % 2) == 1) usage();
				continue;
			}
			if (args.get(i).equals("-game")) {
				if (++i == args.size()) usage();
				game = args.get(i);
				if (!game.equals("first") && !game.equals("most")) usage();
				continue;
			}
			if (args.get(i).equals("-mp")) {
				if (++i == args.size()) usage();
				if (!args.get(i).equals("on") 
						&& !args.get(i).equals("off")) usage();
				matchPlay = args.get(i).equals("on");
				continue;
			}
			if (args.get(i).equals("-t")) {
				if (++i == args.size()) usage();
				msec = Integer.parseInt(args.get(i));
				if (msec < 0) usage();
				continue;
			}
			if (args.get(i).equals("-p1")) {
				if (++i == args.size()) usage();
				p1 = args.get(i);
				continue;
			}
			if (args.get(i).equals("-p2")) {
				if (++i == args.size()) usage();
				p2 = args.get(i);
				continue;
			}
			if (args.get(i).equals("-n")) {
				if (++i == args.size()) usage();
				ngames = Integer.parseInt(args.get(i));
				if (ngames < 1) usage();
				continue;
			}
			usage();
		}
		if (numRandMoves > rows*cols) usage();

		return new Connect4Game (rows, cols, game, msec, ngames, numRandMoves, 
				matchPlay, p1, p2, this);
	}

	public static final void main (String[] args) throws Exception {

		launch(args);
		//        new Connect4Game (
		//            rows, cols, game, msec, ngames, numRandMoves, 
		//            matchPlay, p1, p2);
	}

	@Override
	public void start(Stage stage) throws Exception {

		// Set up the JavaFX stage
		stage.setTitle("Connect-4");

		// Top level container
		Group root = new Group();

		// Border pane that will go into the root group. Allows us to organize components
		// on the top, center, and bottom of the application
		BorderPane borderPane = new BorderPane();
		borderPane.setId("borderPane");

		// Set up top buttons and labels:
		statusLabel = new Label("Status:");

		announceWinnerCheckBox = new CheckBox("Announce Game Winner");
		matchPlayCheckBox = new CheckBox("Match Play");
		moveLabel = new Label("Move #1");
		gameLabel = new Label("Game #1");

		p1Circle = new Circle(10, Color.RED);
		p2Circle = new Circle(10, Color.BLUE);
		turnLabel = new Label("Turn: ", p1Circle);
		turnLabel.setId("turnLabel");

		newMatchButton = new Button("New Match");
		newMatchButton.setOnAction(actionEvent -> {
			c4Game.newMatch();
		});


		stepButton = new Button("Step");
		stepButton.setOnAction(actionEvent -> {
			c4Game.step();
		});


		pauseButton = new Button("Pause");
		pauseButton.setOnAction(actionEvent -> {
			c4Game.pause();
		});


		autoplayButton = new Button("Auto-Play");
		autoplayButton.setOnAction(actionEvent -> {
			c4Game.play();
		});

		changePlayer1Button = new Button("Change");
		changePlayer1Button.setOnAction(actionEvent -> {
			FileChooser fileChooser = new FileChooser();
			
			// Really we will be loading the compiled class file, but looking at .class files might confuse
			// students so we will have them select the corresponding .java src file instead of looking in ./bin/
			fileChooser.setInitialDirectory(new File("./src/"));
			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Java files (*.java)", "*.java");
			fileChooser.getExtensionFilters().add(extFilter);
			File selectedFile = fileChooser.showOpenDialog(stage);
			
			if (selectedFile != null) {
				String className = selectedFile.getName();
				className = className.substring(0, className.lastIndexOf(".")); //strip the .java ending
			
				String name = null;
				if (className.contentEquals("HumanPlayer"))
				{
					TextInputDialog dialog = new TextInputDialog("name");
					dialog.setTitle("Enter name");
					dialog.setHeaderText(null);
					dialog.setContentText("Please enter a name for the human player:");
					Optional<String> result = dialog.showAndWait();
					if (result.isPresent()) name = result.get();
					if (name.length() > 12)
					{
						name = name.substring(0, 12) + "...";
					}
				}

				if (selectedFile != null) {
					c4Game.changeP1(className, name);
				}
			}

		});
		p1Label = new Label("Player #1:");
		p1Label.setTextFill(Color.RED);

		changePlayer2Button = new Button("Change");
		changePlayer2Button.setOnAction(actionEvent -> {
			FileChooser fileChooser = new FileChooser();
			
			// Really we will be loading the compiled class file, but looking at .class files might confuse
			// students so we will have them select the corresponding .java src file instead of looking in ./bin/
			fileChooser.setInitialDirectory(new File("./src/"));
			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Java files (*.java)", "*.java");
			fileChooser.getExtensionFilters().add(extFilter);
			File selectedFile = fileChooser.showOpenDialog(stage);
			
			if (selectedFile != null) {
				String className = selectedFile.getName();
				className = className.substring(0, className.lastIndexOf(".")); //strip the .java ending
			
				String name = null;
				if (className.contentEquals("HumanPlayer"))
				{
					TextInputDialog dialog = new TextInputDialog("name");
					dialog.setTitle("Enter name");
					dialog.setHeaderText(null);
					dialog.setContentText("Please enter a name for the human player:");
					Optional<String> result = dialog.showAndWait();
					if (result.isPresent()) name = result.get();
					if (name.length() > 12)
					{
						name = name.substring(0, 12) + "...";
					}
				}

				if (selectedFile != null) {
					c4Game.changeP2(className, name);
				}
			}
		});
		p2Label = new Label("Player #2:");
		p2Label.setTextFill(Color.BLUE);

		numGames = 1;//c4Game.getNumGames();
		numGamesSpinner = new Spinner<Integer>(1, 25, numGames);
		numGamesSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
			numGames = newValue;
			c4Game.changeNGames(numGames);
		});
		numGamesSpinner.setEditable(true);
		NumberFormat format = NumberFormat.getIntegerInstance();
		TextFormatter<Integer> formatter = new TextFormatter<Integer>(new IntegerStringConverter(), numGames, 	
				c -> {
					if (c.isContentChange()) {
						ParsePosition parsePosition = new ParsePosition(0);
						// NumberFormat evaluates the beginning of the text
						format.parse(c.getControlNewText(), parsePosition);
						if (parsePosition.getIndex() == 0 ||
								parsePosition.getIndex() < c.getControlNewText().length()) {
							// reject parsing the complete text failed
							return null;
						}
					}
					return c;
				});
		numGamesSpinner.getEditor().setTextFormatter(formatter);
		numGamesSpinner.setId("spinner");

		seedMoves = 0;//c4Game.getNumSeedMoves();
		numSeedMovesSpinner = new Spinner<Integer>(0, 10, seedMoves);
		numSeedMovesSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
			// only allow even numbers of seed moves
			if ((newValue % 2) == 1) {
				if (newValue > oldValue) newValue++; 
				else newValue--;
			}
			seedMoves = newValue;
			c4Game.changeNumRandMoves(seedMoves);
			numSeedMovesSpinner.getValueFactory().setValue(seedMoves);
		});
		numSeedMovesSpinner.setEditable(true);
		TextFormatter<Integer> formatter2 = new TextFormatter<Integer>(new IntegerStringConverter(), seedMoves, 	
				c -> {
					if (c.isContentChange()) {
						ParsePosition parsePosition = new ParsePosition(0);
						// NumberFormat evaluates the beginning of the text
						format.parse(c.getControlNewText(), parsePosition);
						if (parsePosition.getIndex() == 0 ||
								parsePosition.getIndex() < c.getControlNewText().length()) {
							// reject parsing the complete text failed
							return null;
						}
					}
					return c;
				});
		numSeedMovesSpinner.getEditor().setTextFormatter(formatter2);
		numSeedMovesSpinner.setId("spinner");

		moveTime = 250;//c4Game.getTimePerMove();
		maxMoveTimeSpinner = new Spinner<Integer>(100, 1000, moveTime);
		maxMoveTimeSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
			moveTime = newValue;
			c4Game.changeMaxMoveTime(moveTime);
		});
		maxMoveTimeSpinner.setEditable(true);
		TextFormatter<Integer> formatter3 = new TextFormatter<Integer>(new IntegerStringConverter(), moveTime, 	
				c -> {
					if (c.isContentChange()) {
						ParsePosition parsePosition = new ParsePosition(0);
						// NumberFormat evaluates the beginning of the text
						format.parse(c.getControlNewText(), parsePosition);
						if (parsePosition.getIndex() == 0 ||
								parsePosition.getIndex() < c.getControlNewText().length()) {
							// reject parsing the complete text failed
							return null;
						}
					}
					return c;
				});
		maxMoveTimeSpinner.getEditor().setTextFormatter(formatter3);
		maxMoveTimeSpinner.setId("spinner");		

		p1GamesLabel = new Label("0");
		p1ScoreLabel = new Label("0");
		p1GamesLabel.setTextFill(Color.RED);
		p1ScoreLabel.setTextFill(Color.RED);
		p2GamesLabel = new Label("0");
		p2ScoreLabel = new Label("0");
		p2GamesLabel.setTextFill(Color.BLUE);
		p2ScoreLabel.setTextFill(Color.BLUE);
		
		speedSlider = new Slider(0, 1, 0.5);

		// Setup scoreboard
		HBox scoreBoard = new HBox();
		scoreBoard.setId("scoreboard");
		VBox leftVbox = new VBox(new Label("Games"), p1GamesLabel, p2GamesLabel);
		leftVbox.setAlignment(Pos.CENTER);
		VBox rightVbox = new VBox(new Label("Score"), p1ScoreLabel, p2ScoreLabel);
		rightVbox.setAlignment(Pos.CENTER);
		scoreBoard.getChildren().addAll(leftVbox, rightVbox);
		scoreBoard.setPrefWidth(150);

		// Set up a grid to hold the labels and buttons along the top of the application
		GridPane topGrid = new GridPane();
		topGrid.setId("grid");
		HBox statusBox = new HBox(new Label("Status: "), statusLabel);
		topGrid.add(statusBox, 0, 0);
		GridPane.setColumnSpan(statusBox, 5);
		topGrid.add(announceWinnerCheckBox, 0, 1);
		topGrid.add(matchPlayCheckBox, 1, 1);
		topGrid.add(new HBox(new Label("Move #"), moveLabel), 2, 1);
		topGrid.add(new HBox(new Label("Game #"), gameLabel), 3, 1);
		topGrid.add(turnLabel, 4, 1);

		topGrid.add(scoreBoard, 5, 1);
		GridPane.setRowSpan(scoreBoard, 2);

		topGrid.add(newMatchButton, 0, 2);
		topGrid.add(stepButton, 1, 2);
		topGrid.add(pauseButton, 2, 2);
		topGrid.add(autoplayButton, 3, 2);
		
//		HBox p1box = new HBox(changePlayer1Button, new Label("Player #1: "), p1Label);
//		p1box.setId("hbox");
//		HBox p2box = new HBox(changePlayer2Button, new Label("Player #2: "), p2Label);
//		p2box.setId("hbox");
//		topGrid.add(p1box, 0, 3);
//		topGrid.add(p2box, 0, 4);
//		GridPane.setColumnSpan(p1box, 2);
//		GridPane.setColumnSpan(p2box, 2);



		// Set up left grid of options
		GridPane leftGrid = new GridPane();
		leftGrid.setId("leftgrid");
		leftGrid.add(new Label("Number of Games:"), 0, 2);
		leftGrid.add(numGamesSpinner, 1, 2);
		leftGrid.add(new Label("Number of Seed Moves:"), 0, 3);
		leftGrid.add(numSeedMovesSpinner, 1, 3);
		leftGrid.add(new Label("Max Move Time (ms):"), 0, 4);
		leftGrid.add(maxMoveTimeSpinner, 1, 4);

		HBox p1box = new HBox(changePlayer1Button, new Label("Player #1: "), p1Label);
		p1box.setId("hbox");
		HBox p2box = new HBox(changePlayer2Button, new Label("Player #2: "), p2Label);
		p2box.setId("hbox");
		leftGrid.add(p1box, 0, 5);
		leftGrid.add(p2box, 0, 6);
		GridPane.setColumnSpan(p1box, 2);
		GridPane.setColumnSpan(p2box, 2);
//		leftGrid.getColumnConstraints().add(new ColumnConstraints(200));
		
		HBox speedBox = new HBox(new Label("Speed (fast - slow): "), speedSlider);
		speedBox.setId("hbox");
		leftGrid.add(speedBox, 0, 7);
		GridPane.setColumnSpan(speedBox, 2);
		

		// Set up a canvas for drawing the game
		canvas = new Canvas(400, 400);
		canvas.setId("canvas");
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.strokeRect(0, 0, canvas.getWidth(), canvas.getHeight()); // draw a 1px black border around the canvas 


		// Add the top grid of labels and buttons to the top of the application
		borderPane.setTop(topGrid);
		//		Insets borderMargin = new Insets(20);
		//		BorderPane.setMargin(topGrid, borderMargin);

		// Add left grid of options
		borderPane.setLeft(leftGrid);
		//		BorderPane.setAlignment(leftGrid, Pos.TOP_CENTER);
		//		BorderPane.setMargin(leftGrid, borderMargin);

		// Add the canvas to the center of the application
		borderPane.setCenter(canvas);
		BorderPane.setAlignment(canvas, Pos.TOP_CENTER);
		canvas.setOnMouseClicked(e -> {
			double width = canvas.getWidth();
			double columnWidth = width / board.numCols();
			int col = (int) e.getX() / (int) columnWidth; // integer division tells us which column was clicked on
			c4Game.moveHuman(col);
		});
		canvas.setOnMouseMoved(e -> {
			isMouseHovering = true;
			double width = canvas.getWidth();
			double columnWidth = width / board.numCols();
			mouseHoveringCol = (int) e.getX() / (int) columnWidth; // integer division tells us the column
			drawBoard();
		});
		canvas.setOnMouseExited(e -> {
			isMouseHovering = false;
			drawBoard();
		});
		
		//BorderPane.setMargin(canvas, borderMargin);

		// Package everything up and show the scene
		borderPane.setPrefSize(WIDTH, HEIGHT); // Needed to center everything in the bP since the bP is in the root group
		root.getChildren().add(borderPane);
		Scene scene = new Scene(root, WIDTH, HEIGHT);
		scene.getStylesheets().add("connect4.css");
		stage.setScene(scene);
		stage.show();
		stage.setResizable(false);

		// Parse command line arguments to create the game object
		c4Game = parseArgs();
		c4Game.newMatch();
		board = c4Game.getBoard();

	}

	public void updateMaxTime(int maxTime) {
		maxMoveTimeSpinner.getValueFactory().setValue(maxTime);
	}

	public void updateTurn(int player) {
		if (player == 1) {
			turnLabel.setGraphic(p1Circle);
		}
		else {
			turnLabel.setGraphic(p2Circle);
		}
	}

	public void updateMove(int move) {
		moveLabel.setText("" + move);
	}

	public void updateNames(String p1Name, String p2Name) {
		p1Label.setText(p1Name);
		p2Label.setText(p2Name);
	}

	public void updateScore(int p1GamesWon, int p2GamesWon, int p1Points, int p2Points) {
		p1GamesLabel.setText("" + p1GamesWon);
		p2GamesLabel.setText("" + p2GamesWon);
		p1ScoreLabel.setText("" + p1Points);
		p2ScoreLabel.setText("" + p2Points);
	}

	public void updateStatus(String status) {
		statusLabel.setText((c4Game.isPaused() ? "[paused] " : "") + status);
	}

	public void updateGame(int gameNum, int numGames, int numSeedMoves, boolean matchPlay) {
				gameLabel.setText(""+gameNum);
		//		numGamesSpinner.getValueFactory().setValue(numGames);
		//		numSeedMovesSpinner.getValueFactory().setValue(numSeedMoves);
		//		matchPlayCheckBox.setSelected(matchPlay);
	}

	public void updateBoard(Connect4Board board) {
		this.board = board;
		drawBoard();
	}

	private void drawBoard()
	{
		GraphicsContext gc = canvas.getGraphicsContext2D();

		double width = canvas.getWidth();
		double height = canvas.getHeight();

		// Clear the drawing
		gc.setFill(Color.GREY);
		gc.fillRect(0, 0, width, height);

		int numCells = board.numCells();
		int rows = board.numRows();
		double rowHeight = height / rows;

		int cols = board.numCols();
		double colWidth = width / cols;

		gc.setStroke(Color.BLACK);
		for (int x = 0; x < cols; x++) {
			gc.strokeLine(x * colWidth, 0, x*colWidth, height);
		}

		for (int y = 0; y < rows; y++) {
			gc.strokeLine(0, y*rowHeight, width, y*rowHeight);
		}

		gc.setFill(Color.BLACK);
		double cellDiameter = Math.min(rowHeight, colWidth);
		cellDiameter *= .75; //give some padding
		// draw from bottom up, left to right
		gc.save();
		gc.translate(0, height);
		gc.scale(1, -1);
		for (int x = 0; x < cols; x++) {
			for (int y = 0; y < rows; y++) {

				switch(board.get(y, x))
				{
				case 0: 
					if (isMouseHovering && x == mouseHoveringCol) gc.setFill(Color.DARKGREY);
					else gc.setFill(Color.BLACK);
				break;
				case 1: gc.setFill(Color.RED);
				break;
				case 2: gc.setFill(Color.BLUE);
				}

				double centerX = x*colWidth + colWidth/2;
				double centerY = y*rowHeight + rowHeight/2;

				gc.fillOval(centerX - cellDiameter/2, centerY - cellDiameter/2, cellDiameter, cellDiameter);
			}
		}
		gc.restore();
	}

	public void showMessageDialog(String msg) {
		Alert a = new Alert(AlertType.INFORMATION);
		a.setContentText(msg);
		a.setHeaderText(null);
//		a.show();
		a.showAndWait();
	}

	public void pressStepButton(Connect4Game connect4Game, int epoch) {
//		stepButton.fire();
		// Can't just do fire as the fires just queue up events that could happen any time so it won't draw it in progress
		// instead need to make sure we actually do a step and draw rather than just say ok, queue up an event
		
		ActionListener task = new ActionListener() {
			public void actionPerformed(ActionEvent evt)
			{
				if (epoch != c4Game.epoch) return;
				Platform.runLater(() -> {
					stepButton.fire();
				});
//				c4Game.step();
			}
		};
		int delay = (int) (speedSlider.getValue() * 500);
		javax.swing.Timer t = new javax.swing.Timer(delay, task); // change 100 to 0 to speed things up (or increase to slow down)
		t.setRepeats(false);
		t.start();
	}

	public void announceGameWinner(String name, int gameNum, int numGames) {
		if (announceWinnerCheckBox.isSelected()) {
			String msg;
			if (name == null) {
				msg = "Game " + gameNum + " of " + numGames + " is a draw.";
			}
			else {
				msg = name + " wins game " + gameNum + " of " + numGames + ".";
			}
			c4Game.status(msg);
			showMessageDialog(msg);
		}
	}

	public void announceMatchWinner(String name, int nwon, int nlost) {
		String msg;
		if (nwon == nlost) {
			msg = "The match is a draw: " + nwon + " games each.";
		}
		else {
			msg = name + " wins the match " + nwon + " games to " + nlost + ".";
		}
		c4Game.status(msg);
		showMessageDialog(msg);
	}
}

