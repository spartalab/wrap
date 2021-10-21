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
package edu.utexas.wrap.assignment;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.net.Link;

public interface StaticAssigner<C extends AssignmentContainer> extends Assigner<C> {

	public TimePeriod getTimePeriod();
	
	public Class<? extends Link> getLinkType();
	
	public Integer maxIterations();
	
	public void setTollingPolicy(ToDoubleFunction<Link> policy);
	
}
