package engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/* AI for Hexapawn, attmepts to learn based off of past games in a series. */
public class HexapawnBrain {

	/* A list of BrainStates containing input/ouput vectors correspoding to 
	   moves on the Hexapawn board. */
	private ArrayList<BrainState> brain;
	/* Buffer to hold moves in the current games */
	protected ArrayList<BrainState> buffer;
	protected int totalMemories;

	public HexapawnBrain() {
		brain = new ArrayList<BrainState>();
		buffer = new ArrayList<BrainState>();
		totalMemories = 0;
	}

	/* Add a pair of input/ouput vectors corresponding to a Hexapawn move by 
	  'player' to learning buffer to possibly be used to train the 
	  HexapawnBrain in future */
	public void addToLearningBuffer(int[] input, int[] output, int player) {

		BrainState state = null;
		int[] inpcopy = new int[9];
		int[] outcopy = new int[9];

		System.arraycopy(input, 0, inpcopy, 0 , input.length);
		System.arraycopy(output, 0, outcopy, 0 , output.length);

		/* If the move was not done by the CPU we flip the situation to 
		   simulate as if the CPU had done the move, and thus can learn 
		   from having it in its brain */
		state =  ( player == HexapawnConstants.WHITE_SQUARE ) ? new BrainState(translateToBlack(inpcopy), translateToBlack(outcopy), player) : new BrainState(input, output, player); 

		buffer.add(state);
	}

	/* Function called if game result is WIN.  Adds all moves by winner in 
	   buffer to brain */
	public void learnFromBuffer( int winner ) {
		for (BrainState state : buffer) {
			if ( state.player == winner ) addState( state, true );
		}
		buffer.clear();
	}

	/* Function called if game result is DRAW. Adds all moves by both players 
	   in buffer to brain */
	public void learnFromBuffer() {
		for (BrainState state : buffer) {
			addState(state, false);
		}
		buffer.clear();
	}

	// Flip the order of the arr then flip all white pawns to black and vice versa.
	public int[] translateToBlack(int[] arr) {
		int[] temp = new int[9];

		temp[0] = arr[8];
		temp[1] = arr[7];
		temp[2] = arr[6];
		temp[3] = arr[5];
		temp[4] = arr[4];
		temp[5] = arr[3];
		temp[6] = arr[2];
		temp[7] = arr[1];
		temp[8] = arr[0];

		for (int i = 0 ; i < 9 ; i++) {
			if(temp[i] == HexapawnConstants.WHITE_SQUARE) {
				temp[i] = HexapawnConstants.BLACK_SQUARE;
			} else if (temp[i] == HexapawnConstants.BLACK_SQUARE) {
				temp[i] = HexapawnConstants.WHITE_SQUARE;
			}
		}

		return temp;
	}

	/* Add a weighted BrainState to the brain based on whether the move is 
	   part of a winning path or not */
	public void addState( BrainState state, boolean isWinner ) {
		for (BrainState memory : brain) {
			// If input/output pair already present in brain...
			if (Arrays.equals(memory.input, state.input) && 
				Arrays.equals(memory.output, state.output)) {
				// Increment frequency count of matched input/output pair
				if (isWinner) {
					/* We value moves from winning paths more, thus they 
					  are weighted 50% more heavily than moves from a DRAW path */
					memory.count += 3;
					totalMemories += 3;
				} else {
					/* Increment based on move from DRAW path */
					memory.count += 2;
					totalMemories += 2;
				}
				return;
			} 
		}

		// Input/output pair not found in brain, must add...
		state.player = -1;

		// Increment frequency count based on same rules as above.
		if (isWinner) {
			state.count += 3;
			totalMemories += 3;
		} else {
			state.count += 2;
			totalMemories += 2;
		}
			
		brain.add(state);
	}

	/* Get a board representing the game state after a CPU move on the board 
	  passed as 'inp' */
	public int[] getMove(int[] inp) {
		return (brain.isEmpty()) ? getRandomMove(inp) : getSmartMove(inp);
	}

	/* Get a move based on past game results */
	private int[] getSmartMove( int[] input ) {
		/* List to store BrainStates in the brain where 'input' 
		  (current engine.board state) matches the input state stored in the 
		  brain already */
		ArrayList<BrainState> possibles = new ArrayList<BrainState>();
		/* For selecting a random possible move base on the frequency of move 
		   in winning and tieing paths */
		ArrayList<Integer> intervals = new ArrayList<Integer>();
		int totalHistoricalMovesFromInputState = 0;

		for (BrainState memory : brain) {
			if (Arrays.equals(memory.input, input)){
				/* If entry in the brain has the same input value as the one 
				   passed to the function we add it to a list of possible moves
				   from the given input state. */
				possibles.add(memory.copyState());
				/* Update count of how many times this input state has been 
				   encountered in the current game series */
				totalHistoricalMovesFromInputState += memory.count;
			}	
		}

		/* If the HexapawnBrain does not contain any moves from the 
		   given input state we generate a random move */
		if (possibles.isEmpty()) return getRandomMove( input );
		
		// Examine each possible move from the given input state 
		for (int i = 0; i < possibles.size() ; i++) {
			BrainState mem = possibles.get(i);
			/* Calculate percentage of total moves from this input state 
			   accounted for by mem (this possible move we are currently looking at) */
			double range = (double)mem.count/totalHistoricalMovesFromInputState;
			/* normalize decimal ratio into value between 0 and 1000, the sum 
				of all intervals of all possible moves will be 1000  */
			int interval = (int)(range * 1000);
			

			/* Add all previously stored intervals to the calculated interval.  This 
			  generates a weighted division of the range  0 - 1000 based on the 
			  frequency of the move. 
				ex. If move x has happened 2 times and move y has happened 3 
					times, the range 1-400 represnets move x, and the range 
					401-1000 represents move y 
			  	*/
			
			if (i > 0) {
				interval += intervals.get(i-1);
			}
			

			if (interval >= 999) interval = 1000;

			// Add the newly generated interval.
			intervals.add(interval);
		}

		// Generate random integer up to 1000.
		Random r = new Random();
		int index = r.nextInt(1000);

		int base = 0;
		/* If the random integer lies in interval[i-1] and interval[i] 
		  (interval[i<0] = 0), we select possible[i] as our move */
		for (int i = 0; i < intervals.size() ; i++) {
			if (index > base && index <= intervals.get(i)) {
				return possibles.get(i).output;
			}
			base = intervals.get(i);
		}

		// If all else fails (it shouldn't) return a random move.
		return getRandomMove(input);
	}

	// Generate a random move based on the board input state inp.
	private int[] getRandomMove( int[] inp ) {
		Random rand = new Random();
		int from = -1, to =-1;

		do {
			from = rand.nextInt(9);
			to = rand.nextInt(9);

			if (inp[from] == HexapawnConstants.BLACK_SQUARE) {
				if (HexapawnEngine.isValidMove(inp, from ,to)) {
					break;
				}
			} 
		} while (true);

		inp[to] = inp[from];
		inp[from] = HexapawnConstants.EMPTY_SQUARE;

		return inp;
	}

	/* Class used to store "memories" with the brain */
	private class BrainState {
		/* The board state before the move, the board state after the move */
		public int[] input, output;
		/* Number of times the move occured, player who played the move */
		public int count, player;

		public BrainState(int[] input, int[] output, int player) {
			this.input = input;
			this.output = output;
			this.player = player;
			this.count = -1;
		}

		public BrainState() {
			this.input = new int[9];
			this.output = new int[9];
			this.player = -1;
			this.count = -1;
		}

		// Returns a copy of the given BrainState.
		public BrainState copyState() {
			BrainState st = new BrainState();
			System.arraycopy(this.input, 0, st.input, 0, this.input.length);
			System.arraycopy(this.output, 0, st.output, 0, this.output.length);
			st.player = this.player;
			st.count = this.count;
			return st;
		}
	}
}