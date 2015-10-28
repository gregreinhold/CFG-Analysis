package graph;

import static graph.Constant.*;
import java.util.*;

public class Graph 
{
	private ArrayList<Vertex> lstVertex = new ArrayList<Vertex>();
	private ArrayList<Edge> lstEdge = new ArrayList<Edge>();

	public ArrayList<Edge> GetEdge()
	{
		return lstEdge;
	}
	public ArrayList<Vertex> GetVertex()
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
		pNewEdge.GetSource().GetOutEdge().add(pNewEdge);
		pNewEdge.GetTarget().GetInEdge().add(pNewEdge);
	}

	public Vertex GetVertexByLabel(String pLabel)
	{
		for (Vertex v : lstVertex)
		{ 
			if (v.GetLabel().toUpperCase().equals(pLabel.toUpperCase()))	// Case insensitive
				return v;
		}
		return null;
	}
	public Vertex GetVertexByName(String pName)
	{
		for (Vertex v : lstVertex)
		{ 
			if (v.GetName().toUpperCase().equals(pName.toUpperCase()))	// Case insensitive
				return v;
		}
		return null;
	}
	public Edge FindEdgeByLabel(String pLabel)
	{
		for (Edge e : lstEdge)
		{ 
			if (e.GetLabel().toUpperCase().equals(pLabel.toUpperCase()))	// Case insensitive
				return e;
		}
		return null;
	}
	
	public void DeleteEdge(Edge e)
	{
		lstEdge.remove(e);						// Remove from graph edge list
		e.GetTarget().GetInEdge().remove(e);	// Remove from target's InEdge list
		e.GetSource().GetOutEdge().remove(e);	// Remove from source's OutEdge list
	}
	
	public void DeleteVertex(Vertex v)
	{
		lstVertex.remove(v);			// Remove from graph vertex list
		for (Edge e : v.GetOutEdge())	// Remove all edges leaving v from targets' InEdge list
		{
			e.GetTarget().GetInEdge().remove(e);
			lstEdge.remove(e);
		}
		for (Edge e : v.GetInEdge())	// Remove all edges entering v from sources' OutEdge list
		{
			e.GetSource().GetOutEdge().remove(e);
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

        if (pvCurrent.GetVisited())    // Cycle exists, visited list
        {
        	vTarget = pvCurrent.GetPrevVertex();
            while (vTarget.GetPrevVertex() != null && vTarget != pvCurrent)
            {
                plstVisited.add(0, vTarget);	// Insert vertex to beginning of the list
                vTarget = vTarget.GetPrevVertex();
            }
            plstVisited.add(0, pvCurrent);	// This is the first vertex in the cycle
            return true;
        }
        else
        {
            pvCurrent.SetVisited(true);
            
            // Forward Edges
            for (Edge eOutgoing : pvCurrent.GetOutEdge())
            {
                if (!eOutgoing.GetVisited() && !eOutgoing.GetIndependent())
                {
                    eOutgoing.SetVisited(true);
                    vTarget = eOutgoing.GetTarget();
                    vTarget.SetPrevVertex(pvCurrent);
                    if (FindCycle(vTarget, plstVisited))
                    	return true;
                }
            }

            // Backward Edges
            for (Edge eIncoming : pvCurrent.GetInEdge())
            {
                if (!eIncoming.GetVisited() && !eIncoming.GetIndependent())
                {
                	eIncoming.SetVisited(true);
                    vTarget = eIncoming.GetSource();
                    vTarget.SetPrevVertex(pvCurrent);
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
			v.SetVisited(false);
		}
		for (Edge e : lstEdge)
		{
			if (pResetAll)
				e.SetIndependent(false);
			e.SetVisited(false);
		}
	}
	
	// Just some funky code that set the curve of edges when their endpoints are identical so they don't overlap
	// Need a better idea to do this
	public void CurveEdges()
	{
		Vertex vTarget;
		boolean blnForwardCycle, blnBackwardCycle;
		ResetGraph(false);
		for (Vertex v : lstVertex)
		{
			for (Edge e : v.GetOutEdge())
			{
				blnForwardCycle = false;
				blnBackwardCycle = false;
				int angle=0;		
				e.SetVisited(true);
				vTarget = e.GetTarget();
				for (Edge e2 : v.GetOutEdge())
				{
					if (!e2.GetVisited() && e2.GetTarget() == vTarget)
					{
						e.SetVisited(true);
						blnForwardCycle = true;
						e.SetCurve(1);
						e2.SetCurve((int) Math.pow(-1, ++angle) * angle);
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
			e.SetVisited(false);
		}
	}
	
	public void PrintGraph(boolean pVertex, boolean pEdge)
	{
		if (pVertex)
		{
			for (Vertex v : lstVertex)
			{
				System.out.println("Vertex: " + v.GetLabel() + " " + v.GetType());
			}
		}
		if (pEdge)
		{
			for (Edge e : lstEdge)
			{
				System.out.println("Edge " + e.GetLabel() + " : " + e.GetSource().GetLabel() + " -> " + e.GetTarget().GetLabel());
			}
		}
	}
}
