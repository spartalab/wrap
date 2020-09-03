package edu.utexas.wrap.tests.modechoice;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.marketsegmentation.MarketSegment;
import edu.utexas.wrap.modechoice.FixedProportionSplitter;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.util.io.GraphFactory;
import edu.utexas.wrap.util.io.ProductionAttractionFactory;

public class FixedProportionSplitTest {
	public static void main(String[] args) {
		try {
			System.out.println("Reading graph");
			Graph g = GraphFactory.readConicGraph(new File(args[0]), 50000);
			
			System.out.println("Reading matrix");
			AggregatePAMatrix mtx = (AggregatePAMatrix) ProductionAttractionFactory.readMatrix(new File(args[1]), false, g);
			MarketSegment HBWINC1 = new MarketSegment(g, null, null, null, null, null, true, true, (char) 0, (char) 0, false, false, 21.0F/60.0F);
			Map<MarketSegment,Map<Mode,Float>> pct = new HashMap<MarketSegment,Map<Mode,Float>>();
			
			Map<Mode,Float> spct = new HashMap<Mode,Float>();
			spct.put(Mode.SINGLE_OCC, 0.7999F);
			spct.put(Mode.HOV_2_PSGR, 0.0820F);
			spct.put(Mode.HOV_3_PSGR, 0.0639F);
			pct.put(HBWINC1, spct);
			
			System.out.println("Splitting");
			FixedProportionSplitter splitter = new FixedProportionSplitter(pct);
			Set<ModalPAMatrix> results = splitter.split(mtx, HBWINC1);
			System.out.println("Writing");
			results.parallelStream().forEach(m -> {
				try {
					m.toFile(new File(m.getMode().toString()+"-split.csv"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
