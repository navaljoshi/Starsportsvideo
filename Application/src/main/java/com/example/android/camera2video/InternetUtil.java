package com.example.android.camera2video;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by vaibhav on 1/4/17.
 */

public class InternetUtil {
    //MongoLab Constants
    private static final String MONGO_KEY = "veTqID_gkb74tG-yL4MGcS1p2RRBP1Pf";
    public static final String MOGO_URL = "https://api.mongolab.com/api/1/databases/geolocation/collections/onlinedb?apiKey=" + MONGO_KEY;

    public void getSpeed(AsyncResponse asyncResponse) {
        new GetDataAsyncTask(asyncResponse).execute();
    }

    private class GetDataAsyncTask extends AsyncTask<Void, Void, String> {
        private AsyncResponse asyncResponse;
        private StringBuffer response = new StringBuffer();
        long startTime, endTime, takenTime;

        public GetDataAsyncTask(AsyncResponse asyncResponse) {
            this.asyncResponse = asyncResponse;
        }

        @Override
        protected String doInBackground(Void... jsonObjects) {
            HttpURLConnection connection = null;
            startTime = System.currentTimeMillis();
            try {
                URL obj = new URL(MOGO_URL);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                // optional default is GET
                con.setRequestMethod("GET");
                //add request header
                //con.setRequestProperty("User-Agent", USER_AGENT);
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "application/json");
                int responseCode = con.getResponseCode();
                //System.out.println("\nSending 'GET' request to URL : " + Constants.MOGO_URL);
                System.out.println("Response Code : " + responseCode);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                endTime = System.currentTimeMillis();
                in.close();
                //print result
                System.out.println(response.toString());
            } catch (Exception exception) {
                //Log.e(LOG_TAG, exception.toString());
                return null;
            }
            return response.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            long dataSize = s.length() / 1024;
            takenTime = endTime - startTime;
            long ss = takenTime / 1000;
            double speed = dataSize / ss;
            asyncResponse.getSpeed(takenTime);
        }
    }

    ;
}
