
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
	
	private String subroutineName="";
	private String subroutineCallClass="";
	private String subroutineCallMethod="";
	
	private VMWriter vm;
	
	private int expressionCount=0;
	
	private String className="";
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

			tknzr = new JackTokenizer(input);
			tokens = tknzr.getTokensList();

			int dot = input.indexOf('.');

			className = input.substring(0, dot);
			
			//xml for debug
			String outputFile = className + ".xml";
			writer = new PrintWriter(outputFile, "UTF-8");

			vm = new VMWriter(className);
			
			compileClass();
	
			
//			sym.printClassSymbol();
			
			vm.close();

		}

		
	}

	// A class - className { classVarDec, subrountineDec }
	private void compileClass() {

		//empty class and sub
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

				compileSubroutineDec();

			}
			currentTokenIndex++;

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
				currentToken= tokens.get(currentTokenIndex);
				
				
				
				if (tknzr.tokenType(currentToken).equals("field")||tknzr.tokenType(currentToken).equals("static")) {

					classSymbolType = currentToken;


					printToken();
					
					currentTokenIndex++;
					currentToken= tokens.get(currentTokenIndex);
					

				} else {
					
					currentTokenIndex++;
					currentToken= tokens.get(currentTokenIndex);

					classSymbolName = currentToken;
					
					
					//define new symbol in class symbol table
					int i = sym.varCount(classSymbolKind);
					
					writer.println("<classVar_"+classSymbolKind+"_"+classSymbolType+"_"+i+">" + currentToken + "</classVar_"+classSymbolKind+"_"+classSymbolType+"_"+i+">");
	
					sym.define(classSymbolName, classSymbolType, classSymbolKind, i);
					
					
					
				}

			
				
			} else if (tknzr.tokenType(currentToken).equals("keyword")) {
				
				if (currentToken.equals("static")||currentToken.equals("field")) {
					
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

		//reset subSym when compiling new subroutine
		sym.startSubroutine();
		sym.define("this", className, "argument", 0);
		
		
		
		
		writer.println("<subroutineDec>");

		while (!currentToken.equals("(")) {

			
			if(tknzr.tokenType(currentToken).equals("identifier")) {
				
				subroutineName = currentToken;
				
				writer.println("<subroutineName>"+currentToken+"</subroutineName>");
			
			} else {
				printToken();
			}
			
			

			currentTokenIndex++;
			currentToken = tokens.get(currentTokenIndex);
		}

		compileParameterList();
		
		vm.writeFunction(subroutineName, sym.varCount(subSymbolKind));
		
		compileSubroutineBody();

		
		
		writer.println("</subroutineDec>");

	}

	private void compileParameterList() {

		//(
		writer.print("<" + tknzr.tokenType(currentToken) + ">");
		writer.print(" " + currentToken + " ");
		writer.print("</" + tknzr.tokenType(currentToken) + ">");

		currentTokenIndex++;
		currentToken = tokens.get(currentTokenIndex);

		writer.println();
		writer.println("<parameterList>");

		
	
		
		while (!currentToken.equals(")")) {

			if (tknzr.tokenType(currentToken).equals("identifier")) {
				
				//currentToken is type if previous token is symbol else is name
				
				currentTokenIndex--;
				currentToken= tokens.get(currentTokenIndex);
				
				if (tknzr.tokenType(currentToken).equals("symbol")) {
					
					subSymbolType = currentToken;
					
					printToken();
					
					currentTokenIndex++;
					currentToken= tokens.get(currentTokenIndex);
					
					
				} else {
					
					currentTokenIndex++;
					currentToken= tokens.get(currentTokenIndex);
					
					subSymbolName = currentToken;
					subSymbolKind = "argument";
					
					int i = sym.varCount(subSymbolKind);
					
					
					
					writer.println("<subVar_"+subSymbolKind+"_"+subSymbolType+"_"+i+">"+currentToken+"</subVar_"+subSymbolKind+"_"+subSymbolType+"_"+i+">");
	
					sym.define(subSymbolName, subSymbolType, subSymbolKind, i);
					
		//			sym.printSubSymbol();
				}
				
				
			} else if (tknzr.tokenType(currentToken).equals("keyword")){
				
				subSymbolType = currentToken;
				printToken();
				
			} else {
				
				printToken();
			}
			
			

			currentTokenIndex++;
			currentToken = tokens.get(currentTokenIndex);
		}

		writer.println("</parameterList>");

		//)
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

		// Token!= let do if while
		while (!currentToken.equals("let") && !currentToken.equals("do") && !currentToken.equals("if")
				&& !currentToken.equals("while")) {

			if (currentToken.equals("var")) {

				compileVarDec();
			}

			currentTokenIndex++;
			currentToken = tokens.get(currentTokenIndex);
		}

		// statements
		compileStatements();

		currentToken = tokens.get(currentTokenIndex);

		printToken();

		writer.println("</subroutineBody>");

	}

	// TODO - 
	private void compileVarDec() {

		writer.println("<varDec>");

		//local variables are var
		subSymbolKind = "var";
		
		while (!currentToken.equals(";")) {

			if (tknzr.tokenType(currentToken).equals("keyword")) {
				
				
				if (!currentToken.equals("var")) {
					//after var
					subSymbolType = currentToken;
					printToken();
					
				} else {
					
					//var
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

					writer.println("<subVar_"+subSymbolKind+"_"+subSymbolType+"_"+i+">"+currentToken+"</subVar_"+subSymbolKind+"_"+subSymbolType+"_"+i+">");
	
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

		// let
		printToken();

		currentTokenIndex++;
		currentToken = tokens.get(currentTokenIndex);
		// varName
		printToken();

		currentTokenIndex++;
		currentToken = tokens.get(currentTokenIndex);

		if (currentToken.equals("[")) {

			printToken();
			currentTokenIndex++;
			currentToken = tokens.get(currentTokenIndex);

			compileExpression();

			// ]
			printToken();
			currentTokenIndex++;
			currentToken = tokens.get(currentTokenIndex);
		}

		if (currentToken.equals("=")) {

			printToken();

			currentTokenIndex++;
			currentToken = tokens.get(currentTokenIndex);
		}

		compileExpression();

		printToken();

		writer.println("</letStatement>");

	}

	private void compileIf() {

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

		compileStatements();

		// }
		printToken();

		// check else
		currentTokenIndex++;
		currentToken = tokens.get(currentTokenIndex);

		// if else
		if (currentToken.equals("else")) {

			// else
			printToken();
			currentTokenIndex++;
			currentToken = tokens.get(currentTokenIndex);

			// {
			printToken();
			currentTokenIndex++;
			currentToken = tokens.get(currentTokenIndex);

			compileStatements();

			// }
			printToken();

		} else {

			currentTokenIndex--;
			currentToken = tokens.get(currentTokenIndex);

		}

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

		// while
		printToken();
		currentTokenIndex++;
		currentToken = tokens.get(currentTokenIndex);

		// (
		printToken();

		currentTokenIndex++;
		currentToken = tokens.get(currentTokenIndex);

		compileExpression();

		// )
		printToken();

		// {
		currentTokenIndex++;
		currentToken = tokens.get(currentTokenIndex);
		printToken();

		compileStatements();

		// }
		printToken();

		writer.println("</whileStatement>");

	}

	private void compileDo() {

		writer.println("<doStatement>");

		//do
		printToken();
		currentTokenIndex++;
		currentToken = tokens.get(currentTokenIndex);
		
		//className
//		printToken();
		writer.println("<subroutineCallClass>"+currentToken+"</subroutineCallClass>");	
		className = currentToken;	
		
		currentTokenIndex++;
		currentToken = tokens.get(currentTokenIndex);
	
		//.
		printToken();
		currentTokenIndex++;
		currentToken = tokens.get(currentTokenIndex);
		
		//method
//		printToken();
		writer.println("<subroutineCallMethod>"+currentToken+"</subroutineCallMethod>");
		subroutineCallMethod = currentToken;	
		currentTokenIndex++;
		currentToken = tokens.get(currentTokenIndex);

		/*
		 * while (!currentToken.equals("(")) {
		 * 
		 * 
		 * printToken();
		 * 
		 * currentTokenIndex++; currentToken = tokens.get(currentTokenIndex); }
		 */

		//(
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
		printToken();

		//every VM method must return something - 
		//do - return value not relevant
		//pop off return value to temp 0 from the stack because we don't want the return value
		vm.writeCall(className, subroutineCallMethod, expressionCount);
		//do return value stores at temp 0 - 
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

		//if current function is void - return 0
		//if not return the retuen value
		vm.writePush("constant", 0);
				
		vm.writeReturn();
		
		writer.println("</returnStatement>");

	}

	private void compileExpression() {


		String expOp="";
		
		writer.println("<expression>");

		compileTerm();

		currentTokenIndex++;
		currentToken = tokens.get(currentTokenIndex);

		while (!currentToken.equals(")") && !currentToken.equals(";") && !currentToken.equals("]")) {

			
			if (OP.contains(currentToken)) {

				printToken();
				
				//VMCommands
				expOp=currentToken;
				

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

		
		writer.println("<term>");

		printToken();
		
		//VM commands
		if(tknzr.tokenType(currentToken).equals("integerConstant")) {
			
			vm.writePush("constant", Integer.parseInt(currentToken));
		}
		
		
		
		
		
		//XML output
		if (tknzr.tokenType(currentToken).equals("identifier")) {

			
			currentTokenIndex++;
			currentToken = tokens.get(currentTokenIndex);

			// subroutineCall
			if (currentToken.equals(".")) {

				while (!currentToken.equals(";")) {

					if (currentToken.equals("(")) {

						// (
						printToken();

						currentTokenIndex++;
						currentToken = tokens.get(currentTokenIndex);

						compileExpressionList();

						printToken();
						break;
					}

					printToken();
					currentTokenIndex++;
					currentToken = tokens.get(currentTokenIndex);
				}

			} else if (currentToken.equals("[")) {

				printToken();
				currentTokenIndex++;
				currentToken = tokens.get(currentTokenIndex);

				compileExpression();
				printToken();

			} else {

				// reverse
				currentTokenIndex--;
				currentToken = tokens.get(currentTokenIndex);

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