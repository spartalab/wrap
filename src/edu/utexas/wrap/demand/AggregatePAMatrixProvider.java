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

/**This interface defines the origin point for AggregatePAMatrices;
 * that is, any class implementing this interface must provide a
 * means to generate an AggregatePAMatrix. The person-trips from
 * this matrix will not yet have been allocated to a particular Mode
 * 
 * @author William
 *
 */
public interface AggregatePAMatrixProvider {

	AggregatePAMatrix getAggregatePAMatrix(PAMap map);
}
