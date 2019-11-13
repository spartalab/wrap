package edu.utexas.wrap.util.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.demand.ODMatrix;

public class ODMatrixWriter {

	public static void write(String outputDirectory, TimePeriod timePeriod, ODMatrix matrix) {
		try (BufferedWriter out = Files.newBufferedWriter(
				Paths.get(outputDirectory, timePeriod.toString(), matrix.getMode().toString(), matrix.getVOT().toString()+".matrix"),
				StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)){
			matrix.getGraph().getTSZs().parallelStream().forEach( orig -> {
				matrix.getGraph().getTSZs().parallelStream().filter(dest -> matrix.getDemand(orig,dest) > 0)
				.forEach(dest ->{
					try {
						StringBuilder sb = new StringBuilder();
						sb.append(orig.getNode().getID());
						sb.append(",");
						sb.append(dest.getNode().getID());
						sb.append(",");
						sb.append(matrix.getDemand(orig,dest));
						sb.append("\r\n");
						out.write(sb.toString());
						out.flush();
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
