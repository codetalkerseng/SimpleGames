import 'package:flutter/material.dart';
import '../models/playing_card.dart';

class CardWidget extends StatelessWidget {
  final PlayingCard? card;
  final bool isPlaceholder;
  final VoidCallback? onTap;
  final double width;
  final double height;

  const CardWidget({
    super.key,
    this.card,
    this.isPlaceholder = false,
    this.onTap,
    this.width = 60,
    this.height = 85,
  });

  @override
  Widget build(BuildContext context) {
    if (card == null && !isPlaceholder) {
      return SizedBox(width: width, height: height);
    }

    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: width,
        height: height,
        decoration: BoxDecoration(
          color: isPlaceholder
              ? Colors.transparent
              : (card!.isFaceUp ? Colors.white : Colors.blue[800]),
          border: Border.all(
            color: isPlaceholder
                ? Colors.grey.withOpacity(0.5)
                : Colors.black,
            width: isPlaceholder ? 2 : 1,
          ),
          borderRadius: BorderRadius.circular(8),
          boxShadow: isPlaceholder
              ? []
              : [
                  BoxShadow(
                    color: Colors.black.withOpacity(0.2),
                    blurRadius: 4,
                    offset: const Offset(2, 2),
                  ),
                ],
        ),
        child: _buildCardContent(context),
      ),
    );
  }

  Widget _buildCardContent(BuildContext context) {
    if (isPlaceholder) {
      return const SizedBox.shrink();
    }

    if (card == null || !card!.isFaceUp) {
      return Center(
        child: Container(
          width: width * 0.7,
          height: height * 0.8,
          decoration: BoxDecoration(
            color: Colors.blue[600],
            borderRadius: BorderRadius.circular(4),
            border: Border.all(color: Colors.white, width: 2),
          ),
          child: const Center(
            child: Icon(Icons.style, color: Colors.white, size: 20),
          ),
        ),
      );
    }

    final color = card!.color == CardColor.red ? Colors.red : Colors.black;

    return Padding(
      padding: const EdgeInsets.all(4),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            '${card!.rankString}${card!.suitSymbol}',
            style: TextStyle(
              color: color,
              fontSize: 16,
              fontWeight: FontWeight.bold,
            ),
          ),
          Expanded(
            child: Center(
              child: Text(
                card!.suitSymbol,
                style: TextStyle(
                  color: color,
                  fontSize: 32,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
          ),
          Align(
            alignment: Alignment.bottomRight,
            child: Transform.rotate(
              angle: 3.14159, // 180 degrees
              child: Text(
                '${card!.rankString}${card!.suitSymbol}',
                style: TextStyle(
                  color: color,
                  fontSize: 16,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
