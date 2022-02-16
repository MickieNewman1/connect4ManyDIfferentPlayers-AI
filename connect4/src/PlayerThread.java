/**
 * The thread within which the player's calcMove() method is called.
 * We need to keep the player in a separate thread in order to keep
 * the game running in the event that the player misbehaves or
 * crashes.
 * 
 * @author Daniel Szafir
 *
 */
class PlayerThread extends Thread
{
    private Player player;
    private Connect4Board board;
    private int oppMoveCol;
    private Arbitrator arb;

    // Make sure to copy the board in the constructor so that the
    // player can't do any damage to the real board.
    public PlayerThread (
        Player player, Connect4Board board, int oppMoveCol, Arbitrator arb) {
        this.player = player;
        this.board = new Connect4Board(board);
        this.oppMoveCol = oppMoveCol;
        this.arb = arb;
    }

    public void run() {
        try {
            player.calcMove(board,oppMoveCol,arb);
        } catch (TimeUpException e) {
        }
    }
}
