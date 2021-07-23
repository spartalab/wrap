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
package edu.utexas.wrap.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

/**Implementation of Demographic which maps TSZs to an in-memory Float array
 * 
 * @author William
 *
 */
public class BasicDemographic implements Demographic {
	Map<TravelSurveyZone,Float[]> zoneData;

	/**Read a CSV file containing a table of Demographics
	 * 
	 * A Path to a given CSV demographic table is provided. This file is read
	 * line-by-line, with one line provided for each TSZ. The format of the CSV
	 * should be as follows:
	 * 
	 * ID,Demo[0],Demo[1],...
	 * 
	 * @param demographicFile locates the CSV file to be read
	 * @param zoneIDs a mapping from Integer TSZ ids to the corresponding object
	 * @throws IOException if the file is corrupted, missing, or another IO error occurs
	 */
	public BasicDemographic(Path demographicFile, Map<Integer,TravelSurveyZone> zoneIDs) throws IOException {
		zoneData = new HashMap<TravelSurveyZone,Float[]>();
		BufferedReader reader = Files.newBufferedReader(demographicFile);
		
		String header = reader.readLine();
		int cols = header.split(",").length;
		
		reader.lines().forEach(line ->{
			String[] args = line.split(",");
			TravelSurveyZone zone = zoneIDs.get(Integer.parseInt(args[0]));
			Float[] demos = new Float[cols-1];
			
			IntStream.range(1, cols)
			.forEach(
					col -> demos[col-1] = Float.parseFloat(args[col])
					);
			
			zoneData.put(zone, demos);
		});
		reader.close();
	}
	
	@Override
	public Float[] valueFor(TravelSurveyZone zone) {
		return zoneData.get(zone);
	}

}
