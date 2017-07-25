package net.suntrans.bikecharge.maputils.bean;

import com.amap.api.maps.model.LatLng;

import net.suntrans.bikecharge.maputils.ClusterItem;

/**
 * Created by yiyi.qi on 16/10/10.
 */

public class RegionItem implements ClusterItem {
    private LatLng mLatLng;
    private String mTitle;
    private String info;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    private String address;
    public RegionItem(LatLng latLng, String title) {
        mLatLng=latLng;
        mTitle=title;
    }

    @Override
    public LatLng getPosition() {
        // TODO Auto-generated method stub
        return mLatLng;
    }
    public String getTitle(){
        return mTitle;
    }

}
