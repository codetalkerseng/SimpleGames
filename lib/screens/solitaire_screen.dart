import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/solitaire_state.dart';
import '../models/playing_card.dart';
import '../widgets/card_widget.dart';

class SolitaireScreen extends StatelessWidget {
  const SolitaireScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (_) => SolitaireState()..initGame(drawCount: 3, maxPasses: 0),
      child: const SolitaireGameView(),
    );
  }
}

class SolitaireGameView extends StatelessWidget {
  const SolitaireGameView({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Solitaire'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () {
              context.read<SolitaireState>().resetGame();
            },
          ),
          PopupMenuButton<int>(
            onSelected: (value) {
              final state = context.read<SolitaireState>();
              state.initGame(drawCount: value, maxPasses: 0);
            },
            itemBuilder: (context) => [
              const PopupMenuItem(value: 1, child: Text('Draw 1 card')),
              const PopupMenuItem(value: 3, child: Text('Draw 3 cards')),
            ],
          ),
        ],
      ),
      body: Consumer<SolitaireState>(
        builder: (context, state, child) {
          if (!state.isInitialized) {
            return const Center(child: CircularProgressIndicator());
          }

          if (state.isGameWon) {
            return _buildWinScreen(context, state);
          }

          return SingleChildScrollView(
            child: Padding(
              padding: const EdgeInsets.all(8.0),
              child: Column(
                children: [
                  _buildTopRow(context, state),
                  const SizedBox(height: 20),
                  _buildTableau(context, state),
                ],
              ),
            ),
          );
        },
      ),
      floatingActionButton: Consumer<SolitaireState>(
        builder: (context, state, child) {
          return FloatingActionButton(
            onPressed: () => state.autoMoveToFoundation(),
            child: const Icon(Icons.auto_fix_high),
          );
        },
      ),
    );
  }

  Widget _buildWinScreen(BuildContext context, SolitaireState state) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Icon(Icons.celebration, size: 100, color: Colors.amber),
          const SizedBox(height: 20),
          const Text(
            'You Win!',
            style: TextStyle(fontSize: 32, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 20),
          ElevatedButton(
            onPressed: () => state.resetGame(),
            child: const Text('Play Again'),
          ),
        ],
      ),
    );
  }

  Widget _buildTopRow(BuildContext context, SolitaireState state) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Row(
          children: [
            // Stock pile
            GestureDetector(
              onTap: () => state.drawFromStock(),
              child: CardWidget(
                card: state.stock.isNotEmpty
                    ? PlayingCard(suit: Suit.hearts, rank: 1, isFaceUp: false)
                    : null,
                isPlaceholder: state.stock.isEmpty,
                width: 70,
                height: 100,
              ),
            ),
            const SizedBox(width: 10),
            // Waste pile
            GestureDetector(
              onTap: state.waste.isEmpty
                  ? null
                  : () => _showWasteCardOptions(context, state),
              child: Stack(
                children: [
                  CardWidget(
                    isPlaceholder: true,
                    width: 70,
                    height: 100,
                  ),
                  if (state.waste.isNotEmpty)
                    CardWidget(
                      card: state.waste.last,
                      width: 70,
                      height: 100,
                    ),
                ],
              ),
            ),
          ],
        ),
        // Foundations
        Row(
          children: List.generate(4, (index) {
            return Padding(
              padding: const EdgeInsets.only(left: 10),
              child: DragTarget<Map<String, dynamic>>(
                onWillAccept: (data) {
                  if (data == null) return false;
                  final card = data['card'] as PlayingCard;
                  return state.canMoveToFoundation(card, index);
                },
                onAccept: (data) {
                  final source = data['source'] as String;
                  if (source == 'waste') {
                    state.moveWasteToFoundation(index);
                  } else if (source.startsWith('tableau-')) {
                    final tableauIndex =
                        int.parse(source.split('-')[1]);
                    state.moveTableauToFoundation(tableauIndex, index);
                  }
                },
                builder: (context, candidateData, rejectedData) {
                  return Stack(
                    children: [
                      CardWidget(
                        isPlaceholder: true,
                        width: 70,
                        height: 100,
                      ),
                      if (state.foundations[index].isNotEmpty)
                        CardWidget(
                          card: state.foundations[index].last,
                          width: 70,
                          height: 100,
                          onTap: () {
                            // Allow moving from foundation to tableau
                            _showFoundationCardOptions(context, state, index);
                          },
                        ),
                    ],
                  );
                },
              ),
            );
          }),
        ),
      ],
    );
  }

  Widget _buildTableau(BuildContext context, SolitaireState state) {
    return SizedBox(
      height: 500,
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: List.generate(7, (pileIndex) {
          return _buildTableauPile(context, state, pileIndex);
        }),
      ),
    );
  }

  Widget _buildTableauPile(
      BuildContext context, SolitaireState state, int pileIndex) {
    final pile = state.tableau[pileIndex];

    return DragTarget<Map<String, dynamic>>(
      onWillAccept: (data) {
        if (data == null) return false;
        final cards = data['cards'] as List<PlayingCard>;
        return state.canMoveToTableau(cards, pileIndex);
      },
      onAccept: (data) {
        final source = data['source'] as String;
        if (source == 'waste') {
          state.moveWasteToTableau(pileIndex);
        } else if (source.startsWith('tableau-')) {
          final parts = source.split('-');
          final fromPile = int.parse(parts[1]);
          final cardIndex = int.parse(parts[2]);
          state.moveTableauToTableau(fromPile, pileIndex, cardIndex);
        } else if (source.startsWith('foundation-')) {
          final foundationIndex = int.parse(source.split('-')[1]);
          state.moveFoundationToTableau(foundationIndex, pileIndex);
        }
      },
      builder: (context, candidateData, rejectedData) {
        return Container(
          width: 70,
          child: Stack(
            children: [
              CardWidget(
                isPlaceholder: true,
                width: 70,
                height: 100,
              ),
              ...List.generate(pile.length, (cardIndex) {
                final card = pile[cardIndex];
                return Positioned(
                  top: cardIndex * 25.0,
                  child: Draggable<Map<String, dynamic>>(
                    data: {
                      'source': 'tableau-$pileIndex-$cardIndex',
                      'card': card,
                      'cards': pile.sublist(cardIndex),
                    },
                    feedback: Opacity(
                      opacity: 0.7,
                      child: CardWidget(
                        card: card,
                        width: 70,
                        height: 100,
                      ),
                    ),
                    childWhenDragging: Container(),
                    child: CardWidget(
                      card: card,
                      width: 70,
                      height: 100,
                      onTap: card.isFaceUp
                          ? () => _showTableauCardOptions(
                              context, state, pileIndex, cardIndex)
                          : null,
                    ),
                  ),
                );
              }),
            ],
          ),
        );
      },
    );
  }

  void _showWasteCardOptions(BuildContext context, SolitaireState state) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Move Card'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ...List.generate(4, (foundationIndex) {
              if (state.canMoveToFoundation(
                  state.waste.last, foundationIndex)) {
                return ListTile(
                  title: Text('To Foundation ${foundationIndex + 1}'),
                  onTap: () {
                    state.moveWasteToFoundation(foundationIndex);
                    Navigator.pop(context);
                  },
                );
              }
              return const SizedBox.shrink();
            }),
            ...List.generate(7, (tableauIndex) {
              if (state.canMoveToTableau([state.waste.last], tableauIndex)) {
                return ListTile(
                  title: Text('To Tableau ${tableauIndex + 1}'),
                  onTap: () {
                    state.moveWasteToTableau(tableauIndex);
                    Navigator.pop(context);
                  },
                );
              }
              return const SizedBox.shrink();
            }),
          ],
        ),
      ),
    );
  }

  void _showTableauCardOptions(
      BuildContext context, SolitaireState state, int pileIndex, int cardIndex) {
    final card = state.tableau[pileIndex][cardIndex];
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Move Card'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            if (cardIndex == state.tableau[pileIndex].length - 1)
              ...List.generate(4, (foundationIndex) {
                if (state.canMoveToFoundation(card, foundationIndex)) {
                  return ListTile(
                    title: Text('To Foundation ${foundationIndex + 1}'),
                    onTap: () {
                      state.moveTableauToFoundation(pileIndex, foundationIndex);
                      Navigator.pop(context);
                    },
                  );
                }
                return const SizedBox.shrink();
              }),
            ...List.generate(7, (tableauIndex) {
              if (tableauIndex != pileIndex) {
                final cards = state.tableau[pileIndex].sublist(cardIndex);
                if (state.canMoveToTableau(cards, tableauIndex)) {
                  return ListTile(
                    title: Text('To Tableau ${tableauIndex + 1}'),
                    onTap: () {
                      state.moveTableauToTableau(
                          pileIndex, tableauIndex, cardIndex);
                      Navigator.pop(context);
                    },
                  );
                }
              }
              return const SizedBox.shrink();
            }),
          ],
        ),
      ),
    );
  }

  void _showFoundationCardOptions(
      BuildContext context, SolitaireState state, int foundationIndex) {
    final card = state.foundations[foundationIndex].last;
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Move Card'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ...List.generate(7, (tableauIndex) {
              if (state.canMoveToTableau([card], tableauIndex)) {
                return ListTile(
                  title: Text('To Tableau ${tableauIndex + 1}'),
                  onTap: () {
                    state.moveFoundationToTableau(foundationIndex, tableauIndex);
                    Navigator.pop(context);
                  },
                );
              }
              return const SizedBox.shrink();
            }),
          ],
        ),
      ),
    );
  }
}
