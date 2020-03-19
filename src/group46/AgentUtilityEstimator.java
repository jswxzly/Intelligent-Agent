package group46;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import genius.core.Bid;
import genius.core.Domain;
import genius.core.issue.Issue;
import genius.core.issue.Objective;
import genius.core.issue.ValueDiscrete;
import genius.core.uncertainty.AdditiveUtilitySpaceFactory;
import genius.core.uncertainty.BidRanking;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.Evaluator;
import genius.core.utility.EvaluatorDiscrete;

public class AgentUtilityEstimator extends AdditiveUtilitySpaceFactory{
	
	private Domain domain;

    /**
     * Generates an simple Utility Space on the domain, with equal weights and zero values.
     * Everything is zero-filled to already have all keys contained in the utility maps.
     *
     * @param d
     */
    public AgentUtilityEstimator(Domain d) {
        super(d);
        domain = d;
    }

    @Override
    public void estimateUsingBidRanks(BidRanking r) {
    	List<Issue> issues = domain.getIssues();
		int noIssues = issues.size();
		HashSet<String> haveDone = new HashSet();
		Map<ValueDiscrete, Integer> sort = new HashMap<>();
		Map<String, Integer> sortRecod = new HashMap<>();
		Map<String, Double> leftRate = new HashMap<>();
		double goldRate = 0.618;
		Map<String, String> nameMap = new HashMap<>();
		List<Bid> bidList = r.getBidOrder(); // ”…µÕµΩ∏ﬂ≈≈–Ú
//		bidList = Collections.reverse(bidList);
		
		Map<Objective, Evaluator> evaluatorMap = new HashMap<Objective, Evaluator>();
		for (Issue i : issues) {
			haveDone.add(bidList.get(0).getValue(i).toString());
			sort.put((ValueDiscrete) bidList.get(0).getValue(i), 0);
			sortRecod.put(i.toString(), 0);
			leftRate.put(i.toString(), 1.0);
			nameMap.put(bidList.get(0).getValue(i).toString(), i.toString());
		}
		for (Bid aa : bidList) {
			for (Issue ii : issues) {
				if (!haveDone.contains(aa.getValue(ii))) {
					int no = sortRecod.get(ii.toString()) + 1;
					sort.put((ValueDiscrete) aa.getValue(ii), no);
					sortRecod.put(ii.toString(), no);
					nameMap.put(aa.getValue(ii).toString(), ii.toString());
					haveDone.add(aa.getValue(ii).toString());
				}
			}
		}
		for (Issue iii : issues) {
			getUtilitySpace().setWeight(iii, 1.0 / noIssues);
			int n = sortRecod.get(iii.toString());
			for (int i = 0; i < n + 1; i++) {
				if (i != n) {
					for (Map.Entry<ValueDiscrete, Integer> entry : sort.entrySet()) {
						if (iii.toString().equals(nameMap.get(entry.getKey().toString()))) {
							if (i == entry.getValue()) {								
								this.setUtility(iii, entry.getKey(), leftRate.get(iii.toString()) * goldRate);
								leftRate.put(iii.toString(), leftRate.get(iii.toString()) - leftRate.get(iii.toString()) * goldRate);
							}
						}
					}
				} else {
					for (Map.Entry<ValueDiscrete, Integer> entry : sort.entrySet()) {
						if (iii.toString().equals(nameMap.get(entry.getKey().toString()))) {
							if (i == entry.getValue()) {
								this.setUtility(iii, entry.getKey(), leftRate.get(iii.toString()));
							}
						}
					}

				}

			}
		}

}}
