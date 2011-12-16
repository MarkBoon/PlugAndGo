/**
 * Project: Tesuji Go Framework.<br>
 * <br>
 * <font color="#CC6600"><font size=-1> Copyright (c) 1985-2006 Mark Boon<br>
 * All rights reserved.<br>
 * <br>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * provided that the above copyright notice(s) and this permission notice appear
 * in all copies of the Software and that both the above copyright notice(s) and
 * this permission notice appear in supporting documentation.<br>
 * <br>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.<br>
 * <br>
 * <font color="#00000"><font size=+1>
 * 
 */
package tesuji.games.go.util;

import java.text.ParseException;

import tesuji.core.util.ArrayList;
import tesuji.core.util.StringBufferFactory;

import tesuji.games.general.BoardMark;
import tesuji.games.general.Move;
import tesuji.games.general.MoveStack;
import tesuji.games.general.TreeNode;

import tesuji.games.sgf.SGFData;
import tesuji.games.sgf.SGFParser;

import tesuji.games.go.common.GoMove;
import tesuji.games.go.common.GoMoveFactory;

import static tesuji.games.general.ColorConstant.*;

/**
 * Some utility routines
 */
public class SGFUtil
{
	public static String createSGF(IntStack moveList)
	{
		byte color = BLACK;
		
		StringBuffer output = StringBufferFactory.createStringBuffer();
		output.append("(");
		for (int i=0; i<moveList.getSize(); i++)
		{
			GoMove move = GoMoveFactory.getSingleton().createMove(moveList.get(i), color);
			output.append(";");
			output.append(move.toSGF());
			color = opposite(color);
			move.recycle();
		}
		output.append(")");
		
		String outputString = output.toString();
		StringBufferFactory.recycleStringBuffer(output);
		return outputString;
	}

	public static String createSGF(TreeNode<GoMove> moveNode)
	{
		int childCount = moveNode.getChildCount();
		boolean isVariation = (childCount>1 || moveNode.getParent()==null);
		
		// TODO - check root for setup of a position
		
		StringBuffer output = StringBufferFactory.createStringBuffer();
		for (int i=0; i<childCount; i++)
		{
			TreeNode<GoMove> childNode = moveNode.getChildAt(i);
			if (isVariation)
				output.append("(;");
			Move move = childNode.getContent();
			output.append(move.toSGF());
			if (!childNode.isLeaf())
				output.append(";");
			output.append(createSGF(childNode));
			if (isVariation)
				output.append(")");
		}
		String outputString = output.toString();
		StringBufferFactory.recycleStringBuffer(output);
		return outputString;		
	}

	public static MoveStack<GoMove> parseSGFPath(String sgfString)
		throws ParseException
	{
        SGFParser<GoMove> parser = new SGFParser<GoMove>(GoMoveFactory.getSingleton());
		parser.parse(sgfString);        
        TreeNode<SGFData<GoMove>> rootNode = parser.getDocumentNode();
        
        MoveStack<GoMove> moveList = new MoveStack<GoMove>();
        
        // The rootNode may contain a position to set up.
        GoMove setupMove = rootNode.getContent().getMove();
        if (setupMove!=null) // ?
        {
        	ArrayList<BoardMark> boardMarks = setupMove.getBoardMarks();
	        if (boardMarks!=null)
	        {
	        	for (BoardMark boardMark : boardMarks)
	        	{
	        		if (boardMark.getColor()==BLACK || boardMark.getColor()==WHITE)
	        		{
	        			int xy = GoArray.toXY(boardMark.getX(), boardMark.getY());
	        			GoMove move = GoMoveFactory.getSingleton().createMove(xy, boardMark.getColor());
	        			moveList.add(move);
	        		}
	        		// else - don't support other types
	        	}
	        }
	        }
        
        TreeNode<SGFData<GoMove>> node = rootNode;
        do
        {
        	node = node.getFirstChild(); // The first node never contains a move.
        	if (node!=null)
        	{
	        	GoMove move = node.getContent().getMove();
	        	if (move!=null)
        			moveList.add(GoMoveFactory.getSingleton().cloneMove(move));
        	}
        }
        while (!node.isLeaf());
        
        parser.getDocumentNode().recycle(); // Clean up the datastructure created by the parser.

        return moveList;
	}
}
