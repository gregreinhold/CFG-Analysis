package graph;

import java.util.ArrayList;


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
	
	public void addVertex(Vertex pNewVertex)
	{
		lstVertex.add(pNewVertex);
	}
	public void addEdge(Edge pNewEdge)
	{
		lstEdge.add(pNewEdge);
		pNewEdge.getSource().getOutEdgeList().add(pNewEdge);
		pNewEdge.getTarget().getInEdgeList().add(pNewEdge);
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
	public Edge findEdgeByLabel(String pLabel)
	{
		for (Edge e : lstEdge)
		{ 
			if (e.getLabel().toUpperCase().equals(pLabel.toUpperCase()))	// Case insensitive
				return e;
		}
		return null;
	}
	
	public void deleteEdge(Edge e)
	{
		lstEdge.remove(e);						// Remove from graph edge list
		e.getTarget().getInEdgeList().remove(e);	// Remove from target's InEdge list
		e.getSource().getOutEdgeList().remove(e);	// Remove from source's OutEdge list
	}
	
	public void deleteVertex(Vertex v)
	{
		lstVertex.remove(v);			// Remove from graph vertex list
		for (Edge e : v.getOutEdgeList())	// Remove all edges leaving v from targets' InEdge list
		{
			e.getTarget().getInEdgeList().remove(e);
		}
		for (Edge e : v.getInEdgeList())	// Remove all edges entering v from sources' OutEdge list
		{
			e.getSource().getOutEdgeList().remove(e);
		}
	}
	
	// Simple DFS. plstVisited is the ordered vertex list of the cycle
	// If an unvisited edge leads to a visited vertex, there's a cycle
	// Vertex has PrevVertex pointers to where it was traveled from, use that to build the list of vertices in the cycle
	// Note that we also have to check incoming edges into the current vertex as the analysis need to find all cycle as if the graph is undirected
	public boolean findCycle(Vertex pvCurrent, ArrayList<Vertex> plstVisited)
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
            for (Edge eOutgoing : pvCurrent.getOutEdgeList())
            {
                if (!eOutgoing.getVisited() && !eOutgoing.getIndependent())
                {
                    eOutgoing.setVisited(true);
                    vTarget = eOutgoing.getTarget();
                    vTarget.setPrevVertex(pvCurrent);
                    if (findCycle(vTarget, plstVisited))
                    	return true;
                }
            }

            // Backward Edges
            for (Edge eIncoming : pvCurrent.getInEdgeList())
            {
                if (!eIncoming.getVisited() && !eIncoming.getIndependent())
                {
                	eIncoming.setVisited(true);
                    vTarget = eIncoming.getSource();
                    vTarget.setPrevVertex(pvCurrent);
                    if (findCycle(vTarget, plstVisited))
                    	return true;
                }
            }
        }
        return false;
    }
	
	public void resetGraph(boolean pResetAll)
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
	public void curveEdges()
	{
		Vertex vTarget;
		boolean blnForwardCycle, blnBackwardCycle;
		resetGraph(false);
		for (Vertex v : lstVertex)
		{
			for (Edge e : v.getOutEdgeList())
			{
				blnForwardCycle = false;
				blnBackwardCycle = false;
				int angle=0;		
				e.setVisited(true);
				vTarget = e.getTarget();
				for (Edge e2 : v.getOutEdgeList())
				{
					if (!e2.getVisited() && e2.getTarget() == vTarget)
					{
						e.setVisited(true);
						blnForwardCycle = true;
						e.setCurve(1);
						e2.setCurve((int) Math.pow(-1, ++angle) * angle);
					}
				}/*
				for (Edge e2 : v.GetInEdge())
				{
					if (!e2.GetVisited() && e2.GetSource() == vTarget)
					{
						e.SetVisited(true);
						if (blnForwardCycle)	// 2 out, 1 in
						{
							e2.SetCurve(2);
						}
						else // 1 in
						{
							if (blnBackwardCycle)	// 1 in, 2 out
							{
								e2.SetCurve(2);
							}
							else // 1 in, 1 out
							{
								blnBackwardCycle = true;
								e2.SetCurve(-1);
							}
							e.SetCurve(-1);
						}
					}
				}*/
			}
		}
		for (Edge e : lstEdge)
		{
			e.setVisited(false);
		}
	}
}
