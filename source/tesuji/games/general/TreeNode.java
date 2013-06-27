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
package tesuji.games.general;

import java.util.Enumeration;

import javax.swing.tree.MutableTreeNode;

import tesuji.core.util.ArrayList;
import tesuji.core.util.FlyWeight;
import tesuji.core.util.List;

/**
 * This is a generic implementation of a tree. It more or less follows the DefaultMutableTreeNode
 * implementation with the main differences in the use of generics and FlyWeight objects.
 * 
 * Note that this is implemented as a general container class much like List. So instead of
 * sub-classing this class, the methods setContent() and and getContent() are used to store
 * and retrieve any data stored in the tree.
 */
public class TreeNode<NodeContentType extends FlyWeight>
	implements FlyWeight, javax.swing.tree.MutableTreeNode
{
	private TreeNode<NodeContentType> _parent;
	private ArrayList<TreeNode<NodeContentType>> _children;
	private NodeContentType _content;
	private long _checksum = Checksum.UNINITIALIZED;
	
	TreeNode()
	{
	}
	
	public TreeNode(NodeContentType o)
	{
		this();
		_content = o;
	}
	
	/* (non-Javadoc)
     * @see javax.swing.tree.MutableTreeNode#insert(javax.swing.tree.MutableTreeNode, int)
     */
    public void insert(TreeNode<NodeContentType> treeNode, int index)
    {
    	treeNode.setParent(this);
    	if (_children==null)
    		allocateChildren();
	    _children.insert(index, treeNode);
    }

	/* (non-Javadoc)
     * @see javax.swing.tree.MutableTreeNode#remove(int)
     */
    public void remove(int index)
    {
    	if (_children!=null)
    		_children.remove(index);
    }

	/* (non-Javadoc)
     * @see javax.swing.tree.MutableTreeNode#remove(javax.swing.tree.MutableTreeNode)
     */
    public void remove(TreeNode<NodeContentType> treeNode)
    {
    	if (_children!=null)
    		_children.remove(treeNode);
    }

	/* (non-Javadoc)
     * @see javax.swing.tree.MutableTreeNode#setUserObject(java.lang.Object)
     */
    public void setContent(NodeContentType content)
    {
	   _content = content;
    }
    
    public NodeContentType getContent()
    {
    	return _content;
    }

	/* (non-Javadoc)
     * @see javax.swing.tree.MutableTreeNode#removeFromParent()
     */
    public void removeFromParent()
    {
    	if (_parent!=null)
    	{
		    _parent.remove(this);
		    _parent = null;
    	}
    }

	/* (non-Javadoc)
     * @see javax.swing.tree.MutableTreeNode#setParent(javax.swing.tree.MutableTreeNode)
     */
    public void setParent(TreeNode<NodeContentType> treeNode)
    {
	    _parent = treeNode;
    }

	/* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getChildAt(int)
     */
    public TreeNode<NodeContentType> getChildAt(int index)
    {
    	if (_children==null)
    		return null;
	    return _children.get(index);
    }

	/* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getChildCount()
     */
    public int getChildCount()
    {
    	if (_children==null)
    		return 0;
	    return _children.size();
    }

	/* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getParent()
     */
    public TreeNode<NodeContentType> getParent()
    {
	    return _parent;
    }

	/* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getIndex(javax.swing.tree.TreeNode)
     */
    public int getIndex(TreeNode<NodeContentType> treeNode)
    {
    	if (_children==null)
    		return -1;
    	return _children.indexOf(treeNode);
    }

	/* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getAllowsChildren()
     */
    public boolean getAllowsChildren()
    {
	    return true;
    }

	/* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#isLeaf()
     */
    public boolean isLeaf()
    {
	    return (_children==null || _children.size()==0);
    }

	public List<TreeNode<NodeContentType>> getChildren()
	{
    	if (_children==null)
    		allocateChildren();
		return _children;
	}
    
    public void add(TreeNode<NodeContentType> treeNode)
    {
    	treeNode.setParent(this);
    	if (_children==null)
    		allocateChildren();
    	_children.add(treeNode);
    }
    
    public TreeNode<NodeContentType> getFirstChild()
    {
    	if (_children==null)
    		return null;
    	return _children.get(0);
    }
    
    public void makeFirstChild(TreeNode<NodeContentType> child)
    {
    	_children.set(getIndex(child),_children.get(0));
    	_children.set(0,child);
    }
    
    public boolean hasChild(TreeNode<NodeContentType> child)
    {
    	if (_children==null)
    		return false;
    	if (_children.indexOf(child)<0)
    		return false;
    	
    	for (int i=0; i<getChildCount(); i++)
    	{
    		boolean is = _children.get(i)==child;
    		boolean equals = _children.get(i).equals(child);
    		if (_children.get(i)==child && _children.get(i).equals(child))
    				return true;
    		if (is!=equals)
    		{
        		is = _children.get(i)==child;
        		equals = _children.get(i).equals(child);
    			return false;
    		}
    	}
    	return false;
    }
    
    public TreeNode<NodeContentType> getNextSibling()
    {
    	if (_parent==null)
    		return null;
    	
    	int index = _parent.getIndex(this);
    	if (index<0 || index>=_parent.getChildCount()-1)
    		return null;
    	
    	return _parent.getChildAt(index+1);
    }
    
    public TreeNode<NodeContentType> getPreviousSibling()
    {
    	if (_parent==null)
    		return null;
    	
    	int index = _parent.getIndex(this);
    	if (index<=0)
    		return null;
    	
    	return _parent.getChildAt(index-1);
    }

    public void removeAllChildren()
    {
    	if (_children!=null)
    		_children.clear();
    }

	public void recycle()
	{
		removeFromParent();

		for (int i=0; i<getChildCount(); i++)
		{
			TreeNode<NodeContentType> childNode = _children.get(i);
			childNode.setParent(null); // Not strictly necessary but it speeds things up.
			childNode.recycle();
		}
		_children = null;
		
		if (_content!=null)
			_content.recycle();
		_content = null;
		_parent = null;
		_checksum = Checksum.UNINITIALIZED;
		
		TreeNodeFactory.recycle(this);
	}

	@Override
	public String toString()
	{
		if (getContent()!=null)
			return getContent().toString();
		else
			return "<empty>";
	}
	
	public void setChecksum(long checksum)
	{
		_checksum = checksum;
	}
	
	@Override
	public int hashCode()
	{
		return (int)_checksum;
	}
	
	@Override
	public boolean equals(Object o)
	{
		return (o==this);
	}

    // This is actually silly but necessary because the 'official' TreeNode doesn't support generics.
	@SuppressWarnings("unchecked")
	public int getIndex(javax.swing.tree.TreeNode node)
	{
    	if (_children==null)
    		return -1;
		
    	return _children.indexOf((TreeNode<NodeContentType>)node);
	}

	public Enumeration<TreeNode<NodeContentType>> children()
	{
    	if (_children==null)
    		allocateChildren();
		return _children.elements();
	}
    
	@SuppressWarnings("unchecked")
	public void insert(MutableTreeNode arg0, int arg1)
	{
		insert((TreeNode<NodeContentType>)arg0,arg1);		
	}

	@SuppressWarnings("unchecked")
	public void remove(MutableTreeNode arg0)
	{
		remove((TreeNode<NodeContentType>)arg0);		
	}

	@SuppressWarnings("unchecked")
	public void setParent(MutableTreeNode arg0)
	{
		setParent((TreeNode<NodeContentType>)arg0);
	}

	@SuppressWarnings("unchecked")
	public void setUserObject(Object arg0)
	{
		setContent((NodeContentType)arg0);
	}
	
	private void allocateChildren()
	{
		if (_parent!=null && _parent.getChildCount()!=0)
			_children = new ArrayList<TreeNode<NodeContentType>>(_parent.getChildCount());
		else
			_children = new ArrayList<TreeNode<NodeContentType>>(1);		
	}
}
