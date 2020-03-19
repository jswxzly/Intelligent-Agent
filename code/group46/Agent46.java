package group46;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import genius.core.AgentID;
import genius.core.Bid;
import genius.core.Domain;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.Offer;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.Objective;
import genius.core.issue.Value;
import genius.core.issue.ValueDiscrete;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;
import genius.core.uncertainty.BidRanking;
import genius.core.uncertainty.UserModel;
import genius.core.utility.AbstractUtilitySpace;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.Evaluator;
import genius.core.utility.EvaluatorDiscrete;
import group46.ParatoSpace.ParatoPoint;

/**
 * A simple example agent that makes random bids above a minimum target utility.
 *
 * @author Tim Baarslag
 */
public class Agent46 extends AbstractNegotiationParty {
	private MyOpponentModel opponentModel;
	private ParatoSpace paratoSpace;
	private static double MINIMUM_TARGET = 0.9;
	private Bid lastOffer;
	private Map<String, String> nameMap = new HashMap<>();
	private AdditiveUtilitySpace u;
	private NegotiationInfo info;
	private AbstractUtilitySpace utilitySpace = null;
	private AdditiveUtilitySpace additiveUtilitySpace = null;
	private double utilityThreshold;
	public ParatoPoint nashPoint;
	private double discount = 0.01;
	private List<Bid> bidList;
	private int n = 0;

	private double maxOpponentUtility = 0;
	private Bid maxOpponentBid;

	/**
	 * Initializes a new instance of the agent.
	 */
	@Override
	public void init(NegotiationInfo info) {
		this.info = info;
		this.rand = new Random(info.getRandomSeed());
		this.timeline = info.getTimeline();
		this.userModel = info.getUserModel();
		this.user = info.getUser();

		if (hasPreferenceUncertainty()) {

			BidRanking bidRanking = userModel.getBidRanking();
			System.out.println("The agent ID is:" + info.getAgentID());
			System.out.println("Total number of possible bids:" + userModel.getDomain().getNumberOfPossibleBids());
			System.out.println("The number of bids in the ranking is:" + bidRanking.getSize());
			System.out.println("The lowest bidis:" + bidRanking.getMinimalBid());
			System.out.println("The highest bidis:" + bidRanking.getMaximalBid());
			System.out.println("The elicitation costs are:" + user.getElicitationCost());
			bidList = bidRanking.getBidOrder(); // 由低到高排序
			Collections.reverse(bidList);
			System.out.println("The 1th bid in the rankingis:" + bidList.get(0));
			for (Bid aa : bidList) {
				System.out.println("Bid is " + aa);
			}
			AbstractUtilitySpace passedUtilitySpace = info.getUtilitySpace();
			AbstractUtilitySpace estimatedUtilitySpace = estimateUtilitySpace();
			estimatedUtilitySpace.setReservationValue(passedUtilitySpace.getReservationValue());
			estimatedUtilitySpace.setDiscount(passedUtilitySpace.getDiscountFactor());
			info.setUtilSpace(estimatedUtilitySpace);
			utilitySpace = info.getUtilitySpace();
			additiveUtilitySpace = (AdditiveUtilitySpace) utilitySpace;
			List<Issue> issues = additiveUtilitySpace.getDomain().getIssues();
			try {
				utilityThreshold = this.getUtility(utilitySpace.getMaxUtilityBid());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				utilityThreshold = 0.9;
			}
			for (Issue issue : issues) {
				int issueNumber = issue.getNumber();
				System.out.println(">> " + issue.getName() + " weight: " + additiveUtilitySpace.getWeight(issueNumber));
				// Assuming that issues are discrete only
				IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
				EvaluatorDiscrete evaluatorDiscrete = (EvaluatorDiscrete) additiveUtilitySpace
						.getEvaluator(issueNumber);
				for (ValueDiscrete valueDiscrete : issueDiscrete.getValues()) {
					System.out.println("Evaluation(getValue): " + evaluatorDiscrete.getValue(valueDiscrete));
					// freMap用于统计各个小类出现的次数
					// freMap.put(valueDiscrete.getValue(), 0);
					// 在初始化过程中将issue的小类作为nameMap的key值，issue作为value值方便为之后匹配
					nameMap.put(issue.getName() + ":" + valueDiscrete.getValue(), issue.getName());
					try {
						System.out.println(
								"Evaluation(getEvaluation): " + evaluatorDiscrete.getEvaluation(valueDiscrete));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		} else {
			utilitySpace = info.getUtilitySpace();
			additiveUtilitySpace = (AdditiveUtilitySpace) utilitySpace;
			List<Issue> issues = additiveUtilitySpace.getDomain().getIssues();
			// BidRanking bidRanking = userModel.getBidRanking();
			// List<Bid> bidList = bidRanking.getBidOrder();
			// System.out.println("The 5th bid in the
			// rankingis:"+bidList.get(5));
			try {
				utilityThreshold = this.getUtility(utilitySpace.getMaxUtilityBid());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				utilityThreshold = 0.9;
			}

			for (Issue issue : issues) {
				int issueNumber = issue.getNumber();
				System.out.println(">> " + issue.getName() + " weight: " + additiveUtilitySpace.getWeight(issueNumber));
				// Assuming that issues are discrete only
				IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
				EvaluatorDiscrete evaluatorDiscrete = (EvaluatorDiscrete) additiveUtilitySpace
						.getEvaluator(issueNumber);
				for (ValueDiscrete valueDiscrete : issueDiscrete.getValues()) {
					System.out.println("Evaluation(getValue): " + evaluatorDiscrete.getValue(valueDiscrete));
					// freMap用于统计各个小类出现的次数
					// freMap.put(valueDiscrete.getValue(), 0);
					// 在初始化过程中将issue的小类作为nameMap的key值，issue作为value值方便为之后匹配
					nameMap.put(issue.getName() + ":" + valueDiscrete.getValue(), issue.getName());

					try {
						System.out.println(
								"Evaluation(getEvaluation): " + evaluatorDiscrete.getEvaluation(valueDiscrete));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		opponentModel = new MyOpponentModel(nameMap);
		paratoSpace = new ParatoSpace(userModel.getDomain(), utilitySpace, opponentModel);
	}

	/**
	 * Makes a random offer above the minimum utility target Accepts everything
	 * above the reservation value at the very end of the negotiation; or breaks
	 * off otherwise.
	 */
	@Override
	public Action chooseAction(List<Class<? extends Action>> possibleActions) {
		// store the normalised current time
		double time = getTimeLine().getTime();

		// if the utility of opponent offer is above acceptance utility
		if (getUtility(lastOffer) >= MINIMUM_TARGET) {
			// accept opponent offer
			System.out.println("Agreement reached.");
			return new Accept(info.getAgentID(), lastOffer);
		} else {
			// if half the time remains...
			if (time >= 0.5) {
				// if more than two-tenth of the time remains...
				if (time < 0.8) {
					// offer a bid based upon the k-agent strategy
					System.out.println("time is over 0.5");
					return new Offer(info.getAgentID(), Strategy());
				}
				// if less than two-tenth of the time remain...
				else {
					if (time > 0.98) {
						return new Offer(info.getAgentID(), maxOpponentBid);
					} else {
						// offer a bid based upon the linear conceder strategy
						System.out.println("time is over 0.8");
						return new Offer(info.getAgentID(), linearConcederStrategy());
					}
				}
			}
			// if less than half the time remains
			else {
				// offer a bid based upon a hard-headed strategy
				System.out.println("New bid offered.");
				return new Offer(info.getAgentID(), hardHeadedNegotiation());
			}
		}

	}

	// first strategy used by the agent while time < 0.5
	// agent generates random bids that maximise its utility and does not
	// concede
	private Bid hardHeadedNegotiation() {
		Bid randomBid;
		double util;
		do {
			randomBid = generateRandomBid();
			util = utilitySpace.getUtility(randomBid);
		} while (util < MINIMUM_TARGET);
		return randomBid;
	}

	// second strategy used by the agent while time >= 0.5 and < 0.8
	// generate a high utility bid close to the nash point based on the opponent
	// model
	// generate bids that are more likely to be accepted by opponent
	// does not concede
	private Bid Strategy() {
		Queue<Bid> agentBids = new PriorityQueue<Bid>(new BidComparator());
		int n = 20;
		int nBid = 0;
		while (nBid <= n) {
			nBid++;
			Bid randomBid;
			double util;
			do {
				randomBid = generateRandomBid();
				util = utilitySpace.getUtility(randomBid);
			} while (util < MINIMUM_TARGET);
			agentBids.add(randomBid);
		}
		return agentBids.remove();
	}

	// strategy used by the opponent while time >= 0.8
	// agent prioritising agreement over maximising its own utility
	// more likely to concede over time so that agreement is more likely
	private Bid linearConcederStrategy() {
		if(MINIMUM_TARGET > getUtility(maxOpponentBid)){
			Bid randomBid;
			randomBid = Strategy();
			if(n % 10 == 0){
				MINIMUM_TARGET = MINIMUM_TARGET * (1 - discount);
			}
			return randomBid;
		}else{
			return maxOpponentBid;
		}
		
	}

	private Bid getMaxUtilityBid() {
		try {
			return utilitySpace.getMaxUtilityBid();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// private String getIssueName() {
	// try {
	// return utilitySpace.;
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// return null;
	// }

	/**
	 * Remembers the offers received by the opponent.
	 */
	@Override
	public void receiveMessage(AgentID sender, Action action) {
		if (action instanceof Offer) {
			lastOffer = ((Offer) action).getBid();
			opponentModel.recievedBid(lastOffer);
			paratoSpace.updateParatoSpace(opponentModel, lastOffer);
			nashPoint = paratoSpace.getNashPoint();
			n++;
//			System.out.println("nash pint is " + nashPoint.getBid());
//			System.out.println("agent Utility is " + nashPoint.getAgentUtility());
//			System.out.println("opponent Utility is " + nashPoint.getOpponentUtility());

			if (getUtility(lastOffer) >= maxOpponentUtility) {
				maxOpponentBid = lastOffer;
			}
		}
	}

	@Override
	public String getDescription() {
		return "Places random bids >= " + MINIMUM_TARGET;
	}

	/**
	 * Sets e_i(v) := value
	 */
	public void setUtility(Issue i, ValueDiscrete v, double value) {
		EvaluatorDiscrete evaluator = (EvaluatorDiscrete) u.getEvaluator(i);
		if (evaluator == null) {
			evaluator = new EvaluatorDiscrete();
			u.addEvaluator(i, evaluator);
		}
		evaluator.setEvaluationDouble(v, value);
	}

	public double getUtilityNew(Issue i, ValueDiscrete v) {
		EvaluatorDiscrete evaluator = (EvaluatorDiscrete) u.getEvaluator(i);
		return evaluator.getDoubleValue(v);
	}

	public List<IssueDiscrete> getIssues() {
		List<IssueDiscrete> issues = new ArrayList<>();
		for (Issue i : getDomain().getIssues()) {
			IssueDiscrete issue = (IssueDiscrete) i;
			issues.add(issue);
		}
		return issues;
	}

//	// 初始化
//	public void AdditiveUtilitySpaceFactory(Domain d,List<Bid> bidList) {
//		List<Issue> issues = d.getIssues();
//		int noIssues = issues.size();
//		HashSet<String> haveDone = new HashSet();
//		double goldRate = 0.618;
//		
//		Map<Objective, Evaluator> evaluatorMap = new HashMap<Objective, Evaluator>();
//		for (Issue i : issues) {
//			haveDone.add(bidList.get(0).getValue(i).toString());
//			IssueDiscrete issue = (IssueDiscrete) i;
//			EvaluatorDiscrete evaluator = new EvaluatorDiscrete();
//			evaluator.setWeight(1.0 / noIssues);
//			evaluator.setEvaluationDouble((ValueDiscrete) bidList.get(0).getValue(i), goldRate);
//			evaluatorMap.put(issue, evaluator);		
//		}
//		
//		
//
//		u = new AdditiveUtilitySpace(d, evaluatorMap);
//	}

	/**
	 * Returns an estimate of the utility space given uncertain preferences
	 * specified by the user model. By default, the utility space is estimated
	 * with a simple counting heuristic so that any agent can deal with
	 * preference uncertainty.
	 * 
	 * This method can be overridden by the agent to provide better estimates.
	 */
	public AbstractUtilitySpace estimateUtilitySpace() {
		return defaultUtilitySpaceEstimator(getDomain(), userModel);
	}

	/**
	 * Provides a simple estimate of a utility space given the partial
	 * preferences of a {@link UserModel}. This is constructed as a static
	 * funtion so that other agents (that are not an
	 * {@link AbstractNegotiationParty}) can also benfit from this
	 * functionality.
	 */
	public static AbstractUtilitySpace defaultUtilitySpaceEstimator(Domain domain, UserModel um) {
//		AdditiveUtilitySpaceFactory factory = new AdditiveUtilitySpaceFactory(domain,bidList);
		AgentUtilityEstimator factory = new AgentUtilityEstimator(domain);
		BidRanking bidRanking = um.getBidRanking();
		factory.estimateUsingBidRanks(bidRanking);
		return factory.getUtilitySpace();
	}

	protected Bid generateRandomBid() {
		try {
			// Pairs <issue number, chosen value string>
			HashMap<Integer, Value> values = new HashMap<Integer, Value>();

			// For each issue, put a random value
			for (Issue currentIssue : utilitySpace.getDomain().getIssues()) {
				values.put(currentIssue.getNumber(), getRandomValue(currentIssue));
			}

			// return the generated bid
			return new Bid(utilitySpace.getDomain(), values);

		} catch (Exception e) {

			// return empty bid if an error occurred
			return new Bid(utilitySpace.getDomain());
		}
	}

	public double getUtility(Bid bid) {
		try {
			// throws exception if bid incomplete or not in utility space
			return bid == null ? 0 : utilitySpace.getUtility(bid);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}


	private class BidComparator implements Comparator<Bid> {
		@Override
		// prioritise bids on the queue with a high utility and low distance to
		// the nash point
		public int compare(Bid bid1, Bid bid2) {
			if (getUtility(bid1) > getUtility(bid2)) {
				if (calculateEuclideanDistance(bid1) < calculateEuclideanDistance(bid2)) {
					return 1;
				} else {
					return 0;
				}
			} else if (getUtility(bid1) < getUtility(bid2)) {
				if (calculateEuclideanDistance(bid1) > calculateEuclideanDistance(bid2)) {
					return -1;
				} else {
					return 0;
				}
			} else {
				if (calculateEuclideanDistance(bid1) < calculateEuclideanDistance(bid2)) {
					return 0;
				} else {
					return -1;
				}
			}

		}
	}

	// calculate the euclidean distance between a bid and the nash point
	// optimal bid should be close to nash point
	private double calculateEuclideanDistance(Bid bid) {
		double opponentDist = Math.abs(opponentModel.getUtility(bid) - nashPoint.getOpponentUtility());
		double agentDist = Math.abs(getUtility(bid) - nashPoint.getAgentUtility());
		double result = Math.sqrt(Math.pow(opponentDist, 2) + Math.pow(agentDist, 2));
		return result;
	}

}
