package edu.utexas.wrap.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.distribution.FrictionFactorMap;
import edu.utexas.wrap.distribution.GravityDistributor;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.util.FrictionFactorFactory;
import edu.utexas.wrap.util.GraphFactory;
import edu.utexas.wrap.util.ProductionAttractionFactory;
import edu.utexas.wrap.util.SkimFactory;

public class frictionRun {

	public static void main(String[] args) {

		try {
			Graph g = GraphFactory.readEnhancedGraph(new File(args[0]), 50000);
			Map<Node,Map<Node, Float>> skim = SkimFactory.readSkimFile(new File(args[1]), false, g);
			FrictionFactorMap ffm = FrictionFactorFactory.readFactorFile(new File(args[2]), true, skim);
			PAMap pa = ProductionAttractionFactory.readMap(new File(args[3]),true,g);

			GravityDistributor gd = new GravityDistributor(g,ffm);

			AggregatePAMatrix pam = gd.distribute(pa);
			pam.toFile(new File(args[3]));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
