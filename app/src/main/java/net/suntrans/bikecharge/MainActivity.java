package net.suntrans.bikecharge;

import android.content.ComponentName;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pgyersdk.update.PgyUpdateManager;
import com.trello.rxlifecycle.android.ActivityEvent;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import net.suntrans.bikecharge.bean.Message;
import net.suntrans.bikecharge.fragment.ChargeFragment;
import net.suntrans.bikecharge.fragment.EnergyProfilerFragment;
import net.suntrans.bikecharge.fragment.MapFragment;
import net.suntrans.bikecharge.fragment.PersonFragment;
import net.suntrans.bikecharge.service.WebScketService;
import net.suntrans.bikecharge.utils.LogUtil;
import net.suntrans.bikecharge.utils.RxBus;
import net.suntrans.bikecharge.utils.UiUtils;

import cn.jpush.android.api.JPushInterface;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static android.support.design.widget.TabLayout.GRAVITY_FILL;
import static android.support.design.widget.TabLayout.MODE_FIXED;
import static net.suntrans.bikecharge.utils.UiUtils.getContext;

public class MainActivity extends RxAppCompatActivity {
    //    private final int[] TAB_TITLES = new int[]{R.string.nav_charge,R.string.nav_battery,R.string.nav_map, R.string.nav_mime};
    private final int[] TAB_TITLES = new int[]{R.string.nav_charge, R.string.nav_map, R.string.nav_mime};
    //    private final int[] TAB_IMGS = new int[]{R.drawable.select_charge,R.drawable.select_battery, R.drawable.select_map, R.drawable.select_mime};
    private final int[] TAB_IMGS = new int[]{R.drawable.select_charge, R.drawable.select_map, R.drawable.select_mime};
    private TabLayout tabLayout;
    private Fragment[] fragments;
    private WebScketService.ibinder binder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (WebScketService.ibinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtil.i("绑定失败");
        }
    };
    private Subscription subscribe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Intent broadcastIntent = new Intent();
//        broadcastIntent.setAction("com.tencent.mm.plugin.openapi.Intent.ACTION_REFRESH_WXAPP");
//        sendBroadcast(broadcastIntent);
        setContentView(R.layout.activity_main);
        tabLayout = (TabLayout) findViewById(R.id.main_tabLayout);
        tabLayout.setTabMode(MODE_FIXED);
        tabLayout.setTabGravity(GRAVITY_FILL);
        setTabs(tabLayout, this.getLayoutInflater(), TAB_TITLES, TAB_IMGS);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                changFragment(position);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        Intent intent = new Intent(this, WebScketService.class);
        this.bindService(intent, connection, ContextWrapper.BIND_AUTO_CREATE);
        subscribe = RxBus.getInstance().toObserverable(Message.class)
                .compose(this.<Message>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Message>() {
                    @Override
                    public void call(Message message) {
                        if (message.getMessage().contains("停止充电"))
                            UiUtils.showToast("停止充电");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
        initFragment();
        PgyUpdateManager.register(this,"net.suntrans.bikecharge.fileProvider");
    }

    private void initFragment() {
        ChargeFragment fragment = new ChargeFragment();
        EnergyProfilerFragment fragment3 = new EnergyProfilerFragment();
        MapFragment fragment1 = new MapFragment();
        PersonFragment fragment2 = new PersonFragment();
        fragments = new Fragment[]{fragment, fragment1, fragment2};
        getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
//        String registrationID = JPushInterface.getRegistrationID(this);
//        System.out.println(registrationID);
    }


    int currentIndex = 0;

    private void changFragment(int index) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.hide(fragments[currentIndex]);
        if (!fragments[index].isAdded()) {
            transaction.add(R.id.content, fragments[index]);
        }
        transaction.show(fragments[index]).commit();
        currentIndex = index;
    }

    /**
     * @description: 设置添加Tab
     */
    private void setTabs(TabLayout tabLayout, LayoutInflater inflater, int[] tabTitlees, int[] tabImgs) {
        for (int i = 0; i < tabImgs.length; i++) {
            TabLayout.Tab tab = tabLayout.newTab();
            View view = inflater.inflate(R.layout.tab, null);
            tab.setCustomView(view);

            TextView tvTitle = (TextView) view.findViewById(R.id.tv_tab);
            ImageView imgTab = (ImageView) view.findViewById(R.id.img_tab);

            tvTitle.setText(tabTitlees[i]);
            imgTab.setImageResource(tabImgs[i]);

            tabLayout.addTab(tab);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        if (connection != null) {
            unbindService(connection);
        }
        if (!subscribe.isUnsubscribed()) {
            subscribe.unsubscribe();
        }
        PgyUpdateManager.unregister();
        super.onDestroy();
    }

    private long[] mHits = new long[2];

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
            mHits[mHits.length - 1] = SystemClock.uptimeMillis();
            if (mHits[0] >= (SystemClock.uptimeMillis() - 2000)) {
                android.os.Process.killProcess(android.os.Process.myPid());
            } else {
                UiUtils.showToast("再按一次退出");
            }
            return true;

        }
        return super.onKeyDown(keyCode, event);
    }


}
