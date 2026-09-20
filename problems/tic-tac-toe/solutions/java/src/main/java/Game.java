import java.util.Scanner;

public class Game {
    private final Player player1;
    private final Player player2;
    private final Board board;
    private Player currentPlayer;
    private Player winner;
    private GameStatus status;
    private Scanner scanner; // created lazily, only for interactive play

    public Game(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.board = new Board();
        this.currentPlayer = player1;
        this.status = GameStatus.IN_PROGRESS;
    }

    /**
     * Plays one move for the current player.
     *
     * @throws IllegalStateException    if the game is already over
     * @throws IllegalArgumentException if the cell is out of bounds or already taken
     */
    public synchronized GameStatus makeMove(int row, int col) {
        if (status != GameStatus.IN_PROGRESS) {
            throw new IllegalStateException("Game is already over.");
        }
        board.makeMove(row, col, currentPlayer.getSymbol());
        if (board.hasWinner()) {
            winner = currentPlayer;
            status = GameStatus.WIN;
        } else if (board.isFull()) {
            status = GameStatus.DRAW;
        } else {
            switchPlayer();
        }
        return status;
    }

    public GameStatus getStatus() {
        return status;
    }

    public Player getWinner() {
        return winner;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public Board getBoard() {
        return board;
    }

    /** Interactive loop reading moves from stdin. */
    public void play() {
        board.printBoard();

        while (status == GameStatus.IN_PROGRESS) {
            System.out.println(currentPlayer.getName() + "'s turn.");
            int row = getValidInput("Enter row (0-2): ");
            int col = getValidInput("Enter column (0-2): ");

            try {
                makeMove(row, col);
                board.printBoard();
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }

        if (status == GameStatus.WIN) {
            System.out.println(winner.getName() + " wins!");
        } else {
            System.out.println("It's a draw!");
        }
    }

    private void switchPlayer() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }

    private int getValidInput(String message) {
        if (scanner == null) {
            scanner = new Scanner(System.in);
        }
        int input;

        while (true) {
            System.out.print(message);
            if (scanner.hasNextInt()) {
                input = scanner.nextInt();
                if (input >= 0 && input <= 2) {
                    return input;
                }
            } else if (scanner.hasNext()) {
                scanner.next();
            } else {
                throw new IllegalStateException("No more input available.");
            }
            System.out.println("Invalid input! Please enter a number between 0 and 2.");
        }
    }
}
