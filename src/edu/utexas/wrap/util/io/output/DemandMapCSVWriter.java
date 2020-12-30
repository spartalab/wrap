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
