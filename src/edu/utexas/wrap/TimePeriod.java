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
package edu.utexas.wrap;

/**A set of time-frames among which trips will be dispersed and assigned.
 * Each time period represents a portion of the day with relatively
 * homogeneous travel demand characteristics. These periods partition
 * a typical weekday into five segments:
 * <ul>
 *   <li>{@link #EARLY_OP} - Early off-peak (e.g., midnight to 6 AM)</li>
 *   <li>{@link #AM_PK} - Morning peak (e.g., 6 AM to 9 AM)</li>
 *   <li>{@link #MID_OP} - Midday off-peak (e.g., 9 AM to 3 PM)</li>
 *   <li>{@link #PM_PK} - Afternoon/evening peak (e.g., 3 PM to 7 PM)</li>
 *   <li>{@link #LATE_OP} - Late off-peak (e.g., 7 PM to midnight)</li>
 * </ul>
 * 
 * @author Will Alexander
 */
public enum TimePeriod {
	EARLY_OP,
	AM_PK,
	MID_OP,
	PM_PK,
	LATE_OP
}
