import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class VMWriter {

	private PrintWriter pw;
	private String className;
	
	
	//this - class field variables
	
	
	public VMWriter(String className) throws FileNotFoundException, UnsupportedEncodingException {
		
		this.className = className;
		String outputFile = className + ".vm";
		
		pw = new PrintWriter(outputFile, "UTF-8");
		
	}
	

	protected void writePush(String segment, int index) {
		
		pw.println("push "+ segment + " " + index);
		
	}
	
	protected void writePop(String segment, int index) {
		
		pw.println("pop "+ segment + " " + index);
		
	}
	
	
	protected void writeArithmetic(String command) {
		
	
		
		if (command.equals("+")) {
			
			pw.println("add");
		
		} else if (command.equals("-")) {
			
			
			pw.println("sub");
			
		} else if (command.equals("*")) {
			
			pw.println("call Math.multiply 2");
			
		} else if (command.equals("/")) {
			
			pw.println("call Math.divide 2");
		} else if (command.equals("not")) {
			
			pw.println("not");
		} else if (command.equals("&gt;")) {
			
			pw.println("gt");
		} else if (command.equals("=")) {
			
			pw.println("eq");
		} else if (command.equals("&amp;")) {
			
			pw.println("and");
		}
	
	
		
	}
	
	protected void writeLabel(String label) {
		
		pw.println("label "+ label);
	}
	
	protected void writeGoto(String label) {
		
		pw.println("goto "+ label);
	}
	
	protected void writeIf(String label) {
		
		pw.println("if-goto "+ label);
	}
	
	
	//every VM method must return something
	protected void writeCall(String className, String name, int nArgs) {
		
		pw.println("call "+className+"."+name+" "+nArgs);
		
		
	}
	
	protected void writeFunction(String name, int nLocals) {
		
		pw.println("function "+className+"."+name+" "+nLocals);
		
	}
	
	protected void writeReturn() {
		
		pw.println("return");
	}
	
	protected void close() {
		
		pw.close();
	}
	
	protected void writeUnary() {
		
		pw.println("neg");
	}
}
