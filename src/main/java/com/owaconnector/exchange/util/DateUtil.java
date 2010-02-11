package com.owaconnector.exchange.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;

public class DateUtil {

	/**
	 * Reference GMT timezone to format dates
	 */
	public static final SimpleTimeZone GMT_TIMEZONE = new SimpleTimeZone(0,
			"GMT");
	
	private static final String YYYY_MM_DD_HH_MM_SS = "yyyy/MM/dd HH:mm:ss";
	private static final String YYYYMMDD_T_HHMMSS_Z = "yyyyMMdd'T'HHmmss'Z'";
	private static final String YYYY_MM_DD_T_HHMMSS_Z = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	private static final String YYYY_MM_DD_T_HHMMSS_SSS_Z = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	public static String formatSearchDate(Date date) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(
				YYYY_MM_DD_HH_MM_SS, Locale.ENGLISH);
		dateFormatter.setTimeZone(GMT_TIMEZONE);
		return dateFormatter.format(date);
	}

	public static SimpleDateFormat getZuluDateFormat() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(YYYYMMDD_T_HHMMSS_Z,
				Locale.ENGLISH);
		dateFormat.setTimeZone(GMT_TIMEZONE);
		return dateFormat;
	}

	public static SimpleDateFormat getExchangeZuluDateFormat() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				YYYY_MM_DD_T_HHMMSS_Z, Locale.ENGLISH);
		dateFormat.setTimeZone(GMT_TIMEZONE);
		return dateFormat;
	}

	public static SimpleDateFormat getExchangeZuluDateFormatMillisecond() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				YYYY_MM_DD_T_HHMMSS_SSS_Z, Locale.ENGLISH);
		dateFormat.setTimeZone(GMT_TIMEZONE);
		return dateFormat;
	}

	public static Date parseDate(String dateString) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		dateFormat.setTimeZone(GMT_TIMEZONE);
		return dateFormat.parse(dateString);
	}

}
