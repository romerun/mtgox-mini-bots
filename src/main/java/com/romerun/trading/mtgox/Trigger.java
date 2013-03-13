package com.romerun.trading.mtgox;

public final class Trigger {
  String name;
	double percent;
	double size;
	String stopOrderId;
	
	Trigger (String name, double percent, double size) {
	  this.name = name;
		this.percent = percent;
		this.size = size;
	}
}
