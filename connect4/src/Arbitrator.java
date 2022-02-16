/**
 * A class providing the communication mechanism between the player and
 * the game.  All methods are synchronized because the player and the
 * game run in different threads.  All cross-thread communication is
 * managed by this class.
 * 
 * @author Mickie Newman
 *
 */
public class Arbitrator 
{
    private int moveCol = -1;
    private boolean timeUp = false;

    // Set the column of the next move.  Must be called by the
    // player so that the game can access the move.  This
    // may be called many times safely.
    public final synchronized void setMove(int col) throws TimeUpException { 
        checkTime();
        moveCol = col; 
    }

    // Must be called by frequently by the player to protect against
    // run-away calculations.
    public final synchronized void checkTime() throws TimeUpException {
        if (timeUp) {
            throw new TimeUpException();
        }
    }

    // So the player can check if time is up without having an
    // exception thrown.
    public final synchronized boolean isTimeUp() {
        return timeUp;
    }

    // Called by the game when the player has run out of time to
    // calculate its next move.  The effect of this method being
    // called is that checkTime() and setMove() will throw a
    // TimeUpException when called.
    public final synchronized void timeUp() {
        timeUp = true;
    }

    // Get the column of the next move.  The game calls this
    // method to access the player's next move.
    public final synchronized int getMove() { 
        return moveCol; 
    }
}