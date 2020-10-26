package edu.utexas.wrap.assignment.bush;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import edu.utexas.wrap.assignment.AssignmentConsumer;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;

public class BushWriter implements AssignmentConsumer<Bush> {
	private Graph network;
	private Path outputPath;

	public BushWriter(Graph network, Path ioPath) throws IOException {
		this.network = network;
		this.outputPath = ioPath;
		Files.createDirectories(outputPath
		.resolve(network.toString()));
	}
	
	public void consumeStructure(Bush bush) throws IOException {
		OutputStream out = Files.newOutputStream(
				outputPath
				.resolve(network.toString())
				.resolve(Integer.toString(bush.hashCode())),
				
				StandardOpenOption.CREATE, 
				StandardOpenOption.TRUNCATE_EXISTING
				);
		writeToStream(bush, out);
		bush.setQ(null);
		bush.clear();
		out.close();
	}
	
	private void writeToStream(Bush bush, OutputStream out) {
		int size = Integer.BYTES*2+Double.BYTES; //Size of each link's data
		
		
		//For each node
		network.getNodes().parallelStream().filter(n -> n != null).forEach(n ->{
			BackVector qn = bush.getBackVector(n);
			//get all the links leading to the node
			//write them to a file
			if (qn instanceof Link) {
				byte[] b = ByteBuffer.allocate(size)
						.putInt(n.getID())
						.putInt(((Link) qn).hashCode())
						.putDouble(1.0)
						.array();
				try {
					out.write(b);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			else if (qn instanceof BushMerge) {
				BushMerge qm = (BushMerge) qn;
				qm.getLinks().forEach(l ->{
					byte[] b = ByteBuffer.allocate(size)
							.putInt(n.getID())
							.putInt(l.hashCode())
							.putDouble(qm.getSplit(l))
							.array();
					try {
						out.write(b);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
			}
		});
	}

}
