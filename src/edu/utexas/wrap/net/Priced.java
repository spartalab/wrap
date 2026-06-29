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
package edu.utexas.wrap.net;

import edu.utexas.wrap.modechoice.Mode;

/**A facility that incurs a generalized price based on the mode of transport
 * and the traveler's value of time. The price combines monetary costs (tolls,
 * fares, operating costs) with the time cost, where the value of time converts
 * travel time into monetary units. This allows route choice algorithms to
 * compare routes on a common generalized-cost basis.
 * 
 * @author Will Alexander
 * @see Link
 * @see edu.utexas.wrap.assignment.Path
 */
public interface Priced {

	public double getPrice(Float vot, Mode c);

}
