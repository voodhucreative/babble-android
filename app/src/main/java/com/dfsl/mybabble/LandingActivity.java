package com.dfsl.mybabble;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

public class LandingActivity extends Activity
{

    Intent myIntent;
    RelativeLayout relativeLayout;
    Boolean isLoggedIn = false;

    int height;
    int width;

    int width10;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;

        width10 = Math.round(width * 0.10f);

        Context context = this;
        SharedPreferences prefs = context.getSharedPreferences("babblePrefs", context.MODE_PRIVATE);
        String bearer = prefs.getString("bearerToken", null);

        if(bearer != null && bearer != "")
        {
            CheckAutoLogin(bearer);
        }
        else
        {
            Log.d("login", "no previous login");
        }

        AndroidNetworking.initialize(getApplicationContext());
        // Creating a new RelativeLayout
        relativeLayout = new RelativeLayout(this);
        LinearLayout cont = new LinearLayout(this);
        relativeLayout.setBackgroundColor(Color.parseColor(BabbleColors.SLATE));
        // Defining the RelativeLayout layout parameters.
        // In this case I want to fill its parent
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        cont.setPadding(width10,0,width10,0);

        RelativeLayout.LayoutParams tmarign = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        tmarign.setMargins(0,40,0,40);
        tmarign.addRule(RelativeLayout.CENTER_HORIZONTAL);


        TextView welcome = new TextView(this);
        welcome.setGravity(Gravity.CENTER);
        welcome.setText("Welcome to");
        welcome.setTextSize(26);
        welcome.setTextColor(Color.WHITE);
        welcome.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_b));

        if(width > 1500){
            welcome.setTextSize(32);
        }

        ImageView babbleLogo = new ImageView(this);
        babbleLogo.setImageResource(R.drawable.mybabblelogo4);
        babbleLogo.setLayoutParams(tmarign);
        babbleLogo.setAdjustViewBounds(true);
        babbleLogo.setScaleType(ImageView.ScaleType.FIT_XY);

        Button signIn = new Button(this);

        signIn.setLayoutParams(new LinearLayout.LayoutParams(Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT));
        //signIn.setWidth(Constants.BUTTON_WIDTH);
        //signIn.setHeight(Constants.BUTTON_HEIGHT);


        signIn.setText("Login");
        signIn.setTransformationMethod(null);
        signIn.setTypeface(null, Typeface.BOLD);
        signIn.setTextColor(Color.WHITE);
        signIn.setBackgroundResource(R.drawable.rounded_button);
        signIn.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_b));
        signIn.setTextSize(24);

        GradientDrawable btn = (GradientDrawable) signIn.getBackground();
        btn.setColor(Color.parseColor(BabbleColors.DARK_ORANGE));

        signIn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                myIntent = new Intent(getApplicationContext(), LoginActivity.class);
                if(AppSettings.showTermsAndCondition)
                {
                    TOSPopup();
                }
                else
                {
                    startActivity(myIntent);
                }
            }

        });
        signIn.setLayoutParams(tmarign);

        if(width > 1500){
            signIn.setTextSize(26);
        }

        Button register = new Button(this);
        register.setText("New User?");
        register.setBackgroundColor(Color.TRANSPARENT);
        register.setTextColor(Color.WHITE);
        register.setTransformationMethod(null);
        register.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_l));
        register.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v)
                {
                    myIntent = new Intent(getApplicationContext(), RegisterActivity.class);
                    if(AppSettings.showTermsAndCondition)
                    {
                        TOSPopup();
                    }
                    else
                    {
                        startActivity(myIntent);
                    }
                }
        });

        if(width > 1500){
            register.setTextSize(20);
        }

        // Defining the layout parameters of the TextView
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);

        // Setting the parameters on the TextView
        cont.setLayoutParams(lp);
        cont.setOrientation(LinearLayout.VERTICAL);

        // Adding the TextView to the RelativeLayout as a child`
        cont.addView(welcome);
        cont.addView(babbleLogo);
        cont.addView(signIn);
        cont.addView(register);

        relativeLayout.addView(BackgroundManager.landingBackgroundView(this));
        relativeLayout.addView(cont);

        // Setting the RelativeLayout as our content view
        setContentView(relativeLayout, rlp);
    }

    public void TOSPopup()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("By Continuing in this app you accept our Terms of Service");
        builder.setTitle("Terms of Service");

        builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Continue to destination, user accepted
                startActivity(myIntent);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Close dialog they haven't agreed
                dialog.dismiss();
            }
        });

        builder.setNeutralButton("View Terms", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //send user to terms page
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://mybabble.chat"));
                startActivity(browserIntent);
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void CheckAutoLogin(String bT)
    {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("babblePrefs", getApplicationContext().MODE_PRIVATE);

        AndroidNetworking.get( AppSession.baseApiUrl + "/call/token?android=1")
                .addHeaders("Accept", "application/json")
                .addHeaders("Authorization", "Bearer " + bT)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("log", "Successfully grabbed");
                        try {
                            AppSession.aToken = response.getString("token");

                            AppSession.bToken = bT;
                            Log.d("log", "accesstoken: " + AppSession.aToken);
                            Log.d("log", "bearertoken: " + AppSession.bToken);

                            if(!prefs.getBoolean("verified", false))
                            {
                                Intent myIntent = new Intent(getApplicationContext(), VerifyAccountActivity.class);
//                                VerifyAccountActivity.SendVerificationEmail();
                                startActivity(myIntent);
                            }
                            else {
                                myIntent = new Intent(getApplicationContext(), CallActivity.class);
                                startActivity(myIntent);
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Log.d("error" ,"Something has happened");
                        Log.d("error", error.getErrorBody());
                    }
                });
    }
}
