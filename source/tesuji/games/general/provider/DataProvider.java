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
package tesuji.games.general.provider;

/**
 * DataProvider interface
 * 
 * DataProviders are the method with which modules in a game engine can provide data to the outside for inspection.
 * The way this is generally done is by registering a DataProvider with the singleton class DataProviderList.
 * Each provider has a unique name that is used to distinguish them by the DataProviderList. A special treatment is
 * reserved for a provider called BOARD_PROVIDER
 */
public interface DataProvider
{
	/**
	 * @return a name to (uniquely) identify a provider
	 */
	public String getName();
	
	public int getBoardSize();
	
	/**
	 * Get a data-object associated with coordinate x,y
	 * 
	 * @param x
	 * @param y
	 * 
	 * @return data-object
	 */
	public Number getData(int x,int y);
	
	/**
	 * Tell the provider to fill it's data-structures with data related to x,y
	 * 
	 * @param x
	 * @param y
	 */
	public void fillData(int x,int y);
	
	/**
	 * Describes what data this provider returns.
	 * 
	 * @return Class
	 */
	@SuppressWarnings("unchecked")
    public Class getDataClass();
	
	/**
	 * @return a flag whether this provider always has data available for each x,y coordinate
	 * or whether it needs to be told explicitly through fillData.
	 * Returns true by default.
	 */
	public boolean isFullProvider();
}
