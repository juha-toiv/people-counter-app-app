package com.example.juha.peoplecounterapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.juha.peoplecounterapp.document.Timestamp;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import org.joda.time.DateTime;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class DeviceDetailActivity extends UserAuthStateListenerActivity {

    public static final String DEVICE_ID = "device_id";

    private String mDeviceId;

    private TextView mErrorTextView;
    private TextView mDeviceNameView;
    private TextView mLastVisitorValueView;
    private TextView mDateTodayView;
    private TextView mTodayValueView;
    private TextView mYesterdayValueView;
    private TextView mLastHourValueView;
    private TextView mLast7DaysValueView;
    private TextView mLast30DaysValueView;
    private TextView mAllTimeValueView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDeviceId = getIntent().getStringExtra(DEVICE_ID);
        setContentView(R.layout.activity_device_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_activity_device_detail);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mErrorTextView = (TextView) findViewById(R.id.textView_device_detail_error_text);
        mDeviceNameView = (TextView) findViewById(R.id.textView_device_name);
        mDateTodayView = (TextView) findViewById(R.id.textView_date_now);
        mTodayValueView = (TextView) findViewById(R.id.textView_today_value);
        mLastVisitorValueView = (TextView) findViewById(R.id.textView_last_visitor_value);
        mYesterdayValueView = (TextView) findViewById(R.id.textView_yesterday_value);
        mLastHourValueView = (TextView) findViewById(R.id.textView_last_hour_value);
        mLast7DaysValueView = (TextView) findViewById(R.id.textview_last_7_days_value);
        mLast30DaysValueView = (TextView) findViewById(R.id.textview_last_30_days_value);
        mAllTimeValueView = (TextView) findViewById(R.id.textView_all_time_value);
        mDateTodayView.setText(getTodaysDate());
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = firebaseFirestore.collection("devices").document(mDeviceId);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                fetchDeviceDocumentAndUpdateUI();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_item_signout:
                mFirebaseAuth.signOut();
                return true;
            case R.id.menu_item_settings:
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void fetchDeviceDocumentAndUpdateUI() {
        mErrorTextView.setVisibility(View.GONE);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = firebaseFirestore.collection("devices").document(mDeviceId);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    mDeviceNameView.setVisibility(View.VISIBLE);
                    DocumentSnapshot documentSnapshot = task.getResult();
                    String deviceName =  documentSnapshot.getString("name");
                    mDeviceNameView.setText(deviceName);
                }
                else {
                    mDeviceNameView.setVisibility(View.GONE);
                }
            }
        });
        documentReference.collection("timestamps").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<Timestamp> timestamps = new ArrayList<>();
                    for (DocumentSnapshot document : task.getResult()) {
                        Date time = document.getDate("timestamp");
                        int count = document.getLong("count").intValue();
                        String direction = document.getString("direction");
                        timestamps.add(new Timestamp(time, count, direction));
                    }
                    mLastVisitorValueView.setText(getLastVisitorDateAndTime(timestamps));
                    mTodayValueView.setText(getNumberOfVisitorsToday(timestamps) + "");
                    mLastHourValueView.setText(getNumberOfVisitorsLastHour(timestamps) + "");
                    mYesterdayValueView.setText(getNumberOfVisitorsYesterday(timestamps) + "");
                    mLast7DaysValueView.setText(getNumberOfVisitorsLast7Days(timestamps) + "");
                    mLast30DaysValueView.setText(getNumberOfVisitorsLast30Days(timestamps) + "");
                    mAllTimeValueView.setText(getNumberOrVisitorsAllTime(timestamps) + "");
                } else {
                    mErrorTextView.setVisibility(View.VISIBLE);
                    if (Utilities.isInternetConnection(getApplicationContext()) == false) {
                        mErrorTextView.setText(R.string.all_no_internet_connection);
                    }
                    else {
                        mErrorTextView.setText(R.string.device_detail_device_not_found);
                    }
                }
            }
        });
    }

    private String getTodaysDate() {
        Date currentTime = Calendar.getInstance().getTime();
        return DateFormat.getDateInstance(DateFormat.LONG).format(currentTime);
    }

    private String getLastVisitorDateAndTime(ArrayList<Timestamp> timestamps) {
        if (timestamps.size() == 0) {
            return getString(R.string.device_detail_no_visitors);
        }
        DateTime lastVisitor = new DateTime(timestamps.get(0).getTimestamp());
        for (Timestamp timestamp : timestamps) {
            DateTime timestampDateTime = new DateTime(timestamp.getTimestamp());
            if (lastVisitor.isBefore(timestampDateTime)) {
                lastVisitor = timestampDateTime;
            }
        }
        Date lastVisitorDate = lastVisitor.toDate();
        return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(lastVisitorDate);
    }

    private int getNumberOrVisitorsAllTime(ArrayList<Timestamp> timestamps) {
        int i = 0;
        for (Timestamp timestamp : timestamps) {
            i = i + timestamp.getCount();
        }
        return i;
    }

    private int getNumberOfVisitorsToday(ArrayList<Timestamp> timestamps) {
        int i = 0;
        Date currentTime = Calendar.getInstance().getTime();
        int dayToday = currentTime.getDay();
        int monthToday = currentTime.getMonth();
        int yearToday = currentTime.getYear();
        for (Timestamp timestamp : timestamps) {
            int day = timestamp.getTimestamp().getDay();
            int month = timestamp.getTimestamp().getMonth();
            int year = timestamp.getTimestamp().getYear();
            if (day == dayToday && month == monthToday && year == yearToday) {
                i = i + timestamp.getCount();
            }
        }
        return i;
    }

    private int getNumberOfVisitorsLastHour(ArrayList<Timestamp> timestamps) {
        int i = 0;
        Date currentTime = Calendar.getInstance().getTime();
        DateTime today = new DateTime(currentTime);
        DateTime hourAgo = today.minusHours(1);
        for (Timestamp timestamp : timestamps) {
            DateTime dt = new DateTime(timestamp.getTimestamp());
            if (dt.isAfter(hourAgo)) {
                i = i + timestamp.getCount();
            }
        }
        return i;
    }

    private int getNumberOfVisitorsYesterday(ArrayList<Timestamp> timestamps) {
        int i = 0;
        Date currentTime = Calendar.getInstance().getTime();
        DateTime today = new DateTime(currentTime);
        DateTime dayAgo = today.minusDays(1);
        for (Timestamp timestamp : timestamps) {
            DateTime dt = new DateTime(timestamp.getTimestamp());
            if (dt.isAfter(dayAgo)) {
                i = i + timestamp.getCount();
            }
        }
        return i;
    }

    private int getNumberOfVisitorsLast7Days(ArrayList<Timestamp> timestamps) {
        int i = 0;
        Date currentTime = Calendar.getInstance().getTime();
        DateTime today = new DateTime(currentTime);
        DateTime sevenDaysAgo = today.minusDays(7);
        for (Timestamp timestamp : timestamps) {
            DateTime dt = new DateTime(timestamp.getTimestamp());
            if (dt.isAfter(sevenDaysAgo)) {
                i = i + timestamp.getCount();
            }
        }
        return i;
    }

    private int getNumberOfVisitorsLast30Days(ArrayList<Timestamp> timestamps) {
        int i = 0;
        Date currentTime = Calendar.getInstance().getTime();
        DateTime today = new DateTime(currentTime);
        DateTime thirtyDaysAgo = today.minusDays(30);
        for (Timestamp timestamp : timestamps) {
            DateTime dt = new DateTime(timestamp.getTimestamp());
            if (dt.isAfter(thirtyDaysAgo)) {
                i = i + timestamp.getCount();
            }
        }
        return i;
    }

    private int getNumberOfVisitorsBetweenDates(Date date1, Date date2, ArrayList<Timestamp> timestamps) {
        int i = 0;
        DateTime dateTime1 = new DateTime(date1);
        DateTime dateTime2 = new DateTime(date2);
        for (Timestamp timestamp : timestamps) {
            DateTime dt = new DateTime(timestamp.getTimestamp());
            if (dt.isAfter(dateTime1) && dt.isBefore(dateTime2)) {
                i = i + timestamp.getCount();
            }
        }
        return i;
    }

}
