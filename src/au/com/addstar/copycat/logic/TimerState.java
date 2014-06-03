package au.com.addstar.copycat.logic;

import au.com.addstar.copycat.GameBoard;

public abstract class TimerState extends State<GameBoard>
{
	protected long endTime = Long.MAX_VALUE;
	private long mLastNotifyTime = 0;
	
	@Override
	public void onTick( StateEngine<GameBoard> engine, GameBoard game )
	{
		long left = endTime - System.currentTimeMillis();
		
		if(left <= 0)
			onNotifyTimeLeft(0, engine, game);
		else
		{
			if(left >= 30000)
			{
				if(System.currentTimeMillis() - mLastNotifyTime >= 15000)
				{
					onNotifyTimeLeft(left, engine, game);
					mLastNotifyTime = System.currentTimeMillis();
				}
			}
			else if(left >= 10000)
			{
				if(System.currentTimeMillis() - mLastNotifyTime >= 5000)
				{
					onNotifyTimeLeft(left, engine, game);
					mLastNotifyTime = System.currentTimeMillis();
				}
			}
			else
			{
				if(System.currentTimeMillis() - mLastNotifyTime >= 1000)
				{
					onNotifyTimeLeft(left, engine, game);
					mLastNotifyTime = System.currentTimeMillis();
				}
			}
		}
	}
	
	protected abstract void onNotifyTimeLeft(long remaining, StateEngine<GameBoard> engine, GameBoard game );
}
