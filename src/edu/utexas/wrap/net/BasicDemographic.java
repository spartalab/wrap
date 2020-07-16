package edu.utexas.wrap.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class BasicDemographic implements Demographic {
	Map<TravelSurveyZone,Float[]> zoneData;

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
	}
	
	@Override
	public Float[] valueFor(TravelSurveyZone zone) {
		return zoneData.get(zone);
	}

}
