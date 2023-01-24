package com.example.juha.peoplecounterapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.juha.peoplecounterapp.document.Device;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;


public class DeviceListActivity extends UserAuthStateListenerActivity {

    private FirebaseFirestore mFirebaseFirestore;

    private ArrayList<Device> mDevices = new ArrayList<>();

    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerView mRecyclerView;
    private DeviceRecyclerViewAdapter mDeviceRecyclerViewAdapter;
    private TextView mErrorTextView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseAuth = FirebaseAuth.getInstance();
        if (mFirebaseAuth.getCurrentUser() != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            Boolean isReceivingNotifications = sharedPreferences.getBoolean("preference_category_notifications", true);
            if (isReceivingNotifications) {
                FirebaseMessaging.getInstance().subscribeToTopic(getString(R.string.notification_receiveNotificationsTopic));
            }
        }
        setContentView(R.layout.activity_device_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_activity_device_list);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());
        mErrorTextView = findViewById(R.id.textView_device_list_error);
        mRecyclerView = findViewById(R.id.recyclerView_device_list);
        assert mRecyclerView != null;
        mDeviceRecyclerViewAdapter = new DeviceRecyclerViewAdapter();
        mRecyclerView.setAdapter(mDeviceRecyclerViewAdapter);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresherLayout_device_list);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchDeviceDocumentsAndUpdateUI();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference collectionReferenceDevices = mFirebaseFirestore.collection("devices");
        collectionReferenceDevices.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                fetchDeviceDocumentsAndUpdateUI();
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

    private void fetchDeviceDocumentsAndUpdateUI() {
        mErrorTextView.setVisibility(View.GONE);
        mDevices.clear();
        mDeviceRecyclerViewAdapter.notifyDataSetChanged();
        mSwipeRefreshLayout.setRefreshing(true);
        mFirebaseFirestore.collection("devices").orderBy("name").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                mSwipeRefreshLayout.setRefreshing(false);
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        String deviceId = document.getId();
                        String deviceName = document.getString("name");
                        mDevices.add(new Device(deviceId, deviceName));
                    }
                    if (mDevices.isEmpty()) {
                        mErrorTextView.setVisibility(View.VISIBLE);
                        mErrorTextView.setText(R.string.device_list_no_devices_found);
                    } else {
                        mDeviceRecyclerViewAdapter.notifyDataSetChanged();
                    }
                } else {
                    mErrorTextView.setVisibility(View.VISIBLE);
                    if (Utilities.isInternetConnection(getApplicationContext()) == false) {
                        mErrorTextView.setText(R.string.all_no_internet_connection);
                    } else {
                        mErrorTextView.setText(R.string.device_list_no_devices_found);
                    }
                }
            }
        });
    }


    private class DeviceRecyclerViewAdapter extends RecyclerView.Adapter<DeviceRecyclerViewAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.mDeviceNameView.setText(mDevices.get(position).getName());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), DeviceDetailActivity.class);
                    intent.putExtra(DeviceDetailActivity.DEVICE_ID, mDevices.get(position).getId());
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mDevices.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public final TextView mDeviceNameView;

            public ViewHolder(View view) {
                super(view);
                mDeviceNameView = (TextView) view.findViewById(R.id.textView_device_list_item_name);
            }
        }
    }

}
