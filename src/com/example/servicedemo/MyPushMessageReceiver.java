package com.example.servicedemo;

import org.json.JSONException;
import org.json.JSONObject;

import cn.bmob.push.PushConstants;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.widget.Toast;

public class MyPushMessageReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(PushConstants.ACTION_MESSAGE)){
			Log.d("bmob", "客户端收到推送内容"+intent.getStringExtra(PushConstants.EXTRA_PUSH_MESSAGE_STRING));
			Toast.makeText(context, intent.getStringExtra(PushConstants.EXTRA_PUSH_MESSAGE_STRING), 1).show();
			try {
				// 1.写文件
				SharedPreferences preferences = context.getSharedPreferences("pushMessage", Context.MODE_PRIVATE);  
				// 获取编辑器  
				Editor editor = preferences.edit();  
				// 通过editor进行设置 
				JSONObject person = new JSONObject(intent.getStringExtra(PushConstants.EXTRA_PUSH_MESSAGE_STRING));
				editor.putString("lon", person.getString("lon"));
				editor.putString("lat", person.getString("lat"));
				editor.putString("addr", person.getString("addr"));
				editor.putString("date", person.getString("date"));
				editor.putString("battery", person.getString("battery"));
				// 提交修改, 将数据写出到文件 
				editor.commit();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			// 2.传递信息
			Intent myIntent = new Intent();
			myIntent.setAction("com.example.bmobpushdemo.send");
			context.sendBroadcast(myIntent);

		}
	}

}
