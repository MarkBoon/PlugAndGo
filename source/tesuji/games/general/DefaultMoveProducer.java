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

import java.util.ArrayList;

/**
	This is a default implementation of the MoveProducer interface.
	
	Classes that implement the MoveProducer interface usually need to do no more than
	delegating all the calls to an instance of this class.
*/

public class DefaultMoveProducer<MoveType extends Move>
	implements MoveProducer<MoveType>
{
	private ArrayList<MoveListener<MoveType>> moveListeners = new ArrayList<MoveListener<MoveType>>();

	/**
	 * @see tesuji.games.general.MoveProducer#addMoveListener(tesuji.games.general.MoveListener)
	 */
	public void addMoveListener(MoveListener<MoveType> listener)
	{
		if (!moveListeners.contains(listener))
			moveListeners.add(listener);
	}

    
	/**
	 * @see tesuji.games.general.MoveProducer#removeMoveListener(tesuji.games.general.MoveListener)
	 */
	public void removeMoveListener(MoveListener<MoveType> listener)
	{
		moveListeners.remove(listener);
	}


	/**
	 * @see tesuji.games.general.MoveProducer#notifyMove(tesuji.games.general.MoveEvent)
	 */
	public void notifyMove(MoveEvent<MoveType> event)
	{
		for (MoveListener<MoveType> listener : moveListeners)
			if (event.getSource()!=listener) // Don't loop the event around unecessarily
				listener.playMove(event);
	}
	
	/**
	 * @see tesuji.games.general.MoveProducer#notifyTakeBack()
	 */
	public void	notifyTakeBack()
	{
		for (MoveListener<MoveType> listener : moveListeners)
			listener.takeBack();
	}
	
	/**
		Notify all the MoveListeners that many moves will be played in a row.
		
		@param event
	*/
	public void notifyStartMultiple(MoveEvent<MoveType> event)
	{
		for (MoveListener<MoveType> listener : moveListeners)
			listener.startMultiple(event);
	}
	
	/**
		Notify all the MoveListeners that the last of many moves was played.
		
		@param event
	*/
	public void notifyStopMultiple(MoveEvent<MoveType> event)
	{
		for (MoveListener<MoveType> listener : moveListeners)
			listener.stopMultiple(event);
	}
}