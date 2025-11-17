import 'dart:math';
import 'package:flutter/foundation.dart';
import 'playing_card.dart';

class SolitaireState extends ChangeNotifier {
  // Tableau - 7 piles
  List<List<PlayingCard>> tableau = List.generate(7, (_) => []);

  // Foundations - 4 piles (one per suit)
  List<List<PlayingCard>> foundations = List.generate(4, (_) => []);

  // Stock (face-down deck)
  List<PlayingCard> stock = [];

  // Waste (face-up from stock)
  List<PlayingCard> waste = [];

  // Number of cards to turn from stock (1 or 3)
  int drawCount = 3;

  // Number of passes through deck (0 = unlimited)
  int maxPasses = 0;
  int currentPass = 0;

  // Selected card info for dragging
  List<PlayingCard>? selectedCards;
  int? selectedTableauIndex;

  bool _isInitialized = false;

  void initGame({int drawCount = 3, int maxPasses = 0}) {
    this.drawCount = drawCount;
    this.maxPasses = maxPasses;
    currentPass = 0;

    // Create and shuffle deck
    final deck = _createDeck();
    deck.shuffle(Random());

    // Clear all piles
    tableau = List.generate(7, (_) => []);
    foundations = List.generate(4, (_) => []);
    stock = [];
    waste = [];
    selectedCards = null;
    selectedTableauIndex = null;

    // Deal tableau
    int cardIndex = 0;
    for (int i = 0; i < 7; i++) {
      for (int j = 0; j <= i; j++) {
        final card = deck[cardIndex++];
        card.isFaceUp = (j == i); // Only top card is face up
        tableau[i].add(card);
      }
    }

    // Remaining cards go to stock
    for (int i = cardIndex; i < deck.length; i++) {
      stock.add(deck[i]);
    }

    _isInitialized = true;
    notifyListeners();
  }

  List<PlayingCard> _createDeck() {
    final deck = <PlayingCard>[];
    for (final suit in Suit.values) {
      for (int rank = 1; rank <= 13; rank++) {
        deck.add(PlayingCard(suit: suit, rank: rank));
      }
    }
    return deck;
  }

  void drawFromStock() {
    if (stock.isEmpty) {
      if (maxPasses > 0 && currentPass >= maxPasses) {
        return; // Max passes reached
      }

      // Move waste back to stock
      stock = waste.reversed.toList();
      for (var card in stock) {
        card.isFaceUp = false;
      }
      waste = [];
      currentPass++;
    } else {
      // Draw cards from stock to waste
      final cardsToDraw = min(drawCount, stock.length);
      for (int i = 0; i < cardsToDraw; i++) {
        final card = stock.removeLast();
        card.isFaceUp = true;
        waste.add(card);
      }
    }

    notifyListeners();
  }

  bool canMoveToFoundation(PlayingCard card, int foundationIndex) {
    final foundation = foundations[foundationIndex];

    if (foundation.isEmpty) {
      return card.rank == 1; // Only Ace can start a foundation
    }

    final topCard = foundation.last;
    return card.suit == topCard.suit && card.rank == topCard.rank + 1;
  }

  bool canMoveToTableau(List<PlayingCard> cards, int tableauIndex) {
    final targetPile = tableau[tableauIndex];

    if (targetPile.isEmpty) {
      return cards.first.rank == 13; // Only King can go on empty tableau
    }

    final topCard = targetPile.last;
    final movingCard = cards.first;

    return topCard.color != movingCard.color &&
        topCard.rank == movingCard.rank + 1;
  }

  void moveWasteToFoundation(int foundationIndex) {
    if (waste.isEmpty) return;

    final card = waste.last;
    if (canMoveToFoundation(card, foundationIndex)) {
      waste.removeLast();
      foundations[foundationIndex].add(card);
      notifyListeners();
      _checkWin();
    }
  }

  void moveWasteToTableau(int tableauIndex) {
    if (waste.isEmpty) return;

    final card = waste.last;
    if (canMoveToTableau([card], tableauIndex)) {
      waste.removeLast();
      tableau[tableauIndex].add(card);
      notifyListeners();
    }
  }

  void moveTableauToFoundation(int tableauIndex, int foundationIndex) {
    if (tableau[tableauIndex].isEmpty) return;

    final card = tableau[tableauIndex].last;
    if (canMoveToFoundation(card, foundationIndex)) {
      tableau[tableauIndex].removeLast();
      foundations[foundationIndex].add(card);

      // Flip the next card if any
      if (tableau[tableauIndex].isNotEmpty) {
        tableau[tableauIndex].last.isFaceUp = true;
      }

      notifyListeners();
      _checkWin();
    }
  }

  void moveTableauToTableau(
      int fromIndex, int toIndex, int cardIndex) {
    if (tableau[fromIndex].isEmpty) return;

    final cards = tableau[fromIndex].sublist(cardIndex);
    if (canMoveToTableau(cards, toIndex)) {
      tableau[fromIndex].removeRange(cardIndex, tableau[fromIndex].length);
      tableau[toIndex].addAll(cards);

      // Flip the next card if any
      if (tableau[fromIndex].isNotEmpty) {
        tableau[fromIndex].last.isFaceUp = true;
      }

      notifyListeners();
    }
  }

  void moveFoundationToTableau(int foundationIndex, int tableauIndex) {
    if (foundations[foundationIndex].isEmpty) return;

    final card = foundations[foundationIndex].last;
    if (canMoveToTableau([card], tableauIndex)) {
      foundations[foundationIndex].removeLast();
      tableau[tableauIndex].add(card);
      notifyListeners();
    }
  }

  void autoMoveToFoundation() {
    // Try to move top waste card to foundation
    if (waste.isNotEmpty) {
      for (int i = 0; i < 4; i++) {
        if (canMoveToFoundation(waste.last, i)) {
          moveWasteToFoundation(i);
          return;
        }
      }
    }

    // Try to move top tableau cards to foundation
    for (int t = 0; t < 7; t++) {
      if (tableau[t].isNotEmpty) {
        final card = tableau[t].last;
        for (int f = 0; f < 4; f++) {
          if (canMoveToFoundation(card, f)) {
            moveTableauToFoundation(t, f);
            return;
          }
        }
      }
    }
  }

  void _checkWin() {
    // Win condition: all 52 cards in foundations
    final totalInFoundations = foundations.fold<int>(
      0,
      (sum, foundation) => sum + foundation.length,
    );

    if (totalInFoundations == 52) {
      // Game won!
      // You could add a callback or state here to show victory screen
    }
  }

  bool get isGameWon {
    return foundations.fold<int>(
          0,
          (sum, foundation) => sum + foundation.length,
        ) ==
        52;
  }

  bool get isInitialized => _isInitialized;

  void resetGame() {
    initGame(drawCount: drawCount, maxPasses: maxPasses);
  }
}
