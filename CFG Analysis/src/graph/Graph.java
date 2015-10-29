package graph;

import java.util.*;

public class Graph 
{
	private ArrayList<Vertex> lstVertex = new ArrayList<Vertex>();
	private ArrayList<Edge> lstEdge = new ArrayList<Edge>();

	public ArrayList<Edge> getEdgeList()
	{
		return lstEdge;
	}
	public ArrayList<Vertex> getVerticesList()
	{
		return lstVertex;
	}
	
	public void AddVertex(Vertex pNewVertex)
	{
		lstVertex.add(pNewVertex);
	}
	public void AddEdge(Edge pNewEdge)
	{
		lstEdge.add(pNewEdge);
		pNewEdge.getSource().getOutEdge().add(pNewEdge);
		pNewEdge.getTarget().getInEdge().add(pNewEdge);
	}

	public Vertex getVertexByLabel(String pLabel)
	{
		for (Vertex v : lstVertex)
		{ 
			if (v.getLabel().toUpperCase().equals(pLabel.toUpperCase()))	// Case insensitive
				return v;
		}
		return null;
	}
	public Vertex getVertexByName(String pName)
	{
		for (Vertex v : lstVertex)
		{ 
			if (v.getName().toUpperCase().equals(pName.toUpperCase()))	// Case insensitive
				return v;
		}
		return null;
	}
	public Edge FindEdgeByLabel(String pLabel)
	{
		for (Edge e : lstEdge)
		{ 
			if (e.getLabel().toUpperCase().equals(pLabel.toUpperCase()))	// Case insensitive
				return e;
		}
		return null;
	}
	
	public void DeleteEdge(Edge e)
	{
		lstEdge.remove(e);						// Remove from graph edge list
		e.getTarget().getInEdge().remove(e);	// Remove from target's InEdge list
		e.getSource().getOutEdge().remove(e);	// Remove from source's OutEdge list
	}
	
	public void DeleteVertex(Vertex v)
	{
		lstVertex.remove(v);			// Remove from graph vertex list
		for (Edge e : v.getOutEdge())	// Remove all edges leaving v from targets' InEdge list
		{
			e.getTarget().getInEdge().remove(e);
			lstEdge.remove(e);
		}
		for (Edge e : v.getInEdge())	// Remove all edges entering v from sources' OutEdge list
		{
			e.getSource().getOutEdge().remove(e);
			lstEdge.remove(e);
		}
	}
	
	// Simple DFS. plstVisited is the ordered vertex list of the cycle
	// If an unvisited edge leads to a visited vertex, there's a cycle
	// Vertex has PrevVertex pointers to where it was traveled from, use that to build the list of vertices in the cycle
	// Note that we also have to check incoming edges into the current vertex as the analysis need to find all cycle as if the graph is undirected
	public boolean FindCycle(Vertex pvCurrent, ArrayList<Vertex> plstVisited)
    {
        Vertex vTarget;

        if (pvCurrent.getVisited())    // Cycle exists, visited list
        {
        	vTarget = pvCurrent.getPrevVertex();
            while (vTarget.getPrevVertex() != null && vTarget != pvCurrent)
            {
                plstVisited.add(0, vTarget);	// Insert vertex to beginning of the list
                vTarget = vTarget.getPrevVertex();
            }
            plstVisited.add(0, pvCurrent);	// This is the first vertex in the cycle
            return true;
        }
        else
        {
            pvCurrent.setVisited(true);
            
            // Forward Edges
            for (Edge eOutgoing : pvCurrent.getOutEdge())
            {
                if (!eOutgoing.getVisited() && !eOutgoing.getIndependent())
                {
                    eOutgoing.setVisited(true);
                    vTarget = eOutgoing.getTarget();
                    vTarget.setPrevVertex(pvCurrent);
                    if (FindCycle(vTarget, plstVisited))
                    	return true;
                }
            }

            // Backward Edges
            for (Edge eIncoming : pvCurrent.getInEdge())
            {
                if (!eIncoming.getVisited() && !eIncoming.getIndependent())
                {
                	eIncoming.setVisited(true);
                    vTarget = eIncoming.getSource();
                    vTarget.setPrevVertex(pvCurrent);
                    if (FindCycle(vTarget, plstVisited))
                    	return true;
                }
            }
        }
        return false;
    }
	
	public void ResetGraph(boolean pResetAll)
	{
		for (Vertex v : lstVertex)
		{
			v.setVisited(false);
		}
		for (Edge e : lstEdge)
		{
			if (pResetAll)
				e.setIndependent(false);
			e.setVisited(false);
		}
	}
	
	// Just some funky code that set the curve of edges when their endpoints are identical so they don't overlap
	// Need a better idea to do this
	public void CurveEdges()
	{
		Vertex vTarget;
		ResetGraph(false);
		for (Vertex v : lstVertex)
		{
			for (Edge e : v.getOutEdge())
			{
				int angle=0;		
				e.setVisited(true);
				vTarget = e.getTarget();
				for (Edge e2 : v.getOutEdge())
				{
					if (!e2.getVisited() && e2.getTarget() == vTarget)
					{
						e.setVisited(true);
						e.setCurve(1);
						e2.setCurve((int) Math.pow(-1, ++angle) * angle);
					}
				}
			}
		}
		for (Edge e : lstEdge)
		{
			e.setVisited(false);
		}
	}
	
	public void PrintGraph(boolean bVertex, boolean bEdge)
	{
		System.out.println();
		if (bVertex)
		{
			for (Vertex v : lstVertex)
			{
				System.out.println("Vertex: " + v.getLabel() + " " + v.getType());
			}
		}
		if (bEdge)
		{
			for (Edge e : lstEdge)
			{
				System.out.println("Edge " + e.getLabel() + " : " + e.getSource().getLabel() + " -> " + e.getTarget().getLabel() + " " + (e.getFlowType() != null ? e.getFlowType().toString() : ""));
			}
		}
	}
}
