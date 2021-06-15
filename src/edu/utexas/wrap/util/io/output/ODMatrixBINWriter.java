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
	
	public static void write(String outputDirectory, TimePeriod timePeriod, Mode mode, Float vot, ODMatrix matrix) {
		Path path = Paths.get(outputDirectory, 
				timePeriod.toString(), 
				mode.toString(), 
				vot.toString()+".bmtx");
		try{
			Files.createDirectories(path.getParent());
			OutputStream out = Files.newOutputStream(path,
					StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			matrix.getZones().parallelStream().forEach( orig -> {
				matrix.getZones().stream()
//				.filter(dest -> matrix.getDemand(orig,dest) > 0)
				.forEach(dest ->{
					try {
						float demand = matrix.getDemand(orig,dest);
						
						if (demand > 0)	out.write(
								ByteBuffer.allocate(2*Integer.BYTES+Float.BYTES)
								.putInt(orig.getID())
								.putInt(dest.getID())
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
