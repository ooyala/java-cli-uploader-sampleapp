package com.ooyala.sample;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {

	/**
	 * Prints final message, appending date-time
	 * @param level
	 * @param message
	 */
	private void print(String level, String message) {
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss,SSS");
		System.out.println(level + " " + ft.format(dNow) + ": " + message);
	}

	/**
	 * Trace Logging Level
	 * @param message
	 */
	public void t(String message) {
		print("TRACE", message);
	}

	/**
	 * Debug Logging Level
	 * @param message
	 */
	public void d(String message) {
		print("DEBUG", message);
	}

	/**
	 * Info Logging Level
	 * @param message
	 */
	public void i(String message) {
		print("INFO", message);
	}

	/**
	 * Warning Logging Level
	 * @param message
	 */
	public void w(String message) {
		print("WARN", message);
	}

	/**
	 * Error Logging Level
	 * @param message
	 */
	public void e(String message) {
		print("ERROR", message);
	}
}