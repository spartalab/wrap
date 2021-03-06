package edu.utexas.wrap.util.io.output;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.demand.ODMatrix;

public class ODMatrixCSVWriter {

	public static void write(String outputDirectory, TimePeriod timePeriod, Float vot, ODMatrix matrix) {
		Path path = Paths.get(outputDirectory, 
				timePeriod.toString(), 
				matrix.getMode().toString(), 
				vot.toString()+".matrix");
		try{
			Files.createDirectories(path.getParent());
			BufferedWriter out = Files.newBufferedWriter(path,
					StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			matrix.getZones().parallelStream().forEach( orig -> {
				matrix.getZones().parallelStream()
//				.filter(dest -> matrix.getDemand(orig,dest) > 0)
				.forEach(dest ->{
					try {
						float demand = matrix.getDemand(orig, dest);
						if (demand > 0) {
							StringBuilder sb = new StringBuilder();
							sb.append(orig.getID());
							sb.append(",");
							sb.append(dest.getID());
							sb.append(",");
							sb.append(demand);
							sb.append("\r\n");
							out.write(sb.toString());
//							out.flush();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
