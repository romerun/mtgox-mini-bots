package com.romerun.trading.mtgox;

import com.beust.jcommander.IStringConverter;

public final class TriggerConverter implements IStringConverter<Trigger> {
	@Override
	public Trigger convert(String value) {
		String[] s = value.split(":");
		
		return (new Trigger(s[0],Double.parseDouble(s[1]),Double.parseDouble(s[2])));
	}
}
