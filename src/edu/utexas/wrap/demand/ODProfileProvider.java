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

import java.util.Collection;

/**Provides {@link ODProfile} instances from a collection of daily
 * {@link ODMatrix} inputs. Implementations allocate daily vehicle-trips
 * across {@link edu.utexas.wrap.TimePeriod}s using time-of-day factors,
 * producing the final demand representation consumed by assigners.
 * 
 * @author Will Alexander
 * @see edu.utexas.wrap.util.TimeOfDaySplitter
 */
public interface ODProfileProvider {

	public Collection<ODProfile> getODProfiles(Collection<ODMatrix> matrices);
}
