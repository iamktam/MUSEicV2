/**
 * Example of using libmuse library on android.
 * Interaxon, Inc. 2015
 */

package com.interaxon.test.libmuse;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.widget.Toast;

import com.interaxon.libmuse.AnnotationData;
import com.interaxon.libmuse.ConnectionState;
import com.interaxon.libmuse.Eeg;
import com.interaxon.libmuse.LibMuseVersion;
import com.interaxon.libmuse.MessageType;
import com.interaxon.libmuse.Muse;
import com.interaxon.libmuse.MuseArtifactPacket;
import com.interaxon.libmuse.MuseConfiguration;
import com.interaxon.libmuse.MuseConnectionListener;
import com.interaxon.libmuse.MuseConnectionPacket;
import com.interaxon.libmuse.MuseDataListener;
import com.interaxon.libmuse.MuseDataPacket;
import com.interaxon.libmuse.MuseDataPacketType;
import com.interaxon.libmuse.MuseFileFactory;
import com.interaxon.libmuse.MuseFileReader;
import com.interaxon.libmuse.MuseFileWriter;
import com.interaxon.libmuse.MuseManager;
import com.interaxon.libmuse.MusePreset;
import com.interaxon.libmuse.MuseVersion;

import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;

import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Arm;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.XDirection;
import com.thalmic.myo.scanner.ScanActivity;

import org.w3c.dom.Text;

/**
 * In this simple example MainActivity implements 2 MuseHeadband listeners
 * and updates UI when data from Muse is received. Similarly you can implement
 * listers for other data or register same listener to listen for different type
 * of data.
 * For simplicity we create Listeners as inner classes of MainActivity. We pass
 * reference to MainActivity as we want listeners to update UI thread in this
 * example app.
 * You can also connect multiple muses to the same phone and register same
 * listener to listen for data from different muses. In this case you will
 * have to provide synchronization for data members you are using inside
 * your listener.
 *
 * Usage instructions:
 * 1. Enable bluetooth on your device
 * 2. Pair your device with muse
 * 3. Run this project
 * 4. Press Refresh. It should display all paired Muses in Spinner
 * 5. Make sure Muse headband is waiting for connection and press connect.
 * It may take up to 10 sec in some cases.
 * 6. You should see EEG and accelerometer data as well as connection status,
 * Version information and MuseElements (alpha, beta, theta, delta, gamma waves)
 * on the screen.
 */



public class MainActivity extends Activity implements
        OnClickListener, PlayerNotificationCallback, ConnectionStateCallback {

    //MYO CODE

    TextView myoDebug;

    // MYO Device Listener
    private DeviceListener mListener = new AbstractDeviceListener() {

        // onConnect() is called whenever a Myo has been connected.
        @Override
        public void onConnect(Myo myo, long timestamp) {
            // Set the text color of the text view to cyan when a Myo connects.
            myoDebug.setText("Myo Connected.");
        }

        // onDisconnect() is called whenever a Myo has been disconnected.
        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            // Set the text color of the text view to red when a Myo disconnects.
            myoDebug.setText("Myo Disconnected.");
        }

        // onArmSync() is called whenever Myo has recognized a Sync Gesture after someone has put it on their
        // arm. This lets Myo know which arm it's on and which way it's facing.
        @Override
        public void onArmSync(Myo myo, long timestamp, Arm arm, XDirection xDirection) {
            myoDebug.setText("Myo synced");
        }

        // onArmUnsync() is called whenever Myo has detected that it was moved from a stable position on a person's arm after
        // it recognized the arm. Typically this happens when someone takes Myo off of their arm, but it can also happen
        // when Myo is moved around on the arm.
        @Override
        public void onArmUnsync(Myo myo, long timestamp) {
            myoDebug.setText("Myo unsynced");
        }

        // onUnlock() is called whenever a synced Myo has been unlocked. Under the standard locking
        // policy, that means poses will now be delivered to the listener.
        @Override
        public void onUnlock(Myo myo, long timestamp) {
            myoDebug.setText("Myo unlocked");
        }

        // onLock() is called whenever a synced Myo has been locked. Under the standard locking
        // policy, that means poses will no longer be delivered to the listener.
        @Override
        public void onLock(Myo myo, long timestamp) {
            myoDebug.setText("Myo Locked.");
        }

        // onOrientationData() is called whenever a Myo provides its current orientation,
        // represented as a quaternion.
        @Override
        public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
            // Calculate Euler angles (roll, pitch, and yaw) from the quaternion.
        }

        // onPose() is called whenever a Myo provides a new pose.
        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            // Handle the cases of the Pose enumeration, and change the text of the text view
            // based on the pose we receive.
            switch (pose) {
                case UNKNOWN:
                    break;
                case REST:
                case DOUBLE_TAP:
                    //int restTextId = R.string.hello_world;
                    break;
                case FIST:

                    break;
                case WAVE_IN:
                    playPrevSong();
                    break;
                case WAVE_OUT:
                    playNextSong();
                    break;
                case FINGERS_SPREAD:
                    pausePlay();
                    break;
            }

            if (pose != Pose.UNKNOWN && pose != Pose.REST) {
                // Tell the Myo to stay unlocked until told otherwise. We do that here so you can
                // hold the poses without the Myo becoming locked.
                myo.unlock(Myo.UnlockType.HOLD);

                // Notify the Myo that the pose has resulted in an action, in this case changing
                // the text on the screen. The Myo will vibrate.
                myo.notifyUserAction();
            } else {
                // Tell the Myo to stay unlocked only for a short period. This allows the Myo to
                // stay unlocked while poses are being performed, but lock after inactivity.
                myo.unlock(Myo.UnlockType.TIMED);
            }
        }
    };

    // TODO: Replace with your client ID
    private static final String CLIENT_ID = "502e0cebcbbe4cd89668a1086a118533";
    // TODO: Replace with your redirect URI
    private static final String REDIRECT_URI = "museic://callback";

    private Player mPlayer;

    private boolean playing = false;

    private final String HAPPYMEL = "Happy Chilling";
    private final String HAPPYCON = "Excited";
    private final String SADMEL = "The Feels";
    private final String SADCON = "Studying";

    private String track = "";
    private String artist = "";

    private int current_mood = 0;  //Default is happy & mellow
    private int prev_mood = -1;
    private String prev_type = HAPPYMEL;

    public void pausePlay() {
        if (playing == false) {
            myoDebug.setText("Song played.");
            playing = true;
            if (prev_mood == -1) {
                mPlayer.play("spotify:user:1215411810:playlist:7EvcJnRVzgbbznThpJ7Rum");
                prev_mood = current_mood;
                TextView curr_song_type = (TextView) findViewById(R.id.curr_song_type);
                curr_song_type.setText(HAPPYMEL);
            } else {
                mPlayer.resume();
            }
        } else {
            myoDebug.setText("Song paused.");
            playing = false;
            mPlayer.pause();
        }
    }

    public void playPrevSong() {
        myoDebug.setText("Prev Song.");
        mPlayer.skipToPrevious();
        TextView curr_song_type = (TextView) findViewById(R.id.curr_song_type);
        curr_song_type.setText(prev_type);
    }

    public void playNextSong() {
        myoDebug.setText("Next Song.");
        if (prev_mood != current_mood) {
            switch (current_mood) {
                case 0:
                    mPlayer.play("spotify:user:1215411810:playlist:7EvcJnRVzgbbznThpJ7Rum");
                    prev_type = HAPPYMEL;
                    break;
                case 1:
                    mPlayer.play("spotify:user:1215411810:playlist:472PPXuMZC2xNqJThoMwlD");
                    prev_type = HAPPYCON;
                    break;
                case 2:
                    mPlayer.play("spotify:user:1215411810:playlist:4sWnTsWWXrGtTRdPgjTpFu");
                    prev_type = SADMEL;
                    break;
                case 3:
                    mPlayer.play("spotify:user:1215411810:playlist:7tDZt6ANIqgBeDaR6lHqqt");
                    prev_type = SADCON;
                    break;
                default:
                    mPlayer.play("spotify:user:1215411810:playlist:7EvcJnRVzgbbznThpJ7Rum");
                    prev_type = HAPPYMEL;
                    break;
            }
            prev_mood = current_mood;
            TextView curr_song_type = (TextView) findViewById(R.id.curr_song_type);
            curr_song_type.setText(prev_type);
        } else {
            mPlayer.skipToNext();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
                    @Override
                    public void onInitialized(Player player) {
                        mPlayer = player;
                        mPlayer.addConnectionStateCallback(MainActivity.this);
                        mPlayer.addPlayerNotificationCallback(MainActivity.this);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Throwable error) {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.d("MainActivity", "Playback event received: " + eventType.name());
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String errorDetails) {
        Log.d("MainActivity", "Playback error received: " + errorType.name());
    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
        Hub.getInstance().removeListener(mListener);
        if (isFinishing()) {
            Hub.getInstance().shutdown();
        }
    }

    /**
     * Connection listener updates UI with new connection status and logs it.
     */
    class ConnectionListener extends MuseConnectionListener {

        final WeakReference<Activity> activityRef;

        ConnectionListener(final WeakReference<Activity> activityRef) {
            this.activityRef = activityRef;
        }

        @Override
        public void receiveMuseConnectionPacket(MuseConnectionPacket p) {
            final ConnectionState current = p.getCurrentConnectionState();
            final String status = p.getPreviousConnectionState().toString() +
                    " -> " + current;
            final String full = "Muse " + p.getSource().getMacAddress() +
                    " " + status;
            Log.i("Muse Headband", full);
            Activity activity = activityRef.get();
            // UI thread is used here only because we need to update
            // TextView values. You don't have to use another thread, unless
            // you want to run disconnect() or connect() from connection packet
            // handler. In this case creating another thread is required.
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView statusText =
                                (TextView) findViewById(R.id.con_status);
                        statusText.setText(status);
                        if (current == ConnectionState.CONNECTED) {
                            MuseVersion museVersion = muse.getMuseVersion();
                            String version = museVersion.getFirmwareType() +
                                    " - " + museVersion.getFirmwareVersion() +
                                    " - " + Integer.toString(
                                    museVersion.getProtocolVersion());
                        }
                    }
                });
            }
        }
    }

    /**
     * Data listener will be registered to listen for: Accelerometer,
     * Eeg and Relative Alpha bandpower packets. In all cases we will
     * update UI with new values.
     * We also will log message if Artifact packets contains "blink" flag.
     * DataListener methods will be called from execution thread. If you are
     * implementing "serious" processing algorithms inside those listeners,
     * consider to create another thread.
     */
    class DataListener extends MuseDataListener {

        final WeakReference<Activity> activityRef;
        private MuseFileWriter fileWriter;

        private ArrayList<Double> gammas = new ArrayList<>(4);
        private ArrayList<Double> connections = new ArrayList<>(4);

        private ArrayList<Double> samples = new ArrayList<>();
        private long last_time;
        private int sample_num = 0;

        private boolean concentration = false;
        private boolean mellow = true;

        private final String GOOD = "Good";
        private final String BAD = "Bad";

        private final String YES = "Yes";
        private final String NO = "No";

        DataListener(final WeakReference<Activity> activityRef) {
            this.activityRef = activityRef;
            for (int i = 0; i < 4; ++i) {
                gammas.add(0.0);
                connections.add(0.0);
            }
            last_time = Calendar.getInstance().getTimeInMillis();
        }

        @Override
        public void receiveMuseDataPacket(MuseDataPacket p) {
            switch (p.getPacketType()) {
                case GAMMA_RELATIVE:
                    updateGammaRelative(p.getValues());
                    break;
                case HORSESHOE:
                    updateHorseshoe(p.getValues());
                    break;
                case CONCENTRATION:
                    updateConcentration(p.getValues());
                    break;
                case MELLOW:
                    updateMellow(p.getValues());
                    break;
                case BATTERY:
                    updateBattery(p.getValues());
                    break;
                default:
                    break;
            }
        }

        @Override
        public void receiveMuseArtifactPacket(MuseArtifactPacket p) {
            if (p.getHeadbandOn() && p.getBlink()) {
                Log.i("Artifacts", "blink");
            }
        }

        private void updateGammaRelative(final ArrayList<Double> data) {
            Activity activity = activityRef.get();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView gamma1 = (TextView) findViewById(R.id.gamma1);
                        TextView gamma2 = (TextView) findViewById(R.id.gamma2);
                        TextView gamma3 = (TextView) findViewById(R.id.gamma3);
                        TextView gamma4 = (TextView) findViewById(R.id.gamma4);
                        TextView avg_gamma = (TextView) findViewById(R.id.avg_gamma);
                        TextView happysad = (TextView) findViewById(R.id.happysad);
                        TextView songtype = (TextView) findViewById(R.id.songtype);

                        double g1 = data.get(Eeg.TP9.ordinal());
                        double g2 = data.get(Eeg.FP1.ordinal());
                        double g3 = data.get(Eeg.FP2.ordinal());
                        double g4 = data.get(Eeg.TP10.ordinal());
                        gammas.set(0, g1);
                        gammas.set(1, g2);
                        gammas.set(2, g3);
                        gammas.set(3, g4);

                        //Calculate avg gamma with only numeric gamma values and good connections
                        double avg = 0;
                        double size = 0;
                        for (int i = 0; i < 4; ++i) {
                            Double gamma = gammas.get(i);
                            if (!Double.isNaN(gamma) && (connections.get(i) <= 2)) {
                                avg += gamma;
                                ++size;
                            }
                        }
                        avg = avg / size;

                        gamma1.setText(String.format("%6.2f", g1));
                        gamma2.setText(String.format("%6.2f", g2));
                        gamma3.setText(String.format("%6.2f", g3));
                        gamma4.setText(String.format("%6.2f", g4));
                        avg_gamma.setText(String.format("%6.2f", avg));

                        long current_time = Calendar.getInstance().getTimeInMillis();
                        //Sample twice a second!
                        if ((current_time - last_time) >= (500 * sample_num)) {
                            ++sample_num;
                            samples.add(avg);
                        }
                        Double avg_5secs = Double.NaN;
                        //Average of 5 second samples
                        if ((current_time - last_time) >= 5000) {
                            avg_5secs = 0.0;
                            //Check every 5 seconds
                            last_time = current_time;
                            sample_num = 0;

                            for (Double sample : samples) {
                                avg_5secs += sample;
                            }
                            avg_5secs /= (double) samples.size();
                            samples.clear();
                        }

                        if (!Double.isNaN(avg_5secs)) {
                            if (avg > .175) {
                                happysad.setText("Happy! :)");
                                if (concentration) {
                                    songtype.setText(HAPPYCON);
                                    current_mood = 1;
                                } else {
                                    songtype.setText(HAPPYMEL);
                                    current_mood = 0;
                                }
                            } else if (avg < 0.125) {
                                happysad.setText("Sad. :(");
                                if (concentration) {
                                    songtype.setText(SADCON);
                                    current_mood = 3;
                                } else {
                                    songtype.setText(SADMEL);
                                    current_mood = 2;
                                }
                            } else {
                                happysad.setText("Neutral");
                                if (concentration) {
                                    songtype.setText(HAPPYCON);
                                } else {
                                    songtype.setText(HAPPYMEL);
                                }
                            }
                        }
                    }
                });
            }
        }

        private void updateHorseshoe(final ArrayList<Double> data) {
            Activity activity = activityRef.get();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView horse1 = (TextView) findViewById(R.id.horse1);
                        TextView horse2 = (TextView) findViewById(R.id.horse2);
                        TextView horse3 = (TextView) findViewById(R.id.horse3);
                        TextView horse4 = (TextView) findViewById(R.id.horse4);

                        double h1 = data.get(Eeg.TP9.ordinal());
                        double h2 = data.get(Eeg.FP1.ordinal());
                        double h3 = data.get(Eeg.FP2.ordinal());
                        double h4 = data.get(Eeg.TP10.ordinal());
                        connections.set(0, h1);
                        connections.set(1, h2);
                        connections.set(2, h3);
                        connections.set(3, h4);

                        if (h1 <= 2) {
                            horse1.setText(GOOD);
                        } else {
                            horse1.setText(BAD);
                        }
                        if (h2 <= 2) {
                            horse2.setText(GOOD);
                        } else {
                            horse2.setText(BAD);
                        }
                        if (h3 <= 2) {
                            horse3.setText(GOOD);
                        } else {
                            horse3.setText(BAD);
                        }
                        if (h4 <= 2) {
                            horse4.setText(GOOD);
                        } else {
                            horse4.setText(BAD);
                        }
                    }
                });
            }
        }

        private void updateConcentration(final ArrayList<Double> data) {
            Activity activity = activityRef.get();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView con = (TextView) findViewById(R.id.con);
                        if (data.get(0) >= .5) {
                            con.setText(YES);
                            concentration = true;
                        } else {
                            con.setText(NO);
                            concentration = false;
                        }
                    }
                });
            }
        }

        private void updateMellow(final ArrayList<Double> data) {
            Activity activity = activityRef.get();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView mel = (TextView) findViewById(R.id.mel);
                        if (data.get(0) >= .5) {
                            mel.setText(YES);
                            mellow = true;
                        } else {
                            mel.setText(NO);
                            mellow = false;
                        }
                    }
                });
            }
        }

        private void updateBattery(final ArrayList<Double> data) {
            Activity activity = activityRef.get();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView battery = (TextView) findViewById(R.id.battery);
                        battery.setText(String.format("Battery: %6.2f%%", data.get(0)));
                    }
                });
            }
        }

        public void setFileWriter(MuseFileWriter fileWriter) {
            this.fileWriter  = fileWriter;
        }
    }

    private Muse muse = null;
    private ConnectionListener connectionListener = null;
    private DataListener dataListener = null;
    private boolean dataTransmission = true;
    private MuseFileWriter fileWriter = null;
    private static final int REQUEST_CODE = 1337;

    public MainActivity() {
        // Create listeners and pass reference to activity to them
        WeakReference<Activity> weakActivity =
                new WeakReference<Activity>(this);

        connectionListener = new ConnectionListener(weakActivity);
        dataListener = new DataListener(weakActivity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();
        myoDebug = (TextView) findViewById(R.id.debug_label);
        Hub hub = Hub.getInstance();
        if (!hub.init(this, "com.example.kelvin.muse_ic")) {
            // We can't do anything with the Myo device if the Hub can't be initialized, so exit.
            Toast.makeText(this, "Couldn't initialize Hub", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Next, register for DeviceListener callbacks.
        hub.addListener(mListener);

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
        createButtonListeners();
        Button refreshButton = (Button) findViewById(R.id.refresh);
        refreshButton.setOnClickListener(this);
        Button connectButton = (Button) findViewById(R.id.connect);
        connectButton.setOnClickListener(this);

        Button disconnectButton = (Button) findViewById(R.id.disconnect);
        disconnectButton.setOnClickListener(this);
        // // Uncommet to test Muse File Reader
        //
        // // file can be big, read it in a separate thread
        // Thread thread = new Thread(new Runnable() {
        //     public void run() {
        //         playMuseFile("testfile.muse");
        //     }
        // });
        // thread.start();

        File dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        fileWriter = MuseFileFactory.getMuseFileWriter(
                new File(dir, "new_muse_file.muse"));
        Log.i("Muse Headband", "libmuse version=" + LibMuseVersion.SDK_VERSION);
        fileWriter.addAnnotationString(1, "MainActivity onCreate");
        dataListener.setFileWriter(fileWriter);
    }

    private void createButtonListeners() {
        Button prevButton = (Button) findViewById(R.id.prevButton);
        Button nextButton = (Button) findViewById(R.id.nextButton);
        Button playButton = (Button) findViewById(R.id.playButton);

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(0);
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(1);
            }

        });
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(2);
            }
        });
    }

    private void buttonClicked(int i) {
        if (prev_mood == -1) {
            mPlayer.setShuffle(true);
        }
        if (i == 0) {
            //prev
            mPlayer.skipToPrevious();
            TextView curr_song_type = (TextView) findViewById(R.id.curr_song_type);
            curr_song_type.setText(prev_type);
        } else if (i == 1) {
            //next
            if (prev_mood != current_mood) {
                switch (current_mood) {
                    case 0:
                        mPlayer.play("spotify:user:1215411810:playlist:7EvcJnRVzgbbznThpJ7Rum");
                        prev_type = HAPPYMEL;
                        break;
                    case 1:
                        mPlayer.play("spotify:user:1215411810:playlist:472PPXuMZC2xNqJThoMwlD");
                        prev_type = HAPPYCON;
                        break;
                    case 2:
                        mPlayer.play("spotify:user:1215411810:playlist:4sWnTsWWXrGtTRdPgjTpFu");
                        prev_type = SADMEL;
                        break;
                    case 3:
                        mPlayer.play("spotify:user:1215411810:playlist:7tDZt6ANIqgBeDaR6lHqqt");
                        prev_type = SADCON;
                        break;
                    default:
                        mPlayer.play("spotify:user:1215411810:playlist:7EvcJnRVzgbbznThpJ7Rum");
                        prev_type = HAPPYMEL;
                        break;
                }
                prev_mood = current_mood;
                TextView curr_song_type = (TextView) findViewById(R.id.curr_song_type);
                curr_song_type.setText(prev_type);
            } else {
                mPlayer.skipToNext();
            }
        } else if (i == 2) {
            //play
            if (playing == false) {
                playing = true;
                if (prev_mood == -1) {
                    mPlayer.play("spotify:user:1215411810:playlist:7EvcJnRVzgbbznThpJ7Rum");
                    prev_mood = current_mood;
                    TextView curr_song_type = (TextView) findViewById(R.id.curr_song_type);
                    curr_song_type.setText(HAPPYMEL);
                } else {
                    mPlayer.resume();
                }
            } else {
                playing = false;
                mPlayer.pause();
            }
        }
    }

    static final class BroadcastTypes {
        static final String SPOTIFY_PACKAGE = "com.spotify.music";
        static final String PLAYBACK_STATE_CHANGED = SPOTIFY_PACKAGE + ".playbackstatechanged";
        static final String QUEUE_CHANGED = SPOTIFY_PACKAGE + ".queuechanged";
        static final String METADATA_CHANGED = SPOTIFY_PACKAGE + ".metadatachanged";
    }
    public class SpotifyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // This is sent with all broadcasts, regardless of type. The value is taken from
            // System.currentTimeMillis(), which you can compare to in order to determine how
            // old the event is.
            long timeSentInMs = intent.getLongExtra("timeSent", 0L);

            String action = intent.getAction();

            if (action.equals(BroadcastTypes.METADATA_CHANGED)) {
                String trackId = intent.getStringExtra("id");
                String artistName = intent.getStringExtra("artist");
                String albumName = intent.getStringExtra("album");
                String trackName = intent.getStringExtra("track");
                int trackLengthInSec = intent.getIntExtra("length", 0);
                // Do something with extracted information...
                boolean playing = intent.getBooleanExtra("playing", false);
                int positionInMs = intent.getIntExtra("playbackPosition", 0);
                // Do something with extracted information
            } else if (action.equals(BroadcastTypes.QUEUE_CHANGED)) {
                // Sent only as a notification, your app may want to respond accordingly.
            }
        }
    }

    @Override
    public void onClick(View v) {

        Spinner musesSpinner = (Spinner) findViewById(R.id.muses_spinner);
        if (v.getId() == R.id.refresh) {
            MuseManager.refreshPairedMuses();
            List<Muse> pairedMuses = MuseManager.getPairedMuses();
            List<String> spinnerItems = new ArrayList<String>();
            for (Muse m: pairedMuses) {
                String dev_id = m.getName() + "-" + m.getMacAddress();
                Log.i("Muse Headband", dev_id);
                spinnerItems.add(dev_id);
            }
            ArrayAdapter<String> adapterArray = new ArrayAdapter<String> (
                    this, android.R.layout.simple_spinner_item, spinnerItems);
            musesSpinner.setAdapter(adapterArray);


        }
        else if (v.getId() == R.id.connect) {
            List<Muse> pairedMuses = MuseManager.getPairedMuses();
            if (pairedMuses.size() < 1 ||
                    musesSpinner.getAdapter().getCount() < 1) {
                Log.w("Muse Headband", "There is nothing to connect to");
            }
            else {
                muse = pairedMuses.get(musesSpinner.getSelectedItemPosition());
                ConnectionState state = muse.getConnectionState();
                if (state == ConnectionState.CONNECTED ||
                        state == ConnectionState.CONNECTING) {
                    Log.w("Muse Headband",
                            "doesn't make sense to connect second time to the same muse");
                    return;
                }
                configureLibrary();
                fileWriter.open();
                fileWriter.addAnnotationString(1, "Connect clicked");
                /**
                 * In most cases libmuse native library takes care about
                 * exceptions and recovery mechanism, but native code still
                 * may throw in some unexpected situations (like bad bluetooth
                 * connection). Print all exceptions here.
                 */
                try {
                    muse.runAsynchronously();
                } catch (Exception e) {
                    Log.e("Muse Headband", e.toString());
                }
            }
        }


        else if (v.getId() == R.id.disconnect) {
            if (muse != null) {
                /**
                 * true flag will force libmuse to unregister all listeners,
                 * BUT AFTER disconnecting and sending disconnection event.
                 * If you don't want to receive disconnection event (for ex.
                 * you call disconnect when application is closed), then
                 * unregister listeners first and then call disconnect:
                 * muse.unregisterAllListeners();
                 * muse.disconnect(false);
                 */
                muse.disconnect(true);
                fileWriter.addAnnotationString(1, "Disconnect clicked");
                fileWriter.flush();
                fileWriter.close();
            }
        }
    }

    /*
     * Simple example of getting data from the "*.muse" file
     */
    private void playMuseFile(String name) {
        File dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File file = new File(dir, name);
        final String tag = "Muse File Reader";
        if (!file.exists()) {
            Log.w(tag, "file doesn't exist");
            return;
        }
        MuseFileReader fileReader = MuseFileFactory.getMuseFileReader(file);
        while (fileReader.gotoNextMessage()) {
            MessageType type = fileReader.getMessageType();
            int id = fileReader.getMessageId();
            long timestamp = fileReader.getMessageTimestamp();
            Log.i(tag, "type: " + type.toString() +
                    " id: " + Integer.toString(id) +
                    " timestamp: " + String.valueOf(timestamp));
            switch(type) {
                case QUANTIZATION:
                    MuseDataPacket packet = fileReader.getDataPacket();
                    Log.i(tag, "data packet: " + packet.getPacketType().toString());
                    break;
                case VERSION:
                    MuseVersion version = fileReader.getVersion();
                    Log.i(tag, "version" + version.getFirmwareType());
                    break;
                case CONFIGURATION:
                    MuseConfiguration config = fileReader.getConfiguration();
                    Log.i(tag, "config" + config.getBluetoothMac());
                    break;
                case ANNOTATION:
                    AnnotationData annotation = fileReader.getAnnotation();
                    Log.i(tag, "annotation" + annotation.getData());
                    break;
                default:
                    break;
            }
        }
    }


    private void configureLibrary() {
        //Packet types we care about
        muse.registerConnectionListener(connectionListener);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.ARTIFACTS);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.MELLOW);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.CONCENTRATION);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.GAMMA_RELATIVE);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.HORSESHOE);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.BATTERY);
        muse.setPreset(MusePreset.PRESET_14);
        muse.enableDataTransmission(dataTransmission);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (R.id.action_scan == id) {
            onScanActionSelected();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onScanActionSelected() {
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }
}
