package com.example.currentplacedetailsonmap;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**View Adapter for the scrolling list of nearest carparks - can also be used for showing list of searched carparks(if needed)*/
public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    private ArrayList<CarparkItem> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private final int limit = 10;
    private long startClickTime;
    private GestureDetector mGestureDetector;
    // data is passed into the constructor
    MyRecyclerViewAdapter(Context context, ArrayList<CarparkItem> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.carpark_name, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(mData.size()>0) {
            CarparkItem object = mData.get(position);
            if (object != null) {
                holder.carparkAddress.setText(object.getCarparkDevelopment());


                holder.carparkDistance.setText(Double.toString(roundTwoDecimals(object.getDistanceToUser())));
            }
        }
    }


    double roundTwoDecimals(double d)
    {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
    }
    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void update(ArrayList<CarparkItem> datas){

        mData.clear();
        GlobalInstance.getInstance().recyclerView.getRecycledViewPool().clear();
        mData.addAll(datas);
        notifyDataSetChanged();
    }
    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnTouchListener {
        TextView carparkAddress,carparkDistance;

        ViewHolder(View itemView) {
            super(itemView);

            carparkAddress = (TextView) itemView.findViewById(R.id.carparkAddress);
            carparkDistance = (TextView) itemView.findViewById(R.id.carparkDistance);

            itemView.setOnClickListener(this);
            itemView.setOnTouchListener(this);


        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                startClickTime = System.currentTimeMillis();
                //v.setBackgroundColor(Color.TRANSPARENT);


            } else if (event.getAction() == MotionEvent.ACTION_UP) {

                if (System.currentTimeMillis() - startClickTime < ViewConfiguration.getTapTimeout()) {



                } else {
                   // v.setBackgroundColor(Color.parseColor("#f0f0f0"));
                    // Touch was a simple tap. Do whatever.
                    int position = getLayoutPosition();
                    GlobalInstance.getInstance().currentClickedInList = mData.get(getLayoutPosition());
                    if(mData.get(position).getCarparkLocation()!=null) {
                        LatLng latlng = new LatLng(mData.get(position).getCarparkLocation().getLatitude(), mData.get(position).getCarparkLocation().getLongitude());
                        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latlng, 15);
                        mData.get(position).getCarparkMarker().showInfoWindow();
                        GlobalInstance.getInstance().mMap.animateCamera(update);
                        // Touch was a not a simple tap.
                    }

                }

            }

            return true;

        }

        @Override
        public void onClick(View view) {
            int position = getLayoutPosition();
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {

        return mData.get(id).getCarparkID();
    }

    /**Allows clicks events to be caught*/
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
