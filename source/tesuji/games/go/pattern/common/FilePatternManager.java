/**
 *	Product: Tesuji Software Go Library.<br><br>
 *
 *	<font color="#CC6600"><font size=-1>
 *	Copyright (c) 2001-2004 Tesuji Software B.V.<br>
 *	All rights reserved.<br><br>
 *
 *	Permission is hereby granted, free of charge, to any person obtaining a
 *	copy of this software and associated documentation files (the
 *	"Software"), to deal in the Software without restriction, including
 *	without limitation the rights to use, copy, modify, merge, publish,
 *	distribute, and/or sell copies of the Software, and to permit persons
 *	to whom the Software is furnished to do so, provided that the above
 *	copyright notice(s) and this permission notice appear in all copies of
 *	the Software and that both the above copyright notice(s) and this
 *	permission notice appear in supporting documentation.<br><br>
 *
 *	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 *	COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *	IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *	CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.<br><br>
 *
 *	Except as contained in this notice, the name of a Tesuji Software
 *	shall not be used in advertising or otherwise to promote the sale, use
 *	or other dealings in this Software without prior written authorization
 *	of Tesuji Software.<br><br>
 *	<font color="#00000"><font size=+1>
 */
package tesuji.games.go.pattern.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;

import org.apache.log4j.Logger;

import tesuji.core.util.ArrayList;
import tesuji.core.util.FileUtil;
import tesuji.core.util.List;

/**
 * Flat-file implementation of PatternManager
 * 
 * The PatternGroup objects are stored in a file called "PatternGroups.ser"
 * The patterns are stored in a file that contains the group-id: "PatternsXXX.ser"
 * 
 * For large number of patterns this is not very efficient as it doesn't provide
 * the random-access a database does. So every operation writes the whole list
 * of patterns that belong to the same group.
 */
public class FilePatternManager
	implements PatternManager
{
	private static final String PATTERN_GROUPS_FILE = "PatternGroups.ser";
	
	private static final Logger _logger = Logger.getLogger(FilePatternManager.class);
	private ArrayList<PatternGroup> patternGroupList;
	private int nextPatternGroupID = 1;
	
	private static FilePatternManager _singleton;
	
	public static FilePatternManager getSingleton()
	{
		if (_singleton==null)
			_singleton = new FilePatternManager();
		return _singleton;
	}
	/**
	 * 
	 */
	private FilePatternManager()
	{
		readPatternGroupList();
	}

	/**
	 * @see tesuji.games.go.pattern.common.PatternManager#createPattern(tesuji.games.go.pattern.common.Pattern)
	 */
	public Pattern createPattern(Pattern pattern)
	{
		synchronized (_singleton)
		{
			PatternGroup group = getPatternGroup(pattern.getGroupId());
			group.getPatternList().add(pattern);
			renumberPatterns(group.getPatternList());
			savePatternList(group);
			return pattern;
		}
	}

	/**
	 * @see tesuji.games.go.pattern.common.PatternManager#removePattern(tesuji.games.go.pattern.common.Pattern)
	 */
	public void removePattern(Pattern pattern)
	{
		synchronized (_singleton)
		{
			PatternGroup group = getPatternGroup(pattern.getGroupId());
			group.getPatternList().remove(pattern);
			renumberPatterns(group.getPatternList());
			savePatternList(group);
		}
	}

	/**
	 * @see tesuji.games.go.pattern.common.PatternManager#updatePattern(tesuji.games.go.pattern.common.Pattern)
	 */
	public void updatePattern(Pattern pattern)
	{
		synchronized (_singleton)
		{
			PatternGroup group = getPatternGroup(pattern.getGroupId());
			int index = group.getPatternList().indexOf(pattern);
			group.getPatternList().set(index,pattern);
			renumberPatterns(group.getPatternList());
			savePatternList(group);
		}
	}

	/**
	 * @see tesuji.games.go.pattern.common.PatternManager#getPatterns(tesuji.games.go.pattern.common.PatternGroup)
	 */
	public ArrayList<Pattern> getPatterns(PatternGroup group)
	{
		synchronized (_singleton)
		{
			readPatternList(group);
			return group.getPatternList();
		}
	}

	/**
	 * @see tesuji.games.go.pattern.common.PatternManager#updatePatterns(java.util.List)
	 */
	public void updatePatterns(ArrayList<Pattern> patternList)
	{
		synchronized (_singleton)
		{
			if (patternList.size()>0)
			{
				renumberPatterns(patternList);
				PatternGroup group = getPatternGroup(patternList.get(0).getGroupId());
				savePatternList(group);
			}
		}
	}

	/**
	 * @see tesuji.games.go.pattern.common.PatternManager#createPatternGroup(tesuji.games.go.pattern.common.PatternGroup)
	 */
	public PatternGroup createPatternGroup(PatternGroup group)
	{
		synchronized (_singleton)
		{
			patternGroupList.add(group);
			group.setGroupId(nextPatternGroupID++);
			savePatternGroupList();
			return group;
		}
	}

	/**
	 * @see tesuji.games.go.pattern.common.PatternManager#removePatternGroup(tesuji.games.go.pattern.common.PatternGroup)
	 */
	public void removePatternGroup(PatternGroup group)
	{
		synchronized (_singleton)
		{
			patternGroupList.remove(group);
			savePatternGroupList();
		}
	}

	/**
	 * @see tesuji.games.go.pattern.common.PatternManager#updatePatternGroup(tesuji.games.go.pattern.common.PatternGroup)
	 */
	public void updatePatternGroup(PatternGroup group)
	{
		synchronized (_singleton)
		{
			int index = patternGroupList.indexOf(group);
			patternGroupList.set(index,group);
			savePatternGroupList();
		}
	}

	/**
	 * @see tesuji.games.go.pattern.common.PatternManager#getPatternGroups()
	 */
	public List<PatternGroup> getPatternGroups()
	{
		return patternGroupList;
	}

	/**
	 * @see tesuji.games.go.pattern.common.PatternManager#getPatternGroup(java.lang.String)
	 */
	public PatternGroup getPatternGroup(String groupName)
	{
        for (PatternGroup group : patternGroupList)
        {
			if (group.getGroupName().equals(groupName))
			{
				getPatterns(group);
				return group;
			}
        }

		return null;
	}

	/**
	 * @see tesuji.games.go.pattern.common.PatternManager#getPatternGroup(int)
	 */
	public PatternGroup getPatternGroup(int groupId)
	{
		for (int i=patternGroupList.size(); --i>=0;)
		{
			PatternGroup group = patternGroupList.get(i);
			if (group.getGroupId()==groupId)
				return group;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private void readPatternGroupList()
	{
		try
		{
			InputStream input = FileUtil.getInputStream(PATTERN_GROUPS_FILE, getClass());
			/*String className = this.getClass().getName().replace(".","/")+".class";
        	URL url = getClass().getClassLoader().getResource(className);
        	File classFile = new File(url.toURI());
        	// TODO - Need to find something a little more elegant.
        	File patternFile = new File(classFile.getParent()+"/../../../../../../source/"+PATTERN_GROUPS_FILE);
        	if (!patternFile.exists())
        	{
        		patternGroupList = new ArrayList<PatternGroup>();
        		return;
        	}
			FileInputStream input = new FileInputStream(patternFile);*/
			ObjectInputStream objectInput = new ObjectInputStream(input);
			patternGroupList = (ArrayList<PatternGroup>) objectInput.readObject();
			objectInput.close();
			input.close();
			
			for (int i=patternGroupList.size(); --i>=0;)
			{
				PatternGroup group = patternGroupList.get(i);
				group.setPatternList(new ArrayList<Pattern>());
				if (group.getGroupId()>=nextPatternGroupID)
					nextPatternGroupID = group.getGroupId()+1;
			}
		}
		catch (Exception ex)
		{
			patternGroupList = new ArrayList<PatternGroup>();
			_logger.error("Error reading file "+PATTERN_GROUPS_FILE,ex);
		}
	}

	private void savePatternGroupList()
	{
		try
		{
			URL url = FileUtil.getResourceURL(PATTERN_GROUPS_FILE, getClass());
        	File patternGroupFile = new File(url.toURI());
			FileOutputStream output = new FileOutputStream(patternGroupFile);
        	
			/*String className = this.getClass().getName().replace(".","/")+".class";
        	URL url = getClass().getClassLoader().getResource(className);
        	File classFile = new File(url.toURI());
        	// TODO - Need to find something a little more elegant.
        	File patternFile = new File(classFile.getParent()+"/../../../../../../source/"+PATTERN_GROUPS_FILE);
			FileOutputStream output = new FileOutputStream(patternFile);*/
			ObjectOutputStream objectOutput = new ObjectOutputStream(output);
			objectOutput.writeObject(patternGroupList);
			objectOutput.close();
			output.close();
		}
		catch (Exception ex)
		{
			_logger.error("Error writing file "+PATTERN_GROUPS_FILE,ex);
		}		
	}
	

	@SuppressWarnings("unchecked")
	private void readPatternList(PatternGroup patternGroup)
	{
		String fileName = "Patterns"+patternGroup.getGroupName()+".ser";
		try
		{
			InputStream input = FileUtil.getInputStream(fileName, getClass());
			
//			String className = this.getClass().getName().replace(".","/")+".class";
//        	URL url = getClass().getClassLoader().getResource(className);
//        	File classFile = new File(url.toURI());
//        	// TODO - Need to find something a little more elegant.
//        	File patternFile = new File(classFile.getParent()+"/../../../../../../source/"+fileName);
//			FileInputStream input = new FileInputStream(patternFile);
			ObjectInputStream objectInput = new ObjectInputStream(input);
			ArrayList<Pattern> patternList = (ArrayList<Pattern>) objectInput.readObject();
			patternGroup.setPatternList(patternList);
			objectInput.close();
			input.close();
		}
		catch (Exception ex)
		{
			_logger.error("Error reading file "+fileName,ex);
		}
	}

	private void savePatternList(PatternGroup patternGroup)
	{
		renumberPatterns(patternGroup.getPatternList());
		String fileName = "Patterns"+patternGroup.getGroupName()+".ser";
		try
		{
			URL url = FileUtil.getResourceURL(fileName, getClass());
			File patternFile = new File(url.toURI());
//			String className = this.getClass().getName().replace(".","/")+".class";
//        	URL url = getClass().getClassLoader().getResource(className);
//        	File classFile = new File(url.toURI());
//        	// TODO - Need to find something a little more elegant.
//        	File patternFile = new File(classFile.getParent()+"/../../../../../../source/"+fileName);
			FileOutputStream output = new FileOutputStream(patternFile);
			ObjectOutputStream objectOutput = new ObjectOutputStream(output);
			objectOutput.writeObject(patternGroup.getPatternList());
			objectOutput.close();
			output.close();
		}
		catch (Exception ex)
		{
			_logger.error("Error writing file "+fileName,ex);
		}		
	}
	
	private void renumberPatterns(List<Pattern> patternList)
	{
		for(int i=patternList.size(); --i>=0;)
		{
			Pattern pattern = patternList.get(i);
			pattern.setPatternNr(i);
		}
	}
    
    public PatternGroup getDefaultPatternGroup()
    {
    	// Not implemented.
    	return null;
    }
}
