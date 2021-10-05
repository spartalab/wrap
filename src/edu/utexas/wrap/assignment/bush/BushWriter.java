/*
    wrap - free and open-source urban transportation modeling software
    Copyright (C) 2017 the wrap project, The University of Texas at Austin

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
	private Path outputPath;

	public BushWriter(Path ioPath) {
		this.outputPath = ioPath;
		
	}
	
	public void consumeStructure(Bush bush, Graph network) throws IOException {
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
		bush.getNodes().parallelStream().filter(n -> n != null).forEach(n ->{
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
