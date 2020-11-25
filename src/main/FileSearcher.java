package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class FileSearcher {

	public static void findStringInFiles(
			Map<File, ArrayList<Integer>> filesWLineNums,
			File directory, String stringToFind, String extension)
			throws IOException {
		File[] fileList = directory.listFiles();
		for (File file : fileList) {
			if (file.isDirectory()) {
				findStringInFiles(
						filesWLineNums, file, stringToFind, extension);
			} else if (file.getName().endsWith(extension)) {
				findStringInFile(filesWLineNums, file, stringToFind);
			}
		}
	}

	private static void findStringInFile(
			Map<File, ArrayList<Integer>> filesWLineNums, File file, String stringToFind)
			throws IOException {
		int lineNum = 0;
		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			if (line.contains(stringToFind)) {
				if (filesWLineNums.get(file) == null) {
					filesWLineNums.put(file, new ArrayList<>());
				}
				filesWLineNums.get(file).add(lineNum);
			}
			lineNum++;
		}
		bufferedReader.close();
	}

}