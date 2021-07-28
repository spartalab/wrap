///*
//    wrap - free and open-source urban transportation modeling software
//    Copyright (C) 2017 the wrap project, The University of Texas at Austin
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//package edu.utexas.wrap.assignment.sensitivity;
//
//import java.util.Map;
//
//import edu.utexas.wrap.net.Link;
//import edu.utexas.wrap.net.Node;
//import edu.utexas.wrap.net.TolledBPRLink;
//
//class CapacityDerivativeLink extends DerivativeLink {
//	
//	public CapacityDerivativeLink(Node tail, Node head, Float capacity, Float length, Float fftime, Link oldLink,
//			Map<Link, Double> derivs) {
//		super(tail, head, capacity, length, fftime, oldLink, derivs);
//	}
//
//	@Override
//	public double getTravelTime() {
//		Double dtdc = null;
//		if (parent instanceof TolledBPRLink) {
//			TolledBPRLink ll = (TolledBPRLink) parent;
//			dtdc = -ll.getPower()*ll.getBValue()*ll.freeFlowTime()*Math.pow(ll.getFlow()/ll.getCapacity(), ll.getPower())/ll.getCapacity();
//		}
//		return deriv*flo + dtdc;
//	}
//
//}