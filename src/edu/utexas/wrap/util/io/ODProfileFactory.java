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
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.TravelSurveyZone;

public class ODProfileFactory {

	public static ODProfile readFromFile(Path path, Mode mode, Map<TimePeriod,Float> vots, Map<Integer, TravelSurveyZone> zones) throws IOException {
		
		BufferedReader in = Files.newBufferedReader(path);
		final String[] header = in.readLine().split(",");
		
		final IntFunction<TimePeriod> idx = i -> TimePeriod.valueOf(header[i]);
		
		
		Map<TimePeriod,ODMatrix> matrices = IntStream.range(2, header.length)
				.mapToObj(idx)
				.collect(
						Collectors.toMap(
								Function.identity(), 
								tp -> {
									FixedSizeODMatrix<FixedSizeDemandMap> mtx = new FixedSizeODMatrix<FixedSizeDemandMap>(vots.get(tp), mode, zones.values());
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
		
		return new SegmentedODProfile(matrices,mode);
	}
}
