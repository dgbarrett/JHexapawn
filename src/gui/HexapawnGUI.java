package gui;

import engine.*;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JOptionPane;

/* Displays a human vs AI, GUI version of the Hexapawn game */
public class HexapawnGUI extends JFrame {

	// Extended JPanel holding buttons and JLabels displaying results 
	private HexapawnControls controls;
	// Extended JPanel holding GUI 3x3 Hexapawn board 
	private HexapawnBoard board;
	// Logic and internal representations of Hexapawn game 
	private HexapawnEngine engine;

	public HexapawnGUI() {
		super("Hexapawn");

		setLayout(new BorderLayout());
		setSize(500, 700);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		engine = new HexapawnEngine();
		add((board = new HexapawnBoard(this)), BorderLayout.CENTER);
		add((controls = new HexapawnControls(this)), BorderLayout.SOUTH);


		/* Get the int[] representation of current state of the board from
		   the engine and render it to the GUI board */
		board.render(engine.getCurrentState());
		setVisible(true);
	}

	/* Reset the learning done by the AI, as well as the number of games 
	   played, won by each party.  Render the default board and reset game
	   counter back to the GUI */
	protected void resetSeries() {
		engine.reset();
		board.render(engine.getCurrentState());
		controls.updateGameCounter();
	}

	/* Player through one move phase.  A move phase is triggered by a move by the player (white). 
		A move phase consists of distinct steps:
			1. Store the current state of the board to keep track of moves and allow AI to learn from then in future.
			2. Pass player move to engine. 
				a. If the move is valid, engine.move() will return true to signify it has applied the move to the board. Go to 2.
				b. If the move is not valid, engine.move() returns false to signify no changes were made to the board. Exit.
			3. Get the current state of the board, this is the "post" board (the output vector for the given input vector stored above so to speak).
			4. Add the pre and post white move boards as a pair to the engines "learning buffer".
				aside. read about the Learning Buffer in HexapawnBrain.java
			5. Render the white move to the board.
			6. Check if game is over.
				a. If game is over, we tell the engine to learn based on the outcome of the game. Exit.
				b. Else, continue.
			7. Perform a CPU move following roughly the same logic.
	*/
	protected void movePhase(int from, int to) {
		int[] pre = engine.getCurrentState();
		if (engine.move(from, to)) {
			int[] post = engine.getCurrentState();
			engine.addToLearningBuffer( pre, post, engine.getPlayerColor() );
			board.render( post );

			if (engine.gameCompleted()) {
				engine.learn( engine.getPlayerColor() );
				resetGame();
				return;				
			} else { moveCPU(); }
		}
	}

	protected int getCurrentSeriesLength() {
		return engine.getCurrentSeriesLength();
	}

	protected int getPlayerWinTotal() {
		return engine.getPlayerWinTotal();
	}

	protected int getCPUWinTotal() {
		return engine.getCPUWinTotal();
	}

	/* Perform a move for the CPU (black).  Follows same logic as movePhase() 
	   except it uses engine.makeMove to allow the engine to decide the best 
	   move based on past matches in the series. */
	private void moveCPU() {
		int[] pre = engine.getCurrentState();
		engine.makeMove();
		int[] post = engine.getCurrentState();
		engine.addToLearningBuffer( pre, post, engine.getCPUColor() );

		board.render( post );

		if (engine.gameCompleted()) {
			engine.learn( engine.getCPUColor() );
			resetGame();
		}
	}

	/* Reset the board GUI after a game has completed. This is not the same as
	   resetting the series, in this case, all learning by the Hexapawn is 
	   retained, and the program continues to track the win totals for the 
	   player and the CPU */
	private void resetGame() {
		engine.refresh();
		controls.updateGameCounter();
		JOptionPane.showMessageDialog(null, 
			"             Winner: " + engine.getWinner(), "Game Over!", 
			JOptionPane.INFORMATION_MESSAGE		);

		if ((engine.getCurrentSeriesLength() % 2) == 1) {
			moveCPU();
		} else {
			board.render(engine.getCurrentState());
		}
	}
}