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

import java.nio.file.Path;
import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.NetworkSkim;

public interface Assigner {
	
	public void process(ODProfile profile);
	
	public NetworkSkim getSkim(String id, ToDoubleFunction<Link> function);

	public void outputFlows(Path outputFile);
	
	public void initialize();
	
	public boolean isConverged();
	
	public void iterate();
	
	public double getProgress();
	
}
