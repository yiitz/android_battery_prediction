package com.yiitz.battery;

interface IBatteryStats {
	public long computeBatteryTimeRemaining();
	public long computeChargeTimeRemaining();
	
	public void stop();
}
