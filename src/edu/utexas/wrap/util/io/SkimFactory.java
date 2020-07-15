package edu.utexas.wrap.util.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.net.FixedSizeNetworkSkim;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.NetworkSkim;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.net.TravelSurveyZone;
import edu.utexas.wrap.util.FibonacciHeap;
import edu.utexas.wrap.util.FibonacciLeaf;

/**
 * This class provides static methods to read information about Skim rates
 */
public class SkimFactory {
	/**
	 * This method reads a skim file and produces a 2d array mapping between two zones and the skim factor
	 * It expects a csv file with information in the following order:
	 * |origin zone id, destination zone id, ..,..,..,.., skim  factor| //!notice the skim factor is the 6th column!!!
	 * @param file File with skim rates
	 * @param header boolean indicating if the file has a header row
	 * @param graph Graph representation of the network
	 * @return 2d array of floats that can be indexed by the pair of zones storing the skim rates between the zones
	 * @throws IOException
	 */
	public static NetworkSkim readSkimFile(Path file, boolean header, Graph graph) {
		float[][] ret = new float[graph.numZones()][graph.numZones()];
//		Map<TravelSurveyZone,Map<TravelSurveyZone,Float>> ret = new ConcurrentSkipListMap<TravelSurveyZone, Map<TravelSurveyZone,Float>>(new ZoneComparator());
		BufferedReader in = null;
		
		try {
			in = new BufferedReader(Files.newBufferedReader(file));
			if (header) in.readLine();
			in.lines()//.parallel()
			.forEach(line -> processLine(graph,ret,line));
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return new FixedSizeNetworkSkim(ret);
	}

	private static void processLine(Graph graph, float[][] ret, String line) {
		String[] args = line.split(",");
		TravelSurveyZone orig = graph.getNode(Integer.parseInt(args[0])).getZone();
		TravelSurveyZone dest = graph.getNode(Integer.parseInt(args[1])).getZone();
		Float cost = Float.parseFloat(args[2]);
//		ret.putIfAbsent(orig, new ConcurrentSkipListMap<TravelSurveyZone, Float>(new ZoneComparator()));
		ret[orig.getOrder()][dest.getOrder()] = cost;
//		ret.get(orig).put(dest, cost);
	}

	/**
	 * This metord returns a comparator between two zones based on their order value
	 */
	public static class ZoneComparator implements Comparator<TravelSurveyZone> {

		@Override
		public int compare(TravelSurveyZone z1, TravelSurveyZone z2) {
			return (z1.getOrder() - z2.getOrder());
		}
	}


	public static NetworkSkim calculateSkim(Graph network,ToDoubleFunction<Link> costFunction) {
		Collection<TravelSurveyZone> zones = network.getTSZs();
		
		FixedSizeNetworkSkim skim = new FixedSizeNetworkSkim(network.numZones());
		
		zones.stream()
		
		.forEach(orig -> {
			FibonacciHeap<Node> Q = new FibonacciHeap<Node>(zones.size(),1.0f);
			for (Node n : network.getNodes()) {
				if (!n.equals(orig.node())) {
					Q.add(n, Double.MAX_VALUE);
				}
			}
			Q.add(orig.node(), 0.0);

			while (!Q.isEmpty()) {
				FibonacciLeaf<Node> u = Q.poll();
				if (u.node.getZone() != null) {
					skim.putCost(orig, u.node.getZone(), (float) u.key);
				}

				for (Link uv : u.node.forwardStar()) {


					FibonacciLeaf<Node> v = Q.getLeaf(uv.getHead());
					Double alt = costFunction.applyAsDouble(uv)+u.key;
					if (alt<v.key) {
						Q.decreaseKey(v, alt);
					}
				}
			}
		});
		return skim;
	}

	public static void outputCSV(NetworkSkim skim, Path path, Collection<TravelSurveyZone> zones) {
		// TODO Auto-generated method stub
		try {
			BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			
			zones.stream().forEach(orig -> {
				zones.stream().forEach(dest ->{
					try {
						writer.write(orig.getID()+","+dest.getID()+","+skim.getCost(orig, dest)+"\r\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
			});
			
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		throw new RuntimeException("Not yet implemented");
	}
}
