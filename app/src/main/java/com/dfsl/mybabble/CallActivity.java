package com.dfsl.mybabble;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Space;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;
import com.twilio.audioswitch.AudioDevice;
import com.twilio.audioswitch.AudioSwitch;
import com.twilio.voice.Call;
import com.twilio.voice.CallException;
import com.twilio.voice.CallInvite;
import com.twilio.voice.ConnectOptions;
import com.twilio.voice.RegistrationException;
import com.twilio.voice.RegistrationListener;
import com.twilio.voice.Voice;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import kotlin.Unit;

public class CallActivity extends Activity implements SensorEventListener
{

    private SensorManager mSensorManager;
    private Sensor mProximity;
    private static final int SENSOR_SENSITIVITY = 4;
    private static float screenBrightnessLevel;

    private AudioSwitch audioSwitch;

    private boolean isCaller = true;//false;
//    private boolean inPool = false;

    RegistrationListener registrationListener = registrationListener();
    Call.Listener callListener = callListener();
    TimerTask statusCheckTask;
    Timer statusTimer;
    Handler handler;
    private boolean inited = false;
    private boolean wasCaller = false;
    private boolean feedbackScreen = true;

    private boolean isReceiverRegistered = false;
    private CallActivity.VoiceBroadcastReceiver voiceBroadcastReceiver;

    private NotificationManager notificationManager;
    private AlertDialog alertDialog;
    private CallInvite activeCallInvite;
    private Call activeCall;
    private int activeCallNotificationId;

    private static final String TAG = "CallActivity";
    private RelativeLayout relativeLayout;
    private RelativeLayout cLayout;
    private static final int MIC_PERMISSION_REQUEST_CODE = 1;
    HashMap<String, String> params = new HashMap<>();

    TextView count;
    ImageButton callButton;
    Chronometer countdown;

    TextView tv;

    int height;
    int width;
    int width10;

    boolean isBusy = false;
    boolean isBackgrounded = false;
    static boolean proximityLock = false;

    public static Context callContext;

    public Switch mode;

    LinearLayout customSwitch;

    TextView acceptingCalls;
    TextView yesText;
    TextView noText;

    public void initaliseView()
    {
        AndroidNetworking.initialize(getApplicationContext());
        APICalls.getPoolStatus(); //check if in pool
        APICalls.getPoolCount();
        statusTimer = new Timer();
        handler = new Handler();
        initStatusChecker();
        statusTimer.schedule(statusCheckTask, 0, 5000);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        WindowManager.LayoutParams WMLP = getWindow().getAttributes();
        screenBrightnessLevel = WMLP.screenBrightness;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;

        width10 = Math.round(width * 0.10f);

        // set default state

        // add the user to the listener pool


    }

    @Override
    public void onBackPressed()
    {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!inited)
        {
            initaliseView();
            inited = true;
        }

        callContext = this;

        // Defining the RelativeLayout layout parameters.
        // In this case I want to fill its parent
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT,
                RelativeLayout.LayoutParams.FILL_PARENT);


        // Setting the RelativeLayout as our content view
        setContentView(activeView(), rlp);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        /*
         * Setup the broadcast receiver to be notified of FCM Token updates
         * or incoming call invite in this Activity.
         */
        voiceBroadcastReceiver = new CallActivity.VoiceBroadcastReceiver();
        registerReceiver();

        /*
         * Setup audio device management and set the volume control stream
         */
        audioSwitch = new AudioSwitch(getApplicationContext());
        //savedVolumeControlStream = getVolumeControlStream();
        //setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

        /*
         * Setup the UI
         */
        resetUI();

        /*
         * Displays a call dialog if the intent contains a call invite
         */
        handleIncomingCallIntent(getIntent());

        /*
         * Ensure the microphone permission is enabled
         */
        if (!checkPermissionForMicrophone()) {
            requestPermissionForMicrophone();
        } else {
            retrieveAccessToken();
        }
        //Prevent app from sleeping while on this page
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        APICalls.getUserUse(this);

        InitDefaultView();
    }

    private RelativeLayout activeView()
    {
        if(InCall())
        {
            return callLayout();
        }
        else if (AppSession.showRating)
        {
            return ratingScreen();
        }
        else
        {
            return mainLayout();
        }
    }

    private RelativeLayout callLayout()
    {
        cLayout = new RelativeLayout(this);
        LinearLayout cont = new LinearLayout(this);
        cLayout.setBackgroundColor(Color.parseColor(BabbleColors.GREEN));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0,0,0,0);

        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params2.setMargins(32,80,32,80);
//        params2.width = 200;
//        params2.height = 200;

        LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params3.setMargins(0,80,0,0);

        LinearLayout callDuration = new LinearLayout(this);
        callDuration.setOrientation(LinearLayout.HORIZONTAL);
        callDuration.setLayoutParams(params3);
        callDuration.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView durText = CustomWidgets.SimpleTextView(this, 20, "Time Remaining: ", Color.WHITE, Gravity.CENTER);


        countdown = new Chronometer(this);
        countdown.setCountDown(true);
        //countdown.setFormat("MM:SS");
        countdown.setBase(SystemClock.elapsedRealtime() + (AppSession.callDurationInSeconds * 1000) + 1000);//set countdown time
        countdown.setTextColor(Color.WHITE);
        countdown.setTextSize(20);
        countdown.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_b));
        countdown.setGravity(Gravity.CENTER_HORIZONTAL);
        countdown.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener()
        {

            Boolean played = false;
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                if(SystemClock.elapsedRealtime() >= chronometer.getBase())
                {
                    Log.d("debug", "hungup from timer");
                    disconnect();
                    chronometer.stop();
                    played = false;
                }
                else if (SystemClock.elapsedRealtime() >= chronometer.getBase() - 60000 && !played)
                {
                    SoundPoolManager.getInstance(getApplicationContext()).playWarningBeeps();
                    Log.d("debug", "calling warning sound");
                    played = true;
                }
            }
        });

        callDuration.addView(durText);
        callDuration.setLayoutParams(params);
        callDuration.addView(countdown);

        RelativeLayout.LayoutParams btnmarign = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        btnmarign.height = Math.round(height / 4);
        btnmarign.addRule(RelativeLayout.CENTER_HORIZONTAL);

        ImageView icon = new ImageButton(this);
        icon.setImageResource(R.drawable.ic_babble_circle_active);
        icon.setBackgroundColor(Color.TRANSPARENT);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icon.setLayoutParams(btnmarign);

        Button panic = new Button(this);
        panic.setText("Report as abusive");
        panic.setTextSize(16);
//        panic.setHeight(200);
        panic.setPadding(50,50,50,50);
        panic.setBackgroundResource(R.drawable.rounded_button);
        panic.setTextColor(Color.WHITE);
        GradientDrawable pandraw = (GradientDrawable) panic.getBackground();
        pandraw.setColor(Color.BLACK);
        panic.setTransformationMethod(null);
        panic.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_r));
        panic.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                //report Call
                PanicPopup();
            }
        });

        LinearLayout inCallButtons = new LinearLayout(this);
        inCallButtons.setOrientation(LinearLayout.HORIZONTAL);
        //inCallButtons.setLayoutParams(new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        inCallButtons.setGravity(Gravity.CENTER);

        panic.setLayoutParams(params);

        ImageButton mutebtn = new ImageButton(this);
        mutebtn.setImageResource(R.drawable.ic_mute);
        mutebtn.setBackgroundColor(Color.TRANSPARENT);
        //mutebtn.setAlpha(0.7f);
        mutebtn.setMaxWidth(32);//width > 1750? 48: 28);
        //mutebtn.setScaleType(ImageView.ScaleType.FIT_XY);
        mutebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!proximityLock) {
                    mute(mutebtn);
                }
            }
        });
        mutebtn.setLayoutParams(params2);

        ImageButton hangup = new ImageButton(this);
        hangup.setImageResource(R.drawable.ic_call_end_white_24dp);
        hangup.setBackgroundColor(Color.RED);
        hangup.setBackgroundResource(R.drawable.rounded_button);
        //hangup.setScaleType(ImageView.ScaleType.FIT_XY);
        hangup.setMaxWidth(32);//width > 1750? 48: 28);
        GradientDrawable hang = (GradientDrawable) hangup.getBackground();
        hang.setColor(Color.RED);
        hangup.setPadding(24,24,24,24);
        hangup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!proximityLock) {
                    disconnect();
                    countdown.stop();
                }
            }
        });
        hangup.setLayoutParams(params2);

        ImageButton speakerbtn = new ImageButton(this);
        speakerbtn.setImageResource(R.drawable.ic_phonelink_ring_white_24dp);
        speakerbtn.setBackgroundColor(Color.TRANSPARENT);
        //speakerbtn.setScaleType(ImageView.ScaleType.FIT_XY);
        speakerbtn.setMaxWidth(32);//width > 1750? 48: 28);
        speakerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!proximityLock) {
                    showAudioDevices(speakerbtn);
                }
            }
        });
        speakerbtn.setLayoutParams(params2);

        audioSwitch.start((audioDevices, audioDevice) -> {
            updateAudioDeviceIcon(audioDevice, speakerbtn);
            return Unit.INSTANCE;
        });

        inCallButtons.addView(mutebtn);
        inCallButtons.addView(hangup);
        inCallButtons.addView(speakerbtn);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);

        // Setting the parameters on the TextView
        cont.setLayoutParams(lp);
        cont.setOrientation(LinearLayout.VERTICAL);

        cont.addView(callDuration);
        cont.addView(icon);
        cont.addView(inCallButtons);
        cont.addView(panic);
        cont.setPadding(width10,250, width10,0);

        cLayout.addView(BackgroundManager.callBackgroundView(this));
        cLayout.addView(cont);

        countdown.start();

        return cLayout;
    }


    SeekBar slider1;
    SeekBar slider2;
    SeekBar slider3;
    SeekBar slider4;
    SeekBar slider5;
    SeekBar slider6;

    private LinearLayout ratingSlider(SeekBar slider, int numberOfRatings)
    {
        RelativeLayout.LayoutParams tmarign = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        tmarign.setMargins(0,16,0,16);
        tmarign.addRule(RelativeLayout.CENTER_HORIZONTAL);

        // Slider Section
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        LinearLayout sliderSection = new LinearLayout(this);
        sliderSection.setOrientation(LinearLayout.VERTICAL);
        sliderSection.setLayoutParams(tmarign);

        LinearLayout poorGreatText = new LinearLayout(this);
        poorGreatText.setOrientation(LinearLayout.HORIZONTAL);

        TextView poor = new TextView(this);
        TextView great = new TextView(this);
        poor.setText("Poor");
        poor.setLayoutParams(slp);
        poor.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_r));
        great.setText("Great");
        great.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_r));
        great.setLayoutParams(slp);
        poor.setGravity(Gravity.LEFT);
        great.setGravity(Gravity.RIGHT);
        poor.setTextSize(16);
        great.setTextSize(16);

        poorGreatText.addView(poor);
        poorGreatText.addView(new Space(this));
        poorGreatText.addView(great);

        slider = new SeekBar(this);
        slider.setMax(4);
        slider.setProgress(2);
        slider.setPadding(60,0,60,0);
        slider.setThumbOffset(0);
        //slider.setMinimumHeight(30);

        GradientDrawable gD = new GradientDrawable();
        gD.setColor(Color.parseColor(BabbleColors.DARK_ORANGE));
        gD.setShape(GradientDrawable.OVAL);
        gD.setSize(40,40);

        slider.setThumb(gD);


        LinearLayout lines = new LinearLayout(this);
        lines.setOrientation(LinearLayout.HORIZONTAL);

        for(int i=0; i<numberOfRatings; i++)
        {
            TextView rateLine = new TextView(this);
            rateLine.setText("" + (i+1));
            rateLine.setLayoutParams(slp);
            rateLine.setGravity(Gravity.CENTER);
            lines.addView(rateLine);
        }

        //sliderSection.addView(poorGreatText);
        sliderSection.addView(slider);
        sliderSection.addView(lines);

        return sliderSection;
    }


    private RelativeLayout happiRatingScreen()
    {
        RelativeLayout rLay = new RelativeLayout(this);
        rLay.setBackgroundColor(Color.parseColor(BabbleColors.SLATE));
        rLay.setGravity(Gravity.CENTER);



        return rLay;
    }

    private RelativeLayout ratingScreen()
    {
        slider1 = new SeekBar(this);
        slider2 = new SeekBar(this);
        slider3 = new SeekBar(this);
        slider4 = new SeekBar(this);
        slider5 = new SeekBar(this);
        slider6 = new SeekBar(this);

        LinearLayout sliderSection1 = ratingSlider(slider1, 5);
        LinearLayout sliderSection2 = ratingSlider(slider2, 5);
        LinearLayout sliderSection3 = ratingSlider(slider3, 5);
        LinearLayout sliderSection4 = ratingSlider(slider4, 5);
        LinearLayout sliderSection5 = ratingSlider(slider5, 5);
        LinearLayout sliderSection6 = ratingSlider(slider6, 5);


        RelativeLayout.LayoutParams tmarign = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        tmarign.setMargins(0,40,0,40);
        tmarign.addRule(RelativeLayout.CENTER_HORIZONTAL);

        RelativeLayout rLay = new RelativeLayout(this);
        rLay.setBackgroundColor(Color.parseColor(BabbleColors.SLATE));
        rLay.setGravity(Gravity.CENTER);

        LinearLayout cont = new LinearLayout(this);
        //cont.setBackgroundColor(Color.parseColor(BabbleColors.WHITE));
        cont.setBackgroundResource(R.drawable.rounded_box);
        GradientDrawable contBG = (GradientDrawable) cont.getBackground();
        contBG.setColor(Color.WHITE);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(width10,200, width10,200);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        cont.setLayoutParams(params);
        cont.setGravity(Gravity.CENTER);
        cont.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams reportLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        reportLayout.setMargins(0, 20, 0, 20);

        Button reportButton = new Button(this);
        reportButton.setText("Report the call");
        reportButton.setBackgroundResource(R.drawable.rounded_button);
        reportButton.setTextColor(Color.WHITE);
        GradientDrawable reportdraw = (GradientDrawable) reportButton.getBackground();
        reportdraw.setColor(Color.parseColor(BabbleColors.BLACK));
        reportButton.setTransformationMethod(null);
        reportButton.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_r));
        reportButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if(!proximityLock) {
                    //Report call
                    APICalls.reportCall(2);
                    resetUI();
                    ReportPopup();
                }
            }
        });
        reportButton.setTextSize(16);
        reportButton.setLayoutParams(reportLayout);


        Button submitButton = new Button(this);
        submitButton.setText("Submit");
        submitButton.setBackgroundResource(R.drawable.rounded_button);
        submitButton.setTextColor(Color.WHITE);
        submitButton.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_b));
        GradientDrawable subdraw = (GradientDrawable) submitButton.getBackground();
        subdraw.setColor(Color.parseColor(BabbleColors.DARK_ORANGE));
        submitButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                //Submit Rating
                APICalls.rateCall(slider1.getProgress() + 1);
                AppSession.showRating = false;
                AppSession.lastCallSID = "";
                resetUI();
            }
        });

        submitButton.setTransformationMethod(null);
        submitButton.setTypeface(null, Typeface.BOLD);
        submitButton.setTextSize(16);

        TextView title = CustomWidgets.SimpleTextView(this, 20, "How are you feeling?\n");
        cont.addView(title);

        TextView metric1 = CustomWidgets.SimpleTextView(this, 16, "Positivity");
        cont.addView(metric1);
        cont.addView(sliderSection1);

        TextView metric2 = CustomWidgets.SimpleTextView(this, 16, "Energy");
        cont.addView(metric2);
        cont.addView(sliderSection2);

        /*
        TextView metric3 = CustomWidgets.SimpleTextView(this, 16, "Calmness");
        cont.addView(metric3);
        cont.addView(sliderSection3);
        */

        /*
        TextView metric4 = CustomWidgets.SimpleTextView(this, 16, "Anxiety");
        cont.addView(metric4);
        cont.addView(sliderSection4);*/


        TextView metric5 = CustomWidgets.SimpleTextView(this, 16, "Loneliness");
        cont.addView(metric5);
        cont.addView(sliderSection5);

        TextView title2 = CustomWidgets.SimpleTextView(this, 20, "\nHow was your call?\n");
        cont.addView(title2);

        TextView metric6 = CustomWidgets.SimpleTextView(this, 16, "Call Quality");
        cont.addView(metric6);
        cont.addView(sliderSection6);
        cont.addView(submitButton);

        TextView title3 = CustomWidgets.SimpleTextView(this, 20, "\nBad Chat Experience?");
        cont.addView(title3);
        cont.addView(reportButton);

        cont.setPadding(20,20, 20,20);


        rLay.addView(BackgroundManager.callBackgroundView(this));
        rLay.addView(cont);

        return rLay;
    }

    private void InitDefaultView()
    {
        APICalls.getPoolStatus();

        relativeLayout.setBackgroundColor(Color.parseColor(BabbleColors.PURPLE));

        count.setVisibility(View.VISIBLE);

        if (customSwitch != null)
        {
           // customSwitch.setVisibility(View.INVISIBLE); // temporary, will change to accept calls
        }

        SetCallButtonState(Constants.PRESS_TO_CHAT, 1f);
        /*
        if (AppSession.inPool)
        {
            //isCaller = false;
            //mode.setChecked(isCaller);
            //InPoolPopup();
            //return;
        }

        isCaller = isChecked;

        if(isCaller)
        {
            relativeLayout.setBackgroundColor(Color.parseColor(BabbleColors.PURPLE));
            callButton.setAlpha(1f);
            tv.setText("Press to start a call");
            count.setVisibility(View.VISIBLE);
        }*/

        LeavePool();
        JoinPool();
    }

    private Boolean MakeCall()
    {
        LeavePool();
        startCall();
        SetCallButtonState("Hang Up", 1f);
        return false;
    }

    private Boolean HangUp()
    {
        PanicPopup();
        JoinPool();
        return false;
    }

    private void LeavePool()
    {
        APICalls.leavePool();
        AppSession.inPool = false;
        SetCallButtonState(Constants.PRESS_TO_CHAT, 1f);
    }

    private void JoinPool()
    {
        APICalls.joinPool();
        AppSession.inPool = true;
        SetCallButtonState(Constants.PRESS_TO_CHAT, 1f);
    }

    private void SetCallButtonState(String text, Float opacity)
    {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            callButton.setAlpha(opacity);
            tv.setText(text);
        }, 500);
    }

    private void AnimateCallButton()
    {
        callButton.startAnimation(tappedDown());
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            callButton.startAnimation(tappedUp());
        }, 150);
    }

    private void SetAcceptingCalls(Boolean acctingCalls)
    {
        AppSession.acceptingCalls = acctingCalls;

        if (AppSession.acceptingCalls)
        {
            JoinPool();
        }
        else
        {
            LeavePool();
        }
    }

    private Boolean InCall()
    {
        if (activeCall != null)
        {
            return true;
        }
        return false;
    }

    private Boolean HandleCallButtonClicked()
    {
        if(!isBusy)
        {
            isBusy = true;
            AnimateCallButton();

            // if we're using the combined view, the call button will only
            // ever make a call, listening will be automated
            if (AppSettings.combinedViews)
            {
                // the main button action will always be to make a call or hang up
                if (AppSession.poolCount > 0)
                {
                    //MakeCall();
                    ShowPromptsPopup();
                }
                else
                {
                    JoinPool();
                    NoListenersPopup();
                }
            }
            else
            {

                if (isCaller)
                {
                    if (AppSession.poolCount > 0) // there are listeners available
                    {
                        //make call
                        APICalls.leavePool();
                        startCall();
                    }
                    else
                    {
                        NoListenersPopup();
                    }
                }
                else
                {
                    // listener
                    SetCallButtonState("Changing Status", 1f);
                    if (AppSession.inPool)
                    {
                        //remove from pool
                        LeavePool();
                        /*
                        APICalls.leavePool();
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            AppSession.inPool = false;
                            callButton.setAlpha(1f);
                            tv.setText("Press to start listening");
                        }, 500);*/

                    } else {
                        //join pool
                        JoinPool();
                        /*
                        APICalls.joinPool();
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            //retrieveAccessToken();
                            AppSession.inPool = true;
                            callButton.setAlpha(0.5f);
                            tv.setText("You are in the queue to accept calls, hit the Babble button to leave.");
                        }, 500);*/
                    }
                }
            }
            isBusy = false;
        }
        APICalls.getPoolStatus();

        return false; //
    }

    private RelativeLayout mainLayout()
    {
        relativeLayout = new RelativeLayout(this);
        relativeLayout.setBackgroundColor(Color.parseColor(BabbleColors.PURPLE));

        LinearLayout cont = new LinearLayout(this);
        cont.setPadding(8,8,8,8);

        RelativeLayout.LayoutParams tmarign = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        tmarign.setMargins(0,40,0,40);
        tmarign.addRule(RelativeLayout.CENTER_HORIZONTAL);

        RelativeLayout.LayoutParams btnmarign = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        btnmarign.setMargins(0,128,0,16);
        btnmarign.height = Math.round(height / 4);
        btnmarign.addRule(RelativeLayout.CENTER_HORIZONTAL);

        RelativeLayout.LayoutParams marign = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        marign.setMargins(0,16,0,72);
        marign.addRule(RelativeLayout.CENTER_HORIZONTAL);

        // CREATE MAIN INSTRUCTION LABEL
        tv = new TextView(this);
        tv.setText(Constants.PRESS_TO_CHAT);
        tv.setTextSize(28);
        tv.setTextColor(Color.WHITE);
        tv.setGravity(Gravity.CENTER);
        tv.setLayoutParams(marign);
        tv.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_b));

        if(width > 1500)
        {
            tv.setTextSize(32);
        }

        if (width <= 800)
        {
            tv.setPadding(0, 20, 0, 0);
            tv.setTextSize(24);
        }

        // CREATE MAIN CALL BUTTON
        callButton = new ImageButton(this);
        callButton.setImageResource(R.drawable.ic_babble_circle_active);
        callButton.setBackgroundColor(Color.TRANSPARENT);
        callButton.setLayoutParams(btnmarign);
        callButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        callButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                HandleCallButtonClicked();
            }
        });

        // CREATE LOGOUT BUTTON
        Button logout = new Button(this);
        logout.setText(Constants.LOGOUT);
        logout.setTextSize(16);
        logout.setTransformationMethod(null);
        logout.setBackgroundResource(R.drawable.rounded_button);
        logout.setTextColor(Color.WHITE);
        GradientDrawable logdraw = (GradientDrawable) logout.getBackground();
        logdraw.setColor(Color.TRANSPARENT);
        //logout.setPadding(8,8,8,8);
        logout.setWidth((int) Math.round(width * 0.5));
        logout.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_b));
        logout.setGravity(Gravity.END);
        logout.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                LogOut();
            }
        });

        if(width > 1500)
        {
            logout.setTextSize(26);
        }

        Button issue = new Button(this);
        issue.setText(Constants.HAD_AN_ISSUE);
        issue.setTextSize(16);
        issue.setTransformationMethod(null);
        issue.setBackgroundResource(R.drawable.rounded_button);
        issue.setTextColor(Color.WHITE);
        logdraw.setColor(Color.TRANSPARENT);
        //issue.setPadding(8,8,8,8);
        issue.setWidth((int) Math.round(width * 0.5));
        issue.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_b));
        issue.setGravity(Gravity.START);
        issue.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                String[] addresses = new String[]{ "support@mybabble.chat" };
                String subject = "Issue with Babble Android Application";

                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL, addresses);
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                startActivity(intent);
            }
        });
        /*
        Button issueText = new Button(this);
        issueText.setText("Had an issue?");
        issueText.setBackgroundColor(Color.TRANSPARENT);
        issueText.setTransformationMethod(null);
        issueText.setTextSize(16);
        issueText.setTextColor(Color.WHITE);
        issueText.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_b));
        issueText.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                String[] addresses = new String[]{ "support@mybabble.chat" };
                String subject = "Issue with Babble Android Application";

                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL, addresses);
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                startActivity(intent);
            }
        });*/


        count = new TextView(this);

        count.setText(Constants.USERS_ONLINE + " : " + AppSession.poolCount + "\n\n");
        count.setTextColor(Color.parseColor(BabbleColors.ORANGE));
        count.setTextSize(20);

        count.setGravity(Gravity.CENTER);
        count.setVisibility(View.VISIBLE);
        count.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_b));

        // Defining the layout parameters of the TextView
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);

        // Setting the parameters on the TextView
        cont.setLayoutParams(lp);
        cont.setOrientation(LinearLayout.VERTICAL);



        RelativeLayout bottomButtons = new RelativeLayout(this);
        RelativeLayout.LayoutParams bbp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        bbp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM );
        bbp.setMargins(width10,0,width10,32);
        bottomButtons.setLayoutParams(bbp);
        bottomButtons.setGravity(Gravity.CENTER);


        //bottomButtons.addView(count);
        //cont.addView(issue);
        LinearLayout buttonsHolder = new LinearLayout(this);
        buttonsHolder.setPadding(8,8,8,8);
        buttonsHolder.setOrientation(LinearLayout.HORIZONTAL);
        buttonsHolder.setGravity(Gravity.CENTER);
        buttonsHolder.addView(issue);
        buttonsHolder.addView(logout);

        //bottomButtons.addView(issue);
        bottomButtons.addView(buttonsHolder);

        // create the "ACCEPT CALLS" switch
        customSwitch = new LinearLayout(this);
        customSwitch.setOrientation(LinearLayout.HORIZONTAL);
        customSwitch.setGravity(Gravity.CENTER);
        customSwitch.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        acceptingCalls = CustomWidgets.SimpleTextView(this, 16, Constants.ACCEPTING_CALLS + "\n", Color.WHITE);
        acceptingCalls.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        yesText = CustomWidgets.SimpleTextView(this, 16, Constants.YES, Color.WHITE);
        noText = CustomWidgets.SimpleTextView(this, 16, Constants.NO, Color.WHITE);

        //Allows screen scaling but in case of smaller screens does not cause an issue
        int toggleTextWidth = (int) Math.round(width * 0.25);

        if(toggleTextWidth < 300)
        {
            toggleTextWidth = 250;
        }

        yesText.setWidth(toggleTextWidth);
        yesText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        noText.setWidth(toggleTextWidth);
        noText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);

        mode = new Switch(this);
        mode.setChecked(isCaller);
        mode.setGravity(Gravity.CENTER);
        mode.setTextColor(Color.WHITE);
        mode.getThumbDrawable().setTint(ContextCompat.getColor(this, R.color.toggleCaller));

        mode.setChecked(AppSession.acceptingCalls);
        UpdatToggle();


        mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isBusy)
                {
                    return;
                }
                APICalls.getPoolStatus();

                AppSession.acceptingCalls = !AppSession.acceptingCalls;
                mode.setChecked(AppSession.acceptingCalls);
                UpdatToggle();
            }
        });

        customSwitch.addView(noText);
        customSwitch.addView(mode);
        customSwitch.addView(yesText);

        // Adding the TextView to the RelativeLayout as a child`

        cont.addView(callButton);
        cont.addView(tv);

        cont.addView(count);

        cont.addView(acceptingCalls);
        cont.addView(customSwitch);




        relativeLayout.addView(BackgroundManager.callBackgroundView(this));
        relativeLayout.addView(cont);

        relativeLayout.addView(bottomButtons);

        JoinPool();

        return relativeLayout;
    }

    private void UpdatToggle()
    {
        if (AppSession.acceptingCalls)
        {
            acceptingCalls.setAlpha(1.0f);
            noText.setAlpha(0.5f);
            yesText.setAlpha(1.0f);
        }
        else
        {
            acceptingCalls.setAlpha(1.0f);
            noText.setAlpha(1.0f);
            yesText.setAlpha(0.5f);
        }
    }




    private RelativeLayout mainLayoutOld()
    {
        // Creating a new RelativeLayout
        relativeLayout = new RelativeLayout(this);
        LinearLayout cont = new LinearLayout(this);

        relativeLayout.setBackgroundColor(Color.parseColor(BabbleColors.ORANGE));

        RelativeLayout.LayoutParams tmarign = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        tmarign.setMargins(0,40,0,40);
        tmarign.addRule(RelativeLayout.CENTER_HORIZONTAL);

        RelativeLayout.LayoutParams btnmarign = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        btnmarign.setMargins(0,40,0,40);
        btnmarign.height = Math.round(height / 4);
        btnmarign.addRule(RelativeLayout.CENTER_HORIZONTAL);

        RelativeLayout.LayoutParams marign = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        marign.setMargins(0,100,0,0);
        marign.addRule(RelativeLayout.CENTER_HORIZONTAL);

        tv = new TextView(this);
        tv.setText(Constants.PRESS_TO_LISTEN);
        tv.setTextSize(28);
        tv.setTextColor(Color.WHITE);
        tv.setGravity(Gravity.CENTER);
        tv.setLayoutParams(marign);
        tv.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_b));

        if(width > 1500)
        {
            tv.setTextSize(32);
        }

        if (width <= 800)
        {
            tv.setPadding(0, 20, 0, 0);
            tv.setTextSize(24);
        }

        callButton = new ImageButton(this);
        callButton.setImageResource(R.drawable.ic_babble_circle_active);
        callButton.setBackgroundColor(Color.TRANSPARENT);
        callButton.setLayoutParams(btnmarign);
        callButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        callButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                HandleCallButtonClicked();

                /*
                if(!isBusy)
                {
                    isBusy = true;
                    callButton.startAnimation(tappedDown());
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        callButton.startAnimation(tappedUp());
                    }, 150);

                    if (AppSettings.combinedViews)
                    {
                        // the main button action will always be to make a call
                        if (AppSession.poolCount > 0)
                        {
                            APICalls.leavePool();
                            startCall();
                        }
                        else
                        {
                            NoListenersPopup();
                        }
                    }
                    else
                    {
                        if (isCaller) {
                            if (AppSession.poolCount > 0) {
                                //make call
                                APICalls.leavePool();
                                startCall();
                            } else {
                                NoListenersPopup();
                            }
                        } else {
                            tv.setText("Changing Status");
                            if (AppSession.inPool) {
                                //remove from pool
                                APICalls.leavePool();
                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    AppSession.inPool = false;
                                    callButton.setAlpha(1f);
                                    tv.setText("Press to start listening");
                                }, 500);

                            } else {
                                //join pool
                                APICalls.joinPool();
                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    //retrieveAccessToken();
                                    AppSession.inPool = true;
                                    callButton.setAlpha(0.5f);
                                    tv.setText("You are in the queue to accept calls, hit the Babble button to leave.");
                                }, 500);
                            }

                        }
                    }
                    isBusy = false;
                }
                APICalls.getPoolStatus();
                */
            }
        });

//        if(AppSession.inPool)
//        {
//            callButton.setAlpha(0.5f);
//            tv.setText("Hit button to leave queue");
//        }
//        else
//        {
//            callButton.setAlpha(1f);
//            tv.setText("Press to start listening");
//        }

        Button logout = new Button(this);
        logout.setText(Constants.LOGOUT);
        logout.setTextSize(16);
        logout.setTransformationMethod(null);
        logout.setBackgroundResource(R.drawable.rounded_button);
        logout.setTextColor(Color.RED);
        GradientDrawable logdraw = (GradientDrawable) logout.getBackground();
        logdraw.setColor(Color.WHITE);
        logout.setPadding(50,0,50,0);
        logout.setWidth((int) Math.round(width * 0.9));
        logout.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_b));
        logout.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                LogOut();
            }
        });

        if(width > 1500)
        {
            logout.setTextSize(26);
        }

        customSwitch = new LinearLayout(this);
        customSwitch.setOrientation(LinearLayout.HORIZONTAL);
        customSwitch.setGravity(Gravity.CENTER);
        customSwitch.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView listenerText = new TextView(this);
        listenerText.setText(Constants.LISTENER);
        listenerText.setTextSize(18);
        listenerText.setTextColor(Color.WHITE);
        listenerText.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_b));

        TextView callerText = new TextView(this);
        callerText.setText(Constants.CALLER);
        callerText.setTextSize(18);
        callerText.setTextColor(Color.WHITE);
        callerText.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_b));

        if(width > 1500)
        {
            listenerText.setTextSize(24);
            callerText.setTextSize(24);
        }

        //Allows screen scaling but in case of smaller screens does not cause an issue
        int toggleTextWidth = (int) Math.round(width * 0.25);

        if(toggleTextWidth < 300)
        {
            toggleTextWidth = 250;
        }

        listenerText.setWidth(toggleTextWidth);
        listenerText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        callerText.setWidth(toggleTextWidth);
        callerText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);

        mode = new Switch(this);
        mode.setChecked(isCaller);
        mode.setGravity(Gravity.CENTER);
        mode.setTextColor(Color.WHITE);
        mode.getThumbDrawable().setTint(ContextCompat.getColor(this, R.color.toggleListner));

        mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isBusy) {
                    return;
                }
                APICalls.getPoolStatus();
                //retrieveAccessToken();

                if (AppSession.inPool)
                {
                    isCaller = false;
                    mode.setChecked(isCaller);
                    InPoolPopup();
                    return;
                }

                isCaller = isChecked;

                if(isCaller)
                {
                    relativeLayout.setBackgroundColor(Color.parseColor(BabbleColors.PURPLE));
                    mode.getThumbDrawable().setTint(ContextCompat.getColor(getApplicationContext(), R.color.toggleCaller));
                    callButton.setAlpha(1f);
                    tv.setText(Constants.PRESS_TO_CHAT);
                    count.setVisibility(View.VISIBLE);
                    logout.setVisibility(View.GONE);
                }
                else
                {
                    if(AppSession.inPool)
                    {
                        //callButton.setAlpha(0.5f);
                        callButton.setAlpha(1f);
                        mode.setChecked(false);
                        InPoolPopup();
                    }
                    else
                    {
                        callButton.setAlpha(1f);
                    }
                    tv.setText(Constants.PRESS_TO_LISTEN);
                    relativeLayout.setBackgroundColor(Color.parseColor(BabbleColors.ORANGE));
                    mode.getThumbDrawable().setTint(ContextCompat.getColor(getApplicationContext(), R.color.toggleListner));
                    count.setVisibility((View.GONE));
                    logout.setVisibility(View.VISIBLE);
                }
            }
        });

        customSwitch.addView(listenerText);
        customSwitch.addView(mode);
        customSwitch.addView(callerText);

        Button issueText = new Button(this);
        issueText.setText(Constants.HAD_AN_ISSUE);
        issueText.setBackgroundColor(Color.TRANSPARENT);
        issueText.setTransformationMethod(null);
        issueText.setTextSize(20);
        issueText.setTextColor(Color.WHITE);
        issueText.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_b));
        issueText.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                String[] addresses = new String[]{ "support@mybabble.chat" };
                String subject = "Issue with Babble Android Application";

                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL, addresses);
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                startActivity(intent);
            }
        });

        count = new TextView(this);
        count.setText(Constants.USERS_ONLINE + " : " + AppSession.poolCount + "\n\n");
        count.setTextColor(Color.parseColor(BabbleColors.ORANGE));
        count.setTextSize(20);
        count.setGravity(Gravity.CENTER);

        count.setVisibility(View.GONE);
        count.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_b));

        // Defining the layout parameters of the TextView
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);

        // Setting the parameters on the TextView
        cont.setLayoutParams(lp);
        cont.setOrientation(LinearLayout.VERTICAL);

        // Adding the TextView to the RelativeLayout as a child`
        cont.addView(tv);
        cont.addView(callButton);
        cont.addView(customSwitch);
        cont.addView(issueText);

        RelativeLayout bottomButtons = new RelativeLayout(this);
        RelativeLayout.LayoutParams bbp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        bbp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM );
        bbp.setMargins(width10,0,width10,32);
        bottomButtons.setLayoutParams(bbp);
        bottomButtons.setGravity(Gravity.CENTER);


        //count.setGravity(Gravity.CENTER);
        //logout.setLayoutParams(bbp);

        bottomButtons.addView(count);
        bottomButtons.addView(logout);

        relativeLayout.addView(BackgroundManager.callBackgroundView(this));
        relativeLayout.addView(cont);
        relativeLayout.addView(bottomButtons);

        return relativeLayout;
    }

    public Animation tappedDown()
    {
        Animation anim = new ScaleAnimation(
                1f, 0.95f,
                1f, 0.95f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
            anim.setFillAfter(true);
            anim.setDuration(150);
        return anim;
    }

    public Animation tappedUp()
    {
        Animation anim = new ScaleAnimation(
                1f, 1.05f,
                1f, 1.05f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setFillAfter(true);
        anim.setDuration(150);
        return anim;
    }

    public void PanicPopup()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Are you sure you want to end the call and report this user?");
        builder.setTitle("Report call");

        builder.setPositiveButton(Constants.YES, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Continue to destination, user accepted
                AppSession.skipRating = true;
                APICalls.reportCall(2);
                disconnect();
                countdown.stop();
                ReportPopup();
                dialog.dismiss();
                ReportPopup();
            }
        });

        builder.setNegativeButton(Constants.NO, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Continue to destination, user accepted
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void ReportPopup()
    {
        ShowDismissiblePrompt(
                "Call reported",
                "Thank you for reporting this call. We will review and take the appropriate action",
                Constants.OK
        );
        /*
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Thank you for reporting this call. We will review and take the appropriate action");
        builder.setTitle("Call reported");

        builder.setPositiveButton(Constants.OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Continue to destination, user accepted
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();*/
    }

    public void InPoolPopup()
    {
        ShowDismissiblePrompt(
                "Currently In Pool",
                "You're currently in the listener pool, please leave before attempting to make a call.",
                Constants.OK
        );
        /*
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("You're currently in the listener pool, please leave before attempting to make a call.");
        builder.setTitle("Currently In Pool");

        builder.setPositiveButton(Constants.OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Continue to destination, user accepted
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();*/
    }

    public void ShowDismissiblePrompt(String title, String message, String buttonText)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);

        builder.setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Continue to destination, user accepted
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    
    
    public void ShowPromptsPopup()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(Constants.HAPPI_PROMPT_INFORMATION);
        builder.setTitle(Constants.HAPPI_PROMPT_TITLE);
        
        builder.setPositiveButton(Constants.OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Continue to destination, user accepted
                dialog.dismiss();
                MakeCall();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void NoListenersPopup()
    {
        ShowDismissiblePrompt(
                "No Listeners",
                "There are currently no active listeners, please try calling again later",
                Constants.OK
        );

        /*AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("There are currently no active listeners, please try calling again later");
        builder.setTitle("No Listeners");

        builder.setPositiveButton(Constants.OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Continue to destination, user accepted
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();*/
    }




    private RegistrationListener registrationListener() {
        return new RegistrationListener() {
            @Override
            public void onRegistered(@NonNull String accessToken, @NonNull String fcmToken) {
                Log.d(TAG, "Successfully registered FCM " + fcmToken);
            }

            @Override
            public void onError(@NonNull RegistrationException error,
                                @NonNull String accessToken,
                                @NonNull String fcmToken) {
                String message = String.format(
                        Locale.US,
                        "Registration Error: %d, %s",
                        error.getErrorCode(),
                        error.getMessage());
                Log.e(TAG, message);
                //Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
            }
        };
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(InCall())
        {
            if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                if (event.values[0] >= -SENSOR_SENSITIVITY && event.values[0] <= SENSOR_SENSITIVITY) {
                    //near
                    WindowManager.LayoutParams WMLP = getWindow().getAttributes();
                    WMLP.screenBrightness = 0F;
                    getWindow().setAttributes(WMLP);
                    InCall(true);

                } else {
                    //far
                    WindowManager.LayoutParams WMLP = getWindow().getAttributes();
                    WMLP.screenBrightness = screenBrightnessLevel;
                    getWindow().setAttributes(WMLP);
                    InCall(false);
                }
            }
        }
        else
        {
            WindowManager.LayoutParams WMLP = getWindow().getAttributes();
            WMLP.screenBrightness = screenBrightnessLevel;
            getWindow().setAttributes(WMLP);
            InCall(false);
        }

    }

    void InCall(boolean inCall){
        if(inCall){
            proximityLock = true;
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES);
        }
        else {
            proximityLock = false;
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private Call.Listener callListener() {
        return new Call.Listener() {
            /*
             * This callback is emitted once before the Call.Listener.onConnected() callback when
             * the callee is being alerted of a Call. The behavior of this callback is determined by
             * the answerOnBridge flag provided in the Dial verb of your TwiML application
             * associated with this client. If the answerOnBridge flag is false, which is the
             * default, the Call.Listener.onConnected() callback will be emitted immediately after
             * Call.Listener.onRinging(). If the answerOnBridge flag is true, this will cause the
             * call to emit the onConnected callback only after the call is answered.
             * See answeronbridge for more details on how to use it with the Dial TwiML verb. If the
             * twiML response contains a Say verb, then the call will emit the
             * Call.Listener.onConnected callback immediately after Call.Listener.onRinging() is
             * raised, irrespective of the value of answerOnBridge being set to true or false
             */
            @Override
            public void onRinging(@NonNull Call call) {
                Log.d(TAG, "Ringing");
                /*
                 * When [answerOnBridge](https://www.twilio.com/docs/voice/twiml/dial#answeronbridge)
                 * is enabled in the <Dial> TwiML verb, the caller will not hear the ringback while
                 * the call is ringing and awaiting to be accepted on the callee's side. The application
                 * can use the `SoundPoolManager` to play custom audio files between the
                 * `Call.Listener.onRinging()` and the `Call.Listener.onConnected()` callbacks.
                 */
                //If this is missing a reference you need to go to build -> Run generate sources gradle
                if (BuildConfig.playCustomRingback) {
                    SoundPoolManager.getInstance(CallActivity.this).playRinging();
                }
            }

            @Override
            public void onConnectFailure(@NonNull Call call, @NonNull CallException error) {
                audioSwitch.deactivate();
                if (BuildConfig.playCustomRingback) {
                    SoundPoolManager.getInstance(CallActivity.this).stopRinging();
                }
                Log.d(TAG, "Connect failure");
                String message = String.format(
                        Locale.US,
                        "Call Error: %d, %s",
                        error.getErrorCode(),
                        error.getMessage());
                Log.e(TAG, message);
                //Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
                resetUI();
            }

            @Override
            public void onConnected(@NonNull Call call) {
                audioSwitch.activate();
                if (BuildConfig.playCustomRingback) {
                    SoundPoolManager.getInstance(CallActivity.this).stopRinging();
                }
                Log.d(TAG, "Connected");
                activeCall = call;
                AppSession.lastCallSID = activeCall.getSid();
            }

            @Override
            public void onReconnecting(@NonNull Call call, @NonNull CallException callException) {
                Log.d(TAG, "onReconnecting");
            }

            @Override
            public void onReconnected(@NonNull Call call) {
                Log.d(TAG, "onReconnected");
            }

            @Override
            public void onDisconnected(@NonNull Call call, CallException error) {
                audioSwitch.deactivate();
                if (BuildConfig.playCustomRingback) {
                    SoundPoolManager.getInstance(CallActivity.this).stopRinging();
                }
                Log.d(TAG, "Disconnected");
                if (error != null) {
                    String message = String.format(
                            Locale.US,
                            "Call Error: %d, %s",
                            error.getErrorCode(),
                            error.getMessage());
                    Log.e(TAG, message);
                    //Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
                }

                if(AppSession.skipRating)
                {
                    AppSession.showRating = false;
                    AppSession.skipRating = false;
                    resetUI();
                }
                else
                {
                    AppSession.showRating = true;
                    ratingUI();
                }
            }
            /*
             * currentWarnings: existing quality warnings that have not been cleared yet
             * previousWarnings: last set of warnings prior to receiving this callback
             *
             * Example:
             *   - currentWarnings: { A, B }
             *   - previousWarnings: { B, C }
             *
             * Newly raised warnings = currentWarnings - intersection = { A }
             * Newly cleared warnings = previousWarnings - intersection = { C }
             */
            public void onCallQualityWarningsChanged(@NonNull Call call,
                                                     @NonNull Set<Call.CallQualityWarning> currentWarnings,
                                                     @NonNull Set<Call.CallQualityWarning> previousWarnings) {

                if (previousWarnings.size() > 1) {
                    Set<Call.CallQualityWarning> intersection = new HashSet<>(currentWarnings);
                    currentWarnings.removeAll(previousWarnings);
                    intersection.retainAll(previousWarnings);
                    previousWarnings.removeAll(intersection);
                }

                String message = String.format(
                        Locale.ENGLISH,
                        "Newly raised warnings: " + currentWarnings + " Clear warnings " + previousWarnings);
                Log.e(TAG, message);
                //Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
            }
        };
    }

    private void initStatusChecker()
    {
        statusCheckTask = new TimerTask()
        {
            @Override
            public void run() {
                APICalls.getPoolStatus();
                APICalls.getPoolCount();

                handler.postDelayed(new Runnable() {
                    public void run() {
                        //refresh UI
                        if (!isBackgrounded)
                        {
                            if (!InCall()) {
                                relativeLayout.invalidate();
                                count.setText(Constants.USERS_ONLINE + " : " + AppSession.poolCount + "\n\n");

                                if (AppSession.inPool) {
                                    //callButton.setAlpha(0.5f);
                                    callButton.setAlpha(1f);
                                } else {
                                    callButton.setAlpha(1f);
                                }
                                relativeLayout.setVisibility(View.GONE);
                                relativeLayout.setVisibility(View.VISIBLE);
                                Log.d("debug", "refreshing view");
                            }
                        }
                    }
                }, 1000);
            }
        };
    }

    private void setCallUI()
    {
        //set in call view here
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT,
                RelativeLayout.LayoutParams.FILL_PARENT);


        // Setting the RelativeLayout as our content view
        setContentView(callLayout(), rlp);
    }

    /*
     * Reset UI elements
     */
    private void resetUI() {
        //set standard call view here
        //set in call view here
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT,
                RelativeLayout.LayoutParams.FILL_PARENT);

        isCaller = false;//true;
        activeCall = null;
        isBackgrounded = false;

        // Setting the RelativeLayout as our content view
        setContentView(mainLayout(), rlp);
    }

    private void ratingUI()
    {
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT,
                RelativeLayout.LayoutParams.FILL_PARENT);
        // Setting the RelativeLayout as our content view
        setContentView(ratingScreen(), rlp);
    }

    private void mute(ImageButton btn) {
        if (InCall()) {
            boolean mute = !activeCall.isMuted();
            activeCall.mute(mute);
            if(mute)
            {
                btn.setAlpha(1f);
            }
            else
            {
                btn.setAlpha(0.7f);
            }
            btn.invalidate();
        }
    }

    private void showAudioDevices(ImageButton btn) {
        AudioDevice selectedDevice = audioSwitch.getSelectedAudioDevice();
        List<AudioDevice> availableAudioDevices = audioSwitch.getAvailableAudioDevices();
        Log.d("debug", "available devices: " + availableAudioDevices.size());

        if (selectedDevice != null) {
            int selectedDeviceIndex = availableAudioDevices.indexOf(selectedDevice);

            ArrayList<String> audioDeviceNames = new ArrayList<>();
            for (AudioDevice a : availableAudioDevices) {
                audioDeviceNames.add(a.getName());
            }

            new AlertDialog.Builder(this)
                    .setTitle("Select Device")
                    .setSingleChoiceItems(
                            audioDeviceNames.toArray(new CharSequence[0]),
                            selectedDeviceIndex,
                            (dialog, index) -> {
                                dialog.dismiss();
                                AudioDevice selectedAudioDevice = availableAudioDevices.get(index);
                                updateAudioDeviceIcon(selectedAudioDevice, btn);
                                audioSwitch.selectDevice(selectedAudioDevice);
                            }).create().show();
        }
        else
        {
            Log.d("debug", "no selected audio device");
        }
    }

    private void updateAudioDeviceIcon(AudioDevice selectedAudioDevice, ImageButton btn) {

        if (selectedAudioDevice instanceof AudioDevice.BluetoothHeadset) {
            btn.setImageResource(R.drawable.ic_bluetooth_white_24dp);
        } else if (selectedAudioDevice instanceof AudioDevice.WiredHeadset) {
            btn.setImageResource(R.drawable.ic_headset_mic_white_24dp);
        } else if (selectedAudioDevice instanceof AudioDevice.Earpiece) {
            btn.setImageResource(R.drawable.ic_phonelink_ring_white_24dp);
        } else if (selectedAudioDevice instanceof AudioDevice.Speakerphone) {
            btn.setImageResource(R.drawable.ic_volume_up_white_24dp);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
        isBackgrounded = false;
        SetCallButtonState(Constants.PRESS_TO_CHAT, 1f);

        /*
        if (AppSession.inPool)
        {
            SetCallButtonState(Constants.PRESS_TO_CHAT, 1f);
        }
        else
        {
            SetCallButtonState(Constants.PRESS_TO_LISTEN, 1f);
        }*/
    }

        @Override
        protected void onPause() {
        super.onPause();
        unregisterReceiver();
        mSensorManager.unregisterListener(this);
        isBackgrounded = true;
    }

    @Override
    public void onDestroy() {
        /*
         * Tear down audio device management and restore previous volume stream
         */
        audioSwitch.stop();
        //setVolumeControlStream(savedVolumeControlStream);
        SoundPoolManager.getInstance(this).release();
        super.onDestroy();
    }

    private void handleIncomingCallIntent(Intent intent) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            activeCallInvite = intent.getParcelableExtra(Constants.INCOMING_CALL_INVITE);
            activeCallNotificationId = intent.getIntExtra(Constants.INCOMING_CALL_NOTIFICATION_ID, 0);

            switch (action) {
                case Constants.ACTION_INCOMING_CALL:
                    handleIncomingCall();
                    break;
                case Constants.ACTION_INCOMING_CALL_NOTIFICATION:
                    showIncomingCallDialog();
                    break;
                case Constants.ACTION_CANCEL_CALL:
                    handleCancel();
                    break;
                case Constants.ACTION_FCM_TOKEN:
                    retrieveAccessToken();
                    break;
                case Constants.ACTION_ACCEPT:
                    answer();
                    break;
                default:
                    break;
            }
        }
    }

    private void handleIncomingCall() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            showIncomingCallDialog();
        } else {
            if (isAppVisible()) {
                showIncomingCallDialog();
            }
        }
    }

    private void handleCancel() {
        if (alertDialog != null && alertDialog.isShowing()) {
            SoundPoolManager.getInstance(this).stopRinging();
            alertDialog.cancel();
        }
    }

    private void registerReceiver() {
        if (!isReceiverRegistered) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Constants.ACTION_INCOMING_CALL);
            intentFilter.addAction(Constants.ACTION_CANCEL_CALL);
            intentFilter.addAction(Constants.ACTION_FCM_TOKEN);
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    voiceBroadcastReceiver, intentFilter);
            isReceiverRegistered = true;
        }
    }

    private void unregisterReceiver() {
        if (isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(voiceBroadcastReceiver);
            isReceiverRegistered = false;
        }
    }

    private class VoiceBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && (action.equals(Constants.ACTION_INCOMING_CALL) || action.equals(Constants.ACTION_CANCEL_CALL))) {
                /*
                 * Handle the incoming or cancelled call invite
                 */
                Log.d("Call intent", intent.toString());
                handleIncomingCallIntent(intent);
            }
        }
    }

    private DialogInterface.OnClickListener answerCallClickListener() {
        return (dialog, which) -> {
            Log.d(TAG, "Clicked accept");
            Intent acceptIntent = new Intent(getApplicationContext(), IncomingCallNotificationService.class);
            acceptIntent.setAction(Constants.ACTION_ACCEPT);
            acceptIntent.putExtra(Constants.INCOMING_CALL_INVITE, activeCallInvite);
            acceptIntent.putExtra(Constants.INCOMING_CALL_NOTIFICATION_ID, activeCallNotificationId);
            Log.d(TAG, "Clicked accept startService");
            startService(acceptIntent);
//            APICalls.leavePool();
        };
    }

    private DialogInterface.OnClickListener callClickListener() {
        return (dialog, which) -> {
            // Place a call
            EditText contact = ((AlertDialog) dialog).findViewById(com.dfsl.mybabble.R.id.contact);
            params.put("to", contact.getText().toString());
            ConnectOptions connectOptions = new ConnectOptions.Builder(AppSession.aToken)
                    .params(params)
                    .build();
            activeCall = Voice.connect(CallActivity.this, connectOptions, callListener);
            setCallUI();
            alertDialog.dismiss();
        };
    }

    private void startCall()
    {
        //params.put("to", contact.getText().toString());
        ConnectOptions connectOptions = new ConnectOptions.Builder(AppSession.aToken)
                .build();
        activeCall = Voice.connect(CallActivity.this, connectOptions, callListener);
        setCallUI();
        AppSession.lastCallSID = activeCall.getSid();
    }

    private DialogInterface.OnClickListener cancelCallClickListener() {
        return (dialogInterface, i) -> {
            SoundPoolManager.getInstance(CallActivity.this).stopRinging();
            if (activeCallInvite != null) {
                Intent intent = new Intent(CallActivity.this, IncomingCallNotificationService.class);
                intent.setAction(Constants.ACTION_REJECT);
                intent.putExtra(Constants.INCOMING_CALL_INVITE, activeCallInvite);
                startService(intent);
            }
            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
        };
    }

    public static AlertDialog createIncomingCallDialog(
            Context context,
            CallInvite callInvite,
            DialogInterface.OnClickListener answerCallClickListener,
            DialogInterface.OnClickListener cancelClickListener) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setIcon(com.dfsl.mybabble.R.drawable.ic_call_black_24dp);
        alertDialogBuilder.setTitle("Babble");
        alertDialogBuilder.setPositiveButton("Accept", answerCallClickListener);
        alertDialogBuilder.setNegativeButton("Reject", cancelClickListener);
        alertDialogBuilder.setMessage(callInvite.getFrom() + " is calling with " + returnVerifiedString(callInvite) + " status");
        alertDialogBuilder.setCancelable(false);
        return alertDialogBuilder.create();
    }

    static String returnVerifiedString(CallInvite callInvite)
    {
        try {
            if (callInvite.getCallerInfo().isVerified()) {
                return "verified";
            }
        }
        catch (Exception ex){
            //Could not get user status
        }
        return "unverified";
    }

    /*
     * Register your FCM token with Twilio to receive incoming call invites
     */
    private void registerForCallInvites() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, instanceIdResult -> {
            String fcmToken = instanceIdResult.getToken();
            Log.i(TAG, "Registering with FCM");
            Voice.register(AppSession.aToken, Voice.RegistrationChannel.FCM, fcmToken, registrationListener);
        });
    }

    private View.OnClickListener callActionFabClickListener() {
        return v -> {
            alertDialog = createCallDialog(callClickListener(), cancelCallClickListener(), CallActivity.this);
            alertDialog.show();
        };
    }

    private View.OnClickListener hangupActionFabClickListener() {
        return v -> {
            SoundPoolManager.getInstance(CallActivity.this).playDisconnect();
            resetUI();
            disconnect();

        };
    }

    /*
     * Accept an incoming Call
     */
    private void answer() {
        SoundPoolManager.getInstance(this).stopRinging();
        activeCallInvite.accept(this, callListener);
        notificationManager.cancel(activeCallNotificationId);
        setCallUI();
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    /*
     * Disconnect from Call
     */
    private void disconnect() {
        if (InCall()) {
            activeCall.disconnect();
            activeCall = null;
        }
    }

    private void applyFabState(FloatingActionButton button, boolean enabled) {
        // Set fab as pressed when call is on hold
        ColorStateList colorStateList = enabled ?
                ColorStateList.valueOf(ContextCompat.getColor(this,
                        com.dfsl.mybabble.R.color.colorPrimaryDark)) :
                ColorStateList.valueOf(ContextCompat.getColor(this,
                        com.dfsl.mybabble.R.color.colorAccent));
        button.setBackgroundTintList(colorStateList);
    }

    private boolean checkPermissionForMicrophone() {
        int resultMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return resultMic == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissionForMicrophone() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
            Snackbar.make(relativeLayout,
                    "Microphone permissions needed for call functionality. Please allow in your application settings.",
                    Snackbar.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MIC_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        /*
         * Check if microphone permissions is granted
         */
        if (requestCode == MIC_PERMISSION_REQUEST_CODE && permissions != null) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(relativeLayout,
                        "Microphone permissions needed. Please allow in your application settings.",
                        Snackbar.LENGTH_LONG).show();
            } else {
                retrieveAccessToken();
            }
        }
    }

    private static AlertDialog createCallDialog(final DialogInterface.OnClickListener callClickListener,
                                                final DialogInterface.OnClickListener cancelClickListener,
                                                final Activity activity) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        alertDialogBuilder.setIcon(com.dfsl.mybabble.R.drawable.ic_call_black_24dp);
        alertDialogBuilder.setTitle("Call");
        alertDialogBuilder.setPositiveButton("Call", callClickListener);
        alertDialogBuilder.setNegativeButton(Constants.CANCEL, cancelClickListener);
        alertDialogBuilder.setCancelable(false);

        LayoutInflater li = LayoutInflater.from(activity);
        View dialogView = li.inflate(
                com.dfsl.mybabble.R.layout.dialog_call,
                activity.findViewById(android.R.id.content),
                false);
        final EditText contact = dialogView.findViewById(com.dfsl.mybabble.R.id.contact);
        contact.setHint(R.string.callee);
        alertDialogBuilder.setView(dialogView);

        return alertDialogBuilder.create();

    }

    private void showIncomingCallDialog() {
        SoundPoolManager.getInstance(this).playRinging();
        if (activeCallInvite != null) {
            alertDialog = createIncomingCallDialog(CallActivity.this,
                    activeCallInvite,
                    answerCallClickListener(),
                    cancelCallClickListener());
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    }

    private boolean isAppVisible() {
        return ProcessLifecycleOwner
                .get()
                .getLifecycle()
                .getCurrentState()
                .isAtLeast(Lifecycle.State.STARTED);
    }

    /*
     * Get an access token from your Twilio access token server
     */
    private void retrieveAccessToken() {
        /*Ion.with(this).load(TWILIO_ACCESS_TOKEN_SERVER_URL + "?identity=" + identity)
                .asString()
                .setCallback((e, accessToken) -> {
                    if (e == null) {
                        Log.d(TAG, "Access token: " + accessToken);
                        VoiceActivity.this.accessToken = accessToken;
                        registerForCallInvites();
                    } else {
                        Snackbar.make(coordinatorLayout,
                                "Error retrieving access token. Unable to make calls",
                                Snackbar.LENGTH_LONG).show();
                    }
                });*/

        AndroidNetworking.get( AppSession.baseApiUrl + "/call/token?android=1")
                .addHeaders("Accept", "application/json")
                .addHeaders("Authorization", "Bearer " + AppSession.bToken)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("log", "Successfully grabbed");
                        try {
                            AppSession.aToken = response.getString("token");
                            Log.d(TAG, "Access token: " + AppSession.aToken);
                            registerForCallInvites();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Log.d("error" ,"Something has happened");
                        Log.d("error", error.getErrorBody());
                        Snackbar.make(relativeLayout,
                                "Error retrieving access token. Unable to make calls",
                                Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private void LogOut()
    {
        Context context = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // leave listener pool and logout
        builder.setMessage("You will no longer be contactable as a listener until you log back in. Do you want to logout?");
        builder.setTitle("Logout");

        builder.setPositiveButton(Constants.YES, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id)
            {
                LeavePool();

                //Continue to destination, user accepted
                SharedPreferences prefs = context.getSharedPreferences("babblePrefs", context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                AppSession.bToken = "";
                editor.putString("bearerToken", AppSession.bToken);
                editor.putBoolean("verified", false);
                editor.apply();
                Intent myIntent = new Intent(getApplicationContext(), LandingActivity.class);
                startActivity(myIntent);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(Constants.NO, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });









        /*
        if(!AppSession.inPool)
        {
            builder.setMessage("You will no longer be contactable as a listener until you log back in. Do you want to logout?");
            builder.setTitle("Logout");

            builder.setPositiveButton(Constants.YES, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id)
                {
                    //Continue to destination, user accepted
                    SharedPreferences prefs = context.getSharedPreferences("babblePrefs", context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    AppSession.bToken = "";
                    editor.putString("bearerToken", AppSession.bToken);
                    editor.putBoolean("verified", false);
                    editor.apply();
                    Intent myIntent = new Intent(getApplicationContext(), LandingActivity.class);
                    startActivity(myIntent);
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton(Constants.NO, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
        else
        {
            builder.setMessage("You're currently in the listener pool, please leave before attempting to logout.");
            builder.setTitle("Currently In Pool");

            builder.setPositiveButton(Constants.OK, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
        }*/

        AlertDialog alert = builder.create();
        alert.show();
    }
}
