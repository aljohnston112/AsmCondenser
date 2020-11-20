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
	// TODO Update equals map for registers where two registers are written and then
	// accessed as one.

	private static HashMap<String, HashMap<String, String>> routineEqualMaps = new HashMap<>();

	private static HashMap<String, ArrayList<String>> callMap = new HashMap<>();

	private static File fileIn = new File("C:\\Users\\aljoh\\Documents\\Programming\\TEST\\engine.asm");
	// private static File fileIn = new
	// File("C:\\Users\\aljoh\\Documents\\Programming\\TEST\\test.txt");

	public static void main(String[] args) {
		condenseAsm(fileIn);
	}

	private static void condenseAsm(File asmFile) {
		File refactoredFile = null;
		try {
			Scanner scanner = new Scanner(asmFile);
			refactoredFile = new File(asmFile.getParent() + "\\out\\" + asmFile.getName());
			BufferedWriter out = new BufferedWriter(new FileWriter(refactoredFile));
			refactorAsm(scanner, out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (refactoredFile != null) {
			try {
				Scanner scanner = new Scanner(refactoredFile);
				BufferedWriter out = new BufferedWriter(
						new FileWriter(new File(asmFile.getParent() + "\\out\\refactored_" + asmFile.getName())));
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

	private static void condenseASM(Scanner scanner, BufferedWriter out) throws IOException {
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
				// Reset the equals map after reaching a spot that invalidates it
				if (first.charAt(0) == '.' || first.charAt(first.length() - 1) == ':'
						|| first.toLowerCase().equals("pop") || first.toLowerCase().equals("call")
						|| first.toLowerCase().equals("ret") || first.toLowerCase().equals("reti")
						|| first.toLowerCase().equals("rst")) {
					if (first.charAt(0) == '.' || first.charAt(first.length() - 1) == ':') {
						updateName = true;
					}
					if (routineName != null) {
						routineEqualMaps.put(routineName + routineIndex, new HashMap<>(equalsMap));
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
		String destination =null;
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

	private static void updateLineAndEqualsMap(Map<String, String> equalsMap, String line, List<String> comments,
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
					lhsScanner.useDelimiter("[\\p{javaWhitespace}\\[\\]\\+\\-\\^\\|\\&\\(\\)]+");
					while (lhsScanner.hasNext()) {
						s = lhsScanner.next();
						lhs = replaceVariable(equalsMap, lhs, s);
					}
				}
				rhsScanner = new Scanner(rhs);
				rhsScanner.useDelimiter("[\\p{javaWhitespace}\\[\\]\\+\\-\\^\\|\\&\\(\\)]+");
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

	private static String replaceVariable(Map<String, String> equalsMap, String line, String s) {
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
			line = line.replace(" " + s + " ", " " + (String) equalsMap.get(s) + " ");
			line = line.replace("[" + s + "]", "[" + (String) equalsMap.get(s) + "]");
			if (incHL) {
				line = line.replace("hl]", (String) equalsMap.get("hl") + "++]");
				equalsMap.put("hl", (String) equalsMap.get("hl") + "++ ");
			}
			if (decHL) {
				line = line.replace("hl]", (String) equalsMap.get("hl") + "--]");
				equalsMap.put("hl", (String) equalsMap.get("hl") + "-- ");
			}
		} else {
			if (incHL) {
				line = line.replace("hl]", "hl++]");	
				equalsMap.put("hl", "hl++ ");
				}
			if (decHL) {
				line = line.replace("hl]", "hl--]");
				equalsMap.put("hl", "hl-- ");
			}
		}
		
		return line;
	}

	private static void refactorAsm(Scanner scanner, BufferedWriter out) throws IOException {
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
				if (arg2.charAt(0) == ';' || instruction == null || arg1 == null) {
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
				if (instruction.charAt(instruction.length() - 1) == ':' || instruction.charAt(0) == '.') {
					out.write(line);
					fluff.clear();
					printTheRest = false;
				} else if (lCInstruction.equals("push") || lCInstruction.equals("pop") || lCInstruction.equals("call")
						|| lCInstruction.equals("jr") || lCInstruction.equals("jp") || lCInstruction.equals("ret")
						|| lCInstruction.equals("bit") || lCInstruction.equals("res") || lCInstruction.equals("set")
						|| lCInstruction.equals("db") || lCInstruction.equals("dw") || lCInstruction.equals("dl")
						|| lCInstruction.equals("rept") || lCInstruction.equals("endr") || lCInstruction.equals("scf")
						|| lCInstruction.equals("swap") || lCInstruction.equals("include")) {
					out.write(line);
					fluff.clear();
					printTheRest = false;
				} else if (lCInstruction.equals("ld") || lCInstruction.equals("ldh")) {
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

	private static void printTheRest(Scanner scanner, BufferedWriter out) throws IOException {
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