import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/game_info.dart';
import '../models/viking_ttt_state.dart';

class VikingTTTScreen extends StatelessWidget {
  final GameMode mode;

  const VikingTTTScreen({super.key, required this.mode});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (_) => VikingTTTState(),
      child: VikingTTTGameView(mode: mode),
    );
  }
}

class VikingTTTGameView extends StatelessWidget {
  final GameMode mode;

  const VikingTTTGameView({super.key, required this.mode});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(
          mode == GameMode.singlePlayer
              ? 'Viking Tic Tac Toe'
              : 'Viking TTT - Multiplayer',
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () {
              context.read<VikingTTTState>().resetGame();
            },
          ),
        ],
      ),
      body: Consumer<VikingTTTState>(
        builder: (context, state, child) {
          return Container(
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
                colors: [
                  Theme.of(context).colorScheme.primaryContainer.withOpacity(0.3),
                  Theme.of(context).colorScheme.surface,
                ],
              ),
            ),
            child: SafeArea(
              child: Column(
                children: [
                  const SizedBox(height: 20),
                  _buildGameInfo(context, state),
                  const SizedBox(height: 30),
                  Expanded(
                    child: Center(
                      child: _buildBoard(context, state),
                    ),
                  ),
                  const SizedBox(height: 20),
                  _buildInstructions(context, state),
                  const SizedBox(height: 20),
                ],
              ),
            ),
          );
        },
      ),
    );
  }

  Widget _buildGameInfo(BuildContext context, VikingTTTState state) {
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 20),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            if (state.winner != null)
              Text(
                '${state.winner == Player.player1 ? "Player 1" : "Player 2"} Wins!',
                style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                      color: Theme.of(context).colorScheme.primary,
                      fontWeight: FontWeight.bold,
                    ),
              )
            else if (state.isDraw)
              Text(
                'Draw!',
                style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                      color: Theme.of(context).colorScheme.error,
                      fontWeight: FontWeight.bold,
                    ),
              )
            else
              Column(
                children: [
                  Text(
                    state.phaseDescription,
                    style: Theme.of(context).textTheme.titleMedium,
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'Current Turn: ${state.currentPlayerName}',
                    style: Theme.of(context).textTheme.titleLarge?.copyWith(
                          color: state.currentPlayer == Player.player1
                              ? Colors.blue
                              : Colors.red,
                          fontWeight: FontWeight.bold,
                        ),
                  ),
                ],
              ),
            const SizedBox(height: 12),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                _buildPlayerInfo(
                  context,
                  'Player 1',
                  state.getPiecesPlaced(Player.player1),
                  Colors.blue,
                  state.currentPlayer == Player.player1,
                ),
                _buildPlayerInfo(
                  context,
                  'Player 2',
                  state.getPiecesPlaced(Player.player2),
                  Colors.red,
                  state.currentPlayer == Player.player2,
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildPlayerInfo(
    BuildContext context,
    String name,
    int pieces,
    Color color,
    bool isActive,
  ) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      decoration: BoxDecoration(
        color: isActive ? color.withOpacity(0.2) : Colors.transparent,
        borderRadius: BorderRadius.circular(8),
        border: Border.all(
          color: isActive ? color : Colors.grey.withOpacity(0.3),
          width: isActive ? 2 : 1,
        ),
      ),
      child: Column(
        children: [
          Text(
            name,
            style: TextStyle(
              fontWeight: FontWeight.bold,
              color: color,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            'Pieces: $pieces/3',
            style: Theme.of(context).textTheme.bodySmall,
          ),
        ],
      ),
    );
  }

  Widget _buildBoard(BuildContext context, VikingTTTState state) {
    final size = MediaQuery.of(context).size.width * 0.9;
    final cellSize = size / 3;

    return Container(
      width: size,
      height: size,
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surface,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.2),
            blurRadius: 10,
            offset: const Offset(0, 5),
          ),
        ],
      ),
      child: Stack(
        children: [
          // Grid lines
          CustomPaint(
            size: Size(size, size),
            painter: GridPainter(
              color: Theme.of(context).colorScheme.onSurface.withOpacity(0.3),
            ),
          ),
          // Pieces
          ...List.generate(3, (row) {
            return ...List.generate(3, (col) {
              return Positioned(
                left: col * cellSize,
                top: row * cellSize,
                width: cellSize,
                height: cellSize,
                child: GestureDetector(
                  onTap: () => state.handleTap(row, col),
                  child: Container(
                    margin: const EdgeInsets.all(8),
                    decoration: BoxDecoration(
                      color: state.selectedPosition?.row == row &&
                              state.selectedPosition?.col == col
                          ? Colors.yellow.withOpacity(0.3)
                          : Colors.transparent,
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Center(
                      child: _buildPiece(context, state.board[row][col]),
                    ),
                  ),
                ),
              );
            });
          }),
        ],
      ),
    );
  }

  Widget _buildPiece(BuildContext context, Player? player) {
    if (player == null) return const SizedBox.shrink();

    final color = player == Player.player1 ? Colors.blue : Colors.red;
    final icon = player == Player.player1 ? Icons.close : Icons.circle_outlined;

    return Container(
      decoration: BoxDecoration(
        color: color,
        shape: BoxShape.circle,
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.3),
            blurRadius: 5,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Center(
        child: Icon(
          icon,
          size: 40,
          color: Colors.white,
        ),
      ),
    );
  }

  Widget _buildInstructions(BuildContext context, VikingTTTState state) {
    String instructions;
    if (state.winner != null || state.isDraw) {
      instructions = 'Tap the refresh button to play again';
    } else if (state.phase == GamePhase.placement) {
      instructions =
          'Place your 3 pieces on the board. Tap any empty space to place.';
    } else {
      instructions = state.selectedPosition == null
          ? 'Select one of your pieces to move'
          : 'Tap an empty space to move your piece';
    }

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20),
      child: Card(
        color: Theme.of(context).colorScheme.secondaryContainer,
        child: Padding(
          padding: const EdgeInsets.all(12),
          child: Row(
            children: [
              Icon(
                Icons.info_outline,
                color: Theme.of(context).colorScheme.onSecondaryContainer,
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Text(
                  instructions,
                  style: TextStyle(
                    color: Theme.of(context).colorScheme.onSecondaryContainer,
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class GridPainter extends CustomPainter {
  final Color color;

  GridPainter({required this.color});

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = color
      ..strokeWidth = 2;

    final cellSize = size.width / 3;

    // Vertical lines
    for (int i = 1; i < 3; i++) {
      canvas.drawLine(
        Offset(i * cellSize, 0),
        Offset(i * cellSize, size.height),
        paint,
      );
    }

    // Horizontal lines
    for (int i = 1; i < 3; i++) {
      canvas.drawLine(
        Offset(0, i * cellSize),
        Offset(size.width, i * cellSize),
        paint,
      );
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}
