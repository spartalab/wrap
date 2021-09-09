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
package edu.utexas.wrap.modechoice;

import java.util.stream.Collectors;
import java.util.Collection;
import java.util.Map;
import edu.utexas.wrap.demand.containers.FixedMultiplierPassthroughModalPAMatrix;
import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.ModalPAMatrix;

/**The purpose of this class is to return
 * a set of modal PA matrices that contain
 * the proportion of trips that are taken for
 * a particular mode.
 *
 * @author Karthik & Rishabh
 *
 */
public class FixedProportionSplitter implements TripInterchangeSplitter {

	private final Map<Mode, Float> map;

	public FixedProportionSplitter(Map<Mode, Float> map) {
		this.map = map;
	}

	/**
	 *	This method returns a set of Modal PA matrices
	 *
	 *  This method	essentially iterates through all
	 *  (i,j) pairs for each mode type and then adds the
	 *  respective proportional value to the a new matrix
	 *  which is added to a set.
	 */
	@Override
	public Collection<ModalPAMatrix> split(AggregatePAMatrix aggregate) {
		return map.entrySet().stream()
				.filter(entry -> entry.getValue() > 0)
				.map(entry -> new FixedMultiplierPassthroughModalPAMatrix(entry.getKey(),entry.getValue(),aggregate)
		).collect(Collectors.toSet());
	}
}
