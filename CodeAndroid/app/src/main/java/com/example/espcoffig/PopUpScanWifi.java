package com.example.espcoffig;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class PopUpScanWifi extends Activity {
    WifiManager wifiManager;
    ListView listView;
    Button buttonCancel;
    List<ScanResult> results;
    ArrayList<String> arrayList = new ArrayList<>();
    Handler handler = new Handler();
    SwipeRefreshLayout swipeRefreshLayout;
    Intent intent = new Intent();
    ProgressBar progressBar;
    LocationManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_up_scan_wifi);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * .9), (int) (height * .5));
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = 300;
        getWindow().setAttributes(params);
        buttonCancel = (Button) findViewById(R.id.btn_popup_cancel_scan);
        listView = findViewById(R.id.listview);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefreshlayout);
        Refresh();
        CancelPopup();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "WiFi is disabled ... We need to enable it", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            scanWifi();
        else {
            Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show();
            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }

    public void Refresh() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                scanWifi();
            }
        });
    }

    public void CancelPopup() {
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void scanWifi() {
        arrayList.clear();
        progressBar.setVisibility(View.VISIBLE);
        if (listView != null)
            listView.setAdapter(null);
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
    }

    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                results = wifiManager.getScanResults();
                for (int i = 0; i < results.size(); i++) {
                    if (results.get(i).frequency <= 3000) {
                        if (!results.get(i).SSID.equals("")) {
                            if (getCategoryPos(results.get(i).SSID) == -1) {
                                arrayList.add(results.get(i).SSID);
                                listView.setAdapter(new ArrayAdapter<String>(context,
                                        android.R.layout.simple_list_item_1, arrayList));
                            }
                        }
                    }
                }
                if (listView != null) {
                    swipeRefreshLayout.setRefreshing(false);
                    progressBar.setVisibility(View.INVISIBLE);
                    getSSID();
                }
            }

        }
    };

    public void getSSID() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
                String SSID = item.toString();
                intent.putExtra("SSID", SSID);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    public int getCategoryPos(String category) {
        return arrayList.indexOf(category);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listView != null)
            listView.setAdapter(null);
        unregisterReceiver(wifiReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        scanWifi();
    }
}