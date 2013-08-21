package tesuji.games.go.test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import tesuji.core.util.LoggerConfigurator;
import tesuji.games.general.TreeNode;
import tesuji.games.go.common.GoMove;
import tesuji.games.go.common.GoMoveFactory;
import tesuji.games.go.monte_carlo.EyeMoveFilter;
import tesuji.games.go.monte_carlo.MonteCarloPluginAdministration;
import tesuji.games.go.monte_carlo.MoveFilter;
import tesuji.games.go.monte_carlo.move_generator.MatchPatterns;
import tesuji.games.go.monte_carlo.move_generator.MoveGenerator;
import tesuji.games.go.pattern.common.HibernatePatternManager;
import tesuji.games.go.pattern.incremental.PatternMatchList;
import tesuji.games.go.util.ProbabilityMap;
import tesuji.games.sgf.SGFData;
import tesuji.games.sgf.SGFParser;

public class PatternTester
{
	private static final String sgf = "(;B[de];W[dg];B[fe];W[ee];B[ic];W[ec];B[gc];W[ff];B[ef];W[ed];B[eg];W[df];B[gf];W[fg];B[eh];W[gg];B[fh];W[ge];B[hf];W[cc];B[cg];W[he];B[hh];W[fd];B[gh];W[be];B[hc];W[hg];B[ih];W[ig];B[if];W[ie];B[fb];W[fc];B[db];W[cb];B[eb];W[ah];B[ac];W[dc];B[gb];W[bh];B[gi];W[ag];B[cf];W[ce];B[dh];W[bf];B[ca];W[ba];B[da];W[ab];B[fi];W[ch];B[bc];W[bb];B[ci];W[bi];B[di];W[dd];B[df];W[ae];B[hd];W[ii];B[ga];W[id];B[if];W[gf];B[bg];W[ad];B[ea];W[bd];B[ac];W[hb];B[ib];W[ha];B[ia];W[gd];B[ha];W[hf];B[hi];W[af])";

//	private static final String sgf = "(;B[ba];W[fe];B[gb];W[fb];B[gf];W[ce];B[eg];W[db];B[fc];W[hc];B[af];W[dd];B[bd];W[fi];B[eh];W[gc];B[fg];W[bi];B[bg];W[dg];B[fh];W[gd];B[ef];W[ab];B[hb];W[dh];B[he];W[dc];B[ci];W[bh];B[cc];W[ig];B[if];W[hg];B[gh];W[eb];B[ia];W[ee];B[ie];W[ca];B[da];W[ic];B[id];W[ed];B[cf];W[ea];B[ib];W[di];B[gi];W[ff];B[ad];W[ch];B[cd];W[ah];B[hf];W[ag];B[gg];W[hd];B[ec];W[de];B[bb];W[ei];B[ih];W[hh];B[ga];W[ac];B[ii];W[ge];B[df];W[cb];B[bc];W[be];B[aa];W[ae];B[fa];W[fd];B[bf];W[ec];B[cg];W[ac];B[hi];W[hg];B[ig];W[hh])";
	
	public static void main(String[] args) throws ParseException
	{
		LoggerConfigurator.configure();
		HibernatePatternManager patternManager = new HibernatePatternManager("Simulation9x9");
		MatchPatterns matchPatterns = new MatchPatterns(patternManager);
		MonteCarloPluginAdministration admin = new MonteCarloPluginAdministration();
		List<MoveFilter> filterList = new ArrayList<MoveFilter>();
		filterList.add(new EyeMoveFilter());
		admin.setSimulationMoveFilterList(filterList);
		List<MoveGenerator> generatorList = new ArrayList<MoveGenerator>();
		generatorList.add(matchPatterns);
		//admin.setSimulationMoveGeneratorList(generatorList);
		admin.setExplorationMoveGeneratorList(generatorList);
		admin.setBoardSize(9);
		
		SGFParser<GoMove> parser = new SGFParser<GoMove>(GoMoveFactory.getSingleton());
		parser.parse(sgf);
		TreeNode<SGFData<GoMove>> node = parser.getDocumentNode();
		PatternMatchList list = matchPatterns.getMatches();
		ProbabilityMap map = admin.getProbabilityMap();
		int nrMoves = 0;
		node = node.getFirstChild();
		while (node!=null)
		{
			admin.playMove(node.getContent().getMove());
			node = node.getFirstChild();
			nrMoves++;
		}
		System.out.println("Done");
	}
}
