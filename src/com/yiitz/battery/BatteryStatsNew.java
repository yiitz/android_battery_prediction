package com.yiitz.battery;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;

public class BatteryStatsNew implements IBatteryStats {
	private Object batteryStatsStubObject;
	private Class<?> batteryStatsStubClass;

	public BatteryStatsNew(Context context) throws NoSuchMethodException, ClassNotFoundException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		Class<?> serviceManagerClass = Class.forName("android.os.ServiceManager");
		Method getServiceMethod = serviceManagerClass.getMethod("getService", java.lang.String.class);
		Object batteryStatsService = getServiceMethod.invoke(null, "batterystats");

		batteryStatsStubClass = Class.forName("com.android.internal.app.IBatteryStats$Stub");
		batteryStatsStubObject = batteryStatsStubClass.getMethod("asInterface", android.os.IBinder.class).invoke(null,
				batteryStatsService);
	}

	@Override
	public long computeBatteryTimeRemaining() {
		try {
			long time = (Long) batteryStatsStubClass.getMethod("computeBatteryTimeRemaining").invoke(batteryStatsStubObject);
			return time;
		} catch (Exception e) {
			return -1;
		}
	}

	@Override
	public long computeChargeTimeRemaining() {
		try {
			long time = (Long) batteryStatsStubClass.getMethod("computeChargeTimeRemaining").invoke(batteryStatsStubObject);
			return time;
		} catch (Exception e) {
			return -1;
		}
	}

	@Override
	public void stop() {
	}

}
