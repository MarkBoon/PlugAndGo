package tesuji.games.go.util;

import java.text.DecimalFormat;
import java.util.Arrays;

import tesuji.core.util.MersenneTwisterFast;

import static tesuji.games.general.ColorConstant.BLACK;

public class ProbabilityMap
{
	private static final  DecimalFormat df = new DecimalFormat("##.##");
	
	public static final double ERROR_MARGIN = 0.000000001;
	public static final double DEFAULT = 1.0;
//	public static final double DEFAULT = Math.sqrt(ERROR_MARGIN);
	private MersenneTwisterFast _random;

	private double[][] _weights;
	private double[][] _rowSum;
	private double[] _total;
	
	public ProbabilityMap(MersenneTwisterFast randomNumberGenerator)
	{
		_random = randomNumberGenerator;
		_weights = new double[2][];
		_weights[0] = GoArray.createDoubles();
		_weights[1] = GoArray.createDoubles();
		_rowSum = new double[2][];
		_rowSum[0] = new double[GoArray.WIDTH];
		_rowSum[1] = new double[GoArray.WIDTH];
		_total = new double[2];

		reset();
	}
	
	public double getWeight(int xy, byte color)
	{
		
		return (color==BLACK)?_weights[0][xy]:_weights[1][xy];
	}

	public void add(int xy, double weight)
	{
		int y = GoArray.getY(xy);
		_weights[0][xy] += weight;
		_weights[1][xy] += weight;
		_rowSum[0][y] += weight;
		_rowSum[1][y] += weight;
		_total[0] += weight;
		_total[1] += weight;
		if (_weights[0][xy]<-ERROR_MARGIN)
			throw new IllegalStateException();
		if (_weights[1][xy]<-ERROR_MARGIN)
			throw new IllegalStateException();
		assert(isConsistent());
	}

	public void add(int xy, double weight, byte color)
	{
		int i = (color==BLACK)? 0 : 1;
		int y = GoArray.getY(xy);
		_weights[i][xy] += weight;
		_rowSum[i][y] += weight;
		_total[i] += weight;
		if (_weights[i][xy]<-ERROR_MARGIN)
			throw new IllegalStateException();
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
		_weights[0][xy] -= weight;
		_weights[1][xy] -= weight;
		_rowSum[0][y] -= weight;
		_rowSum[1][y] -= weight;
//		assert(_rowSum[y]>=0.0);
		_total[0] -= weight;
		_total[1] -= weight;
//		assert(_total>=0.0);
		if (_weights[0][xy]<-ERROR_MARGIN)
			throw new IllegalStateException();
		if (_weights[1][xy]<-ERROR_MARGIN)
			throw new IllegalStateException();
		assert(isConsistent());
	}
	
	public void subtract(int xy, double weight, byte color)
	{
		int i = (color==BLACK)? 0 : 1;
		int y = GoArray.getY(xy);
		_weights[i][xy] -= weight;
		_rowSum[i][y] -= weight;
//		assert(_rowSum[y]>=0.0);
		_total[i] -= weight;
//		assert(_total>=0.0);
//		assert(_weights[i][xy]>=0.0);
		if (_weights[i][xy]<-ERROR_MARGIN)
			throw new IllegalStateException();
		assert(isConsistent());
	}

	public void subtract(int xy, int urgency)
	{
		subtract(xy,urgency*DEFAULT);
	}

	public void clear(int xy)
	{
		int y = GoArray.getY(xy);
		double weight0 = _weights[0][xy];
		double weight1 = _weights[1][xy];
		_weights[0][xy] = 0.0;
		_weights[1][xy] = 0.0;
		_rowSum[0][y] -= weight0;
		_rowSum[1][y] -= weight1;
//		assert(_rowSum[y]>=0.0);
		_total[0] -= weight0;
		_total[1] -= weight1;
//		assert(_total>=0.0);
	}

	public void clear(int xy, byte color)
	{
		int i = (color==BLACK)? 0 : 1;
		int y = GoArray.getY(xy);
		double weight = _weights[i][xy];
		_weights[i][xy] = 0.0;
		_rowSum[i][y] -= weight;
//		assert(_rowSum[y]>=0.0);
		_total[i] -= weight;
//		assert(_total>=0.0);
	}

	public void reset(int xy, byte color)
	{
		int i = (color==BLACK)? 0 : 1;
		int y = GoArray.getY(xy);
		double weight = _weights[i][xy];
		_weights[i][xy] = DEFAULT;
		_rowSum[i][y] -= weight-DEFAULT;
//		assert(_rowSum[y]>=0.0);
		_total[i] -= weight-DEFAULT;
//		assert(_total>=0.0);
	}

	public void reset()
	{
		GoArray.clear(_weights[0]);
		GoArray.clear(_weights[1]);
		Arrays.fill(_rowSum[0],0.0);
		Arrays.fill(_rowSum[1],0.0);
		_total[0] = 0.0;
		_total[1] = 0.0;
	}
	
	public int getCoordinate(byte color)
	{
		int i = (color==BLACK)? 0 : 1;
		double randomValue = _random.nextDouble() * _total[i];
		assert( randomValue<_total[i]);
		int y = 0;
		double tmpSum = 0.0;
		while (tmpSum<=randomValue)
			tmpSum += _rowSum[i][++y];
		
		assert(y<GoArray.WIDTH);
		
		tmpSum -= _rowSum[i][y];
		int xy = y * GoArray.WIDTH;
		while (tmpSum<=randomValue)
			tmpSum += _weights[i][++xy];
		
		assert(_weights[i][xy]!=0.0);
		return xy;
	}
	
	public void copyFrom(ProbabilityMap source)
	{
		System.arraycopy(source._weights[0],0,_weights[0],0,GoArray.MAX);
		System.arraycopy(source._weights[1],0,_weights[1],0,GoArray.MAX);
		System.arraycopy(source._rowSum[0],0,_rowSum[0],0,GoArray.WIDTH);
		System.arraycopy(source._rowSum[1],0,_rowSum[1],0,GoArray.WIDTH);
		_total[0] = source._total[0];
		_total[1] = source._total[1];
	}
	
//	public boolean hasPoints()
//	{
//		return _total > ERROR_MARGIN;
//	}
	
	public boolean isConsistent()
	{
//		double total = 0.0;
//		double rowTotal = 0.0;
//		
//		for (int i=GoArray.FIRST; i<=GoArray.LAST; i++)
//		{
//			total += _weights[i];
//			assert(_weights[i]>=0.0);
//		}
//		for (int i=0; i<GoArray.WIDTH; i++)
//		{
//			rowTotal += _rowSum[i];
//			assert(_rowSum[i]>-ERROR_MARGIN);
//		}
//		
//		assert(Math.abs(_total-total)<ERROR_MARGIN);
//		assert(Math.abs(_total-rowTotal)<ERROR_MARGIN);
		
		return true;
	}
	
	public String toString()
	{
		StringBuilder out = new StringBuilder();
		out.append('\n');
		for (int row=1; row<GoArray.WIDTH; row++)
		{
			for (int col=1; col<GoArray.WIDTH; col++)
			{
				int xy = GoArray.toXY(col, row);
				out.append(df.format(_weights[0][xy]));
				out.append('\t');
			}
			out.append("\t\t");
			for (int col=1; col<GoArray.WIDTH; col++)
			{
				int xy = GoArray.toXY(col, row);
				out.append(df.format(_weights[1][xy]));
				out.append('\t');
			}
			out.append('\n');
		}
		out.append('\n');
		return out.toString();
	}
}
