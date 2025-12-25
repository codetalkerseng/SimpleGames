# Ionic AppFlow Firebase Deployment Setup

This guide walks you through setting up automated Firebase Hosting deployment via Ionic AppFlow.

## Overview

When you trigger a web build in AppFlow:
1. **Pre-build**: Generates `firebase-key.json` from AppFlow secrets
2. **Build**: Builds your Ionic app (`npm run build`)
3. **Post-build**: Deploys to Firebase Hosting automatically

## Prerequisites

- Ionic AppFlow account
- Firebase project created (ID: `solitear-91ba6`)
- Firebase CLI installed locally: `npm install -g firebase-tools`

## Step 1: Create Firebase Service Account

1. **Go to Google Cloud Console**:
   - Visit: https://console.cloud.google.com/
   - Select your project: `solitear-91ba6`

2. **Create Service Account**:
   - Navigate to: **IAM & Admin** → **Service Accounts**
   - Click **Create Service Account**
   - Name: `appflow-deployer`
   - Description: `Service account for Ionic AppFlow deployments`
   - Click **Create and Continue**

3. **Assign Role**:
   - Role: **Firebase Hosting Admin**
   - Click **Continue** → **Done**

4. **Create JSON Key**:
   - Click on the newly created service account
   - Go to **Keys** tab
   - Click **Add Key** → **Create new key**
   - Choose **JSON** format
   - Click **Create** (downloads the JSON file)

5. **Keep this JSON file safe** - you'll need values from it in the next steps

## Step 2: Update gen-firebase-key-json.js

Open `gen-firebase-key-json.js` and replace the placeholder values with data from your downloaded JSON key file:

```javascript
const credentials = {
  type: "service_account",
  project_id: "solitear-91ba6", // ✅ Already set
  private_key_id: "[YOUR_PRIVATE_KEY_ID]", // ⚠️ Replace from JSON
  private_key: process.env.FIREBASE_PRIVATE_KEY?.replace(/\\n/g, '\n'), // ✅ From AppFlow
  client_email: "[YOUR_CLIENT_EMAIL]", // ⚠️ Replace from JSON
  client_id: "[YOUR_CLIENT_ID]", // ⚠️ Replace from JSON
  auth_uri: "https://accounts.google.com/o/oauth2/auth",
  token_uri: "https://oauth2.googleapis.com/token",
  auth_provider_x509_cert_url: "https://www.googleapis.com/oauth2/v1/certs",
  client_x509_cert_url: "https://www.googleapis.com/robot/v1/metadata/x509/[YOUR_CLIENT_EMAIL]", // ⚠️ Update
  universe_domain: "googleapis.com"
};
```

**Values to copy from your JSON file:**
- `private_key_id` → Copy from `private_key_id` field
- `client_email` → Copy from `client_email` field (looks like: `appflow-deployer@solitear-91ba6.iam.gserviceaccount.com`)
- `client_id` → Copy from `client_id` field
- `client_x509_cert_url` → Update the `[YOUR_CLIENT_EMAIL]` part with your `client_email`

**DO NOT commit the private_key to Git** - it will be stored as a secret in AppFlow.

## Step 3: Configure Ionic AppFlow Secrets

1. **Log in to Ionic AppFlow**:
   - Visit: https://dashboard.ionicframework.com/

2. **Go to your app** → **Environments**

3. **Create a new Environment** (or edit existing):
   - Name: `Production` (or `Dev`)
   - Click **Create** (or **Edit**)

4. **Add Secret: FIREBASE_PRIVATE_KEY**:
   - Click **Add Secret**
   - Name: `FIREBASE_PRIVATE_KEY`
   - Value: Copy the `private_key` value from your JSON file **EXACTLY as it appears**

   **CRITICAL**: The private key in your JSON file contains `\n` characters (backslash-n). These are escape sequences representing newlines. You must copy them EXACTLY as shown, including the backslashes.

   **Example of what it should look like when you copy it:**
   ```
   -----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC...\nYOUR_KEY_DATA_HERE...\n-----END PRIVATE KEY-----\n
   ```

   **Method 1 - Copy from JSON file** (Recommended):
   - Open your downloaded JSON file in a text editor
   - Find the `"private_key"` field
   - Copy EVERYTHING between the quotes (including all `\n` sequences)
   - Paste into AppFlow secret value

   **Method 2 - Use command line** (if on Mac/Linux):
   ```bash
   # This prints the private_key value with \n visible
   cat your-service-account.json | jq -r '.private_key'
   # Copy the entire output and paste into AppFlow
   ```

   ⚠️ **Common Mistakes to Avoid:**
   - Don't remove the `\n` sequences
   - Don't convert `\n` to actual newlines (it should be backslash-n, not a line break)
   - Don't add or remove quotes

   - Click **Save**

5. **Add Variable: GOOGLE_APPLICATION_CREDENTIALS**:
   - Click **Add Variable**
   - Name: `GOOGLE_APPLICATION_CREDENTIALS`
   - Value: `./firebase-key.json`
   - Click **Save**

## Step 4: Commit and Push Changes

Commit all the new files to your repository:

```bash
git add .
git commit -m "Add AppFlow Firebase deployment automation"
git push
```

**Files added:**
- `web-deploy.sh` - Deployment script
- `gen-firebase-key-json.js` - Service account key generator
- Updated `package.json` - Pre/post build scripts
- Updated `.gitignore` - Excludes `firebase-key.json`

## Step 5: Trigger AppFlow Build

1. **In AppFlow Dashboard** → **Builds**
2. Click **New Build**
3. Select:
   - **Commit**: Latest commit
   - **Target Platform**: **Web**
   - **Build Stack**: Latest available
   - **Environment**: The environment you configured (e.g., `Production`)
4. Click **Build**

## Step 6: Monitor Deployment

Watch the build logs in AppFlow:

1. **Pre-build**: Should show "✅ firebase-key.json generated successfully"
2. **Build**: Angular builds the app to `www/` directory
3. **Post-build**: Should show "Deploying to Firebase Hosting..."

## Step 7: Verify Deployment

After the build completes:

1. **Check Firebase Console**:
   - Visit: https://console.firebase.google.com/project/solitear-91ba6/hosting
   - Verify the latest deployment timestamp

2. **Visit your live site**:
   - URL: `https://solitear-91ba6.web.app`
   - Or: `https://solitear-91ba6.firebaseapp.com`

## Troubleshooting

### "error:1E08010C:DECODER routines::unsupported" or "Failed to authenticate"

This error occurs when the private key format is incorrect. The most common cause is improper newline handling.

**Solution**:
1. **Verify the FIREBASE_PRIVATE_KEY secret in AppFlow**:
   - The value should contain literal `\n` sequences (backslash-n), NOT actual newlines
   - It should be a single line like: `-----BEGIN PRIVATE KEY-----\nMIIEv...\n-----END PRIVATE KEY-----\n`
   - NOT multiple lines like this:
     ```
     -----BEGIN PRIVATE KEY-----
     MIIEv...
     -----END PRIVATE KEY-----
     ```

2. **Re-copy the private_key from your JSON file**:
   - Open the service account JSON file in a text editor (NOT a word processor)
   - The `"private_key"` field should look like one long line with `\n` in it
   - Copy the value between the quotes EXACTLY as shown
   - Update the AppFlow secret with this exact value

3. **Verify placeholders are updated**:
   - Check the pre-build logs - if you see errors about placeholders, you need to update `gen-firebase-key-json.js`
   - Replace `[YOUR_PRIVATE_KEY_ID]`, `[YOUR_CLIENT_EMAIL]`, and `[YOUR_CLIENT_ID]`

4. **Test the key format**:
   - The updated script now validates the key format and will show clear error messages
   - Check the pre-build logs for validation errors

### "firebase-key.json not found"
- Verify `FIREBASE_PRIVATE_KEY` secret is set in AppFlow environment
- Check pre-build logs for errors in `gen-firebase-key-json.js`
- Ensure the secret name is EXACTLY `FIREBASE_PRIVATE_KEY` (case-sensitive)

### "Permission denied" during deployment
- Verify service account has **Firebase Hosting Admin** role
- Check that `GOOGLE_APPLICATION_CREDENTIALS` variable is set
- Ensure service account is enabled in Google Cloud Console

### "Module not found" errors
- Ensure `firebase-tools` is in `devDependencies`
- Verify `"type": "module"` is in `package.json`
- Clear AppFlow cache and rebuild

### Build works but deployment doesn't run
- Verify `CI_PLATFORM` is set to `web` in AppFlow
- Check that post-build script runs in build logs
- Ensure you selected a "Web" build (not iOS/Android)

### Pre-build script fails with "Please update placeholders"
- You need to edit `gen-firebase-key-json.js` in your repository
- Replace `[YOUR_PRIVATE_KEY_ID]`, `[YOUR_CLIENT_EMAIL]`, and `[YOUR_CLIENT_ID]` with actual values
- Commit and push the updated file

## Security Notes

- **Never commit `firebase-key.json`** - it's generated at build time
- **Never commit the service account JSON** - store `private_key` in AppFlow secrets
- **Rotate service account keys** periodically for security
- **Delete downloaded JSON files** from your local machine after extracting values

## Local Testing (Optional)

To test the deployment locally:

1. Set environment variable:
   ```bash
   export FIREBASE_PRIVATE_KEY="your-private-key-here"
   ```

2. Generate key file:
   ```bash
   node gen-firebase-key-json.js
   ```

3. Run deployment:
   ```bash
   ./web-deploy.sh
   ```

4. Clean up:
   ```bash
   rm firebase-key.json
   unset FIREBASE_PRIVATE_KEY
   ```

## Additional Resources

- [Firebase Service Accounts](https://firebase.google.com/docs/admin/setup#initialize-sdk)
- [Ionic AppFlow Environments](https://ionic.io/docs/appflow/automation/environments)
- [Firebase Hosting](https://firebase.google.com/docs/hosting)

---

**Your Firebase Project**: `solitear-91ba6`
**Live URL**: https://solitear-91ba6.web.app
