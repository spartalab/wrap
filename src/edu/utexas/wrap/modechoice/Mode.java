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
package edu.utexas.wrap.modechoice;

public enum Mode {
	MED_TRUCK {
		@Override
		public double occupancy() {
			return 1.19;
		}
	},	
	
	HVY_TRUCK {
		@Override
		public double occupancy() {
			return 1.19;
		}
	},
	
	SINGLE_OCC {

		@Override
		public double occupancy() {
			return 1.0;
		}
		
	}, 
	
	HOV {

		@Override
		public double occupancy() {
			return 2.57;
		}
		
	},
	
	HOV_2_PSGR {

		@Override
		public double occupancy() {
			return 2.0;
		}
		
	}, 
	
	HOV_3_PSGR {

		@Override
		public double occupancy() {
			return 3.53;
		}
		
	}, 
	
	WALK_TRANSIT {

		@Override
		public double occupancy() {
			// TODO Auto-generated method stub
			return 20.29;
		}
		
	}, 
	
	DRIVE_TRANSIT {

		@Override
		public double occupancy() {
			// TODO Auto-generated method stub
			return 20.29;
		}
		
	};
	
	public abstract double occupancy();
}