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

/**
 * Interface of a move-producing class.
 * 
 * That means MoveListener objects can register to get notified whenever a move
 * was played or taken back
 */
public interface MoveProducer<MoveType extends Move>
{
	/**
	 * Register a MoveListener to get notified of moves played
	 * 
	 * @param listener
	 */
	public void addMoveListener(MoveListener<MoveType> listener);
	/**
	 * Remove a MoveListener so itwon't get notified anymore.
	 * 
	 * @param listener
	 */
	public void removeMoveListener(MoveListener<MoveType> listener);
	/**
	 * Notify all the registered MoveListeners that a move was played
	 * 
	 * @param event
	 */
	public void notifyMove( MoveEvent<MoveType> event );
	/**
	 * Notify all the registered MoveListeners that a move was taken back
	 */
	public void notifyTakeBack();    
}