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
package tesuji.games.go.tactics;

import tesuji.core.util.FlyWeight;
import tesuji.core.util.SynchronizedArrayStack;
import tesuji.games.go.util.GoArray;

/**
 * Simple data class to store the results of some tactical reading.
 */
public class TacticsResult
	implements FlyWeight
{
	private int xy;
	private int nrLiberties;
	private int result;
	private int secondaryResult;
	private byte[] dirtyMap;
	
	private static SynchronizedArrayStack<TacticsResult> pool =
		new SynchronizedArrayStack<TacticsResult>();
		//(SynchronizedArrayStack<TacticsResult>)SynchronizedArrayStack.getLocalInstance();
	
	public static TacticsResult createTacticsResult()
	{
		synchronized(pool)
		{
			if (pool.isEmpty())
				return new TacticsResult();
			else
				return pool.pop();
		}
	}
	
	public static TacticsResult createTacticsResult(int xy, int nrLiberties)
	{
		TacticsResult result = createTacticsResult();
		
		result.setXY(xy);
		result.setNrLiberties(nrLiberties);
		result.setResult(TacticsConstant.RESULT_UNDEFINED);
		return result;
	}
	
	private TacticsResult()
	{
		dirtyMap = GoArray.createBytes();
	}

	public int getNrLiberties() 
	{
		return nrLiberties;
	}

	public void setNrLiberties(int nrLiberties) 
	{
		this.nrLiberties = nrLiberties;
	}

	public int getResult() 
	{
		return result;
	}

	public void setResult(int result) 
	{
		this.result = result;
	}

	public int getXY() 
	{
		return xy;
	}

	public void setXY(int xy) 
	{
		this.xy = xy;
	}
	
	public void recycle()
	{
		pool.push(this);
	}

	public byte[] getDirtyMap()
	{
		return dirtyMap;
	}
	
	public final boolean isAffectedBy(int xy)
	{
		return dirtyMap[xy]!=0;
	}

	/**
     * @return the secondaryResult
     */
    public int getSecondaryResult()
    {
    	return secondaryResult;
    }

	/**
     * @param secondaryResult the secondaryResult to set
     */
    public void setSecondaryResult(int secondaryResult)
    {
    	this.secondaryResult = secondaryResult;
    }
}
