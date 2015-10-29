package graph;
import static graph.GraphConstants.*;

import java.awt.Color;

public class Edge
{
	private String label = "";
	private boolean visited;
	private boolean independent;
	private Vertex source = null;
	private Vertex target = null;
	
	private Color color = EDGECOLOR;
	private int curve = 0;
	private boolean visible = true;
	
	private String equation = ""; 	// set it as string for now
	private String cost = "";		// set it as string for now 
	                                // this is the value of e, not time cost
	private String timecost="";
	private String looptype=null;
	private String condition=null;
	
	public Edge(Vertex pSource, Vertex pTarget, String pLabel)
	{
		source = pSource;
		target = pTarget;
		label = pLabel;
		initTimecost(pSource,pTarget);
	}
	
    public void initTimecost(Vertex pSource, Vertex pTarget){
    	if(timecost.equals(""))
		if(pTarget.getLabel().equals("EXIT") || pTarget.getLabel().equals("START")) timecost="";
		else if(pSource.getLabel().equals("START")) timecost="C"+pTarget.getLabel();
    	// use line# to determine the order of instruction
		else if(Integer.parseInt(pTarget.getLabel())<Integer.parseInt(pSource.getLabel())) timecost="";
		else timecost="C"+pTarget.getLabel();
	}
  
    public String getCondition(){
    	return condition;
    }
    public void setCondition(String condition){
    	this.condition=condition;
    }
	public String getLoopType(){
		return looptype;
	}
	public void setLooptype(String looptype){
		this.looptype=looptype;
	}
	public String getTimecost(){
		return timecost;
	}
	
	public void setTimecost(String timecost){
		this.timecost=timecost;
	}

	public boolean getVisible()
	{
		return visible;
	}
	public void setVisible(boolean pVisible)
	{
		visible = pVisible;
	}
	public int getCurve()
	{
		return curve;
	}
	public void setCurve(int pCurve)
	{
		curve = pCurve;
	}
	public Color getColor()
	{
		return color;
	}
	public void setColor(Color pColor)
	{
		color = pColor;
	}
	public String getCost()
	{
		return cost;
	}
	public void setCost(String pCost)
	{
		cost = pCost;
	}
	public String getEquation()
	{
		return equation;
	}
	public void setEquation(String pEquation)
	{
		equation = pEquation;
	}
	public String getLabel()
	{
		return label;
	}
	public void setLabel(String pLabel)
	{
		label = pLabel;
	}
	public boolean getIndependent()
	{
		return independent;
	}
	public void setIndependent(boolean pIndependent)
	{
		independent = pIndependent;
		if (pIndependent)
			color = INDCOLOR;
		else
			color = EDGECOLOR;
	}
	public boolean getVisited()
	{
		return visited;
	}
	public void setVisited(boolean pVisited)
	{
		visited = pVisited;
	}
	
	public Vertex getSource()
	{
		return source;
	}
	
	public Vertex getTarget()
	{
		return target;
	}
}
