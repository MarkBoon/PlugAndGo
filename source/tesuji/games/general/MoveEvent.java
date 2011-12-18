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

import java.io.Serializable;

/**
 * Nothing more than a wrapper around a Move-object so that a 'source' object 
 * can be associated with it. This is used to prevent an event to keep looping 
 * around.
 */
public class MoveEvent<MoveType extends Move>
    implements Serializable
{
	private static final long serialVersionUID = 2183708471940378767L;
	
	private transient Object _eventSource;
	private MoveType _move;

	/**
	 * MoveEvent constructor
	 * 
	 * @param source
	 * @param move
	 */
	public MoveEvent(Object source, MoveType move)
	{
		_eventSource = source;
		_move = move;
	}
	
	/**
	 * @return the source where the event originated
	 * Note that this field is read-only.
	 */
	public Object getSource()
	{
		return _eventSource;
	}
	
	/**
	 * @return the _move this event was created for
	 */
	public MoveType getMove()
	{
		return _move;
	}
	
	/**
	 * @param move
	 */
	public void setMove(MoveType move)
	{
		_move = move;
	}
}