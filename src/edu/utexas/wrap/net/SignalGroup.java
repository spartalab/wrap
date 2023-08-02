package edu.utexas.wrap.net;

import java.util.Collection;
import java.util.Map;

public class SignalGroup {
	
	private final Map<Link,Collection<TurningMovement>> movements;
	private final Map<TurningMovement,Ring> rings;
	private final Integer id;
	
	public SignalGroup(Integer id,
			Map<Link,Collection<TurningMovement>> linkMovements,
			Map<TurningMovement,Ring> rings
			) {
		this.id = id;
		this.movements = linkMovements;
		this.rings = rings;
	}

	public Integer getID() {
		// TODO Auto-generated method stub
		return id;
	}

	public Collection<TurningMovement> getMovements(Link inLink) {
		// TODO Auto-generated method stub
		return movements.get(inLink);
	}

	public Double getGreenShare(TurningMovement tm) {
		// TODO Auto-generated method stub
		return rings.get(tm).getGreenShare(tm);
	}

}
