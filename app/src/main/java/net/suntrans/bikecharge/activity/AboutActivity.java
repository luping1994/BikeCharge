package net.suntrans.bikecharge.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import net.suntrans.bikecharge.BuildConfig;
import net.suntrans.bikecharge.R;

/**
 * Created by Looney on 2017/5/28.
 */

public class AboutActivity extends BasedActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        TextView version = (TextView) findViewById(R.id.version);
        version.setText("三川智充"+BuildConfig.VERSION_NAME);
        findViewById(R.id.backRl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
