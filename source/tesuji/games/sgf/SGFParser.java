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

import javax.swing.tree.DefaultTreeModel;

import org.apache.log4j.Logger;

import tesuji.core.util.StringBufferFactory;

import tesuji.games.general.Move;
import tesuji.games.general.MoveFactory;
import tesuji.games.general.TreeNode;
import tesuji.games.general.TreeNodeFactory;

/**
 * A local helper class for parsing an SGF file.
 * Basically it provides a fast way to iterate over the characters in a string.
 */
class SGFBuffer
{
    char[] _contents;
    int _index = 0;
    
    SGFBuffer(String initialString)
    {
        _contents = initialString.toCharArray();
    }
    
    SGFBuffer(StringBuilder initialString)
    {
        _contents = new char[initialString.length()];
        initialString.getChars(0,_contents.length,_contents,0);
    }
    
    public final int length()
    {
        return _contents.length-_index;
    }
    
    public final char getNextChar()
    {
        return _contents[_index++];
    }
    
    public final void substring(int i)
    {
        _index += i;
    }
}

/**
 * Simple SGF parser
 * 
 * The data doesn't get interpreted. It simply creates a tree of SGFData objects. 
 * The Movefactory instance passed into the constructor
 * is used to interprete the SGFData objects into actual move objects.
 */
public class SGFParser<MoveType extends Move>
{
	private static Logger _logger = Logger.getLogger(SGFParser.class);
	
	private TreeNode<SGFData<MoveType>> _documentNode;
	
	// It's the parseMove of the MoveFactory that does the actual interpreting of the data.
	private MoveFactory<MoveType> _moveFactory;
	
	// SGF often lists many properties in a row where only the first one has a name
	// and all the others are implicitly the same. Here we keep the last property that
	// had a name so we can infer the name for those that don't.
 	private SGFProperty _lastProperty;
 	
	/**
	 * @param moveFactory used to make instantiations of the moves.
	 */
	public SGFParser(MoveFactory<MoveType> moveFactory)
	{
		_moveFactory = moveFactory;
	}
	
	/**
	 * Parse an input string containing SGF data
	 * 
	 * @param sgf input string
	 * 
	 * @throws ParseException
	 */
	@SuppressWarnings("unchecked")
	public void parse(StringBuilder sgf)
		throws ParseException
	{
		_documentNode = TreeNodeFactory.getSingleton().createTreeNode();
		SGFData<MoveType> sgfData = SGFDataFactory.createSGFData();
		_documentNode.setContent(sgfData);
		parse(new SGFBuffer(sgf),_documentNode);
	}

    /**
     * Parse an input string containing SGF data
     * 
     * @param sgf input string
     * 
     * @throws ParseException
     */
    @SuppressWarnings("unchecked")
	public void parse(String sgf)
        throws ParseException
    {
		_documentNode = TreeNodeFactory.getSingleton().createTreeNode();
		SGFData<MoveType> sgfData = SGFDataFactory.createSGFData();
		_documentNode.setContent(sgfData);
        sgfData.setSGF(sgf);
        parse(new SGFBuffer(sgf),_documentNode);
    }

	/**
	 * @return the root-node
	 */
	public TreeNode<SGFData<MoveType>> getDocumentNode()
	{
		return _documentNode;
	}

	/**
	 * @param sgf input data
	 * @param parentNode to which it attaches any nodes parsed
	 * 
	 * @throws ParseException
	 */
	private void parse(SGFBuffer sgf, TreeNode<SGFData<MoveType>> parentNode)
		throws ParseException
	{
		StringBuffer propertyName = null;
		boolean variation = false;
        
		while (sgf.length()>0)
		{
            char c = sgf.getNextChar();
			if (c>='A' && c<='Z')
			{
				if (propertyName==null)
					propertyName = StringBufferFactory.createSmallStringBuffer();
				assert propertyName.length()==0;
				do
				{
					propertyName.append(c);
                    c = sgf.getNextChar();
				}
				while (c>='A' && c<='Z');
			}
            switch(c)
            {
                case '[':
               		parseProperty(propertyName,sgf,parentNode.getContent());
               		propertyName = null;
                    break;
                case '(':
                    variation = true;
                    break;
                case ')':
                    return;
                case ';':
                	@SuppressWarnings("unchecked")
                	TreeNode<SGFData<MoveType>> newNode = TreeNodeFactory.getSingleton().createTreeNode();
                	@SuppressWarnings("unchecked")
                	SGFData<MoveType> sgfData = SGFDataFactory.createSGFData();
                	newNode.setContent(sgfData);
                    parentNode.add(newNode);
                    parse(sgf,newNode);
                    sgfData.setMove(_moveFactory.parseMove(sgfData));
                    if (!variation)
                        return;
                    break;
            }
		}
		if (propertyName!=null)
			StringBufferFactory.recycleSmallStringBuffer(propertyName);
	}

	/**
	 * Parse an SGF property and add it to its node
	 * 
	 * @param propertyName
	 * @param sgf input data
	 * @param sgfData to which the property needs to be added
	 * 
	 * @throws ParseException
	 */
	private void parseProperty(StringBuffer propertyName, SGFBuffer sgf, SGFData<MoveType> sgfData)
		throws ParseException
	{
		StringBuffer value = StringBufferFactory.createSmallStringBuffer();
	    assert value.length()==0;
	    boolean escaped = false;
	    while (sgf.length()>0)
	    {
            char c = sgf.getNextChar();
	        if (escaped)
	        {
	            if (!value.equals("\n"))
	                value.append(c);
	            escaped = false;
	        }
	        else if (c == '\\')
	        	escaped = true;
	        else if (c == ']') // This marks the end of a property
	        {
	        	SGFProperty property = SGFProperty.createSGFProperty();
	        	property.setValue(value);
	        	
	    		if (propertyName==null || propertyName.length()==0)
	    		{
	    			if (_lastProperty==null)
	    			    throw new ParseException("Illegal property construct.",0);
	    			else
	    			{
	    				// We basically copy the property-name.
	    				StringBuffer propertyNameBuffer = StringBufferFactory.createSmallStringBuffer();
	    				propertyNameBuffer.append(_lastProperty.getName().toString());
	    				property.setName(propertyNameBuffer);
	    			}
	    		}
	    		else
	    		{
		        	property.setName(propertyName);
	    			_lastProperty = property;
	    		}
	        	
	        	sgfData.addProperty(property);
	            return;
	        }
	        else
	            value.append(c);
	    }
	    throw new ParseException("Unexpected end of SGF data.\n",0);
	}

	/**
	 * Read an SGF file and make a game-tree out of it.
	 * 
	 * @param fileName of the file to read from the file-system.
	 * 
	 * @return rootNode of the resulting tree.
	 */
	public TreeNode<SGFData<MoveType>> readSGFFile(String fileName)
	{
		StringBuffer fileContents = StringBufferFactory.createStringBuffer();
		try
		{
			FileReader fileReader = new FileReader(fileName);
            BufferedReader reader = new BufferedReader(fileReader);
            while (reader.ready())
            {
                String line = reader.readLine();
                fileContents.append(line);
            }
            parse(fileContents.toString());
            StringBufferFactory.recycleStringBuffer(fileContents);
            reader.close();
            return getDocumentNode();
		}
		catch (IOException ex)
		{
			_logger.error("IOException: "+ex.getMessage());
		}
		catch (ParseException ex)
		{
			_logger.error("ParseException: "+ex.getMessage());			
		}
		return null;
	}

	/**
	 * Import the SGF tree in the game-tree represented by
	 * the GameTreeNode passed in.
	 * 
	 * @param moveNode
	 * @param sgfNode
	 */
	public void importSGFNode(TreeNode<MoveType> moveNode, TreeNode<SGFData<MoveType>> sgfNode)
	{
		/*SGFData<MoveType> sgfData = sgfNode.getContent();
		if (sgfData.getPropertyList().size()==0 && sgfNode.getChildCount()==1)
		{
			// This takes care of the first empty node.
			@SuppressWarnings("unchecked")
			GameTreeNode<MoveType> nextMoveNode = GameTreeNodeFactory.createNode(_moveFactory.createMove());
			moveNode.add(nextMoveNode);
			importSGFNode(nextMoveNode, sgfNode.getFirstChild());
		}
		else*/
		{
			// moveNode.setMove(sgfData.getMove()); TODO - check, should be obsolete since the first node never contains a move
			for (TreeNode<SGFData<MoveType>> childNode : sgfNode.getChildren())
			{
				MoveType move = _moveFactory.cloneMove(childNode.getContent().getMove());
				if (move.isInitialised())
				{
					@SuppressWarnings("unchecked")
					TreeNode<MoveType> nextMoveNode = TreeNodeFactory.getSingleton().createTreeNode();
					nextMoveNode.setContent(move);
					moveNode.add(nextMoveNode);
					importSGFNode(nextMoveNode,childNode);
				}
				else
					importSGFNode(moveNode,childNode);
			}
		}
	}

	/**
	 * Import the SGF tree in the game-tree represented by
	 * the GameTreeNode passed in.
	 * 
	 * @param moveNode
	 * @param sgfNode
	 */
	public void importSGFNode(DefaultTreeModel treeModel, TreeNode<MoveType> moveNode, TreeNode<SGFData<MoveType>> sgfNode)
	{
		/*SGFData<MoveType> sgfData = sgfNode.getContent();
		if (sgfData.getPropertyList().size()==0 && sgfNode.getChildCount()==1)
		{
			// This takes care of the first empty node.
			@SuppressWarnings("unchecked")
			GameTreeNode<MoveType> nextMoveNode = GameTreeNodeFactory.createNode(_moveFactory.createMove());
			moveNode.add(nextMoveNode);
			importSGFNode(nextMoveNode, sgfNode.getFirstChild());
		}
		else*/
		{
			// moveNode.setMove(sgfData.getMove()); TODO - check, should be obsolete since the first node never contains a move
			for (TreeNode<SGFData<MoveType>> childNode : sgfNode.getChildren())
			{
				MoveType move = _moveFactory.cloneMove(childNode.getContent().getMove());
				@SuppressWarnings("unchecked")
				TreeNode<MoveType> nextMoveNode = TreeNodeFactory.getSingleton().createTreeNode();
				nextMoveNode.setContent(move);
				treeModel.insertNodeInto(nextMoveNode, moveNode, moveNode.getChildCount());
//				moveNode.add(nextMoveNode);
				importSGFNode(nextMoveNode,childNode);
			}
		}
	}
}
