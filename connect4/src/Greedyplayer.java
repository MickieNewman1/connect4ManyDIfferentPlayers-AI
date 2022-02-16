/**
 * This class is a basic player whos only goal is to get more points.
 * its moves are based on which move is a higher value and will get them closer to scoring a point
 * it doesnt take into consideration the other players moves only to the extent that it doesnt 
 * move into spaces occupied by the enemy and it doesnt consider future moves.
 * 
 * @author (Mickie Newman)
 */
public class Greedyplayer implements Player {
	private int id;
	private int enemyid;
	private int cols;
	
	private Move[] possibleMoves;// array of possible moves
	/**
	 * sets name of the player
	 * 
	 * @return string the players name
	 */
	public String name() {
		return "Greedo";
		
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
		this.enemyid = 3 - id;
		this.cols = cols;
		
	}
	/**
	 * calcMove method. it tests if it is ok to make a move by checking if the board is full 
	 * and if a column is free to move to.
	 * it then runs all the possible moves and unmoves them
	 * it figures out which move to do by finding the value of the moves available to it 
	 * and then choosing the move with the highest value
	 * and then executes the move 
	 * 
	 * @param board a Connect4 object the board configuration of the connect4 board
	 * @param oppMoveCol an int the collumn of your opponents most recent move
	 * @param arb an arbitrator object
	 */
	public void calcMove(Connect4Board board, int oppMoveCol, Arbitrator arb) throws TimeUpException{
		if(board.isFull()) 
			throw new Error("error: The board is full!");
		//consider all possible moves the Ai could make	
		possibleMoves = new Move[cols];
		//for each move:
		for(int c = 0; c < cols; c++) {
			if(board.isValidMove(c)) {//if collumn is not full consider that move
			//temporarily make the move using board.move()
				board.move(c, id);
			//calculates a score based on how the board is for you now that youve made the move
				int moveValue = evaluateBoard(board, id, enemyid);
				possibleMoves[c]= new Move(c, moveValue);
			//undoes the move using board.unmove
				board.unmove(c, id);
				}
		}
		Move bestMove = null;
		//return the move that had the highest calculated score
		for(int i = 0; i < possibleMoves.length; i++) {
			if(bestMove == null) {
				bestMove = possibleMoves[i];
			}
			else if(possibleMoves[i]!= null && bestMove.compareTo(possibleMoves[i]) < 0) {
				bestMove = possibleMoves[i];
			}
		}
		arb.setMove(bestMove.colunm);
		
	}
	/**
	 * evaluateBoard: this determines how well the player is doing against the opponent 
	 * by getting the score if you played that particular move and your enemys score if you
	 * played that move. it then compares the scores by subtracting your score from the enemys score
	 * and returns the difference.
	 * 
	 * @param board a Connect4 object the board configuration of the connect4 board
	 * @param myid an int that determines which person is playing
	 * @param enemyid an int that determines which person is playing
	 */
	
	private int evaluateBoard(Connect4Board board, int myid, int enemyid) {
		int myScore = calcScore(board, myid);
		int oppScore = calcScore(board,enemyid);
		
		return myScore - oppScore;
	}
	
	
	
	/**
	 * calcScore : this figures out if you scored a point by finding how many times 
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
		for(int r=0; r<rows; r++) {
			for(int c=0; c<=cols - 4; c++) {
				if(board.get(r, c+0) != id) continue;
				if(board.get(r, c+1)!= id) continue;
				if(board.get(r, c+2)!= id) continue;
				if(board.get(r, c+3)!= id) continue;
				score++;
		
			}
		}
		//vertical 
		for(int c = 0; c<cols;c++) {
			for(int r=0;r<=rows-4;r++) {
				if(board.get(r+0, c)!= id)continue;
				if(board.get(r+1, c)!= id)continue;
				if(board.get(r+2, c)!= id)continue;
				if(board.get(r+3, c)!= id)continue;
				score++;
				
				
			}
		}
		//diagonal 
		for(int c = 0; c<=cols-4;c++) {
			for(int r=0;r<=rows-4;r++) {
				if(board.get(r+0, c+0)!= id)continue;
				if(board.get(r+1, c+1)!= id)continue;
				if(board.get(r+2, c+2)!= id)continue;
				if(board.get(r+3, c+3)!= id)continue;
				score++;
				
			}
		}
		for(int c = 0; c<=cols-4;c++) {
			for(int r=rows-1;r>= 3;r--) {
				if(board.get(r-0, c+0)!= id)continue;
				if(board.get(r-1, c+1)!= id)continue;
				if(board.get(r-2, c+2)!= id)continue;
				if(board.get(r-3, c+3)!= id)continue;
				score++;
				
			}
		}
		return score;
	}



	
	private class Move implements Comparable<Move>{
		private int colunm;
		private int value;
		/**
		 * Move constructor. creates a move based on the column and value of the position
		 * 
		 * @param column : the column of the position
		 * @param value : the value of the position
		 */
		public Move(int colunm, int value) {
			this.colunm = colunm;
			this.value = value;
			
		}
		/**
		 * compares two values
		 * 
		 * @return value of the integer that is greater
		 */
		public int compareTo(Move other) {
			return Integer.compare(this.value, other.value);
			
		}
	}
}

