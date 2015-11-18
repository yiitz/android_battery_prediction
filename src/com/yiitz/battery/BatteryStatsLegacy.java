package com.yiitz.battery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.SystemClock;

class BatteryStatsLegacy implements IBatteryStats {
	private boolean isCharging;

	private int mLastDischargeStepLevel = -1;
	private int mLastChargeStepLevel = -1;

	private int level = -1;
	private final int maxLevel;

	final LevelStepTracker mDischargeStepTracker = new LevelStepTracker();
	final LevelStepTracker mChargeStepTracker = new LevelStepTracker();
	
	private Context context;

	public BatteryStatsLegacy(Context context) {
		this.context = context;
		Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
		maxLevel = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
		isCharging = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0;
		if (isCharging) {
			mLastChargeStepLevel = level;
		} else {
			mLastDischargeStepLevel = level;
		}
		
		registerReceiver();
	}

	@Override
	public long computeBatteryTimeRemaining() {
		return level * mDischargeStepTracker.computeTimePerLevel();
	}

	@Override
	public long computeChargeTimeRemaining() {
		return (maxLevel - level) * mChargeStepTracker.computeTimePerLevel();
	}
	
	private void registerReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
		intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
		
		context.registerReceiver(batteryChangedReceiver, intentFilter);
	}
	
	private void unregisterReceiver() {
		context.unregisterReceiver(batteryChangedReceiver);
	}

	private BroadcastReceiver batteryChangedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			String action = intent.getAction();
			level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
			
			if (action == Intent.ACTION_POWER_CONNECTED) {
				onPowerConnected();
			} else if (action == Intent.ACTION_POWER_DISCONNECTED) {
				onPowerDisconnected();
			} else if (action == Intent.ACTION_BATTERY_CHANGED) {
				onPowerChanged();
			}
		}
	};

	private void onPowerConnected() {
		isCharging = true;
		mLastChargeStepLevel = level;
		mChargeStepTracker.init();
	}
	
	private void onPowerDisconnected() {
		isCharging = false;
		mLastDischargeStepLevel = level;
		if (shouldResetState()) {
			mDischargeStepTracker.init();
		}
	}
	
	private boolean shouldResetState(){
		return level >= 90 || (mLastChargeStepLevel <=20 && level >= 80);
	}
	
	private void onPowerChanged() {
		if (isCharging) {
			if (mLastChargeStepLevel < level) {
				mChargeStepTracker.addLevelSteps(level - mLastChargeStepLevel, SystemClock.elapsedRealtime());
				mLastChargeStepLevel = level;
			}
		} else {
			if (mLastDischargeStepLevel > level) {
				mDischargeStepTracker.addLevelSteps(mLastDischargeStepLevel - level,
						SystemClock.elapsedRealtime());
				mLastDischargeStepLevel = level;
			}
		}
	}

	@Override
	public void stop() {
		unregisterReceiver();
	}
}
