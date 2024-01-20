package edu.utexas.wrap.net;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

public class SignalGroup {
	
	private final Map<Link,Collection<TurningMovement>> movements;
	private final Map<TurningMovement,Ring> rings;
	private final Integer id, order;
	
	public SignalGroup(Integer id,
			Map<Link,Collection<TurningMovement>> linkMovements,
			Map<TurningMovement,Ring> rings
			) {
		this.id = id;
		this.movements = linkMovements;
		this.rings = rings;
		this.order = (id % 100)-1; //TODO generalize this
	}

	public Integer getID() {
		// TODO Auto-generated method stub
		return id;
	}
	
	public Integer getOrder() {return order;}

	public Collection<TurningMovement> getMovements(Link inLink) {
		// TODO Auto-generated method stub
		return movements.get(inLink);
	}

	public Double getGreenShare(TurningMovement tm) {
		// TODO Auto-generated method stub
		return rings.get(tm).getGreenShare(tm);
	}
	
	public Collection<Link> getLinks(){
		return movements.keySet();
	}
	
	public Stream<Ring> getRings(){
		return rings.values().stream().distinct();
	}

	public void updateMovementGreenShares(Map<TurningMovement, Double> deltaM) {
		// TODO Auto-generated method stub
		rings.values().stream().forEach(ring -> ring.updateGreenShares(deltaM));
	}

}
