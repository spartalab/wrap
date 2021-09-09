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
package edu.utexas.wrap.util;

import java.util.Collection;
import java.util.stream.Collectors;

import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.containers.FixedMultiplierPassthroughModalPAMatrix;
import edu.utexas.wrap.demand.containers.ODPassthroughMatrix;

public class PassengerVehicleTripConverter {


	public Collection<ODMatrix> convert(Collection<ModalPAMatrix> modalPAMtxs) {
		return modalPAMtxs.stream().map(
				pa -> new ODPassthroughMatrix(
						new FixedMultiplierPassthroughModalPAMatrix(
								1.0/pa.getMode().occupancy(), 
								pa)
						)
				).collect(Collectors.toSet());
	}

}
