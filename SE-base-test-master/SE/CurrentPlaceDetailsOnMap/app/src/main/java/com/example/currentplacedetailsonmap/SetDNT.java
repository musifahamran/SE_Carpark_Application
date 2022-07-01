package com.example.currentplacedetailsonmap;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import customfonts.MyEditText;

public class SetDNT extends AppCompatActivity {
/**
 ************** DO NOT TOUCH THIS CLASS UNLESS YOU KNOW WHAT YOU'RE DOING*****************
 * */
    Button btn_Done, btn_Cancel;
    MyEditText chooseTime, chooseTime2, chooseDate1, chooseDate2;
    TimePickerDialog timePickerDialog;
    DatePickerDialog datePickerDialog;
    long startDate,endDate,startTime;
    String amPm, dayOfWeek, time, time2, agency, cpID, formatDate1, formatDate2, conc = "", strCost;
    Calendar calendar;
    InputStream inputStream;
    String[] data;
    List<String> carparkID = new ArrayList<String>();
    List<String> freeParking = new ArrayList<String>();
    List<String> nightParking = new ArrayList<String>();
    ArrayList<CarparkItem> cplist = new ArrayList<CarparkItem>();

    int currentHour, currentMinute;

    ArrayList<CarparkItem> getCplist()
    {
        return cplist;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_set_dnt);
        Intent intent = getIntent();
        agency = intent.getStringExtra("Agency");
        cpID = intent.getStringExtra("CarParkID");
        cplist = GlobalInstance.getInstance().carparkList;

        Log.e("Agency: ", "" + agency);
        Log.e("CarParkID: ", "" + cpID);


        chooseTime = (MyEditText) findViewById(R.id.et_ChooseTime);
        chooseTime.setInputType(InputType.TYPE_NULL);
        chooseTime2 = (MyEditText) findViewById(R.id.et_ChooseTime2);
        chooseTime2.setInputType(InputType.TYPE_NULL);
        chooseDate1 = (MyEditText) findViewById(R.id.et_ChooseDate2);
        chooseDate1.setInputType(InputType.TYPE_NULL);
        chooseDate2 = (MyEditText) findViewById(R.id.et_ChooseDate);
        chooseDate2.setInputType(InputType.TYPE_NULL);

        btn_Done = (Button) findViewById(R.id.btn_Done);
        btn_Cancel = (Button) findViewById(R.id.btn_Cancel);
        /*
        SimpleDateFormat dateF = new SimpleDateFormat("E, dd MMM yyyy", Locale.getDefault());
        SimpleDateFormat timeF = new SimpleDateFormat("hh:mm aaa", Locale.getDefault());
        String NowDate = dateF.format(Calendar.getInstance().getTime());
        String NowTime = timeF.format(Calendar.getInstance().getTime());
        chooseDate1.setText(NowDate);
        chooseTime.setText(NowTime);
*/
        showClock(chooseTime, 0);
        showClock(chooseTime2, 1);
        showDate(chooseDate1, 0);
        showDate(chooseDate2, 1);


        btn_Done.setOnClickListener(view -> {
            double totalCost = 0;

            try {
                formatting(time, String.valueOf(chooseDate1.getText()), 0);
                formatting(time2, String.valueOf(chooseDate2.getText()), 1);
                totalCost = calcFare(agency, cpID);
                NumberFormat formatter = NumberFormat.getCurrencyInstance();
                strCost = formatter.format(totalCost);


            } catch (ParseException e) {
                e.printStackTrace();
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra("StartTime", String.valueOf(chooseTime.getText()));
            resultIntent.putExtra("EndTime", String.valueOf(chooseTime2.getText()));
            resultIntent.putExtra("EndDay", String.valueOf(chooseDate2.getText()));
            resultIntent.putExtra("StartDay", String.valueOf(chooseDate1.getText()));
            resultIntent.putExtra("Cost", strCost);
            setResult(RESULT_OK, resultIntent);
            finish();
        });


        btn_Cancel.setOnClickListener(view -> {
            Intent resultIntent = new Intent();
            setResult(RESULT_CANCELED, resultIntent);
            finish();

        });
    }
    public void formatting(String time, String date, int check) throws ParseException {

        conc = date;
        conc = conc.concat(" " + time);
        SimpleDateFormat format = new SimpleDateFormat("E, dd MMM yyyy HH:mm");
        Date newDate = format.parse(conc);
        format = new SimpleDateFormat("yyyy,MM,dd,HH,mm");
        if (check == 0) {
            formatDate1 = format.format(newDate);
            //formatDate1=formatDate1.concat("T"+time+".000+08:00");
        } else {
            formatDate2 = format.format(newDate);
            //formatDate2=formatDate2.concat("T"+time+".000+08:00");
        }


    }

    public void gettingData() {
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
                    freeParking.add(data[7]);
                    nightParking.add(data[8]);
                    Log.e("Carpark ID ", "" + carparkID.get(i));
                    Log.e("Freeparking ", "" + freeParking.get(i));
                    Log.e("NightParking ", "" + nightParking.get(i));
                } catch (Exception e) {
                    Log.e("Problem", e.toString());
                }
                i++;
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error in reading CSV file: " + ex);
        }

    }

    public double calcFare(String agency, String cpID) throws ParseException {
        double cost = 0;
        double shortTermRate, wholeDayRate, nightRate;


        int[] startDate = Arrays.stream(formatDate1.split(",")).mapToInt(Integer::parseInt).toArray();
        int[] endDate = Arrays.stream(formatDate2.split(",")).mapToInt(Integer::parseInt).toArray();

        LocalDateTime fromDate = LocalDateTime.of(startDate[0], startDate[1], startDate[2], startDate[3], startDate[4], 0);
        LocalDateTime toDate = LocalDateTime.of(endDate[0], endDate[1], endDate[2], endDate[3], endDate[4], 0);


        if (agency.equalsIgnoreCase("HDB") || agency.equalsIgnoreCase("URA")) {
            LocalDateTime toNightPark = LocalDateTime.of(startDate[0], startDate[1], startDate[2], 22, 30);
            LocalDateTime endOfNightPark = LocalDateTime.of(endDate[0], endDate[1], (endDate[2] + 1), 07, 00);

            //long test=ChronoUnit.MINUTES.between(fromDate,toDate);
            long noOfDays = Duration.between(fromDate, toDate).toDays();
            long totalMinutes = Duration.between(fromDate, toDate).toMinutes();
            totalMinutes = totalMinutes - (noOfDays * 1440);
            long minutesLeft = totalMinutes;
            long minuteToNightParking = ChronoUnit.MINUTES.between(fromDate, toNightPark);

            shortTermRate = 0.60; //per 30min
            wholeDayRate = 12.00; //per day
            int totalNightParkingMins = 510;
            int totalWhleDayParkingMin = 1440;

            do {
                if (noOfDays > 0) {
                    cost = cost + wholeDayRate;
                    noOfDays -= 1;
                } else {
                    cost = cost + ((minuteToNightParking * shortTermRate) / 30);
                    minutesLeft = totalMinutes - minuteToNightParking;
                    if (minutesLeft >= 510) {
                        minutesLeft -= 510;
                        cost = cost + 5 + ((minutesLeft * shortTermRate) / 30);
                    } else {
                        if (minutesLeft >= 250)
                            cost = cost + 5;
                        else
                            cost = cost + ((minutesLeft * shortTermRate) / 30);


                    }
                    noOfDays-=1;
                }
            } while (noOfDays != -1);


        }


        return cost;
    }

    public void showDate(MyEditText chooseDate, int check) {
        chooseDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);

                calendar = Calendar.getInstance();


                DatePickerDialog dialog = new DatePickerDialog(SetDNT.this, new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {



                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);



                        String dateFormat = "E, dd MMM yyyy";
                        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
                        chooseDate.setText(sdf.format(calendar.getTime()));

                        if (check==0)
                            startDate=calendar.getTimeInMillis();
                        else
                            endDate=calendar.getTimeInMillis();
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));;
                if(check==0)
                    dialog.getDatePicker().setMinDate(System.currentTimeMillis()-1000);
                else
                    dialog.getDatePicker().setMinDate(startDate);
                dialog.show();




            }
        });
    }


    public void showClock(MyEditText ct, final int check) {


        ct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);


                calendar = Calendar.getInstance();
                currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                currentMinute = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(SetDNT.this, new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                        if (hourOfDay >= 12)
                            amPm = "PM";
                        else
                            amPm = "AM";

                        if (check == 0) {
                            time = String.format("%02d:%02d", hourOfDay, minutes);
                        } else {
                            time2 = String.format("%02d:%02d", hourOfDay, minutes);
                        }
                        int hour = hourOfDay % 12;
                        ct.setText(String.format("%02d:%02d %s", hour == 0 ? 12 : hour,
                                minutes, hourOfDay < 12 ? "am" : "pm"));

                    }

                }, currentHour, currentMinute, false);

                timePickerDialog.show();

            }


        });

    }
}
