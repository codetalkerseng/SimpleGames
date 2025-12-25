import fs from 'fs';

// Generate firebase-key.json from environment variables and hardcoded service account details
// IMPORTANT: Replace the placeholder values below with your actual Firebase service account details
// Get these from your Firebase service account JSON key file (Google Cloud Console)

const credentials = {
  type: "service_account",
  project_id: "solitear-91ba6",
  private_key_id: "[YOUR_PRIVATE_KEY_ID]", // TODO: Replace with your private_key_id from service account JSON
  private_key: process.env.FIREBASE_PRIVATE_KEY?.replace(/\\n/g, '\n'), // This comes from AppFlow secret
  client_email: "[YOUR_CLIENT_EMAIL]", // TODO: Replace with your client_email (e.g., firebase-adminsdk-xxxxx@solitear-91ba6.iam.gserviceaccount.com)
  client_id: "[YOUR_CLIENT_ID]", // TODO: Replace with your client_id from service account JSON
  auth_uri: "https://accounts.google.com/o/oauth2/auth",
  token_uri: "https://oauth2.googleapis.com/token",
  auth_provider_x509_cert_url: "https://www.googleapis.com/oauth2/v1/certs",
  client_x509_cert_url: "https://www.googleapis.com/robot/v1/metadata/x509/[YOUR_CLIENT_EMAIL]", // TODO: Update with your client_email
  universe_domain: "googleapis.com"
};

// Write the credentials to firebase-key.json
fs.writeFileSync('./firebase-key.json', JSON.stringify(credentials, null, 2));
console.log('✅ firebase-key.json generated successfully');
