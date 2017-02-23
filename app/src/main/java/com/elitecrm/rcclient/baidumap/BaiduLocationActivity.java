package com.elitecrm.rcclient.baidumap;

import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.WindowCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.elitecrm.rcclient.R;

import java.util.ArrayList;
import java.util.List;

import io.rong.message.LocationMessage;

/**
 * Created by Loriling on 2017/2/22.
 */

public class BaiduLocationActivity extends AppCompatActivity {
    public static final int RESULT_CODE = 6;
    MapView mMapView;
    BaiduMap baiduMap;
    private double longitude;
    private double latitude;
    LatLng mLoactionLatLng;
    private String address;
    LocationClient locationClient;
    LocationMessage mMsg;
    boolean isFirstLoc = true;
    Point mCenterPoint;
    GeoCoder mGeoCoder;
    List<PoiInfo> mInfoList;
    PoiInfo mCurentInfo;
    ListView Maplistview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置窗口风格为顶部显示Actionbar
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true); // 决定左上角图标的右侧是否有向左的小箭头, true
        // 有小箭头，并且图标可以点击
        actionBar.setDisplayShowHomeEnabled(false);
        // 使左上角图标是否显示，如果设成false，则没有程序图标，仅仅就个标题，
        // 否则，显示应用程序图标，对应id为android.R.id.home，对应ActionBar.DISPLAY_SHOW_HOME

        setContentView(R.layout.baidulocation);
        initview();
        getimgxy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:// 点击返回图标事件
                this.finish();
            case R.id.action_confirm:
                Uri uri = Uri.parse("http://api.map.baidu.com/staticimage?width=300&height=200&center="+ longitude + "," + latitude + "&zoom=17&markers=" + longitude + "," + latitude + "&markerStyles=m,A");
                Intent intent = new Intent();
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                intent.putExtra("address",address);
                intent.putExtra("locuri",uri.toString());
                setResult(RESULT_CODE , intent);
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topactionbar_menu, menu);
        return true;
    }

    private void initview() {
        setTitle("地理位置");
        mMapView = (MapView) findViewById(R.id.bmapView);
        mMapView.showZoomControls(false);
        baiduMap = mMapView.getMap();
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(17.0f);
        baiduMap.setMapStatus(msu);
        //开启定位图层
        baiduMap.setMyLocationEnabled(true);
        baiduMap.setOnMapTouchListener(touchListener);
        try {
            if (getIntent().hasExtra("location")) {
                mMsg = getIntent().getParcelableExtra("location");
            }
            if (mMsg != null) {
                Maplistview.setVisibility(View.GONE);
                locationClient = new LocationClient(getApplicationContext()); // 实例化LocationClient类
                MyLocationData locData = new MyLocationData.Builder()
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(100).latitude(mMsg.getLat())
                        .longitude(mMsg.getLng()).build();
                baiduMap.setMyLocationData(locData);    //设置定位数据
                mLoactionLatLng = new LatLng(mMsg.getLat(),
                        mMsg.getLng());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(mLoactionLatLng, 16);    //设置地图中心点以及缩放级别
                baiduMap.animateMapStatus(u);
            } else {
                locationClient = new LocationClient(getApplicationContext()); // 实例化LocationClient类
                locationClient.registerLocationListener(myListener); // 注册监听函数
                this.setLocationOption();    //设置定位参数
                locationClient.start(); // 开始定位
            }
        } catch (Exception e) {
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            locationClient.stop();
            Log.d("stop", "定位关闭");
            finish();
        }
        return false;
    }
    /**
     * 设置定位参数
     */
    private void setLocationOption() {
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开GPS
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
        option.setCoorType("bd09ll"); // 返回的定位结果是百度经纬度,默认值gcj02
        option.setScanSpan(5000); // 设置发起定位请求的间隔时间为5000ms
        option.setIsNeedAddress(true); // 返回的定位结果包含地址信息
        option.setNeedDeviceDirect(true); // 返回的定位结果包含手机机头的方向
        locationClient.setLocOption(option);
    }
    public BDLocationListener myListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null)
                return;
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            address = location.getAddrStr();
            MyLocationData locData = new MyLocationData.Builder()
            /*.accuracy(location.getRadius())*/
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            baiduMap.setMyLocationData(locData);    //设置定位数据
            if (isFirstLoc) {
                isFirstLoc = false;
                mLoactionLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(mLoactionLatLng, 20);//设置地图中心点以及缩放级别
                baiduMap.animateMapStatus(u);
            }
            // 获取当前MapView中心屏幕坐标对应的地理坐标
            baiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    LatLng currentLatLng;
                    currentLatLng = baiduMap.getProjection().fromScreenLocation(mCenterPoint);
                    // 发起反地理编码检索
                    mGeoCoder.reverseGeoCode((new ReverseGeoCodeOption()).location(currentLatLng));
                }
            });

        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    };
    /**
     * 初始化地图物理坐标
     */
    private void getimgxy() {
        // 初始化POI信息列表
        mInfoList = new ArrayList<PoiInfo>();
        mCenterPoint = baiduMap.getMapStatus().targetScreen;
        mLoactionLatLng = baiduMap.getMapStatus().target;
        // 地理编码
        mGeoCoder = GeoCoder.newInstance();
        mGeoCoder.setOnGetGeoCodeResultListener(GeoListener);
    }
    // 地理编码监听器
    OnGetGeoCoderResultListener GeoListener = new OnGetGeoCoderResultListener() {
        public void onGetGeoCodeResult(GeoCodeResult result) {
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                // 没有检索到结果
            }
            // 获取地理编码结果
        }
        @Override
        public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                // 没有找到检索结果
            }
            // 获取反向地理编码结果
            else {
                // 当前位置信息
                mCurentInfo = new PoiInfo();
                mCurentInfo.address = result.getAddress();
                mCurentInfo.location = result.getLocation();
                mCurentInfo.name = "[位置]";
                mInfoList.clear();
                mInfoList.add(mCurentInfo);
                // 将周边信息加入表
                if (result.getPoiList() != null) {
                    mInfoList.addAll(result.getPoiList());
                }
            }
        }
    };
    // 地图触摸事件监听器
    BaiduMap.OnMapTouchListener touchListener = new BaiduMap.OnMapTouchListener() {
        @Override
        public void onTouch(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (mCenterPoint == null) {
                    return;
                }
                // 获取当前MapView中心屏幕坐标对应的地理坐标
//                LatLng currentLatLng;
//                currentLatLng = baiduMap.getProjection().fromScreenLocation(mCenterPoint);
                // 发起反地理编码检索
                // mGeoCoder.reverseGeoCode((new ReverseGeoCodeOption()).location(currentLatLng));

//                MyLocationData locData = new MyLocationData.Builder()
//                        .direction(100).latitude(currentLatLng.latitude)
//                        .longitude(currentLatLng.longitude).build();
//                baiduMap.setMyLocationData(locData);    //设置定位数据

//                BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.marker_blue);
//                MyLocationConfiguration config = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, mCurrentMarker);
//                baiduMap.setMyLocationConfigeration(config);
                // 当不需要定位图层时关闭定位图层
                //baiduMap.setMyLocationEnabled(false);
            }

        }
    };
}
