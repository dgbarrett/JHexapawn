package gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class HexapawnControls extends JPanel {

	private HexapawnGUI parent;
	private JButton reset, exit;
	private JLabel games, wins;

	public HexapawnControls( HexapawnGUI parent ) {
		super(new GridLayout(2,2));
		this.parent = parent;

		reset = new JButton("Reset series");
		reset.addActionListener( new ResetListener() );
		reset.setMargin(new Insets(10,10,5,10));
		add(reset);

		games = new JLabel();
		add(games);

		exit = new JButton("Exit");
		exit.addActionListener(new ExitListener() );
		exit.setMargin(new Insets(5,10,10,10));
		add(exit);

		wins = new JLabel();
		add(wins);

		updateGameCounter();

		setPreferredSize(new Dimension(500, 200));
	}

	/* Get values about results in current series and display them on the GUI
	   control panel. */
	protected void updateGameCounter() {
		games.setText("                           Games played: " + 
			parent.getCurrentSeriesLength());
		wins.setText("                      Wins (player/cpu): " + 
			parent.getPlayerWinTotal() + "/" + parent.getCPUWinTotal());
	}

	// Listener for the reset button.
	private class ResetListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			parent.resetSeries();
		}
	}

	// Listener for the exit button.
	private class ExitListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}
}