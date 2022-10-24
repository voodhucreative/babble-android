package com.dfsl.mybabble;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.inputmethodservice.Keyboard;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONException;
import org.json.JSONObject;

public class VerifyAccountActivity extends Activity
{

    int height;
    int width;
    TextView errors;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;
        Context context = this;


        AndroidNetworking.initialize(getApplicationContext());
        // Creating a new RelativeLayout
        RelativeLayout relativeLayout = new RelativeLayout(this);
        LinearLayout cont = new LinearLayout(this);
        relativeLayout.setBackgroundColor(Color.parseColor(BabbleColors.SLATE));

        // Defining the RelativeLayout layout parameters.
        // In this case I want to fill its parent
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT,
                RelativeLayout.LayoutParams.FILL_PARENT);

        RelativeLayout.LayoutParams tmarign = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        tmarign.setMargins(0,40,0,40);
        tmarign.addRule(RelativeLayout.CENTER_HORIZONTAL);

        // Creating a new TextView
        TextView tv = new TextView(this);
        tv.setText("Thanks! We’ve sent a verification code to your email address. Please enter it below to reset your password.");
        tv.setTextSize(28);
        tv.setTextColor(Color.WHITE);
        tv.setGravity(Gravity.LEFT);
        tv.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_b));
        //tv.setWidth();

        if (width <= 800)
        {
            tv.setPadding(0, 20, 0, 0);
            tv.setTextSize(24);
        }

        errors = new TextView(this);
        //errors.setText("wrong password");
        errors.setTextSize(22);
        errors.setTextColor(Color.parseColor(BabbleColors.RED));
        errors.setGravity(Gravity.CENTER);

        EditText code = new EditText(this);
        code.setBackgroundResource(R.drawable.rounded_button);
        GradientDrawable field = (GradientDrawable) code.getBackground();
        field.setColor(Color.WHITE);
        code.setHint("Code");
        code.setPadding(40,25,40,25);
        code.setLayoutParams(tmarign);
        code.setGravity(Gravity.CENTER);
        code.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        code.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_r));

        Button submit = new Button(this);
        submit.setBackgroundResource(R.drawable.rounded_button);
        submit.setTextColor(Color.WHITE);
        GradientDrawable btn = (GradientDrawable) submit.getBackground();
        btn.setColor(Color.parseColor(BabbleColors.DARK_ORANGE));
        submit.setText("Submit");
        submit.setLayoutParams(tmarign);
        submit.setTransformationMethod(null);
        submit.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_b));
        submit.setTextSize(24);
        submit.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {

                AndroidNetworking.post(AppSession.baseApiUrl + "/verify-email/verification")
                        .addBodyParameter("verification_code", code.getText().toString())
                        .addHeaders("Accept", "application/json")
                        .addHeaders("Authorization", "Bearer " + AppSession.bToken)
                        .setPriority(Priority.MEDIUM)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // do anything with response
                                Log.d("log", "Success");
                                try {
                                    try
                                    {
                                        String error = response.getJSONObject("errors").getJSONArray("verification_code").getString(0);
                                        errors.setText(error);
                                        errors.invalidate();
                                    }
                                    catch (JSONException e)
                                    {
                                        response.getString("message");
                                        AppSession.forCode = code.getText().toString();

                                        verifyEmail();
                                        return;
                                    }


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    errors.setText("Unknown Error");
                                    errors.invalidate();
                                    return;
                                }
                            }
                            @Override
                            public void onError(ANError error) {
                                // handle error
                                Log.d("error" ,"Something has happened");
                                Log.d("error", error.getErrorBody());
                                NError er = error.getErrorAsObject(NError.class);
                                SubError ner = er.errors;

                                if(ner.email != null)
                                {
                                    errors.setText(ner.email[0]);
                                }
                                else if(ner.password != null)
                                {
                                    errors.setText(ner.password[0]);
                                }
                                else if(ner.username != null)
                                {
                                    errors.setText(ner.username[0]);
                                }
                                else if(ner.verification_code != null)
                                {
                                    errors.setText(ner.verification_code[0]);
                                }
                                else
                                {
                                    errors.setText("Unknown error");
                                }

                                errors.invalidate();
                            }
                        });
            }
        });

        Button logout = new Button(this);
        logout.setBackgroundResource(R.drawable.rounded_button);
        logout.setTextColor(Color.RED);
        GradientDrawable logoutBtn = (GradientDrawable) logout.getBackground();
        logoutBtn.setColor(Color.parseColor(BabbleColors.WHITE));
        logout.setText("Logout");
        logout.setWidth((int) Math.round(width * 0.9));
        logout.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_b));
        logout.setTextSize(22);
        logout.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                SharedPreferences prefs = context.getSharedPreferences("babblePrefs", context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                AppSession.bToken = "";
                editor.putString("bearerToken", AppSession.bToken);
                editor.putBoolean("verified", false);
                editor.apply();
                Intent myIntent = new Intent(getApplicationContext(), LandingActivity.class);
                startActivity(myIntent);
            }
        });

        // Defining the layout parameters of the TextView
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);

        // Setting the parameters on the TextView
        cont.setLayoutParams(lp);
        cont.setOrientation(LinearLayout.VERTICAL);
        cont.setPadding((int) Math.round(displayMetrics.widthPixels * 0.1),0,(int) Math.round(displayMetrics.widthPixels * 0.1),0);

        //Quick add spacer since padding does not work :(
        TextView spacer = new TextView(this);
        spacer.setText("space");
        spacer.setTextSize(10);
        spacer.setTextColor(Color.TRANSPARENT);
        spacer.setGravity(Gravity.CENTER);

        // Adding the TextView to the RelativeLayout as a child
        cont.addView(tv);
        cont.addView(errors);
        cont.addView(code);
        cont.addView(submit);
        cont.addView(spacer);
        cont.addView(logout);

        relativeLayout.addView(BackgroundManager.homeBackgroundView(this));
        relativeLayout.addView(cont);

        SendVerificationEmail();

        // Setting the RelativeLayout as our content view
        setContentView(relativeLayout, rlp);
    }

    public void verifyEmail()
    {
        AndroidNetworking.post(AppSession.baseApiUrl + "/verify-email")
                .addBodyParameter("verification_code", AppSession.forCode)
                .addHeaders("Accept", "application/json")
                .addHeaders("Authorization", "Bearer " + AppSession.bToken)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("log", "Success");
                        try {
                            try
                            {
                                String error = response.getJSONObject("errors").getJSONArray("verification_code").getString(0);
                                errors.setText(error);
                                errors.invalidate();
                            }
                            catch (JSONException e)
                            {
                                response.getString("message");
                                AppSession.forCode = "";
                                Intent myIntent = new Intent(getApplicationContext(), CallActivity.class);
                                startActivity(myIntent);
                                return;
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                            errors.setText("Unknown Error");
                            errors.invalidate();
                            return;
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Log.d("error" ,"Something has happened");
                        Log.d("error", error.getErrorBody());
                        NError er = error.getErrorAsObject(NError.class);
                        SubError ner = er.errors;

                        if(ner.email != null)
                        {
                            errors.setText(ner.email[0]);
                        }
                        else if(ner.password != null)
                        {
                            errors.setText(ner.password[0]);
                        }
                        else if(ner.username != null)
                        {
                            errors.setText(ner.username[0]);
                        }
                        else if(ner.verification_code != null)
                        {
                            errors.setText(ner.verification_code[0]);
                        }
                        else
                        {
                            errors.setText("Unknown error");
                        }

                        errors.invalidate();
                    }
                });
    }

    public static void SendVerificationEmail(){

        AndroidNetworking.post(AppSession.baseApiUrl + "/verify-email/resend")
                .addBodyParameter("verification_code", AppSession.forCode)
                .addHeaders("Accept", "application/json")
                .addHeaders("Authorization", "Bearer " + AppSession.bToken)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                            try
                            {
                                String error = response.getJSONObject("errors").getJSONArray("verification_code").getString(0);
                            }
                            catch (JSONException e)
                            {

                                return;
                            }
                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Log.d("error" ,"Something has happened");
                        Log.d("error", error.getErrorBody());
                        NError er = error.getErrorAsObject(NError.class);
                        SubError ner = er.errors;

                    }
                });
    }

}
