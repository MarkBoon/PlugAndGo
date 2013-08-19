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
	private static final String sgf = "(;B[ii];W[fd];B[hc];W[fb];B[ee];W[hb];B[ge];W[fe];B[ed];W[ef];B[gf];W[ff];B[ic];W[ce];B[cd];W[bd];B[be];W[bf];B[bc];W[ae];B[ec];W[hf];B[cc];W[gg];B[hg];W[hh];B[ih];W[di];B[he];W[ig];B[eb];W[gh];B[fc];W[gd];B[gc];W[gb];B[cf];W[cg];B[ai];W[gi];B[bg];W[bh];B[ah];W[dg];B[ca];W[bi];B[ib];W[dh];B[ea];W[ha];B[if];W[hi];B[fa];W[ie];B[hd];W[bb];B[db];W[eh];B[fi];W[fh];B[ac];W[ab];B[ba];W[ei];B[ga];W[cb];B[da];W[dc];B[dd];W[ad];B[fg];W[df];B[ii];W[id];B[if];W[ie];B[ia];W[hb];B[ha];W[fb];B[gb];W[ih];B[ci];W[ch];B[af];W[ag];B[aa];W[de];B[ai];W[ah];B[ab];W[id];B[if])";

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
