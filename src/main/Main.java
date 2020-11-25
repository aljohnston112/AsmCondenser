package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {

	// TODO delete unnecessary assignments.
	// TODO Add variable assignment replacement to jumps and bit operations.
	// TODO Follow variable assignment when push and pop are used.
	// TODO Follow register values through calls and jumps.
	// TODO Update equals map for registers where two registers are written and
	// then
	// accessed as one.

	private static HashMap<String, HashMap<String, String>> routineEqualMaps = new HashMap<>();

	private static HashMap<String, ArrayList<String>> callMap = new HashMap<>();

	private static File dirOut = new File(
			"C:\\Users\\aljoh\\Documents\\Programming\\TEST\\");

	private static File dir = new File(
			"C:\\Users\\aljoh\\Documents\\Programming\\Github\\pokecrystal");

	private static String methodName = "_UpdateSound::";

	// private static File fileIn = new
	// File("C:\\Users\\aljoh\\Documents\\Programming\\TEST\\test.txt");

	private static Map<File, ArrayList<Integer>> callerMap;

	public static void main(String[] args) {
		Map<File, ArrayList<Integer>> routineMap = new HashMap<>();
		Map<File, ArrayList<Integer>> callerMap = new HashMap<>();
		findUsages(routineMap, callerMap, dir, methodName);
		System.out.print(true);
		if (routineMap != null && routineMap.size() > 0) {
			File fileWRoutine = (File) routineMap.keySet().toArray()[0];
			condenseAsm(fileWRoutine, routineMap.get(fileWRoutine).get(0));
		}
		System.out.print(true);
	}

	private static void findUsages(
			Map<File, ArrayList<Integer>> routineMap,
			Map<File, ArrayList<Integer>> callerMap,
			File directory, String methodName) {
		try {
			FileSearcher.findStringInFiles(
					routineMap, directory, methodName, ".asm");
			ASMFindCallers.findCallers(
					callerMap, directory,
					methodName.replace(":", ""), ".asm");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void condenseAsm(File asmFile, Integer lineNum) {
		File refactoredFile = refactorRoutine(asmFile, lineNum);
		if (refactoredFile != null) {
			try {
				Scanner scanner = new Scanner(refactoredFile);
				BufferedWriter out = new BufferedWriter(
						new FileWriter(new File(dirOut.getAbsoluteFile()
								+ "\\out\\refactored_" + asmFile.getName())));
				condenseASM(scanner, out);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Takes a gbz80 asm file and refactors the routine,
	 * starting at the specified line number,
	 * for the condensing step.
	 * 
	 * @param asmFile The asm file with the method to refactor
	 * @param lineNum The line number where the routine starts
	 *                (Line number starts at 0).
	 * @return A file with the refactored routine.
	 */
	private static File refactorRoutine(File asmFile, Integer lineNum) {
		File refactoredFile = null;
		try {
			refactoredFile = new File(
					dirOut.getAbsoluteFile() + "\\out\\" + asmFile.getName());
			BufferedWriter out = new BufferedWriter(
					new FileWriter(refactoredFile));
			Scanner scanner = new Scanner(asmFile);
			for (int i = 0; i < lineNum; i++) {
				scanner.nextLine();
			}
			refactorRoutine(scanner, out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return refactoredFile;
	}
	
	/** 
	 * Takes a Scanner and writes refactored gbz80 asm 
	 * to a BufferedWriter until it reaches a return instruction
	 * or the end of the Scanner.
	 * 
	 * @param scanner      The Scanner where the next line is the start of a routine.
	 * @param out          The BufferedWriter to write the refactored routine to.
	 * @throws IOException If there is an error writing to the BufferedWriter.
	 */
	private static void refactorRoutine(Scanner scanner, BufferedWriter out)
			throws IOException {
		String line;
		String leadingWhiteSpace;
		String trimmedLine;
		String instruction;
		String lCInstruction;
		String lineArgs;
		String[] lineAndComment;
		String[] args;
		String arg1;
		String arg2;
		Scanner lineScanner;
		List<String> fluff = new ArrayList<>();
		boolean printTheRest = true;
		while (scanner.hasNext()) {
			printTheRest = true;
			instruction = null;
			arg1 = null;
			arg2 = null;
			line = scanner.nextLine();
			leadingWhiteSpace = getLeadingWhiteSpace(line);
			trimmedLine = line.trim();
			lineScanner = new Scanner(line);
			lineScanner.useDelimiter("[\\p{javaWhitespace},]+");
			fluff.clear();
			if (lineScanner.hasNext()) {
				instruction = lineScanner.next();
				if (instruction.charAt(0) == ';') {
					fluff.add(instruction);
					instruction = null;
				}
			}
			if (lineScanner.hasNext()) {
				lineArgs = lineScanner.nextLine();
				lineAndComment = lineArgs.split(";");
				if (lineAndComment.length > 0) {
					args = lineAndComment[0].split(",");
					if (lineAndComment.length > 1) {
						fluff.add(" ");
					}
					for (int i = 1; i < lineAndComment.length; i++) {
						fluff.add(";");
						fluff.add(lineAndComment[i]);
					}
					if (args.length > 0) {
						arg1 = args[0].trim();
						if (args.length > 1) {
							arg2 = args[1].trim();
						}
					}
				}
			}
			if (arg1 != null && arg1.length() > 0) {
				if (arg1.charAt(0) == ';' || instruction == null) {
					fluff.add(arg1);
					arg1 = null;
				}
			}
			if (arg2 != null && arg2.length() > 0) {
				if (arg2.charAt(0) == ';' || instruction == null
						|| arg1 == null) {
					fluff.add(arg2);
					arg2 = null;
				}
			}
			if (trimmedLine.length() > 0) {
				if (trimmedLine.charAt(0) == ';') {
					out.write(line);
					fluff.clear();
					printTheRest = false;
				}
			}
			if (instruction != null && instruction.length() > 0) {
				lCInstruction = instruction.toLowerCase();
				if (instruction.charAt(instruction.length() - 1) == ':'
						|| instruction.charAt(0) == '.') {
					out.write(line);
					fluff.clear();
					printTheRest = false;
				} else if (lCInstruction.equals("push")
						|| lCInstruction.equals("pop")
						|| lCInstruction.equals("call")
						|| lCInstruction.equals("jr")
						|| lCInstruction.equals("jp")
						|| lCInstruction.equals("ret")
						|| lCInstruction.equals("bit")
						|| lCInstruction.equals("res")
						|| lCInstruction.equals("set")
						|| lCInstruction.equals("db")
						|| lCInstruction.equals("dw")
						|| lCInstruction.equals("dl")
						|| lCInstruction.equals("rept")
						|| lCInstruction.equals("endr")
						|| lCInstruction.equals("scf")
						|| lCInstruction.equals("swap")
						|| lCInstruction.equals("include")
						|| lCInstruction.equals("reti")) {
					if (lCInstruction.equals("call")) {
						Map<File, ArrayList<Integer>> routineMap = new HashMap<>();
						Map<File, ArrayList<Integer>> callerMap = new HashMap<>();
						FileSearcher.findStringInFiles(routineMap, dir,
								arg1 + ":", ".asm");
						ASMFindCallers.findCallers(callerMap, dir,
								arg1.replace(":", ""), ".asm");
						if (routineMap != null && routineMap.size() > 0) {
							File f = (File) routineMap.keySet().toArray()[0];
							out.flush();
							refactorRoutine(out, f, routineMap.get(f).get(0));
						}
					} else {
						out.write(line);
					}
					fluff.clear();
					printTheRest = false;
					if ((lCInstruction.equals("ret")
							|| lCInstruction.equals("reti")) && arg1 == null) {
						lineScanner.close();
						out.flush();
						return;
					}
				} else if (lCInstruction.equals("ld")
						|| lCInstruction.equals("ldh")) {
					out.write(leadingWhiteSpace);
					out.write(getLD(arg1, arg2));
				} else if (lCInstruction.equals("cp")) {
					out.write(leadingWhiteSpace);
					out.write(getCP(arg1, arg2));
				} else if (lCInstruction.equals("dec")) {
					out.write(leadingWhiteSpace);
					out.write(getDEC(arg1));
				} else if (lCInstruction.equals("inc")) {
					out.write(leadingWhiteSpace);
					out.write(getINC(arg1));
				} else if (lCInstruction.equals("add")) {
					out.write(leadingWhiteSpace);
					out.write(getADD(arg1, arg2));
				} else if (lCInstruction.equals("adc")) {
					out.write(leadingWhiteSpace);
					out.write(getADC(arg1, arg2));
				} else if (lCInstruction.equals("sub")) {
					out.write(leadingWhiteSpace);
					out.write(getSUB(arg1, arg2));
				} else if (lCInstruction.equals("sbc")) {
					out.write(leadingWhiteSpace);
					out.write(getSBC(arg1, arg2));
				} else if (lCInstruction.equals("sla")) {
					out.write(leadingWhiteSpace);
					out.write(getSLA(arg1));
				} else if (lCInstruction.equals("sra")) {
					out.write(leadingWhiteSpace);
					out.write(getSRA(arg1));
				} else if (lCInstruction.equals("srl")) {
					out.write(leadingWhiteSpace);
					out.write(getSRL(arg1));
				} else if (lCInstruction.equals("rr")) {
					out.write(leadingWhiteSpace);
					out.write(getRR(arg1));
				} else if (lCInstruction.equals("rl")) {
					out.write(leadingWhiteSpace);
					out.write(getRL(arg1));
				} else if (lCInstruction.equals("rra")) {
					out.write(leadingWhiteSpace);
					out.write(getRRA());
				} else if (lCInstruction.equals("rla")) {
					out.write(leadingWhiteSpace);
					out.write(getRLA());
				} else if (lCInstruction.equals("rlca")) {
					out.write(leadingWhiteSpace);
					out.write(getRLCA());
				} else if (lCInstruction.equals("rrca")) {
					out.write(leadingWhiteSpace);
					out.write(getRRCA());
				} else if (lCInstruction.equals("and")) {
					out.write(leadingWhiteSpace);
					out.write(getAND(arg1, arg2));
				} else if (lCInstruction.equals("or")) {
					out.write(leadingWhiteSpace);
					out.write(getOR(arg1, arg2));
				} else if (lCInstruction.equals("xor")) {
					out.write(leadingWhiteSpace);
					out.write(getXOR(arg1, arg2));
				} else {
					out.write("unimplemented: ");
					out.write(line);
					fluff.clear();
					printTheRest = false;
				}
			}
			if (printTheRest) {
				for (String s : fluff) {
					out.write(s);
				}
				if (fluff.isEmpty()) {
					out.write(" ");
				}
				printTheRest(lineScanner, out);
			}
			out.write("\n");
		}
		out.flush();
	}


	private static void refactorRoutine(
			BufferedWriter out, File asmFile, Integer lineNum) {
		try {
			Scanner scanner = new Scanner(asmFile);
			for (int i = 0; i < lineNum; i++) {
				scanner.nextLine();
			}
			refactorRoutine(scanner, out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void condenseASM(Scanner scanner, BufferedWriter out)
			throws IOException {
		Map<String, String> equalsMap = new HashMap<>();
		String line;
		Scanner lineScanner;
		String first;
		String routineName = null;
		int routineIndex = -1;
		List<String> comments = new ArrayList<>();
		boolean updateName = false;
		while (scanner.hasNext()) {
			line = scanner.nextLine();
			lineScanner = new Scanner(line);
			if (lineScanner.hasNext()) {
				first = lineScanner.next();
				// Reset the equals map after reaching a spot that invalidates
				// it
				if (first.charAt(0) == '.'
						|| first.charAt(first.length() - 1) == ':'
						|| first.toLowerCase().equals("pop")
						|| first.toLowerCase().equals("call")
						|| first.toLowerCase().equals("ret")
						|| first.toLowerCase().equals("reti")
						|| first.toLowerCase().equals("rst")) {
					if (first.charAt(0) == '.'
							|| first.charAt(first.length() - 1) == ':') {
						updateName = true;
					}
					if (routineName != null) {
						routineEqualMaps.put(routineName + routineIndex,
								new HashMap<>(equalsMap));
					}
					routineIndex++;
					if (updateName) {
						routineIndex = 0;
						first = first.replace(".", "");
						first = first.replace(":", "");
						routineName = first;
						updateName = false;
					}
					equalsMap.clear();
					updateCallMap(lineScanner, routineName);
				}
				if (line.contains("=")) {
					updateLineAndEqualsMap(equalsMap, line, comments, out);
				} else {
					out.write(line);
				}
			}
			out.write("\n");
		}
		out.flush();
	}

	private static void updateCallMap(Scanner lineScanner, String routineName) {
		String destination = null;
		String s;
		String lastS = null;
		while (lineScanner.hasNext()) {
			s = lineScanner.next();
			if (s.contains(";")) {
				eatTheRest(lineScanner);
			} else {
				lastS = s;
			}
			if (lastS != null) {
				destination = lastS;
			}
		}
		if (routineName != null) {
			if (callMap.get(routineName) == null) {
				callMap.put(routineName, new ArrayList<>());
			}
			callMap.get(routineName).add(destination);
			destination = null;
		}
	}

	private static void updateLineAndEqualsMap(Map<String, String> equalsMap,
			String line, List<String> comments,
			BufferedWriter out) throws IOException {
		String[] args;
		String[] realArgs;
		String lhs = null;
		String rhs = null;
		Scanner lhsScanner;
		Scanner rhsScanner;
		String s;
		args = line.split(";");
		if (args.length > 0) {
			String realLine = args[0];
			realArgs = realLine.split("=");
			if (realArgs.length > 0) {
				lhs = realArgs[0];
			}
			if (realArgs.length > 1) {
				rhs = realArgs[1];
			}
			if (lhs != null && rhs != null) {
				if (lhs.contains("[") || lhs.contains("]")) {
					lhsScanner = new Scanner(lhs);
					lhsScanner.useDelimiter(
							"[\\p{javaWhitespace}\\[\\]\\+\\-\\^\\|\\&\\(\\)]+");
					while (lhsScanner.hasNext()) {
						s = lhsScanner.next();
						lhs = replaceVariable(equalsMap, lhs, s);
					}
				}
				rhsScanner = new Scanner(rhs);
				rhsScanner.useDelimiter(
						"[\\p{javaWhitespace}\\[\\]\\+\\-\\^\\|\\&\\(\\)]+");
				while (rhsScanner.hasNext()) {
					s = rhsScanner.next();
					rhs = replaceVariable(equalsMap, rhs, s);
				}
				equalsMap.put(lhs.trim(), rhs.trim());
			}
			for (int i = 1; i < args.length; i++) {
				comments.add(args[i]);
			}
			if (lhs != null && rhs != null) {
				out.write(lhs);
				out.write("=");
				out.write(rhs);
				for (String string : comments) {
					out.write(";");
					out.write(string);
				}
				comments.clear();
			}
		} else {
			out.write(line);
		}
	}

	private static String replaceVariable(Map<String, String> equalsMap,
			String line, String s) {
		boolean incHL = false;
		boolean decHL = false;
		if (s.toLowerCase().equals("hli")) {
			s = s.replace("hli", "hl");
			line = line.replace("hli", "hl");
			incHL = true;
		}
		if (s.toLowerCase().equals("hld")) {
			s = s.replace("hld", "hl");
			line = line.replace("hld", "hl");
			decHL = true;
		}
		if (equalsMap.containsKey(s)) {
			line = line.replace(" " + s + " ",
					" " + (String) equalsMap.get(s) + " ");
			line = line.replace("[" + s + "]",
					"[" + (String) equalsMap.get(s) + "]");
			if (incHL) {
				line = line.replace("[" + (String) equalsMap.get(s) + "]",
						"[" + equalsMap.get(s) + "]");
				equalsMap.put(s,
						(String) "( " + (String) equalsMap.get(s) + " + 1 )");
			}
			if (decHL) {
				line = line.replace("[" + (String) equalsMap.get(s) + "]",
						"[" + equalsMap.get(s) + "]");
				equalsMap.put(s, "( " + (String) equalsMap.get(s) + " - 1 )");
			}
		} else {
			if (incHL) {
				line = line.replace("[" + s + "]", "[(" + s + " + 1)]");
				equalsMap.put(s, "( " + s + " + 1)");
			}
			if (decHL) {
				line = line.replace("[" + s + "]", "[(" + s + " - 1)]");
				equalsMap.put(s, "( " + s + " - 1)");
			}
		}

		return line;
	}

	private static String getSLA(String arg1) {
		StringBuilder stringBuilder = new StringBuilder();
		if (arg1 != null) {
			stringBuilder.append("( cf <- ");
			stringBuilder.append(arg1);
			stringBuilder.append(" <- 0 )");
		} else {
			throw new IllegalArgumentException("Invalid SLA instruction");
		}
		return stringBuilder.toString();
	}

	private static String getSRA(String arg1) {
		StringBuilder stringBuilder = new StringBuilder();
		if (arg1 != null) {
			stringBuilder.append("( bit7 -> ");
			stringBuilder.append(arg1);
			stringBuilder.append(" -> cf )");
		} else {
			throw new IllegalArgumentException("Invalid SRA instruction");
		}
		return stringBuilder.toString();
	}

	private static String getSRL(String arg1) {
		StringBuilder stringBuilder = new StringBuilder();
		if (arg1 != null) {
			stringBuilder.append("( 0 -> ");
			stringBuilder.append(arg1);
			stringBuilder.append(" -> cf )");
		} else {
			throw new IllegalArgumentException("Invalid SRL instruction");
		}
		return stringBuilder.toString();
	}

	private static String getRLCA() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("( cf <- a <- bit7 )");
		return stringBuilder.toString();
	}

	private static String getRRCA() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("( bit0 -> a -> cf )");
		return stringBuilder.toString();
	}

	private static String getRRA() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("( cf -> a -> cf )");
		return stringBuilder.toString();
	}

	private static String getRLA() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("( cf <- a <- cf )");
		return stringBuilder.toString();
	}

	private static String getRR(String arg1) {
		StringBuilder stringBuilder = new StringBuilder();
		if (arg1 != null) {
			stringBuilder.append("( cf -> ");
			stringBuilder.append(arg1);
			stringBuilder.append(" -> cf )");
		} else {
			throw new IllegalArgumentException("Invalid RR instruction");
		}
		return stringBuilder.toString();
	}

	private static String getRL(String arg1) {
		StringBuilder stringBuilder = new StringBuilder();
		if (arg1 != null) {
			stringBuilder.append("( cf <- ");
			stringBuilder.append(arg1);
			stringBuilder.append(" <- cf )");
		} else {
			throw new IllegalArgumentException("Invalid RL instruction");
		}
		return stringBuilder.toString();
	}

	private static String getCP(String arg1, String arg2) {
		StringBuilder stringBuilder = new StringBuilder();
		if (arg1 != null) {
			if (arg2 != null) {
				stringBuilder.append("cf if ( ");
				stringBuilder.append(arg1);
				stringBuilder.append(" < ");
				stringBuilder.append(arg2);
				stringBuilder.append(" )");
			} else {
				stringBuilder.append("cf if ( a < ");
				stringBuilder.append(arg1);
				stringBuilder.append(" )");
			}
		} else {
			throw new IllegalArgumentException("Invalid cp instruction");
		}
		return stringBuilder.toString();
	}

	private static String getDEC(String arg1) {
		StringBuilder stringBuilder = new StringBuilder();
		if (arg1 != null) {
			stringBuilder.append(arg1);
			stringBuilder.append("--");
		} else {
			throw new IllegalArgumentException("Invalid dec instruction");
		}
		return stringBuilder.toString();
	}

	private static String getINC(String arg1) {
		StringBuilder stringBuilder = new StringBuilder();
		if (arg1 != null) {
			stringBuilder.append(arg1);
			stringBuilder.append("++");
		} else {
			throw new IllegalArgumentException("Invalid inc instruction");
		}
		return stringBuilder.toString();
	}

	private static String getADD(String arg1, String arg2) {
		StringBuilder stringBuilder = new StringBuilder();
		if (arg1 != null) {
			if (arg2 != null) {
				stringBuilder.append(arg1);
				stringBuilder.append(" = ");
				stringBuilder.append("( ");
				stringBuilder.append(arg1);
				stringBuilder.append(" + ");
				stringBuilder.append(arg2);
				stringBuilder.append(" )");
			} else {
				stringBuilder.append("a = ( a");
				stringBuilder.append(" + ");
				stringBuilder.append(arg1);
				stringBuilder.append(" )");
			}
		} else {
			throw new IllegalArgumentException("Invalid add instruction");
		}
		return stringBuilder.toString();
	}

	private static String getADC(String arg1, String arg2) {
		StringBuilder stringBuilder = new StringBuilder();
		if (arg1 != null) {
			if (arg2 != null) {
				stringBuilder.append(arg1);
				stringBuilder.append(" = ");
				stringBuilder.append("( ");
				stringBuilder.append(arg1);
				stringBuilder.append(" + ");
				stringBuilder.append(arg2);
				stringBuilder.append(" + cf )");
			} else {
				stringBuilder.append("a = ( a");
				stringBuilder.append(" + ");
				stringBuilder.append(arg1);
				stringBuilder.append(" + cf )");
			}
		} else {
			throw new IllegalArgumentException("Invalid adc instruction");
		}
		return stringBuilder.toString();
	}

	private static String getSUB(String arg1, String arg2) {
		StringBuilder stringBuilder = new StringBuilder();
		if (arg1 != null) {
			if (arg2 != null) {
				stringBuilder.append(arg1);
				stringBuilder.append(" = ");
				stringBuilder.append("(");
				stringBuilder.append(arg1);
				stringBuilder.append(" - ");
				stringBuilder.append(arg2);
				stringBuilder.append(")");
			} else {
				stringBuilder.append("a = (a");
				stringBuilder.append(" - ");
				stringBuilder.append(arg1);
				stringBuilder.append(")");
			}
		} else {
			throw new IllegalArgumentException("Invalid sub instruction");
		}
		return stringBuilder.toString();
	}

	private static String getSBC(String arg1, String arg2) {
		StringBuilder stringBuilder = new StringBuilder();
		if (arg1 != null) {
			if (arg2 != null) {
				stringBuilder.append(arg1);
				stringBuilder.append(" = ");
				stringBuilder.append("( ");
				stringBuilder.append(arg1);
				stringBuilder.append(" - ");
				stringBuilder.append(arg2);
				stringBuilder.append(" - cf )");
			} else {
				stringBuilder.append("a = ( a");
				stringBuilder.append(" - ");
				stringBuilder.append(arg1);
				stringBuilder.append(" - cf )");
			}
		} else {
			throw new IllegalArgumentException("Invalid sbc instruction");
		}
		return stringBuilder.toString();
	}

	private static String getXOR(String arg1, String arg2) {
		StringBuilder stringBuilder = new StringBuilder();
		if (arg1 != null) {
			if (arg2 != null) {
				stringBuilder.append(arg1);
				stringBuilder.append(" = ");
				stringBuilder.append("( ");
				stringBuilder.append(arg1);
				stringBuilder.append(" ^ ");
				stringBuilder.append(arg2);
				stringBuilder.append(" )");
			} else {
				if (arg1.toLowerCase().equals("a")) {
					stringBuilder.append("a = 0");
				} else {
					stringBuilder.append("a = ( a");
					stringBuilder.append(" ^ ");
					stringBuilder.append(arg1);
					stringBuilder.append(" )");
				}
			}
		} else {
			throw new IllegalArgumentException("Invalid xor instruction");
		}
		return stringBuilder.toString();
	}

	private static String getOR(String arg1, String arg2) {
		StringBuilder stringBuilder = new StringBuilder();
		if (arg1 != null) {
			if (arg2 != null) {
				stringBuilder.append("( ");
				stringBuilder.append(arg1);
				stringBuilder.append(" = ");
				stringBuilder.append(arg1);
				stringBuilder.append(" | ");
				stringBuilder.append(arg2);
				stringBuilder.append(" )");
			} else {
				stringBuilder.append("a = ( a");
				stringBuilder.append(" | ");
				stringBuilder.append(arg1);
				stringBuilder.append(" )");
			}
		} else {
			throw new IllegalArgumentException("Invalid or instruction");
		}
		return stringBuilder.toString();
	}

	private static String getAND(String arg1, String arg2) {
		StringBuilder stringBuilder = new StringBuilder();
		if (arg1 != null) {
			if (arg2 != null) {
				stringBuilder.append(arg1);
				stringBuilder.append(" = ");
				stringBuilder.append("( ");
				stringBuilder.append(arg1);
				stringBuilder.append(" & ");
				stringBuilder.append(arg2);
				stringBuilder.append(" )");
			} else {
				stringBuilder.append("a = ( a");
				stringBuilder.append(" & ");
				stringBuilder.append(arg1);
				stringBuilder.append(" )");
			}
		} else {
			throw new IllegalArgumentException("Invalid and instruction");
		}
		return stringBuilder.toString();
	}

	private static String getLD(String arg1, String arg2) {
		StringBuilder stringBuilder = new StringBuilder();
		if (arg1 != null) {
			if (arg2 != null) {
				stringBuilder.append(arg1);
				stringBuilder.append(" = ");
				stringBuilder.append(arg2);
			} else {
				stringBuilder.append(" a = ");
				stringBuilder.append(arg1);
			}
		} else {
			throw new IllegalArgumentException("Invalid ld instruction");
		}
		return stringBuilder.toString();
	}

	private static String getLeadingWhiteSpace(String line) {
		StringBuilder whiteSpace = new StringBuilder();
		int i = -1;
		int j = 0;
		for (char c : line.toCharArray()) {
			i++;
			if (i == j && Character.isWhitespace(c)) {
				whiteSpace.append(c);
				j++;
			}
		}
		return whiteSpace.toString();
	}

	private static void printTheRest(Scanner scanner, BufferedWriter out)
			throws IOException {
		if (scanner.hasNext()) {
			out.write(scanner.nextLine());
		}
	}

	private static void eatTheRest(Scanner scanner) {
		while (scanner.hasNext()) {
			scanner.next();
		}
	}

}