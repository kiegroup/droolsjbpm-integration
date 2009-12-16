package org.drools.helper;

import java.io.File;

public class FileHelper {
	public static void remove(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : files) {
				remove(f);
			}
		}
		file.delete();
	}
}
