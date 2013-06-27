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
package tesuji.core.util;

/**
 * This aspect keeps track of FlyWeight objects that have been created by a
 * static factory class. It throws an IllegalstateException whenever an object
 * is referenced after it has been recycled to the factory.
 */
public aspect DanglingReferenceChecker
{
	private boolean FlyWeight.isInFactory = false;

	/**
	 * Note that for this aspect to work properly all instantations of the FlyWeight objects MUST happen in a method
	 * which name starts with 'create' in a separate class which name ends in 'Factory'.
	 */
	pointcut factoryCall() : call(* *.create*());
	pointcut methodCall() : call(* *.*(..));
	pointcut recycleCall() : call(* FlyWeight.recycle());

	/**
	 * After the factory call, set isInFactory to false.
	 */
	after() returning (FlyWeight flyWeightObject) : factoryCall()
	{
		flyWeightObject.isInFactory = false;
	}

	/**
	 * After the recycle call, set isInFactory to true.
	 */
	after(FlyWeight flyWeightObject) : target(flyWeightObject) && recycleCall()
	{
		if (flyWeightObject.isInFactory)
			throw new IllegalStateException();
		flyWeightObject.isInFactory = true;
	}

	/**
	 * Before accessing an object, first check that it's not in the factory.
	 */
	before(FlyWeight flyWeightObject) : target(flyWeightObject) && methodCall()
	{
		if (flyWeightObject.isInFactory)
			throw new IllegalStateException();
	}
}
