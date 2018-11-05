package edu.utexas.wrap;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bson.conversions.Bson;
/**
 * @author rahulpatel
 *
 */
public abstract class Link implements Priced {

	private static final String dbName = "network";

	private final Float capacity;
	private final Node head;
	private final Node tail;
	private final Float length;
	private final Float fftime;

	private BigDecimal cachedFlow = null;
	protected BigDecimal cachedTT = null;
	protected BigDecimal cachedPrice = null;
	private static MongoDatabase databaseCon;
	static {
		MongoClient server = new MongoClient ("local host", 27107);
		MongoDatabase databaseCon = server.getDatabase(dbName);
		
	}

	public Link(Node tail, Node head, Float capacity, Float length, Float fftime) {
		this.tail = tail;
		this.head = head;
		this.capacity = capacity;
		this.length = length;
		this.fftime = fftime;
		createTable();
	}


	private void createTable() {
        databaseCon.createCollection("t" + hashCode());
        
	}

	private BigDecimal totalFlowFromTable() {
		
			MongoCollection<Document> collection = databaseCon.getCollection("t" + hashCode());
			BigDecimal total = BigDecimal.ZERO;
		try (MongoCursor<Document> cursor = collection.find().iterator()) {
			while (cursor.hasNext()) {
				Document d = cursor.next();
				total = total.add(d.get("flow", BigDecimal.class));
			}
		}
		return total;
	}
	public Float getCapacity() {
		return capacity;
	}

	public Float freeFlowTime() {
		return fftime;
	}

	public Node getHead() {
		return head;
	}

	public Node getTail() {
		return tail;
	}

	public Float getLength() {
		return length;
	}

	public BigDecimal getFlow() {
		if (cachedFlow != null) return cachedFlow;
		//BigDecimal f = BigDecimal.ZERO;
		//for (Bush b : flow.keySet()) f = f.add(flow.get(b));
		BigDecimal f = totalFlowFromTable();
		if (f.compareTo(BigDecimal.ZERO) < 0) throw new NegativeFlowException("Negative link flow");
		cachedFlow = f;
		return f;
	}

	public String toString() {
		return tail.toString() + "\t" + head.toString();
	}

	public abstract BigDecimal getTravelTime();
	
	public abstract BigDecimal tPrime();

	public abstract BigDecimal tIntegral();

	public abstract BigDecimal getPrice(Float vot, VehicleClass c);

	public abstract BigDecimal pricePrime(Float float1);

	/** Modifies the flow on a link which comes from a specified bush. 
	 * <b> THIS METHOD SHOULD ONLY BE CALLED BY THE {@link edu.utexas.wrap.Bush}'s {@link edu.utexas.wrap.Bush.changeFlow} METHOD </b>
	 * @param delta amount by how much the flow should be altered
	 * @param bush the origin Bush of this flow
	 * @return whether the flow from this bush on the link is non-zero
	 */
	public synchronized Boolean alterBushFlow(BigDecimal delta, Bush bush) {
		if (delta.compareTo(BigDecimal.ZERO) != 0) {
			cachedTT = null;
			cachedPrice = null;
			cachedFlow = null;
		}
		//Retrieve Bush with the specified parameters
		BigDecimal updateFlow = getBushFlow(bush);
		updateFlow = updateFlow.add(delta).setScale(Optimizer.decimalPlaces, RoundingMode.HALF_EVEN);
		if(updateFlow.compareTo(BigDecimal.ZERO) < 0) throw new NegativeFlowException("invalid alter request");
		else if(updateFlow.compareTo(BigDecimal.ZERO) > 0) {
			MongoCollection<Document> collection = databaseCon.getCollection("t" + hashCode());
			Bson filter;
			Bson update;
			if(bush.getVehicleClass() != null) {
				filter = and(eq("bush_origin_id", bush.getOrigin().getID()), eq("vot_real", bush.getVOT()), eq("vehicle_class", bush.getVehicleClass().toString()));
				update = new Document("$set", new Document().append("bush_origin_id", bush.getOrigin().getID()).append("vot_real", bush.getVOT()).append("vehicle_class", bush.getVehicleClass().toString()).append("flow", updateFlow));
			}else {
				filter = and(eq("bush_origin_id", bush.getOrigin().getID()), eq("vot_real", bush.getVOT()), eq("vehicle_class", bush.toString()));
				update = new Document("$set", new Document().append("bush_origin_id", bush.getOrigin().getID()).append("vot_real", bush.getVOT()).append("vehicle_class", bush.toString()).append("flow", updateFlow));
			}

			UpdateOptions options = new UpdateOptions().upsert(true);
			collection.updateOne(filter, update, options);
			return true;
		} else {
			MongoCollection<Document> collection = databaseCon.getCollection("t" + hashCode());
			Bson filter;
			if(bush.getVehicleClass() != null) {
				filter = and(eq("bush_origin_id", bush.getOrigin().getID()), eq("vot_real", bush.getVOT()), eq("vehicle_class", bush.getVehicleClass().toString()));
			}else {
				filter = and(eq("bush_origin_id", bush.getOrigin().getID()), eq("vot_real", bush.getVOT()), eq("vehicle_class", bush.toString()));
			}
			collection.deleteOne(filter);
			return false;
		}
		//If Bush exists, get the flow and add delta to flow
			//If new flow is < 0 throw error
			//if new flow == 0 remove the row and return false
			//If new flow is > 0 update the row in the table
		//Otherwise add Bush with delta flow
//		BigDecimal newFlow = flow.getOrDefault(bush,BigDecimal.ZERO).add(delta).setScale(Optimizer.decimalPlaces, RoundingMode.HALF_EVEN);
//		if (newFlow.compareTo(BigDecimal.ZERO) < 0) throw new NegativeFlowException("invalid alter request");
//		else if (newFlow.compareTo(BigDecimal.ZERO) > 0) flow.put(bush, newFlow);
//		else {
//			flow.remove(bush);
//			return false;
//		}
	}

	public BigDecimal getBushFlow(Bush bush) {
		BigDecimal output = BigDecimal.ZERO;
		MongoCollection<Document> collection = databaseCon.getCollection("t" + hashCode());
		Bson filter;
		if(bush.getVehicleClass() != null) {
			filter = and(eq("bush_origin_id", bush.getOrigin().getID()), eq("vot_real", bush.getVOT()), eq("vehicle_class", bush.getVehicleClass().toString()));
		}else {
			filter = and(eq("bush_origin_id", bush.getOrigin().getID()), eq("vot_real", bush.getVOT()), eq("vehicle_class", bush.toString()));
		}
		try (MongoCursor<Document> cursor = collection.find(filter).iterator()) {
			if (cursor.hasNext()) {
				Document d = cursor.next();
				output = (d.get("flow", BigDecimal.class));
			}
		}
		return output;
	}

	public Boolean hasFlow(Bush bush) {
		MongoCollection<Document> collection = databaseCon.getCollection("t" + hashCode());
		Bson filter;
		if(bush.getVehicleClass() != null) {
			filter = and(eq("bush_origin_id", bush.getOrigin().getID()), eq("vot_real", bush.getVOT()), eq("vehicle_class", bush.getVehicleClass().toString()));
		}else {
			filter = and(eq("bush_origin_id", bush.getOrigin().getID()), eq("vot_real", bush.getVOT()), eq("vehicle_class", bush.toString()));
		}
		MongoCursor<Document> cursor = collection.find(filter).iterator();
		return cursor.hasNext();

	}

	public void removeTable() {
		MongoCollection<Document> collection = databaseCon.getCollection("t" + hashCode());
		collection.drop();
	}
	
	public abstract Boolean allowsClass(VehicleClass c);
}