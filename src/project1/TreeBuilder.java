package project1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class TreeBuilder {

/**
 **	 Parameters: The pathname of the users directory which contains code to parse.
 **  Convert the contents of all java files in the directory to one string.
 **	 Return: The string of code from the directory.
 * @param counter
 **/
	public TypeCounter parseDir(String pathname, TypeCounter counter, String[] classpath, String[] sources ) throws IOException, FileNotFoundException {

		File directory = new File(pathname);
		File[] allFiles = directory.listFiles();

		if (allFiles == null) return null;


		for (File f : allFiles) {
			String fileName = f.getName().toLowerCase();
			// If is directory, parse files within it recursively
			if	(f.isDirectory()){
				StringBuilder sb = new StringBuilder();
				sb.append(parseDir(f.getAbsolutePath(),counter, classpath, sources));
			}
			// If is jar file, parse files within it
			if (f.getName().endsWith(".jar")) {
				StringBuilder sb = new StringBuilder();
				counter = parseJar(f,pathname,sb,counter,classpath,sources);
			}
			// If is java file, read file and append to StringBuilder
			if (f.isFile() && fileName.endsWith(".java")) {
				counter = countTree(counter, f, classpath, sources);

			}
		}
		return counter;
	}
	

	/**
	 **	 Runs through elements in Jar file and will read any Java files into the StringBuilder object
	 **/
	public TypeCounter parseJar(File jarFile, String dirPath, StringBuilder sb, TypeCounter counter,
		String[] classpath, String[] sources) throws IOException {

		// Create JarFile object and check for entries
		JarFile jFile = new JarFile(jarFile);
		Enumeration<JarEntry> jEntries = jFile.entries();

		// Loop through entries
		while(jEntries.hasMoreElements()) {

			// Find java files to read into StringBuilder
			ZipEntry jarElem = jEntries.nextElement();
			String jarName = jarElem.getName();
			
			// Create java file to count from jar element
			if (jarName.endsWith(".java")) {
				// Convert jar entry into java file
				String javaFilePath = null;
				if (jarName.contains(".java")) {
					javaFilePath = dirPath + "temp.java";
					InputStream input = jFile.getInputStream(jarElem);
					Files.copy(input, Paths.get(javaFilePath));
				}

				// Count in java file
				if (javaFilePath != null) {
					File javaFile = new File(javaFilePath);
					counter = countTree(counter, javaFile, classpath, sources);
					javaFile.delete();
				}
			}
		}
		jFile.close();
		return counter;
	}
	

/**
 **	 Parameters: The string of code to parse, all parameters needed to set up bindings, unitname, and environment
 **  Build a tree out of the user's code
 **	 Return: The starting node
 **/
	public ASTNode makeSyntaxTree(char[] sourceCode, String[] classpath, String[] sources, String unitName ) {

		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(sourceCode);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		Map<String, String> options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
		parser.setCompilerOptions(options);
		parser.setUnitName(unitName);
		parser.setEnvironment(classpath, sources, new String[] {"UTF-8"}, true);

		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		return cu;
	}
	
	/**
	 **	 Makes AST from given Java file and counts it
	 **/
	public TypeCounter countTree(TypeCounter counter, File f, String[] classpath, String[] sources) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb = readJavaFile(f,sb);
		String javaCode = sb.toString();
		ASTNode cu = makeSyntaxTree(javaCode.toCharArray(), classpath, sources, javaCode);
		counter.count(cu);
		counter.countVarDec(cu);
		return counter;
	}

	
	/**
	 **	 Reads through Java file and adds each line into StringBuilder object
	 **/
	public StringBuilder readJavaFile (File javaFile, StringBuilder sb) throws IOException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(javaFile));
			String aLine;

			while ((aLine = reader.readLine()) != null) {
				sb.append(aLine);
				sb.append(System.lineSeparator());
			}
		} finally {
			reader.close();
		}
		return sb;
	}
}
