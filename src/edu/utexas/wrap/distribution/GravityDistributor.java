package edu.utexas.wrap.distribution;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.containers.AggregatePAHashMatrix;
import edu.utexas.wrap.demand.containers.DemandHashMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;

public class GravityDistributor extends TripDistributor {
	private FrictionFactorMap friction;
	private ForkJoinPool executor = (ForkJoinPool) Executors.newWorkStealingPool();
	private Graph g;

	public GravityDistributor(Graph g, FrictionFactorMap fm) {
		this.g = g;
		friction = fm;
	}
	@Override
	public AggregatePAMatrix distribute(PAMap pa) {
		Map<Node, Double> a = new Object2DoubleOpenHashMap<Node>(g.numZones(),1.0f);
		Map<Node, Double> b = new Object2DoubleOpenHashMap<Node>(g.numZones(),1.0f);
		AggregatePAHashMatrix pam = new AggregatePAHashMatrix(g);
		Boolean converged = false;

		int iterations = 0;
		while (!converged && iterations < 100) {
			converged = true;
			iterations++;
			System.out.println("Iteration "+iterations);
			for (Node i : pa.getProducers()) {
				Double denom = pa.getAttractors().stream().parallel().mapToDouble(x -> b.getOrDefault(x, 1.0)*pa.getAttractions(x)*friction.get(i,x)).sum();
				if (denom == 0.0 || denom.isNaN()) throw new RuntimeException();
				if (!a.containsKey(i) || !converged(a.getOrDefault(i,1.0), 1.0/denom)) {
					converged = false;
					a.put(i, 1.0/denom);
				}
			}

			for (Node j : pa.getAttractors()) {
				Double denom = pa.getProducers().stream().parallel().mapToDouble(x-> a.getOrDefault(x, 1.0)*pa.getProductions(x)*friction.get(x,j)).sum();
				if (denom == 0.0 || denom.isNaN()) throw new RuntimeException();
				if (!b.containsKey(j) || !converged(b.getOrDefault(j,1.0), 1.0/denom)) {
					converged = false;
					
					b.put(j, 1.0/denom);
				}
			}
		}
		
		for (Node i : pa.getProducers()) {
			executor.submit(
					new Thread() {
						public void run() {
							DemandHashMap d = new DemandHashMap(g);


							for (Node j : pa.getAttractors()) {
								d.put(j, (float) (a.get(i)*pa.getProductions(i)*b.get(j)*pa.getAttractions(j)*friction.get(i, j)));
							}

							pam.putDemandMap(i, d);
						}
					}
					);
		}
		executor.shutdown();
		while (!executor.isTerminated()) {
			System.out.print("Remaining: "+(executor.getQueuedSubmissionCount()+executor.getRunningThreadCount())+"\r");
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		};
		return pam;
	}

	private Boolean converged(Double a, Double b) {
		Double margin = 0.001;
		return Math.abs(a-b) < margin;
	}
}
