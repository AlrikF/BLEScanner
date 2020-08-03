package com.example.blescanner;

import androidx.appcompat.app.AppCompatActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    public static final int REQUEST_ENABLE_BT = 1;

    private static HashMap<String, BTLE_Device> deviceHashMap;
    private static ArrayList<BTLE_Device> deviceList;
    private static BTLE_ListAdapter adapter;
    public static String plan_link;
    Handler handler;


    public static  BTLE_Scan myScanner;
    private static   NotifService notif;
    boolean notifFlag[]=new boolean[10];
    private static Button btn_Scan,btn_qr;
    private BroadcastReceiver_BTState BTonoffreceiver;
    private ExpandableListView listView;

    private ExpandableListAdapter listAdapter;
    private List<String>listDataHeader;
    private HashMap<String,List<String>>listHash;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ////////////////////////////////////////
        listView =(ExpandableListView)findViewById(R.id.lvExp);
        initData();
        listAdapter=new ExpandableListAdapter(this,listDataHeader,listHash);
        listView.setAdapter(listAdapter);

        ///////////////////////////////////////



        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Utils.toast(getApplicationContext(), "BLE not supported");
            finish();
        }

        BTonoffreceiver = new BroadcastReceiver_BTState(getApplicationContext());
        deviceHashMap = new HashMap<>();
        deviceList = new ArrayList<>();
        notif=new NotifService(this);
        handler=new Handler();

        adapter = new BTLE_ListAdapter(this, R.layout.device_list_item, deviceList);
        myScanner=new BTLE_Scan(this,Integer.MAX_VALUE,-120);
        btn_qr=(Button)findViewById(R.id.btn_qrScan);



        notif.createNotificationChannel();
        ListView listView = new ListView(getApplicationContext());
        listView.setAdapter(adapter);
        //listView.setOnItemClickListener(this);
        ((ScrollView) findViewById(R.id.scrollView)).addView(listView);

        registerReceiver(BTonoffreceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        btn_Scan = (Button) findViewById(R.id.btn_scan);
        btn_Scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.toast(getApplicationContext(), "Scan Button Pressed");
                if(myScanner.isScanning()==false)
                {
                    startScan();
                }
                else
                {
                    stopScan();
                }
            }
        });

        btn_qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),ScanCodeActivity.class));
            }
        });








    }

    ////////////////////////////////////////
    private void initData()
    {
        listDataHeader =new ArrayList<>();
        listHash =new HashMap<>();
        listDataHeader.add("Beacon1        ");
        listDataHeader.add("Beacon2        ");
        listDataHeader.add("Beacon3        ");

        List<String> bea1=new ArrayList<>();
        bea1.add("Beacon1");

        List<String> bea2=new ArrayList<>();
        bea2.add("Beacon2");

        List<String> bea3=new ArrayList<>();
        bea3.add("Beacon3");

        listHash.put(listDataHeader.get(0),bea1);
        listHash.put(listDataHeader.get(1),bea2);
        listHash.put(listDataHeader.get(2),bea3);

    }




/////////////////////////////////////////////
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // Utils.toast(getApplicationContext(), "Bluetooth turned on");
            }
            else if (resultCode == RESULT_CANCELED) {
                Utils.toast(getApplicationContext(), "Failed to turn on Bluetooth");
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(BTonoffreceiver);
    }

    public void addDevice(BluetoothDevice device, int new_rssi, final int[] distArray) {

        String address = device.getAddress();

            if (!deviceHashMap.containsKey(address)) {
                BTLE_Device newDevice = new BTLE_Device(device);


                newDevice.setRSSI(new_rssi);
                newDevice.setDistArray(distArray);
                System.out.println(newDevice.getAddress());

                    if(distArray[1] == 1 || distArray[2]==1)
                    {
                        //notif.triggerNotification();

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim);
                                Animation animation2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim2);

                                if(distArray[1]==1 && notifFlag[1]==false)
                                {
                                    notif.triggerNotification(0001,"1");
                                    PositioningActivity.b2.startAnimation(animation);


                                    //PositioningActivity.b11.startAnimation(animation);

                                    PositioningActivity.b11.startAnimation(animation2);
                                    notifFlag[1] = true;
                                }

                                if(distArray[2]==1 && notifFlag[2]==false)
                                {
                                    notif.triggerNotification(0002,"2");
                                    PositioningActivity.b4.startAnimation(animation2);

                                    notifFlag[2] = true;
                                }


                            }
                        },200);


                    }

                    if (distArray[1] == 0 || distArray[2]==0) {

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(distArray[1]==0 && notifFlag[1]==true)
                                {
                                    notif.cancelNotification(0001);
                                    PositioningActivity.b2.clearAnimation();
                                    PositioningActivity.b11.clearAnimation();
                                    notifFlag[1] = false;
                                }
                                if(distArray[2]==0 && notifFlag[2]==true)
                                {
                                    notif.cancelNotification(0002);
                                    PositioningActivity.b4.clearAnimation();
                                    notifFlag[2] = false;
                                }

                            }
                        },200);
                        //notif.cancelNotification();


                    }
                deviceHashMap.put(address, newDevice);
                deviceList.add(newDevice);
            } else {
                    if (distArray[1] == 1 || distArray[2] == 1) {
                        //notif.triggerNotification();

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim);
                                if (distArray[1] == 1 && notifFlag[1]==false) {
                                    notif.triggerNotification(0001,"1");
                                    Animation animation2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim);
                                    PositioningActivity.b11.startAnimation(animation2);

                                    PositioningActivity.b2.startAnimation(animation);
                                    notifFlag[1] = true;

                                }
                                if (distArray[2] == 1 && notifFlag[2]==false) {
                                    notif.triggerNotification(0002,"2");
                                    Animation animation2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim);
                                    PositioningActivity.b4.startAnimation(animation2);
                                    notifFlag[2] = true;
                                }
                            }
                        }, 200);

                    }
                    if (distArray[1] == 0 || distArray[2] == 0) {

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (distArray[1] == 0 && notifFlag[1]==true) {
                                    notif.cancelNotification(0001);
                                    PositioningActivity.b2.clearAnimation();
                                    PositioningActivity.b11.clearAnimation();
                                    notifFlag[1] = false;
                                }
                                if (distArray[2] == 0 && notifFlag[2]==true) {
                                    notif.cancelNotification(0002);
                                    PositioningActivity.b4.clearAnimation();
                                    notifFlag[2] = false;
                                }
                            }
                        }, 200);

                }
            }
                deviceHashMap.get(address).setRSSI(new_rssi);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScan();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopScan();
    }

    public static void startScan()
    {

        btn_Scan.setText("Scanning...");
        deviceList.clear();
        deviceHashMap.clear();
        adapter.notifyDataSetChanged();
        myScanner.start();
    }

    public static void stopScan() {

        btn_Scan.setText("Scan Again...");
        myScanner.stop();

    }
}
