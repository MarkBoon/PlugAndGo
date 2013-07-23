package tesuji.games.general;

import java.util.Iterator;

//import tesuji.core.util.FlyWeight;

public interface MoveIterator<MoveType extends Move>
	extends Iterator<MoveType>//, FlyWeight // Not created by factory...
{
	void recycle();
}
