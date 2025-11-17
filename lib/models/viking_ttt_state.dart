import 'package:flutter/foundation.dart';

enum Player { player1, player2 }

enum GamePhase { placement, movement }

class Position {
  final int row;
  final int col;

  const Position(this.row, this.col);

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is Position && other.row == row && other.col == col;
  }

  @override
  int get hashCode => row.hashCode ^ col.hashCode;

  @override
  String toString() => 'Position($row, $col)';
}

class VikingTTTState extends ChangeNotifier {
  // Board state: null = empty, Player.player1 or Player.player2
  List<List<Player?>> board = List.generate(3, (_) => List.filled(3, null));

  Player currentPlayer = Player.player1;
  GamePhase phase = GamePhase.placement;
  Position? selectedPosition;
  Player? winner;
  bool isDraw = false;

  // Track how many pieces each player has placed
  int player1Pieces = 0;
  int player2Pieces = 0;

  final int maxPiecesPerPlayer = 3;

  void resetGame() {
    board = List.generate(3, (_) => List.filled(3, null));
    currentPlayer = Player.player1;
    phase = GamePhase.placement;
    selectedPosition = null;
    winner = null;
    isDraw = false;
    player1Pieces = 0;
    player2Pieces = 0;
    notifyListeners();
  }

  void placePiece(int row, int col) {
    if (phase != GamePhase.placement) return;
    if (board[row][col] != null) return;
    if (winner != null) return;

    board[row][col] = currentPlayer;

    if (currentPlayer == Player.player1) {
      player1Pieces++;
    } else {
      player2Pieces++;
    }

    // Check for win
    if (_checkWin(currentPlayer)) {
      winner = currentPlayer;
      notifyListeners();
      return;
    }

    // Switch to movement phase if both players have placed all pieces
    if (player1Pieces == maxPiecesPerPlayer &&
        player2Pieces == maxPiecesPerPlayer) {
      phase = GamePhase.movement;
    }

    // Switch player
    currentPlayer =
        currentPlayer == Player.player1 ? Player.player2 : Player.player1;
    notifyListeners();
  }

  void selectPiece(int row, int col) {
    if (phase != GamePhase.movement) return;
    if (winner != null) return;
    if (board[row][col] != currentPlayer) return;

    selectedPosition = Position(row, col);
    notifyListeners();
  }

  void movePiece(int toRow, int toCol) {
    if (phase != GamePhase.movement) return;
    if (selectedPosition == null) return;
    if (board[toRow][toCol] != null) return;
    if (winner != null) return;

    // Move the piece
    board[selectedPosition!.row][selectedPosition!.col] = null;
    board[toRow][toCol] = currentPlayer;
    selectedPosition = null;

    // Check for win
    if (_checkWin(currentPlayer)) {
      winner = currentPlayer;
      notifyListeners();
      return;
    }

    // Check for draw (if no valid moves for either player)
    if (_isStalemate()) {
      isDraw = true;
      notifyListeners();
      return;
    }

    // Switch player
    currentPlayer =
        currentPlayer == Player.player1 ? Player.player2 : Player.player1;
    notifyListeners();
  }

  void handleTap(int row, int col) {
    if (phase == GamePhase.placement) {
      placePiece(row, col);
    } else {
      // Movement phase
      if (selectedPosition == null) {
        // Select a piece
        selectPiece(row, col);
      } else if (selectedPosition!.row == row &&
          selectedPosition!.col == col) {
        // Deselect
        selectedPosition = null;
        notifyListeners();
      } else {
        // Try to move
        movePiece(row, col);
      }
    }
  }

  bool _checkWin(Player player) {
    // Check rows
    for (int row = 0; row < 3; row++) {
      if (board[row][0] == player &&
          board[row][1] == player &&
          board[row][2] == player) {
        return true;
      }
    }

    // Check columns
    for (int col = 0; col < 3; col++) {
      if (board[0][col] == player &&
          board[1][col] == player &&
          board[2][col] == player) {
        return true;
      }
    }

    // Check diagonals
    if (board[0][0] == player &&
        board[1][1] == player &&
        board[2][2] == player) {
      return true;
    }
    if (board[0][2] == player &&
        board[1][1] == player &&
        board[2][0] == player) {
      return true;
    }

    return false;
  }

  bool _isStalemate() {
    // A stalemate occurs if the current player has no valid moves
    // In Viking TTT, you can always move to any empty space, so stalemate
    // is rare. For simplicity, we'll check if board is full (shouldn't happen
    // with 3 pieces each, but just in case).

    for (int row = 0; row < 3; row++) {
      for (int col = 0; col < 3; col++) {
        if (board[row][col] == null) {
          return false; // There's an empty space
        }
      }
    }
    return true; // Board is full
  }

  int getPiecesPlaced(Player player) {
    return player == Player.player1 ? player1Pieces : player2Pieces;
  }

  String get phaseDescription {
    if (phase == GamePhase.placement) {
      return 'Placement Phase - Place your pieces';
    } else {
      return 'Movement Phase - Move your pieces';
    }
  }

  String get currentPlayerName {
    return currentPlayer == Player.player1 ? 'Player 1' : 'Player 2';
  }
}
