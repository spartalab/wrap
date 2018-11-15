package edu.utexas.wrap;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.*;


import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author rahulpatel
 *
 */
public abstract class Link implements Priced {

    private static final String dbName = "network";

    private final float capacity, length, fftime;
	private final Node head;
	private final Node tail;

	private Double cachedFlow = null;
	protected Double cachedTT = null;
	protected Double cachedPrice = null;
    private static MongoDatabase databaseCon;
	public Link(Node tail, Node head, Float capacity, Float length, Float fftime) {
		this.tail = tail;
		this.head = head;
		this.capacity = capacity;
		this.length = length;
		this.fftime = fftime;
		createTable();
	}
    static {
        Logger  mongoLogger = Logger.getLogger("com.mongodb");
        mongoLogger.setLevel(Level.SEVERE);
        MongoClient server = new MongoClient ("127.0.0.1", 27017);
        databaseCon = server.getDatabase(dbName);

    }

	private void createTable() {
        databaseCon.createCollection("t" + hashCode());
	}

	private Double totalFlowFromTable() {

			MongoCollection<Document> collection = databaseCon.getCollection("t" + hashCode());
			Double total = 0.0;
		try (MongoCursor<Document> cursor = collection.find().iterator()) {
			while (cursor.hasNext()) {
				Document d = cursor.next();
				total += d.getDouble("flow");
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

	public Double getFlow() {
		if (cachedFlow != null) return cachedFlow;
		Double f = totalFlowFromTable();
		if (f < 0) throw new NegativeFlowException("Negative link flow");
		cachedFlow = f;
		return f;
	}

	public String toString() {
		return tail.toString() + "\t" + head.toString();
	}

	public abstract Double tPrime();

	public abstract Double tIntegral();

	public abstract Double pricePrime(Float float1);

	/** Modifies the flow on a link which comes from a specified bush. 
	 * <b> THIS METHOD SHOULD ONLY BE CALLED BY THE {@link edu.utexas.wrap.Bush}'s {@link edu.utexas.wrap.Bush.changeFlow} METHOD </b>
	 * @param delta amount by how much the flow should be altered
	 * @param bush the origin Bush of this flow
	 * @return whether the flow from this bush on the link is non-zero
	 */
	public synchronized Boolean alterBushFlow(Double delta, Bush bush) {
		if (delta != 0) {
			cachedTT = null;
			cachedPrice = null;
			cachedFlow = null;
		}
		//Retrieve Bush with the specified parameters
		Double updateFlow = getBushFlow(bush);
		updateFlow += delta;
		if(updateFlow < 0) throw new NegativeFlowException("invalid alter request");
		else if(updateFlow > 0) {
			MongoCollection<Document> collection = databaseCon.getCollection("t" + hashCode());
			Bson filter;
			Bson update;
			if(bush.getVehicleClass() != null) {
				filter = and(eq("bush_origin_id", bush.getOrigin().hashCode()), eq("vot_real", bush.getVOT()), eq("vehicle_class", bush.getVehicleClass().toString()));
				update = new Document("$set", new Document().append("bush_origin_id", bush.getOrigin().hashCode()).append("vot_real", bush.getVOT()).append("vehicle_class", bush.getVehicleClass().toString()).append("flow", updateFlow));
			}else {
				filter = and(eq("bush_origin_id", bush.getOrigin().hashCode()), eq("vot_real", bush.getVOT()), eq("vehicle_class", bush.toString()));
				update = new Document("$set", new Document().append("bush_origin_id", bush.getOrigin().hashCode()).append("vot_real", bush.getVOT()).append("vehicle_class", bush.toString()).append("flow", updateFlow));
			}

			UpdateOptions options = new UpdateOptions().upsert(true);
			collection.updateOne(filter, update, options);
			return true;
		} else {
			MongoCollection<Document> collection = databaseCon.getCollection("t" + hashCode());
			Bson filter;
			if(bush.getVehicleClass() != null) {
				filter = and(eq("bush_origin_id", bush.getOrigin().hashCode()), eq("vot_real", bush.getVOT()), eq("vehicle_class", bush.getVehicleClass().toString()));
			}else {
				filter = and(eq("bush_origin_id", bush.getOrigin().hashCode()), eq("vot_real", bush.getVOT()), eq("vehicle_class", bush.toString()));
			}
			collection.deleteOne(filter);
			return false;
		}
	}

	public Double getBushFlow(Bush bush) {
		Double output = 0.0;
		MongoCollection<Document> collection = databaseCon.getCollection("t" + hashCode());
		Bson filter;
		if(bush.getVehicleClass() != null) {
			filter = and(eq("bush_origin_id", bush.getOrigin().hashCode()), eq("vot_real", bush.getVOT()), eq("vehicle_class", bush.getVehicleClass().toString()));
		}else {
			filter = and(eq("bush_origin_id", bush.getOrigin().hashCode()), eq("vot_real", bush.getVOT()), eq("vehicle_class", bush.toString()));
		}
		try (MongoCursor<Document> cursor = collection.find(filter).iterator()) {
			if (cursor.hasNext()) {
				Document d = cursor.next();
				output = d.getDouble("flow");
			}
		}
		return output;
	}

	public Float getLength() {
		return length;
	}

	public abstract Double getPrice(Float vot, VehicleClass c);

	public Node getTail() {
		return tail;
	}

	public abstract Double getTravelTime();

	public Boolean hasFlow(Bush bush) {
		MongoCollection<Document> collection = databaseCon.getCollection("t" + hashCode());
		Bson filter;
		if(bush.getVehicleClass() != null) {
			filter = and(eq("bush_origin_id", bush.getOrigin().hashCode()), eq("vot_real", bush.getVOT()), eq("vehicle_class", bush.getVehicleClass().toString()));
		}else {
			filter = and(eq("bush_origin_id", bush.getOrigin().hashCode()), eq("vot_real", bush.getVOT()), eq("vehicle_class", bush.toString()));
		}
		MongoCursor<Document> cursor = collection.find(filter).iterator();
		boolean output =  cursor.hasNext();
		cursor.close();
		return output;

	}

	public void removeTable() {
		MongoCollection<Document> collection = databaseCon.getCollection("t" + hashCode());
		collection.drop();
	}
	
	public abstract Boolean allowsClass(VehicleClass c);
}