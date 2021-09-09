package edu.utexas.wrap.util.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import edu.utexas.wrap.net.NetworkSkim;
import edu.utexas.wrap.net.TravelSurveyZone;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;

public class SkimLoader extends Task<Void> {
	
	private NetworkSkim skim;
	private Path skimCSV;
	private Map<Integer,TravelSurveyZone> zones;
	private SimpleIntegerProperty completedLines;
	
	public SkimLoader(NetworkSkim skim, Path csv, Map<Integer, TravelSurveyZone> zones) {
		// TODO Auto-generated constructor stub
		this.skim = skim;
		this.skimCSV = csv;
		this.zones = zones;
		int size = zones.size() * zones.size();

		completedLines = new SimpleIntegerProperty(0);
		completedLines.addListener((obs,oldVal,newVal)->{
			updateProgress((int) newVal,size);
		});
	}

	@Override
	protected Void call() throws Exception {
		// TODO Auto-generated method stub
		try (BufferedReader reader = Files.newBufferedReader(skimCSV)) {
			reader.lines().forEach(line ->{
				String[] args = line.split(",");
				skim.putCost(
						zones.get(Integer.parseInt(args[0])), 
						zones.get(Integer.parseInt(args[1])),
						Float.parseFloat(args[2]));
				completedLines.set(completedLines.get()+1);
				
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public String toString() {
		return skim.toString();
	}
}
