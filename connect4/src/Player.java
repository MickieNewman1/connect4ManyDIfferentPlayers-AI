
/**
 * Connect4 player interface.  Any class implementing this interface can
 * by dynamically loaded into the game as a player.
 * 
 * @author Daniel Szafir
 *
 */
public interface Player
{
    /**
     * Return the name of this player.
     * 
     * @return A name for this player
     */
    String name();

  
    /**
     * Initialize the player. The game calls this method once,
     * before any calls to calcMove().
     * 
     * @param id integer identifier for the player (can get opponent's id via 3-id);
     * @param msecPerMove time allowed for each move
     * @param rows the number of rows in the board
     * @param cols the number of columns in the board
     */
    void init(int id, int msecPerMove, int rows, int cols);

    
    /**
     * Called by driver program to calculate the next move.
     *  
     * @param board current connect 4 board
     * @param oppMoveCol column of opponent's most recent move; -1 if this is the first move 
     * 		  of the game; note that the board may not be empty on the first move of the game!
     * @param arb handles communication between game and player
     * @throws TimeUpException If the game determines the player has run out of time
     */
    void calcMove(Connect4Board board, int oppMoveCol, Arbitrator arb) 
        throws TimeUpException;
}
