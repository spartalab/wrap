package edu.utexas.wrap.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.net.TravelSurveyZone;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class SkimFactory {
	public static Map<TravelSurveyZone, Map<TravelSurveyZone, Float>> readSkimFile(File file, boolean header, Graph graph) throws IOException {
		Map<TravelSurveyZone,Map<TravelSurveyZone,Float>> ret = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<TravelSurveyZone, Map<TravelSurveyZone,Float>>());
		BufferedReader in = null;

		try {
			in = new BufferedReader(new FileReader(file));
			if (header) in.readLine();
			in.lines().forEach(line -> processLine(graph,ret,line));
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
		ret.putIfAbsent(orig, Object2FloatMaps.synchronize(new Object2FloatOpenHashMap<TravelSurveyZone>()));
		ret.get(orig).put(dest, cost);
	}
}
