import 'package:flutter/material.dart';

enum GameMode { singlePlayer, multiPlayer }

class GameInfo {
  final String id;
  final String name;
  final String description;
  final IconData icon;
  final List<GameMode> supportedModes;
  final Widget Function(GameMode mode) screenBuilder;

  const GameInfo({
    required this.id,
    required this.name,
    required this.description,
    required this.icon,
    required this.supportedModes,
    required this.screenBuilder,
  });
}
