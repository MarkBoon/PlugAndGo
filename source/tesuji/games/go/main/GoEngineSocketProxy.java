package tesuji.games.go.main;

import tesuji.games.general.MoveFactory;
import tesuji.games.go.common.GoEngine;
import tesuji.games.go.common.GoMove;
import tesuji.games.go.common.GoMoveFactory;
import tesuji.games.gtp.GameEngineSocketAdapter;

public class GoEngineSocketProxy
	extends GameEngineSocketAdapter<tesuji.games.go.common.GoMove>
	implements GoEngine
{
	/*
	 * (non-Javadoc)
	 * @see tesuji.games.gtp.GameEngineSocketAdapter#getMoveFactory()
	 */
	@Override
	public MoveFactory<GoMove> getMoveFactory()
	{
		return GoMoveFactory.getSingleton();
	}

	public String getJar()
	{
		return null;
	}

	public void setJar(String jarName)
	{
	}

}
