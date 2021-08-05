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
package edu.utexas.wrap.marketsegmentation;

import edu.utexas.wrap.demand.AggregatePAMatrixProvider;
import edu.utexas.wrap.demand.DailyODMatrixProvider;
import edu.utexas.wrap.demand.ModalPAMatrixProvider;
import edu.utexas.wrap.demand.ODProfileProvider;
import edu.utexas.wrap.demand.PAMapProvider;

/**An interface for providing a full implementation of the UTMS
 * 
 * A class implementing this interface should be able to provide
 * data for any step in the four-step Urban Transportation Modeling
 * System; that is, it should be able to provide a PAMap, an
 * AggregatePAMatrix, a collection of ModalPAMatrices, a collection
 * of daily ODMatrices, and a collection of ODProfiles, each developed
 * according to their respective model.
 * 
 * @author William
 *
 */
public interface Purpose extends 
							ODProfileProvider, 
							DailyODMatrixProvider, 
							ModalPAMatrixProvider, 
							AggregatePAMatrixProvider, 
							PAMapProvider {
	
	public double personTrips();
};

