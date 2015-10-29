package graph;

import graph.Edge;
import graph.Graph;
import graph.Vertex;
import static graph.GraphConstants.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;


public class GraphParser
{
	HashMap<String, String> mVariable = new HashMap<String, String>();
	
	private LinkedList<Token> Tokenize(String s) throws Exception
	{
		char c;
		boolean bOperator = false;
		boolean bArrayOpen = false;
		int iParenOpen = 0;
		String sToken = "";
		LinkedList<Token> lstToken = new LinkedList<Token>();
		TokenType tokenType;
		Token tToken;

		// # is comment
		if (s.length() > 0 && s.charAt(0) == '#')
			return lstToken;
		
		for (int i = 0; i <= s.length(); i++)
		{
			if (i < s.length())
			{
				c = s.charAt(i);
				bOperator = IsOperator(c);
			}
			else c = ' ';
			
			if ((c == ' ' || bOperator) && !(bArrayOpen || iParenOpen > 0))
			{
				if (sToken.length() > 0)
				{
					if (IsKeyword(sToken))
					{
						tokenType = TokenType.KEYWORD;
					}
					else	// Token is not a keyword
					{
						if (IsNumeric(sToken))
						{
							tokenType = TokenType.CONSTANT;
						}
						else if (sToken.contains("[") || sToken.contains("]") || sToken.contains("(") || sToken.contains(")"))
						{
							tokenType = TokenType.EXPR;	// function call, array, parenthesized group of operator operands ...etc
						}
						else
						{
							tokenType = TokenType.VARIABLE;
						}
					}
					tToken = new Token(tokenType, sToken);
					lstToken.add(tToken);
					sToken = "";
				}
				if (bOperator)
				{
					// If first token of line is a variable or FOR then this equal sign must be an assignment, not equal.
					if (c == '=' && (lstToken.peekFirst().type == TokenType.VARIABLE || lstToken.peekFirst().value.equals(Keyword.FOR.Value())))
						lstToken.add(new Token(TokenType.ASSIGN, Character.toString(c)));
					else lstToken.add(new Token(TokenType.OPERATOR, Character.toString(c)));
				}
			}
			else if (i < s.length())
			{
				if (c == '#')
					throw new Exception("Invalid character #");
				if (c == '[')
					bArrayOpen = true;
				else if (c == ']')
				{
					if (bArrayOpen)
						bArrayOpen = false;
					else throw new Exception("Syntax Error");
				}
				else if (c == '(')
					iParenOpen++;
				else if (c == ')')
				{
					iParenOpen--;
					if (iParenOpen < 0)
						throw new Exception("Syntax Error");
				}
				sToken += c;
			}
		}
		if (bArrayOpen || iParenOpen > 0)
			throw new Exception("Syntax Error, unclosed bracket/parenthesis.");
		
		return lstToken;
	}
	
	// Very limited syntax checking
	// Returns graph
	public Graph Parse(File codeFile)
	{
		NodeType nodeType = null;
		FlowType flowType = null;
		String sAssignVar, sAssignVal;
		String sUpperBound, sLowerBound;
		String sExpr, sLine;

		Graph gGraph = new Graph();
		Vertex vNewVertex = null;
		Vertex vLastVertex = null;
		Edge eNewEdge = null;
		
		// Lists used for syntax checking
		LinkedList<Keyword> lstKeyword = new LinkedList<Keyword>();			// stack
		LinkedList<Vertex> lstBranchVertex = new LinkedList<Vertex>();		// stack
		LinkedList<Keyword> lstLineKeyword = new LinkedList<Keyword>();		// stack
		LinkedList<TokenType> lstExpectedToken = new LinkedList<TokenType>();
		LinkedList<Token> lstToken = new LinkedList<Token>();

		int iLine = 0;
		int iEdge = 0;
		int x = 100;
		int y = 100;
		int i;
		
		BufferedReader br = null;
		
		try
		{
			System.out.println("*** BEGIN PARSING ***");
			vLastVertex = new Vertex("START", "START", NodeType.START.toString(), x, y);
			gGraph.AddVertex(vLastVertex);

			br = new BufferedReader(new FileReader(codeFile));
			while((sLine = br.readLine()) != null)
			{
				sExpr = "";
				sAssignVar = "";
				sAssignVal = "";
				sUpperBound = "";
				sLowerBound = "";
				flowType = getFlowType(lstKeyword);
				nodeType = NodeType.INSTRUCTION;
				lstLineKeyword.clear();
				lstExpectedToken.clear();
				lstExpectedToken.add(TokenType.EXPR);
				lstExpectedToken.add(TokenType.VARIABLE);
				lstExpectedToken.add(TokenType.KEYWORD);
				lstExpectedToken.add(TokenType.NULL);
				
				i = 0;
				lstToken = Tokenize(sLine.trim());
				for (Token token : lstToken)
				{
					if (lstExpectedToken.isEmpty())
					{
						throw new Exception("[Line" + iLine + "] End of statement expected.");
					}
					
					if (token.type == TokenType.KEYWORD)
					{
						if (token.value.equals(Keyword.IF.Value()))
						{
							if (i != 0)
								throw new Exception("[Line" + iLine + "] IF keyword is required to be first in the line.");
							nodeType = NodeType.IF_BRANCH;
							lstExpectedToken.clear();
							lstExpectedToken.add(TokenType.VARIABLE);
							lstLineKeyword.add(Keyword.IF);
							lstKeyword.add(Keyword.IF);
						}
						else if (token.value.equals(Keyword.FOR.Value()))
						{
							if (i != 0)
								throw new Exception("[Line" + iLine + "] FOR keyword is required to be first in the line.");
							nodeType = NodeType.FOR_BRANCH;
							lstExpectedToken.clear();
							lstExpectedToken.add(TokenType.VARIABLE);
							lstLineKeyword.add(Keyword.FOR);
							lstKeyword.add(Keyword.FOR);
						}
						else if (token.value.equals(Keyword.WHILE.Value()))
						{
							if (i != 0)
								throw new Exception("[Line" + iLine + "] WHILE keyword is required to be first in the line.");
							nodeType = NodeType.WHILE_BRANCH;
							lstExpectedToken.clear();
							lstExpectedToken.add(TokenType.VARIABLE);
							lstLineKeyword.add(Keyword.WHILE);
							lstKeyword.add(Keyword.WHILE);
						}
						else if (token.value.equals(Keyword.ENDIF.Value()))
						{
							if (lstKeyword.peekLast() != Keyword.IF)
								throw new Exception("ENDIF without IF.");
							nodeType = NodeType.BRANCH_MERGE;
							lstExpectedToken.clear();
						}
						else if (token.value.equals(Keyword.ENDFOR.Value()))
						{
							if (lstKeyword.peekLast() != Keyword.FOR)
								throw new Exception("[Line" + iLine + "] ENDFOR without FOR.");
							nodeType = NodeType.LOOP_MERGE;
							lstExpectedToken.clear();
						}
						else if (token.value.equals(Keyword.ENDWHILE.Value()))
						{
							if (lstKeyword.peekLast() != Keyword.WHILE)
								throw new Exception("[Line" + iLine + "] ENDWHILE without WHILE.");
							nodeType = NodeType.LOOP_MERGE;
							lstExpectedToken.clear();
						}
						else if (token.value.equals(Keyword.TO.Value()))
						{
							if (lstKeyword.peekFirst() != Keyword.FOR)
								throw new Exception("[Line" + iLine + "] Keyword \"TO\" is to be used with the \"FOR\" keyword.");			
							if (!lstExpectedToken.contains(token.type))
								throw new Exception("[Line" + iLine + "] Syntax error.");
							lstExpectedToken.clear();
							lstExpectedToken.add(TokenType.CONSTANT);
							lstExpectedToken.add(TokenType.VARIABLE);
							lstExpectedToken.add(TokenType.EXPR);
							lstLineKeyword.add(Keyword.TO);
							mVariable.put(sAssignVar, sAssignVal.trim());
						}
					}
					else	// Token is not a keyword
					{
						if (token.type == TokenType.OPERATOR)
						{	// Operator, expects variable, constant, expr next
							if (!lstExpectedToken.contains(token.type))
								throw new Exception ("[Line" + iLine + "] " + SerializeList(lstExpectedToken, " or ") + " expected.");
							if (lstLineKeyword.peekLast() == Keyword.TO)
								sUpperBound += token.value;
							else if (lstLineKeyword.peekLast() == Keyword.ASSIGN)
							{
								sAssignVal += token.value + " ";
								if (lstLineKeyword.peekFirst() == Keyword.FOR)
									sLowerBound += token.value;
							}
							lstExpectedToken.clear();
							lstExpectedToken.add(TokenType.VARIABLE);
							lstExpectedToken.add(TokenType.CONSTANT);
							lstExpectedToken.add(TokenType.EXPR);
						}
						else if (token.type == TokenType.CONSTANT || token.type == TokenType.EXPR)
						{	// Constant, expects nothing or operator next
							if (!lstExpectedToken.contains(token.type))
								throw new Exception ("[Line" + iLine + "] " + SerializeList(lstExpectedToken, " or ") + " expected.");
							lstExpectedToken.clear();
							if (lstLineKeyword.peekLast() == Keyword.TO)
								sUpperBound += token.value;
							else if (lstLineKeyword.peekLast() == Keyword.ASSIGN)
							{
								sAssignVal += token.value + " ";
								if (lstLineKeyword.peekFirst() == Keyword.FOR)
									sLowerBound += token.value;
							}

							if (lstLineKeyword.peekFirst() == Keyword.FOR)
								lstExpectedToken.add(TokenType.KEYWORD);	// Expects TO
							lstExpectedToken.add(TokenType.NULL);
							lstExpectedToken.add(TokenType.OPERATOR);
						}
						else if (token.type == TokenType.ASSIGN)
						{
							if (!lstExpectedToken.contains(token.type))
								throw new Exception ("[Line" + iLine + "] " + SerializeList(lstExpectedToken, " or ") + " expected.");
							lstExpectedToken.clear();
							lstExpectedToken.add(TokenType.CONSTANT);
							lstExpectedToken.add(TokenType.VARIABLE);
							lstExpectedToken.add(TokenType.EXPR);
							lstLineKeyword.add(Keyword.ASSIGN);
						}
						else if (token.type == TokenType.VARIABLE || token.type == TokenType.EXPR)
						{	// Variable, expects nothing or operator next
							if (!lstExpectedToken.contains(token.type))
								throw new Exception ("[Line" + iLine + "] " + SerializeList(lstExpectedToken, " or ") + " expected.");
							lstExpectedToken.clear();
							
							if (i == 0)
							{	// This is variable assignment if a variable appears first in line
								lstExpectedToken.add(TokenType.ASSIGN);
								sAssignVar = token.value;
							}
							else
							{
								if (!mVariable.containsKey(token.value) && lstLineKeyword.peekLast() != Keyword.FOR)	// Loop variable does not need to be assigned before the loop
									throw new Exception("[Line" + iLine + "] Unassigned variable " + token.value);
								if (lstLineKeyword.peekLast() == Keyword.TO)
									sUpperBound += token.value;
								else if (lstLineKeyword.peekLast() == Keyword.ASSIGN)
								{
									sAssignVal += token.value + " ";
									if (lstLineKeyword.peekFirst() == Keyword.FOR)
										sLowerBound += token.value;
								}
								if (lstLineKeyword.peekLast() == Keyword.FOR)
								{
									sAssignVar = token.value;
									lstExpectedToken.add(TokenType.ASSIGN);	// Expects assignment
								}
								lstExpectedToken.add(TokenType.NULL);
								lstExpectedToken.add(TokenType.OPERATOR);
							}
						}
					}

					sExpr += token.value + " ";
					i++;
				}
				
				if (!lstExpectedToken.isEmpty() && !lstExpectedToken.contains(TokenType.NULL))
					throw new Exception ("[Line" + iLine + "] " + SerializeList(lstExpectedToken, " or ") + " expected.");
				lstExpectedToken.clear();
				
				if (lstLineKeyword.peekLast() == Keyword.ASSIGN)
					mVariable.put(sAssignVar, sAssignVal.trim());
				
				// Add node
				if (sExpr.trim().length() > 0)
				{
					if (nodeType == NodeType.BRANCH_MERGE)
					{
						// Drawings for debug
						x += VDISTANCE / 2;
						y = lstBranchVertex.peekLast().getY();
						
						vNewVertex = new Vertex(String.valueOf(iLine), String.valueOf(iLine), nodeType.toString(), x, y);
						vNewVertex.setExpr(sExpr);
						gGraph.AddVertex(vNewVertex);
						
						eNewEdge = new Edge(vLastVertex, vNewVertex, "e" + iEdge);
						eNewEdge.setFlowType(flowType);
						gGraph.AddEdge(eNewEdge);	// Last vertex to merge vertex
						eNewEdge = new Edge(lstBranchVertex.removeLast(), vNewVertex, "e" + (iEdge + 1));
						eNewEdge.setFlowType(flowType);
						gGraph.AddEdge(eNewEdge);	// If branch vertex to merge vertex
						
						lstKeyword.removeLast();
						iEdge += 2;
					}
					else if (nodeType == NodeType.LOOP_MERGE)
					{
						vNewVertex = lstBranchVertex.peekLast();		// Back to where the loop starts
						eNewEdge = new Edge(vLastVertex, lstBranchVertex.removeLast(), "e" + iEdge);
						eNewEdge.setFlowType(flowType);
						gGraph.AddEdge(eNewEdge);
						lstKeyword.removeLast();
						iEdge++;
					}
					else 
					{
						// Drawings for debugging, not used for actual project deliverable
						if (vLastVertex.getType().equals(NodeType.IF_BRANCH.toString()))
						{
							x = vLastVertex.getX() + VDISTANCE / 2;
							y = vLastVertex.getY() + VDISTANCE / 2;
						}
						else if ((vLastVertex.getType().equals(NodeType.FOR_BRANCH.toString()) || vLastVertex.getType().equals(NodeType.WHILE_BRANCH.toString()))	// This needs to be simplified
								&& lstBranchVertex.size() > 0 && (lstBranchVertex.peekLast().getType().equals(NodeType.FOR_BRANCH.toString()) || 					// Basically if the last vertex is a loop, then the new vertex is within a loop, place the new vertex under the loop
																  lstBranchVertex.peekLast().getType().equals(NodeType.WHILE_BRANCH.toString())))					// If the last vertex is a loop but has already exited, don't place it under
							y = vLastVertex.getY() + VDISTANCE;
						else 
						{
							x = vLastVertex.getX() + VDISTANCE;
							y = vLastVertex.getY();
						}
						// END Drawing
						
						vNewVertex = new Vertex(String.valueOf(iLine), String.valueOf(iLine), nodeType.toString(), x, y);
						vNewVertex.setExpr(sExpr);
						if (nodeType == NodeType.IF_BRANCH || nodeType == NodeType.WHILE_BRANCH)
						{
							lstBranchVertex.add(vNewVertex);
						}
						else if (nodeType == NodeType.FOR_BRANCH)
						{
							vNewVertex.setLowerBound(EvalExpr(sLowerBound));
							vNewVertex.setUpperBound(EvalExpr(sUpperBound));
							lstBranchVertex.add(vNewVertex);
						}
						
						eNewEdge = new Edge(vLastVertex, vNewVertex, "e" + iEdge);
						eNewEdge.setFlowType(flowType);
						gGraph.AddVertex(vNewVertex);
						gGraph.AddEdge(eNewEdge);
						iEdge++;
					}
					vLastVertex = vNewVertex;
					System.out.println(PadRight(vNewVertex.getLabel() + ")", ' ', 4) + PadRight(sExpr.trim(), ' ', 30) + "[" + nodeType.toString() + "] " + ((nodeType == NodeType.FOR_BRANCH) ? (" For [" + vNewVertex.getLowerBound() + ", " + vNewVertex.getUpperBound() + "]") : ""));
				}
				iLine++;
			}
			br.close();
			
			if (lstKeyword.size() > 0)
			{
				sLine = "Unterminated ";
				for (Keyword key : lstKeyword)
				{
					sLine += key.toString() + " ";
				}
				throw new Exception(sLine.trim());
			}
			
			vNewVertex = new Vertex("EXIT", "EXIT", NodeType.END.toString(), vLastVertex.getX() + VDISTANCE, vLastVertex.getY());
			gGraph.AddVertex(vNewVertex);
			gGraph.AddEdge(new Edge(vLastVertex, vNewVertex, "e" + iEdge));
			System.out.println("*** END PARSING ***");
			
			// DEBUG PRINT
			PrintVariables(mVariable);
			for (Edge e : gGraph.getEdgeList())
			{
				System.out.println(e.getLabel() + " " + (e.getFlowType() != null ? e.getFlowType().toString() : ""));
			}
			System.out.println();
		}
		catch (Exception ex)
		{
			// Should we catch exception here?
			System.out.println(ex.getMessage());
			return null;
		}
		finally
		{
			try { br.close(); } 
			catch (IOException ex) {} // Nothing meaningful from this exception 
		}
		
		return gGraph;
	}
	
	// Supports add, subtract, multiply, divide, power, n choose k
	// x = x + 1 -> infinite recursion
	// x = 1, y = x, x = y -> same, I don't know how to fix this, may be we can store line # of assignment to back track variable values
	public int EvalExpr(String expr) throws Exception
	{
		int iNum = 0;
		int iOperatorIndex;
		int[] aOperatorIndex;
		boolean bIsInt = true;
		
		// Remove meaningless parenthesis/spaces
		expr = RemoveParenthesis(expr);
		
		// If expr is an int, return it
		try
		{
			iNum = Integer.parseInt(expr);
		}
		catch (Exception e)
		{
			bIsInt = false;
		}
		if (!bIsInt)	// Can probably put this entire block inside the catch block...
		{
			if (mVariable.containsKey(expr))			// Check variable table for assignment
				return EvalExpr(mVariable.get(expr));	// Very probable infinite recursion

			// Single pass on expr
			aOperatorIndex = FindOperatorIndices(expr);
			
			// To preserve order of arithmetic operation, split +,- before *,/.
			if (aOperatorIndex[ADD] > -1)
			{
				iNum = EvalExpr(expr.substring(0, aOperatorIndex[ADD])) + EvalExpr(expr.substring(aOperatorIndex[ADD] + 1, expr.length()));
			}
			else if (aOperatorIndex[SUB] > -1)
			{
				iNum = EvalExpr(expr.substring(0, aOperatorIndex[SUB])) - EvalExpr(expr.substring(aOperatorIndex[SUB] + 1, expr.length()));
			}
			else if (aOperatorIndex[MUL] > -1)
			{
				iNum = EvalExpr(expr.substring(0, aOperatorIndex[MUL])) * EvalExpr(expr.substring(aOperatorIndex[MUL] + 1, expr.length()));
			}
			else if (aOperatorIndex[DIV] > -1)
			{
				iNum = EvalExpr(expr.substring(0, aOperatorIndex[DIV])) / EvalExpr(expr.substring(aOperatorIndex[DIV] + 1, expr.length()));
			}
			else if (aOperatorIndex[POW] > -1)
			{
				iNum = (int) Math.pow(EvalExpr(expr.substring(0, aOperatorIndex[POW])), EvalExpr(expr.substring(aOperatorIndex[POW] + 1, expr.length())));
			}
			else
			{
				// This block is for other math operations
				iOperatorIndex = expr.indexOf(',');
				if (expr.startsWith("C(") && expr.endsWith(")") && iOperatorIndex > -1)
				{
					iNum = C(EvalExpr(expr.substring(2, iOperatorIndex)), EvalExpr(expr.substring(iOperatorIndex + 1, expr.length() - 1)));
				}
			}
		}
		
		return iNum;
	}
	
	public String RemoveParenthesis(String s)
	{
		char c;
		int iOpenParen = 0;
		String sReturn = s.trim();
		String sTemp = sReturn;
		
		while (sTemp.startsWith("(") && sTemp.endsWith(")"))
		{
			sTemp = sTemp.substring(1, sTemp.length() - 1).trim();
			for (int i = 0; i < sTemp.length(); i++)
			{
				c = sTemp.charAt(i);
				if (c == '(')
					iOpenParen++;
				else if (c == ')')
					iOpenParen--;
				if (iOpenParen < 0)
					break;
			}
			if (iOpenParen != 0)
				break;
			else sReturn = sTemp;
		}
		return sReturn;
	}
	
	private int[] FindOperatorIndices(String s)
	{
		int iOpenParen = 0;
		int[] aOperatorIndex = {-1, -1, -1, -1, -1};
		char c;
		
		for (int i = 0; i < s.length(); i++)
		{
			c = s.charAt(i);
			if (c == '(')
				iOpenParen++;
			else if (c == ')')
				iOpenParen--;
			else if (iOpenParen == 0)
			{
				switch (c)
				{
					case '+':
						aOperatorIndex[ADD] = i;
						break;
					case '-':
						aOperatorIndex[SUB] = i;
						break;
					case '*':
						aOperatorIndex[MUL] = i;
						break;
					case '/':
						aOperatorIndex[DIV] = i;
						break;
					case '^':
						aOperatorIndex[POW] = i;
						break;
				}
			}
		}
		return aOperatorIndex;
	}

	// This gets the type of flow of a certain edge
	// Only a limited set of combination is allowed
	private FlowType getFlowType(LinkedList<Keyword> lstKW)	
	{
		FlowType et = null;
		Keyword kw;
		for (int i = lstKW.size() - 1; i >= 0; i--)
		{
			kw = lstKW.get(i);
			if (et == null)
			{
				if (kw == Keyword.IF)	// The edge leading to if branch does not count as inside the if
					et = FlowType.IF;
				else if (kw == Keyword.FOR)
					et = FlowType.FOR;
				else if (kw == Keyword.WHILE)
					et = FlowType.WHILE;
			}
			else	// Outer
			{
				if (kw == Keyword.FOR && et == FlowType.IF)
					et = FlowType.IF_INSIDE_FOR;
				else if (kw == Keyword.WHILE && et == FlowType.IF)
					et = FlowType.IF_INSIDE_WHILE;
			}
		}
		return et;
	}
	
	// None of the following functions belongs here, should have some common function class that holds these
	private String PadRight(String s, char c, int length)
	{
		int sl = s.length();
		for (int i = 0; i < length - sl; i++)
		{
			s += c;
		}
		return s;
	}
	
	private boolean IsNumeric(String s)
	{
		boolean bReturn = true;
		try
		{
			Double.parseDouble(s);
		}
		catch (Exception e)
		{
			bReturn = false;
		}
		return bReturn;
	}
	
	// We are only allowing single character operator to simplify parsing
	private boolean IsOperator(char c)
	{
		if (c == OperatorType.ADD.Char() || c == OperatorType.SUB.Char() ||
			c == OperatorType.DIV.Char() || c == OperatorType.MUL.Char() ||
			c == OperatorType.POW.Char() || c == OperatorType.EQN.Char() || c == OperatorType.NEQ.Char())
			return true;
		
		return false;
	}
	
	private boolean IsKeyword(String sToken)
	{
		return (sToken.equals(Keyword.IF.Value()) ||
				sToken.equals(Keyword.FOR.Value()) ||
				sToken.equals(Keyword.WHILE.Value()) ||
				sToken.equals(Keyword.ENDIF.Value()) ||
				sToken.equals(Keyword.ENDFOR.Value()) ||
				sToken.equals(Keyword.ENDWHILE.Value()) ||
				sToken.equals(Keyword.TO.Value()));			
	}

	private String SerializeList(LinkedList<TokenType> l, String sep)
	{
		StringBuffer sb = new StringBuffer();
		for (TokenType t : l)
		{
			if (sb.length() == 0)
				sb.append(t.toString());
			else sb.append(sep).append(t.toString());
		}
		return sb.toString().trim();
	}
	
	public int C(int n, int k) throws Exception
	{
		int iFactN = 1;
		int iFactK = 1;
		int iFactNK = 1;
		
		if (n >= k)
		{
			for (int i = 2; i <= n; i++)
			{
				iFactN *= i;
				if (i == k)
					iFactK = iFactN;
				if (i == n - k)
					iFactNK = iFactN;
			}
		}
		else throw new Exception("n < k");
		
		return iFactN / (iFactK * iFactNK);
	}
	
	// DEBUG PRINT
	private void PrintVariables(HashMap<String, String> mVar)
	{
		System.out.println("\n*** VARIABLES ***");
		for (String key : mVar.keySet())
		{
			System.out.println("[" + key + ", " + mVar.get(key) + "]");
		}
		System.out.println("");
	}
	
	private class Token
	{
		private TokenType type;
		private String value = "";
		
		public Token(TokenType tt, String val)
		{
			type = tt;
			value = val;
		}
	}
}
