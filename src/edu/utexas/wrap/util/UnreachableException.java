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
package edu.utexas.wrap.util;

import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.net.Node;

public class UnreachableException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6856927021111691245L;

	public UnreachableException() {
	}
	
	public UnreachableException(Node to, Bush from) {
		this(to.toString()+" unreachable from "+from.root().toString()+", demand="+from.getDemand(to));
	}

	public UnreachableException(String arg0) {
		super(arg0);
	}

	public UnreachableException(Throwable cause) {
		super(cause);
	}

	public UnreachableException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnreachableException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
