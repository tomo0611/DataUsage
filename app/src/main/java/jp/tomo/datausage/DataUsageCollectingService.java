package jp.tomo.datausage;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.IBinder;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DataUsageCollectingService extends Service {

    private ConnectivityManager manager;

    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.

        manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        manager.registerDefaultNetworkCallback(networkCallback);
    }

    private String networkType() {
        TelephonyManager teleMan = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        SubscriptionManager subManager = (SubscriptionManager) getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        List<SubscriptionInfo> subInfoList = subManager.getActiveSubscriptionInfoList();
        int result = -1;
        for(int i = 0; i < subInfoList.size(); i++ ) {
            int subID = subInfoList.get(i).getSubscriptionId();
            int simPosition = subInfoList.get(i).getSimSlotIndex();
            Log.d("TEST", "Simcard in slot " + simPosition + " is "+teleMan.createForSubscriptionId(subID).getNetworkOperatorName());
            Log.d("TEST", "Cellular data is " +teleMan.createForSubscriptionId(subID).getDataNetworkType());
            if(teleMan.createForSubscriptionId(subID).getDataNetworkType() != TelephonyManager.NETWORK_TYPE_UNKNOWN){
                result = subID;
            }
        }
        if(result==-1){
            result=0;
        }
        switch (teleMan.createForSubscriptionId(result).getDataNetworkType()) {
            case TelephonyManager.NETWORK_TYPE_1xRTT: return "1xRTT";
            case TelephonyManager.NETWORK_TYPE_CDMA: return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EDGE: return "EDGE";
            case TelephonyManager.NETWORK_TYPE_EHRPD: return "eHRPD";
            case TelephonyManager.NETWORK_TYPE_EVDO_0: return "EVDO rev.0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A: return "EVDO rev.A";
            case TelephonyManager.NETWORK_TYPE_EVDO_B: return "EVDO rev.B";
            case TelephonyManager.NETWORK_TYPE_GPRS: return "GPRS";
            case TelephonyManager.NETWORK_TYPE_HSDPA: return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSPA: return "HSPA";
            case TelephonyManager.NETWORK_TYPE_HSPAP: return "HSPA+";
            case TelephonyManager.NETWORK_TYPE_HSUPA: return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_IDEN: return "iDen";
            case TelephonyManager.NETWORK_TYPE_LTE: return "LTE";
            case TelephonyManager.NETWORK_TYPE_UMTS: return "UMTS";
            default: return "Unknown";
        }
    }

    private String getDataCarrier() {
        TelephonyManager teleMan = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        SubscriptionManager subManager = (SubscriptionManager) getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        List<SubscriptionInfo> subInfoList = subManager.getActiveSubscriptionInfoList();
        int result = -1;
        for(int i = 0; i < subInfoList.size(); i++ ) {
            int subID = subInfoList.get(i).getSubscriptionId();
            int simPosition = subInfoList.get(i).getSimSlotIndex();
            Log.d("TEST", "Simcard in slot " + simPosition + " is "+teleMan.createForSubscriptionId(subID).getNetworkOperatorName());
            Log.d("TEST", "Cellular data is " +teleMan.createForSubscriptionId(subID).getDataNetworkType());
            if(teleMan.createForSubscriptionId(subID).getDataNetworkType() != TelephonyManager.NETWORK_TYPE_UNKNOWN){
                return teleMan.createForSubscriptionId(subID).getNetworkOperatorName();
            }
        }
        return teleMan.getNetworkOperatorName();
    }

    private final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            super.onAvailable(network);
            Log.i("network",manager.getActiveNetworkInfo().getType()+" "+manager.getActiveNetworkInfo().getTypeName());
            switch(manager.getActiveNetworkInfo().getType()){
                case ConnectivityManager.TYPE_WIFI:
                    temp(DataUsageCollectingService.this);
                    Toast.makeText(getApplicationContext(), "Connected to Wifi ("+manager.getActiveNetworkInfo().getExtraInfo()+")", Toast.LENGTH_SHORT).show();
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    temp(DataUsageCollectingService.this);
                    Toast.makeText(getApplicationContext(), "Connected to Cellular Network\n("+networkType()+"/"+getDataCarrier()+")", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    temp(DataUsageCollectingService.this);
                    Toast.makeText(getApplicationContext(), "Connected to "+manager.getActiveNetworkInfo().getSubtype(), Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        @Override
        public void onLost(Network network) {
            super.onLost(network);
            Toast.makeText(getApplicationContext(), "No Connection", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //if(intent.getBooleanExtra("BOOT",false)){

        //}
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }

    /*public void write(Context context){
        try {
            JSONObject data = new JSONObject(new FileReader("/sdcard/datausage.json").toString());
            PackageManager packageManager = context.getPackageManager();
            File dir = new File("/proc/uid_stat/");
            JSONObject json = new JSONObject();
            String[] children = dir.list();
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy_MM_dd");
            if(!data.has(""+ConnectivityManager.TYPE_WIFI)){
                data.put(""+ConnectivityManager.TYPE_WIFI,new JSONObject());
            }
            if(!data.has(""+ConnectivityManager.TYPE_MOBILE)){
                data.put(""+ConnectivityManager.TYPE_MOBILE,new JSONObject());
            }
            if(!data.getJSONObject(""+ConnectivityManager.TYPE_WIFI).has(sdf1.format(new Date()))){
                data.getJSONObject(""+ConnectivityManager.TYPE_WIFI).put(sdf1.format(new Date()),new JSONObject());
            }
            if(!data.getJSONObject(""+ConnectivityManager.TYPE_MOBILE).has(sdf1.format(new Date()))){
                data.getJSONObject(""+ConnectivityManager.TYPE_MOBILE).put(sdf1.format(new Date()),new JSONObject());
            }

            int networkType = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo().getType();
            JSONObject target = data.getJSONObject(""+networkType).getJSONObject(sdf1.format(new Date()));
            JSONObject json1 = new JSONObject();
            for (String s : children) {
                long RxBytes = getUidRxBytes(Integer.valueOf(s));
                long TxBytes = getUidTxBytes(Integer.valueOf(s));
                if(packageManager.getPackagesForUid(Integer.valueOf(s)) != null) {

                }else{
                    if(!target.has("android")){
                        target.put("android",new JSONArray().put(RxBytes).put(TxBytes));
                    }else{
                        JSONArray old_data=target.getJSONArray("android");
                        target.put("android",new JSONArray().put(RxBytes-old_data.getLong(0)).put(TxBytes-old_data.getLong(1)));
                    }
                }
            }
            json.put("data", json1);
            FileOutputStream out = new FileOutputStream("/sdcard/datausage.json");
            out.write(json.toString(3).getBytes("UTF-8"));
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    public void temp(Context context) {
        PackageManager packageManager = context.getPackageManager();
        File dir = new File("/proc/uid_stat/");
        JSONObject json = new JSONObject();
        String[] children = dir.list();
        try {
            for (String s : children) {
                if(packageManager.getPackagesForUid(Integer.valueOf(s)) != null) {
                    for (String t : packageManager.getPackagesForUid(Integer.valueOf(s))) {
                        json.put(t, new JSONArray().put(getUidRxBytes(Integer.valueOf(s))).put(getUidTxBytes(Integer.valueOf(s))));
                    }
                }else{
                    json.put("android", new JSONArray().put(getUidRxBytes(Integer.valueOf(s))).put(getUidTxBytes(Integer.valueOf(s))));
                }
            }
            FileOutputStream out = new FileOutputStream("/sdcard/tmp.json");
            out.write(json.toString(3).getBytes("UTF-8"));
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Long getUidRxBytes(int localUid) {
        File dir = new File("/proc/uid_stat/");
        String[] children = dir.list();
        if (!Arrays.asList(children).contains(String.valueOf(localUid))) {
            return 0L;
        }
        File uidFileDir = new File("/proc/uid_stat/" + String.valueOf(localUid));
        File uidActualFileReceived = new File(uidFileDir, "tcp_rcv");
        String textReceived = "0";
        try {
            BufferedReader brReceived = new BufferedReader(new FileReader(uidActualFileReceived));
            String receivedLine;
            if ((receivedLine = brReceived.readLine()) != null) {
                textReceived = receivedLine;
            }
        } catch (IOException e) {
        }
        return Long.valueOf(textReceived).longValue();
    }

    private static Long getUidTxBytes(int localUid) {
        File dir = new File("/proc/uid_stat/");
        String[] children = dir.list();
        if (!Arrays.asList(children).contains(String.valueOf(localUid))) {
            return 0L;
        }
        File uidFileDir = new File("/proc/uid_stat/" + String.valueOf(localUid));
        File uidActualFileReceived = new File(uidFileDir, "tcp_snd");
        String textReceived = "0";
        try {
            BufferedReader brReceived = new BufferedReader(new FileReader(uidActualFileReceived));
            String receivedLine;
            if ((receivedLine = brReceived.readLine()) != null) {
                textReceived = receivedLine;
            }
        } catch (IOException e) {
        }
        return Long.valueOf(textReceived).longValue();
    }

}
