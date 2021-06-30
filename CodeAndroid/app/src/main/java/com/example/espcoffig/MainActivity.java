package com.example.espcoffig;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private PrintWriter output;
    private BufferedReader input;
    Socket socket;
    int SERVER_PORT;
    String IP_Address;
    EditText editTextIP, editTextPORT, editTextSSID, editTextPASS;
    Button buttonConnectAP, buttonDisconnectAP, buttonConnectWifi;
    ImageButton imageButtonScanWifi, imageButtonVisibility;
    boolean checkconnect = false;
    AlertDialog alertDialog;
    boolean checkResume = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow()
                .getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        this.getWindow().getAttributes().flags &= ~WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_main);
        editTextIP = (EditText) findViewById(R.id.edit_IP);
        editTextPORT = (EditText) findViewById(R.id.edit_PORT);
        editTextSSID = (EditText) findViewById(R.id.edit_SSID);
        editTextPASS = (EditText) findViewById(R.id.edit_PASS);
        buttonConnectAP = (Button) findViewById(R.id.btn_connect_ap);
        buttonDisconnectAP = (Button) findViewById(R.id.btn_disconnect_ap);
        buttonConnectWifi = (Button) findViewById(R.id.btn_connect_sta);
        imageButtonScanWifi = (ImageButton) findViewById(R.id.btn_scan_wifi);
        imageButtonVisibility = (ImageButton) findViewById(R.id.btn_visibility);
        CheckConnect();
        ButtonConnectSever();
        checkAndRequestPermissions();
        CheckWifiEnable();
    }

    public void CheckWifiEnable() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    MainActivity.this);
            // set title
            alertDialogBuilder.setTitle("Wifi Settings");
            // set dialog message
            alertDialogBuilder
                    .setMessage("Do you want to enable WIFI ?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            alertDialog.dismiss();
                            checkResume = true;
                            startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            alertDialog.dismiss();
                            finish();
                        }
                    });
            // create alert dialog
            alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    private boolean checkAndRequestPermissions() {
        int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 1);
            return false;
        }
        return true;
    }

    public void CheckConnect() {
        if (!checkconnect) {
            editTextSSID.setEnabled(false);
            editTextSSID.setAlpha((float) 0.3);
            editTextPASS.setEnabled(false);
            editTextPASS.setAlpha((float) 0.3);
            buttonConnectAP.setEnabled(true);
            buttonConnectAP.setAlpha((float) 1.0);
            buttonDisconnectAP.setEnabled(false);
            buttonDisconnectAP.setAlpha((float) 0.3);
            buttonConnectWifi.setEnabled(false);
            buttonConnectWifi.setAlpha((float) 0.3);
            imageButtonScanWifi.setEnabled(false);
            imageButtonScanWifi.setAlpha((float) 0.3);
            imageButtonVisibility.setEnabled(false);
            imageButtonVisibility.setAlpha((float) 0.3);
        } else {
            editTextSSID.setEnabled(true);
            editTextSSID.setAlpha((float) 1.0);
            editTextPASS.setEnabled(true);
            editTextPASS.setAlpha((float) 1.0);
            buttonConnectAP.setEnabled(false);
            buttonConnectAP.setAlpha((float) 0.3);
            buttonDisconnectAP.setEnabled(true);
            buttonDisconnectAP.setAlpha((float) 1.0);
            buttonConnectWifi.setEnabled(true);
            buttonConnectWifi.setAlpha((float) 1.0);
            imageButtonScanWifi.setEnabled(true);
            imageButtonScanWifi.setAlpha((float) 1.0);
            imageButtonVisibility.setEnabled(true);
            imageButtonVisibility.setAlpha((float) 1.0);
        }
    }

    public void ButtonConnectSever() {
        buttonConnectAP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextPORT.getText().length() != 0 && editTextIP.getText().length() != 0) {
                    IP_Address = editTextIP.getText().toString();
                    SERVER_PORT = Integer.parseInt(editTextPORT.getText().toString());
                    hideKeyboard();
                    new Thread(new Thread1()).start();
                } else
                    Toast.makeText(getApplicationContext(), "Please Enter IP & Port", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void DisconnectSever() {
        buttonDisconnectAP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                output.close();
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                checkconnect = false;
                CheckConnect();
            }
        });
    }

    public void ConnectWifi() {
        buttonConnectWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String SSID = editTextSSID.getText().toString();
                String PASS = editTextPASS.getText().toString();
                hideKeyboard();
                String Buf = "*" + SSID + "," + PASS + "\n";
                if (!Buf.isEmpty())
                    new Thread(new Thread3(Buf)).start();
            }
        });
    }

    // Connect to Server
    class Thread1 implements Runnable {
        public void run() {
            try {
                socket = new Socket(IP_Address, SERVER_PORT);
                output = new PrintWriter(socket.getOutputStream());
                socket.setTcpNoDelay(true);
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Successfully Connected To Server\n"
                                + IP_Address, Toast.LENGTH_SHORT).show();
                        checkconnect = true;
                        CheckConnect();
                        DisconnectSever();
                        ConnectWifi();
                        OnOffVisibility();
                        IntentPopupScanWifi();
                    }
                });
                new Thread(new Thread2()).start();
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), "Can't Connected To Server !!!", Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    //    Get data from server
    class Thread2 implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    final String message = input.readLine();
                    if (message != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                            }
                        });
                    }
                } catch (IOException e) {
                }
            }
        }
    }

    // Send data to server
    class Thread3 implements Runnable {
        private String message;

        Thread3(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            output.write(message);
            output.flush();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }
    }

    public void OnOffVisibility() {
        imageButtonVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextPASS.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                    imageButtonVisibility.setImageResource(R.drawable.ic_baseline_visibility_24);
                    editTextPASS.setInputType(InputType.TYPE_CLASS_TEXT);
                } else {
                    editTextPASS.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    imageButtonVisibility.setImageResource(R.drawable.ic_baseline_visibility_off_24);
                }
            }
        });
    }

    public void IntentPopupScanWifi() {
        imageButtonScanWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PopUpScanWifi.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String SSID = data.getStringExtra("SSID");
            editTextSSID.setText(SSID);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (checkconnect) {
            output.close();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkResume)
            CheckWifiEnable();
    }

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}