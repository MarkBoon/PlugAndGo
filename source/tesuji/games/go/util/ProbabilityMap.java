package tesuji.games.go.util;

import java.util.Arrays;

import tesuji.core.util.MersenneTwisterFast;

public class ProbabilityMap
{
	public static final double ERROR_MARGIN = 0.000000001;
	private static final double DEFAULT = Math.sqrt(ERROR_MARGIN);
	private MersenneTwisterFast _random;

	private double[] _weights;
	private double[] _rowSum;
	private double _total;
	
	public ProbabilityMap(MersenneTwisterFast randomNumberGenerator)
	{
		_random = randomNumberGenerator;
		_weights = GoArray.createDoubles();
		_rowSum = new double[GoArray.WIDTH];
		
		reset();
	}
	
	public double getWeight(int xy)
	{
		return _weights[xy];
	}

	public void add(int xy, double weight)
	{
		int y = GoArray.getY(xy);
		_weights[xy] += weight;
		_rowSum[y] += weight;
		_total += weight;
		assert(isConsistent());
	}

	public void add(int xy)
	{
		add(xy,DEFAULT);
	}
	
	public void add(int xy, int urgency)
	{
		add(xy,urgency*DEFAULT);
	}
	
	public void subtract(int xy, double weight)
	{
		int y = GoArray.getY(xy);
		_weights[xy] -= weight;
		_rowSum[y] -= weight;
//		assert(_rowSum[y]>=0.0);
		_total -= weight;
//		assert(_total>=0.0);
		assert(isConsistent());
	}

	public void subtract(int xy, int urgency)
	{
		subtract(xy,urgency*DEFAULT);
	}

	public void reset(int xy)
	{
		int y = GoArray.getY(xy);
		double weight = _weights[xy];
		_weights[xy] = 0.0;
		_rowSum[y] -= weight;
//		assert(_rowSum[y]>=0.0);
		_total -= weight;
//		assert(_total>=0.0);
	}

	public void reset()
	{
		GoArray.clear(_weights);
		Arrays.fill(_rowSum,0.0);
		_total = 0.0;
	}
	
	public void decay(int xy)
	{
		double weight = _weights[xy];
		if (weight!=DEFAULT && weight!=0)
			subtract(xy,_weights[xy]/2);
	}
	
	public int getCoordinate()
	{
		double randomValue = _random.nextDouble() * _total;
		assert( randomValue<_total);
		int y = 0;
		double tmpSum = 0.0;
		while (tmpSum<=randomValue)
			tmpSum += _rowSum[++y];
		
		assert(y<GoArray.WIDTH);
		
		tmpSum -= _rowSum[y];
		int xy = y * GoArray.WIDTH;
		while (tmpSum<=randomValue)
			tmpSum += _weights[++xy];
		
		assert(_weights[xy]!=0.0);
		return xy;
	}
	
	public void copyFrom(ProbabilityMap source)
	{
		System.arraycopy(source._weights,0,_weights,0,GoArray.MAX);
		System.arraycopy(source._rowSum,0,_rowSum,0,GoArray.WIDTH);
		_total = source._total;
	}
	
	public boolean hasPoints()
	{
		return _total > ERROR_MARGIN;
	}
	
	public boolean isConsistent()
	{
		double total = 0.0;
		double rowTotal = 0.0;
		
		for (int i=GoArray.FIRST; i<GoArray.LAST; i++)
		{
			total += _weights[i];
			assert(_weights[i]>=0.0);
		}
		for (int i=0; i<GoArray.WIDTH; i++)
		{
			rowTotal += _rowSum[i];
			assert(_rowSum[i]>-ERROR_MARGIN);
		}
		
		assert(Math.abs(_total-total)<ERROR_MARGIN);
		assert(Math.abs(_total-rowTotal)<ERROR_MARGIN);
		
		return true;
	}
}
