package com.example.currentplacedetailsonmap;

import android.location.Location;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


/**To make sure that the distances are updated and sorted before showing in the map
And also google map can't show a lot of marker unless put inside another thread*/
public class UIAsync extends AsyncTask<String, String, String> {
    public double calculateDistance(Location userLocation, Location carparkLoction) {
        if (userLocation == null || carparkLoction == null)
            return 9999999;
        else {
            double distance = userLocation.distanceTo(carparkLoction);
            return distance;
        }
    }

    @Override
    protected String doInBackground(String... params) {
        // your code

            for (int i = 0; i < GlobalInstance.getInstance().carparkList.size(); i++) {

                if (GlobalInstance.getInstance().carparkList.get(i).getCarparkLocation() != null) {
                    GlobalInstance.getInstance().carparkList.get(i).setDistanceToUser(calculateDistance(GlobalInstance.getInstance().userLocation, GlobalInstance.getInstance().carparkList.get(i).getCarparkLocation()));
                }

            }
        return "";

    }

    /**Sorts list based on distance, add top x to nearest carparks list
     * Display markers*/
    @Override
    protected void onPostExecute(String result) {
        Collections.sort(GlobalInstance.getInstance().carparkList,
                Comparator.comparingDouble(CarparkItem::getDistanceToUser));
        int currentShownCount = 0;
        ArrayList<CarparkItem> part1 = new ArrayList<CarparkItem>(GlobalInstance.getInstance().carparkList.subList(0, GlobalInstance.getInstance().markersToShow));
        GlobalInstance.getInstance().nearestCarparkList = part1;


        for (int i = 0; currentShownCount < GlobalInstance.getInstance().markersToShow;i++) {

            if(GlobalInstance.getInstance().nearestCarparkList.get(i)!=null) {

                if (GlobalInstance.getInstance().mMap != null && GlobalInstance.getInstance().nearestCarparkList.get(i).getCarparkLocation() != null) {
                    GlobalInstance.getInstance().nearestCarparkList.get(i).setCarparkMarker(GlobalInstance.getInstance().mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(GlobalInstance.getInstance().nearestCarparkList.get(i).getCarparkLocation().getLatitude(), GlobalInstance.getInstance().nearestCarparkList.get(i).getCarparkLocation().getLongitude()))
                            .title(GlobalInstance.getInstance().nearestCarparkList.get(i).getCarparkDevelopment() + " " + GlobalInstance.getInstance().nearestCarparkList.get(i).getCarLots() + " " + GlobalInstance.getInstance().nearestCarparkList.get(i).getMotorLots() + " " + GlobalInstance.getInstance().nearestCarparkList.get(i).getHvLots())));
                    GlobalInstance.getInstance().nearestCarparkList.get(i).getCarparkMarker().setTag(GlobalInstance.getInstance().nearestCarparkList.get(i));
                }
                currentShownCount++;

            }
        }
    }


    @Override
    protected void onPreExecute() {
        //your code
    }


    @Override
    protected void onProgressUpdate(String... text) {
        // your code

    }
}