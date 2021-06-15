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

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.modechoice.Mode;

/**An interface which contains travel demand over multiple
 * TimePeriods. The TimePeriods for which demand exists will
 * be associated with an ODMatrix of trips made during the
 * TimePeriod. The ODProfile also defines the mode used for
 * all trips, as well as a value of time for each TimePeriod.
 * Note that while the Mode is constant for a given ODProfile,
 * the value of time may (and most likely should) be dependent
 * on the TimePeriod
 * 
 * @author William
 *
 */
public interface ODProfile {

	/**
	 * @param period the TimePeriod whose ODMatrix should be returned
	 * @return an ODMatrix defining trips made during the given TimePeriod
	 */
	public ODMatrix getMatrix(TimePeriod period);
	
	/**
	 * @return the Mode used for trips in this ODProfile
	 */
	public Mode getMode();

	/**
	 * @param timePeriod the TimePeriod whose value of time should be returned
	 * @return a Float defining how much a unit of travel time is valued by
	 * those making trips in this ODProfile during the given TimePeriod
	 */
	public Float getVOT(TimePeriod timePeriod);
	
}