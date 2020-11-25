package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public class ASMFindCallers {

	public static void findCallers(
			Map<File, ArrayList<Integer>> filesWLineNums,
			File directory, String find, String extension)
			throws IOException {
		File[] fileList = directory.listFiles();
		for (File file : fileList) {
			if (file.isDirectory()) {
				findCallers(filesWLineNums, file, find, extension);
			} else if (file.getName().endsWith(extension)) {
				findCallersInFile(filesWLineNums, file, find);
			}
		}
	}

	private static void findCallersInFile(
			Map<File, ArrayList<Integer>> filesWLineNums,
			File file, String stringTofind)
			throws IOException {
		int lineN = 0;
		int callerN = -1;
		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		Scanner lineScanner = new Scanner(file);
		String line;
		String firstString;
		while ((line = bufferedReader.readLine()) != null) {
			lineScanner = new Scanner(line);
			if (lineScanner.hasNext()) {
				firstString = lineScanner.next();
				if (firstString.charAt(firstString.length() - 1) == ':') {
					callerN = lineN;
				}
			}
			if (line.contains(stringTofind) && (line.contains("ret")
					|| line.contains("call") || line.contains("jp")
					|| line.contains("jr") || line.contains("rst"))) {
				if (filesWLineNums.get(file) == null) {
					filesWLineNums.put(file, new ArrayList<>());
				}
				filesWLineNums.get(file).add(callerN);
			}
			lineN++;
		}
		if (lineScanner != null) {
			lineScanner.close();
		}
		bufferedReader.close();
	}

}
