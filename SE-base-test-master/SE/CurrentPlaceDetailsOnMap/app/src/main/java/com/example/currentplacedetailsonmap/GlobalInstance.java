package com.example.currentplacedetailsonmap;

import android.location.Location;
import android.support.v7.widget.RecyclerView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

/**Basically a class to share the items in here globally so all classes can access*/
public class GlobalInstance {

    private static final GlobalInstance instance = new GlobalInstance();
    ArrayList<CarparkItem> nearestCarparkList = new ArrayList<CarparkItem>();
    MyRecyclerViewAdapter adapter;
    RecyclerView recyclerView;
    ArrayList<CarparkItem> carparkList = new ArrayList<CarparkItem>();
    GoogleMap mMap;
    int metresToRefresh=300;
    int markersToShow=10;
    boolean onFirstMarkerFetch=false;
    boolean cameraFollowUser=true;
    CarparkItem selectedCarparkItem;
    CarparkItem currentClickedInList;
    Location userLocation = new Location("locationA");
    //private constructor to avoid client applications to use constructor
    private GlobalInstance(){}

    public static GlobalInstance getInstance(){
        return instance;
    }
}
