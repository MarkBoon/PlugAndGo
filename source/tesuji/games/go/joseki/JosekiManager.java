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
package tesuji.games.go.joseki;

import java.util.ArrayList;

import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import org.apache.log4j.Logger;

public class JosekiManager
{
	private SessionFactory sessionFactory;
	private Session localHibernateSession;
	
	private Logger logger;
	
	private static JosekiManager _singleton;
	
	public static JosekiManager getSingleton()
	{
		
		if (_singleton==null)
		{
			_singleton = new JosekiManager();
		}
		return _singleton;
	}
	
	/**
	 * 
	 */
	private JosekiManager()
	{
    	logger = Logger.getLogger(JosekiManager.class);
		Configuration config = new Configuration();
		
		try
		{
			config.addClass(MCJosekiEntry.class);
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
    public synchronized void createJosekiEntry(MCJosekiEntry entry)
	{
        try
        {
        	Session session = openSession();
	    	Transaction t = session.beginTransaction();
        	session.save(entry);
        	t.commit();
        }
        catch (HibernateException ex)
        {
        	logger.error("HibernateException: "+ex.getMessage());
        }
        finally
        {
        	try
        	{
        		localHibernateSession.flush();
        	}
            catch (Exception ex)
            {
            	System.out.flush();
            	logger.error("HibernateException: "+ex.getMessage());
            }
			closeSession();
        }
	}
      
    /**
     * @see tesuji.games.go.pattern.common.PatternManager#removePattern(tesuji.games.go.pattern.common.Pattern)
     */
    public synchronized void removeJosekiEntry(MCJosekiEntry entry)
	{
        try
        {
        	Session hibernateSession = openSession();        	
	    	Transaction t = hibernateSession.beginTransaction();
	    	hibernateSession.delete(entry);
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
    public synchronized void updateJosekiEntry(MCJosekiEntry entry)
	{
        try
        {
        	Session hibernateSession = openSession();        	
	    	Transaction t = hibernateSession.beginTransaction();
        	hibernateSession.update(entry);
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
	public synchronized MCJosekiEntry getJosekiEntry(long checksum)
    {
	    try
	    {
	    	Session hibernateSession = openSession();        	
	    	String queryString = "from tesuji.games.go.joseki.MCJosekiEntry where checksum="+checksum;
	    	Query query = hibernateSession.createQuery(queryString);
	    	MCJosekiEntry entry = (MCJosekiEntry)query.uniqueResult();
	    	return entry;
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
     * @see tesuji.games.go.pattern.common.PatternManager#getPatterns(tesuji.games.go.pattern.common.PatternGroup)
     */
	public synchronized ArrayList<MCJosekiEntry> getJosekiEntries()
    {
	    try
	    {
	    	Session hibernateSession = openSession();        	
	    	String queryString = "from tesuji.games.go.joseki.MCJosekiEntry";
	    	Query query = hibernateSession.createQuery(queryString);
	    	java.util.List<?> list = query.list();
	    	ArrayList<MCJosekiEntry> josekiList = new ArrayList<MCJosekiEntry>(list.size());
	    	for (Object p : list)
	    		josekiList.add((MCJosekiEntry)p);
			logger.info("Loaded "+josekiList.size()+" josekis");
	    	return josekiList;
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
}
