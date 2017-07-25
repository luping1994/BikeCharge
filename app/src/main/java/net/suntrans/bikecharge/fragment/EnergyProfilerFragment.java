package net.suntrans.bikecharge.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import net.suntrans.bikecharge.R;
import net.suntrans.bikecharge.view.NowHwWeatherView;
/**
 * Created by Looney on 2017/5/31.
 */

public class EnergyProfilerFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_energy, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initView(view);
    }

    private void initView(View view) {
//        view.findViewById(R.id.backRl).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                getActivity().finish();
//            }
//        });

        final NowHwWeatherView hwWeatherView = (NowHwWeatherView) view.findViewById(R.id.scanView);
        hwWeatherView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hwWeatherView.startScan();
            }
        });
    }

}
