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

import tesuji.games.model.BoardModel;

/**
 * Definition of an interface for general game playing engines.
 * This is designed to match the (minimum) GTP protocol but the Go-related
 * definitions have been left out and are in GoEngine
 */
public interface GameEngine<MoveType extends Move>
	extends MoveGenerator<MoveType>, Evaluator<MoveType, Double>
{
	// Administrative commands
	String getEngineName();
	String getEngineVersion();
	void quit();
	
	// Setup commands
	void set(String propertyName, String propertyValue);
	void clearBoard();
	void setProperties(GameProperties properties);
	
	// Core play commands
	void playMove(MoveType move);
	void takeBack();
	MoveType generateMove(byte color);
	
	// Tournament commands
	void setTimeConstraints(int mainTime, int byoYomiTime, int nrByoYomiStones);
	void setTimeLeft(byte color, int timeRemaining, int nrStonesRemaining);
	String getFinalScore();
	
	// Development commands
	void setup(Iterable<MoveType> moveList);
	void setup(String[] diagram);
	Double getScore();	
	MoveType requestMove(byte color);
	Iterable<MoveType> requestCandidates(int n);
	
	/**
	 * @return the factory type that is used to create moves for MoveType
	 */
	MoveFactory<MoveType> getMoveFactory();
	
	/**
	 * @return the BoardModel as kept by the engine.
	 */
	BoardModel getBoardModel();
}
