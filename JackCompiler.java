import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

public class JackCompiler {

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {

		if (args.length > 0) {

			String input = args[0];

			File f = new File(input);

			if (f.isFile() && f.getName().endsWith(".jack")) {


				CompilationEngine compiler = new CompilationEngine(input);

			} else if (f.isDirectory()) {

				File dir = new File(input);

				for (File d : dir.listFiles()) {

					if (d.isFile() && d.getName().endsWith(".jack")) {


						CompilationEngine compiler = new CompilationEngine(dir + "/" + d.getName());

					}
				}

			}

		}
	}
	
}
