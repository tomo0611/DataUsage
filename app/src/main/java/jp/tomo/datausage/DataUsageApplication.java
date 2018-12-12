package jp.tomo.datausage;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

public class DataUsageApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("info","Application was started!");
        Intent intent = new Intent(this, DataUsageCollectingService.class);
        startService(intent);
    }
}
