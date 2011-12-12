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

package tesuji.games.go.monte_carlo;

import tesuji.games.go.common.GoMove;
import tesuji.games.go.common.Util;
import tesuji.games.go.util.FourCursor;
import tesuji.games.go.util.SGFUtil;

import static tesuji.games.general.ColorConstant.*;
import static tesuji.games.go.util.GoArray.*;
import static tesuji.games.go.common.GoConstant.*;

/**
 * 
 */
public class MCPseudoLibertyAdministration
	extends AbstractMonteCarloAdministration
{
	public MCPseudoLibertyAdministration()
	{
		super();
	}

	public boolean isLegal(int xy)
	{
		assert _boardModel.get(xy)==EMPTY : SGFUtil.createSGF(getMoveStack());

		
		if (_otherNeighbours[xy]!=4)
			return true;
		if (xy==_koPoint)
			return false;

		boolean result = false;
		for (int n=0; n<4; n++)
		{
			if (--_liberties[_chain[FourCursor.getNeighbour(xy, n)]]==0)
				result = true;
		}
		for (int n=0; n<4; n++)
		{
			_liberties[_chain[FourCursor.getNeighbour(xy, n)]]++;
		}
		
		return result;
	}
	
	public void play(int xy)
	{		
//		_nrMovesPlayed++;
		
		if (xy!=PASS)
		{
			assert isLegal(xy) : "Illegal move at "+Util.printCoordinate(xy);
			
			_boardModel.set(xy, _colorToPlay);
			_chain[xy] = xy;
			_chainNext[xy] = xy;
			
			_emptyPoints.remove(xy);
			
			_liberties[xy] = 4 -_neighbours[xy];
			
			addStone(xy);

			boolean merged = false;
			for (int n=0; n<4; n++)
			{
				int next = FourCursor.getNeighbour(xy, n);
				byte neighbour = _boardModel.get(next);
				if (neighbour==_oppositeColor)
				{
					_liberties[_chain[next]]--;
					if (_liberties[_chain[next]]==0)
					{
						if (_ownNeighbours[next]==4 && _otherNeighbours[xy]==4)
							_koPoint = next;
						removeCapturedChain(_oppositeColor,next);
					}
				}
				else if (neighbour==_colorToPlay)
				{
					_liberties[_chain[next]]--;
					if (!merged)
					{
						merged = true;
						_liberties[_chain[next]] += _liberties[xy];
						_chain[xy] = _chain[next];
						_chainNext[xy] = _chainNext[next];
						_chainNext[next] = xy;
					}
					else if (_chain[next]!=_chain[xy])
					{
						_liberties[_chain[xy]] += _liberties[_chain[next]];
						int mergeLocation = next;
						do
						{
							_chain[mergeLocation] = _chain[xy];
							mergeLocation = _chainNext[mergeLocation];
						}
						while (mergeLocation!=next);
						int temp = _chainNext[xy];
						_chainNext[xy] = _chainNext[next];
						_chainNext[next] = temp;
					}
				}
			}

			assert _liberties[_chain[xy]]==countPseudoLiberties(xy) : SGFUtil.createSGF(getMoveStack());
			
			if (_liberties[_chain[xy]]==0)
				removeCapturedChain(_colorToPlay,xy);			
		}
		
		_oppositeColor = _colorToPlay;
		_colorToPlay = opposite(_colorToPlay);
		
		setNeighbourArrays();
		
		assert isLibertiesConsistent() : SGFUtil.createSGF(getMoveStack());
	}

	public MonteCarloAdministration<GoMove> createClone()
	{
		MCPseudoLibertyAdministration clone = new MCPseudoLibertyAdministration();
		clone.setBoardSize(getBoardSize());
		clone.setKomi(getKomi());
		
		clone.copyDataFrom(this);
		
		return clone;
	}

	private void removeCapturedChain(byte color, int xy)
	{
		assert !hasLiberty(xy);
		
		int captive = xy;
		do
		{
			assert _boardModel.get(captive)==color;
			
			_boardModel.set(captive, EMPTY);
			_chain[captive] = 0;
			
			_liberties[_chain[left(captive)]]++;
			_liberties[_chain[right(captive)]]++;
			_liberties[_chain[above(captive)]]++;
			_liberties[_chain[below(captive)]]++;
			
			removeStone(captive);
			_emptyPoints.add(captive);
			
			captive = _chainNext[captive];
		}
		while (captive!=xy);
	}
	    
    /**
     * This is for verification purposes.
     * 
     * @param xy
     * @return
     */
    public int countPseudoLiberties(int xy)
    {
    	int nrLiberties = 0;
		int stone = xy;
		if (_boardModel.get(xy)==EMPTY)
			return 0;
		
		do
		{
			if (_boardModel.get(left(stone))==EMPTY)
				nrLiberties++;
			if (_boardModel.get(right(stone))==EMPTY)
				nrLiberties++;
			if (_boardModel.get(above(stone))==EMPTY)
				nrLiberties++;
			if (_boardModel.get(below(stone))==EMPTY)
				nrLiberties++;
			
			stone = _chainNext[stone];
		}
		while (stone!=xy);

		return nrLiberties;
    }
    
    /**
     * This is for verification purposes.
     * 
     * @return
     */
    private boolean isLibertiesConsistent()
    {
    	for (int i=FIRST; i<=LAST; i++)
    		if ((_boardModel.get(i)==BLACK || _boardModel.get(i)==WHITE) && _liberties[_chain[i]]!=countPseudoLiberties(i))
    			return false;
    	
    	return true;
    }
}
