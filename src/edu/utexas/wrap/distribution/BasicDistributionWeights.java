package edu.utexas.wrap.distribution;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import edu.utexas.wrap.net.TravelSurveyZone;

public class BasicDistributionWeights implements DistributionWeights {
	
	private final Path weightFile;
	private final Map<Integer, TravelSurveyZone> zones;
	private Double[] productionWeights, attractionWeights;

	public BasicDistributionWeights(Map<Integer,TravelSurveyZone> zones,Path weightFile) {

		this.weightFile = weightFile;
		this.zones = zones;
		
		productionWeights = new Double[zones.size()];
		attractionWeights = new Double[zones.size()];
		
		if (weightFile != null) try {
			readWeightFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void readWeightFile() throws IOException {
		
		BufferedReader reader = Files.newBufferedReader(weightFile);
		
		reader.lines().forEach(line ->{
			String[] args = line.split(",");
			TravelSurveyZone zone = zones.get(Integer.parseInt(args[0]));
			
			Double productionWeight = Double.parseDouble(args[1]);
			Double attractionWeight = Double.parseDouble(args[2]);
			
			productionWeights[zone.getOrder()] = productionWeight;
			attractionWeights[zone.getOrder()] = attractionWeight;
		});
		reader.close();

	}

	@Override
	public Double[] getProductionWeights() {
		return productionWeights;
	}

	@Override
	public Double[] getAttractionWeights() {
		return attractionWeights;
	}

	@Override
	public void updateWeights(Double[] producerWeights, Double[] attractorWeights) {
		this.productionWeights = producerWeights;
		this.attractionWeights = attractorWeights;
		
		if (weightFile != null) try {
			writeWeightFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeWeightFile() throws IOException {
		BufferedWriter writer = Files.newBufferedWriter(weightFile, StandardOpenOption.CREATE);
		
		for (TravelSurveyZone zone : zones.values()) {
			writer.write(zone.getID()+","+productionWeights[zone.getOrder()]+","+attractionWeights[zone.getOrder()]+"\n");
		}
		writer.close();


	}

}
