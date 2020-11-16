package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Main {

	private static File fileIn = new File("C:\\Users\\aljoh\\Documents\\Programming\\TEST\\engine.asm");
	 //private static File fileIn = new
	 //File("C:\\Users\\aljoh\\Documents\\Programming\\TEST\\test.txt");

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
		String trimmedLine;
		while (scanner.hasNext()) {
			line = scanner.nextLine();
			trimmedLine = line.trim();
			if (trimmedLine.length() > 0) {
				if (trimmedLine.charAt(0) == ';') {
					out.write(line);
					out.write("\n");
				} else if (trimmedLine.toLowerCase().equals("ret")) {
					out.write(line);
					out.write("\n");
				} else {
					parseLine(line, out);
				}
			} else {
				out.write(line);
				out.write("\n");
			}
		}
		out.flush();
	}

	private static void parseLine(String line, BufferedWriter out) throws IOException {
		Scanner lineScanner = new Scanner(line);
		lineScanner.useDelimiter("[\\p{javaWhitespace},]+");
		String word;
		int i = -1;
		out.write(getLeadingWhiteSpace(line));
		while (lineScanner.hasNext()) {
			word = lineScanner.next();
			i++;
			if (word.length() > 0) {
				if (i == 0) {
					if (word.charAt(word.length() - 1) == ':' || word.charAt(0) == '.') {
						out.write(word);
					} else if (word.toLowerCase().equals("push") || word.toLowerCase().equals("pop")
							|| word.toLowerCase().equals("call")) {
						out.write(word);
					} else if (word.toLowerCase().equals("ld") || word.toLowerCase().equals("ldh")) {
						String lhs;
						String rhs;
						if (lineScanner.hasNext()) {
							lhs = lineScanner.next();
							if (lineScanner.hasNext()) {
								rhs = lineScanner.next();
								if (rhs.charAt(0) == ';') {
									out.write("a = ");
									out.write(lhs);
									out.write(" ");
									out.write(rhs);
								} else {
									out.write(lhs);
									out.write(" = ");
									out.write(rhs);
								}
							} else {
								out.write("a = ");
								out.write(lhs);
							}
						}
					} else if (word.toLowerCase().equals("cp")) {
						if (lineScanner.hasNext()) {
							word = lineScanner.next();
							if (!word.toLowerCase().equals("a")) {
								if (lineScanner.hasNext()) {
									String firstArg = word;
									word = lineScanner.next();
									if (word.charAt(0) == ';') {
										out.write("c if (a < ");
										out.write(firstArg);
										out.write(") ");
										out.write(word);
									} else {
										out.write("c if (");
										out.write(firstArg);
										out.write(" < ");
										out.write(word);
										out.write(")");
									}
								} else {
									out.write("c if (a < ");
									out.write(word);
									out.write(")");
								}
							} else {
								if (lineScanner.hasNext()) {
									out.write("c if (");
									out.write(word);
									out.write(" < ");
									word = lineScanner.next();
									if (word.charAt(0) == ';') {
										out.write("a ");
										out.write(word);
									} else {
										out.write(word);
										out.write(")");
									}
								} else {
									out.write("cp ");
									out.write(word);
								}
							}
						}
					} else if (word.toLowerCase().equals("add")) {
						if (lineScanner.hasNext()) {
							word = lineScanner.next();
							if (!word.toLowerCase().equals("a")) {
								if (lineScanner.hasNext()) {
									String lhs = word;
									word = lineScanner.next();
									if (word.charAt(0) == ';') {
										out.write("a = (a + ");
										out.write(lhs);
										out.write(") ");
										out.write(word);
									} else {
										out.write(lhs);
										out.write(" = ");
										out.write("(");
										out.write(lhs);
										out.write(" + ");
										out.write(word);
										out.write(")");

									}
								} else {
									out.write("a = (a + ");
									out.write(word);
									out.write(")");
								}
							} else {
								if (lineScanner.hasNext()) {
									String lhs = word;
									word = lineScanner.next();
									if (word.charAt(0) == ';') {
										out.write("add a ");
										out.write(word);
									} else {
										out.write(word);
										out.write(" = ");
										out.write("(");
										out.write(lhs);
										out.write(" + ");
										out.write(word);
										out.write(")");
									}
								} else {
									out.write("add ");
									out.write(word);
								}
							}
						}
					} else if (word.toLowerCase().equals("and")) {
						if (lineScanner.hasNext()) {
							word = lineScanner.next();
							if (!word.toLowerCase().equals("a")) {
								if (lineScanner.hasNext()) {
									String lhs = word;
									word = lineScanner.next();
									if (word.charAt(0) == ';') {
										out.write("a = (a & ");
										out.write(lhs);
										out.write(") ");
										out.write(word);
									} else {
										out.write(lhs);
										out.write(" = ");
										out.write("(");
										out.write(lhs);
										out.write(" & ");
										out.write(word);
										out.write(")");

									}
								} else {
									out.write("a = (a & ");
									out.write(word);
									out.write(")");
								}
							} else {
								if (lineScanner.hasNext()) {
									String lhs = word;
									word = lineScanner.next();
									if (word.charAt(0) == ';') {
										out.write("and a ");
										out.write(word);
									} else {
										out.write(word);
										out.write(" = ");
										out.write("(");
										out.write(lhs);
										out.write(" & ");
										out.write(word);
										out.write(")");
									}
								} else {
									out.write("and ");
									out.write(word);
								}
							}
						}
					} else if (word.toLowerCase().equals("or")) {
						if (lineScanner.hasNext()) {
							word = lineScanner.next();
							if (!word.toLowerCase().equals("a")) {
								if (lineScanner.hasNext()) {
									String lhs = word;
									word = lineScanner.next();
									if (word.charAt(0) == ';') {
										out.write("a = (a & ");
										out.write(lhs);
										out.write(") ");
										out.write(word);
									} else {
										out.write(lhs);
										out.write(" = ");
										out.write("(");
										out.write(lhs);
										out.write(" & ");
										out.write(word);
										out.write(")");

									}
								} else {
									out.write("a = (a & ");
									out.write(word);
									out.write(")");
								}
							} else {
								if (lineScanner.hasNext()) {
									String lhs = word;
									word = lineScanner.next();
									if (word.charAt(0) == ';') {
										out.write("or a ");
										out.write(word);
									} else {
										out.write(word);
										out.write(" = ");
										out.write("(");
										out.write(lhs);
										out.write(" & ");
										out.write(word);
										out.write(")");
									}
								} else {
									out.write("or ");
									out.write(word);
								}
							}
						}
					} else if (word.toLowerCase().equals("xor")) {
						if (lineScanner.hasNext()) {
							word = lineScanner.next();
							if (!word.toLowerCase().equals("a")) {
								if (lineScanner.hasNext()) {
									String lhs = word;
									word = lineScanner.next();
									if (word.charAt(0) == ';') {
										out.write("a = (a ^ ");
										out.write(lhs);
										out.write(") ");
										out.write(word);
									} else {
										out.write(lhs);
										out.write(" = ");
										out.write("(");
										out.write(lhs);
										out.write(" ^ ");
										out.write(word);
										out.write(")");

									}
								} else {
									out.write("a = (a ^ ");
									out.write(word);
									out.write(")");
								}
							} else {
								out.write(word);
								out.write(" = ");
								if (lineScanner.hasNext()) {
									String lhs = word;
									word = lineScanner.next();
									if (word.charAt(0) == ';') {
										out.write("xor a ");
										out.write(word);
									} else {
										out.write("(");
										out.write(lhs);
										out.write(" ^ ");
										out.write(word);
										out.write(")");
									}
								} else {
									out.write("xor a");
									out.write(word);
								}
							}
						}
					} else if ((word.toLowerCase().equals("dec"))) {
						if (lineScanner.hasNext()) {
							word = lineScanner.next();
							out.write(word);
							out.write("--");
						}
					} else if ((word.toLowerCase().equals("inc"))) {
						if (lineScanner.hasNext()) {
							word = lineScanner.next();
							out.write(word);
							out.write("++");
						}
					} else if ((word.toLowerCase().equals("jp"))) {
						out.write("jp");
						if (lineScanner.hasNext()) {
							String condition = lineScanner.next();
							if (lineScanner.hasNext()) {
								word = lineScanner.next();
								if (word.length() > 0 && word.charAt(0) == ';') {
									out.write(" ");
									out.write(condition);
									out.write(" ");
									out.write(word);
								} else {
									out.write(" if ");
									out.write(condition);
									out.write(" to ");
									out.write(word);
								}
							} else {
								out.write(" ");
								out.write(condition);
							}
						}
					} else if ((word.toLowerCase().equals("jr"))) {
						out.write("jr");
						if (lineScanner.hasNext()) {
							String condition = lineScanner.next();
							if (lineScanner.hasNext()) {
								word = lineScanner.next();
								if (word.length() > 0 && word.charAt(0) == ';') {
									out.write(" ");
									out.write(condition);
									out.write(" ");
									out.write(word);
								} else {
									out.write(" if ");
									out.write(condition);
									out.write(" by ");
									out.write(word);
								}
							} else {
								out.write(" ");
								out.write(condition);
							}
						}
					} else if ((word.toLowerCase().equals("bit")) || (word.toLowerCase().equals("db")) ||
							(word.toLowerCase().equals("dw")) || (word.toLowerCase().equals("dl")) ||
							(word.toLowerCase().equals("res")) || (word.toLowerCase().equals("set"))) {
						out.write(word);
					} else if ((word.toLowerCase().equals("ret"))) {
						out.write(word);
						out.write(" if ");
						if(lineScanner.hasNext()) {
							word = lineScanner.next();
							out.write(word);
						}
					} else {
						out.write("unimplemented: ");
						out.write(word);
						out.write(" ");
					}
					printTheRest(lineScanner, out);
				}
			} else {
				out.write(word);
			}
		}
		out.write("\n");
		lineScanner.close();
	}

	private static void printTheRest(Scanner scanner, BufferedWriter out) throws IOException {
		if (scanner.hasNext()) {
			out.write(scanner.nextLine());
		}
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

	private static void eatTheRest(Scanner scanner) {
		while (scanner.hasNext()) {
			scanner.next();
		}
	}

}