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
 */
package tesuji.games.go.pattern.common;

import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import org.apache.log4j.Logger;

import tesuji.core.util.ArrayList;
import tesuji.core.util.List;

/**
 * This is a local version of the PatternManager using Hibernate
 * 
 * Note: this class is NOT thread-safe
 */
public class HibernatePatternManager
	implements PatternManager
{
	private SessionFactory sessionFactory;
	private Session localHibernateSession;
	
	private Logger logger;
	
	private static HibernatePatternManager _singleton;
	private static PatternGroup _defaultGroup;
	
	public static HibernatePatternManager getSingleton()
	{
		
		if (_singleton==null)
		{
			_singleton = new HibernatePatternManager();
		}
		return _singleton;
	}
	
	public HibernatePatternManager(String defaultGroupName)
	{
		this();
		_defaultGroup = getPatternGroup(defaultGroupName);
		getPatterns(_defaultGroup);
	}

	/**
	 * 
	 */
	private HibernatePatternManager()
	{
    	logger = Logger.getLogger(HibernatePatternManager.class);
		Configuration config = new Configuration();
		
		try
		{
			config.addClass(Pattern.class);
			config.addClass(PatternGroup.class);
			sessionFactory = config.buildSessionFactory();
		}
		catch (MappingException ex)
		{
        	logger.error("MappingException: "+ex.getMessage());
		}
		catch (HibernateException ex)
		{
        	logger.error("HibernateException: "+ex.getMessage());
		}
	}
	
	private Session openSession()
		throws HibernateException
	{
		if (localHibernateSession!=null)
			throw new HibernateException("Re-entrant call not allowed.");
		
		localHibernateSession = sessionFactory.openSession();
		localHibernateSession.setFlushMode(FlushMode.MANUAL);
		return localHibernateSession;
	}
	
	private void closeSession()
	{
		try
		{
			localHibernateSession.close();
		}
		catch (HibernateException ex)
		{
	       	logger.error("HibernateException: "+ex.getMessage());
	    }
		finally
		{
			localHibernateSession = null;
		}
	}

    /**
     * @see tesuji.games.go.pattern.common.PatternManager#createPattern(tesuji.games.go.pattern.common.Pattern)
     */
    public Pattern createPattern(Pattern pattern)
	{
        try
        {
        	Session session = openSession();
	    	Transaction t = session.beginTransaction();
        	Integer uniqueId = (Integer)session.save(pattern);
        	pattern.setUniqueId(uniqueId.intValue());
        	t.commit();
        	return pattern;
        }
        catch (HibernateException ex)
        {
        	logger.error("HibernateException: "+ex.getMessage());
        	return null;
        }
        finally
        {
			localHibernateSession.flush();
			closeSession();
        }
	}
      
    /**
     * @see tesuji.games.go.pattern.common.PatternManager#removePattern(tesuji.games.go.pattern.common.Pattern)
     */
    public void removePattern(Pattern pattern)
	{
        try
        {
        	Session hibernateSession = openSession();        	
	    	Transaction t = hibernateSession.beginTransaction();
	    	hibernateSession.delete(pattern);
	    	t.commit();
        }
        catch (HibernateException ex)
        {
        	logger.error("HibernateException: "+ex.getMessage());
        }
        finally
        {
			localHibernateSession.flush();
        	closeSession();
        }
	}
	
    /**
     * @see tesuji.games.go.pattern.common.PatternManager#updatePattern(tesuji.games.go.pattern.common.Pattern)
     */
    public void updatePattern(Pattern pattern)
	{
        try
        {
        	Session hibernateSession = openSession();        	
	    	Transaction t = hibernateSession.beginTransaction();
        	hibernateSession.update(pattern);
        	t.commit();
        }
        catch (HibernateException ex)
        {
        	logger.error("HibernateException: "+ex.getMessage());
        }
        finally
        {
			localHibernateSession.flush();
			closeSession();
        }
	}
    
    /**
     * @see tesuji.games.go.pattern.common.PatternManager#getPatterns(tesuji.games.go.pattern.common.PatternGroup)
     */
	public ArrayList<Pattern> getPatterns(PatternGroup group)
    {
	    try
	    {
	    	Session hibernateSession = openSession();        	
	    	String queryString = "from tesuji.games.go.pattern.common.Pattern as pattern where pattern.groupId="+group.getGroupId()+" order by patternNr asc";
	    	Query query = hibernateSession.createQuery(queryString);
	    	java.util.List<?> list = query.list();
//	    	java.util.List list = hibernateSession.createQuery(query)(query);
	    	ArrayList<Pattern> patternList = new ArrayList<Pattern>(list.size());
	    	for (Object p : list)
	    		patternList.add((Pattern)p);
			group.setPatternList(patternList);
			logger.info("Loaded "+patternList.size()+" patterns");
	    	return patternList;
	    }
	    catch (HibernateException ex)
	    {
	    	logger.error("HibernateException: "+ex.getMessage());
	    	return null;
	    }
	    finally
	    {
	    	closeSession();
	    }
    }

    /**
     * @see tesuji.games.go.pattern.common.PatternManager#updatePatterns(java.util.List)
     */
    public void updatePatterns(ArrayList<Pattern> patternList)
    {
        try
        {
        	Session hibernateSession = openSession(); 
	    	Transaction t = hibernateSession.beginTransaction();
	    	for (int i=0; i<patternList.size(); i++)
        	{
        		Pattern pattern = patternList.get(i);
        		pattern.setPatternNr(i);
        		hibernateSession.update(pattern);
        	}
	    	t.commit();
        }
        catch (HibernateException ex)
        {
        	logger.error("HibernateException: "+ex.getMessage());
        }
        finally
        {
			localHibernateSession.flush();
			closeSession();
        }
    }
    /**
     * @see tesuji.games.go.pattern.common.PatternManager#createPatternGroup(tesuji.games.go.pattern.common.PatternGroup)
     */
    public PatternGroup createPatternGroup(PatternGroup patternGroup)
	{
	    try
	    {
	    	Session hibernateSession = openSession(); 
	    	Transaction t = hibernateSession.beginTransaction();
	    	Integer groupId = (Integer)hibernateSession.save(patternGroup);
	    	patternGroup.setGroupId(groupId.intValue());
	    	t.commit();
	    	return patternGroup;
	    }
	    catch (HibernateException ex)
	    {
	    	logger.error("HibernateException: "+ex.getMessage(),ex);
	    	return null;
	    }
	    finally
	    {
			localHibernateSession.flush();
	    	closeSession();
	    }
	}

    /**
     * @see tesuji.games.go.pattern.common.PatternManager#removePatternGroup(tesuji.games.go.pattern.common.PatternGroup)
     */
    public void removePatternGroup(PatternGroup patternGroup)
	{
	    try
	    {
	    	Session hibernateSession = openSession();        	
	    	Transaction t = hibernateSession.beginTransaction();
	    	hibernateSession.delete(patternGroup);
	    	t.commit();
	    }
	    catch (HibernateException ex)
	    {
	    	logger.error("HibernateException: "+ex.getMessage());
	    }
	    finally
	    {
			localHibernateSession.flush();
	    	closeSession();
	    }
	}
   
    /**
     * @see tesuji.games.go.pattern.common.PatternManager#updatePatternGroup(tesuji.games.go.pattern.common.PatternGroup)
     */
    public void updatePatternGroup(PatternGroup patternGroup)
	{
	    try
	    {
	    	Session hibernateSession = openSession(); 
	    	Transaction t = hibernateSession.beginTransaction();
	    	logger.info("Update group "+patternGroup+"-"+patternGroup.getDescription());
	    	hibernateSession.update(patternGroup);
	    	t.commit();
	    }
	    catch (HibernateException ex)
	    {
	    	logger.error("HibernateException: "+ex.getMessage());
	    }
	    finally
	    {
			localHibernateSession.flush();
	    	closeSession();
	    }
	}
   
    /**
     * @see tesuji.games.go.pattern.common.PatternManager#getPatternGroups()
     */
	public List<PatternGroup> getPatternGroups()
    {
	    try
	    {
	    	Session hibernateSession = openSession();        	
	    	java.util.List<?> list = hibernateSession.createQuery("from tesuji.games.go.pattern.common.PatternGroup").list();
	    	List<PatternGroup> groupList = new ArrayList<PatternGroup>(list.size());
	    	for (Object o : list)
	    		groupList.add((PatternGroup)o);
	    	return groupList;
	    }
	    catch (HibernateException ex)
	    {
	    	logger.error("HibernateException: "+ex.getMessage());
	    	return null;
	    }
	    catch (Exception ex)
	    {
	    	logger.error("Unexpected Exception: "+ex.getMessage());
	    	return null;
	    }
	    finally
	    {
	    	closeSession();
	    }
    }
    
    /**
     * @see tesuji.games.go.pattern.common.PatternManager#getPatternGroup(int)
     */
	public PatternGroup getPatternGroup(int groupId)
    {
	    try
	    {
	    	Session hibernateSession = openSession();        	
	    	String query = "from tesuji.games.go.pattern.common.PatternGroup as patternGroup where patternGroup.groupId="+groupId;
	    	java.util.List<?> list = hibernateSession.createQuery(query).list();
	    	if (list.size()!=1)
	    	{
	    		logger.error("PatternGroup with id '"+groupId+"' does not exist.");
	    		return null;
	    	}
	    	return (PatternGroup)list.get(0);
	    }
	    catch (HibernateException ex)
	    {
	    	logger.error("HibernateException: "+ex.getMessage());
	    	return null;
	    }
	    finally
	    {
	    	closeSession();
	    }
    }
    
    /**
     * @see tesuji.games.go.pattern.common.PatternManager#getPatternGroup(java.lang.String)
     */
    public PatternGroup getPatternGroup(String groupName)
    {
	    try
	    {
	    	Session hibernateSession = openSession();        	
	    	String query = "from tesuji.games.go.pattern.common.PatternGroup as patternGroup where patternGroup.groupName='"+groupName+"'";
	    	java.util.List<?> list = hibernateSession.createQuery(query).list();
	    	if (list.size()!=1)
	    	{
	    		logger.error("PatternGroup '"+groupName+"' does not exist.");
	    		return null;
	    	}
	    	PatternGroup group = (PatternGroup) list.get(0);
	    	return group;
	    }
	    catch (HibernateException ex)
	    {
	    	logger.error("HibernateException: "+ex.getMessage());
	        return null;
	    }
	    finally
	    {
	    	closeSession();
	    }
    }
    
    public PatternGroup getDefaultPatternGroup()
    {
    	return _defaultGroup;
    }
}
