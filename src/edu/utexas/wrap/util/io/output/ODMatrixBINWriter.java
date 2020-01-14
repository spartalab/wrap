package edu.utexas.wrap.util.io.output;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.modechoice.Mode;

/**This class includes a method to write OD matrices to a file named using the following pattern:
 * 		./{outputDirectory}/{timePeriod}/{matrix.getVOT()}.bmtx
 * 
 * The file is encoded with each record requiring 12 bytes:
 * 		0x0-0x3	ORIGIN ID		(int)
 * 		0x4-0x7	DESTINATION ID	(int)
 * 		0x8-0xB	DEMAND			(float)
 * 
 * @author William
 *
 */
public class ODMatrixBINWriter {
	
	public static void write(String outputDirectory, TimePeriod timePeriod, ODMatrix matrix) {
		write(outputDirectory,timePeriod,matrix.getMode(),matrix.getVOT(),matrix);
	}
	
	public static void write(String outputDirectory, TimePeriod timePeriod, Mode mode, Float vot, ODMatrix matrix) {
		Path path = Paths.get(outputDirectory, 
				timePeriod.toString(), 
				mode.toString(), 
				vot.toString()+".bmtx");
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
