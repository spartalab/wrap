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
package edu.utexas.wrap.distribution;

/**Maps a travel cost (impedance) value to a friction factor used in
 * gravity-model trip distribution. Higher friction factors indicate
 * lower resistance to travel, resulting in more trips between zone
 * pairs with that cost level. The mapping is typically derived from
 * observed trip length frequency distributions.
 * 
 * @author Will Alexander
 * @see CostBasedFrictionFactorMap
 * @see ImpedanceMatrix
 */
public interface FrictionFactorMap {

	public Float get(float skimCost);

}
