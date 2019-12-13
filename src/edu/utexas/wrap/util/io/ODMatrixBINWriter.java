package edu.utexas.wrap.util.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.demand.ODMatrix;

public class ODMatrixBINWriter {
	public static void write(String outputDirectory, TimePeriod timePeriod, ODMatrix matrix) {
		Path path = Paths.get(outputDirectory, 
				timePeriod.toString(), 
				matrix.getMode().toString(), 
				matrix.getVOT().toString()+".bmtx");
		try{
			Files.createDirectories(path.getParent());
			OutputStream out = Files.newOutputStream(path,
					StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			matrix.getGraph().getTSZs().parallelStream().forEach( orig -> {
				matrix.getGraph().getTSZs().parallelStream()
//				.filter(dest -> matrix.getDemand(orig,dest) > 0)
				.forEach(dest ->{
					try {
						float demand = matrix.getDemand(orig,dest);
						
						if (demand > 0)	out.write(
								ByteBuffer.allocate(2*Integer.BYTES+Float.BYTES)
								.putInt(orig.getNode().getID())
								.putInt(dest.getNode().getID())
								.putFloat(demand).array());
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
