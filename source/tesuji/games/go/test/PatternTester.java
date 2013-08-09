package tesuji.games.go.test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import tesuji.games.general.TreeNode;
import tesuji.games.go.common.GoMove;
import tesuji.games.go.common.GoMoveFactory;
import tesuji.games.go.monte_carlo.EyeMoveFilter;
import tesuji.games.go.monte_carlo.MonteCarloPluginAdministration;
import tesuji.games.go.monte_carlo.MoveFilter;
import tesuji.games.go.monte_carlo.move_generator.MatchPatterns;
import tesuji.games.go.monte_carlo.move_generator.MoveGenerator;
import tesuji.games.go.pattern.common.HibernatePatternManager;
import tesuji.games.sgf.SGFData;
import tesuji.games.sgf.SGFParser;

public class PatternTester
{
	private static final String sgf = "(;B[ba];W[fe];B[gb];W[fb];B[gf];W[ce];B[eg];W[db];B[fc];W[hc];B[af];W[dd];B[bd];W[fi];B[eh];W[gc];B[fg];W[bi];B[bg];W[dg];B[fh];W[gd];B[ef];W[ab];B[hb];W[dh];B[he];W[dc];B[ci];W[bh];B[cc];W[ig];B[if];W[hg];B[gh];W[eb];B[ia];W[ee];B[ie];W[ca];B[da];W[ic];B[id];W[ed];B[cf];W[ea];B[ib];W[di];B[gi];W[ff];B[ad];W[ch];B[cd];W[ah];B[hf];W[ag];B[gg];W[hd];B[ec];W[de];B[bb];W[ei];B[ih];W[hh];B[ga];W[ac];B[ii];W[ge];B[df];W[cb];B[bc];W[be];B[aa];W[ae];B[fa];W[fd];B[bf];W[ec];B[cg];W[ac];B[hi];W[hg];B[ig];W[hh])";
	
	public static void main(String[] args) throws ParseException
	{
		HibernatePatternManager patternManager = new HibernatePatternManager("Simulation9X9");
		MatchPatterns matchPatterns = new MatchPatterns(patternManager);
		MonteCarloPluginAdministration admin = new MonteCarloPluginAdministration();
		List<MoveFilter> filterList = new ArrayList<MoveFilter>();
		filterList.add(new EyeMoveFilter());
		admin.setSimulationMoveFilterList(filterList);
		List<MoveGenerator> generatorList = new ArrayList<MoveGenerator>();
		generatorList.add(matchPatterns);
		admin.setSimulationMoveGeneratorList(generatorList);
		admin.setExplorationMoveGeneratorList(generatorList);
		admin.setBoardSize(9);
		
		SGFParser<GoMove> parser = new SGFParser<GoMove>(GoMoveFactory.getSingleton());
		parser.parse(sgf);
		TreeNode<SGFData<GoMove>> node = parser.getDocumentNode();
		node = node.getFirstChild();
		while (node!=null)
		{
			admin.playMove(node.getContent().getMove());
			node = node.getFirstChild();
		}
		System.out.println("Done");
	}
}
