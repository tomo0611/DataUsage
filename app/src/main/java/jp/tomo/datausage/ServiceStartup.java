package jp.tomo.datausage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ServiceStartup extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i("ServiceStartup","BOOT!");
        // 端末起動時？
        if( Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            new Thread(new Runnable(){
                @Override
                public void run()
                {
                    Intent i = new Intent(context, DataUsageCollectingService.class);
                    i.putExtra("BOOT",true);
                    context.startService(i);
                }
            }).start();
        }
    }
}