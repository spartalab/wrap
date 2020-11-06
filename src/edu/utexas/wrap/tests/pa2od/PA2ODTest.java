package edu.utexas.wrap.tests.pa2od;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.marketsegmentation.DummyPurpose;
import edu.utexas.wrap.net.AreaClass;
import edu.utexas.wrap.net.TravelSurveyZone;

class PA2ODTest {
	Path dir;
	Map<Integer, TravelSurveyZone> zones;

	@BeforeEach
	void setUp() throws Exception {
		dir = Paths.get("E:/data/nctcog/pa2od/validation");
		BufferedReader reader = Files.newBufferedReader(Paths.get("C:\\Users\\wea326\\Workspace\\wrap\\data\\dfw\\zones.csv"));
		reader.readLine();
		AtomicInteger idx = new AtomicInteger(0);

		zones = reader.lines()
				.map(string -> string.split(","))
				.collect(Collectors.toMap(
						args -> Integer.parseInt(args[0]), 
						args -> new TravelSurveyZone(Integer.parseInt(args[0]),idx.getAndIncrement(),AreaClass.values()[Integer.parseInt(args[1])-1])));
	}


	@Test
	void test() throws IOException {
		String fn = "wrkwrk_sr2";
		Path p = dir.resolve(fn+".wrd");
		DummyPurpose purp = new DummyPurpose(p,zones);
		Collection<ODProfile> odps = purp.getODProfiles().collect(Collectors.toSet());
		
		odps.forEach(odp -> {
			try {
				BufferedWriter out = Files.newBufferedWriter(dir.resolve(fn+"_results.csv"));
				zones.values().stream().forEach(orig ->{
					zones.values().stream().forEach(dest->{
						try {
							out.write(""+orig.getID()+","+dest.getID()+","+Stream.of(TimePeriod.values())
							.map(tp -> Float.toString(
									odp.getMatrix(tp).getDemand(orig, dest)))
							.collect(Collectors.joining(","))+"\r\n");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					});
				});
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		});
		
	}

}
