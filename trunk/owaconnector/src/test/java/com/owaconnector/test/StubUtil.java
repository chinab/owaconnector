package com.owaconnector.test;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public final class StubUtil {

	public static final StringBuilder readFile(File file) {
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		StringBuilder builder = null;
		try {
			fis = new FileInputStream(file);

			// Here BufferedInputStream is added for fast reading.
			bis = new BufferedInputStream(fis);

			BufferedReader br = new BufferedReader(new InputStreamReader(bis));

			// output
			builder = new StringBuilder();

			while (br.ready()) {
				builder.append(br.readLine());
			}

			// dispose all the resources after using them.
			fis.close();
			bis.close();
			
	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder;
	}
}
