# LEADEN1 Button Diagnostic V2

V2 moves the MediaSessionCompat from the Activity into a dedicated MediaBrowserServiceCompat and keeps
a foreground media-playback service active. This follows Android's documented legacy media-session pattern.

It logs media-button callbacks and KeyEvent details:
PLAY_PAUSE, PLAY, PAUSE, NEXT, PREVIOUS, STOP, REWIND, FAST_FORWARD, RECORD, CLOSE, EJECT,
ASSIST, VOLUME_UP, VOLUME_DOWN and unknown key codes, plus the MediaSession callbacks.

Important: Android may consume volume controls at system level, so their absence from the app does not
prove the glasses did not send them.

Build with GitHub Actions: upload this folder's CONTENTS to a GitHub repository, open Actions, run
"Build LEADEN1 Button Diagnostic V2", then download the APK artifact.

Test first with no Spotify/YouTube open:
1. Open V2.
2. Confirm MEDIA SESSION ACTIVA.
3. Connect glases.
4. Press central once and again.
5. Press each side button once.
6. Hold central about 2 seconds.
7. Send the resulting HISTORIAL back for analysis.

Android documentation:
https://developer.android.com/media/legacy/audio/mediabrowserservice
https://developer.android.com/media/implement/surfaces/mobile
