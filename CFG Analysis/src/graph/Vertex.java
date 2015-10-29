package graph;

import java.awt.Color;
import java.util.ArrayList;
import static graph.GraphConstants.*;

public class Vertex 
{
	private String name;
	private String label;
	private String type;
	private String looptype=null;
	private boolean visited;
	
	private int x;
	private int y;
	private Color color = VERTEXCOLOR;
	private boolean visible = true;
	
	private ArrayList<Edge> lstOutEdge = new ArrayList<Edge>();
	private ArrayList<Edge> lstInEdge = new ArrayList<Edge>();
	private Vertex prevVertex = null;
	
	public Vertex(String pName)
	{
		name = pName;
	}
	public Vertex(String pName, String pLabel, String pType, int pX, int pY)
	{
		name = pName;
		label = pLabel;
		type = pType;
		x = pX;
		y = pY;
	}
	
	public String GetLooptype(){
		return looptype;
	}

	public void SetLooptype(String looptype){
		this.looptype=looptype;
	}
	public void SetType(String type){
		this.type=type;
	}
	public String GetType()
	{
		return type;	// type of vertex (start, end, decision, instruction)
	}
	public String GetName()
	{
		return name;
	}
	public String GetLabel()
	{
		return label;
	}
	public boolean GetVisited()
	{
		return visited;
	}
	public void SetVisited(boolean pVisited)
	{
		visited = pVisited;
	}
	public Vertex GetPrevVertex()
	{
		return prevVertex;
	}
	public void SetPrevVertex(Vertex pPrevVertex)
	{
		prevVertex = pPrevVertex;
	}
	public ArrayList<Edge> GetInEdge()
	{
		return lstInEdge;
	}
	public ArrayList<Edge> GetOutEdge()
	{
		return lstOutEdge;
	}
	public ArrayList<Edge> GetEdge()
	{
		ArrayList<Edge> lstReturn = new ArrayList<Edge>();
		lstReturn.addAll(lstOutEdge);
		lstReturn.addAll(lstInEdge);
		return lstReturn;
	}
	
	// Drawings (should probably put this in a parent class or private class)
	public Color GetColor()
	{
		return color;
	}
	public int GetX()
	{
		return x;
	}
	public int GetY()
	{
		return y;
	}
	public boolean GetVisible()
	{
		return visible;
	}
	public void SetVisible(boolean pVisible)
	{
		visible = pVisible;
	}
	// END Drawings
	
	public Edge FindOutEdge(Vertex pTarget, boolean pIndependent)
	{
		for (Edge e : lstOutEdge)
		{
			if (e.getTarget() == pTarget && e.getIndependent() == pIndependent)
			{
				return e;
			}
		}
		return null;
	}
	public Edge FindInEdge(Vertex pSource, boolean pIndependent)
	{
		for (Edge e : lstInEdge)
		{
			if (e.getSource() == pSource && e.getIndependent() == pIndependent)
			{
				return e;
			}
		}
		return null;
	}
	public Edge FindEdge(Vertex pVertex, boolean pIndependent)
	{
		for (Edge e : lstOutEdge)
		{
			if (e.getTarget() == pVertex && e.getIndependent() == pIndependent)
			{
				return e;
			}
		}
		for (Edge e : lstInEdge)
		{
			if (e.getSource() == pVertex && e.getIndependent() == pIndependent)
			{
				return e;
			}
		}
		return null;
	}
	public Edge FindDiffIndepEdge(Vertex pVertex)
	{
		for (Edge e : lstOutEdge)
			if (e.getTarget() == pVertex && e.getIndependent() == true && e.getVisited() ==false)
			{e.setVisited(true); return e;}
		for (Edge e : lstInEdge)
			if (e.getSource() == pVertex && e.getIndependent() == true && e.getVisited() ==false)
			{e.setVisited(true); return e;}
		return null;
	}
	
	public Vertex copyVertex(){
		return new Vertex(name, label, type, x, y);
	}
}
