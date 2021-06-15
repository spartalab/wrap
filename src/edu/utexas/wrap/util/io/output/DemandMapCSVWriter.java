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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import edu.utexas.wrap.demand.DemandMap;

public class DemandMapCSVWriter {

	public static void write(String outputDirectory, DemandMap map) {
		Path path = Paths.get(outputDirectory,"demandMap.csv");
		try {
			Files.createDirectories(path.getParent());
			BufferedWriter out = Files.newBufferedWriter(path, 
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);
			out.write("TSZ,Demand\r\n");
			map.getZones().parallelStream().forEach(
					zone -> {
						try {
							out.write(
									zone.toString()
									+","
									+map.get(zone)
									+"\r\n"
									);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
