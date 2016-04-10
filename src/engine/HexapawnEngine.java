package engine;

public class HexapawnEngine {

	private HexapawnBrain brain;
	private String winner;
	private int games_played, human_wins, cpu_wins, 
		board[] = new int[HexapawnConstants.BOARD_SIDE_LENGTH * 
			HexapawnConstants.BOARD_SIDE_LENGTH];

	// Assigning colors to participating parties.
	private static final int PLAYER_COLOR = HexapawnConstants.WHITE_SQUARE;
	private static final int CPU_COLOR = HexapawnConstants.BLACK_SQUARE;

	public HexapawnEngine() {
		reset();
	}

	public int[] getCurrentState() {
		int[] copy = new int[9];
		System.arraycopy(board, 0, copy, 0, board.length);
		return copy;
	}

	public int getCurrentSeriesLength() {
		return games_played;
	}

	public int getPlayerWinTotal() {
		return human_wins;
	}

	public int getCPUWinTotal() {
		return cpu_wins;
	}

	public String getWinner() {
		return winner;
	}

	public int getPlayerColor() {
		return PLAYER_COLOR;
	}

	public int getCPUColor() {
		return CPU_COLOR;
	}

	/* Add an move by 'player' as an input/output pair of int[] to the 
	   learning buffer. */
	public void addToLearningBuffer( int[] inp, int[] out, int player ) {
		brain.addToLearningBuffer( inp, out, player );
	}

	/* Attempt to move the players peice from 'from' to 'to'. Return true if
	   the move was valid and successfull, return false otherwise. */
	public boolean move(int from, int to) {
		int temp = board[from];

		if (board[from] == PLAYER_COLOR) {
			if (isValidMove(this.board, from ,to)) {
				board[to] = board[from];
				board[from] = HexapawnConstants.EMPTY_SQUARE;
				return true;
			}
		}
		return false;	
	}

	/* Test if the board is in a game completed state.  Returns true if the 
	   game is over, return false if the game can continue. */
	public boolean gameCompleted() {
		boolean white = false;
		boolean black = false;

		// Game over if a color makes it to opposite end.
		for (int i = 0 ; i < 3 ; i++) {
			if (board[i] == HexapawnConstants.WHITE_SQUARE) {
				winner = "You!";
				return true;
			}
			if (board[6+i] == HexapawnConstants.BLACK_SQUARE) {
				winner = "CPU";
				return true;
			}
		}

		// Game over if only one color present on board.
		for (int space : board) {
			if (space == HexapawnConstants.WHITE_SQUARE) white = true;
			if (space == HexapawnConstants.BLACK_SQUARE) black = true;
		}

		if (black && !white) {
			winner = "CPU";
			return true;
		} else if (!black && white) {
			winner = "You!";
			return true;
		}

		// Game over if there are no valid moves for peices on the board.
		for (int i=0 ; i < 9 ; i++) {
			if (board[i] == HexapawnConstants.WHITE_SQUARE) {
				if ( isValidMove(board, i, i - 2) 
					 || isValidMove(board, i, i-3)   
					 || isValidMove(board, i, i-4) ) return false;
			}else if (board[i] == HexapawnConstants.BLACK_SQUARE) {
				if ( isValidMove(board, i, i + 2) 
					 || isValidMove(board, i, i+3) 
					 || isValidMove(board, i, i + 4)) return false;
			}
		}

		winner = "No winner!";
		return true;
	}

	// Check if a move is valid under Hexapawn rules.
	public static boolean isValidMove(int[] map, int i1, int i2) {
		// Return false if either value is beyone range of board.
		if (i1 > 8 || i1 < 0 || i2 > 8 || i2 < 0) return false;
		return  (isLegal(map, i1, i2) && isAllowed(map, i1, i2));
	}

	// Move is allowed as long as it doesn't land on a square of its own color.
	public static boolean isAllowed(int[] map, int i1, int i2) {
		return !(map[i1] == map[i2]);
	}

	// Check move type is legal based on the color and current board state.
	public static boolean isLegal(int[] map, int i1, int i2) {
		int move_length = i1 - i2;
		int abs_move_length = Math.abs(move_length);
		
		if (i1 != i2) {
			if (abs_move_length <= 4 && abs_move_length >= 2) {
				switch(map[i1]) {
					case HexapawnConstants.WHITE_SQUARE: 
						if (move_length > 0) {
							/* Moves of length 3 are allowed by and white piece
							   as long as i1 and i2 are in bounds of the board 
							   and we are moving to an empty square */
							if (move_length == 3 &&
								map[i2] == HexapawnConstants.EMPTY_SQUARE) {
								return true;
							}

							/* If the destination is a square of opposite color
							   , we must move diagonally onto it.  These checks 
							   make the move from i1 to i2 is valid and diagonal */
							if (map[i2] == HexapawnConstants.BLACK_SQUARE) {
								if (((i1 % 3) == 0) && (move_length == 2)) return true;
								if (((i1 % 3) == 1) && (move_length == 2 || move_length == 4)) return true;
								if (((i1 % 3) == 2) && (move_length == 4)) return true;
							}
						}
					break;
					case HexapawnConstants.BLACK_SQUARE:
						// Similar but opposite logic to above.
						if (move_length < 0) {
							if (move_length == -3 && 
								map[i2] == HexapawnConstants.EMPTY_SQUARE) {
								return true;
							}
							if (map[i2] == HexapawnConstants.WHITE_SQUARE) {
								if (((i1 % 3) == 0) && (move_length == -4)) return true;
								if (((i1 % 3) == 1) && (move_length == -2 || move_length == -4)) return true;
								if (((i1 % 3) == 2) && (move_length == -2)) return true;
							}
						}
					break;
				}
			}
		}
		return false;
	}

	/* Get a move, decided by the HexapawnBrain based on previous games in the
	   series. */
	public void makeMove() {
		board = brain.getMove(board);
	}

	/* Reset the engine as if the program just started */
	public void reset() {
		brain = new HexapawnBrain();
		board = getDefaultBoard();
		games_played = 0;
		human_wins = 0;
		cpu_wins = 0;
	}

	/* Allow the HexapawnBrain to learn from its learning buffer based on the
	   outcome of the game */
	public void learn(int winner) {
		if (this.winner.equals("No winner!")) {
			brain.learnFromBuffer();
		} else {
			brain.learnFromBuffer( winner );
		}
	}

	/* Used to prepare the engine for running another game in a series. 
	   Increments the win counter for the winning party, or doesn't 
	   increment if the game was a tie.
	   (does not reset HexapawnBrain or counts of any sort) */
	public void refresh() {
		board = getDefaultBoard(); 
		games_played += 1;

		if (winner.equals("You!")) human_wins += 1;
		if (winner.equals("CPU")) cpu_wins += 1;
	}

	/* Default setup for the board */
	private int[] getDefaultBoard() {
		return new int[]{2,2,2,0,0,0,1,1,1};
	}

	
}