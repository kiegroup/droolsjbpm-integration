package org.jbpm.simulation.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.time.DurationFormatUtils;

public class SimulatiorUtils {
	
	public static final double HUNDRED = 100;
	public static final  NumberFormat formatter = new DecimalFormat("#0.00");
	
	
	public static final String EXECUTED_INSTANCES = "executed.instances";

	public static int asInt(Object value) {
		if (value == null) {
			return -1;
		} else {
			return Integer.parseInt(value.toString());
		}
	}
	
	public static double asDouble(Object value) {
		if (value == null) {
			return -1;
		} else {
			return Double.parseDouble(value.toString());
		}
	}
	
	public static long asLong(Object value) {
		if (value == null) {
			return -1;
		} else {
			return Long.parseLong(value.toString());
		}
	}
	
	
	public static TimeUnit getTimeUnit(Map<String, Object> element) {
		String timeUnitStr = (String) element.get("");
		if (timeUnitStr != null) {
			try {
				TimeUnit durationTimeUnit = TimeUnit.valueOf(timeUnitStr.toUpperCase());
				
				return durationTimeUnit;
			} catch (Exception e) {
				
				return TimeUnit.MILLISECONDS;
			}
		} else {
			return TimeUnit.MILLISECONDS;
		}
	}
	
	
	public static String formatDuration(Double timeInMiliseconds) {
		return formatDuration(timeInMiliseconds.longValue());
	}
	
	public static String formatDuration(long timeInMiliseconds) {
		return DurationFormatUtils.formatDurationWords(timeInMiliseconds, true, true);
	}
	
	public static String formatDouble(double value) {
		
		return formatter.format(value);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getProperty(String property, Map<String, ? extends Object> settings, Class<T> classOfProperty, T defaultValue) {
		if (settings != null && settings.containsKey(property)) {
			return (T) settings.get(property);
		} else {
			return defaultValue;
		}
	}
}
