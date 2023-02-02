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

public class ResetPassActivity extends Activity
{

    int height;
    int width;
    TextView errors;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        tv.setText("Please enter a new password");
        tv.setTextSize(28);
        tv.setTextColor(Color.WHITE);
        tv.setGravity(Gravity.CENTER);
        tv.setLayoutParams(tmarign);
        tv.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_b));
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

        EditText pass = new EditText(this);
        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        pass.setBackgroundResource(R.drawable.rounded_button);
        GradientDrawable field = (GradientDrawable) pass.getBackground();
        field.setColor(Color.WHITE);
        pass.setHint("Password");
        pass.setLayoutParams(tmarign);
        pass.setGravity(Gravity.CENTER);
        pass.setPadding(40,25,40,25);
        pass.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_r));

        pass.addTextChangedListener(EntryTextSpacer.Watcher(pass));

        EditText pass2 = new EditText(this);
        pass2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        pass2.setBackgroundResource(R.drawable.rounded_button);
        GradientDrawable conPass = (GradientDrawable) pass2.getBackground();
        conPass.setColor(Color.WHITE);
        pass2.setHint("Confirm Password");
        pass2.setGravity(Gravity.CENTER);
        pass2.setPadding(40,25,40,25);
        pass2.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_r));

        pass2.addTextChangedListener(EntryTextSpacer.Watcher(pass2));

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

        submit.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {

                AndroidNetworking.post(AppSession.baseApiUrl + "/reset-password")
                        .addBodyParameter("email", AppSession.forEmail)
                        .addBodyParameter("verification_code", AppSession.forCode)
                        .addBodyParameter("password", pass.getText().toString())
                        .addBodyParameter("password_confirmation", pass2.getText().toString())
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
                                        String error = response.getJSONObject("errors").getJSONArray("password").getString(0);
                                        errors.setText(error);
                                        errors.invalidate();
                                    }
                                    catch (JSONException e)
                                    {
                                        response.getString("message");
                                        AppSession.forCode = "";
                                        AppSession.forEmail = "";
                                        Intent myIntent = new Intent(getApplicationContext(), LandingActivity.class);
                                        startActivity(myIntent);
                                        ResetPopup();
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
        cont.addView(pass);
        cont.addView(pass2);
        cont.addView(submit);
        cont.addView(cancel);

        relativeLayout.addView(BackgroundManager.homeBackgroundView(this));
        relativeLayout.addView(cont);

        // Setting the RelativeLayout as our content view
        setContentView(relativeLayout, rlp);
    }

    public void ResetPopup()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Your password has been successfully changed");
        builder.setTitle("Password Reset");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Continue to destination, user accepted
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }
}
