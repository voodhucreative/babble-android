package com.dfsl.mybabble;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.ANResponse;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class APICalls {

    static void reportCall(int code)
    {
        if(AppSession.lastCallSID != "")
        {
            AndroidNetworking.post(AppSession.baseApiUrl + "/issue")
                    .addBodyParameter("call_sid", AppSession.lastCallSID)
                    .addBodyParameter("type", String.valueOf(code))
                    .addHeaders("Accept", "application/json")
                    .addHeaders("Authorization", "Bearer " + AppSession.bToken)
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // do anything with response
                            Log.d("log", "Success");
                            Log.d("response", response.toString());
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

    static void rateCall(int rating)
    {
        if(AppSession.lastCallSID != "")
        {
            AndroidNetworking.post(AppSession.baseApiUrl + "/rating")
                    .addBodyParameter("call_sid", AppSession.lastCallSID)
                    .addBodyParameter("rating", String.valueOf(rating))
                    .addHeaders("Accept", "application/json")
                    .addHeaders("Authorization", "Bearer " + AppSession.bToken)
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // do anything with response
                            Log.d("log", "Success");
                            Log.d("response", response.toString());
                        }
                        @Override
                        public void onError(ANError error) {
                            // handle error
                            Log.d("error" ,"Something has happened");
                            Log.d("error", error.getErrorBody());
                        }
                    });
        }
        else
        {
            Log.d("debug", "no valid callSID");
        }
    }

    static void getPoolCount()
    {
        /*Log.d("debug", "URL:  " + AppSession.baseApiUrl + "/pool/count");
        ANRequest request = AndroidNetworking.get(AppSession.baseApiUrl + "/pool/count")
                .addHeaders("Accept", "application/json")
                .addHeaders("Authorization", "Bearer " + AppSession.bToken)
                .build();

        ANResponse<JSONObject> response = request.executeForJSONObject();

        if (response.isSuccess()) {
            JSONObject jsonObject = response.getResult();
            Log.d("debug", "counted pool");
        } else {
            ANError error = response.getError();
            Log.d("debug", "failed to count pool: " + error.getErrorCode());
        }*/

        AndroidNetworking.get( AppSession.baseApiUrl + "/pool/count")
                .addHeaders("Accept", "application/json")
                .addHeaders("Authorization", "Bearer " + AppSession.bToken)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        try {
                            AppSession.poolCount = response.getInt("pool_count");
                            Log.d("log", "Success! Pool Count: " + AppSession.poolCount);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            AppSession.poolCount = 0;
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Log.d("error" ,"Something has happened for pool count");
                        Log.d("error", error.getErrorBody());
                        AppSession.poolCount = 0;
                    }
                });
        Log.d("log", "returning poolCount");
    }

    static void joinPool()
    {
        AndroidNetworking.post(AppSession.baseApiUrl + "/pool/enter")
                .addHeaders("Accept", "application/json")
                .addHeaders("Authorization", "Bearer " + AppSession.bToken)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("debug", "entered pool");

                        }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Log.d("debug", "failed to enter pool" + error.getErrorDetail());
                        AppSession.inPool = false;
                        }
                });

        /*ANResponse response = request.executeForOkHttpResponse();

        Log.d("debug", response.getResult().toString());

        if (response.isSuccess()) {
            //JSONObject jsonObject = response.getResult();
                Log.d("debug", "entered pool");
        } else {
            ANError error = response.getError();
            Log.d("debug", "failed to enter pool" + error.getErrorDetail());
        }*/
    }

    static void leavePool()
    {
        AndroidNetworking.post(AppSession.baseApiUrl + "/pool/leave")
                .addHeaders("Accept", "application/json")
                .addHeaders("Authorization", "Bearer " + AppSession.bToken)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("debug", "left pool");
                        AppSession.inPool = false;
                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Log.d("debug", "failed to leave pool" + error.getErrorDetail());
                    }
                });

        /*ANResponse<JSONObject> response = request.executeForJSONObject();

        Log.d("debug", response.getResult().toString());

        if (response.isSuccess()) {
            //JSONObject jsonObject = response.getResult();
            Log.d("debug", "left pool");
        } else {
            ANError error = response.getError();
            Log.d("debug", "failed to leave pool" + error.getErrorDetail());
        }*/
    }

    static void getPoolStatus()
    {
        AndroidNetworking.get(AppSession.baseApiUrl + "/pool/status")
                .addHeaders("Accept", "application/json")
                .addHeaders("Authorization", "Bearer " + AppSession.bToken)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                     @Override
                     public void onResponse(JSONObject response) {
                         try {
                             Log.d("debug", "got pool status");
                             AppSession.inPool = response.getBoolean("open");
                         } catch (JSONException e) {
                             Log.d("debug", "didn't get pool status");
                         }
                     }

                     @Override
                     public void onError(ANError error) {
                         // handle error
                     }
                 });

        /*ANResponse<JSONObject> response = request.executeForJSONObject();

        Log.d("debug", response.getResult().toString());

        if (response.isSuccess()) {
            JSONObject jsonObject = response.getResult();
            try {
                Log.d("debug", "got pool status");
                return jsonObject.getBoolean("open");
            } catch (JSONException e) {
                Log.d("debug", "didn't get pool status");
                return false;
            }
        } else {
            ANError error = response.getError();
            Log.d("debug", "didn't get pool status");
            return false;
        }*/
    }

    static void getUserUse(Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences("babblePrefs", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String lastCost = prefs.getString("lastCost", null);

        AndroidNetworking.get(AppSession.baseApiUrl + "/feedback/usage")
                .addHeaders("Accept", "application/json")
                .addHeaders("Authorization", "Bearer " + AppSession.bToken)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                @Override
                public void onResponse(JSONObject response)
                {
                        try {
                            Log.d("debug", "got usage");
                            String cost = response.getJSONObject("data").getString("cost");

                            Log.d("debug", cost + " | " + lastCost);
                            if(!cost.equals(lastCost))
                            {
                                editor.putString("lastCost", cost);
                                editor.apply();
                                String msg = response.getString("message");
                                if(AppSettings.showBetaFeedback)
                                {
                                    AlertDialog fb = feedbackDialog(msg, cost, context);
                                    fb.show();
                                }
                            }
                            else
                            {
                                Log.d("debug", "don't show");
                            }

                        } catch (JSONException e) {
                            Log.d("debug", "didn't get usage");
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Log.d("debug", "didn't get usage");
                    }
                });
    }

    static AlertDialog feedbackDialog(String message, String cost, Context context)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage("Based on your usage, when this app leaves beta \n\n" + message + "\n\n do you feel this is worth it?");
        builder.setTitle("Beta Survey");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Continue to destination, user accepted
                giveFeedback(cost, 1);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Close dialog they haven't agreed
                giveFeedback(cost, 0);
                dialog.dismiss();
            }
        });

        return builder.create();
    }

    static void giveFeedback(String price, int worth)
    {

        AndroidNetworking.post(AppSession.baseApiUrl + "/feedback")
                .addHeaders("Accept", "application/json")
                .addHeaders("Authorization", "Bearer " + AppSession.bToken)
                .addBodyParameter("price", price)
                .addBodyParameter("worth", String.valueOf(worth))
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                            Log.d("debug", "successfully posted feedback");

                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Log.d("debug", "didn't post feedback");
                        Log.d("debug", error.getErrorBody());
                    }
                });
    }
}
