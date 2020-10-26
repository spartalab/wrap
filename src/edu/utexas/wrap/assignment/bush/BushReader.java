package edu.utexas.wrap.assignment.bush;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import edu.utexas.wrap.assignment.AssignmentProvider;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;

public class BushReader implements AssignmentProvider<Bush> {
	Graph network;
	private Path inputPath;
	
	public BushReader(Graph network, Path ioPath) {
		this.network = network;
		this.inputPath = ioPath;
	}

	public void getStructure(Bush bush) throws IOException {
		//Load the bush from the network directory's file matching the bush's hash code
		InputStream in = Files.newInputStream(
				inputPath
				.resolve(network.toString())
				.resolve(Integer.toString(bush.hashCode())));
		readFromStream(bush, in);
		in.close();
	}
	
	private void readFromStream(Bush bush, InputStream in) throws IOException {
		long sz = in.available();
		long pos = 0;
		
		//ensure that we're not overwriting an older structure
		bush.clear();
		BackVector[] q = new BackVector[network.numNodes()];
		
		byte[] b = new byte[Integer.BYTES*2+Double.BYTES];
		
		//For each link in the bush
		while (sz-pos >= Integer.BYTES*2+Double.BYTES) {
			//File IO, formatting
			in.read(b);
			pos += Integer.BYTES*2+Double.BYTES;
			ByteBuffer bb = ByteBuffer.wrap(b);
			processBytes(bush, q, bb);

//			bush.add(bv);
//			throw new RuntimeException("BackVector splits aren't implemented yet");
		}
		bush.setQ(q);
	}

	private void processBytes(Bush bush, BackVector[] q, ByteBuffer bb) {
		Integer nid = bb.getInt();
		Integer bvhc = bb.getInt();
		Double split = bb.getDouble();
		Node n = network.getNode(nid);

		//Find the appropriate link instance
//			Link bv = null;
		Optional<Link> bvo = Stream.of(n.reverseStar()).parallel().filter(l -> l.hashCode()==bvhc).findAny();

		//If it can't be found, throw an error
		if (!bvo.isPresent()) throw new RuntimeException("Unknown Link econuntered. Node ID: "+nid+"\tHash: "+bvhc);
		
		Link bv = bvo.get();
		
		if (split >= 1.0) {
		
			if (q[n.getOrder()] == null) {
				q[n.getOrder()] = bv;
			} 
			
			else {
				if (q[n.getOrder()] instanceof BushMerge) {
					((BushMerge) q[n.getOrder()]).add(bv);
					((BushMerge) q[n.getOrder()]).setSplit(bv, split);
				} else {
					q[n.getOrder()] = new BushMerge(bush, bv, (Link) q[n.getOrder()]);
				}
			}
		
		} 

		
		else if (split >=0.0) {
			
			if (q[n.getOrder()] == null) {
				BushMerge bm = new BushMerge(bush, n);
				bm.add(bv);
				bm.setSplit(bv, split);
				q[n.getOrder()] = bm;
			} 
			
			
			else {
				
				if (q[n.getOrder()] instanceof BushMerge) {
					((BushMerge) q[n.getOrder()]).add(bv);
					((BushMerge) q[n.getOrder()]).setSplit(bv, split);
				} 
				
				else {
					q[n.getOrder()] = new BushMerge(bush, (Link) q[n.getOrder()], bv);
				}
			}
		}
	}

}
