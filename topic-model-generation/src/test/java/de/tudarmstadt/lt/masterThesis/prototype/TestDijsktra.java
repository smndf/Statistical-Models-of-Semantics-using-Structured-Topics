package de.tudarmstadt.lt.masterThesis.prototype;

import java.util.Map;

import org.mortbay.jetty.servlet.HashSessionIdManager;

import util.Dijkstra;
import graph.ArrayBackedGraph;
import graph.Graph;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestDijsktra extends TestCase {

	public static void main(String[] args) {

		Graph<Integer,Float> g = new ArrayBackedGraph<>(10, 10);
		
		g.addNode(0); //A
		g.addNode(1); //B
		g.addNode(2); //C
		g.addNode(3); //D
		g.addNode(4); //E
		
		g.addEdgeUndirected(0, 1, (float) 0.5);
		g.addEdgeUndirected(0, 2, (float) 0.2);
		g.addEdgeUndirected(0, 3, (float) 0.25);
		g.addEdgeUndirected(2, 4, (float) 0.3333);
		g.addEdgeUndirected(1, 2, (float) 0.5);
		
		Integer centroid = 0;
		int size = g.getSize();
		Map<Integer,Float> dist = Dijkstra.runAlgo(g, centroid);

		assertTrue( dist.size() == size );
		for (Integer node : dist.keySet()){
			System.out.println("dist["+node+"] = "+dist.get(node));
		}
		
	}


}
