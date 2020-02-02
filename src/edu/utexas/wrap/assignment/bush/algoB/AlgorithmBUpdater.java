package edu.utexas.wrap.assignment.bush.algoB;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.assignment.bush.PathCostCalculator;
import edu.utexas.wrap.net.Link;

public class AlgorithmBUpdater {

	public boolean update(Bush bush) {
		bush.prune();	//Remove unused links

		AtomicBoolean modified = new AtomicBoolean(false);
		Collection<Link> unusedLinks = bush.getUnusedLinks();

		//Calculate the longest path costs
		PathCostCalculator pcc = bush.getPathCostCalculator();

		unusedLinks.parallelStream()	//For each unused link
		.filter(l -> l.allowsClass(bush.vehicleClass()))	//If the link allows this bush's vehicles
		.filter(l -> bush.isValidLink(l))	//and is a link that can legally be used
		.filter(l -> pcc.checkForShortcut(l))	//see if it provides a shortcut
		.collect(Collectors.toSet()) //ensure we've checked all of them (this prevents concurrent modification)
		//Add each of these to the bush
		.parallelStream()
		.filter(l -> bush.add(l))
		.forEach(y -> {
			modified.set(true);
		});

		return modified.get();

	}
}
