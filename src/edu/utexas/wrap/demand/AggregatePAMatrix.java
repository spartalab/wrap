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

/**A production-attraction matrix aggregated across all travel modes.
 * This is the direct output of the trip distribution step of the
 * four-step model, before mode choice has been applied. It maps
 * origin-destination zone pairs to the total number of person-trips
 * between them regardless of mode. Trip-interchange mode choice
 * models split this into mode-specific {@link ModalPAMatrix} instances.
 * 
 * @author Will Alexander
 * @see ModalPAMatrix
 * @see edu.utexas.wrap.distribution.TripDistributor
 */
public interface AggregatePAMatrix extends PAMatrix {

}
