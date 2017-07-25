package net.suntrans.bikecharge.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import net.suntrans.bikecharge.R;
import net.suntrans.bikecharge.fragment.EnergyProfilerFragment;

/**
 * Created by Looney on 2017/5/31.
 */

public class EnergyProfilerActivity extends BasedActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_energy);
        EnergyProfilerFragment fragment = new EnergyProfilerFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.content,fragment).commit();
    }
}
