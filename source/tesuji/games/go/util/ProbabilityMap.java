package tesuji.games.go.util;

import java.util.Arrays;

import tesuji.core.util.MersenneTwisterFast;

public class ProbabilityMap
{
	private static final double DEFAULT = 0.001;
	private static MersenneTwisterFast _random = new MersenneTwisterFast(System.nanoTime());

	private double[] _weights;
	private double[] _columnSum;
	private double[] _rowSum;
	private double _total;
	
	public ProbabilityMap()
	{
		_weights = GoArray.createDoubles();
		_columnSum = new double[GoArray.WIDTH];
		_rowSum = new double[GoArray.WIDTH];
		
		for (int i=GoArray.FIRST; i<GoArray.LAST; i++)
			add(i,DEFAULT);
	}
	
	public double getWeight(int xy)
	{
		return _weights[xy];
	}

	public void add(int xy, double weight)
	{
		int x = GoArray.getX(xy);
		int y = GoArray.getX(xy);
		_weights[xy] += weight;
		_columnSum[x] += weight;
		_rowSum[y] += weight;
		_total += weight;
	}

	public void add(int xy)
	{
		add(xy,DEFAULT);
	}
	
	public void subtract(int xy, double weight)
	{
		int x = GoArray.getX(xy);
		int y = GoArray.getX(xy);
		_weights[xy] -= weight;
		_columnSum[x] -= weight;
		_rowSum[y] -= weight;
		_total -= weight;
	}

	public void reset(int xy)
	{
		int x = GoArray.getX(xy);
		int y = GoArray.getX(xy);
		double weight = _weights[xy];
		_weights[xy] = 0.0;
		_columnSum[x] -= weight;
		_rowSum[y] -= weight;
		_total -= weight;
	}

	public void reset()
	{
		GoArray.clear(_weights);
		Arrays.fill(_columnSum,0);
		Arrays.fill(_rowSum,0);
		_total = 0.0;
	}
	
	public int getCoordinate()
	{
		double columnValue = _random.nextDouble() * _total;
		double rowValue = _random.nextDouble() * _total;
		int x = 0;
		int y = 0;
		double tmpColumnSum = 0.0;
		while (tmpColumnSum<=columnValue)
			tmpColumnSum += _columnSum[++x];
		double tmpRowSum = 0.0;
		while (tmpRowSum<=rowValue)
			tmpRowSum += _rowSum[++x];
		
		return GoArray.toXY(x, y);
	}
	
	public void copyFrom(ProbabilityMap source)
	{
		System.arraycopy(source._weights,0,_weights,0,GoArray.MAX);
		System.arraycopy(source._columnSum,0,_columnSum,0,GoArray.WIDTH);
		System.arraycopy(source._rowSum,0,_rowSum,0,GoArray.WIDTH);
		_total = source._total;
	}
}
