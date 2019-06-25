package edu.utexas.wrap.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

public class SkimFactory {
	public static Map<Node, Map<Node, Float>> readSkimFile(File file, boolean header, Graph graph) throws IOException {
		Map<Node,Map<Node,Float>> ret = new HashMap<Node, Map<Node,Float>>();
		BufferedReader in = null;

		try {
			in = new BufferedReader(new FileReader(file));
			if (header) in.readLine();
			String line = in.readLine();

			while (line != null) {
				processLine(graph, ret, line);

				line = in.readLine();
			}
		}
		finally {
			if (in != null) in.close();
		}
		return ret;
	}

	static void processLine(Graph graph, Map<Node, Map<Node, Float>> ret, String line) {
		String[] args = line.split(",");
		Node orig = graph.getNode(Integer.parseInt(args[0]));
		Node dest = graph.getNode(Integer.parseInt(args[1]));
		Float cost = Float.parseFloat(args[2]);
		ret.putIfAbsent(orig, new HashMap<Node,Float>());
		ret.get(orig).put(dest, cost);
	}
}
