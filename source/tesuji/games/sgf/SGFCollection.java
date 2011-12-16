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
package tesuji.games.sgf;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;

import org.apache.log4j.Logger;

import tesuji.core.util.ArrayList;
import tesuji.core.util.ArrayStack;
import tesuji.games.general.BoardMark;
import tesuji.games.general.Move;
import tesuji.games.general.MoveFactory;
import tesuji.games.general.TreeNode;
import tesuji.games.sgf.SGFData;
import tesuji.games.sgf.SGFParser;

/**
 * This class reads an SGF file and treats its contents as a collection of games.
 * It implements the Iterator interface to enable iterating over the SGFData
 * objects that are created when parsing the file.
 */
public class SGFCollection<MoveType extends Move>
    implements Iterator<TreeNode<SGFData<MoveType>>>
{
    private static Logger logger = Logger.getLogger(SGFCollection.class);
    
    private FileReader fileReader;
    private BufferedReader reader;
    private String restString = "";
    private boolean abort = false;
    private MoveFactory<MoveType> _moveFactory;
    
    public SGFCollection(String fileName, MoveFactory<MoveType> moveFactory)
    {
    	_moveFactory = moveFactory;
        try
        {
            fileReader = new FileReader(fileName);
            reader = new BufferedReader(fileReader);
        }
        catch (IOException ex)
        {
            logger.error("IOException: "+ex.getMessage());
        }
    }
    
    public boolean hasNext()
    {
        if (abort)
            return false;
        try
        {
            return reader.ready();
        }
        catch (IOException ex)
        {
            logger.error("IOException: "+ex.getMessage());
            abort = true;
            return false;
        }
    }
    
    public TreeNode<SGFData<MoveType>> next()
    {
        StringBuffer fileContents = new StringBuffer(restString);
        
        try
        {
            while (reader.ready())
            {
                String line = reader.readLine();
                int closingBracketIndex = line.lastIndexOf(')');
                if (closingBracketIndex>=0 && line.length()>closingBracketIndex)
                {
                    String closingString = line.substring(0,closingBracketIndex+1);
                    int openingSquareBracketIndex = closingString.lastIndexOf('[');
                    int closingSquareBracketIndex = closingString.lastIndexOf(']');
                    if (closingSquareBracketIndex>openingSquareBracketIndex)
                    {
                        restString = line.substring(closingBracketIndex+1);
                        fileContents.append(closingString);
                        break;
                    }
                    else
                    {
                        fileContents.append(line);
                        restString = "";                        
                    }                        
                }
                else
                {
                    fileContents.append(line);
                    restString = "";
                }
            }

            SGFParser<MoveType> parser = new SGFParser<MoveType>(_moveFactory);
            parser.parse(fileContents.toString());
            return parser.getDocumentNode();
        }
        catch (IOException ex)
        {
            logger.error("IOException: "+ex.getMessage());
            abort = true;
            return null;
        }
        catch (ParseException ex)
        {
            logger.error("ParseException: "+ex.getMessage());   
            logger.error(fileContents.toString());
            abort = true;
            return null;
        }        
    }
    
    public void remove()
    {
    }
    
    /**
	 * This is an iterator that iterates over all the moves in a
	 * game-collection. When the move-number in the move returned is zero it
	 * means a new game was started. Unfortunately this iterator only works when
	 * the SGF file doesn't end in multiple new-lines.
	 * 
	 * @return Iterator<GoMove>
	 */
    public Iterator<MoveType> getMoveIterator()
    {    	
    	Iterator<MoveType> moveIterator = new Iterator<MoveType>()
			{
				private TreeNode<SGFData<MoveType>> currentNode;
				private TreeNode<SGFData<MoveType>> firstNode;
				private MoveType currentMove;
				private MoveType markMove;
				private ArrayStack<MoveType> todoList = new ArrayStack<MoveType>();
				private int moveNr;
				
				/**
				 * This checks if there are move moves. If there are no more moves
				 * it delegates the call to SGFCollection.this.hasNext() to see if
				 * there are more games.
				 */
				public boolean hasNext()
				{
					// First check if board-marks were pushed on the todoList.
					if (!todoList.isEmpty())
						return true;
					// Next check if there are more moves in the current game.
					// Note that this relies on games ending with a proper move.
					if (currentNode != null && currentNode.getChildCount() != 0)
						return true;
					
					// Reached the end of the current game, so recycle it.
					if (firstNode!=null)
					{
						firstNode.recycle();
						currentNode = null;
						firstNode = null;
					}
					
					// See if there are more games.
					return SGFCollection.this.hasNext();
				}

				public MoveType next()
				{
					do
					{
						if (!todoList.isEmpty())
						{
							markMove = todoList.pop();
							markMove.setMoveNr(moveNr++);
							return markMove;
						}
						
						if (currentNode == null || currentNode.getChildCount() == 0)
						{
							firstNode = currentNode = SGFCollection.this.next();
							moveNr = 0;
						}
						else
							currentNode = currentNode.getFirstChild();
						
						currentMove = currentNode.getContent().getMove();
						if (currentMove!=null)
						{
							if (currentMove.hasBoardMarks())
			            	{
			            		ArrayList<BoardMark> list = currentMove.getBoardMarks();
			            		for (int i=list.size(); --i>=0;)
			            		{
			            			BoardMark mark = list.get(i);
			            			MoveType move = _moveFactory.createMove(mark);
			            			if (move!=null)
			            				todoList.push(move);
			            		}
			            	}
			            	if (currentMove.isInitialised())
			            	{
								currentMove.setMoveNr(moveNr++);
			            		return currentMove;
			            	}
						}
					}
					while (true); // Skip empty nodes and uninitialised moves.
				}

				public void remove()
				{
					if (markMove!=null)
					{
						markMove.recycle();
						markMove = null;
					}
				}
			};
			
		return moveIterator;
    }
}
