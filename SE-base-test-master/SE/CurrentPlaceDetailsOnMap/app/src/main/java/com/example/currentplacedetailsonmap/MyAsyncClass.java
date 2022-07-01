package com.example.currentplacedetailsonmap;

import android.location.Location;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

/**Fetching of data from LTA api, only fetches once to get the data at the start of the app, need to fetch this with timer to get new data every 1 minute
 * Stores data to carpark list*/
public class MyAsyncClass extends AsyncTask<String, Integer, JSONObject> {

    JSONObject json;
    @Override
    final protected JSONObject  doInBackground(String... urls) {

        URL url;
        boolean stopFetching = false;
        //LTA api returns only 500 responses, need to skip by 500 to get more
        for(int i = 0; i <Integer.MAX_VALUE ; i+=500) {
            if (stopFetching)
                break;
            else {
                try {
                    String urlString = urls[0].toString() + i;
                    System.out.println(urlString);

                    url = new URL(urlString);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                    urlConnection.setRequestProperty("Content-Type", "application/" + "json");
                    urlConnection.setRequestProperty("AccountKey", "/Ah8ZCPPSeq35ekWujH6Og==");

                    urlConnection.connect();

                    if (urlConnection.getResponseCode() != 200) {
                        throw new RuntimeException("Failed : HTTP error code : "
                                + urlConnection.getResponseCode());
                    }


                    BufferedReader responseBuffer = new BufferedReader(new InputStreamReader(
                            (urlConnection.getInputStream())));

                    String assembledOutput = "";

                    String output;
                    while ((output = responseBuffer.readLine()) != null) {
                        assembledOutput = assembledOutput + output;
                    }
                    try {
                        json = new JSONObject(assembledOutput);
                        JSONArray jArray = json.getJSONArray("value");
                        if (jArray == null || jArray.length() <= 0) {
                            stopFetching = true;
                            break;
                        }
                        try {
                            if (jArray != null && jArray.length() > 0) {
                                for (int a = 0; a < jArray.length(); a++) {
                                    JSONObject json_data = jArray.getJSONObject(a);
                                    if (json_data != null) {
                                        CarparkItem placeholderCarpark = new CarparkItem();
                                        placeholderCarpark.setCarparkID(json_data.getString("CarParkID"));
                                        placeholderCarpark.setCarparkArea(json_data.getString("Area"));
                                        placeholderCarpark.setCarparkDevelopment(json_data.getString("Development"));
                                        placeholderCarpark.setCarparkLocationString(json_data.getString("Location"));
                                        if (!placeholderCarpark.getCarparkLocationString().isEmpty()) {
                                            String[] splitLoc = placeholderCarpark.getCarparkLocationString().split(" ");
                                            Location loc = new Location("locationA");
                                            loc.setLatitude(Double.parseDouble(splitLoc[0]));
                                            loc.setLongitude(Double.parseDouble(splitLoc[1]));
                                            placeholderCarpark.setCarparkLocation(loc);
                                        }
                                        placeholderCarpark.setCarparkType(json_data.getString("LotType"));
                                            placeholderCarpark.setCarLots(json_data.getInt("AvailableLots"));
                                            if(placeholderCarpark.getCarparkType().equalsIgnoreCase("C"))
                                                placeholderCarpark.setCarLots(json_data.getInt("AvailableLots"));
                                        if(placeholderCarpark.getCarparkType().equalsIgnoreCase("Y"))
                                            placeholderCarpark.setMotorLots(json_data.getInt("AvailableLots"));
                                        if(placeholderCarpark.getCarparkType().equalsIgnoreCase("H"))
                                            placeholderCarpark.setHvLots(json_data.getInt("AvailableLots"));

                                        placeholderCarpark.setCarparkAgency(json_data.getString("Agency"));

                                        GlobalInstance.getInstance().carparkList.add(placeholderCarpark);
                                        GlobalInstance.getInstance().carparkList.get(i).setCp(placeholderCarpark.getCp());
                                    } else {
                                        urlConnection.disconnect();
                                    }
                                }
                            }

                        } catch (NullPointerException x) {
                            // TODO Auto-generated catch block
                            x.printStackTrace();
                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return json;
    }

    /**Reformat list to combine multiple carparks with same id that have different car types*/
    @Override
    protected void onPostExecute(JSONObject result) {
        for(int i =0; i <  GlobalInstance.getInstance().carparkList.size(); i++) {
            if (i < GlobalInstance.getInstance().carparkList.size() - 1) {
                {
                    if (GlobalInstance.getInstance().carparkList.get(i).getCarparkID().equalsIgnoreCase(GlobalInstance.getInstance().carparkList.get(i + 1).getCarparkID())) {
                        if (GlobalInstance.getInstance().carparkList.get(i + 1).getCarparkType() .equalsIgnoreCase("C")) //cars
                        {
                            GlobalInstance.getInstance().carparkList.get(i).setCarLots(GlobalInstance.getInstance().carparkList.get(i + 1).getCarparkLots());
                        }
                        if (GlobalInstance.getInstance().carparkList.get(i + 1).getCarparkType() .equalsIgnoreCase("Y")) //motors
                        {
                            GlobalInstance.getInstance().carparkList.get(i).setMotorLots(GlobalInstance.getInstance().carparkList.get(i + 1).getCarparkLots());
                        }
                        if (GlobalInstance.getInstance().carparkList.get(i + 1).getCarparkType().equalsIgnoreCase("H")) //heavy vecs
                        {
                            GlobalInstance.getInstance().carparkList.get(i).setHvLots(GlobalInstance.getInstance().carparkList.get(i + 1).getCarparkLots());

                        }
                        GlobalInstance.getInstance().carparkList.remove(GlobalInstance.getInstance().carparkList.get(i + 1));

                    }
                }
            }

        }
    }
}