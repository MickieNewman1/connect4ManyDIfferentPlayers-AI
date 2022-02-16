
/**
 * A class to represent a connect 4 board.
 * 
 * @author Daniel Szafir
 *
 */
public class Connect4Board
{
    private int rows, cols;
    private int[] cells;
    private int totalCount;
    private int[] colCounts;

    /**
     * Creates a new connect 4 board of the default size (6 rows and 7 columns)
     */
    public Connect4Board () {
        this(6,7);
    }

    /**
     * Construct a new board with a custom size
     * @param rows Number of rows
     * @param cols Number of columns
     */
    public Connect4Board (int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.cells = new int [rows*cols];
        this.totalCount = 0;
        this.colCounts = new int [cols];
    }

    /**
     * Creates a copy of an existing board.
     * @param that The board to copy
     */
    public Connect4Board (Connect4Board that) {
        this(that.numRows(),that.numCols());
        System.arraycopy(that.cells,0,this.cells,0,rows*cols);
        System.arraycopy(that.colCounts,0,this.colCounts,0,cols);
        this.totalCount = that.totalCount;
    }

    /**
     * Get the number of rows of the board
     * @return The number of rows
     */
    public int numRows() { 
        return rows; 
    }

    /**
     * Get the number of columns of the board
     * @return The number of columns
     */
    public int numCols() { 
        return cols; 
    }

    /**
     * Get the number of cells in the board
     * @return The number of cells
     */
    public int numCells() { 
        return cells.length;
    }

    /**
     * Get the number of filled cells in the board
     * @return The number of occupied cells
     */
    public int numOccupiedCells() { 
        return totalCount; 
    }

    /**
     * Get the number of filled cells in a certain column
     * @param col The column to check
     * @return The number of occupied cells in column col
     */
    public int numOccupiedCells(int col) { 
        return colCounts[col]; 
    }

    /**
     * Get the number of empty cells
     * @return The number of empty cells
     */
    public int numEmptyCells() { 
        return numCells() - numOccupiedCells();
    }

    /**
     * Check if the board is full or not
     * @return True if the board is full, false otherwise
     */
    public boolean isFull() { 
        return numOccupiedCells() == numCells();
    }

    /**
     * Check if a column is full or not
     * @param col The column to check
     * @return True if column col is full, false otherwise
     */
    public boolean isColumnFull (int col) { 
        return colCounts[col] == rows; 
    }

    /**
     * Check if a given cell is occupied
     * @param row The row to check
     * @param col The column to check
     * @return True if the cell at (row, col) is occupied, false otherwise
     */
    public boolean isOccupied (int row, int col) { 
        return get(row,col) > 0;
    }

    // Return values:
    //   0 = unoccupied
    //   1 = occupied by player #1
    //   2 = occupied by player #2
    /**
     * Get the status of a given cell. 
     * @param row The row to check
     * @param col The column to check
     * @return 0 if cell (row, col) is unoccupied, 
     * 		   1 if cell (row, col) is occupied by player #1, 
     * 		   2 if cell (row, col) is occupied by player #2
     */
    public int get (int row, int col) { 
        if (row<0 || row>=rows || col<0 || col>=cols) {
            throw new IndexOutOfBoundsException(
                "row=" + row + " col=" + col);
        }
        return cells[row*cols+col];
    }

    // This method is private on purpose.  It is too dangerous to
    // export, since it could be used to put the board into an invalid
    // configuration.  All modifications to the board should be made
    // with the move() and unmove() methods.
    private void set (int row, int col, int id) { 
        if (id < 0 || id > 2) {
            throw new IllegalArgumentException("id="+id);
        }
        if (row<0 || row>=rows || col<0 || col>=cols) {
            throw new IndexOutOfBoundsException(
                "row=" + row + " col=" + col);
        }
        cells[row*cols+col] = id;
    }

    /**
     * Reverse the state of a board, i.e., change all player#1 pieces to player#2, and vice versa.
     */
    public void reverse() {
        for (int r=0; r<rows; r++) {
            for (int c=0; c<cols; c++) {
                int v = get(r,c);
                if (v!=0) set(r,c,3-v);
            }
        }
    }

    /**
     * Check if a move is valid or not.
     * @param col The column indicating which move to make
     * @return True if column col is within the bounds of the board and is not full
     */
    public boolean isValidMove (int col) {
        return (col>=0) && (col<cols) && !isColumnFull(col);
    }

    /**
     * Perform a move (i.e., "drop" a connect 4 piece into a column)
     * @param col The column indicating what move is to be made
     * @param id The player making the move
     * @return The number of open cells remaining in column col after the move has been made
     */
    public int move (int col, int id) {
        if (id < 1 || id > 2) {
            throw new IllegalArgumentException("id="+id);
        }
        if (!isValidMove(col)) {
            throw new IllegalArgumentException(
                "invalid move: col="+col);
        }
        set(colCounts[col]++,col,id);
        totalCount++;
        return colCounts[col]-1;
    }
    
    /**
     * Undo a move.
     * @param col The column indicating what move is to be undone
     * @param id The player who made the move that we will undo
     * @return The updated count of open cells remaining in column col
     */
    public int unmove (int col, int id) {
        if (id < 1 || id > 2) {
            throw new IllegalArgumentException("id="+id);
        }
        if (colCounts[col] == 0 || get(colCounts[col]-1,col) != id) { 
            throw new IllegalArgumentException(
                "invalid unmove: col="+col);
        }
        set(--colCounts[col],col,0);
        totalCount--;
        return colCounts[col];
    }
    
    /**
     * Get a String representation of the board. May be useful for debugging.
     */
    public String toString() {
        StringBuffer s = new StringBuffer ((rows+2)*(cols+1));
        for (int c=0; c<cols+2; c++) s.append('-');
        s.append('\n');
        for (int r=rows-1; r>=0; r--) {
            s.append('|');
            for (int c=0; c<cols; c++) {
                s.append(get(r,c));
            }
            s.append("|\n");
        }
        for (int c=0; c<cols+2; c++) s.append('-');
        s.append('\n');
        return s.toString();
    }
}
