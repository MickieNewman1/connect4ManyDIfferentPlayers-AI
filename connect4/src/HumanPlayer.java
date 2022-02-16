/**
 * A Connect-4 player that represents a human player.
 * All this class does is store the name of the player.
 * The calcMove() method should never be called.
 * 
 * @author Daniel Szafir
 *
 */
public class HumanPlayer implements Player
{
    private final String defaultName = "Some Human";
    private String name;

    public HumanPlayer() { 
        this(null); 
    }
    public HumanPlayer (String name) { 
        this.name = (name == null) ? defaultName : name; 
    }
    public String name() { 
        return name; 
    }
    public void setName (String name) { 
        if (name != null) {
            this.name = name; 
        }
    }
    public void init (int id, int msecPerMove, int rows, int cols) {
    }
    public void calcMove (
        Connect4Board board, int oppMoveCol, Arbitrator arb) 
        throws TimeUpException {
        throw new Error("HumanPlayer.calcMove() should never be called.");
    }
}
