import java.io.*;

/**
 * Logic that runs the game. Needs a bunch of refactoring, but it works for now.
 * 
 * @author Daniel Szafir
 *
 */
public class Connect4Game
{
	private final String defaultPlayer = "HumanPlayer";

	// The top-level GUI object.
	private final Connect4 gui;

	// State that is transient durina a game.
	private final int STATE_PAUSED = 1;
	private final int STATE_PLAYING = 2;
	private final int STATE_MATCHOVER = 3;
	private int state;
	private int prevMoveCol;
	private int gameNum;
	private int moveNum;
	private int whoseTurn;

	// State that is permanent throughout a game but transient
	// during a match.
	private Connect4Board startingBoard;
	private Connect4Board board;
	private Player p1, p2;
	private String p1HumanName, p2HumanName;
	private String p1ClassFileName, p2ClassFileName;

	// State that is permanent throughout a match.
	private boolean matchPlay;
	private int numGames;
	private int numSeedMoves;
	private int rows, cols;
	private int msecPerMove;
	private String game;
	private int p1GamesWon, p2GamesWon;
	private int p1Points, p2Points;

	// Maintain an epoch counter that is bumped whenever a new match
	// starts. This is helpful for UI control.
	public int epoch;

	public Connect4Game(int rows, int cols, String game, int msecPerMove,
			int numGames, int numRandMoves, boolean matchPlay,
			String p1ClassFileName, String p2ClassFileName, Connect4 gui)
		throws Exception
	{

		assert ((numRandMoves % 2) == 0);
		assert (numRandMoves >= 0);
		assert (numRandMoves <= rows * cols);
		assert (rows >= 4);
		assert (cols >= 4);
		assert (numGames > 0);
		assert (msecPerMove >= 0);
		assert (game.equals("first") || game.equals("most"));
		assert (gui != null);

		this.rows = rows;
		this.cols = cols;
		this.game = game;
		this.msecPerMove = msecPerMove; //250
		this.numGames = numGames;
		this.p1ClassFileName = p1ClassFileName;
		this.p2ClassFileName = p2ClassFileName;

		this.matchPlay = matchPlay;
		this.numSeedMoves = numRandMoves;
		this.startingBoard = newBoard(numSeedMoves);

		if (p1ClassFileName == null) {
			this.p1ClassFileName = defaultPlayer;
		}
		if (p2ClassFileName == null) {
			this.p2ClassFileName = defaultPlayer;
		}

		this.epoch = 0;
		
		this.gui = gui;

		// Open the GUI.
//		gui = Connect4GUI.getInstance("Connect-4", this, rows, cols);
//		gui = new Connect4GUI("Connect-4", this, rows, cols);
//		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		gui.setVisible(true);
//		newMatch();
	}
	
	public int getNumGames()
	{
		return numGames;
	}
	
	public int getNumSeedMoves()
	{
		return numSeedMoves;
	}
	
	public int getTimePerMove()
	{
		return msecPerMove;
	}
	
	public Connect4Board getBoard()
	{
		return board;
	}

	private void updateGUI()
	{
		gui.updateMaxTime(msecPerMove);
		gui.updateGame(gameNum, numGames, numSeedMoves, matchPlay);
		gui.updateTurn(whoseTurn);
		gui.updateMove(moveNum);
		gui.updateNames(p1.name() + " [" + p1.getClass().getName() + "]", p2
				.name()
				+ " [" + p2.getClass().getName() + "]");
		gui.updateBoard(board);
		gui.updateScore(p1GamesWon, p2GamesWon, p1Points, p2Points);
		if (state != STATE_MATCHOVER) {
			status(((whoseTurn == 1) ? p1 : p2).name() + "'s turn"
					+ " (Player #" + whoseTurn + ")");
		}
	}

	public void status(String status)
	{
		System.err.println(status);
		gui.updateStatus(status);
	}

	public boolean isPaused()
	{
		return state == STATE_PAUSED;
	}

	private Connect4Board newBoard(int nmoves)
	{
		Connect4Board b = new Connect4Board(rows, cols);
		java.util.Random rand = new java.util.Random();
		for (int i = 0; i < nmoves; i++) {
			int col = 0;
			do {
				col = rand.nextInt(b.numCols());
			}
			while (!b.isValidMove(col));
			b.move(col, 1 + (i % 2));
		}
		return b;
	}

	public void initBoardAndPlayers()
	{
		// Initialize the board.
		assert (numSeedMoves % 2) == 0;
		if (matchPlay && (gameNum % 2) == 0) {
			startingBoard.reverse();
		}
		else {
			startingBoard = newBoard(numSeedMoves);
		}
		board = new Connect4Board(startingBoard);
		// Initialize the players.
		{
			p1 = null;
			try {
				p1 = getPlayer(p1ClassFileName);
			}
			catch (Exception e) {
				String msg = "Failed to initialize player #1 as '"
						+ p1ClassFileName + "'.\n" + "Using " + defaultPlayer
						+ " instead.";
				status(msg);
				gui.showMessageDialog(msg);
			}
			if (p1 == null) {
				changeP1(defaultPlayer, null);
			}
		}
		{
			p2 = null;
			try {
				p2 = getPlayer(p2ClassFileName);
			}
			catch (Exception e) {
				String msg = "Failed to initialize player #2 as '"
						+ p2ClassFileName + "'.\n" + "Using " + defaultPlayer
						+ " instead.";
				status(msg);
				gui.showMessageDialog(msg);
			}
			if (p2 == null) {
				changeP2(defaultPlayer, null);
			}
		}
		if ((p1 instanceof HumanPlayer) && p1HumanName != null) {
			((HumanPlayer) p1).setName(p1HumanName);
		}
		if ((p2 instanceof HumanPlayer) && p2HumanName != null) {
			((HumanPlayer) p2).setName(p2HumanName);
		}
		p1.init(1, msecPerMove, rows, cols);
		p2.init(2, msecPerMove, rows, cols);
	}

	// Reset things for a new match.
	public void newMatch()
	{
		epoch++;
		state = STATE_PAUSED;
		newGame(true);
	}

	// Reset things for a new game.
	public void newGame(boolean newMatch)
	{
		assert newMatch || gameNum < numGames;
		prevMoveCol = -1;
		gameNum = newMatch ? 1 : (gameNum + 1);
		moveNum = 1;
		whoseTurn = 2 - (gameNum % 2);
		// System.err.println("gameNum="+gameNum+" whoseTurn="+whoseTurn);
		p1Points = p2Points = 0;
		if (newMatch) {
			p1GamesWon = p2GamesWon = 0;
		}
		initBoardAndPlayers();
		updateGUI();
	}

	public void changeP1(String p1ClassFileName, String p1HumanName)
	{
		if (p1ClassFileName == null) return;
		status("Changing player #1 to: " + p1ClassFileName);
		this.p1ClassFileName = p1ClassFileName;
		this.p1HumanName = p1HumanName;
		newMatch();
	}

	public void changeP2(String p2ClassFileName, String p2HumanName)
	{
		if (p2ClassFileName == null) return;
		status("Changing player #2 to: " + p2ClassFileName);
		this.p2ClassFileName = p2ClassFileName;
		this.p2HumanName = p2HumanName;
		newMatch();
	}

	public void changeNGames(int ngames)
	{
		if (this.numGames == ngames) return;
		if (ngames < 1) return;
		status("Changing #games to: " + ngames);
		this.numGames = ngames;
		if (matchPlay) {
			// If we are doing match play, make sure the number of
			// games is even.
			if ((numGames % 2) == 1) numGames++;
		}
		newMatch();
	}

	public void changeMatchPlay(boolean matchPlay)
	{
		if (matchPlay == this.matchPlay) return;
		this.matchPlay = matchPlay;
		status("Match play now: " + (matchPlay ? "on" : "off"));
		// Make sure the number of games is even.
		if ((numGames % 2) == 1) numGames++;
		newMatch();
	}

	public void changeNumRandMoves(int nmoves)
	{
		if (nmoves == this.numSeedMoves) return;
		if (nmoves < 0) return;
		if (nmoves > rows * cols) return;
		if ((nmoves % 2) == 1) nmoves++; // must be even
		status("Changing #rand moves to: " + nmoves);
		this.numSeedMoves = nmoves;
		newMatch();
	}

	public void changeMaxMoveTime(int maxMoveTime)
	{
		if (this.msecPerMove == maxMoveTime) return;
		if (maxMoveTime < 0) return;
		status("Changing max move time to: " + maxMoveTime);
		this.msecPerMove = maxMoveTime;
		newMatch();
	}

	public void pause()
	{
		if (state != STATE_PLAYING) return;
		state = STATE_PAUSED;
	}

	public void moveHuman(int col)
	{
		if (state == STATE_MATCHOVER) return;
		if (whoseTurn == 1 && !(p1 instanceof HumanPlayer)) return;
		if (whoseTurn == 2 && !(p2 instanceof HumanPlayer)) return;
		if (!board.isValidMove(col)) return;
		doMove(col);
		if (state == STATE_PLAYING) {
			gui.pressStepButton(this, epoch);
		}
		updateGUI();
	}

	// Return the id (>0) of the winner for normal connect-4 play.
	// Return 0 if there is no winner.
	// Return -1 if there is more than 1 winner.
	public int calcWinner(Connect4Board board)
	{
		final int rows = board.numRows();
		final int cols = board.numCols();
		int winner = 0;
		// Look for horizontal connect-4s.
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c <= cols - 4; c++) {
				int id = board.get(r, c);
				if (id == 0) continue;
				if (board.get(r, c + 1) != id) continue;
				if (board.get(r, c + 2) != id) continue;
				if (board.get(r, c + 3) != id) continue;
				if (winner != 0 && winner != id) return -1;
				winner = id;
			}
		}
		// Look for vertical connect-4s.
		for (int c = 0; c < cols; c++) {
			for (int r = 0; r <= rows - 4; r++) {
				int id = board.get(r, c);
				if (id == 0) continue;
				if (board.get(r + 1, c) != id) continue;
				if (board.get(r + 2, c) != id) continue;
				if (board.get(r + 3, c) != id) continue;
				if (winner != 0 && winner != id) return -1;
				winner = id;
			}
		}
		// Look for diagonal connect-4s.
		for (int c = 0; c <= cols - 4; c++) {
			for (int r = 0; r <= rows - 4; r++) {
				int id = board.get(r, c);
				if (id == 0) continue;
				if (board.get(r + 1, c + 1) != id) continue;
				if (board.get(r + 2, c + 2) != id) continue;
				if (board.get(r + 3, c + 3) != id) continue;
				if (winner != 0 && winner != id) return -1;
				winner = id;
			}
		}
		for (int c = 0; c <= cols - 4; c++) {
			for (int r = rows - 1; r >= 4 - 1; r--) {
				int id = board.get(r, c);
				if (id == 0) continue;
				if (board.get(r - 1, c + 1) != id) continue;
				if (board.get(r - 2, c + 2) != id) continue;
				if (board.get(r - 3, c + 3) != id) continue;
				if (winner != 0 && winner != id) return -1;
				winner = id;
			}
		}
		return winner;
	}

	// Return the number of connect-4s that player #id has.
	public int calcScore(Connect4Board board, int id)
	{
		final int rows = board.numRows();
		final int cols = board.numCols();
		int score = 0;
		// Look for horizontal connect-4s.
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c <= cols - 4; c++) {
				if (board.get(r, c + 0) != id) continue;
				if (board.get(r, c + 1) != id) continue;
				if (board.get(r, c + 2) != id) continue;
				if (board.get(r, c + 3) != id) continue;
				score++;
			}
		}
		// Look for vertical connect-4s.
		for (int c = 0; c < cols; c++) {
			for (int r = 0; r <= rows - 4; r++) {
				if (board.get(r + 0, c) != id) continue;
				if (board.get(r + 1, c) != id) continue;
				if (board.get(r + 2, c) != id) continue;
				if (board.get(r + 3, c) != id) continue;
				score++;
			}
		}
		// Look for diagonal connect-4s.
		for (int c = 0; c <= cols - 4; c++) {
			for (int r = 0; r <= rows - 4; r++) {
				if (board.get(r + 0, c + 0) != id) continue;
				if (board.get(r + 1, c + 1) != id) continue;
				if (board.get(r + 2, c + 2) != id) continue;
				if (board.get(r + 3, c + 3) != id) continue;
				score++;
			}
		}
		for (int c = 0; c <= cols - 4; c++) {
			for (int r = rows - 1; r >= 4 - 1; r--) {
				if (board.get(r - 0, c + 0) != id) continue;
				if (board.get(r - 1, c + 1) != id) continue;
				if (board.get(r - 2, c + 2) != id) continue;
				if (board.get(r - 3, c + 3) != id) continue;
				score++;
			}
		}
		return score;
	}

	private void doMove(int col)
	{
		assert state == STATE_PAUSED || state == STATE_PLAYING : state;
		assert board.isValidMove(col) : "" + board + col;
		assert whoseTurn == 1 || whoseTurn == 2 : whoseTurn;

		status(((whoseTurn == 1) ? p1 : p2).name() + " (Player #" + whoseTurn
				+ ")" + " moves to column #" + col);
		board.move(col, whoseTurn);

		// Check for a winner.
		boolean gameWon = false;
		if (game.equals("first")) {
			int winner = calcWinner(board);
			if (winner > 0 || board.isFull()) {
				declareWinner(winner);
				gameWon = true;
			}
		}
		else if (game.equals("most")) {
			p1Points = calcScore(board, 1);
			p2Points = calcScore(board, 2);
			if (board.isFull()) {
				if (p1Points == p2Points)
					declareWinner(0);
				else
					declareWinner((p1Points > p2Points) ? 1 : 2);
				gameWon = true;
			}
		}
		else {
			throw new Error("bug: game=" + game);
		}

		if (!gameWon) {
			whoseTurn = (whoseTurn == 1) ? 2 : 1;
			prevMoveCol = col;
			moveNum++;
		}
	}

	public void declareWinner(int id)
	{
		assert id >= 0 && id <= 2;
		assert state != STATE_MATCHOVER : state;
		assert gameNum <= numGames;

		String msg = null;
		switch (id) {
			case 0:
				msg = null;
				break;
			case 1:
				p1GamesWon++;
				msg = p1.name() + " (Player #1)";
				break;
			case 2:
				p2GamesWon++;
				msg = p2.name() + " (Player #2)";
				break;
			default:
				throw new Error("bug: winner=" + id);
		}
		updateGUI();
		gui.announceGameWinner(msg, gameNum, numGames);

		if (gameNum == numGames) {
			state = STATE_MATCHOVER;
			if (p1GamesWon > p2GamesWon) {
				gui.announceMatchWinner(p1.name() + " (Player #1)", p1GamesWon,
						p2GamesWon);
			}
			else {
				gui.announceMatchWinner(p2.name() + " (Player #2)", p2GamesWon,
						p1GamesWon);
			}
		}

		if (gameNum < numGames) {
			newGame(false);
		}
	}

	public void step()
	{
		if (state == STATE_MATCHOVER) return;
		if (whoseTurn == 1 && (p1 instanceof HumanPlayer)) return;
		if (whoseTurn == 2 && (p2 instanceof HumanPlayer)) return;
		if (board.isFull()) return;

		// Let the next player calculate their next move.
		assert whoseTurn == 1 || whoseTurn == 2 : whoseTurn;
		Player[] players = { null, p1, p2 };
		int moveCol = calcMove(board, players[whoseTurn], prevMoveCol,
				msecPerMove);

		// Make sure the move is valid. If it is not, then
		// the other player wins.
		if (!board.isValidMove(moveCol)) {
			status("Player #" + whoseTurn + " (" + players[whoseTurn].name()
					+ ") made an illegal move in col "+moveCol);
			int winner = (whoseTurn == 1) ? 2 : 1;
			declareWinner(winner);
		}
		else {
			// Make the move.
			doMove(moveCol);
		}
		
		updateGUI();

		// Do it this way instead of calling step() so that the
		// user can interact with the UI during play.
		if (state == STATE_PLAYING) {
			gui.pressStepButton(this, epoch);
//			try {
//				Thread.sleep(100); // slow it down a bit to improve animation
//			} catch (InterruptedException e) {}
		}

		updateGUI();
	}

	public void play()
	{
		if (state != STATE_PAUSED) return;
		state = STATE_PLAYING;
		gui.pressStepButton(this, epoch);
	}

	// Given the name of a class that implemements the Player interface,
	// return an instance of that class. Dynamic loading is cool!
	private Player getPlayer(String classFileName)
		throws Exception
	{
		File f = new File(classFileName);
		
		if (f.getParent() != null) {
			// The class loader doesn't seem to respect this.
			// System.setProperty("java.class.path",f.getParent());
		}
		ClassLoader loader = ClassLoader.getSystemClassLoader();
		Class<?> cl = loader.loadClass(f.getName());
//		return (Player) cl.newInstance(); // this call is deprecated, new way to do it is:
		return (Player) cl.getDeclaredConstructor().newInstance();
	}

	// Let player #i calculate their next move. The player is not
	// permitted to spend more than msecAllowed time to do this
	// calculation. If the player takes too much time, then we
	// have it make an illegal move (which forfeits the game).
	private int calcMove(Connect4Board board, Player player, int prevMoveCol,
			int msecPerMove)
	{
		// Start up the player in a separate thread so we are
		// protected from its exceptions and infinite loops.
		Arbitrator arb = new Arbitrator();
		PlayerThread pt = new PlayerThread(player, board, prevMoveCol, arb);
		pt.start();
		long startTime = System.currentTimeMillis();
		Thread.yield();

		// Let the player think for a while.
		int msecAllowed = msecPerMove * 120 / 100;
		int msecPerSpin = msecAllowed / 100;
		msecPerSpin = Math.max(1, msecPerSpin);
		while (true) {
			// If the player is done thinking, then stop spinning.
			if (!pt.isAlive()) break;
			// If the max allowed time has elapsed, then stop spinning.
			if (System.currentTimeMillis() - startTime > msecAllowed) break;
			// Otherwise, spin.
			try {
				Thread.sleep(msecPerSpin);
			}
			catch (InterruptedException e) {
			}
		}

		// Notify the player thread that time is up. If the player
		// is already done calculating its next move, then this has
		// no effect. If the player is still working, then its
		// next call to checkTime() will cause it to stop.
		arb.timeUp();

		// Give the player thread time to stop.
		for (int i = 0; i < 5; i++) {
			Thread.yield();
			if (pt.isAlive()) {
				try {
					Thread.sleep(100);
				}
				catch (InterruptedException e) {
				}
			}
		}

		// If the player thread has still not exited, then disqualify it by
		// returning an invalid move; otherwise, return whatever move it
		// calculated.
		if (pt.isAlive()) {
			status("Player #" + whoseTurn + " (" + player.name()
					+ ") is disqualified for taking too much time.");
			return -1;
		}
		else {
			return arb.getMove();
		}
	}
}
