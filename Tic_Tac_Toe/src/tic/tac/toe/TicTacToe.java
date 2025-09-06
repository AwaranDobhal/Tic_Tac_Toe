package tic.tac.toe;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public class TicTacToe extends JFrame implements ActionListener {

    private final JButton[][] buttons = new JButton[3][3];
    private final String[][] board = new String[3][3];
    private final JLabel statusLabel = new JLabel("Player X's Turn", SwingConstants.CENTER);
    private boolean xTurn = true;

    private final ImageIcon xIcon = loadAndResizeIcon("/icons/X.png", 120, 120);
    private final ImageIcon oIcon = loadAndResizeIcon("/icons/O.png", 120, 120);
    private final ImageIcon resetIcon = loadAndResizeIcon("/icons/reset.png", 45, 45);
    private final ImageIcon sparkleIcon = loadAndResizeIcon("/icons/party-popper.png", 60, 60);
    private final ImageIcon checkIcon = loadAndResizeIcon("/icons/confetti-ball.png", 60, 60);
    private final ImageIcon gearIcon = loadAndResizeIcon("/icons/load.png", 60, 60);

    public TicTacToe() {
        setTitle("Tic Tac Toe");
        setSize(1920, 1080);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setContentPane(new BackgroundPanel(loadImage("/icons/bg.jpeg")));
        setLayout(new BorderLayout(20, 20));

        setupUI();
        setVisible(true);
    }

    private void setupUI() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = "";
            }
        }
        add(createStatusLabel(), BorderLayout.NORTH);
        add(createGridPanel(), BorderLayout.CENTER);
        add(createResetButton(), BorderLayout.SOUTH);
    }

    private JLabel createStatusLabel() {
        statusLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 56));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(220, 240, 255));
        statusLabel.setForeground(new Color(25, 25, 112));
        statusLabel.setPreferredSize(new Dimension(1920, 100));
        statusLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
        statusLabel.setIconTextGap(20);
        return statusLabel;
    }

    private JPanel createGridPanel() {
        JPanel grid = new JPanel(new GridLayout(3, 3, 20, 20));
        grid.setOpaque(false);
        grid.setPreferredSize(new Dimension(600, 600));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                JButton btn = createCellButton();
                buttons[i][j] = btn;
                grid.add(btn);
            }
        }
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.add(grid);
        return center;
    }

    private JButton createCellButton() {
        JButton btn = new JButton();
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setBorder(new LineBorder(new Color(200, 200, 200), 2));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(this);

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled() && btn.getIcon() == null) {
                    btn.setBackground(new Color(240, 248, 255));
                }
            }
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled() && btn.getIcon() == null) {
                    btn.setBackground(Color.WHITE);
                }
            }
        });
        return btn;
    }

    private JButton createResetButton() {
        JButton reset = new JButton("Reset Game", resetIcon);
        reset.setFont(new Font("Arial", Font.PLAIN, 36));
        reset.setBackground(new Color(60, 179, 113));
        reset.setForeground(Color.WHITE);
        reset.setBorderPainted(false);
        reset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        reset.setPreferredSize(new Dimension(90, 80));
        reset.addActionListener(e -> resetGame());
        return reset;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!xTurn) return;

        JButton clicked = (JButton) e.getSource();
        Point pos = findButtonPosition(clicked);
        if (pos == null || !board[pos.x][pos.y].isEmpty()) return;

        board[pos.x][pos.y] = "X";
        clicked.setIcon(xIcon);

        if (checkWin("X")) {
            endGame("Player X Wins!", sparkleIcon);
        } else if (isDraw()) {
            endGame("It's a Draw!", checkIcon);
        } else {
            xTurn = false;
            statusLabel.setText("(O) is thinking...");
            statusLabel.setIcon(gearIcon);
            new Timer(800, evt -> {
                aiMove();
                ((Timer) evt.getSource()).stop();
            }).start();
        }
    }

    private void aiMove() {
        Point bestMove = null;
        int bestScore = Integer.MIN_VALUE;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j].isEmpty()) {
                    board[i][j] = "O";
                    int score = minimax(false);
                    board[i][j] = "";
                    if (score > bestScore) {
                        bestScore = score;
                        bestMove = new Point(i, j);
                    }
                }
            }
        }
        if (bestMove != null) {
            board[bestMove.x][bestMove.y] = "O";
            buttons[bestMove.x][bestMove.y].setIcon(oIcon);
        }

        if (checkWin("O")) {
            endGame("(O) Wins!", sparkleIcon);
        } else if (isDraw()) {
            endGame("It's a Draw!", checkIcon);
        } else {
            xTurn = true;
            statusLabel.setText("Player X's Turn");
            statusLabel.setIcon(null);
        }
    }

    private int minimax(boolean isMax) {
        if (checkWin("O")) return 10;
        if (checkWin("X")) return -10;
        if (isDraw()) return 0;

        int best = isMax ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        String player = isMax ? "O" : "X";

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j].isEmpty()) {
                    board[i][j] = player;
                    int score = minimax(!isMax);
                    board[i][j] = "";
                    best = isMax ? Math.max(score, best) : Math.min(score, best);
                }
            }
        }
        return best;
    }

    private boolean checkWin(String p) {
        for (int i = 0; i < 3; i++) {
            if (p.equals(board[i][0]) && p.equals(board[i][1]) && p.equals(board[i][2])) return true;
            if (p.equals(board[0][i]) && p.equals(board[1][i]) && p.equals(board[2][i])) return true;
        }
        return (p.equals(board[0][0]) && p.equals(board[1][1]) && p.equals(board[2][2])) ||
               (p.equals(board[0][2]) && p.equals(board[1][1]) && p.equals(board[2][0]));
    }

    private boolean isDraw() {
        for (String[] row : board) {
            for (String cell : row) {
                if (cell.isEmpty()) return false;
            }
        }
        return true;
    }

    private void endGame(String message, ImageIcon icon) {
        statusLabel.setText(message);
        statusLabel.setIcon(icon);
        for (JButton[] row : buttons) {
            for (JButton btn : row) {
                btn.setEnabled(false);
            }
        }
    }

    private void resetGame() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = "";
                JButton btn = buttons[i][j];
                btn.setIcon(null);
                btn.setEnabled(true);
                btn.setBackground(Color.WHITE);
                btn.setBorder(new LineBorder(new Color(200, 200, 200), 2));
            }
        }
        xTurn = true;
        statusLabel.setText("Player X's Turn");
        statusLabel.setIcon(null);
    }

    private Point findButtonPosition(JButton btn) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j] == btn) return new Point(i, j);
            }
        }
        return null;
    }

    private ImageIcon loadAndResizeIcon(String path, int width, int height) {
        URL url = getClass().getResource(path);
        if (url == null) {
            System.err.println("Couldn't find image file: " + path);
            return null;
        }
        ImageIcon originalIcon = new ImageIcon(url);
        Image resizedImg = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImg);
    }

    private Image loadImage(String path) {
        URL url = getClass().getResource(path);
        if (url == null) {
            System.err.println("Couldn't find image file: " + path);
            return null;
        }
        return new ImageIcon(url).getImage();
    }

    class BackgroundPanel extends JPanel {
        private final Image image;

        public BackgroundPanel(Image img) {
            this.image = img;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TicTacToe::new);
    }
}