package com.yiitz.battery;

interface IBatteryService{
	long computeBatteryTimeRemaining();
	long computeChargeTimeRemaining();
}