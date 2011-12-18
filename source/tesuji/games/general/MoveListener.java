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
 * Interface defining a MoveListener. These can be registered so that
 * whenever a move gets played the method playMove() is called or the
 * method takeBack() in case a move was taken back.<br>
 * <br>
 * In case a sequence of moves is expected startMultiple() is called before
 * the firstmove in the sequence is sent, and stopMultiple() is called
 * after the last move in the sequenceis sent. This allows the MoveListener
 * to queue the moves for improved efficiency.<br>
  */
public interface MoveListener<MoveType extends Move>
{
	/**
	 * Play a move
	 * 
	 * @param event
	 */
	void playMove(MoveEvent<MoveType> event);
	/**
	 * Take back a move
	 */
	void takeBack();
	/**
	 * Mark the start of a sequence of MoveEvents
	 * 
	 * @param event
	 */
	void startMultiple(MoveEvent<MoveType> event);
	/**
	 * Mark the end of a sequence of MoveEvents
	 * 
	 * @param event
	 */
	void stopMultiple(MoveEvent<MoveType> event);
}