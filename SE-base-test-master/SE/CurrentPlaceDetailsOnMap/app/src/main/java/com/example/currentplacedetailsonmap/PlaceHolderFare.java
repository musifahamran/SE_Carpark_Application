package com.example.currentplacedetailsonmap;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class PlaceHolderFare extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener, AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener {
        InputStream inputStream;
        EditText chooseTime, chooseTime2;
        TextView startTime, endTime, day_Now, endDay, finalCost;
        TimePickerDialog timePickerDialog;
        String agency;
        String[] data;
        String amPm;
        Calendar calendar;
        AutoCompleteTextView textView = null;
        ArrayAdapter adapter;
        Button setDateNT;
        int currentHour, currentMinute;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_placeholderfare);


/**
 Reading from CSV and storing into array list carparkID
 */
                List<String> carparkID = new ArrayList<String>();
                // String[]carparkID = new String[2114];
                inputStream = getResources().openRawResource(R.raw.hdbcarparkinformation);
                int i = 0;

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                try {
                        String csvLine;
                        while ((csvLine = reader.readLine()) != null) {
                                data = csvLine.split(",");
                                try {

                                        //Log.e("Data ",""+data[0]) ;
                                        // Log.e("Data ","Car park ID"+data[0]+"Adress"+data[1]+"shortterm"+data[6]) ;
                                        carparkID.add(data[0]);
                                        // Log.e("Carpark ID ",""+carparkID.get(i)) ;
                                } catch (Exception e) {
                                        Log.e("Problem", e.toString());
                                }
                                i++;
                        }
                } catch (IOException ex) {
                        throw new RuntimeException("Error in reading CSV file: " + ex);
                }


                textView = (AutoCompleteTextView) findViewById(R.id.actv_CPID);
                adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, carparkID);
                textView.setAdapter(adapter);
                adapter.addAll();
                textView.setThreshold(1);
                textView.setOnItemSelectedListener(this);
                textView.setOnItemClickListener(this);


                setDateNT = (Button) findViewById(R.id.btn_SetDNT);
                /**Redirecting to SetDNT page */
                setDateNT.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                TextView agency = (TextView) findViewById(R.id.b_Agency);
                                AutoCompleteTextView cpID = (AutoCompleteTextView) findViewById(R.id.actv_CPID);
                                Intent intent = new Intent(PlaceHolderFare.this, SetDNT.class);
                                intent.putExtra("Agency", String.valueOf(agency.getText()));
                                intent.putExtra("CarParkID", String.valueOf(cpID.getText()));
                                startActivityForResult(intent, 1);
                        }
                });


        }

        @Override
/**When returning back to this placeholder class*/
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
                super.onActivityResult(requestCode, resultCode, data);
                day_Now = (TextView) findViewById(R.id.tv_DateValue);
                startTime = (TextView) findViewById(R.id.tv_StartTimeValue);
                endTime = (TextView) findViewById(R.id.tv_EndTimeValue);
                endDay = (TextView) findViewById(R.id.tv_DateValue2);
                finalCost = (TextView) findViewById(R.id.tv_DispCost);

                if (requestCode == 1) {  /**Parsing Values back from SetDNT class*/
                        if (resultCode == RESULT_OK) {
                                String result = data.getStringExtra("StartTime");
                                startTime.setText(result);
                                result = data.getStringExtra("EndTime");
                                endTime.setText(result);
                                result = data.getStringExtra("StartDay");
                                day_Now.setText(result);
                                result = data.getStringExtra("EndDay");
                                endDay.setText(result);
                                result = data.getStringExtra("Cost");
                                finalCost.setText(result);
                        }

                        if (resultCode == RESULT_CANCELED) { /**If parsing fail do smt*/
                                startTime.setText("Set Start Time");
                                endTime.setText("Set End Time");
                                day_Now.setText("Set Start Date");
                                endDay.setText("Set End Date");
                        }
                }
        }

        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int position,
                                   long arg3) {
                // TODO Auto-generated method stub
                //Log.d("AutocompleteContacts", "onItemSelected() position " + position);

        }

        public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

                InputMethodManager imm = (InputMethodManager) getSystemService(
                        INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

        }

        /**
         * Autofill textbox
         */
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                textView = (AutoCompleteTextView) findViewById(R.id.actv_CPID);

                textView.setText((CharSequence) arg0.getItemAtPosition(arg2));
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(arg1.getApplicationWindowToken(), 0);


        }

        public void ShowAgency(View v) {
                PopupMenu popup = new PopupMenu(this, v);
                popup.setOnMenuItemClickListener(this);
                popup.inflate(R.menu.popup_agency);
                popup.show();
        }

        @Override
/**Agency drop down menu*/
        public boolean onMenuItemClick(MenuItem menuItem) {
                TextView btn = (TextView) findViewById(R.id.b_Agency);

                agency = (String) menuItem.getTitle();
                btn.setText(agency);
                // Toast.makeText(this,agency,Toast.LENGTH_LONG).show();
                // Toast.makeText(this,"Item 1 click",Toast.LENGTH_LONG).show();
                return true;
        }
}
