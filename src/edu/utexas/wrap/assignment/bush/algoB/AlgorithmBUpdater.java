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
package edu.utexas.wrap.assignment.bush.algoB;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.assignment.bush.PathCostCalculator;
import edu.utexas.wrap.net.Link;

public class AlgorithmBUpdater {

	public boolean update(Bush bush, PathCostCalculator pcc) {
		bush.prune();	//Remove unused links

		AtomicBoolean modified = new AtomicBoolean(false);
		Stream<Link> unusedLinks = bush.getUnusedLinks();

		//Calculate the longest path costs
//		PathCostCalculator pcc = new PathCostCalculator(bush);

		unusedLinks	//For each unused link
		.filter(l -> l.allowsClass(bush.vehicleClass()))	//If the link allows this bush's vehicles
		.filter(l -> bush.canUseLink(l))	//and is a link that can legally be used
		.filter(l -> pcc.checkForShortcut(l))	//see if it provides a shortcut
		.collect(Collectors.toSet()) //ensure we've checked all of them (this prevents concurrent modification)
		
		//Add each of these to the bush
		.parallelStream()
		.filter(l -> bush.add(l))
		.forEach(y -> modified.set(true));

		return modified.get();

	}
}
