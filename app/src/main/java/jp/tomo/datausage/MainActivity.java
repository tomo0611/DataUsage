package jp.tomo.datausage;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;

    private static final String DEBUG_TAG = "NetworkStatus";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_cellular:
                    mTextMessage.setText(R.string.title_cellular);
                    return true;
                case R.id.navigation_wifi:
                    mTextMessage.setText(R.string.title_wifi);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isWifiConn = false;
        boolean isMobileConn = false;
        for (Network network : connMgr.getAllNetworks()) {
            NetworkInfo networkInfo = connMgr.getNetworkInfo(network);
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                isWifiConn |= networkInfo.isConnected();
            }
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                isMobileConn |= networkInfo.isConnected();
            }
        }
        Log.d(DEBUG_TAG, "Wifi connected: " + isWifiConn);
        Log.d(DEBUG_TAG, "Mobile connected: " + isMobileConn);

        long mobile = TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes();
        long total = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();

        mTextMessage.setText("Wifi connected: " + isWifiConn+"\n"+"Mobile connected: " + isMobileConn + "\nWifi:" + (total - mobile)/1000 + " KB"+"\nMobile:"+mobile/1000+ " KB\n\n");

        PackageManager packageManager = this.getPackageManager();
        List<ApplicationInfo> apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        for(ApplicationInfo app : apps){
            int uid = app.uid;
            long RxBytes = getUidRxBytes(uid);
            long TxBytes = getUidTxBytes(uid);
            if(!(RxBytes==0&&TxBytes==0)) {
                mTextMessage.append(app.packageName + " (rx:" + RxBytes + " tx:" + TxBytes + ")\n");
            }
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
