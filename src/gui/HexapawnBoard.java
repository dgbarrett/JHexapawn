package gui;

import engine.HexapawnConstants;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.imageio.ImageIO;

/* GUI Representation of 3x3 Hexapawn board */
public class HexapawnBoard extends JPanel {

	// Paths to images to be used for pawns
	private static final String WHITE_PATH = "resources/white.png";
	private static final String BLACK_PATH = "resources/black.png";

	private JLabel[] squares = new JLabel[HexapawnConstants.BOARD_SIDE_LENGTH * 
										  HexapawnConstants.BOARD_SIDE_LENGTH];
	private HexapawnGUI parent;
	/* ImageIcons to uses as white and black pawns. Loaded once on class 
		creations so we do not have to access a file every time we render one
		of the image to the GUI board */
	private ImageIcon white_pawn;
	private ImageIcon black_pawn;
	/* The indicies of the squares which to player wishes to move from and the
	   square the player wants to move to.  Set by ActionListeners on 
	   HexapawnBoard sub-JLabels */
	private int to = -1;
	private int from = -1;

	public HexapawnBoard(HexapawnGUI parent) {
		// Using GridLayout to create 3x3 board
		super(new GridLayout(HexapawnConstants.BOARD_SIDE_LENGTH, 
							 HexapawnConstants.BOARD_SIDE_LENGTH));
		this.parent = parent;

		// Load pawn ImageIcons
		white_pawn = loadScaledPawnImage(WHITE_PATH);
		black_pawn = loadScaledPawnImage(BLACK_PATH);

		// Initialize the GUI board
		for (int i =0 ; i < 9 ; i++) {
			squares[i] = new JLabel();
			squares[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
			/* setOpaque(true) so we can render a background color to the 
			   square later if necessary */
			squares[i].setOpaque(true);
			/* Each square has its own listener identifed by its index on the 
			   board.  Squares are numbered starting with 0 in the top left, 
			   moving across, and ending with 8 in the bottom right */
			squares[i].addMouseListener(new SquareListener(i));
			add(squares[i]);
		}
	}

	//Render the int[] representation of the board from the engine onto the GUI
	protected void render(int[] board) {
		/* Remove any previous GUI board state by rendering all squares on the
		   board white and setting their icons null */
		for (JLabel square : squares) {
			square.setIcon(null);
			square.setBackground(Color.WHITE);
		}

		// Render to each square (type = JLabel)
		for (int i = 0 ; i < squares.length ; i++) {
			switch(board[i]) {
				case HexapawnConstants.EMPTY_SQUARE: 
					squares[i].setBackground(Color.WHITE);
					break;
				case HexapawnConstants.WHITE_SQUARE:
					squares[i].setIcon(white_pawn);
					break;
				case HexapawnConstants.BLACK_SQUARE:
					squares[i].setIcon(black_pawn);
					break;
				default:
					System.out.println("!!!! Fatar Error - unrecognized value on hexapawn board :: " + board[i] + " !!!!");
					System.exit(0);
			}
		}
	}

	// Load an image from path into a scaled ImageIcon
	private ImageIcon loadScaledPawnImage(String path) {
		BufferedImage img = null;

		try {
			img = ImageIO.read(new File(path));
		} catch (IOException e) {
			System.out.println("!!!! Fatal Error - Could not load pawn image. - Exiting !!!!");
			System.exit(0);
		}

		return new ImageIcon(img.getScaledInstance(160,160,Image.SCALE_SMOOTH));
	}

	// Listener class for board squares.  Listens for click on the sqaure.
	private class SquareListener extends MouseAdapter {

		// The index of the square the listener is listening on.
		private int index;

		public SquareListener(int index) {
			this.index = index;
		}
		public void mouseClicked(MouseEvent e) {
			// On first click, set from.
			if (from == -1) {
				from = index;
			} else if (to == -1) {
				/* When the listener is called another time and from == -1, we
				   know to set the destination (to) of the desired move.  We 
				   now have everything we need to start a move phase. */
				if (from != index) {
					to = index;
					parent.movePhase(from, to);
					// Reset values so we can process next move.
					from = -1;
					to = -1;
				}
			}
		}
	}
}