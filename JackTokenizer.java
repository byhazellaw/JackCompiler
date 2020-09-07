import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.StringTokenizer;

public class JackTokenizer {

	private String currentToken = null;

	private static final String KEYWORD = "keyword";
	private static final String SYMBOL = "symbol";
	private static final String IDENTIFIER = "identifier";
	private static final String INT_CONST = "integerConstant";
	private static final String STRING_CONST = "stringConstant";
	private static ArrayList<String> tokensList;
	
	private static final String IDENTIFIER_FIELD = "identifier";
	private static final String IDENTIFIER_STATIC = "identifier";
	private static final String IDENTIFIER_VAR = "identifier";
	private static final String IDENTIFIER_ARG = "identifier";

	private static final String CLASS = "class";
	private static final String METHOD = "method";
	private static final String FUNCTION = "function";
	private static final String CONSTRUCTOR = "constructor";
	private static final String INT = "int";
	private static final String BOOLEAN = "boolean";
	private static final String CHAR = "char";
	private static final String VOID = "void";
	private static final String VAR = "var";
	private static final String STATIC = "static";
	private static final String FIELD = "field";
	private static final String LET = "let";
	private static final String DO = "do";
	private static final String IF = "if";
	private static final String ELSE = "else";
	private static final String WHILE = "while";
	private static final String RETURN = "return";
	private static final String TRUE = "true";
	private static final String FALSE = "false";
	private static final String NULL = "null";
	private static final String THIS = "this";

	public JackTokenizer(String f) throws FileNotFoundException, UnsupportedEncodingException {

		tokensList = new ArrayList<>();

		File file = new File(f);

		if (file.isFile()) {

			int dot = f.indexOf('.');

			String outputFile = f.substring(0, dot) + "T.xml";

			Scanner scanner = new Scanner(new File(f));

//			PrintWriter writer = new PrintWriter(outputFile, "UTF-8");
//			writer.print("<tokens>");

			// save all tokens in list
			while (scanner.hasNextLine()) {

				
				String line = scanner.nextLine();
				String cleanLine = line.trim();

				//ignore comment lines - / is the fist symbol
				if (cleanLine.indexOf("//") == 0 || cleanLine.indexOf("/**") == 0 || cleanLine.indexOf('*')==0
						|| line.startsWith("*")) {
					
					
					
					continue;
				}

				//remove strings after comment symbol in the same line
				if (cleanLine.contains("//")) {

					cleanLine = cleanLine.substring(0, cleanLine.indexOf("//"));
	
				}

				//break down characters
				char ch[] = cleanLine.toCharArray();
				StringBuilder sb = new StringBuilder();

				int k = 0;

				while (k < ch.length) {

					if (ch[k] == '\"') {

						k++;

						while (k < ch.length) {

							if (ch[k] == '\"') {

								tokensList.add(sb.toString());
								sb.setLength(0);
								k++;
								break;

							} else {

								sb.append(ch[k]);
								k++;
							}

						}

					}  else if (ch[k] == '{' || ch[k] == '}' || ch[k] == ')' || ch[k] == '{' || ch[k] == '['
							|| ch[k] == ']' || ch[k] == '.' || ch[k] == ',' || ch[k] == ';' || ch[k] == '+'
							|| ch[k] == '-' || ch[k] == '*' || ch[k] == '/' || ch[k] == '&' || ch[k] == '|'
							|| ch[k] == '<' || ch[k] == '>' || ch[k] == '=' || ch[k] == '_' || ch[k] == '('
							|| ch[k] == '~') {

						tokensList.add(sb.toString());
						sb.setLength(0);

						sb.append(ch[k]);
						tokensList.add(sb.toString());
						sb.setLength(0);
						k++;

					} else if (ch[k] == ' ' || ch[k] == '	') {

						tokensList.add(sb.toString());
						sb.setLength(0);
						k++;
						continue;

					}else {

						sb.append(ch[k]);
						k++;

					}

				}

			}
			
			

			

			//replace symbols
			while (tokensList.indexOf("<") != -1) {
				
				int index = tokensList.indexOf("<");
				
				tokensList.set(index, "&lt;");
				
			}
			
			while (tokensList.indexOf(">") != -1) {
				
				int index = tokensList.indexOf(">");
				
				tokensList.set(index, "&gt;");
				
			}
			
			while (tokensList.indexOf("&") != -1) {
				
				int index = tokensList.indexOf("&");
				
				tokensList.set(index, "&amp;");
				
			}
			
			while (tokensList.indexOf("\"") != -1) {
				
				int index = tokensList.indexOf("\"");
				
				tokensList.set(index, "&quot;");
				
			}
			
			
			//remove white spaces
			Iterator<String> iter = tokensList.iterator();
			
			while (iter.hasNext()) {
				
				String str = iter.next();
				
				if (str.isEmpty()) {
					
					iter.remove();
				}
			}

			
			//print out tokens
//			int j = 0;


			
//			while (j < tokensList.size()) {
			
				
			
//				currentToken = tokensList.get(j);
				
				
//				writer.println();
//				writer.print("<" + tokenType(currentToken) + ">");
//				writer.print(" " + currentToken + " ");
//				writer.print("</" + tokenType(currentToken) + ">");

//				j++;
//			}

//			writer.println();
//			writer.print("</tokens>");
			scanner.close();
//			writer.close();
		}

		
	}

	
	protected ArrayList<String> getTokensList() {

		return tokensList;
	}

	

	protected String tokenType(String token) {

		String type = null;

		if (token != null) {

			if (!keyWord(token).isEmpty()) {

				
				
				
				type = KEYWORD;

			} else if (token.equals("{") || token.equals("}") || token.equals("(") || token.equals(")")
					|| token.equals("[") || token.equals("]") || token.equals(".") || token.equals(",")
					|| token.equals(";") || token.equals("+") || token.equals("-") || token.equals("*")
					|| token.equals("/") || token.equals("&amp;") || token.equals("|") || token.equals("&lt;")
					|| token.equals("&gt;") || token.equals("=") || token.equals("_") || token.equals("&quot;")
					|| token.equals("~")) {

				type = SYMBOL;

			} else if (isNumeric(token)) {

				if (Integer.parseInt(token) >= 0 && Integer.parseInt(token) <= 32767) {

					type = INT_CONST;
				}

				// string constants are all upper case
			} else if (hasSpace(token)) {

				type = STRING_CONST;

			} else {
				
				
				type = IDENTIFIER;
			}

		}

		return type;

	}

	protected static boolean hasSpace(String str) {

		char[] charArray = str.toCharArray();

		for (char ch : charArray) {

			if (ch == ' ') {
				return true;
			}

		}
		return false;
	}

	private static boolean isNumeric(String str) {

		if (str == null || str.length() == 0) {
			return false;
		}

		return str.chars().allMatch(Character::isDigit);

	}
	
	

	private static String keyWord(String token) {

		String result = "";

		switch (token) {

		case "class":

			result = CLASS;
			break;

		case "constructor":
			result = CONSTRUCTOR;
			break;
		case "function":
			result = FUNCTION;
			break;
		case "method":
			result = METHOD;
			break;
		case "field":
			result = FIELD;
			break;
		case "static":
			result = STATIC;
			break;
		case "int":
			result = INT;
			break;
		case "boolean":
			result = BOOLEAN;
			break;
		case "char":
			result = CHAR;
			break;
		case "void":
			result = VOID;
			break;
		case "var":
			result = VAR;
			break;
		case "let":
			result = LET;
			break;

		case "do":
			result = DO;
			break;

		case "if":
			result = IF;
			break;

		case "else":
			result = ELSE;
			break;
		case "while":
			result = WHILE;
			break;
		case "return":
			result = RETURN;
		case "true":
			result = TRUE;
			break;
		case "false":
			result = FALSE;
			break;
		case "null":
			result = NULL;
			break;
		case "this":
			result = THIS;
			break;
		default:
			result = "";

		}

		return result;
	}
	

}
