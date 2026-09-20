public class TicTacToeMain {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--interactive")) {
            Game game = new Game(new Player("Player 1", 'X'), new Player("Player 2", 'O'));
            game.play();
            return;
        }

        Player player1 = new Player("Player 1", 'X');
        Player player2 = new Player("Player 2", 'O');

        // --- Game 1: X wins on the main diagonal ---
        System.out.println("=== Game 1: X wins ===");
        Game game = new Game(player1, player2);
        int[][] moves = {{0, 0}, {0, 1}, {1, 1}, {0, 2}, {2, 2}};
        for (int[] m : moves) {
            System.out.println(game.getCurrentPlayer().getName() + " plays (" + m[0] + "," + m[1] + ")");
            game.makeMove(m[0], m[1]);
        }
        game.getBoard().printBoard();
        System.out.println("Status: " + game.getStatus() + ", winner: " + game.getWinner().getName());

        // Moving after the game is over is rejected
        try {
            game.makeMove(2, 0);
        } catch (IllegalStateException e) {
            System.out.println("Rejected: " + e.getMessage());
        }

        // --- Game 2: invalid moves are rejected and the turn does not pass ---
        System.out.println("\n=== Game 2: invalid moves, then a draw ===");
        game = new Game(player1, player2);
        game.makeMove(1, 1);
        try {
            game.makeMove(1, 1); // occupied
        } catch (IllegalArgumentException e) {
            System.out.println("Rejected occupied cell: " + e.getMessage() + " (still " + game.getCurrentPlayer().getName() + "'s turn)");
        }
        try {
            game.makeMove(3, 0); // out of bounds
        } catch (IllegalArgumentException e) {
            System.out.println("Rejected out-of-bounds: " + e.getMessage());
        }
        // X:1,1 already. Sequence ending in a draw:
        int[][] drawMoves = {{0, 0}, {2, 2}, {0, 2}, {2, 0}, {1, 0}, {1, 2}, {2, 1}, {0, 1}};
        for (int[] m : drawMoves) {
            game.makeMove(m[0], m[1]);
        }
        game.getBoard().printBoard();
        System.out.println("Status: " + game.getStatus() + ", board full: " + game.getBoard().isFull());
    }
}
