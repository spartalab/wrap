/*
    wrap - free and open-source urban transportation modeling software
    Copyright (C) 2017 the wrap project, The University of Texas at Austin

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.utexas.wrap.assignment.sensitivity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import edu.utexas.wrap.assignment.bush.AlternateSegmentPair;
import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.assignment.bush.OldBushOrigin;
import edu.utexas.wrap.assignment.bush.algoB.OldAlgoBOptimizer;
import edu.utexas.wrap.demand.AutoDemandMap;
import edu.utexas.wrap.demand.containers.EmptyDemandMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;

public class SensitivityAnalyzer extends OldAlgoBOptimizer {
	
	public SensitivityAnalyzer(Graph g, Set<OldBushOrigin> o) {
		super(g, o);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void optimize() {
		super.optimize();
		Bush.flowCachingEnabled = true;
		if (true) return;
		Map<Link,Double> dtdX = new HashMap<Link,Double>(graph.numLinks(),1.0f);
		for (Link l : graph.getLinks()) {
			dtdX.put(l, l.tPrime());
		}
		
		//duplicate network with new link performance functions and zero flow
		for (Link l : graph.getLinks()) {
			Map<Node,Node> nodeMap = graph.duplicateNodes();

		
			Link focus = new CapacityDerivativeLink(nodeMap.get(l.getTail()), nodeMap.get(l.getHead()), l.getCapacity(), l.getLength(), l.freeFlowTime(), l, dtdX);
			Map<Link,Link> linkMap = graph.getDerivativeLinks(dtdX, l, focus, nodeMap);
			Graph gPrime = graph.getDerivativeGraph(linkMap,nodeMap);
			AutoDemandMap demand = new EmptyDemandMap(gPrime);

			Set<OldBushOrigin> newOrigins = new HashSet<OldBushOrigin>(origins.size(),1.0f);
			for (OldBushOrigin oldOrigin : origins) {
				OldBushOrigin newOrigin = new OldBushOrigin(nodeMap.get(oldOrigin.getNode()));
				for (Bush oldBush : oldOrigin.getContainers()) {
					Bush newBush = new Bush(oldBush,gPrime,demand,linkMap,nodeMap,newOrigin);
					newOrigin.add(newBush);
				}
				newOrigins.add(newOrigin);
			}
			do {sensitivityIterate(gPrime,newOrigins);} while (!sensitivityConverged());
			
			//TODO output link flows
		}
	}

	private boolean sensitivityConverged() {
		// TODO Auto-generated method stub
		return true;
	}

	private void sensitivityIterate(Graph gPrime, Set<OldBushOrigin> newOrigins) {
		for (OldBushOrigin origin : newOrigins) {
			for (Bush b : origin.getContainers()) try {
				b.clearLabels();
				Node[] to = b.getTopologicalOrder(true);
				Node cur;
				b.shortTopoSearch();
				b.longTopoSearch(false);
				Map<Link,Double> bushFlows = b.flows(); 
				for (int i = to.length-1;i >= 0; i--) {
					cur = to[i];
					if (cur == null || cur.equals(b.root().node())) continue;
					AlternateSegmentPair asp = b.getShortLongASP(cur,bushFlows);
					if (asp == null) continue;
					Double deltaH = getNewDeltaH(asp);
					updateNewDeltaX(asp,bushFlows,deltaH);
				}
				b.updateSplits(bushFlows); //FIXME doesn't update splits correctly
			} catch (Exception e) {
				e.printStackTrace();
				
			}
		}
	}
	
	private void updateNewDeltaX(AlternateSegmentPair asp, Map<Link,Double> flows, Double deltaH) {
		StreamSupport.stream(asp.shortPath().spliterator(),true).unordered().forEach(l ->{
			flows.put(l, flows.getOrDefault(lc, 0.0)+deltaH);
			l.changeFlow(deltaH);
		});
		
		StreamSupport.stream(asp.longPath().spliterator(), true).unordered().forEach(l->{
			flows.put(l, flows.getOrDefault(lc, 0.0)-deltaH);
			l.changeFlow(-deltaH);
		});
	}

	private Double getNewDeltaH(AlternateSegmentPair asp) {
		Float vot = asp.getBush().valueOfTime();
		Double denominator = Stream.concat(
				StreamSupport.stream(asp.longPath().spliterator(), true),
				StreamSupport.stream(asp.shortPath().spliterator(), true)
				).unordered().mapToDouble(x -> x.pricePrime(vot)).sum();
		return asp.priceDiff()/denominator;
	}
	
//	private Map<Link, Double> getDerivatives(Graph gprime, BushOrigin origin, Bush bush, Node destination) {
//		//TODO generate pseudo-OD with one destination's demand being 1 and all others 0
//		
//		//TODO solve for equilibrium
//		
//		//TODO record derivatives
//		
//		//TODO reset graph	
//	}

}
