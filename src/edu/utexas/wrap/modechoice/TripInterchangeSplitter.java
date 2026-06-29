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

import java.util.Collection;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.ModalPAMatrix;

/**The standard method of performing mode choice in the four-step model.
 * Trip-interchange mode choice operates on the distributed trip matrix,
 * computing mode shares as a function of the travel characteristics
 * (time, cost) for each origin-destination pair. This model assumes
 * travelers can freely choose among all available modes (i.e., no
 * captive riders bound to a particular mode).
 *
 * <p>Implementations split an {@link AggregatePAMatrix} into a collection
 * of mode-specific {@link ModalPAMatrix} instances.
 * 
 * @author Will Alexander
 * @see FixedProportionSplitter
 * @see TripEndSplitter
 */
public interface TripInterchangeSplitter {
	
	public Collection<ModalPAMatrix> split(AggregatePAMatrix aggregate);
	
}
