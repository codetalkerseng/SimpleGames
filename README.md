# SMS/MMS Network Monitor

A comprehensive Android utility to monitor and troubleshoot SMS/MMS messaging and network connectivity issues. This app provides detailed diagnostics for messaging failures, network state changes, and includes a VPN-based traffic monitor for detecting bandwidth hogs and potential security threats.

## Features

### 📱 SMS/MMS Monitoring
- **Real-time SMS/MMS tracking** - Monitor all incoming and outgoing messages
- **Failure diagnostics** - Detailed logging of message send/receive failures
- **Network correlation** - Track which network conditions cause message failures
- **WiFi Calling detection** - Identify when messages fail on WiFi calling vs cellular

### 🌐 Network Diagnostics
- **Network state monitoring** - Real-time tracking of WiFi/cellular transitions
- **Signal strength tracking** - Monitor signal strength and quality
- **WiFi Calling status** - Detect when WiFi calling is active
- **Cell tower information** - Track Cell ID, LAC, and network type (LTE/5G/etc.)
- **Roaming detection** - Monitor roaming status

### 🔒 Traffic Monitoring & Security
- **VPN-based packet capture** - Intercept and analyze all network traffic
- **Bandwidth monitoring** - Track data usage by app
- **Security threat detection** - Heuristic analysis for suspicious connections
- **Malicious IP detection** - Check against known threat patterns
- **Protocol analysis** - Deep inspection of TCP/UDP/ICMP packets
- **DNS query logging** - Monitor DNS requests

### 📊 Dashboard & Analytics
- **Real-time monitoring** - Live dashboard with current network status
- **Historical data** - 7-day retention of all events (configurable)
- **Top bandwidth consumers** - Identify apps using the most data
- **Security alerts** - Critical alerts for suspicious network activity
- **Failure patterns** - Analyze SMS/MMS failure trends

## Use Cases

### Primary Use Case: SMS/MMS Troubleshooting
This app is specifically designed to diagnose:
- **Poor signal issues** - Identify when messages fail due to weak signal
- **WiFi Calling problems** - Debug why messages fail on WiFi calling but work on cellular
- **Network transition failures** - Detect issues during WiFi ↔ cellular handoffs
- **MMS delivery problems** - Track MMS-specific failures

### Secondary Use Cases
- Monitor data usage to identify bandwidth-hogging apps
- Detect potential malware or suspicious network activity
- Analyze network connection patterns
- Track signal strength in different locations

## Installation

### Requirements
- Android 8.0 (API 26) or higher
- Permissions required:
  - SMS: `RECEIVE_SMS`, `READ_SMS`, `SEND_SMS`
  - Phone: `READ_PHONE_STATE`
  - Location: `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` (for cell tower info)
  - Network: `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE`
  - VPN: `BIND_VPN_SERVICE`
  - Notifications: `POST_NOTIFICATIONS` (Android 13+)

### Build Instructions

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd SimpleGames
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory
   - Wait for Gradle sync to complete

3. **Build the APK**
   ```bash
   ./gradlew assembleDebug
   ```
   The APK will be located at: `app/build/outputs/apk/debug/app-debug.apk`

4. **Install on device**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

## Usage

### First Launch

1. **Grant Permissions**
   - The app will request all necessary permissions on first launch
   - Grant all permissions for full functionality

2. **Start Monitoring Services**
   - Tap "Start Monitoring" to begin SMS/MMS and network monitoring
   - This starts foreground services that run in the background

3. **Enable VPN Monitoring (Optional)**
   - Tap "Start VPN" to enable traffic monitoring
   - Accept the VPN permission dialog
   - Note: This will route all traffic through the app for analysis

### Dashboard Tabs

#### Overview Tab
- Current network status summary
- Recent critical alerts
- Quick glance at system health

#### SMS/MMS Tab
- List of all SMS/MMS events (last 24 hours)
- Color-coded by status:
  - **Green**: Successfully sent/received
  - **Red**: Failed
- Shows:
  - Direction (incoming/outgoing)
  - Phone number
  - Status
  - Network type
  - WiFi calling status
  - Signal strength
  - Failure reason (if applicable)

#### Network Tab
- Real-time network status
- Signal strength and quality
- WiFi/cellular information
- Cell tower details

#### Traffic Tab
- Top apps by bandwidth usage
- Total upload/download per app
- Suspicious activity indicators
- Connection counts

#### Alerts Tab
- All diagnostic events and alerts
- Categorized by:
  - SMS failures
  - Network issues
  - Security alerts
  - Bandwidth warnings
- Severity levels: Info, Warning, Error, Critical

### Troubleshooting SMS/MMS Issues

#### WiFi Calling Issues
1. Navigate to SMS/MMS tab
2. Look for failed messages with "WiFi Calling: Yes"
3. Compare with failures when WiFi calling is off
4. Check signal strength at time of failure

#### Poor Signal Diagnosis
1. Check SMS/MMS tab for signal strength values
2. Failures with signal < -100 dBm indicate poor coverage
3. Check Network tab for signal level (0-4 scale)

#### Network Transition Problems
1. Go to Alerts tab
2. Look for SMS failures correlated with network state changes
3. Check Network tab history for frequent network transitions

### Monitoring Network Traffic

1. **Enable VPN**: Tap "Start VPN" and accept permission
2. **View Traffic**: Go to Traffic tab to see bandwidth usage
3. **Check Security**: Go to Alerts tab for suspicious activity
4. **Analyze Apps**: See which apps consume most bandwidth

**Security Features:**
- Suspicious score (0-100) for each connection
- Alerts for connections to known malicious IPs
- Detection of unusual ports (backdoors, botnets)
- Flagging of unencrypted connections
- DNS query monitoring

### Data Management

**Automatic Cleanup:**
- By default, data older than 7 days is retained
- Call `viewModel.cleanOldData(daysToKeep)` to customize

**Manual Management:**
- Database is stored in app private storage
- Uninstalling the app removes all data
- No cloud sync or external storage

## Architecture

### Components

#### Services
- **SmsMonitorService**: Foreground service monitoring SMS/MMS events
- **NetworkStateService**: Tracks network state changes and signal strength
- **NetworkMonitorVpnService**: VPN service for packet capture and analysis

#### Broadcast Receivers
- **SmsReceiver**: Intercepts incoming SMS messages
- **SmsSentReceiver**: Tracks SMS send status
- **SmsDeliveredReceiver**: Tracks SMS delivery confirmation
- **MmsReceiver**: Intercepts incoming MMS messages
- **NetworkStateReceiver**: Monitors network connectivity changes

#### Database (Room)
- **SmsEvent**: SMS/MMS message events
- **NetworkEvent**: Network state changes
- **TrafficEvent**: Individual packet/connection events
- **DiagnosticEvent**: Alerts and diagnostic messages
- **AppTrafficStats**: Aggregated app bandwidth statistics

#### Utilities
- **NetworkStateHelper**: Queries network and telephony state
- **PacketAnalyzer**: Analyzes network packets for security threats

### Data Flow

```
SMS/MMS → BroadcastReceiver → SmsEvent (Database) → UI
Network Change → PhoneStateListener → NetworkEvent (Database) → UI
Network Packet → VPN Interface → PacketAnalyzer → TrafficEvent (Database) → UI
```

## Privacy & Security

### Data Privacy
- **No network transmission**: All data stays on device
- **No cloud sync**: Data is not uploaded anywhere
- **Local storage only**: SQLite database in app private directory
- **No third-party SDKs**: No analytics or tracking libraries

### Message Content
- SMS/MMS body is stored in local database
- Used only for diagnostic purposes
- Can be excluded by modifying `SmsReceiver.kt` to set `messageBody = null`

### Permissions Justification
- **SMS permissions**: Required to monitor message send/receive events
- **Location**: Required for cell tower information (Cell ID, LAC)
- **Phone state**: Required for signal strength and network type
- **VPN**: Required for traffic monitoring (optional feature)

## Limitations

### VPN Traffic Monitoring
- The VPN implementation provided is **simplified** and **educational**
- **Not production-ready** for actual traffic forwarding
- Packet forwarding is incomplete (TCP connection state tracking needed)
- For production use, implement proper:
  - TCP connection state machine
  - Connection pool management
  - Proper routing and NAT
  - Consider using a library like `tun2socks`

### SMS/MMS Detection
- MMS message body extraction requires additional WAP parsing
- WiFi Calling detection is best-effort and may not be 100% accurate
- Requires being active in background (battery consideration)

### Security Detection
- Malicious IP/port detection is heuristic-based
- Not a replacement for proper antivirus software
- Requires manual update of threat intelligence lists

## Known Issues

1. **VPN packet forwarding**: Simplified implementation may not forward all packets correctly
2. **WiFi Calling detection**: May not detect all WiFi calling scenarios
3. **MMS parsing**: MMS body extraction not fully implemented
4. **Battery usage**: Continuous monitoring may impact battery life
5. **Android 14+**: May require additional permissions or configurations

## Future Enhancements

- [ ] Export diagnostic data to CSV/JSON
- [ ] Charts and graphs for historical data
- [ ] Geolocation mapping of signal strength
- [ ] Automatic SMS retry on failure
- [ ] Integration with threat intelligence feeds
- [ ] ML-based anomaly detection
- [ ] Network speed testing
- [ ] Complete VPN implementation with proper TCP/UDP forwarding

## Development

### Project Structure
```
app/src/main/java/com/monitor/smsnetwork/
├── data/
│   ├── entity/          # Room database entities
│   ├── dao/             # Data access objects
│   └── MonitorDatabase.kt
├── receiver/            # Broadcast receivers
├── service/             # Background services
├── ui/                  # Compose UI
│   ├── theme/
│   ├── viewmodel/
│   └── MainActivity.kt
└── util/                # Helper utilities
```

### Building from Source

**Prerequisites:**
- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 34
- Gradle 8.2+

**Build Commands:**
```bash
# Debug build
./gradlew assembleDebug

# Release build (requires signing configuration)
./gradlew assembleRelease

# Run tests
./gradlew test

# Install on connected device
./gradlew installDebug
```

### Contributing

This is an educational/diagnostic tool. Contributions are welcome:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

**Areas for contribution:**
- Complete VPN packet forwarding implementation
- Improve WiFi calling detection
- Add MMS body parsing
- Enhance security threat detection
- Add data visualization (charts/graphs)
- Improve battery efficiency

## License

This project is provided as-is for educational and diagnostic purposes.

## Disclaimer

This app is designed for **legitimate troubleshooting and diagnostic purposes**.

**Important:**
- Do not use this app to intercept or monitor communications without proper authorization
- Respect privacy laws and regulations in your jurisdiction
- This app is not intended for malicious use
- The developers are not responsible for misuse of this software

**Not a replacement for:**
- Professional network diagnostic tools
- Enterprise-grade security solutions
- Antivirus software
- Official carrier troubleshooting tools

## Support

For issues, questions, or feature requests:
1. Check existing issues in the repository
2. Create a new issue with detailed information
3. Include device model, Android version, and logs if applicable

## Acknowledgments

- Android Open Source Project (AOSP)
- Jetpack Compose team
- Room Persistence Library
- Kotlin Coroutines

---

**Version**: 1.0
**Last Updated**: 2025
**Minimum Android Version**: 8.0 (Oreo)
**Target Android Version**: 14