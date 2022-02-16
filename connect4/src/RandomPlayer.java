/**
 * A Connect-4 player that makes random valid moves.
 * 
 * @author Mickie Newman 
 *
 */
public class RandomPlayer implements Player
{
    private static java.util.Random rand = new java.util.Random();

    @Override
    public String name() {
        return "Randy";
    }

    @Override
    public void init(int id, int msecPerMove, int rows, int cols) {
    }

    @Override
    public void calcMove(
        Connect4Board board, int oppMoveCol, Arbitrator arb) 
        throws TimeUpException {
        // Make sure there is room to make a move.
        if (board.isFull()) {
            throw new Error ("Complaint: The board is full!");
        }
        // Make a random valid move.
        int col = 0;
        do { col = rand.nextInt(board.numCols());
        } while (!board.isValidMove(col));
        arb.setMove(col);
    }
}
