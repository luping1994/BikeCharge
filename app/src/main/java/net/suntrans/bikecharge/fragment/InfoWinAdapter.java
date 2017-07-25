package net.suntrans.bikecharge.fragment;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;

import net.suntrans.bikecharge.App;
import net.suntrans.bikecharge.R;
import net.suntrans.bikecharge.activity.StationDetailActivity;

import android.content.Context;

/**
 * Created by Looney on 2017/6/8.
 */

class InfoWinAdapter implements AMap.InfoWindowAdapter, View.OnClickListener {
    private Context mContext;
    private LatLng latLng;
    private LinearLayout call;
    private LinearLayout navigation;
    private TextView nameTV;
    private TextView detail;
    private String agentName;
    private TextView addrTV;
    private String snippet;
    private String sensus_id;

    public InfoWinAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        initData(marker);
        View view = initView();
        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    private void initData(Marker marker) {
        latLng = marker.getPosition();
        snippet = marker.getSnippet();
        agentName = marker.getTitle();
        sensus_id = (String) marker.getObject();
    }

    @NonNull
    private View initView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.windowinfo, null);

        nameTV = (TextView) view.findViewById(R.id.name);
        addrTV = (TextView) view.findViewById(R.id.addr);
        detail = (TextView) view.findViewById(R.id.detail);

        nameTV.setText(agentName);
        addrTV.setText("地址:" + snippet);

        detail.setOnClickListener(this);
//        call.setOnClickListener(this);
        return view;
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.detail:  //点击导航
                Intent intent = new Intent(mContext, StationDetailActivity.class);
                intent.putExtra("id",sensus_id);
                mContext.startActivity(intent);
                break;
//
//            case R.id.call_LL:  //点击打电话
//                break;
        }
    }
}
