package graph;
import static graph.Constant.*;

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
	
	private String equation = "";
	private String value = "";
	                                
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
		if(pTarget.GetLabel().equals("EXIT") || pTarget.GetLabel().equals("START")) timecost="";
		else if(pSource.GetLabel().equals("START")) timecost="C"+pTarget.GetLabel();
    	// use line# to determine the order of instruction
		else if(Integer.parseInt(pTarget.GetLabel())<Integer.parseInt(pSource.GetLabel())) timecost="";
		else timecost="C"+pTarget.GetLabel();
	}
  
    public String GetCondition(){
    	return condition;
    }
    public void SetCondition(String condition){
    	this.condition=condition;
    }
	public String GetLooptype(){
		return looptype;
	}
	public void SetLooptype(String looptype){
		this.looptype=looptype;
	}
	public String GetTimecost(){
		return timecost;
	}
	
	public void SetTimecost(String timecost){
		this.timecost=timecost;
	}

	public boolean GetVisible()
	{
		return visible;
	}
	public void SetVisible(boolean pVisible)
	{
		visible = pVisible;
	}
	public int GetCurve()
	{
		return curve;
	}
	public void SetCurve(int pCurve)
	{
		curve = pCurve;
	}
	public Color GetColor()
	{
		return color;
	}
	public void SetColor(Color pColor)
	{
		color = pColor;
	}
	public String GetValue()
	{
		return value;
	}
	public void SetValue(String pValue)
	{
		value = pValue;
	}
	public String GetEquation()
	{
		return equation;
	}
	public void SetEquation(String pEquation)
	{
		equation = pEquation;
	}
	public String GetLabel()
	{
		return label;
	}
	public void SetLabel(String pLabel)
	{
		label = pLabel;
	}
	public boolean GetIndependent()
	{
		return independent;
	}
	public void SetIndependent(boolean pIndependent)
	{
		independent = pIndependent;
		if (pIndependent)
			color = INDCOLOR;
		else
			color = EDGECOLOR;
	}
	public boolean GetVisited()
	{
		return visited;
	}
	public void SetVisited(boolean pVisited)
	{
		visited = pVisited;
	}
	
	public Vertex GetSource()
	{
		return source;
	}
	
	public Vertex GetTarget()
	{
		return target;
	}
}
