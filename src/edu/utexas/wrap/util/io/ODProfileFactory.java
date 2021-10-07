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
package edu.utexas.wrap.util.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.demand.containers.FixedSizeDemandMap;
import edu.utexas.wrap.demand.containers.FixedSizeODMatrix;
import edu.utexas.wrap.demand.containers.SegmentedODProfile;
import edu.utexas.wrap.marketsegmentation.Purpose;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.TravelSurveyZone;

public class ODProfileFactory {

	public static ODProfile readFromFile(Path path, Mode mode, Map<TimePeriod,Float> vots, Map<Integer, TravelSurveyZone> zones, Purpose parent) throws IOException {
		
		BufferedReader in = Files.newBufferedReader(path);
		final String[] header = in.readLine().split(",");
		
		final IntFunction<TimePeriod> idx = i -> TimePeriod.valueOf(header[i]);
		
		
		Map<TimePeriod,ODMatrix> matrices = IntStream.range(2, header.length)
				.mapToObj(idx)
				.collect(
						Collectors.toMap(
								Function.identity(), 
								tp -> {
									FixedSizeODMatrix<FixedSizeDemandMap> mtx = new FixedSizeODMatrix<FixedSizeDemandMap>(mode, zones.values());
									zones.values().forEach(zone -> mtx.setDemandMap(zone, new FixedSizeDemandMap(zones.values())));
									return mtx;
								}
								)
						);
		
		
		in.lines().forEach(line ->{
			String[] args = line.split(",");
			
			TravelSurveyZone 
			orig = zones.get(Integer.parseInt(args[0])),
			dest = zones.get(Integer.parseInt(args[1]));
			
			for (int i = 2; i < args.length; i++) {
				matrices.get(idx.apply(i)).put(orig, dest, Float.parseFloat(args[i]));
			}
		});
		
		
		
		in.close();
		
		return new SegmentedODProfile(matrices,vots,mode, parent);
	}
}
