package edu.utexas.wrap.util.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.utexas.wrap.net.NetworkSkim;
import edu.utexas.wrap.net.TravelSurveyZone;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;

public class SkimLoader extends Task<NetworkSkim> {
	
	private NetworkSkim skim;
	private Path skimCSV;
	private Map<Integer,TravelSurveyZone> zones;
	private SimpleIntegerProperty completedLines;
	private Logger logger = Logger.getLogger("wrap.runner.skimLoader");
	
	public SkimLoader(NetworkSkim skim, Path csv, Map<Integer, TravelSurveyZone> zones) {
		this.skim = skim;
		this.skimCSV = csv;
		this.zones = zones;
		int size = zones.size() * zones.size();
		logger.info("Initializing SkimLoader for "+skim+". Size = "+zones.size()+"x"+zones.size());
		completedLines = new SimpleIntegerProperty(0);
		completedLines.addListener((obs,oldVal,newVal)->{
			updateProgress((int) newVal,size);
		});
	}

	@Override
	protected NetworkSkim call() throws Exception {
		logger.info("Reading "+skim+" from file "+skimCSV);
		try (BufferedReader reader = Files.newBufferedReader(skimCSV)) {
			reader.lines().forEach(line ->{
				if (isCancelled()) return;
				String[] args = line.split(",");
				skim.putCost(
						zones.get(Integer.parseInt(args[0])), 
						zones.get(Integer.parseInt(args[1])),
						Float.parseFloat(args[2]));
				completedLines.set(completedLines.get()+1);
				
			});
		} catch (IOException e) {
			logger.log(Level.SEVERE,"An error was encountered while reading "+skim+".",e);
		}
		return skim;
	}

	public String toString() {
		return skim.toString();
	}
}
