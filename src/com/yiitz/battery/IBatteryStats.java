package com.yiitz.battery;

public interface IBatteryStats {
	public long computeBatteryTimeRemaining();
	public long computeChargeTimeRemaining();
	
	public void stop();
}
