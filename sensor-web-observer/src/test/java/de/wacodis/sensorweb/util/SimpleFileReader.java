package de.wacodis.sensorweb.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class SimpleFileReader {
	
	public static String readFile(String path) {
		StringBuilder builder = new StringBuilder();
		String line = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
			while((line = reader.readLine()) != null) {
				builder.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return builder.toString();
		
	}

}
