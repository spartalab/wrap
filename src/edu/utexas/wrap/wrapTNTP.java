package edu.utexas.wrap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;

import edu.utexas.wrap.assignment.Assigner;
import edu.utexas.wrap.assignment.AssignmentBuilder;
import edu.utexas.wrap.assignment.AssignmentInitializer;
import edu.utexas.wrap.assignment.AssignmentProvider;
import edu.utexas.wrap.assignment.BasicStaticAssigner;
import edu.utexas.wrap.assignment.AssignmentConsumer;
import edu.utexas.wrap.assignment.GapEvaluator;
import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.assignment.bush.BushBuilder;
import edu.utexas.wrap.assignment.bush.BushForgetter;
import edu.utexas.wrap.assignment.bush.BushGapEvaluator;
import edu.utexas.wrap.assignment.bush.BushInitializer;
import edu.utexas.wrap.assignment.bush.BushReader;
import edu.utexas.wrap.assignment.bush.BushWriter;
import edu.utexas.wrap.assignment.bush.algoB.AlgorithmBOptimizer;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.ODMatrixFactory;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.util.io.GraphFactory;

@Deprecated
public class wrapTNTP {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		boolean isConic = args[0].equals("-e");
		Graph g = null;
		System.out.println("Reading graph");
		if (isConic) {
			g = GraphFactory.readConicGraph(new File(args[1]), Integer.parseInt(args[2]));
		} 
		else g = GraphFactory.readTNTPGraph(new File(args[0]));
		
		
		
		if (!Files.exists(Paths.get(g.toString()))) 
			Files.createDirectories(Paths.get(g.toString()));
		
		
		
		AssignmentProvider<Bush> reader = new BushReader(g);
		AssignmentConsumer<Bush> writer = new BushWriter(g),
				forgetter = new BushForgetter();
		
		AssignmentBuilder<Bush> builder = new BushBuilder(g);
		
		AssignmentInitializer<Bush> initializer = new BushInitializer(reader, writer, builder,g);

		
		System.out.println("Reading OD Matrices");
		if (isConic) {
			Collection<ODMatrix> mtxs = ODMatrixFactory.readConicTrips(new File(args[3]), g);
			mtxs.forEach(mtx -> initializer.add(mtx));
		}
		else { 
			initializer.add(ODMatrixFactory.readTNTPMatrix(new File(args[1]), g));
		}
		
		System.out.println("Initializing solution");
		BasicStaticAssigner<Bush> assigner = new BasicStaticAssigner<Bush>(
				initializer, 
				new GapEvaluator<Bush>(g, reader, forgetter), 
				new AlgorithmBOptimizer(
						reader, 
						writer, 
						new BushGapEvaluator(g), 
						10E-3),
				10E-4
				);
		
		System.out.println("Writing link flows");
		printFlows(g);
		
		System.out.println("Beginning assignment");
		Thread assignmentThread = new Thread(assigner);
		assignmentThread.start();
		
		try {
			assignmentThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void printFlows(Graph g) {
		try {
			BufferedWriter writer = Files.newBufferedWriter(Paths.get("flows.csv"),StandardOpenOption.CREATE);
			g.getLinks().parallelStream()
			.map(link -> link.getFlow()+","+link.getTravelTime()+"\r\n")
			.forEach(line -> {
				try {
					writer.write(line);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
