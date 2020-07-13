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