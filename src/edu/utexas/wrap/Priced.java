package edu.utexas.wrap;

import java.math.BigDecimal;

public interface Priced {

	public BigDecimal getPrice(Float vot, VehicleClass c);

}
