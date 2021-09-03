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
package edu.utexas.wrap.util.io;

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
	 * This metord returns a comparator between two zones based on their order value
	 */
	public static class ZoneComparator implements Comparator<TravelSurveyZone> {

		@Override
		public int compare(TravelSurveyZone z1, TravelSurveyZone z2) {
			return (z1.getOrder() - z2.getOrder());
		}
	}


	public static NetworkSkim calculateSkim(Graph network,ToDoubleFunction<Link> costFunction, String id) {
		Collection<TravelSurveyZone> zones = network.getTSZs();
		
		FixedSizeNetworkSkim skim = new FixedSizeNetworkSkim(id,network.numZones());
		
		zones.stream()
		
		.forEach(orig -> {
			FibonacciHeap Q = new FibonacciHeap(zones.size(),1.0f);
			for (Node n : network.getNodes()) {
				if (!n.getID().equals(orig.getID())) {
					Q.add(n, Double.MAX_VALUE);
				}
				else Q.add(n,0.0);
			}

			while (!Q.isEmpty()) {
				FibonacciLeaf u = Q.poll();
				if (u.node.getZone() != null) {
					skim.putCost(orig, u.node.getZone(), (float) u.key);
				}

				for (Link uv : u.node.forwardStar()) {


					FibonacciLeaf v = Q.getLeaf(uv.getHead());
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
