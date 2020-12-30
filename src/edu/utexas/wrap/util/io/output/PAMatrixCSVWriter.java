package edu.utexas.wrap.util.io.output;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import edu.utexas.wrap.demand.PAMatrix;

public class PAMatrixCSVWriter {

	public static void write(String outputDirectory,PAMatrix matrix) {
		Path path = Paths.get(outputDirectory,"paMatrix.csv");
		
		try {
			Files.createDirectories(path.getParent());
			BufferedWriter out = Files.newBufferedWriter(path, 
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
			out.write("Prod,Attr,Demand\r\n");
			matrix.getZones().parallelStream().forEach(
					
					prod -> {
						matrix.getZones().stream().forEach(
								
								attr ->{
									
									try {
										out.write(
												
												prod.getID()
												+","
												+attr.getID()
												+","
												+matrix.getDemand(prod, attr)
												+"\r\n"
												
												
												);
										
										
									} catch (IOException e) {
										e.printStackTrace();
									}
									
									
								}
								
								
								);
					}
					
					);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
