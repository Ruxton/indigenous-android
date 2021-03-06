package com.indieweb.indigenous.util;

import android.accounts.AccountManager;
import android.content.Context;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.indieweb.indigenous.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SyndicationTargets {

    private final Context context;
    private final User user;

    public SyndicationTargets(Context context, User user) {
        this.context = context;
        this.user = user;
    }

    /**
     * Refresh syndication targets.
     */
    public void refresh() {

        String microPubEndpoint = user.getMicropubEndpoint();

        // Some endpoints already contain GET params. Instead of overriding the getParams method, we
        // just check it here.
        if (microPubEndpoint.contains("?")) {
            microPubEndpoint += "&q=syndicate-to";
        }
        else {
            microPubEndpoint += "?q=syndicate-to";
        }

        StringRequest getRequest = new StringRequest(Request.Method.GET, microPubEndpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject micropubResponse = new JSONObject(response);
                            JSONArray itemList = micropubResponse.getJSONArray("syndicate-to");
                            if (itemList.length() > 0) {
                                AccountManager am = AccountManager.get(context);
                                am.setUserData(user.getAccount(), "syndication_targets", response);
                                Toast.makeText(context, "Syndication targets saved", Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(context, "No syndication targets found", Toast.LENGTH_LONG).show();
                            }

                        }
                        catch (JSONException e) {
                            Toast.makeText(context, "Error getting syndication targets: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {
                            NetworkResponse networkResponse = error.networkResponse;
                            if (networkResponse != null && networkResponse.statusCode != 0 && networkResponse.data != null) {
                                Integer code = networkResponse.statusCode;
                                String result = new String(networkResponse.data);
                                Toast.makeText(context, "Error getting syndication targets. Status code: " + code + "; message: " + result, Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(context, "Error getting syndication targets: " + error.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                        catch (Exception e) {
                            Toast.makeText(context, "Error getting syndication targets " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
        )
        {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + user.getAccessToken());
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(getRequest);

    }

}
