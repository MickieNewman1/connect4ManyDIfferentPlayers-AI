
/**
 * This class is a player that uses a minimax algorithum
 * this uses a binary search tree to look into possible moves and choose the best move for the player
 * it also tries to minimize the gains of the other player while at the same time maximizing its own score.
 * 
 * @author Mickie Newman
 */


import java.util.ArrayList;

public class MinimaxPlayer implements Player {
	
	private int id;
	private int enemyid;
	private int cols;
	/**
	 * sets name of the player
	 * 
	 * @return string the players name
	 */
	public String name() {
		return "miniMax";
	}
	/**
	 * init: sets the id and enemy id and defines columns and rows.
	 * 
	 * @param id The id of the player
	 * @param msecPerMove miliseconds per move
	 * @param row number of rows in the connect 4 board
	 * @param cols number of collumns in the connect 4 board
	 */
	public void init(int id, int msecPerMove, int rows, int cols) {
		this.id = id;
		this.enemyid = 3 - id; //1 or 2 -> if my id is 1 -> opp id 2 vice versa
		this.cols = cols;
	}
	/**
	 * calcMove method. it tests if it is ok to make a move by checking if the board is full 
	 * and if a column is free to move to.
	 * it then runs all the possible moves and unmoves them
	 * it figures out best move for the player by calling the minimax method
	 * and then executes the move 
	 * 
	 * @param board a Connect4 object the board configuration of the connect4 board
	 * @param oppMoveCol an int the collumn of your opponents most recent move
	 * @param arb an arbitrator object
	 */
	public void calcMove(Connect4Board board, int oppMoveCol, Arbitrator arb) throws TimeUpException{
		if(board.isFull())
			throw new Error("Error: the board is full!!!");
		
		GameTree root = new GameTree(-1, board);
		
		for(int i = 0; i < cols; i++) {
			if(!board.isColumnFull(i)) {//if collumn is not full consider that move
				board.move(i, id);
				root.addChild(i, new Connect4Board(board));
				board.unmove(i, id);
			}
		}
		
		//initialize a maximum search depth to be 1
		int searchDepth = 1;
		// while there is time remaining to calculate your move(you can check this with  the arb.isTimeUp() method) and your current search depth is <= the number of moves remaining (you can check this with board.numEmptyCells() method): 
		while(!arb.isTimeUp() && searchDepth <= board.numEmptyCells()) {
			
		// do a minimax search to the depth of your maximum search variable 
			minimax(root, searchDepth, true, arb);// investigate if the boolean is supposed to alwayse be true
		// set your move as the best move found so far
			arb.setMove(root.chosenMove);
		// increment your maximum search depth
			searchDepth++;
			}
	}
	/**
	 * minimax: this looks at all the possible future moves by looking at future moves of both
	 * the player and the enemy through a binary search tree  
	 * it returns the value of the best possible move
	 *   
	 * 
	 * @param node a gametree object these are the children moves we need to check if they are the best option
	 * @param depth an int the search depth
	 * @param maxminimizingPlayer a boolean to determine if we need to max or min the value based on which player is going 
	 * @param arb an Arbitrator object
	 * @return value an int that is the value of the best possible move.
	 */
	private int minimax(GameTree node, int depth, boolean maxminimizingPlayer, Arbitrator arb) {
		
		if(depth == 0 || node.isTerminal() || arb.isTimeUp()) {
			node.value = evaluateNode(node);
			return node.value;
		}
		
		if(node.isLeaf()){
			
			//int moveId;
			//if(maxminimizingPlayer)
				//moveId= id;
			//else
				//moveId = enemyid;
			
			int moveId = maxminimizingPlayer ? id : enemyid;// will change if minimaximizing player is true or false depending on the player id and sets that to move id
			
			//checks to see if any column is full if not it considers the move
			for(int i = 0; i < cols; i++){
				if(!node.board.isColumnFull(i)){
					node.board.move(i , moveId);
					node.addChild(i, new Connect4Board(node.board));
					node.board.unmove(i, moveId);
					}
				}
			}
		//if its the player's turn
		if(maxminimizingPlayer) {
			int value = Integer.MIN_VALUE;
			for(GameTree child: node.children) {
				int newVal = minimax(child, depth - 1, false, arb);//finds value of child
				if(newVal > value) {// compares the values of all the children
					value = newVal;
					node.value = value;
					node.chosenMove = child.move;
					
				}
				else if (newVal == value) {
					int currMoveDistFromCenter = Math.abs(cols/2 -node.chosenMove);
					int newMoveDistFromCenter = Math.abs(cols/2 - child.move);
					if(newMoveDistFromCenter < currMoveDistFromCenter) 
						node.chosenMove = child.move;
				}
			}
			return value;
		}
		//if its the opponents's turn
		else {
			int value = Integer.MAX_VALUE;
			for(GameTree child: node.children) {
				int newVal = minimax(child, depth - 1, true, arb);//finds value of child
				if(newVal < value) {// compares the values of all the children 
					value = newVal;
					node.value = value;
					node.chosenMove = child.move;
				}
				else if (newVal == value) {
					int currMoveDistFromCenter = Math.abs(cols/2 -node.chosenMove);
					int newMoveDistFromCenter = Math.abs(cols/2 - child.move);
					if(newMoveDistFromCenter < currMoveDistFromCenter) 
						node.chosenMove = child.move;
				}
			}
			return value;
		}
		
		
		}
	/**
	 * evaluateNode method. this determines how well the player is doing against the opponent 
	 * by getting the score if you played that particular move and your enemys score if you
	 * played that move. it then compares the scores by subtracting your score from the enemys score
	 * and returns the difference.
	 *   
	 * 
	 * @param node a gametree object the last move the player did
	 */
	private int evaluateNode(GameTree node) {
		int myScore = calcScore(node.board, id);
		int oppScore = calcScore(node.board,enemyid);
		return myScore - oppScore;
		
	}

	/**
	 * calcScore: this figures out if you scored a point by finding how many times 
	 * there are 4 player moves that are right next to each other in a line, either horizontally
	 * virtecally or diagonally. it then adds up all the occurances and returns the total score
	 *   
	 * 
	 * @param board a Connect4Board object that shows the configuration of the board
	 * @param id an integer that determines who the player is
	 * @return score an int that is the total number of times the player has 4 tiles in a row
	 */
	public int calcScore(Connect4Board board, int id) {
		
		final int rows = board.numRows();
		final int cols = board.numCols();
		int score = 0;		
		
		//horizontal
		for(int r = 0; r < rows; r++) {
			for(int c = 0; c <= cols - 4; c++) {
				if(board.get(r, c + 0) != id) continue;
				if(board.get(r, c + 1) != id) continue;
				if(board.get(r, c + 2) != id) continue;
				if(board.get(r, c + 3) != id) continue;
				score++;
		
			}
		}
		//vertical 
		for(int c = 0; c < cols; c++) {
			for(int r = 0; r <= rows - 4; r++) {
				if(board.get(r + 0, c) != id)continue;
				if(board.get(r + 1, c) != id)continue;
				if(board.get(r + 2, c) != id)continue;
				if(board.get(r + 3, c) != id)continue;
				score++;
				
				
			}
		}
		//diagonal 
		for(int c = 0; c <= cols - 4; c++) {
			for(int r = 0; r <= rows - 4; r++) {
				if(board.get(r + 0, c + 0) != id)continue;
				if(board.get(r + 1, c + 1) != id)continue;
				if(board.get(r + 2, c + 2) != id)continue;
				if(board.get(r + 3, c + 3) != id)continue;
				score++;
				
			}
		}
		for(int c = 0; c <= cols - 4; c++) {
			for(int r = rows - 1; r >= 4 - 1; r--) {
				if(board.get(r - 0, c + 0) != id)continue;
				if(board.get(r - 1, c + 1) != id)continue;
				if(board.get(r - 2, c + 2) != id)continue;
				if(board.get(r - 3, c + 3) != id)continue;
				score++;
			}
		}
		return score;
	}
// creates an object called GameTree it is a binary search tree of possible moves
	private class GameTree{
		private Connect4Board board;
		private int move;
		private ArrayList<GameTree> children; 
		private int chosenMove;
		private int value;
		/**
		 * GameTree constructor. creates a game tree and puts the different nodes 
		 * into an array called children 
		 * the nodes consist of the the move and the board configuration of the move 
		 * 
		 * @param move : the move number of the current node
		 * @param board : the board configuration of that move 
		 */
		public GameTree(int move, Connect4Board board) {
			this.move = move;
			this.board = board;
			children = new ArrayList<GameTree>();
		}
		/**
		 * this adds a new node to the gametree array 
		 * 
		 * @return a new node within the array children 
		 */
		public void addChild(int move, Connect4Board board) {
			children.add(new GameTree(move, board));
			
		}
		/**
		 * whether or not the node is a leaf if it has no children.
		 * 
		 * @return True if the node is a leaf 
		 */
		public boolean isLeaf() {
			return children.size() == 0;
		}
		/**
		 * whether or not the board is full 
		 * 
		 * @return True if the board is full
		 */
		public boolean isTerminal() {
			return board.isFull();
		}
	}

	
}
