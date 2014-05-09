package com.wangxl;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private Button mButton;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mButton = (Button) findViewById(R.id.btn);
        mButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(MainActivity.this, "click listener ", Toast.LENGTH_SHORT).show();
			}
		});
    }

	@Override
	protected void onResume() {
		ProximitySensorManager.startProximitySensorForActivity(this);
		super.onResume();
	}
    
	@Override
	protected void onPause() {
		ProximitySensorManager.stopProximitySensorForActivity(this);
		super.onPause();
	}
    
}