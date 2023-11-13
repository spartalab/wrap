package edu.utexas.wrap.net;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

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
	
	public Collection<Link> getLinks(){
		return movements.keySet();
	}
	
	public Stream<Ring> getRings(){
		return rings.values().stream().distinct();
	}

}
