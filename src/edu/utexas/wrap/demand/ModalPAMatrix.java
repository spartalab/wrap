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
package edu.utexas.wrap.demand;

import edu.utexas.wrap.modechoice.Mode;

/**A production-attraction matrix for a specific travel {@link Mode}.
 * This is produced after both trip distribution and mode choice have
 * been applied, representing the number of trips between zone pairs
 * for a particular mode. The ModalPAMatrix carries metadata (such as
 * value of time) needed for subsequent conversion to OD matrices and
 * route choice.
 * 
 * @author Will Alexander
 * @see AggregatePAMatrix
 * @see edu.utexas.wrap.modechoice.TripInterchangeSplitter
 */
public interface ModalPAMatrix extends AggregatePAMatrix {

	/**
	 * @return the Mode associated with this matrix
	 */
	public Mode getMode();
}
