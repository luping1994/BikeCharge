package net.suntrans.bikecharge.fragment;

import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.SupportMapFragment;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.trello.rxlifecycle.android.FragmentEvent;
import com.trello.rxlifecycle.components.support.RxFragment;

import net.suntrans.bikecharge.R;
import net.suntrans.bikecharge.api.RetrofitHelper;
import net.suntrans.bikecharge.bean.CStationResult;
import net.suntrans.bikecharge.maputils.ClusterClickListener;
import net.suntrans.bikecharge.maputils.ClusterItem;
import net.suntrans.bikecharge.maputils.ClusterOverlay;
import net.suntrans.bikecharge.maputils.ClusterRender;
import net.suntrans.bikecharge.maputils.bean.RegionItem;
import net.suntrans.bikecharge.utils.LogUtil;
import net.suntrans.bikecharge.utils.SensorEventHelper;
import net.suntrans.bikecharge.utils.UiUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Looney on 2017/5/25.
 */

public class MapFragment extends RxFragment implements ClusterRender, ClusterClickListener {

    private AMap aMap;
    private SensorEventHelper mSensorHelper;//手机传感器帮助类
    private int clusterRadius = 100;

    private Map<Integer, Drawable> mBackDrawAbles = new HashMap<Integer, Drawable>();

    private ClusterOverlay mClusterOverlay;
    private List<Marker> markers;
    private InfoWinAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        setHasOptionsMenu(true);
        initMap();
        return view;
    }

    private void initMap() {
        markers = new ArrayList<Marker>();


        if (aMap == null) {
            aMap = ((SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.map)).getMap();
            UiSettings uiSettings = aMap.getUiSettings();
            aMap.moveCamera(CameraUpdateFactory.zoomTo(16));
            uiSettings.setMyLocationButtonEnabled(true);
            uiSettings.setRotateGesturesEnabled(false);
            LatLng localLatLng = new LatLng(22.543527, 114.057939);
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localLatLng, 13));
        }
        aMap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
            @Override
            public void onMapLoaded() {
//                checkPermission();
                initLocation();

            }
        });
        adapter = new InfoWinAdapter(getActivity());
        aMap.setInfoWindowAdapter(adapter);
        mSensorHelper = new SensorEventHelper(getContext());
        if (mSensorHelper != null) {
            mSensorHelper.registerSensorListener();
        }

        aMap.setOnMapClickListener(new AMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                for (Marker marker : markers) {
                    marker.hideInfoWindow();
                }
            }
        });

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {


    }

    @Override
    public void onResume() {

        super.onResume();
    }

    private static final int RC_LOCATION_PERM = 125;



    private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);

    private void initLocation() {
        MyLocationStyle myLocationStyle;
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.interval(10000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
//        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW);//定位一次，且将视角移动到地图中心点。
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked));
        myLocationStyle.radiusFillColor(FILL_COLOR);
        myLocationStyle.strokeColor(STROKE_COLOR);
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
//aMap.getUiSettings().setMyLocationButtonEnabled(true);设置默认定位按钮是否显示，非必需设置。
        aMap.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                getStationData();

                LogUtil.i("位置信息如下：" + location.getLatitude() + "," + location.getLongitude());
            }
        });
        aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String id = (String) marker.getObject();
                marker.showInfoWindow();
                return true;
            }
        });
        getStationData();


    }


    private void getStationData() {
        if (markers.size()>1){
            return;
        }
        LogUtil.e("开始获取信息");
        RetrofitHelper.getApi().getChargeStation()
                .compose(this.<CStationResult>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<CStationResult>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        UiUtils.showToast("获取充电站信息失败");
                    }

                    @Override
                    public void onNext(CStationResult result) {

                        if (result.status.equals("1")) {
//                            List<ClusterItem> items = new ArrayList<ClusterItem>();
                            if (markers != null) {
                                for (Marker marker : markers) {
                                    marker.hideInfoWindow();
                                    marker.remove();
                                }
                            }
                            for (CStationResult.CStation s :
                                    result.result.sites) {
                                LatLng latLng = new LatLng(Float.valueOf(s.latitude), Float.valueOf(s.longitude));
                                MarkerOptions options = new MarkerOptions();
                                options.title(s.name)
                                        .snippet(s.address)
                                        .position(latLng);
                                Marker marker = aMap.addMarker(options);
                                marker.setObject(s.sensus_id);
                                if (markers == null)
                                    markers = new ArrayList<Marker>();
                                markers.add(marker);

//                                LatLng latLng = new LatLng(Float.valueOf(s.latitude), Float.valueOf(s.longitude));
//                                RegionItem regionItem = new RegionItem(latLng, s.name);
//                                regionItem.setAddress(s.address);
//                                items.add(regionItem);

                            }
//                            if (mClusterOverlay != null) {
//                                mClusterOverlay.onDestroy();
//                                mClusterOverlay = null;
//                            }
//                            mClusterOverlay = new ClusterOverlay(aMap, items,
//                                    UiUtils.dip2px(clusterRadius, getContext()),
//                                    getContext());
//                            mClusterOverlay.setClusterRenderer(MapFragment.this);
//                            mClusterOverlay.setOnClusterClickListener(MapFragment.this);

                        } else {
                            UiUtils.showToast("暂无充电站信息");
                        }
                    }
                });
    }




    @Override
    public void onDestroy() {
        if (mClusterOverlay != null) {
            mClusterOverlay.onDestroy();
        }
        if (markers != null) {
            markers.clear();
        }
        super.onDestroy();
    }


    @Override
    public Drawable getDrawAble(int clusterNum) {
        int radius = UiUtils.dip2px(80, getActivity().getApplicationContext());
        if (clusterNum == 1) {
            Drawable bitmapDrawable = mBackDrawAbles.get(1);
            if (bitmapDrawable == null) {
                bitmapDrawable =
                        getContext().getResources().getDrawable(
                                R.drawable.icon_openmap_mark);
                mBackDrawAbles.put(1, bitmapDrawable);
            }

            return bitmapDrawable;
        } else {
            Drawable bitmapDrawable = mBackDrawAbles.get(2);
            if (bitmapDrawable == null) {
                bitmapDrawable = new BitmapDrawable(null, UiUtils.drawCircle(radius,
                        Color.argb(159, 210, 154, 6)));
                mBackDrawAbles.put(2, bitmapDrawable);
            }

            return bitmapDrawable;
        }
    }

    @Override
    public void onClick(Marker marker, List<ClusterItem> clusterItems) {
        if (clusterItems.size() == 1) {
            RegionItem item = (RegionItem) clusterItems.get(0);
            marker.setTitle(item.getTitle());
            marker.showInfoWindow();
        } else {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (ClusterItem clusterItem : clusterItems) {
                builder.include(clusterItem.getPosition());
            }
            LatLngBounds latLngBounds = builder.build();
            aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 0)
            );
        }
    }
}
