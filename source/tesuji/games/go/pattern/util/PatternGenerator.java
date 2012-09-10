package tesuji.games.go.pattern.util;

import tesuji.core.util.ArrayList;
import tesuji.games.go.pattern.common.HibernatePatternManager;
import tesuji.games.go.pattern.common.Pattern;
import tesuji.games.go.pattern.common.PatternGroup;

import static tesuji.games.general.ColorConstant.*;

public class PatternGenerator
{
	public static ArrayList<Pattern> generate3x3Patterns()
	{
		ArrayList<Pattern> list = new ArrayList<Pattern>();
		
		Pattern pattern = new Pattern();

		generate3x3Pattern(0, 0, pattern, list);

		return list;
	}
	
	public static void generate3x3Pattern(int col, int row, Pattern p, ArrayList<Pattern> list)
	{
		if (col>2 || row>2)
			System.err.println("Out of bounds: "+col+","+row);
		if (col==2 && row==2)
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
		if (newCol==3)
		{
			newCol=0;
			newRow++;
		}
		p.setPoint(col, row, WHITE);
		generate3x3Pattern(newCol,newRow,p,list);
		p.setPoint(col, row, BLACK);
		generate3x3Pattern(newCol,newRow,p,list);
		p.setPoint(col, row, EMPTY);
		generate3x3Pattern(newCol,newRow,p,list);
	}
	
	public static void main(String[] args)
	{
		ArrayList<Pattern> list = generate3x3Patterns();
		ArrayList<Pattern> truncatedList = new ArrayList<Pattern>();
		
		for (int i=0; i<list.size(); i++)
		{
			Pattern p = list.get(i);
			if (p.getPoint(1,1)==EMPTY)
				truncatedList.add(p);
		}
		
		System.out.println("Generated "+truncatedList.size()+" patterns");
		
//		for (int i=0; i<truncatedList.size(); i++)
//		{
//			Pattern p = truncatedList.get(i);
//			System.out.println("Pattern: \n"+p.toString()+"\n");
//		}
		
		HibernatePatternManager patternManager = HibernatePatternManager.getSingleton();
		PatternGroup group = new PatternGroup();
		group.setGroupName("GenTest");
		patternManager.createPatternGroup(group);
		for (int i=0; i<truncatedList.size(); i++)
		{
			Pattern p = truncatedList.get(i);
			p.setGroupId(group.getGroupId());
			System.out.println("Save Pattern: \n"+p.toString()+"\n");
			patternManager.createPattern(p);
		}
	}
}
