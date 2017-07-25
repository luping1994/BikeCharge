package net.suntrans.bikecharge.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import net.suntrans.bikecharge.R;
import net.suntrans.bikecharge.api.RetrofitHelper;
import net.suntrans.bikecharge.bean.MSG;

import rx.Subscriber;


/**
 * Created by Looney on 2017/5/31.
 */

public class DealActivity extends BasedActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);
        init();
    }

    private void init() {
        findViewById(R.id.backRl)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getData();
    }

    private void getData() {
        RetrofitHelper.getApi().getBill()
                .subscribe(new Subscriber<MSG>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(MSG msg) {

                    }
                });
    }
}
