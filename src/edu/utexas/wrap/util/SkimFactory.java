package edu.utexas.wrap.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.Map;

import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;
import java.util.concurrent.ConcurrentSkipListMap;
public class SkimFactory {
	public static Map<TravelSurveyZone, Map<TravelSurveyZone, Float>> readSkimFile(File file, boolean header, Graph graph) throws IOException {
		Map<TravelSurveyZone,Map<TravelSurveyZone,Float>> ret = new ConcurrentSkipListMap<TravelSurveyZone, Map<TravelSurveyZone,Float>>(new ZoneComparator());
		BufferedReader in = null;

		try {
			in = new BufferedReader(new FileReader(file));
			if (header) in.readLine();
			in.lines().parallel().forEach(line -> processLine(graph,ret,line));
		}
		finally {
			if (in != null) in.close();
		}
		return ret;
	}

	static void processLine(Graph graph, Map<TravelSurveyZone, Map<TravelSurveyZone, Float>> ret, String line) {
		String[] args = line.split(",");
		TravelSurveyZone orig = graph.getNode(Integer.parseInt(args[0])).getZone();
		TravelSurveyZone dest = graph.getNode(Integer.parseInt(args[1])).getZone();
		Float cost = Float.parseFloat(args[5]);
		ret.putIfAbsent(orig, new ConcurrentSkipListMap<TravelSurveyZone, Float>(new ZoneComparator()));
		ret.get(orig).put(dest, cost);
	}

	public static class ZoneComparator implements Comparator<TravelSurveyZone> {

		@Override
		public int compare(TravelSurveyZone z1, TravelSurveyZone z2) {
			return (z1.getOrder() - z2.getOrder());
		}
	}


}
