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
package tesuji.games.model;

/**
 * This interface describes a 2-dimensional array that is square.
 * The coordinates start at 1, so the valid range is [1..boardSize][1..boardSize]
 * That leaves an undefined edge around the board, which contain MIN_VALUE
 * 
 * There are also methods to get and set the values with a one-dimensional coordinate.
 */
public interface BoardArray
{
    /**
     * @return the board-size.  Note that the board is square.
     */
    public int getBoardSize();
    
    /**
     *  Get the 'value' or 'piece' on the board at the given coordinate.
     * 
     * @param x
     * @param y
     * 
     * @return the value of the piece or EMPTY.
     */
    public byte get(int x, int y);
    
    /**
     *  Get the 'value' or 'piece' on the board at the given coordinate.
     * 
     * @param xy - one-dimensional coordinate
     * 
     * @return the value of the piece or EMPTY.
     */
    public byte get(int xy);
    
    /**
     *  Set the 'value' or 'piece' on the board at the given coordinate.
     * 
     * @param x
     * @param y
     * 
     * @param value of the piece or EMPTY
     */
    public void set(int x, int y, byte value);
    
    /**
     *  Set the 'value' or 'piece' on the board at the given coordinate.
     * 
     * @param xy - one-dimensional coordinate
     * 
     * @param value of the piece or EMPTY
     */
    public void set(int xy, byte value);
    
    /**
     *	Get the one-dimensional array underlying this BoardModel
     *
     * @return byte[]
     */
    public byte[] getSingleArray();
}
