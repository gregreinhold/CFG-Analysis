package graph;

import java.awt.Color;

public final class GraphConstants {
	public static final int VERTEXSIZE = 10;
	public static final Color INDCOLOR = Color.blue;
	public static final Color EDGECOLOR = Color.black;
	public static final Color VERTEXCOLOR = Color.red;
	public static final int VDISTANCE = 80;

	public static final int ADD = 0;
	public static final int SUB = 1;
	public static final int MUL = 2;
	public static final int DIV = 3;
	public static final int POW = 4;
	
	public enum TokenType
	{
		OPERATOR,
		VARIABLE,
		CONSTANT,
		KEYWORD,
		EXPR,
		ASSIGN,
		NULL
	}

	public enum NodeType
	{
		START,
		END,
		INSTRUCTION,
		FOR_BRANCH,
		WHILE_BRANCH,
		IF_BRANCH,
		BRANCH_MERGE,
		LOOP_MERGE,
		ELSE
	}
	
	public enum Keyword
	{
		IF ("if"),
		FOR ("for"),
		WHILE ("while"),
		ENDIF ("endif"),
		ENDFOR ("endfor"),
		ENDWHILE ("endwhile"),
		ELSE ("else"),
		ASSIGN ("="),	// Assign is not a keyword but put it here for convenience
		TO ("to");
		
		private final String sKeyword;
		private Keyword(String s)
		{
			sKeyword = s;
		}
		public String Value()
		{
			return sKeyword;
		}
	}
	
	public enum FlowType
	{
		IF_INSIDE_FOR_TRUE,
		IF_INSIDE_FOR_FALSE,
		IF_INSIDE_WHILE_TRUE,
		IF_INSIDE_WHILE_FALSE,
		FOR,
		WHILE,
		IF,
		ELSE
	}
	
	public enum OperatorType
	{
		ADD ('+'),
		SUB ('-'),
		DIV ('/'),
		MUL ('*'),
		POW ('^'),
		EQN ('='),
		NEQ ('!');
		
		private final char OperatorChar;
		
		private OperatorType(char c)
		{
			OperatorChar = c;
		}
		
		public char Char()
		{
			return OperatorChar;
		}
	}
}
