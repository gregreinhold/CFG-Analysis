package graph;

import java.awt.Color;
import java.util.ArrayList;
import static graph.GraphConstants.*;

public class Vertex 
{
	private String name;
	private String label;
	private String type;
	private String expr;
	private int forUpperBound;
	private int forLowerBound;
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

	
	public int getUpperBound()
	{
		return forUpperBound;
	}
	public void setUpperBound(int value)
	{
		forUpperBound = value;
	}
	public int getLowerBound()
	{
		return forLowerBound;
	}
	public void setLowerBound(int value)
	{
		forLowerBound = value;
	}
	public String getExpr()
	{
		return expr;
	}
	public void setExpr(String s)
	{
		expr = s;
	}
	public String getLooptype(){
		return looptype;
	}
	public void setLooptype(String looptype){
		this.looptype=looptype;
	}
	public void setType(String type){
		this.type=type;
	}
	public String getType()
	{
		return type;	// type of vertex (start, end, decision, instruction)
	}
	public String getName()
	{
		return name;
	}
	public String getLabel()
	{
		return label;
	}
	public boolean getVisited()
	{
		return visited;
	}
	public void setVisited(boolean pVisited)
	{
		visited = pVisited;
	}
	public Vertex getPrevVertex()
	{
		return prevVertex;
	}
	public void setPrevVertex(Vertex pPrevVertex)
	{
		prevVertex = pPrevVertex;
	}
	public ArrayList<Edge> getInEdge()
	{
		return lstInEdge;
	}
	public ArrayList<Edge> getOutEdge()
	{
		return lstOutEdge;
	}
	public ArrayList<Edge> getEdge()
	{
		ArrayList<Edge> lstReturn = new ArrayList<Edge>();
		lstReturn.addAll(lstOutEdge);
		lstReturn.addAll(lstInEdge);
		return lstReturn;
	}
	
	// Drawings (should probably put this in a parent class or private class)
	public Color getColor()
	{
		return color;
	}
	public int getX()
	{
		return x;
	}
	public int getY()
	{
		return y;
	}
	public boolean getVisible()
	{
		return visible;
	}
	public void setVisible(boolean pVisible)
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
