package edu.utexas.wrap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import edu.utexas.wrap.assignment.Assigner;
import edu.utexas.wrap.assignment.AssignmentBuilder;
import edu.utexas.wrap.assignment.AssignmentInitializer;
import edu.utexas.wrap.assignment.AssignmentProvider;
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

public class wrapTNTP {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		Graph g = GraphFactory.readTNTPGraph(new File(args[0]));
		
		if (!Files.exists(Paths.get(g.toString()))) 
			Files.createDirectories(Paths.get(g.toString()));
		
		
		ODMatrix od = ODMatrixFactory.readTNTPMatrix(new File(args[1]), g);
		
		AssignmentProvider<Bush> reader = new BushReader(g);
		AssignmentConsumer<Bush> writer = new BushWriter(g),
				forgetter = new BushForgetter();
		
		AssignmentBuilder<Bush> builder = new BushBuilder(g);
		
		AssignmentInitializer<Bush> initializer = new BushInitializer(reader, writer, builder,g);
		
		initializer.add(od);
		
		Assigner<Bush> assigner = new Assigner<Bush>(
				initializer, 
				new GapEvaluator<Bush>(g, reader, forgetter), 
				new AlgorithmBOptimizer(
						reader, 
						writer, 
						new BushGapEvaluator(g), 
						10E-3),
				10E-6
				);
		
		Thread assignmentThread = new Thread(assigner);
		assignmentThread.start();
		
		try {
			assignmentThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
