package edu.utexas.wrap.net;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import edu.utexas.wrap.assignment.AssignmentContainer;
import edu.utexas.wrap.assignment.bush.BackVector;
import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.assignment.sensitivity.DerivativeLink;
import edu.utexas.wrap.marketsegmentation.IndustryClass;
import edu.utexas.wrap.util.FibonacciHeap;
import edu.utexas.wrap.util.FibonacciLeaf;

public class Graph {
	
	private Map<Node, Set<Link>> outLinks;
	private Map<Node, Set<Link>> inLinks;
	private Map<Integer, Node> nodeMap;
	private List<Node> order;
//	private Map<Node,Integer> nodeOrder;
	private Set<Link> links;
	private Collection<TravelSurveyZone> zones;
	private int numZones;
	private int numNodes;
	private int numLinks;
	private byte[] md5;
	
	private Link[][] forwardStar;
	private Link[][] reverseStar;
	
	
	public Graph() {
		outLinks = (new HashMap<Node, Set<Link>>());
		inLinks = (new HashMap<Node, Set<Link>>());
		nodeMap = (new HashMap<Integer,Node>());
		order = (new ArrayList<Node>());
		links = new HashSet<Link>();
		numZones = 0;
		numNodes = 0;
		numLinks = 0;
		zones = new HashSet<TravelSurveyZone>();
	}
	
	public Graph(Graph g) {
		nodeMap = g.nodeMap;
		order = Collections.unmodifiableList(g.order);
		links = g.links;
		numZones = g.numZones;
		numNodes = g.numNodes;
		numLinks = g.numLinks;
		forwardStar = g.forwardStar;
		reverseStar = g.reverseStar;
		//TODO: duplicate zones
	}
	
	//TODO improve concurrency availability here
	public synchronized Boolean add(Link link) {
		numLinks++;
		Boolean altered = links.add(link);
		Node head = link.getHead();
		Node tail = link.getTail();

		Set<Link> headIns = inLinks.getOrDefault(head, (new HashSet<Link>()));
		Set<Link> tailOuts= outLinks.getOrDefault(tail,  (new HashSet<Link>()));

		altered |= headIns.add(link);
		altered |= tailOuts.add(link);
		if (altered) {
			inLinks.put(head, headIns);
			outLinks.put(tail, tailOuts);
			nodeMap.put(link.getHead().getID(), head);
			nodeMap.put(link.getTail().getID(), tail);
		}

		if (!order.contains(tail)) {
			order.add(tail);
			numNodes++;
		}
		if (!order.contains(head)) {
			order.add(head);
			numNodes++;
		}
		return altered;
	}
	
	public void addAll(Collection<Link> links) {
		for (Link l : links) add(l);
	}
	
//	public Boolean contains(Link l) {
//		for (Link m : inLinks(l.getHead())) {
//			if (m.equals(l)) return true;
//		}
//		for (Link m : outLinks(l.getHead())) {
//			if (m.equals(l)) return true;
//		}
//		return false;
//	}

	public Set<Link> getLinks(){
		return links;
	}
	
	public Node getNode(Integer id) {
		return nodeMap.get(id);
	}
	
	public Collection<Node> getNodes(){
		return order;
//		Set<Node> ret = new HashSet<Node>(inLinks.keySet());
//		ret.addAll(outLinks.keySet());
//		return ret;
//		return nodeOrder.keySet();
	}

	public int numZones() {
		return numZones;
	}

	public void setNumZones(int numZones) {
		this.numZones = numZones;
	}

//	public Link[] inLinks(Node u){
//		return reverseStar[u.getOrder()];
////		return inLinks.getOrDefault(u, Collections.emptySet());
//	}

	public Integer numNodes() {
		return numNodes;
	}
	
//	public Link[] outLinks(Node u) {
//		return forwardStar[u.getOrder()];
////		return outLinks.getOrDefault(u, Collections.emptySet()).stream().toArray(n->new Link[n]);
//	}

	public Boolean remove(Link link) {
		
		//FIXME doesn't remove from Node object
		Node tail = link.getTail();
		Node head = link.getHead();
		Boolean altered = false;
		for (int i = 0; i < forwardStar[tail.getOrder()].length;i++) {
			if (forwardStar[tail.getOrder()][i].equals(link)) {
				altered = true;
				forwardStar[tail.getOrder()][i] = null;
				break;
			}
		}
		for (int i = 0; i < reverseStar[head.getOrder()].length;i++) {
			if (reverseStar[tail.getOrder()][i].equals(link)) {
				altered = true;
				reverseStar[tail.getOrder()][i] = null;
				break;
			}
		}
		
//		Boolean altered = outLinks.get(link.getTail()).remove(link);
//		altered |= inLinks.get(link.getHead()).remove(link);
		return altered;
	}

	public void remove(Node node) {
		for (Link link : inLinks.getOrDefault(node, Collections.emptySet())) {
			outLinks.get(link.getTail()).remove(link);
		}
		for (Link link : outLinks.getOrDefault(node, Collections.emptySet())) {
			inLinks.get(link.getHead()).remove(link);
		}
		inLinks.remove(node);
		outLinks.remove(node);
	}

	public void printFlows(PrintStream out) {
		out.println("\r\nTail\tHead\tflow");
		for (Link l : getLinks()) {
			Double sum = l.getFlow();
			out.println(l+"\t"+sum);
		}
	}

	public void setMD5(byte[] md5) {
		this.md5 = md5;
	}
	
	public byte[] getMD5() {
		return md5;
	}
	
//	public int getOrder(Node n) {
//		return n.getOrder();
////		return nodeOrder.getOrDefault(n,-1);
//	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("struct/");
		for (byte b : getMD5()) {
			sb.append(String.format("%02X", b));
		}
		return sb.toString();
	}
	
	public int numLinks() {
		return numLinks;
	}
	
	public Map<Link, Link> getDerivativeLinks(Map<Link,Double> derivs, Link oldFocus, Link newFocus, Map<Node,Node> nodeMap){
		Map<Link, Link> linkMap = new HashMap<Link,Link>(numLinks,1.0f);

		
		IntStream.range(0,forwardStar.length).parallel().forEach(j ->{
			Link[] oldLinks = forwardStar[j];
			if (oldLinks == null) return;
			for (int i = 0; i < oldLinks.length; i++) {
				Link oldLink = oldLinks[i];
				Link newLink;
				if (oldLink.equals(oldFocus)) newLink = newFocus;
				else  newLink = new DerivativeLink(nodeMap.get(oldLink.getTail()), nodeMap.get(oldLink.getHead()), oldLink.getCapacity(), oldLink.getLength(), oldLink.freeFlowTime(), oldLink, derivs);
				linkMap.put(oldLink, newLink);
			}
		});
		return linkMap;
	}
	
	public Map<Node,Node> duplicateNodes(){
		return order.stream().collect(Collectors.toMap(Function.identity(), x -> new Node(x)));
	}

	public Graph getDerivativeGraph(Map<Link, Link> linkMap, Map<Node,Node> mapOfNodes) {
		// TODO Auto-generated method stub
		Graph ret = new Graph();
		
		ret.nodeMap = nodeMap;
		ret.numLinks = numLinks;
		ret.numNodes = numNodes;
		ret.numZones = numZones;
		ret.forwardStar = new Link[forwardStar.length][];
		ret.reverseStar = new Link[reverseStar.length][];
		ret.setMD5(getMD5());
		ret.order = order.stream().map(n -> mapOfNodes.get(n)).collect(Collectors.toList());
		
		IntStream.range(0,forwardStar.length).parallel().forEach(j ->{
			Link[] oldLinks = forwardStar[j];
			if (oldLinks==null) return;
			Link[] newLinks = new Link[oldLinks.length];
			for (int i = 0; i < oldLinks.length; i++) {
				Link oldLink = oldLinks[i];
				Link newLink = linkMap.get(oldLink);
				newLinks[i] = newLink;
			}
			ret.order.get(j).setForwardStar(newLinks);
			ret.forwardStar[j] = newLinks;
		});
		
		IntStream.range(0, reverseStar.length).parallel().forEach(i->{
//		for (int i = 0; i < reverseStar.length; i++) {
			Link[] oldLinks = reverseStar[i];
			if (oldLinks == null) return;
			Link[] newLinks = new Link[oldLinks.length];
			for (int j = 0; j < oldLinks.length; j++) {
				Link l = oldLinks[j];
				Link ll = linkMap.get(l);
				newLinks[j] = ll;
				
			}
			ret.reverseStar[i] = newLinks;
			ret.order.get(i).setReverseStar(newLinks);
		});
		return ret;
	}
	
	public void complete() {
		forwardStar = new Link[numNodes][];
		reverseStar = new Link[numNodes][];
		outLinks.keySet().parallelStream().forEach(x -> {
			Link[] fs = outLinks.get(x).stream().toArray(Link[]::new);
			forwardStar[x.getOrder()] = fs;
			x.setForwardStar(fs);
		});
		inLinks.keySet().parallelStream().forEach(x ->{
			Link[] rs = inLinks.get(x).stream().toArray(Link[]::new);
			reverseStar[x.getOrder()] = rs;
			x.setReverseStar(rs);
			
		});
		outLinks = null;
		inLinks = null;
	}

	public Collection<TravelSurveyZone> getTSZs() {
		return zones;
	}
	
	public boolean addZone(TravelSurveyZone zone) {
		return zones.add(zone);
	}

//	public Set<RegionalAreaAnalysisZone> getRAAs() {
//		return zones.parallelStream().map(TravelSurveyZone::getRAA).collect(Collectors.toSet());
//	}
	


	public void readHouseholdsByWorkersVehiclesAndIncomeGroups(Path igWkrVehFile) throws IOException {
		Files.lines(igWkrVehFile).parallel().filter(line -> !line.startsWith("TSZ")).forEach(line ->{
			String[] args = line.split(",");
			int tszID = Integer.parseInt(args[0]);
			

			if (args.length < 72) {
				args = new String[]{args[0],"0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0"};
			}
			String[] newArgs = args;
			
			TravelSurveyZone tsz = getNode(tszID).getZone();

			IntStream.range(1, 65).parallel().boxed().forEach(idx ->{
				double val = Double.parseDouble(newArgs[idx]);
				int wkr, veh, ig;
				ig = (idx-1) % 4 + 1;
				veh = ((idx-1)/4) % 4;
				wkr = ((idx-1)/16) % 4;
				tsz.setHouseholdsByIncomeGroupThenWorkersThenVehicles(ig, wkr, veh, val);
			});
		});
	}

	public void readHouseholdsByIncomeGroup(Path igFile) throws IOException {
		Files.lines(igFile).parallel().filter(line -> !line.startsWith("TSZ")).forEach(line ->{
			String[] args = line.split(",");
			int tszID = Integer.parseInt(args[0]);
			
			Map<Integer, Double> hhByIG = IntStream.range(1, 4).parallel().boxed().collect(
					Collectors.toMap(Function.identity(), ig -> Double.parseDouble(args[ig])));
			
			TravelSurveyZone tsz = getNode(tszID).getZone();
			tsz.setHouseholdsByIncomeGroup(hhByIG);
		});
	}
	
	public void readEmploymentData(Path employmentFile) throws IOException {
		Files.lines(employmentFile).parallel().filter(line -> !line.startsWith("TSZ")).forEach(line -> {
			String[] args = line.split(",");
			
			if (args.length < 11) {
				args = new String[]{args[0],args[1],"0","0","0","0","0","0","0","0","0"};
			}
			int tszID = Integer.parseInt(args[0]);
			
			AreaClass ac;
			switch (Integer.parseInt(args[1])) {
			case 1:
				ac = AreaClass.CBD;
				break;
			case 2:
				ac = AreaClass.OBD;
				break;
			case 3:
				ac = AreaClass.URBAN_RESIDENTIAL;
				break;
			case 4:
				ac = AreaClass.SUBURBAN_RESIDENTIAL;
				break;
			case 5:
				ac = AreaClass.RURAL;
				break;
			default:
				throw new RuntimeException("Unknown area type");
			}
			
			Map<Integer,Map<IndustryClass,Double>> empByIGthenIC = new HashMap<Integer,Map<IndustryClass,Double>>();
			Map<IndustryClass,Double> ig1 = new HashMap<IndustryClass,Double>();
			ig1.put(IndustryClass.BASIC, Double.parseDouble(args[2]));
			ig1.put(IndustryClass.RETAIL, Double.parseDouble(args[3]));
			ig1.put(IndustryClass.SERVICE, Double.parseDouble(args[4]));
			empByIGthenIC.put(1, ig1);
			
			Map<IndustryClass,Double> ig2 = new HashMap<IndustryClass,Double>();
			ig2.put(IndustryClass.BASIC, Double.parseDouble(args[5]));
			ig2.put(IndustryClass.RETAIL, Double.parseDouble(args[6]));
			ig2.put(IndustryClass.SERVICE, Double.parseDouble(args[7]));
			empByIGthenIC.put(2, ig2);
			
			Map<IndustryClass,Double> ig3 = new HashMap<IndustryClass,Double>();
			ig3.put(IndustryClass.BASIC, Double.parseDouble(args[8]));
			ig3.put(IndustryClass.RETAIL, Double.parseDouble(args[9]));
			ig3.put(IndustryClass.SERVICE, Double.parseDouble(args[10]));
			empByIGthenIC.put(3, ig3);
			
			TravelSurveyZone tsz = getNode(tszID).getZone();
			tsz.setAreaClass(ac);
			tsz.setEmploymentByIncomeGroupThenIndustry(empByIGthenIC);
		});
	}

	public void loadDemand(AssignmentContainer container) {
		container.flows().entrySet().parallelStream().forEach(pair -> pair.getKey().changeFlow(pair.getValue()));
	}

	public double cheapestCostPossible(AssignmentContainer container) {
		// TODO Auto-generated method stub
		Collection<Node> nodes = getNodes();
		BackVector[] initMap = new BackVector[nodes.size()];
		FibonacciHeap<Node> Q = new FibonacciHeap<Node>(nodes.size(),1.0f);
		
		Double cost = 0.0;
		
		for (Node n : nodes) {
			if (!n.equals(container.root().node())) {
				Q.add(n, Double.MAX_VALUE);
			}
		}
		Q.add(container.root().node(), 0.0);

		while (!Q.isEmpty()) {
			FibonacciLeaf<Node> u = Q.poll();
			cost += container.demand(u.node) * u.key;
			
			for (Link uv : u.node.forwardStar()) {
				//TODO expand this admissibility check to other implementations of AssignmentContainer
				if (container instanceof Bush && !((Bush) container).canUseLink(uv)) continue;
//				if (!uv.allowsClass(c) || isInvalidConnector(uv)) continue;
				
				
				//If this link doesn't allow this bush's class of driver on the link, don't consider it
				//This was removed to allow flow onto all links for the initial bush, and any illegal
				//flow will be removed on the first flow shift due to high price
				
				FibonacciLeaf<Node> v = Q.getLeaf(uv.getHead());
				Double alt = uv.getPrice(container)+u.key;
				if (alt<v.key) {
					Q.decreaseKey(v, alt);
					initMap[v.node.getOrder()] = uv;
				}
			}
		}
		return cost;
	}
}
