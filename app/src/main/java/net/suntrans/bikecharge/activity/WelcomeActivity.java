package net.suntrans.bikecharge.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;

import net.suntrans.bikecharge.App;
import net.suntrans.bikecharge.MainActivity;
import net.suntrans.bikecharge.R;
import net.suntrans.bikecharge.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;
import me.weyye.hipermission.PermissonItem;

/**
 * Created by Looney on 2017/5/26.
 */

public class WelcomeActivity extends BasedActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()) {
            finish();
            return;
        }
        setContentView(R.layout.activity_welcome);
    }

    @Override
    protected void onResume() {
        super.onResume();

        findViewById(R.id.rootView).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//        locationTask();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            checkPermission();
        }else {
            checkLoginState();
        }
    }

    boolean ischecked = false;

    private void checkLoginState() {
        if (ischecked)
            return;
        ischecked = true;
        String access_token = App.getSharedPreferences().getString("access_token", "-1");
        long expires_in = App.getSharedPreferences().getLong("expires_in", 1l);
        long fristLoginTime = App.getSharedPreferences().getLong("fristLoginTime", 1l);
        if (access_token.equals("-1") || expires_in == -1l) {
            App.getSharedPreferences().edit().putBoolean("isLogin", false).commit();
        } else {
            long currentTime = System.currentTimeMillis() / 1000;
            if (currentTime - fristLoginTime > (expires_in - 60 * 60 * 24 * 2)) {
                App.getSharedPreferences().edit().putBoolean("isLogin", false).commit();
            } else {
                App.getSharedPreferences().edit().putBoolean("isLogin", true).commit();
            }
        }
        handler.removeCallbacksAndMessages(null);
        handler.sendEmptyMessageDelayed(FLAG_START_MAIN, 1800);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FLAG_START_MAIN:
                    Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    LogUtil.e("startActivity");

                    overridePendingTransition(android.support.v7.appcompat.R.anim.abc_fade_in, android.support.v7.appcompat.R.anim.abc_fade_out);
                    break;
            }
        }
    };


    private static final int FLAG_START_MAIN = 0;

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }






    public void checkPermission() {
        final List<PermissonItem> permissionItems = new ArrayList<PermissonItem>();
        permissionItems.add(new PermissonItem(Manifest.permission.WRITE_EXTERNAL_STORAGE, "存储权限", R.drawable.permission_ic_memory));
        permissionItems.add(new PermissonItem(Manifest.permission.READ_PHONE_STATE, "手机状态", R.drawable.phone));
        permissionItems.add(new PermissonItem(Manifest.permission.ACCESS_COARSE_LOCATION, "位置信息", R.drawable.permission_ic_location));
        HiPermission.create(WelcomeActivity.this)
                .permissions(permissionItems)
                .checkMutiPermission(new PermissionCallback() {
                    @Override
                    public void onClose() {
                        LogUtil.i("关闭");
                    }

                    @Override
                    public void onFinish() {
                        LogUtil.i("结束了我的儿子");
                        checkLoginState();

                    }

                    @Override
                    public void onDeny(String permisson, int position) {
                        LogUtil.i("拒绝了权限" + permisson);
                    }

                    @Override
                    public void onGuarantee(String permisson, int position) {
                        LogUtil.i("允许：" + permisson);
                    }
                });
    }
}
