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
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.transition.AutoTransition;
import android.transition.ChangeBounds;
import android.transition.ChangeTransform;
import android.transition.Transition;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends Activity
{

    int height;
    int width;
    int width10;
    TextView errors;

    String err = "";
    boolean errorReturned=false;

    SharedPreferences.Editor editor;

    EditText firstName;
    EditText lastName;
    EditText email;
    EditText phoneNumber;
    EditText pass;
    EditText passCon;

    boolean termsAgreed = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = this;
        SharedPreferences prefs = context.getSharedPreferences("babblePrefs", context.MODE_PRIVATE);
         editor = prefs.edit();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;

        width10 = Math.round(width * 0.10f);

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
        tmarign.setMargins(0,24,0,24);
        tmarign.addRule(RelativeLayout.CENTER_HORIZONTAL);

        // Creating a new TextView
        TextView tv = new TextView(this);
        tv.setText("Please enter your details");
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
        errors.setText("\n");
        errors.setTextSize(22);
        errors.setTextColor(Color.parseColor(BabbleColors.RED));
        errors.setGravity(Gravity.CENTER);

        email = new EditText(this);
        email.setBackgroundResource(R.drawable.rounded_button);
        GradientDrawable field = (GradientDrawable) email.getBackground();
        field.setColor(Color.WHITE);
        email.setHint("Email");
        email.setGravity(Gravity.CENTER);
        email.setPadding(40,25,40,25);
        email.setLayoutParams(tmarign);
        email.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_r));

        /*EditText user = new EditText(this);
        user.setHint("Username");
        user.setBackgroundResource(R.drawable.rounded_button);
        user.setPadding(40,25,40,25);*/

        pass = new EditText(this);
        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        pass.setHint("Password");
        pass.setGravity(Gravity.CENTER);
        pass.setBackgroundResource(R.drawable.rounded_button);
        pass.setPadding(40,25,40,25);
        pass.setLayoutParams(tmarign);
        pass.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_r));
        pass.addTextChangedListener(EntryTextSpacer.Watcher(pass));

        passCon = new EditText(this);
        passCon.setHint("Password Confirmation");
        passCon.setGravity(Gravity.CENTER);
        passCon.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passCon.setBackgroundResource(R.drawable.rounded_button);
        passCon.setPadding(40,25,40,25);
        passCon.setLayoutParams(tmarign);
        passCon.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_r));
        passCon.addTextChangedListener(EntryTextSpacer.Watcher(passCon));


        firstName = new EditText(this);
        firstName.setHint("First name");
        firstName.setGravity(Gravity.CENTER);
        firstName.setInputType(InputType.TYPE_CLASS_TEXT);
        firstName.setBackgroundResource(R.drawable.rounded_button);
        firstName.setPadding(40,25,40,25);
        firstName.setLayoutParams(tmarign);
        firstName.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_r));

        lastName = new EditText(this);
        lastName.setHint("Last name");
        lastName.setGravity(Gravity.CENTER);
        lastName.setInputType(InputType.TYPE_CLASS_TEXT);
        lastName.setBackgroundResource(R.drawable.rounded_button);
        lastName.setPadding(40,25,40,25);
        lastName.setLayoutParams(tmarign);
        lastName.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_r));

        phoneNumber = new EditText(this);
        phoneNumber.setHint("Phone number");
        phoneNumber.setGravity(Gravity.CENTER);
        phoneNumber.setInputType(InputType.TYPE_CLASS_PHONE);
        phoneNumber.setBackgroundResource(R.drawable.rounded_button);
        phoneNumber.setPadding(40,25,40,25);
        phoneNumber.setLayoutParams(tmarign);
        phoneNumber.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_r));





        Button submit = new Button(this);
        submit.setBackgroundResource(R.drawable.rounded_button);
        submit.setTextColor(Color.WHITE);
        GradientDrawable btn = (GradientDrawable) submit.getBackground();
        btn.setColor(Color.parseColor(BabbleColors.DARK_ORANGE));
        submit.setText("Sign up");
        submit.setLayoutParams(tmarign);
        submit.setTransformationMethod(null);
        submit.setTypeface(null, Typeface.BOLD);
        submit.setTextSize(16);
        submit.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_b));

        submit.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if(termsAgreed)
                {
                        Register();
                }
                else{
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    alertDialogBuilder.setTitle("Terms and Conditions");
                    alertDialogBuilder.setMessage("Please read and agree to our terms and conditions before you use this app");
                    alertDialogBuilder.setPositiveButton("T’s & C’s", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            OpenTerms();
//                           PopupWindow window =  new PopupWindow(TermsAndConditions(), width, (int)(height * 0.95), true);
//                           window.setEnterTransition(new ChangeTransform());
//
//                           window.showAtLocation(cont, Gravity.BOTTOM,0,0);
                        }
                    });
                    alertDialogBuilder.setNegativeButton("Cancel", null);
                    alertDialogBuilder.setCancelable(true);
                    alertDialogBuilder.show();
                }
            }
        });

        // Defining the layout parameters of the TextView
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);

        // Setting the parameters on the TextView
        cont.setLayoutParams(lp);
        cont.setOrientation(LinearLayout.VERTICAL);

        // Adding the TextView to the RelativeLayout as a child
        cont.addView(tv);
        cont.addView(errors);
        cont.addView(firstName);
        cont.addView(lastName);

        cont.addView(email);
        cont.addView(phoneNumber);

        cont.addView(pass);
        cont.addView(passCon);
        cont.addView(submit);

        cont.setPadding(width10,0,width10,0);

        relativeLayout.addView(BackgroundManager.homeBackgroundView(this));
        relativeLayout.addView(cont);

        // Setting the RelativeLayout as our content view
        setContentView(relativeLayout, rlp);
    }

    private View TermsAndConditions()
    {
        RelativeLayout relLayout = new RelativeLayout(this);
        LinearLayout cont = new LinearLayout(this);
        relLayout.setBackgroundColor(Color.parseColor(BabbleColors.SLATE));

        RelativeLayout.LayoutParams tmarign = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        tmarign.setMargins(20,20,20,0);
        tmarign.addRule(RelativeLayout.ALIGN_BOTTOM);
        tmarign.addRule(RelativeLayout.CENTER_HORIZONTAL);

        RelativeLayout.LayoutParams wvHeight = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (int)(height * 0.75));
        wvHeight.addRule(RelativeLayout.ALIGN_TOP);
        wvHeight.addRule(RelativeLayout.CENTER_HORIZONTAL);

        WebView wv = new WebView(this);
        //Prevents user from navigating in view
        wv.setWebViewClient(new WebViewClient(){
            @Override //for APIs 24 and later
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request){
                return true;
            }
            @Override //for APIs earlier than 24
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                return true;
            }
        });
        wv.loadUrl("https://www.mybabble.chat/terms-and-conditions");
        wv.setForegroundGravity(Gravity.TOP);
        wv.setLayoutParams(wvHeight);

        TextView tv = new TextView(this);
        tv.setText("do you accept these terms?");
        tv.setTextSize(16);
        tv.setTextColor(Color.WHITE);
        tv.setGravity(Gravity.CENTER);
        tv.setLayoutParams(tmarign);
        tv.setTypeface(Fonts.SetTypeFace(this, R.font.cera_pro_b));

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_TOP);

        // Setting the parameters on the TextView
        cont.setLayoutParams(lp);
        cont.setOrientation(LinearLayout.VERTICAL);

        cont.addView(wv);
        cont.addView(tv);

        relLayout.addView(cont);

        return relLayout;
    }

    private void OpenTerms()
    {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.mybabble.chat/terms-and-conditions")));

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Terms and Conditions");
        alertDialogBuilder.setMessage("Do you agree to the terms?");
        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                termsAgreed = true;
                Register();
            }
        });
        alertDialogBuilder.setNegativeButton("No", null);
        alertDialogBuilder.setCancelable(true);
        alertDialogBuilder.show();
    }

    private void Register()
    {
        if (isNetworkConnected()) {

        AndroidNetworking.post(AppSession.baseApiUrl + "/register")
                .addBodyParameter("email", email.getText().toString())
                .addBodyParameter("username", email.getText().toString())//user.getText().toString())
                .addBodyParameter("password", pass.getText().toString())
                .addBodyParameter("password_confirmation", passCon.getText().toString())
                .addBodyParameter("device_name", "ClickTestDevice")
                .addHeaders("Accept", "application/json")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("log", "Success");
                        try {
                            AppSession.bToken = response.getJSONObject("meta").getString("token");
                            editor.putString("bearerToken", AppSession.bToken);
                            editor.apply();

                            errorReturned = false;

                            Intent myIntent = new Intent(getApplicationContext(), CallActivity.class);
//                                    VerifyAccountActivity.SendVerificationEmail();
                            editor.putBoolean("verified", true);
                            editor.apply();
                            startActivity(myIntent);
                        } catch (JSONException e) {
                            Log.d("error", "Failed to store token and start CallActivity");
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error

                        Log.d("error", "Something has happened");
                        Log.d("error", error.getErrorBody());

                        NError er = error.getErrorAsObject(NError.class);
                        SubError ner = er.errors;

                        errorReturned = true;

                        if (ner.email != null) {
                            err = ner.email[0];
                        } else if (ner.password != null) {
                            err = ner.password[0];
                        } else if (ner.username != null) {
                            err = ner.username[0];
                        } else {
                            err = "Unknown error";
                        }

                    }
                });
        if(err.length() > 0) {
            errors.setText(err);
        }
        else if (errorReturned){
            errors.setText("Something has gone wrong!");
        }
        } else {
            errors.setText("Please check your connection");
            errors.invalidate();
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}
