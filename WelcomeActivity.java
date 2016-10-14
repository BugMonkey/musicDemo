package cn.tedu.media_player_v4.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import cn.tedu.media_player_v4.R;

public class WelcomeActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		Handler hander = new Handler() ;
			
		hander.postDelayed(new Runnable() {

			@Override
			public void run() {
               Intent intent=new Intent(WelcomeActivity.this,MainActivity.class);
               startActivity(intent);
               finish();
			}
		}, 1000);

	}
}
