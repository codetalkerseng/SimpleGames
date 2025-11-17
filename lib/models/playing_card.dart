enum Suit { hearts, diamonds, clubs, spades }

enum CardColor { red, black }

class PlayingCard {
  final Suit suit;
  final int rank; // 1 (Ace) to 13 (King)
  bool isFaceUp;

  PlayingCard({
    required this.suit,
    required this.rank,
    this.isFaceUp = false,
  });

  CardColor get color {
    return (suit == Suit.hearts || suit == Suit.diamonds)
        ? CardColor.red
        : CardColor.black;
  }

  String get rankString {
    switch (rank) {
      case 1:
        return 'A';
      case 11:
        return 'J';
      case 12:
        return 'Q';
      case 13:
        return 'K';
      default:
        return rank.toString();
    }
  }

  String get suitSymbol {
    switch (suit) {
      case Suit.hearts:
        return '♥';
      case Suit.diamonds:
        return '♦';
      case Suit.clubs:
        return '♣';
      case Suit.spades:
        return '♠';
    }
  }

  PlayingCard copyWith({bool? isFaceUp}) {
    return PlayingCard(
      suit: suit,
      rank: rank,
      isFaceUp: isFaceUp ?? this.isFaceUp,
    );
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is PlayingCard && other.suit == suit && other.rank == rank;
  }

  @override
  int get hashCode => suit.hashCode ^ rank.hashCode;

  @override
  String toString() => '$rankString$suitSymbol';
}
