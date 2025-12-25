# Solitaire Game

A classic Solitaire (Klondike) card game built with Ionic and Angular, deployable to Firebase Hosting.

## Features

- **Classic Solitaire Gameplay**: Play the traditional Klondike Solitaire
- **Drag and Drop**: Intuitive drag-and-drop interface for moving cards
- **Score Tracking**: Keep track of your moves and score
- **Responsive Design**: Works on desktop, tablet, and mobile devices
- **Progressive Web App**: Installable on mobile devices

## Tech Stack

- **Ionic 8**: Mobile-first UI framework
- **Angular 18**: Frontend framework
- **TypeScript**: Type-safe development
- **Firebase Hosting**: Free deployment platform

## Getting Started

### Prerequisites

- Node.js 18+ and npm
- Firebase account (for deployment)

### Installation

1. Clone the repository:
   ```bash
   git clone <your-repo-url>
   cd SimpleGames
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Run the development server:
   ```bash
   npm start
   ```

4. Open your browser to `http://localhost:4200`

### Building

```bash
npm run build
```

The production build will be in the `www` directory.

## Deployment

See [DEPLOYMENT.md](DEPLOYMENT.md) for detailed instructions on deploying to Firebase Hosting.

Quick steps:
1. Create a Firebase project
2. Login: `firebase login`
3. Update `.firebaserc` with your project ID
4. Build: `npm run build`
5. Deploy: `firebase deploy`

## Game Rules

**Objective**: Move all cards to the four foundation piles (one per suit) in ascending order from Ace to King.

**Setup**:
- Seven tableau piles with 1-7 cards each
- Top card of each pile is face up
- Remaining cards form the stock pile

**Gameplay**:
- Click the stock to draw 3 cards at a time
- Drag cards between tableau piles (alternating colors, descending order)
- Build foundation piles by suit from Ace to King
- Only Kings can be placed on empty tableau piles
- Only Aces can start foundation piles

## Project Structure

```
src/
├── app/
│   ├── home/              # Main game page
│   ├── models/            # Card and game models
│   ├── services/          # Game logic service
│   └── app.module.ts      # App configuration
├── theme/                 # Ionic theme variables
└── global.scss            # Global styles
```

## Firebase Free Tier

This app uses only Firebase Hosting, which includes:
- 10 GB storage
- 360 MB/day transfer
- Free SSL certificate
- Custom domain support

Perfect for hosting this game at no cost!

## License

MIT