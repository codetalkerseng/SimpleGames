import fs from 'fs';

// Generate firebase-key.json from environment variables and hardcoded service account details
// IMPORTANT: Replace the placeholder values below with your actual Firebase service account details
// Get these from your Firebase service account JSON key file (Google Cloud Console)

// Get private key from environment and handle different newline formats
let privateKey = process.env.FIREBASE_PRIVATE_KEY;

if (!privateKey) {
  console.error('❌ ERROR: FIREBASE_PRIVATE_KEY environment variable is not set');
  process.exit(1);
}

// Handle different newline formats:
// 1. If key contains literal \n strings (common in CI/CD), convert to actual newlines
// 2. Some systems use \\n (double backslash), handle that too
privateKey = privateKey
  .replace(/\\\\n/g, '\n')  // Handle double backslash first
  .replace(/\\n/g, '\n');    // Handle single backslash

// Validate the key format
if (!privateKey.includes('BEGIN PRIVATE KEY') || !privateKey.includes('END PRIVATE KEY')) {
  console.error('❌ ERROR: Private key does not appear to be in correct format');
  console.error('Expected format: -----BEGIN PRIVATE KEY-----\\n...\\n-----END PRIVATE KEY-----\\n');
  process.exit(1);
}

const credentials = {
  type: "service_account",
  project_id: "solitear-91ba6",
  private_key_id: "28e4b1771772e71d59893fb3a26ccf7e0f1a3a79",
  private_key: privateKey,
  client_email: "firebase-adminsdk-fbsvc@solitear-91ba6.iam.gserviceaccount.com",
  client_id: "116409103134931765119",
  auth_uri: "https://accounts.google.com/o/oauth2/auth",
  token_uri: "https://oauth2.googleapis.com/token",
  auth_provider_x509_cert_url: "https://www.googleapis.com/oauth2/v1/certs",
  client_x509_cert_url: "https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-fbsvc@solitear-91ba6.iam.gserviceaccount.com",
  universe_domain: "googleapis.com"
};

// Validate that placeholder values have been replaced
const placeholders = ['[YOUR_PRIVATE_KEY_ID]', '[YOUR_CLIENT_EMAIL]', '[YOUR_CLIENT_ID]'];
const credentialsString = JSON.stringify(credentials);
const foundPlaceholders = placeholders.filter(p => credentialsString.includes(p));

if (foundPlaceholders.length > 0) {
  console.error('❌ ERROR: Please update the following placeholders in gen-firebase-key-json.js:');
  foundPlaceholders.forEach(p => console.error(`   - ${p}`));
  console.error('\nRefer to APPFLOW_SETUP.md for instructions.');
  process.exit(1);
}

// Write the credentials to firebase-key.json
try {
  fs.writeFileSync('./firebase-key.json', JSON.stringify(credentials, null, 2));
  console.log('✅ firebase-key.json generated successfully');
  console.log(`   Project: ${credentials.project_id}`);
  console.log(`   Service Account: ${credentials.client_email}`);
} catch (error) {
  console.error('❌ ERROR: Failed to write firebase-key.json:', error.message);
  process.exit(1);
}
