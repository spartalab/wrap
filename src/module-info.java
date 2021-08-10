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
module wrap {
	requires java.base;
	requires javafx.fxml;
	requires javafx.controls;
	requires transitive javafx.graphics;
	requires transitive javafx.base;
	exports edu.utexas.wrap;
	exports edu.utexas.wrap.gui;
	exports edu.utexas.wrap.marketsegmentation;
	exports edu.utexas.wrap.assignment;
	exports edu.utexas.wrap.modechoice;
	exports edu.utexas.wrap.net;
	exports edu.utexas.wrap.demand;
	exports edu.utexas.wrap.distribution;
	opens edu.utexas.wrap.gui;
	
	
}