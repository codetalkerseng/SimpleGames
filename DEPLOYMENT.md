# Firebase Deployment Guide

This guide will help you deploy your Solitaire game to Firebase Hosting for free.

## Prerequisites

- Node.js and npm installed
- A Google account

## Step 1: Create a Firebase Project

1. Go to the [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project" or "Create a project"
3. Enter a project name (e.g., "solitaire-game")
4. Follow the setup wizard (you can disable Google Analytics for this simple project)
5. Click "Create project"

## Step 2: Initialize Firebase in Your Project

1. Login to Firebase from the terminal:
   ```bash
   firebase login
   ```
   This will open a browser window for authentication.

2. Update the `.firebaserc` file with your project ID:
   - Open `.firebaserc`
   - Replace `"your-project-id"` with your actual Firebase project ID
   - You can find your project ID in the Firebase Console

## Step 3: Build Your App

```bash
npm run build
```

This creates an optimized production build in the `www` directory.

## Step 4: Deploy to Firebase

```bash
firebase deploy
```

After deployment completes, you'll see a hosting URL like:
```
Hosting URL: https://your-project-id.web.app
```

## Step 5: Access Your Game

Visit the hosting URL provided by Firebase to play your solitaire game!

## Updating Your Game

Whenever you make changes:

1. Build the app:
   ```bash
   npm run build
   ```

2. Deploy to Firebase:
   ```bash
   firebase deploy
   ```

## Firebase Free Tier Limits

The Firebase free tier (Spark plan) includes:
- **Hosting Storage**: 10 GB
- **Hosting Transfer**: 360 MB/day
- **Custom Domain**: 1 free custom domain

This is more than enough for a simple solitaire game!

## Troubleshooting

### "Project not found" error
- Make sure you've updated the project ID in `.firebaserc`
- Verify the project exists in the Firebase Console

### Build errors
- Run `npm install` to ensure all dependencies are installed
- Check that you're using a compatible Node.js version (18+)

### Deploy fails
- Make sure you're logged in: `firebase login`
- Verify you have permissions for the Firebase project

## Optional: Custom Domain

To use a custom domain:

1. Go to Firebase Console → Hosting
2. Click "Add custom domain"
3. Follow the DNS configuration instructions

Enjoy your game!
