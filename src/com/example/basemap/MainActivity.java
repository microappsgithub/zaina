package com.example.basemap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import cn.bmob.push.BmobPush;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobInstallation;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;

public class MainActivity extends Activity {

	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private MapStatus mMapStatus;
	private Button btn;
	private View popup; // 地图弹出信息框组件
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		refershMapView();
		initBmob();
		regReceiver();
		btn = (Button)this.findViewById(R.id.btn);
		btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mBaiduMap.getMapType()==BaiduMap.MAP_TYPE_NORMAL){
					mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE); 	//卫星地图  
				} else {
					mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL); 	//普通地图  
				}
			}
		});
	}

	/**
	 * 初始化地图
	 * @param point 经纬度
	 * @param level 地图缩放级别
	 */
	private void initMap(final LatLng point,float level,final Map<String,String> params) {
		/**
		 * s1 此部分为初始化地图
		 */
		mMapView = (MapView) findViewById(R.id.bmapView);// 获取地图控件引用
		mMapView.showZoomControls(false); // 设置隐藏缩放控件
		mMapView.showScaleControl(false); //设置隐藏比例尺
		mBaiduMap = mMapView.getMap();
		mBaiduMap.clear();
		mMapStatus = new MapStatus.Builder()  
		.target(point)  
		.zoom(level) 
		.overlook(-45) // 定义俯视角度
		.build(); //定义地图状态  
		//定义MapStatusUpdate对象，以便描述地图状态将要发生的变化  
		MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);  
		/**
		 * s2 此部分为创建定位点
		 */
		mBaiduMap.addOverlay(new MarkerOptions().position(point)
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.ic_findphone_location)));
		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			
			@Override
			public boolean onMarkerClick(Marker arg0) {
				showPopup(point,params);
				return false;
			}
		});
		// 添加圆
//		OverlayOptions ooCircle = new CircleOptions().fillColor(0xe0F55043)
//				.center(point).stroke(new Stroke(1, 0xe0F55043))
//				.radius(300);
//		mBaiduMap.addOverlay(ooCircle);
//		// 添加折线
//		LatLng p1 = new LatLng(41.816925, 123.494002);
//		LatLng p2 = new LatLng(41.815057, 123.494846);
//		List<LatLng> points = new ArrayList<LatLng>();
//		points.add(p1);
//		points.add(p2);
//		points.add(point);
//		OverlayOptions ooPolyline = new PolylineOptions().width(10)
//				.color(0xAAFF0000).points(points);
//		mBaiduMap.addOverlay(ooPolyline);
		/**
		 * s3 此部分为创建地图定位弹出框
		 */
		showPopup(point,params);
		
		//改变地图状态  
		mBaiduMap.setMapStatus(mMapStatusUpdate); 
	}
	
	/**
	 * 显示地图定位弹出框
	 * @param point 定位点坐标对象
	 * @param params 显示信息集合
	 */
	private void showPopup(LatLng point,Map<String,String> params){
		popup = View.inflate(this, R.layout.pop, null); // 地图定位创建弹出框
		popup.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mBaiduMap.hideInfoWindow();
			}
		});
		String newAddr =  params.get("addr");
		newAddr = newAddr.substring(newAddr.indexOf("省")+1,newAddr.length());
		String content = params.get("date") + ",剩余电量 "+params.get("battery");
		((TextView)popup.findViewById(R.id.title)).setText(newAddr);
		((TextView)popup.findViewById(R.id.content)).setText(content);
		int yoffset = -100; //设置显示偏移量
		InfoWindow mInfoWindow = new InfoWindow(popup,point,yoffset);
		mBaiduMap.showInfoWindow(mInfoWindow);
	}
	
	/**
	 * 初始化Bmob推送组件
	 */
	private void initBmob(){
		// 初始化BmobSDK
		Bmob.initialize(this, "63b18905f672c1e4f75af68414e97514");
		// 使用推送服务时的初始化操作
		BmobInstallation.getCurrentInstallation(this).save();
		// 启动推送服务
		BmobPush.startWork(this, "63b18905f672c1e4f75af68414e97514");
	}
	
	/**
	 * 注册广播接收
	 */
	private void regReceiver(){
		DataReceiver dataReceiver = new DataReceiver();
		IntentFilter filter = new IntentFilter();//创建IntentFilter对象
		filter.addAction("com.example.bmobpushdemo.send");
		registerReceiver(dataReceiver, filter);//注册Broadcast Receiver
	}
	
	/**
	 * 刷新地图控件
	 */
	protected void refershMapView(){
		SharedPreferences preferences = this.getSharedPreferences("pushMessage", Context.MODE_PRIVATE);  
		String lon = preferences.getString("lon", "123.50969");  
		String lat = preferences.getString("lat", "41.814465"); 
		String addr = preferences.getString("addr", "北京天安门广场"); 
		String date = preferences.getString("date", "1949年10月1日"); 
		String battery = preferences.getString("battery", "剩余电量 0%"); 
		float level = 18;
		if(mBaiduMap != null){ // 判断是否为初始化调用
			level = mBaiduMap.getMapStatus().zoom;
		}
		Map<String,String> params = new HashMap<String,String>();
		params.put("addr", addr);
		params.put("date", date);
		params.put("battery", battery);
		initMap(new LatLng(Double.parseDouble(lat),Double.parseDouble(lon)),level,params);
	}

	private class DataReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if("com.example.bmobpushdemo.send".equals(intent.getAction())) {
				refershMapView();
			}
		}

	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		mMapView.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		mMapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		mMapView.onPause();
	}
	
	
}
