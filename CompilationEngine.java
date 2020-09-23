
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class CompilationEngine {

	private JackTokenizer tknzr;
	private ArrayList<String> tokens;
	private String currentToken;
	private PrintWriter writer;
	private int currentTokenIndex;
	private ArrayList<String> OP = new ArrayList<>();
	private ArrayList<String> UN = new ArrayList<>();

	private SymbolTable sym;
	private String classSymbolType = "";
	private String classSymbolKind = "";
	private String classSymbolName = "";

	private String subSymbolType = "";
	private String subSymbolKind = "";
	private String subSymbolName = "";

	private String subroutineName = "";
	private String subroutineCallClass = "";
	private String subroutineCallMethod = "";

	private boolean isSubCallMethod = false;
	private boolean isObjMethod = false;
	private boolean isVoid = false;
	private boolean isArrayVar = false;

	private VMWriter vm;

	private int expressionCount = 0;

	private String className = "";

	private int whileLabelCount = -1;
	private int ifLabelCount = -1;

	private boolean isMethod = false;
	private boolean isConstructor = false;

	public CompilationEngine(String input) throws FileNotFoundException, UnsupportedEncodingException {

		// operands
		OP.add("+");
		OP.add("-");
		OP.add("*");
		OP.add("/");
		OP.add("&amp;");
		OP.add("|");
		OP.add("&lt;");
		OP.add("&gt;");
		OP.add("=");

		// UNARY
		UN.add("-");
		UN.add("~");

		File f = new File(input);

		if (f.isFile()) {

			// TODO fix diretory input

			tknzr = new JackTokenizer(input);
			tokens = tknzr.getTokensList();

			int slash = input.indexOf('/');
			int dot = input.indexOf('.');
			String outputFile = "";

			if (slash != -1) {

				className = input.substring(slash + 1, dot);
				// xml for debug
				outputFile = input.substring(0, dot);

			} else {

				className = input.substring(0, dot);
				// xml for debug
				outputFile = className;
			}

			writer = new PrintWriter(outputFile + ".xml", "UTF-8");

			vm = new VMWriter(className, outputFile);

			compileClass();

//			sym.printClassSymbol();

			vm.close();

		}

	}

	// A class - className { classVarDec, subrountineDec }
	private void compileClass() {

		// empty class and sub
		sym = new SymbolTable();

		currentTokenIndex = 0;

		writer.println("<class>");

		// 0
		currentToken = tokens.get(currentTokenIndex);

		int j = 0;

		// write the first line
		while (j < 3) {

			printToken();

			currentTokenIndex++;
			currentToken = tokens.get(currentTokenIndex);

			j++;
		}

		// compile the rest of the class
		// starts with keywords and ends with semicolon before functions/methods

		while (currentToken.equals("static") || currentToken.equals("field")) {

			compileClassVarDec();

			currentTokenIndex++;
			currentToken = tokens.get(currentTokenIndex);

		}

		while (currentTokenIndex < tokens.size() - 1) {

			currentToken = tokens.get(currentTokenIndex);
			// loop through all subroutineDecs
			if (currentToken.equals("constructor") || currentToken.equals("function") || currentToken.equals("method")
					|| currentToken.equals("void")) {

				if (currentToken.equals("method")) {
					isMethod = true;
				} else if (currentToken.equals("constructor")) {

					isConstructor = true;
				}

				compileSubroutineDec();

			}

			currentTokenIndex++;
			isMethod = false;

		}

		// right curly brace - end of class
		currentToken = tokens.get(tokens.size() - 1);
		printToken();

		// end class
		writer.println("</class>");
		writer.close();

	}

	// TODO extend handling of identifiers - static / field , class + index
	private void compileClassVarDec() {

		writer.println("<classVarDec>");

		while (!currentToken.equals(";")) {

			if (tknzr.tokenType(currentToken).equals("identifier")) {

				// if previous token is keyword static/field then currentToken is type
				// variables always start with keyword

				currentTokenIndex--;
				currentToken = tokens.get(currentTokenIndex);

				if (currentToken.equals("field") || currentToken.equals("static")) {

					currentTokenIndex++;
					currentToken = tokens.get(currentTokenIndex);

					classSymbolType = currentToken;

					printToken();

				} else {

					currentTokenIndex++;
					currentToken = tokens.get(currentTokenIndex);

					classSymbolName = currentToken;

					// define new symbol in class symbol table
					int i = sym.varCount(classSymbolKind);

					writer.println("<classVar_" + classSymbolKind + "_" + classSymbolType + "_" + i + ">" + currentToken
							+ "</classVar_" + classSymbolKind + "_" + classSymbolType + "_" + i + ">");

					sym.define(classSymbolName, classSymbolType, classSymbolKind, i);

				}

			} else if (tknzr.tokenType(currentToken).equals("keyword")) {

				if (currentToken.equals("static") || currentToken.equals("field")) {

					classSymbolKind = currentToken;

					printToken();
				} else {

					classSymbolType = currentToken;

					printToken();
				}

			} else {

				printToken();
			}

			currentTokenIndex++;
			currentToken = tokens.get(currentTokenIndex);
		}

		printToken();

		writer.println("</classVarDec>");

	}

	private void compileSubroutineDec() {

		isVoid = false;
		// reset subSym when compiling new subroutine
		sym.startSubroutine();
		// method has arument 0 as this
		if (isMethod) {
			sym.define("this", className, "argument", 0);
		}

		writer.println("<subroutineDec>");

		while (!currentToken.equals("(")) {

			if (tknzr.tokenType(currentToken).equals("identifier")) {

				subroutineName = currentToken;

				writer.println("<subroutineName>" + currentToken + "</subroutineName>");

			} else if (tknzr.tokenType(currentToken).equals("keyword")) {

				if (currentToken.equals("void")) {
					isVoid = true;
				}

				printToken();
			} else {

				printToken();
			}

			currentTokenIndex++;
			currentToken = tokens.get(currentTokenIndex);
		}

		compileParameterList();

		compileSubroutineBody();

		writer.println("</subroutineDec>");

	}

	private void compileParameterList() {

		// (
		writer.print("<" + tknzr.tokenType(currentToken) + ">");
		writer.print(" " + currentToken + " ");
		writer.print("</" + tknzr.tokenType(currentToken) + ">");

		currentTokenIndex++;
		currentToken = tokens.get(currentTokenIndex);

		writer.println();
		writer.println("<parameterList>");

		while (!currentToken.equals(")")) {

			if (tknzr.tokenType(currentToken).equals("identifier")) {

				// currentToken is type if previous token is symbol else is name

				currentTokenIndex--;
				currentToken = tokens.get(currentTokenIndex);

				if (tknzr.tokenType(currentToken).equals("symbol")) {

					subSymbolType = currentToken;

					printToken();

					currentTokenIndex++;
					currentToken = tokens.get(currentTokenIndex);

				} else {

					currentTokenIndex++;
					currentToken = tokens.get(currentTokenIndex);

					subSymbolName = currentToken;
					subSymbolKind = "argument";

					int i = sym.varCount(subSymbolKind);

					writer.println("<subVar_" + subSymbolKind + "_" + subSymbolType + "_" + i + ">" + currentToken
							+ "</subVar_" + subSymbolKind + "_" + subSymbolType + "_" + i + ">");

					sym.define(subSymbolName, subSymbolType, subSymbolKind, i);

					// sym.printSubSymbol();
				}

			} else if (tknzr.tokenType(currentToken).equals("keyword")) {

				subSymbolType = currentToken;
				printToken();

			} else {

				printToken();
			}

			currentTokenIndex++;
			currentToken = tokens.get(currentTokenIndex);
		}

		subSymbolName = "";

		writer.println("</parameterList>");

		// )
		printToken();

	}

	private void compileSubroutineBody() {

		writer.println("<subroutineBody>");

		// {
		currentTokenIndex++;
		currentToken = tokens.get(currentTokenIndex);

		printToken();

		currentTokenIndex++;
		currentToken = tokens.get(currentTokenIndex);

		// Token!= let do if while return
		while (!currentToken.equals("let") && !currentToken.equals("do") && !currentToken.equals("if")
				&& !currentToken.equals("while") && !currentToken.equals("return")) {

			if (currentToken.equals("var")) {

				compileVarDec();
			}

			currentTokenIndex++;
			currentToken = tokens.get(currentTokenIndex);
		}

		vm.writeFunction(subroutineName, sym.varCount("local"));

		if (isMethod) {

			vm.writePush("argument", 0);
			vm.writePop("pointer", 0);
		}

		// Compile Constructor
		if (isConstructor) {

			vm.writePush("constant", sym.varCount("field"));

			vm.writeMemoryAlloc();

			// anchor this at base address
			vm.writePop("pointer", 0);
		}

		// statements
		compileStatements();

		currentToken = tokens.get(currentTokenIndex);

		printToken();

		isMethod = false;
		isConstructor = false;

		writer.println("</subroutineBody>");

	}

	// TODO -
	private void compileVarDec() {

		writer.println("<varDec>");

		// local variables are var
		subSymbolKind = "local";

		while (!currentToken.equals(";")) {

			if (tknzr.tokenType(currentToken).equals("keyword")) {

				if (!currentToken.equals("var")) {
					// after var
					subSymbolType = currentToken;
					printToken();

				} else {

					// var
					printToken();
				}

			} else if (tknzr.tokenType(currentToken).equals("identifier")) {

				currentTokenIndex--;
				currentToken = tokens.get(currentTokenIndex);

				if (currentToken.equals("var")) {

					currentTokenIndex++;
					currentToken = tokens.get(currentTokenIndex);

					subSymbolType = currentToken;

					printToken();

				} else {

					currentTokenIndex++;
					currentToken = tokens.get(currentTokenIndex);

					subSymbolName = currentToken;

					int i = sym.varCount(subSymbolKind);

					writer.println("<subVar_" + subSymbolKind + "_" + subSymbolType + "_" + i + ">" + currentToken
							+ "</subVar_" + subSymbolKind + "_" + subSymbolType + "_" + i + ">");

					sym.define(subSymbolName, subSymbolType, subSymbolKind, i);

				}

			} else {

				printToken();
			}

			currentTokenIndex++;
			currentToken = tokens.get(currentTokenIndex);
		}

		printToken();
		writer.println("</varDec>");
	}

	private void compileStatements() {

		writer.println("<statements>");

		// compile statemnets inside subroutine
		while (!currentToken.equals("}")) {

			if (currentToken.equals("let")) {

				compileLet();

			} else if (currentToken.equals("do")) {

				compileDo();

			} else if (currentToken.equals("while")) {

				compileWhile();

			} else if (currentToken.equals("if")) {

				compileIf();

			} else if (currentToken.equals("return")) {

				compileReturn();
			}

			currentTokenIndex++;
			currentToken = tokens.get(currentTokenIndex);

		}

		writer.println("</statements>");
	}

	private void compileLet() {
		writer.println("<letStatement>");

		String varName = "";

		// let
		printToken();

		currentTokenIndex++;
		currentToken = tokens.get(currentTokenIndex);

		// VM varName
		varName = currentToken;

		if (sym.getClassSymbol().containsKey(currentToken)) {

			classSymbolKind = sym.kindOf(currentToken);
			classSymbolType = sym.typeOf(currentToken);
			int i = sym.indexOf(currentToken);

			writer.println("<classVar_" + classSymbolKind + "_" + classSymbolType + "_" + i + ">" + currentToken
					+ "</classVar_" + classSymbolKind + "_" + classSymbolType + "_" + i + ">");

		} else if (sym.getSubSymbol().containsKey(currentToken)) {

			subSymbolKind = sym.kindOf(currentToken);
			subSymbolType = sym.typeOf(currentToken);
			int i = sym.indexOf(currentToken);
			subSymbolName = currentToken;

			writer.println("<classVar_" + subSymbolKind + "_" + subSymbolType + "_" + i + ">" + currentToken
					+ "</classVar_" + subSymbolKind + "_" + subSymbolType + "_" + i + ">");
		}

		currentTokenIndex++;
		currentToken = tokens.get(currentTokenIndex);

		// TODO finish Array
		if (currentToken.equals("[")) {

			isArrayVar = true;

			//[
			printToken();	
			currentTokenIndex++;
			currentToken = tokens.get(currentTokenIndex);

			
			compileExpression();

			// ]
			printToken();
			currentTokenIndex++;
			currentToken = tokens.get(currentTokenIndex);

			
			// add index and array address
			vm.writePush(sym.kindOf(varName), sym.indexOf(varName));
			vm.writeArithmetic("+");
		}

		if (currentToken.equals("=")) {

			printToken();

			currentTokenIndex++;
			currentToken = tokens.get(currentTokenIndex);
		}

		compileExpression();

		if (isArrayVar) {

			// store expression value on temp 0
			vm.writePop("temp", 0);
			// align varName+index to pointer 1 that
			vm.writePop("pointer", 1);

			// push expression value back on stack
			vm.writePush("temp", 0);
			// pop expression value to pointer 1 that
			vm.writePop("that", 0);

		} else if (sym.kindOf(varName).equals("field")) {

			vm.writePop("this", sym.indexOf(varName));

		} else {

			vm.writePop(sym.kindOf(varName), sym.indexOf(varName));
		}

		isArrayVar = false;
		subSymbolName = "";
		varName = "";
		subSymbolKind = "";
		subSymbolType = "";

		printToken();

		writer.println("</letStatement>");

	}

	// TODO - fix LABELS
	private void compileIf() {

		ifLabelCount++;

		int count = ifLabelCount;

		writer.println("<ifStatement>");

		// if
		printToken();

		// (
		currentTokenIndex++;
		currentToken = tokens.get(currentTokenIndex);

		while (!currentToken.equals("{")) {

			if (currentToken.equals("(")) {

				printToken();

				currentTokenIndex++;
				currentToken = tokens.get(currentTokenIndex);

				compileExpression();

				// VM - negate condition
//				vm.writeArithmetic("not");

				currentTokenIndex--;
				currentToken = tokens.get(currentTokenIndex);
			}

			currentTokenIndex++;
			currentToken = tokens.get(currentTokenIndex);
		}

		currentTokenIndex--;
		currentToken = tokens.get(currentTokenIndex);
		printToken();

		currentTokenIndex++;
		currentToken = tokens.get(currentTokenIndex);
		// {
		printToken();

		// VM if else command
		vm.writeIf("IF_TRUE" + ifLabelCount);
		vm.writeGoto("IF_FALSE" + ifLabelCount);

		// VM if true
		vm.writeLabel("IF_TRUE" + ifLabelCount);

		compileStatements();

		// }
		printToken();

		// check else
		currentTokenIndex++;
		currentToken = tokens.get(currentTokenIndex);

		// else
		if (currentToken.equals("else")) {

			// else
			printToken();
			currentTokenIndex++;
			currentToken = tokens.get(currentTokenIndex);

			// {
			printToken();
			currentTokenIndex++;
			currentToken = tokens.get(currentTokenIndex);

			// VM goto
			vm.writeGoto("IF_END" + count);
			// VM if FALSE
			vm.writeLabel("IF_FALSE" + count);

			compileStatements();

			// }
			printToken();

			// VM end
			vm.writeLabel("IF_END" + count);

		} else {

			currentTokenIndex--;
			currentToken = tokens.get(currentTokenIndex);
			vm.writeLabel("IF_FALSE" + ifLabelCount);

		}

//		vm.writeLabel("IF_FALSE" + ifLabelCount);

		writer.println("</ifStatement>");

	}

	private void printToken() {

		writer.print("<" + tknzr.tokenType(currentToken) + ">");
		writer.print(" " + currentToken + " ");
		writer.print("</" + tknzr.tokenType(currentToken) + ">");
		writer.println();
	}

	private void compileWhile() {
		writer.println("<whileStatement>");

		whileLabelCount++;

		// while
		printToken();
		currentTokenIndex++;
		currentToken = tokens.get(currentTokenIndex);

		// VM
		vm.writeLabel("WHILE_EXP" + whileLabelCount);

		// (
		printToken();

		currentTokenIndex++;
		currentToken = tokens.get(currentTokenIndex);

		compileExpression();

		// negate condition
		vm.writeArithmetic("not");

		// )
		printToken();

		// {
		currentTokenIndex++;
		currentToken = tokens.get(currentTokenIndex);
		printToken();

		// if-goto label
		vm.writeIf("WHILE_END" + whileLabelCount);

		int count = whileLabelCount;

		compileStatements();

		vm.writeGoto("WHILE_EXP" + count);

		vm.writeLabel("WHILE_END" + count);
		// }
		printToken();

		writer.println("</whileStatement>");

		whileLabelCount++;

	}

	private void compileDo() {

		writer.println("<doStatement>");
		
		
		// do
		printToken();
		currentTokenIndex++;
		currentToken = tokens.get(currentTokenIndex);

		// if varName is symbol
		// push varName to the stack - method on obj
		if (sym.getSubSymbol().containsKey(currentToken) || sym.getClassSymbol().containsKey(currentToken)) {
			
			if (sym.getSubSymbol().containsKey(currentToken)) {

				if (sym.kindOf(currentToken).equals("local")) {

					writer.println("<subVar_" + sym.kindOf(currentToken) + "_" + sym.indexOf(currentToken)
							+ currentToken + "</subVar_" + sym.kindOf(currentToken) + "_" + sym.indexOf(currentToken));
					
					
					vm.writePush("local", sym.indexOf(currentToken));
					subroutineCallClass = sym.typeOf(currentToken);
					isObjMethod = true;

				} else {

					writer.println("<subVar_" + sym.kindOf(currentToken) + "_" + sym.indexOf(currentToken)
							+ currentToken + "</subVar_" + sym.kindOf(currentToken) + "_" + sym.indexOf(currentToken));

					vm.writePush("argument", sym.indexOf(currentToken));
					subroutineCallClass = sym.typeOf(currentToken);
					isObjMethod = true;
				}
			} else {

				if (sym.kindOf(currentToken).equals("field")) {

					writer.println("<subVar_" + sym.kindOf(currentToken) + "_" + sym.indexOf(currentToken)
							+ currentToken + "</subVar_" + sym.kindOf(currentToken) + "_" + sym.indexOf(currentToken));

					vm.writePush("this", sym.indexOf(currentToken));
					subroutineCallClass = sym.typeOf(currentToken);
					isObjMethod = true;

				} else {

					writer.println("<subVar_" + sym.kindOf(currentToken) + "_" + sym.indexOf(currentToken)
							+ currentToken + "</subVar_" + sym.kindOf(currentToken) + "_" + sym.indexOf(currentToken));

					vm.writePush("static", sym.indexOf(currentToken));
					subroutineCallClass = sym.typeOf(currentToken);
					isObjMethod = true;
				}
			}
			// is library
		} else if (Character.isUpperCase(currentToken.charAt(0))) {

			writer.println("<subroutineCallClass>" + currentToken + "</subroutineCallClass>");
			subroutineCallClass = currentToken;

			// is method
		} else {

			writer.println("<subroutineCallMethod>" + currentToken + "</subroutineCallMethod>");
			isSubCallMethod = true;
			subroutineCallMethod = currentToken;
			subroutineCallClass = className;
		}

		currentTokenIndex++;
		currentToken = tokens.get(currentTokenIndex);

		if (currentToken.equals(".")) {

			printToken();

			currentTokenIndex++;
			currentToken = tokens.get(currentTokenIndex);

			// method
			writer.println("<subroutineCallMethod>" + currentToken + "</subroutineCallMethod>");
			subroutineCallMethod = currentToken;
			currentTokenIndex++;
			currentToken = tokens.get(currentTokenIndex);

			// (
			printToken();
			currentTokenIndex++;
			currentToken = tokens.get(currentTokenIndex);

			expressionCount = 0;

			compileExpressionList();

			while (!currentToken.equals(";")) {

				printToken();

				currentTokenIndex++;
				currentToken = tokens.get(currentTokenIndex);

			}

			// ;
			printToken();

		} else {

			
			printToken();
			currentTokenIndex++;
			currentToken = tokens.get(currentTokenIndex);

			compileExpressionList();

			printToken();
			currentTokenIndex++;
			currentToken = tokens.get(currentTokenIndex);

			printToken();

		}

		
		
		// every VM method must return something -
		// do - return value not relevant
		// pop off return value to temp 0 from the stack because we don't want the
		// return value
		// TODO - fix argument on object methodcall
		if (isSubCallMethod) {

			vm.writePush("pointer", 0);

			vm.writeCall(subroutineCallClass, subroutineCallMethod, 1);

		} else if (isObjMethod) {
			
			
			vm.writeCall(subroutineCallClass, subroutineCallMethod, 1);

		} else {

			
			vm.writeCall(subroutineCallClass, subroutineCallMethod, expressionCount);
		}

		isObjMethod = false;
		isSubCallMethod = false;
		subroutineCallClass = "";

		// do return value stores at temp 0 -
		vm.writePop("temp", 0);

		writer.println("</doStatement>");

	}

	private void compileReturn() {

		writer.println("<returnStatement>");

		// return
		printToken();

		currentTokenIndex++;
		currentToken = tokens.get(currentTokenIndex);

		if (!currentToken.equals(";")) {

			compileExpression();

		}

		printToken();

		// if current function is void - return 0
		if (isVoid) {

			vm.writePush("constant", 0);

		}

		vm.writeReturn();

		writer.println("</returnStatement>");

	}

	private void compileExpression() {

		String expOp = "";

		writer.println("<expression>");

		compileTerm();

		currentTokenIndex++;
		currentToken = tokens.get(currentTokenIndex);

		while (!currentToken.equals(")") && !currentToken.equals(";") && !currentToken.equals("]")) {

			if (OP.contains(currentToken)) {

				printToken();

				// VMCommands
				expOp = currentToken;

			} else if (currentToken.equals(",")) {

				break;

			} else {

				compileTerm();

			}

			currentTokenIndex++;
			currentToken = tokens.get(currentTokenIndex);
		}

		vm.writeArithmetic(expOp);

		writer.println("</expression>");

	}

	private void compileTerm() {

		boolean isNot = false;
		boolean isNeg = false;
		boolean isArrayIndex = false;
		
		
		writer.println("<term>");

		//TODO finish obj method calls
		
		// VM commands
		if (tknzr.tokenType(currentToken).equals("integerConstant")) {

			vm.writePush("constant", Integer.parseInt(currentToken));
			printToken();

		} else if (tknzr.tokenType(currentToken).equals("identifier")) {

			
			
			if (sym.getClassSymbol().containsKey(currentToken)) {

				
				classSymbolKind = sym.kindOf(currentToken);
				classSymbolType = sym.typeOf(currentToken);
				classSymbolName = currentToken;
				int i = sym.indexOf(currentToken);
				subroutineCallClass = classSymbolType;
				

				writer.println("<classVar_" + classSymbolKind + "_" + classSymbolType + "_" + i + ">" + currentToken
						+ "</classVar_" + classSymbolKind + "_" + classSymbolType + "_" + i + ">");

				if (sym.kindOf(currentToken).equals("field")) {

					vm.writePush("this", sym.indexOf(classSymbolName));
				} else {

					vm.writePush("static", sym.indexOf(classSymbolName));
				}

			} else if (sym.getSubSymbol().containsKey(currentToken)) {

				
				subSymbolKind = sym.kindOf(currentToken);
				subSymbolType = sym.typeOf(currentToken);
				subSymbolName = currentToken;
				int i = sym.indexOf(currentToken);
//				subroutineCallClass = subSymbolType;
				
				
				writer.println("<subVar_" + subSymbolKind + "_" + subSymbolType + "_" + i + ">" + currentToken
						+ "</subVar_" + subSymbolKind + "_" + subSymbolType + "_" + i + ">");

				if (sym.kindOf(currentToken).equals("local")) {

					vm.writePush("local", sym.indexOf(subSymbolName));
				} else {

					vm.writePush("argument", sym.indexOf(subSymbolName));
				}

			} else {

				subroutineCallClass = currentToken;
				writer.println("<subroutineCallClass>" + currentToken + "</subroutineCallClass>");

			}
		} else if (tknzr.tokenType(currentToken).equals("keyword")) {

			if (currentToken.equals("true")) {

				// 1 : true 0 : false
				vm.writePush("constant", 0);
				vm.writeArithmetic("not");

			} else if (currentToken.equals("false")) {

				vm.writePush("constant", 0);

			} else if (currentToken.equals("this")) {
				vm.writePush("pointer", 0);

			} else if (currentToken.equals("null")) {
				vm.writePush("constant", 0);
			}

			printToken();

			// is String
		} else if (tknzr.tokenType(currentToken).equals("stringConstant")) {

			printToken();

			int count = currentToken.length();

			// push total letter counts to stack
			// construct new string
			vm.writePush("constant", count);
			vm.writeCall("String", "new", 1);

			char[] c = currentToken.toCharArray();

			for (char ch : c) {

				int ascii = ch;

				vm.writePush("constant", ascii);
				vm.writeCall("String", "appendChar", 2);

			}

		} else {

			printToken();
		}

		if (currentToken.equals("~")) {

			isNot = true;
		}

		if (currentToken.equals("-")) {

			isNeg = true;

		}

		if (currentToken.equals("[")) {

			isArrayIndex = true;

		}

		// XML output
		if (tknzr.tokenType(currentToken).equals("identifier")) {

			currentTokenIndex++;
			currentToken = tokens.get(currentTokenIndex);

			// subroutineCall
			if (currentToken.equals(".")) {

				while (!currentToken.equals(";")) {

					if (tknzr.tokenType(currentToken).equals("identifier")) {

						subroutineCallMethod = currentToken;
					}

					if (currentToken.equals("(")) {

						// (
						printToken();

						currentTokenIndex++;
						currentToken = tokens.get(currentTokenIndex);

						expressionCount = 0;

						compileExpressionList();

						
						
						
						vm.writeCall(subroutineCallClass, subroutineCallMethod, expressionCount);

						printToken();
						break;
					}

					printToken();
					currentTokenIndex++;
					currentToken = tokens.get(currentTokenIndex);
				}

			} else if (currentToken.equals("[")) {

				isArrayIndex = true;
				printToken();
				currentTokenIndex++;
				currentToken = tokens.get(currentTokenIndex);

				compileExpression();
				printToken();

			} else {

				// reverse
				currentTokenIndex--;
				currentToken = tokens.get(currentTokenIndex);

				// TODO VM look up variable from symbol table

			}

		}

		if (currentToken.equals("(")) {
			currentTokenIndex++;
			currentToken = tokens.get(currentTokenIndex);
			compileExpression();

			printToken();

		}

		if (UN.contains(currentToken)) {
			currentTokenIndex++;
			currentToken = tokens.get(currentTokenIndex);
			compileTerm();

		}

		if (isNeg) {
			vm.writeUnary();

		}

		if (isNot) {
			vm.writeArithmetic("not");
		}

		if (isArrayIndex) {

			
			vm.writeArithmetic("+");
			vm.writePop("pointer", 1);
			vm.writePush("that", 0);

		}

//		subroutineCallClass="";
		isArrayIndex = false;

		writer.println("</term>");
	}

	// comma separated list of expressions
	private void compileExpressionList() {

		writer.println("<expressionList>");

		if (!currentToken.equals(")")) {

			while (!currentToken.equals(")")) {

				if (currentToken.equals(",")) {

					printToken();

				} else {

					compileExpression();
					currentTokenIndex--;
					currentToken = tokens.get(currentTokenIndex);

					expressionCount++;
				}

				currentTokenIndex++;
				currentToken = tokens.get(currentTokenIndex);

			}

		}

		writer.println("</expressionList>");
	}
}