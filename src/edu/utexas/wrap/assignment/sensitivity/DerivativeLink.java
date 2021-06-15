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
package edu.utexas.wrap.assignment.sensitivity;

import java.util.Map;

import edu.utexas.wrap.assignment.AssignmentContainer;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;

public class DerivativeLink extends Link {
	protected final Link parent;
	Double deriv;

	public DerivativeLink(Node tail, Node head, Float capacity, Float length, Float fftime, Link oldLink,
			Map<Link, Double> derivs) {
		super(tail, head, capacity, length, fftime);
		parent = oldLink;
		deriv = derivs.getOrDefault(oldLink, 0.0);
	}

	@Override
	public Boolean allowsClass(Mode c) {
		return parent.allowsClass(c);
	}

	@Override
	public double getPrice(Float vot, Mode c) {
		return getTravelTime();
	}

	@Override
	public double getTravelTime() {
		return deriv*flo;
	}

	@Override
	public double pricePrime(Float vot) {
		return tPrime();
	}

	@Override
	public double tIntegral() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double tPrime() {
		return deriv;
	}

	@Override
	public Boolean changeFlow(Double delta) {
		flo += delta;
		if (flo.isNaN()) {
			throw new RuntimeException();
		}
		if (delta != 0.0) {
			cachedTT = null;
			cachedTP = null;
		}
		return flo > 0.0;
	}
	
	@Override
	public Double getFlow() {
		return flo;
	}

	@Override
	public double getPrice(AssignmentContainer container) {
		// TODO Auto-generated method stub
		return getTravelTime();
	}
}