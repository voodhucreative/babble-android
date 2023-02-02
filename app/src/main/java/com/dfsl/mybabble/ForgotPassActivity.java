package com.dfsl.mybabble;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.dfsl.mybabble.APIObjects.ResponseMeta;

import java.util.UUID;

public class ForgotPassActivity extends Activity
{

    int height;
    int width;
    int width10;

    TextView errors;

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
        SharedPreferences.Editor editor = prefs.edit();


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
        tmarign.setMargins(0,20,0,20);
        tmarign.addRule(RelativeLayout.CENTER_HORIZONTAL);

        // Creating a new TextView
        TextView tv = new TextView(this);
        tv.setText("Please enter email");
        tv.setTextSize(28);
        tv.setTextColor(Color.WHITE);
        tv.setGravity(Gravity.CENTER);
        tv.setLayoutParams(tmarign);
        tv.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_b));
        //tv.setWidth();

        errors = new TextView(this);
        //errors.setText("wrong password");
        errors.setTextSize(22);
        errors.setTextColor(Color.parseColor(BabbleColors.RED));
        errors.setGravity(Gravity.CENTER);

        EditText email = new EditText(this);
        email.setBackgroundResource(R.drawable.rounded_button);
        GradientDrawable field = (GradientDrawable) email.getBackground();
        field.setColor(Color.WHITE);
        email.setWidth(width); // force proper width for page?! - odd
        email.setHint("Email");
        email.setPadding(40,25,40,25);
        email.setLayoutParams(tmarign);
        email.setGravity(Gravity.CENTER);

        EditText pass = new EditText(this);
        pass.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        pass.setHint("Password");
        pass.setBackgroundResource(R.drawable.rounded_button);
        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        pass.setPadding(40,25,40,25);
        pass.setGravity(Gravity.CENTER);

        pass.addTextChangedListener(EntryTextSpacer.Watcher(pass));

        Button submit = new Button(this);
        submit.setBackgroundResource(R.drawable.rounded_button);
        submit.setTextColor(Color.WHITE);
        GradientDrawable btn = (GradientDrawable) submit.getBackground();
        btn.setColor(Color.parseColor(BabbleColors.DARK_ORANGE));
        submit.setText("Submit");
        submit.setLayoutParams(tmarign);
        submit.setTransformationMethod(null);
        submit.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_b));
        submit.setTextSize(16);

        Button register = new Button(this);
        register.setText("Forgot Password?");
        register.setBackgroundColor(Color.TRANSPARENT);
        register.setTextColor(Color.WHITE);
        register.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent myIntent = new Intent(getApplicationContext(), ForgotPassActivity.class);
                startActivity(myIntent);
            }
        });

        submit.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {

                AndroidNetworking.post(AppSession.baseApiUrl + "/reset-password/request")
                        .addBodyParameter("email", email.getText().toString())
                        .addHeaders("Accept", "application/json")
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
                                        String error = response.getJSONObject("errors").getJSONArray("email").getString(0);
                                        errors.setText(error);
                                        errors.invalidate();
                                    }
                                    catch (JSONException e)
                                    {
                                        response.getString("message");
                                        AppSession.forEmail = email.getText().toString();
                                        Intent myIntent = new Intent(getApplicationContext(), VerifyCodeActivity.class);
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
                                Log.d("error", error.getErrorDetail());
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
                                else
                                {
                                    errors.setText("Unknown error");
                                }

                                errors.invalidate();
                            }
                        });
            }
        });

        Button cancel = new Button(this);
        cancel.setBackgroundResource(R.drawable.rounded_button);
        cancel.setTextColor(Color.BLACK);
        GradientDrawable cbtn = (GradientDrawable) cancel.getBackground();
        cbtn.setColor(Color.parseColor(BabbleColors.WHITE));
        cancel.setText("Cancel");
        cancel.setTypeface(null, Typeface.BOLD);
        cancel.setLayoutParams(tmarign);
        cancel.setTransformationMethod(null);
        cancel.setTextSize(22);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        // Adding the TextView to the RelativeLayout as a child
        cont.addView(tv);
        cont.addView(errors);
        cont.addView(email);
        //cont.addView(pass);
        cont.addView(submit);
        cont.addView(cancel);

        //cont.addView(register);

        relativeLayout.addView(BackgroundManager.homeBackgroundView(this));
        cont.setPadding(width10, 0, width10, 0);
        relativeLayout.addView(cont);

        // Setting the RelativeLayout as our content view
        setContentView(relativeLayout, rlp);
    }


}
