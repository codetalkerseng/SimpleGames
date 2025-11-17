import 'package:flutter/material.dart';
import '../models/game_info.dart';
import '../screens/solitaire_screen.dart';
import '../screens/viking_ttt_screen.dart';

class GameRegistry {
  static final List<GameInfo> _games = [
    GameInfo(
      id: 'solitaire',
      name: 'Solitaire',
      description: 'Classic Klondike solitaire card game',
      icon: Icons.style,
      supportedModes: [GameMode.singlePlayer],
      screenBuilder: (mode) => const SolitaireScreen(),
    ),
    GameInfo(
      id: 'viking_ttt',
      name: 'Viking Tic Tac Toe',
      description: '3 pieces each, strategic placement game',
      icon: Icons.grid_3x3,
      supportedModes: [GameMode.singlePlayer, GameMode.multiPlayer],
      screenBuilder: (mode) => VikingTTTScreen(mode: mode),
    ),
  ];

  static List<GameInfo> getAllGames() => _games;

  static GameInfo? getGameById(String id) {
    try {
      return _games.firstWhere((game) => game.id == id);
    } catch (e) {
      return null;
    }
  }
}
