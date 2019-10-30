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
import edu.utexas.wrap.util.io.FrictionFactorFactory;
import edu.utexas.wrap.util.io.GraphFactory;
import edu.utexas.wrap.util.io.ProductionAttractionFactory;
import edu.utexas.wrap.util.io.SkimFactory;

public class GravityDistributionTest {

	public static void main(String[] args) {

		try {
			System.out.println("Reading graph");
			Graph g = GraphFactory.readEnhancedGraph(new File(args[0]), 50000);
			System.out.println("Reading skim");
			Map<Node,Map<Node, Float>> skim = SkimFactory.readSkimFile(new File(args[1]), false, g);
			System.out.println("Reading friction factors");
			FrictionFactorMap ffm = FrictionFactorFactory.readFactorFile(new File(args[2]), true, skim);
			System.out.println("Reading PA map");
			PAMap pa = ProductionAttractionFactory.readMap(new File(args[3]),true,g);
			
			System.out.println("Running gravity distributor");
			GravityDistributor gd = new GravityDistributor(g,ffm);
			AggregatePAMatrix pam = gd.distribute(pa);
			System.out.println("Writing to file");
			pam.toFile(new File(args[4]));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
