package jp.azisaba.main.survival.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;

public class LogWriter {

	private File logFile;

	public LogWriter(File logFile) {
		this.logFile = logFile;

		if (!this.logFile.exists()) {
			try {
				this.logFile.getParentFile().mkdirs();
				this.logFile.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void writeLine(String data) {

		if (!logFile.exists()) {
			return;
		}

		String str = read();
		str += getDate() + " " + data;
		writeText(str);
	}

	public void writeError(Exception e) {
		if (!logFile.exists()) {
			return;
		}

		String str = read();

		String date = getDate();

		StringBuilder builder = new StringBuilder();

		for (StackTraceElement elem : e.getStackTrace()) {
			builder.append(date + convertStringFromElem(elem));
		}

		StringBuilder builder2 = new StringBuilder();

		int i = 0;
		for (StackTraceElement elem : e.getCause().getStackTrace()) {
			builder2.append(date + convertStringFromElem(elem));

			i++;

			if (elem.toString().startsWith("org.bukkit.")) {
				break;
			}
		}

		builder2.append(ChatColor.GRAY + StringUtils.repeat(" ", 8) + "... " + (e.getCause().getStackTrace().length - i)
				+ " more");

		String txt = builder.toString();
		txt += "Caused by: " + ChatColor.RED + e.getCause().toString() + getEndOfLine();
		txt += builder2.toString();

		writeText(str + txt);
	}

	private String convertStringFromElem(StackTraceElement elem) {
		StringBuilder builder = new StringBuilder(StringUtils.repeat(" ", 8) + "at ");

		builder.append(elem.toString());
		builder.append("\n");

		return builder.toString();
	}

	private void writeText(String data) {
		try {
			FileWriter fw = new FileWriter(logFile);
			fw.write(data);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String read() {
		String data = null;
		StringBuilder sb = new StringBuilder();

		String lineSeparator = System.getProperty("line.separator");

		try {
			BufferedReader br = new BufferedReader(new FileReader(logFile));
			while ((data = br.readLine()) != null) {
				sb.append(data);
				sb.append(lineSeparator);
			}
			data = sb.toString();
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	private String getDate() {

		Calendar cal = Calendar.getInstance();

		SimpleDateFormat sdf = new SimpleDateFormat("[1] h/m/s S");

		sdf.applyPattern("[MM/dd HH:mm:ss]");
		return sdf.format(cal.getTime());
	}

	public String getEndOfLine() {
		return "\r\n";
	}
}
