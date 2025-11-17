# SimpleGames

A collection of ad-free phone games built with Flutter, supporting both Android and iOS platforms with multiplayer capabilities.

## Features

- **Ad-Free Gaming**: No advertisements, just pure gaming experience
- **Cross-Platform**: Works on both Android and iOS devices
- **Multiplayer Ready**: Infrastructure foundation for multiplayer gameplay
- **Extensible Menu System**: Easy to add new games
- **Beautiful UI**: Modern Material Design 3 with light/dark theme support

## Games Included

### 1. Solitaire (Klondike)

Classic solitaire card game with the following features:
- Standard 52-card deck
- Seven tableau piles with cascading layout
- Four foundation piles (build from Ace to King by suit)
- Stock and waste piles
- Configurable draw count (1 or 3 cards)
- Drag and drop support
- Auto-move functionality
- Win detection

**How to Play:**
1. Build foundation piles by suit from Ace to King
2. Arrange tableau piles in descending order with alternating colors
3. Only Kings can be placed on empty tableau piles
4. Tap cards to see available moves or drag them to valid positions
5. Use the auto-move button to automatically move eligible cards to foundations

### 2. Viking Tic Tac Toe

A strategic variant of Tic Tac Toe with unique mechanics:
- Each player has exactly 3 pieces
- Two phases: Placement and Movement
- Placement Phase: Take turns placing your 3 pieces on the board
- Movement Phase: After all pieces are placed, move your pieces to any empty space
- First player to get 3 in a row wins
- Supports both single-player and multiplayer modes

**How to Play:**
1. **Placement Phase**: Tap empty spaces to place your 3 pieces
2. **Movement Phase**: Tap one of your pieces to select it, then tap an empty space to move
3. Get three of your pieces in a row (horizontal, vertical, or diagonal) to win

## Project Structure

```
lib/
├── main.dart                 # App entry point
├── models/                   # Data models
│   ├── game_info.dart       # Game metadata model
│   ├── playing_card.dart    # Card model for Solitaire
│   ├── solitaire_state.dart # Solitaire game state
│   └── viking_ttt_state.dart# Viking TTT game state
├── screens/                  # Game screens
│   ├── menu_screen.dart     # Main menu
│   ├── solitaire_screen.dart# Solitaire game
│   └── viking_ttt_screen.dart# Viking TTT game
├── widgets/                  # Reusable widgets
│   └── card_widget.dart     # Playing card widget
├── services/                 # Services
│   └── multiplayer_service.dart # Multiplayer infrastructure
└── utils/                    # Utilities
    └── game_registry.dart   # Game registration system
```

## Getting Started

### Prerequisites

- Flutter SDK (3.0.0 or higher)
- Dart SDK (3.0.0 or higher)
- Android Studio / Xcode (for mobile development)
- A device or emulator

### Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd SimpleGames
```

2. Install dependencies:
```bash
flutter pub get
```

3. Run the app:
```bash
# For Android
flutter run

# For iOS
flutter run

# For a specific device
flutter devices  # List available devices
flutter run -d <device-id>
```

### Building for Production

**Android:**
```bash
flutter build apk --release
# Output: build/app/outputs/flutter-apk/app-release.apk

# Or for App Bundle (recommended for Play Store)
flutter build appbundle --release
```

**iOS:**
```bash
flutter build ios --release
# Then open ios/Runner.xcworkspace in Xcode to archive and distribute
```

## Adding New Games

The app is designed to make adding new games easy:

1. Create a new game state model in `lib/models/`
2. Create a new game screen in `lib/screens/`
3. Register the game in `lib/utils/game_registry.dart`:

```dart
GameInfo(
  id: 'my_game',
  name: 'My Game',
  description: 'Description of my game',
  icon: Icons.games,
  supportedModes: [GameMode.singlePlayer],
  screenBuilder: (mode) => MyGameScreen(),
),
```

The game will automatically appear in the menu!

## Multiplayer Support

The app includes a foundation for multiplayer functionality:
- `MultiplayerService` in `lib/services/multiplayer_service.dart`
- WebSocket support via `web_socket_channel` package
- Connection status tracking
- Game room management placeholders

To implement full multiplayer:
1. Set up a WebSocket server (Node.js, Python, etc.)
2. Implement the connection logic in `MultiplayerService`
3. Add game-specific multiplayer state synchronization
4. Update game screens to handle multiplayer events

## Dependencies

- `flutter`: SDK for cross-platform development
- `provider`: State management solution
- `shared_preferences`: Local data persistence
- `web_socket_channel`: WebSocket client for multiplayer

## Platform Support

- ✅ Android (API 21+)
- ✅ iOS (12.0+)
- ✅ Web (experimental)
- ✅ Desktop (Windows, macOS, Linux - experimental)

## Development Tips

### Running Tests
```bash
flutter test
```

### Code Analysis
```bash
flutter analyze
```

### Formatting
```bash
flutter format lib/
```

## Future Enhancements

- [ ] Add more games (Chess, Checkers, Poker, etc.)
- [ ] Implement full multiplayer server
- [ ] Add game statistics and leaderboards
- [ ] Achievement system
- [ ] Sound effects and music
- [ ] Animations and transitions
- [ ] Customizable themes
- [ ] Save/load game state
- [ ] Undo/redo functionality for Solitaire
- [ ] AI opponents for single-player modes

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues for bugs and feature requests.

## License

This project is open source and available under the MIT License.

## Credits

Created with Flutter - Google's UI toolkit for building beautiful, natively compiled applications.

---

Enjoy ad-free gaming! 🎮
