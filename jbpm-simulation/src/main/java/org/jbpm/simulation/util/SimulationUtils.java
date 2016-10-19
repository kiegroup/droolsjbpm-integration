/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.jbpm.simulation.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.DurationFormatUtils;

public class SimulationUtils {

	public static final double HUNDRED = 100;
	public static final  NumberFormat formatter = new DecimalFormat("#0.00");
	
	public static HashMap<String, String> timeUnitMapping = new HashMap<String, String>();
	
	static {
	    timeUnitMapping.put("ms", "milliseconds");
	    timeUnitMapping.put("min", "minutes");
	    timeUnitMapping.put("s", "seconds");
	    timeUnitMapping.put("hour", "hours");
	    timeUnitMapping.put("day", "days");
	    timeUnitMapping.put("year", "years");
	}

	private SimulationUtils() {}

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
		String timeUnitStr = timeUnitMapping.get((String) element.get(SimulationConstants.TIMEUNIT));
		
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
