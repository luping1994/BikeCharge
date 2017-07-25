package net.suntrans.bikecharge;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.pgyersdk.crash.PgyCrashManager;
import com.squareup.leakcanary.LeakCanary;

import cn.jpush.android.api.JPushInterface;

import static net.suntrans.bikecharge.BuildConfig.ENABLE_DEBUG;


/**
 * Created by Looney on 2017/5/25.
 */

public class App extends Application {
    private static final String appID = "0717df255a";
    private static final String WXAPPID = "wxd41679d9bf36e90d";
    public static SharedPreferences sharedPreferences;
    private static Application application;

    public static Application getApplication() {
        return application;
    }

    public static SharedPreferences getSharedPreferences() {
        if (sharedPreferences == null) {
            sharedPreferences = getApplication().getSharedPreferences("bikecharge", Context.MODE_PRIVATE);
        }
        return sharedPreferences;
    }

    @Override
    public void onCreate() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        super.onCreate();
        application = this;
        JPushInterface.setDebugMode(ENABLE_DEBUG);
        JPushInterface.init(this);
        if (!ENABLE_DEBUG)
            PgyCrashManager.register(this);
    }


}
