package com.yiitz.battery;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.Build.VERSION;

public class BatteryService extends Service {

	private IBatteryStats batteryStats;
	
	private IBatteryService.Stub binder = new IBatteryService.Stub() {
		
		@Override
		public long computeChargeTimeRemaining() throws RemoteException {
			return batteryStats.computeChargeTimeRemaining();
		}
		
		@Override
		public long computeBatteryTimeRemaining() throws RemoteException {
			return batteryStats.computeBatteryTimeRemaining();
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		batteryStats = getProperBatteryStats();
	}

	private IBatteryStats getProperBatteryStats() {

		if (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
			try {
				return new BatteryStatsNew(getApplicationContext());
			} catch (Exception e) {
				e.printStackTrace();
				return new BatteryStatsLegacy(getApplicationContext());
			}
		} else {
			return new BatteryStatsLegacy(getApplicationContext());
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		batteryStats.stop();
	}
}
