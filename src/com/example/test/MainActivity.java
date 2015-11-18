package com.example.test;

import java.util.List;

import com.yiitz.battery.IBatteryService;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.TextView;

public class MainActivity extends Activity {

	TextView textView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		textView = (TextView) findViewById(R.id.textView);

		Intent intent = createExplicitFromImplicitIntent(getApplicationContext(), new Intent("com.yiitz.battery.BatteryService"));
		startService(intent);
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
	}

	private IBatteryService batteryService;
	private ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			batteryService = null;
			unregisterReceiver(batteryChangedReceiver);
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			batteryService = IBatteryService.Stub.asInterface(service);
			registerReceiver(batteryChangedReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		}
	};

	@Override
	protected void onDestroy() {
		if (null != batteryService) {
			unbindService(conn);
		}
		super.onDestroy();
	}

	private BroadcastReceiver batteryChangedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			try {
				refreshBatteryInfo();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	};

	public void refreshBatteryInfo() throws RemoteException {
		textView.setText(getString(R.string.discharge_remain) + batteryService.computeBatteryTimeRemaining()
				+ "ms\n"+getString(R.string.charge_remain) + batteryService.computeChargeTimeRemaining()+"ms");
	}
	
	/***
     * Android L (lollipop, API 21) introduced a new problem when trying to invoke implicit intent,
     * "java.lang.IllegalArgumentException: Service Intent must be explicit"
     *
     * If you are using an implicit intent, and know only 1 target would answer this intent,
     * This method will help you turn the implicit intent into the explicit form.
     *
     * Inspired from SO answer: http://stackoverflow.com/a/26318757/1446466
     * @param context
     * @param implicitIntent - The original implicit intent
     * @return Explicit Intent created from the implicit original intent
     */
    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
 
        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }
 
        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);
 
        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);
 
        // Set the component to be explicit
        explicitIntent.setComponent(component);
 
        return explicitIntent;
    }
}
