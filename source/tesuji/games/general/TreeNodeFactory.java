package tesuji.games.general;

import tesuji.core.util.Factory;
import tesuji.core.util.FactoryReport;
import tesuji.core.util.SynchronizedArrayStack;

public class TreeNodeFactory
	implements Factory
{
	private static int nrTreeNodes = 0;
	
	private static SynchronizedArrayStack<TreeNode<?>> treeNodePool =	new SynchronizedArrayStack<TreeNode<?>>();

	private static TreeNodeFactory _singleton;
	
	public static TreeNodeFactory getSingleton()
	{
		if (_singleton==null)
		{
			_singleton = new TreeNodeFactory();
			FactoryReport.addFactory(_singleton);
		}
		
		return _singleton;
	}
	
	public String getFactoryName()
	{
		return "TreeNodeFactory";
	}
	
	public String getFactoryReport()
	{
		return "Number of TreeNode objects:\t\t\t"+nrTreeNodes;
	}
	
	@SuppressWarnings("unchecked") // This is where generics stop...?
	public TreeNode createTreeNode()
	{
    	synchronized (treeNodePool)
        {
	        TreeNode newNode;
	        if (treeNodePool.isEmpty())
	        {
	        	newNode = new TreeNode();
	        	nrTreeNodes++;
	        }
	        else
	            newNode = treeNodePool.pop();
	        
	        return newNode;
        }	
	}
	
	public static void recycle(TreeNode<?> node)
	{
		treeNodePool.push(node);
	}
}
