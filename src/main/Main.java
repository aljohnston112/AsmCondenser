package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

	private static File fileIn = new File("C:\\Users\\aljoh\\Documents\\Programming\\TEST\\engine.asm");
	// private static File fileIn = new
	// File("C:\\Users\\aljoh\\Documents\\Programming\\TEST\\test.txt");

	public static void main(String[] args) {
		condenseAsm(fileIn);
	}

	private static void condenseAsm(File asmFile) {
		try {
			Scanner scanner = new Scanner(asmFile);
			BufferedWriter out = new BufferedWriter(
					new FileWriter(new File(asmFile.getParent() + "\\out\\" + asmFile.getName())));
			condenseAsm(scanner, out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void condenseAsm(Scanner scanner, BufferedWriter out) throws IOException {
		String line;
		String leadingWhiteSpace;
		String trimmedLine;
		String instruction;
		String lCInstruction;
		String lineArgs;
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
				String[] args = lineArgs.split(",");
				if(args.length > 0){
					arg1 = args[0].split(";")[0];
					arg1 = arg1.trim();
				}
				if(args.length > 1){
					arg2 = args[1].split(";")[0];
					arg2 = arg2.trim();
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
						|| lCInstruction.equals("bit")) {
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
				printTheRest(lineScanner, out);
			}
			out.write("\n");
		}
		out.flush();
	}

	private static String getCP(String arg1, String arg2) {
		StringBuilder stringBuilder = new StringBuilder();
		if (arg1 != null) {
			if (arg2 != null) {
				stringBuilder.append("c if (");
				stringBuilder.append(arg1);
				stringBuilder.append(" < ");
				stringBuilder.append(arg2);
				stringBuilder.append(") ");
			} else {
				stringBuilder.append("c if (a < ");
				stringBuilder.append(arg1);
				stringBuilder.append(") ");
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
				stringBuilder.append(arg1);
				stringBuilder.append(" + ");
				stringBuilder.append(arg2);
			} else {
				stringBuilder.append("a = a");
				stringBuilder.append(" + ");
				stringBuilder.append(arg1);
			}
		} else {
			throw new IllegalArgumentException("Invalid add instruction");
		}
		return stringBuilder.toString();
	}

	private static String getXOR(String arg1, String arg2) {
		StringBuilder stringBuilder = new StringBuilder();
		if (arg1 != null) {
			if (arg2 != null) {
				stringBuilder.append(arg1);
				stringBuilder.append(" = ");
				stringBuilder.append(arg1);
				stringBuilder.append(" ^ ");
				stringBuilder.append(arg2);
			} else {
				stringBuilder.append("a = a");
				stringBuilder.append(" ^ ");
				stringBuilder.append(arg1);
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
				stringBuilder.append(arg1);
				stringBuilder.append(" = ");
				stringBuilder.append(arg1);
				stringBuilder.append(" | ");
				stringBuilder.append(arg2);
			} else {
				stringBuilder.append("a = a");
				stringBuilder.append(" | ");
				stringBuilder.append(arg1);
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
				stringBuilder.append(arg1);
				stringBuilder.append(" & ");
				stringBuilder.append(arg2);
			} else {
				stringBuilder.append("a = a");
				stringBuilder.append(" & ");
				stringBuilder.append(arg1);
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
				stringBuilder.append("a = ");
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

}