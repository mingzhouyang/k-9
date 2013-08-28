package org.ancode.secmail.activity;

import org.ancode.secmail.R;
import org.ancode.secmail.update.AppUpdate;
import org.ancode.secmail.update.AppUpdateService;
import org.ancode.secmail.update.internal.SimpleJSONParser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class UpdateActivity extends Activity {

	AppUpdate appUpdate;

	final static String UPDATE_URL = "http://www.gezimail.com/update/update.ini";

	public static void update(Context context) {
		Intent i = new Intent(context, UpdateActivity.class);
		context.startActivity(i);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.update);

		appUpdate = AppUpdateService.getAppUpdate(this);

		View check = findViewById(R.id.check);
		check.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 检查最新版本，并弹出窗口
				appUpdate.checkLatestVersion(UPDATE_URL, new SimpleJSONParser());
			}
		});

		View download = findViewById(R.id.download);
		download.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 无须提示，直接升级
				appUpdate.checkAndUpdateDirectly(UPDATE_URL, new SimpleJSONParser());
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		// ********
		appUpdate.callOnResume();
	}

	@Override
	protected void onPause() {
		super.onPause();

		// ********
		appUpdate.callOnPause();
	}

}
