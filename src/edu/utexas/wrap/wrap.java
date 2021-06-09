package edu.utexas.wrap;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class wrap {
	static Path projDir, projFile;
	static Project proj;
	
	public static void main(String[] args) {
		//Get a path to the project file (a Properties file of arbitrary extension)
		if (args.length < 1) {
			System.err.println("No model input file supplied");
			System.exit(1);
		}
		projFile = Paths.get(args[0]);
		
		
		//Load the project from the given path
		try {
			proj = new Project(projFile);
		} catch (IOException e) {
			System.err.println("Error loading project properties");
			e.printStackTrace();
			proj = null;
			System.exit(-1);
		}
		
		proj.run();
	}
}
