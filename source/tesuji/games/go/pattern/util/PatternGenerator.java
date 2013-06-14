package tesuji.games.go.pattern.util;

import org.apache.log4j.BasicConfigurator;

import tesuji.core.util.ArrayList;
import tesuji.games.go.pattern.common.HibernatePatternManager;
import tesuji.games.go.pattern.common.Pattern;
import tesuji.games.go.pattern.common.PatternGroup;

import static tesuji.games.general.ColorConstant.*;

public class PatternGenerator
{
	private static int PATTERN_SIZE = 3;
	private static final String GROUP_NAME = "GenTest";
	
	public static ArrayList<Pattern> generate3x3Patterns()
	{
		ArrayList<Pattern> list = new ArrayList<Pattern>();
		
		Pattern pattern = new Pattern();
//		pattern.addLeftColumn();
//		pattern.addRightColumn();
//		pattern.addTopRow();
//		pattern.addBottomRow();
		pattern.setUserX(PATTERN_SIZE/2);
		pattern.setUserY(PATTERN_SIZE/2);
		
		generate3x3Pattern(0, 0, pattern, list, PATTERN_SIZE, PATTERN_SIZE);

		Pattern sidePattern = new Pattern();
//		pattern.addLeftColumn();
//		pattern.addRightColumn();
//		pattern.addTopRow();
//		pattern.addBottomRow();
		sidePattern.removeTopRow();
		sidePattern.setUserX(PATTERN_SIZE/2);
		sidePattern.setUserY(0);
		sidePattern.setTopEdge(true);

		generate3x3Pattern(0, 0, sidePattern, list, PATTERN_SIZE, PATTERN_SIZE-1);
		
		return list;
	}
	
	public static void generate3x3Pattern(int col, int row, Pattern p, ArrayList<Pattern> list, int width, int height)
	{
		if (col>=width || row>=height)
			System.err.println("Out of bounds: "+col+","+row);
		if (col==width-1 && row==height-1)
		{
			p.setPoint(col, row, WHITE);
			list.add((Pattern)p.clone());
			System.out.println("Pattern: \n"+p.toString()+"\n");
			p.setPoint(col, row, BLACK);
			list.add((Pattern)p.clone());
			System.out.println("Pattern: \n"+p.toString()+"\n");
			p.setPoint(col, row, EMPTY);
			list.add((Pattern)p.clone());
			System.out.println("Pattern: \n"+p.toString()+"\n");
			return;
		}
		int newCol = col;
		int newRow = row;
		newCol++;
		if (newCol==width)
		{
			newCol=0;
			newRow++;
		}
		p.setPoint(col, row, WHITE);
		generate3x3Pattern(newCol,newRow,p,list, width, height);
		p.setPoint(col, row, BLACK);
		generate3x3Pattern(newCol,newRow,p,list, width, height);
		p.setPoint(col, row, EMPTY);
		generate3x3Pattern(newCol,newRow,p,list, width, height);
	}
	
	public static void generateSymmetricalPatterns(ArrayList<Pattern> list)
	{
		Pattern p = new Pattern();
		p.setUserX(1);
		p.setUserY(1);
		for (int row=0; row<3; row++)
			for (int col=0; col<3; col++)
				p.setPoint(col, row, EMPTY);
		
		p.addLeftColumn();
		p.addRightColumn();
		p.addTopRow();
		p.addBottomRow();
		
		p.setPoint(0, 2, EMPTY);
		p.setPoint(2, 0, EMPTY);
		p.setPoint(4, 2, EMPTY);
		p.setPoint(2, 4, EMPTY);
		
		list.add((Pattern)p.clone());

		p.setPoint(0, 1, EMPTY);
		p.setPoint(0, 3, EMPTY);
		p.setPoint(1, 0, EMPTY);
		p.setPoint(3, 0, EMPTY);
		p.setPoint(4, 1, EMPTY);
		p.setPoint(4, 3, EMPTY);
		p.setPoint(1, 4, EMPTY);
		p.setPoint(3, 4, EMPTY);
		
		list.add((Pattern)p.clone());

		p.setPoint(0, 0, EMPTY);
		p.setPoint(0, 4, EMPTY);
		p.setPoint(0, 0, EMPTY);
		p.setPoint(4, 0, EMPTY);
		p.setPoint(4, 0, EMPTY);
		p.setPoint(4, 4, EMPTY);
		p.setPoint(0, 4, EMPTY);
		p.setPoint(4, 4, EMPTY);
		
		list.add((Pattern)p.clone());

		p.addLeftColumn();
		p.addRightColumn();
		p.addTopRow();
		p.addBottomRow();

		p.setPoint(0, 3, EMPTY);
		p.setPoint(3, 0, EMPTY);
		p.setPoint(6, 3, EMPTY);
		p.setPoint(3, 6, EMPTY);
		
		list.add((Pattern)p.clone());
		
		p.setPoint(0, 2, EMPTY);
		p.setPoint(0, 4, EMPTY);
		p.setPoint(2, 0, EMPTY);
		p.setPoint(4, 0, EMPTY);
		p.setPoint(6, 2, EMPTY);
		p.setPoint(6, 4, EMPTY);
		p.setPoint(2, 6, EMPTY);
		p.setPoint(4, 6, EMPTY);
		
		list.add((Pattern)p.clone());
	}
	
	public static void main(String[] args)
	{
		BasicConfigurator.configure();
	
		ArrayList<Pattern> list = generate3x3Patterns();
		//generateSymmetricalPatterns(list);
		ArrayList<Pattern> truncatedList = new ArrayList<Pattern>();
		
		for (int i=0; i<list.size(); i++)
		{
			Pattern p = list.get(i);
			if (p.getPoint(p.getUserX(),p.getUserY())==EMPTY)
				truncatedList.add(p);
		}
		
		System.out.println("Generated "+truncatedList.size()+" patterns");
		
//		for (int i=0; i<truncatedList.size(); i++)
//		{
//			Pattern p = truncatedList.get(i);
//			System.out.println("Pattern: \n"+p.toString()+"\n");
//		}
		
		HibernatePatternManager patternManager = HibernatePatternManager.getSingleton();
		PatternGroup group = patternManager.getPatternGroup(GROUP_NAME);
		if (group!=null)
		{
			ArrayList<Pattern> patternList = patternManager.getPatterns(group);
			for (Pattern p : patternList)
				patternManager.removePattern(p);
		}
		else
		{
			group = new PatternGroup();
			group.setGroupName(GROUP_NAME);
			patternManager.createPatternGroup(group);
		}
		
		for (int i=0; i<truncatedList.size(); i++)
		{
			Pattern p = truncatedList.get(i);
//			if (p.isAdded())
			{
				p.setGroupId(group.getGroupId());
				System.out.println("Save Pattern: \n"+p.toString()+"\n");
				patternManager.createPattern(p);
			}
		}
	}
}
