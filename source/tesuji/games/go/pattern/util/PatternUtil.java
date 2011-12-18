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

package tesuji.games.go.pattern.util;

import tesuji.games.general.provider.DataProvider;
import tesuji.games.go.pattern.common.Pattern;
import tesuji.games.go.pattern.common.PatternCondition;
import tesuji.games.go.util.FourCursor;
import tesuji.games.model.BoardModel;
import tesuji.games.util.Point;

import static tesuji.games.general.ColorConstant.*;
import static tesuji.games.go.common.GoConstant.*;
import static tesuji.games.go.util.GoArray.*;

/**
 * 
 */
public class PatternUtil
{
	/**
	 * Adjust a 2-dimensional coordinate according to a certain orientation.
	 * This is the inverse operation of adjustInversedOrientation()
	 * One of these should be used when building the decision-tree for the patterns,
	 * the other to convert coordinates of the moves and conditions in the leaves.
	 * 
	 * @param x
	 * @param y
	 * @param orientation
	 * @param point
	 */
	public static void adjustOrientation(int x, int y, int orientation, Point point)
	{
		switch(orientation&7)
		{
			case 0:										// No change
				point.x = x;
				point.y = y;
				break;
			case 1:										// Clockwise rotation 270
				point.x = y;
				point.y = -x;
				break;
			case 2:										// Rotation of 180 degrees
				point.x = -x;
				point.y = -y;
				break;
			case 3:										// Clockwise rotation 90
				point.x = -y;
				point.y = x;
				break;
			case 4:										// Flip about vertical axis
				point.x = -x;
				point.y = y;
				break;
			case 5:										// Flip about diagonal
				point.x = y;
				point.y = x;
				break;
			case 6:										// Flip about horizontal axis
				point.x = x;
				point.y = -y;
				break;
			case 7:										// Flip about diagonal
				point.x = -y;
				point.y = -x;
				break;
		}
	}
	
	/**
	 * Adjust a 2-dimensional coordinate according to a certain orientation.
	 * This is the inverse operation of adjustOrientation()
	 * One of these should be used when building the decision-tree for the patterns,
	 * the other to convert coordinates of the moves and conditions in the leaves.
	 * 
	 * @param x
	 * @param y
	 * @param orientation
	 * @param point
	 */
	public static void adjustInversedOrientation(int x, int y, int orientation, Point point)
	{
		switch(orientation&7)
		{
			case 0:										// No change
				point.x = x;
				point.y = y;
				break;
			case 1:										// Clockwise rotation 90
				point.x = -y;
				point.y = x;
				break;
			case 2:										// Rotation of 180 degrees
				point.x = -x;
				point.y = -y;
				break;
			case 3:										// Clockwise rotation 270
				point.x = y;
				point.y = -x;
				break;
			case 4:										// Flip about vertical axis
				point.x = -x;
				point.y = y;
				break;
			case 5:										// Flip about diagonal
				point.x = y;
				point.y = x;
				break;
			case 6:										// Flip about horizontal axis
				point.x = x;
				point.y = -y;
				break;
			case 7:										// Flip about diagonal
				point.x = -y;
				point.y = -x;
				break;
		}
	}
	
	/**
	 * Create a n-by-n pattern based on board-data.
	 * 
	 * @param n
	 * @param xy
	 * @param color
	 * @param boardModel
	 * @param boardSize
	 * @param patternSet
	 */
	public static Pattern createPattern(int n, int xy, byte color, BoardModel boardModel, DataProvider libertyProvider, DataProvider rowProvider)
	{
		boolean leftEdge = 		false;
		boolean rightEdge =		false;
		boolean topEdge = 		false;
		boolean bottomEdge = 	false;
		int shiftMaskX = 0;
		int shiftMaskY = 0;
		int boardSize = boardModel.getBoardSize();
		
		int x = getX(xy);
		int y = getY(xy);
		
		int startX = x - n/2;
		int startY = y - n/2;
		
		int endX = x + n/2;
		int endY = y + n/2;
		
		if (startX<1)
		{
			shiftMaskX = startX - 1;
			startX = 1;
			leftEdge = true;
		}
		if (startY<1)
		{
			shiftMaskY = startY - 1;
			startY = 1;
			topEdge = true;
		}
		if (endX>boardSize)
		{
			endX = boardSize;
			rightEdge = true;
		}
		if (endY>boardSize)
		{
			endY = boardSize;
			bottomEdge = true;
		}
		
		byte[][] mask = createMask(n, xy, boardModel);
		
		Pattern newPattern = new Pattern();
		
		newPattern.setLeftEdge(leftEdge);
		newPattern.setTopEdge(topEdge);
		newPattern.setRightEdge(rightEdge);
		newPattern.setBottomEdge(bottomEdge);
		
		// Make 1x1 Pattern
		newPattern.removeBottomRow();
		newPattern.removeBottomRow();
		newPattern.removeLeftColumn();
		newPattern.removeLeftColumn();
		
		for (int i=1; i<n; i++)
			newPattern.addBottomRow();
		
		for (int j=1; j<n; j++)
			newPattern.addLeftColumn();
		
		// Fill the pattern
		for (int i=startY; i<=endY; i++)
		{
			for (int j=startX; j<=endX; j++)
			{
				int boardXY = toXY(j,i);
				if (boardXY>=FIRST && boardXY<=LAST && boardModel.get(boardXY)!=EDGE && mask[j-startX-shiftMaskX][i-startY-shiftMaskY]!=0)
				{
					byte boardValue = boardModel.get(boardXY);
					newPattern.setPoint(j-startX, i-startY, boardValue);
				}
			}
		}
//		System.out.println("Masked:");
//		System.out.print(newPattern);
//		System.out.println("\n");
		
		// Add liberty conditions around the center.
		if (libertyProvider!=null)
		{
			for (int i=x-1; i<=x+1; i++)
			{
				for (int j=y-1; j<=y+1; j++)
				{
					int libertyXY = toXY(i,j);
					if (boardModel.get(libertyXY)==BLACK || boardModel.get(libertyXY)==WHITE)
					{
						byte liberties = libertyProvider.getData(i, j).byteValue();
						if (liberties<=3)
						{
							PatternCondition condition =
								new PatternCondition(libertyProvider.getName(),i-x,j-y,PatternCondition.EQUALS,liberties);
							newPattern.getConditionList().add(condition);
						}
						else
						{
							PatternCondition condition =
								new PatternCondition(libertyProvider.getName(),i-x,j-y,PatternCondition.GT,(byte)3);
							newPattern.getConditionList().add(condition);						
						}
					}
				}
			}
		}
		
		// Add row condition
		/*byte row = rowProvider.getData(x,y).byteValue();
		if (row<5 && topEdge==false && bottomEdge==false && leftEdge==false && rightEdge==false)
		{
			PatternCondition condition =
				new PatternCondition(rowProvider.getName(),0,0,PatternCondition.EQUALS,row);
			newPattern.getConditionList().add(condition);			
		}*/
		
		if (color==BLACK)
		{
			newPattern.setBlackX(x-startX);
			newPattern.setBlackY(y-startY);
			newPattern.setBlackNrOccurrences(1);
			newPattern.setBlackNrSuccesses(1);
		}
		else
		{
			newPattern.setWhiteX(x-startX);
			newPattern.setWhiteY(y-startY);
			newPattern.setWhiteNrOccurrences(1);
			newPattern.setWhiteNrSuccesses(1);
		}
		newPattern.setStartX(x-startX);
		newPattern.setStartY(y-startY);
				
		// Create the 16 identical variations of the pattern.
		Pattern[] patternArray = new Pattern[16];
		patternArray[0] = newPattern;
		for (int orientation=0; orientation<8; orientation++)
			patternArray[orientation] = createPatternCopy(newPattern,orientation);
		
		for (int i=8; i<16; i++)
			patternArray[i] = invertPattern(patternArray[i-8]);
		
		// Shrink the data while there are empty rows or columns.
		for (int orientation=0; orientation<16; orientation++)
		{
			Pattern p = patternArray[orientation];
			while (p.removeTopRow());
			while (p.removeLeftColumn());
			while (p.removeBottomRow());
			while (p.removeRightColumn());
		}
		
		newPattern = patternArray[0];
		
		//System.out.println("Compare:");

		for (int i=0; i<16; i++)
		{
			//System.out.print(patternArray[i]);
			if (patternArray[i].rankBefore(newPattern))
				newPattern = patternArray[i];
		}
//		System.out.println("Selected:");
//		System.out.print(newPattern);
//		System.out.println("\n");
		
		assert (newPattern.getBlackX()==UNDEFINED_COORDINATE || (newPattern.getBlackX()==newPattern.getStartX() && newPattern.getBlackY()==newPattern.getStartY()));
		assert (newPattern.getWhiteX()==UNDEFINED_COORDINATE || (newPattern.getWhiteX()==newPattern.getStartX() && newPattern.getWhiteY()==newPattern.getStartY()));
//???		assert newPattern.getPoint(newPattern.getStartX(), newPattern.getStartY())==EMPTY : "\n"+newPattern.getPoint(newPattern.getStartX(), newPattern.getStartY())+"\n"+newPattern.toString()+"\n"+GoArray.printBoardToString(board);
		
//		for (Pattern pattern : patternSet.getPatternGroup().getPatternList())
//		{
//			if (newPattern.isSamePattern(pattern))
//				return;
//		}
		
		newPattern.setGenerated(true);
		newPattern.initOccurrences();
		
		return newPattern;
	}
	
	/**
	 * Create a copy of a pattern with a given orientation.
	 * 
	 * @param pattern
	 * @param orientation
	 * @return
	 */
	public static Pattern createPatternCopy(Pattern pattern, int orientation)
	{
		Point tmpPoint = Point.create();
		Pattern newPattern = new Pattern();
		newPattern.setGroupId(pattern.getGroupId());
		int size = (pattern.getWidth()>pattern.getHeight()? pattern.getWidth() : pattern.getHeight());
		
		int dx = pattern.getWidth()/2;
		int dy = pattern.getHeight()/2;
		
		for (int i=0; i<size-3; i++)
			newPattern.addLeftColumn();
		for (int i=0; i<size-3; i++)
			newPattern.addBottomRow();
		
		for (int x=0; x<pattern.getWidth(); x++)
		{
			for (int y=0; y<pattern.getHeight(); y++)
			{
				byte value = pattern.getPoint(x, y);
				adjustOrientation(x-dx, y-dy, orientation, tmpPoint);
				newPattern.setPoint(tmpPoint.x+dx, tmpPoint.y+dy, value);
			}
		}
		newPattern.setBlackNrOccurrences(pattern.getBlackNrOccurrences());
		newPattern.setWhiteNrOccurrences(pattern.getWhiteNrOccurrences());
		newPattern.setBlackNrSuccesses(pattern.getBlackNrSuccesses());
		newPattern.setWhiteNrSuccesses(pattern.getWhiteNrSuccesses());

		if (pattern.getBlackX()!=UNDEFINED_COORDINATE)
		{
			adjustOrientation(pattern.getBlackX()-dx, pattern.getBlackY()-dy, orientation, tmpPoint);
			newPattern.setBlackX(tmpPoint.x+dx);
			newPattern.setBlackY(tmpPoint.y+dy);
		}
		if (pattern.getWhiteX()!=UNDEFINED_COORDINATE)
		{
			adjustOrientation(pattern.getWhiteX()-dx, pattern.getWhiteY()-dy, orientation, tmpPoint);
			newPattern.setWhiteX(tmpPoint.x+dx);
			newPattern.setWhiteY(tmpPoint.y+dy);
		}
		newPattern.setStartX(tmpPoint.x+dx);
		newPattern.setStartY(tmpPoint.y+dy);
		
		switch(orientation)
		{
		case 0:
			newPattern.setLeftEdge(pattern.hasLeftEdge());
			newPattern.setTopEdge(pattern.hasTopEdge());
			newPattern.setBottomEdge(pattern.hasBottomEdge());
			newPattern.setRightEdge(pattern.hasRightEdge());
			break;
		case 1:
			newPattern.setLeftEdge(pattern.hasTopEdge());
			newPattern.setTopEdge(pattern.hasRightEdge());
			newPattern.setBottomEdge(pattern.hasLeftEdge());
			newPattern.setRightEdge(pattern.hasBottomEdge());
			break;
		case 2:
			newPattern.setLeftEdge(pattern.hasRightEdge());
			newPattern.setTopEdge(pattern.hasBottomEdge());
			newPattern.setBottomEdge(pattern.hasTopEdge());
			newPattern.setRightEdge(pattern.hasLeftEdge());
			break;
		case 3:
			newPattern.setLeftEdge(pattern.hasBottomEdge());
			newPattern.setTopEdge(pattern.hasLeftEdge());
			newPattern.setBottomEdge(pattern.hasRightEdge());
			newPattern.setRightEdge(pattern.hasTopEdge());
			break;
		case 4:
			newPattern.setLeftEdge(pattern.hasRightEdge());
			newPattern.setTopEdge(pattern.hasTopEdge());
			newPattern.setBottomEdge(pattern.hasBottomEdge());
			newPattern.setRightEdge(pattern.hasLeftEdge());
			break;
		case 5:
			newPattern.setLeftEdge(pattern.hasTopEdge());
			newPattern.setTopEdge(pattern.hasLeftEdge());
			newPattern.setBottomEdge(pattern.hasRightEdge());
			newPattern.setRightEdge(pattern.hasBottomEdge());
			break;
		case 6:
			newPattern.setLeftEdge(pattern.hasLeftEdge());
			newPattern.setTopEdge(pattern.hasBottomEdge());
			newPattern.setBottomEdge(pattern.hasTopEdge());
			newPattern.setRightEdge(pattern.hasRightEdge());
			break;
		case 7:
			newPattern.setLeftEdge(pattern.hasBottomEdge());
			newPattern.setTopEdge(pattern.hasRightEdge());
			newPattern.setBottomEdge(pattern.hasLeftEdge());
			newPattern.setRightEdge(pattern.hasTopEdge());
			break;
		}
		
		for (PatternCondition condition : pattern.getConditionList())
			newPattern.getConditionList().add(createPatternConditionCopy(condition,orientation));
		
		tmpPoint.recycle();
		return newPattern;
	}

	public static PatternCondition createPatternConditionCopy(PatternCondition condition, int orientation)
	{
		Point tmpPoint = Point.create();
		adjustOrientation(condition.getX(), condition.getY(), orientation, tmpPoint);
		PatternCondition newCondition = new PatternCondition(condition.getDataProviderName(),tmpPoint.x,tmpPoint.y,condition.getOperation(),condition.getValue());
		tmpPoint.recycle();
		return newCondition;
	}
	
	private static Pattern invertPattern(Pattern pattern)
	{
		Pattern newPattern = new Pattern();
		newPattern.setGroupId(pattern.getGroupId());
		
		for (int i=0; i<pattern.getWidth()-3; i++)
			newPattern.addLeftColumn();
		for (int i=0; i<pattern.getHeight()-3; i++)
			newPattern.addBottomRow();
		
		for (int x=0; x<pattern.getWidth(); x++)
		{
			for (int y=0; y<pattern.getHeight(); y++)
			{
				byte value = pattern.getPoint(x, y);
				if (value==BLACK)
					value = WHITE;
				else if (value==WHITE)
					value = BLACK;
				newPattern.setPoint(x, y, value);
			}
		}
		newPattern.setBlackNrOccurrences(pattern.getBlackNrOccurrences());
		newPattern.setWhiteNrOccurrences(pattern.getWhiteNrOccurrences());
		newPattern.setBlackNrSuccesses(pattern.getBlackNrSuccesses());
		newPattern.setWhiteNrSuccesses(pattern.getWhiteNrSuccesses());
		
		newPattern.setBlackX(pattern.getWhiteX());
		newPattern.setBlackY(pattern.getWhiteY());
		newPattern.setWhiteX(pattern.getBlackX());
		newPattern.setWhiteY(pattern.getBlackY());
		newPattern.setStartX(pattern.getStartX());
		newPattern.setStartY(pattern.getStartY());
				
		newPattern.setLeftEdge(pattern.hasLeftEdge());
		newPattern.setTopEdge(pattern.hasTopEdge());
		newPattern.setBottomEdge(pattern.hasBottomEdge());
		newPattern.setRightEdge(pattern.hasRightEdge());

		for (PatternCondition condition : pattern.getConditionList())
			newPattern.getConditionList().add(createPatternConditionCopy(condition,0));
		
		return newPattern;
	}
	private static byte[][] createMask(int n, int startXY, BoardModel boardModel)
	{
		byte[][] mask = new byte[n][n];
		int offsetX = n/2;
		int offsetY = n/2;
		mask[offsetX][offsetY] = 1;
		mask[offsetX-1][offsetY] = 2;
		mask[offsetX+1][offsetY] = 2;
		mask[offsetX][offsetY-1] = 2;
		mask[offsetX][offsetY+1] = 2;
		
		if (boardModel.get(left(startXY))==boardModel.get(above(startXY)) || boardModel.get(left(startXY))==EMPTY || boardModel.get(above(startXY))==EMPTY)
			mask[offsetX-1][offsetY-1] = 3;
		if (boardModel.get(above(startXY))==boardModel.get(right(startXY)) || boardModel.get(above(startXY))==EMPTY || boardModel.get(right(startXY))==EMPTY)
			mask[offsetX+1][offsetY-1] = 3;
		if (boardModel.get(right(startXY))==boardModel.get(below(startXY)) || boardModel.get(right(startXY))==EMPTY || boardModel.get(below(startXY))==EMPTY)
			mask[offsetX+1][offsetY+1] = 3;
		if (boardModel.get(below(startXY))==boardModel.get(left(startXY)) || boardModel.get(below(startXY))==EMPTY || boardModel.get(left(startXY))==EMPTY)
			mask[offsetX-1][offsetY+1] = 3;
		
		for (byte i=3; i<=n; i++)
		{
			for (int row=0; row<n; row++)
			{
				for (int column=0; column<n; column++)
				{
					if (mask[column][row]==i-1)
					{
						int xy = startXY + toXY(column-offsetX,row-offsetY);
						if (xy>=FIRST && xy<=LAST && boardModel.get(xy)==EMPTY)
						{
							int nrNeighbours = 0;
							for (int k=0; k<4; k++)
							{
								int next = FourCursor.getNeighbour(xy, k);
								if (boardModel.get(next)!=EMPTY  && boardModel.get(next)!=EDGE)
								{
									int x = offsetX+getX(next)-getX(startXY);
									int y = offsetY+getY(next)-getY(startXY);
									if (x>=0 && x<n && y>=0 && y<n)
									{
										nrNeighbours++;
										if (mask[x][y]==0)
											mask[x][y] = i;
									}
								}
							}
							if (nrNeighbours<2)
							{
								for (int k=0; k<4; k++)
								{
									int next = FourCursor.getNeighbour(xy, k);
									if (boardModel.get(next)==EMPTY)
									{
										int x = offsetX+getX(next)-getX(startXY);
										int y = offsetY+getY(next)-getY(startXY);
										if (x>=0 && x<n && y>=0 && y<n && mask[x][y]==0)
											mask[x][y] = i;
									}
								}								
							}
							if (mask[column][row]==2 || mask[column][row]==3)
							{
								if (column>0 && row>0 && boardModel.get(left(xy))!=EMPTY && boardModel.get(left(xy))==boardModel.get(above(xy)) && boardModel.get(left(above(xy)))==opposite(boardModel.get(left(xy))) && mask[column-1][row-1]==0)
									mask[column-1][row-1] = (byte)(i+2);
								if (column<n-1 && row>0 && boardModel.get(above(xy))!=EMPTY && boardModel.get(above(xy))==boardModel.get(right(xy))&& boardModel.get(above(right(xy)))==opposite(boardModel.get(above(xy))) && mask[column+1][row-1]==0)
									mask[column+1][row-1] = (byte)(i+2);
								if (column<n-1 && row<n-1 && boardModel.get(right(xy))!=EMPTY && boardModel.get(right(xy))==boardModel.get(below(xy))&& boardModel.get(right(below(xy)))==opposite(boardModel.get(right(xy))) && mask[column+1][row+1]==0)
									mask[column+1][row+1] = (byte)(i+2);
								if (column>0 && row<n-1 && boardModel.get(below(xy))!=EMPTY && boardModel.get(below(xy))==boardModel.get(left(xy)) && boardModel.get(below(left(xy)))==opposite(boardModel.get(below(xy))) && mask[column-1][row+1]==0)
									mask[column-1][row+1] = (byte)(i+2);
								
								
							}
						}
					}
				}
			}
//			print(mask);
		}
		
		for (int row=0; row<n; row++)
		{
			for (int column=0; column<n; column++)
			{
				if (mask[column][row]!=0 && mask[column][row]<n-2)
				{
					int xy = startXY + toXY(column-offsetX,row-offsetY);
					if (xy>=FIRST && xy<=LAST && boardModel.get(xy)==EMPTY)
					{
						for (int k=0; k<4; k++)
						{
							int next = FourCursor.getNeighbour(xy, k);
							if (boardModel.get(next)!=EMPTY && boardModel.get(next)!=EDGE)
							{
								int x = offsetX+getX(next)-getX(startXY);
								int y = offsetY+getY(next)-getY(startXY);
								if (x>=0 && x<n && y>=0 && y<n && mask[x][y]==0)
									mask[x][y] = (byte)n;
							}
						}
					}
				}
			}
		}
//		print(mask);
		
		return mask;
	}
	
	@SuppressWarnings("unused")
	private static void print(byte[][] mask)
	{
		for (int row=0; row<mask.length; row++)
		{
			StringBuilder builder = new StringBuilder();
			for (int column=0; column<mask.length; column++)
				builder.append(""+mask[column][row]);
			System.out.println(builder.toString());
		}
		System.out.println();
	}
}
