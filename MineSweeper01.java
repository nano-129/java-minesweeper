import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

// --- 難易度の定義 ---

enum Difficulty {
	EASY (9, 10, "初級"),
	NORMAL (16, 40, "中級"),
	HARD (16, 99, "上級");
	
	final int size;
	final int mines;
	final String label;
	
	Difficulty ( int size, int mines, String label ) {
		this.size = size;
		this.mines = mines;
		this.label = label;
	}
}

// --- Cellクラス : マスの情報管理 ---

class Cell {
	boolean isMine = false;
	boolean isRevealed = false;
	boolean isFlagged = false;
	int nearbyMines = 0;
	
	void reset() {
		isMine = isRevealed = isFlagged = false;
		nearbyMines = 0;
	}
}

// --- Boardクラス : 計算ロジック管理 ---

class Board {
	private final int size;
	private final int mineCount;
	private Cell [][] grid;
	
	public Board ( int size, int mineCount ) {
		this.size = size;
		this.mineCount = mineCount;
		this.grid = new Cell [size][size];
		for ( int r = 0; r < size; r++ ) {
			for ( int c = 0; c < size; c++ )  {
				grid [r][c] = new Cell ();
			}
		}
		generateMines () ;
	}
	
	private void generateMines () {
		Random rand = new Random () ;
		int placed = 0;
		while ( placed < mineCount ) {
			int r = rand.nextInt(size);
			int c = rand.nextInt(size);
			if ( !grid [r][c].isMine )  {
				grid [r][c].isMine = true;
				placed++;
			}
		}
		calculateNumbers () ;
	}
	
	private void calculateNumbers () {
		for ( int r = 0; r < size; r++ )  {
			for ( int c = 0; c < size; c++ ) {
				if ( grid [r][c].isMine ) continue;
				grid [r][c].nearbyMines = countAdjacentMines ( r, c );
			}
		}
	}
	
	private int countAdjacentMines ( int r, int c ) {
		int count = 0;
		for ( int i = r - 1; i <= r + 1; i++ ) {
			for ( int j = c - 1; j <= c + 1; j++ ) {
				if ( isValid ( i, j ) && grid [i][j].isMine ) count++;
			}
		}
		return count;
	}
	
	public boolean isValid ( int r, int c ) {
		return r >= 0 && r < size && c >= 0 && c < size;
	}
	
	public Cell getCell ( int r, int c ) { return grid [r][c]; }
	public int getSize() { return size; }
	public int getMineCount() { return mineCount; }
}

// --- UIとアプリケーション本体 ---

public class MineSweeper01 extends JFrame {
	
	// --- 色の定数をまとめて管理 ---
	
	private final Color BG_DARK = new Color ( 20, 20, 20 );
	private final Color BG_OPENED = new Color ( 45, 45, 45 );
	private final Color NEON_CYAN = new Color ( 0, 225, 225 );
	private final Color MINE_RED = Color.red;
	private final Color GRID_LINE = new Color ( 80, 80, 80 );

	private Board board;
	private JButton [][] buttons;
	private JPanel boardPanel;
	private boolean gameOver = false;
	
	public MineSweeper01 () {
		setTitle ( "Java Object-Oriented Mine Sweeper" );
		setDefaultCloseOperation ( JFrame.EXIT_ON_CLOSE);
		setLayout ( new BorderLayout());
		
		//　--- メニューバー(難易度) ---
		
		JPanel menuPanel = new JPanel();
		for ( Difficulty d : Difficulty.values()) {
			JButton btn = new JButton ( d.label );
			btn.addActionListener( e -> startGame(d) );
			menuPanel.add(btn);
		}
		add ( menuPanel, BorderLayout.NORTH);
		
		// --- 初期状態で開始 ---
		startGame ( Difficulty.EASY);
		
		setLocationRelativeTo ( null );
		setVisible ( true );
	}
	
	private void startGame ( Difficulty d )  {
		if ( boardPanel != null ) remove ( boardPanel );
		
		this.board = new Board ( d.size, d.mines );
		this.buttons = new JButton [d.size][d.size];
		this.gameOver = false;
		
		boardPanel = new JPanel ( new GridLayout ( d.size, d.size));
		boardPanel.setBackground ( Color.BLACK );
		
		for ( int r = 0; r < d.size; r++ )  {
			for ( int c = 0; c < d.size; c++ ) {
				JButton btn = new JButton ();

				btn.setOpaque ( true );
				btn.setContentAreaFilled ( true );
				btn.setBorderPainted( true );

				btn.setBackground ( BG_DARK );
				btn.setForeground ( NEON_CYAN );
				btn.setBorder ( BorderFactory.createLineBorder ( GRID_LINE ));

				btn.setPreferredSize ( new Dimension ( 40, 40 ));
				btn.setFont ( new Font ( "SansSerif", Font.BOLD, 14 ));
				
				buttons [r][c] = btn;
				
				final int row = r;
				final int col = c;
				
				btn.addMouseListener( new MouseAdapter() {
					@Override
					public void mousePressed ( MouseEvent e ) {
						if ( gameOver ) return;
						if ( SwingUtilities.isRightMouseButton(e)) {
							toggleFlag ( row, col );
						} else {
							openCell ( row, col );
						}
					}
				});
				boardPanel.add ( btn );
			}
		}
		
		add ( boardPanel, BorderLayout.CENTER );
		pack () ; // --- コンポーネントに合わせてウィンドウサイズを調整 ---
		revalidate () ;
		repaint () ;
	}
	
	private void openCell ( int r, int c ) {
		Cell cell = board.getCell(r, c);
		if ( cell.isRevealed || cell.isFlagged ) return;
		
		cell.isRevealed = true;
		JButton btn = buttons [r][c];
		//btn.setEnabled(false);
		btn.setBackground( new Color ( 45, 45, 45 ));
		
		if ( cell.isMine ) {
			btn.setText("💣");
			btn.setForeground ( Color.WHITE );
			btn.setBackground ( Color.RED );
			btn.setBorder( javax.swing.BorderFactory.createLineBorder( Color.RED));
			
			btn.setContentAreaFilled ( false );
			btn.setOpaque( true );
			gameOver = true;
			revealAllMines();
			JOptionPane.showMessageDialog(this, " Game Over! 爆発しました。" );
			return;
		}
		
		if ( cell.nearbyMines > 0 ) {
			btn.setText( String.valueOf( cell.nearbyMines));
			setNumberColor ( btn, cell.nearbyMines );
		} else {
			// --- 再帰的に周囲を開く ---
			for ( int i = r - 1; i <= r + 1; i++ ) {
				for ( int j = c -1; j <= c + 1; j++ ) {
					if ( board.isValid(i, j)) openCell ( i, j );
				}
			}
		}
		checkWin ();
	}
	
	private void toggleFlag ( int r, int c ) {
		Cell cell = board.getCell(r, c);
		if ( cell.isRevealed ) return;
		
		cell.isFlagged = !cell.isFlagged;
		buttons [r][c].setText( cell.isFlagged ? "🚩" : " " );
		buttons [r][c].setForeground ( Color.RED );
	}
	
	private void setNumberColor ( JButton btn, int num ) {
		switch ( num ) {
		case 1: btn.setForeground ( new Color ( 0, 255, 255 )); break;
		case 2: btn.setForeground ( new Color ( 57, 255, 20 )); break;
		case 3: btn.setForeground ( new Color ( 255, 20, 147 )); break;
		case 4: btn.setForeground ( new Color ( 191, 0, 255 )); break;
		default: btn.setForeground ( Color.WHITE );
		}
		btn.setBackground ( new Color ( 45, 45, 45 ));
	}
	
	private void checkWin () {
		int count = 0;
		int totalCells = board.getSize() * board.getSize();
		for ( int r = 0; r < board.getSize(); r++ ) {
			for ( int c = 0; c < board.getSize(); c++ ) {
				if ( board.getCell( r, c ).isRevealed ) count++;
			}
		}
		if ( count == totalCells - board.getMineCount()) {
			gameOver = true;
			JOptionPane.showMessageDialog ( this, "おめでとう! クリアだよ!");
		}
	}
	
	private void revealAllMines() {
		for ( int r = 0; r < board.getSize(); r++ ) {
			for ( int c = 0; c < board.getSize(); c++ ) {
				Cell cell = board.getCell(r, c);
				if ( cell.isMine ) {
					JButton btn = buttons [r][c];
					
					btn.setText( "💣" );
					btn.setBackground ( Color.RED);
					btn.setForeground ( Color.WHITE );
					btn.setBorder(javax.swing.BorderFactory.createLineBorder( Color.RED ));
					btn.setContentAreaFilled ( false );
					btn.setOpaque ( true );
					
					btn.setBackground ( Color.RED );
					btn.setBorder ( javax.swing.BorderFactory.createLineBorder ( Color.RED ));
					btn.setOpaque ( true );
				}
			}
		}
	}
	
	public static void main ( String[] args ) {
		// --- 見た目をOS標準に合わせる ---
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch ( Exception ignored ) {}
		
		SwingUtilities.invokeLater(() -> new MineSweeper01());
	}
}