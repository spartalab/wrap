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
package edu.utexas.wrap.generation;

import edu.utexas.wrap.net.AreaClass;
import edu.utexas.wrap.net.TravelSurveyZone;

/**A GenerationRate implementation in which every TravelSurveyZone
 * has the same generation rate, regardless of any other factor
 * 
 * @author William
 *
 */
public class GeneralGenerationRate implements GenerationRate {

	private double rate;
	
	public GeneralGenerationRate(double rate) {
		this.rate = rate;
	}

	@Override
	public double getRate(TravelSurveyZone segment) {
		return rate;
	}
	
	@Override
	public double getRate(AreaClass klass) {
		return rate;
	}

	public String toString() {
		return Double.toString(rate);
	}

	@Override
	public int getDimension() {
		return 1;
	}
}
