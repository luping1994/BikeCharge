package net.suntrans.bikecharge.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Looney on 2016/12/27.
 */

public class BasedActivity extends RxAppCompatActivity {
    public final static List<BasedActivity> mList = new LinkedList<BasedActivity>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        synchronized (mList) {
            mList.add(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        synchronized (mList) {
            mList.remove(this);
        }
    }


    public void killAll() {
        // 复制了一份mActivities 集合
        List<BasedActivity> copy;
        synchronized (mList) {
            copy = new LinkedList<BasedActivity>(mList);
        }
        for (BasedActivity activity : copy) {
            activity.finish();
        }

    }

    @Override
    public void finish() {
        super.finish();
    }
}
