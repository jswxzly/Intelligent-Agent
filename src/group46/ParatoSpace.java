package group46;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import genius.core.Bid;
import genius.core.BidIterator;
import genius.core.Domain;
import genius.core.utility.AbstractUtilitySpace;

public class ParatoSpace {
	private Domain domain;
	private AbstractUtilitySpace utilitySpace;
	private MyOpponentModel opponentModel;
	private List<ParatoPoint> paratoPoint;
	private List<ParatoPoint> paratoSpace;
	private List<ParatoPoint> paretoFrontier = new ArrayList<ParatoPoint>();
	private ParatoPoint nashPoint;

	class ParatoPoint {

		private Bid bid;
		private double agentUtility;
		private double opponentUtility;

		public ParatoPoint(Bid bid, double agentUtility, double opponentUtility) {
			this.bid = bid;
			this.agentUtility = agentUtility;
			this.opponentUtility = opponentUtility;
		}

		public Bid getBid() {
			return bid;
		}

		public double getAgentUtility() {
			return agentUtility;
		}

		public double getOpponentUtility() {
			return opponentUtility;
		}

		public void setOpponentUtility(double utility) {
			this.opponentUtility = utility;
		}

		public double getTotalUtility() {
			return agentUtility * opponentUtility;
		}
		
		public MyOpponentModel getOpponentModel()
		{
			return opponentModel;
		}
	}

	public ParatoSpace(Domain domain, AbstractUtilitySpace utilitySpace, MyOpponentModel opponentModel) {
		this.domain = domain;
		this.utilitySpace = utilitySpace;
		this.opponentModel = opponentModel;
		paratoPoint = new ArrayList<ParatoPoint>();
	}

	public void initalParatoSpace() {
		paratoSpace = new ArrayList<ParatoPoint>();
		BidIterator allBid = new BidIterator(domain);

		while (allBid.hasNext()) {
			Bid bid = allBid.next();
			ParatoPoint paratoPoint = new ParatoPoint(bid, utilitySpace.getUtility(bid), 0);
			paratoSpace.add(paratoPoint);
		}

	}

	public void updateParatoSpace(MyOpponentModel opponentModel, Bid lastOffer) {
		this.opponentModel = opponentModel;
		if (paratoSpace == null) {
			initalParatoSpace();
		} else {
			for (ParatoPoint pp : paratoSpace) {
				if (pp.getBid().equals(lastOffer) || pp.getOpponentUtility() != 0)// 因为对手模型建立的不全，所以只能更新存在对手模型的数据
					pp.setOpponentUtility(opponentModel.getUtility(pp.getBid()));
			}
		}
	}

	public ParatoPoint getNashPoint() {

//		double maxTotalUtility = -1;
//		double currentTotalUtility = 0;
//
//		for (ParatoPoint pp : paratoSpace) {
//			currentTotalUtility = pp.getTotalUtility();
//			if (currentTotalUtility > maxTotalUtility) {
//				nashPoint = pp;
//				maxTotalUtility = currentTotalUtility;
//			}
//
//		}

        //排序从大到小
        Collections.sort(paratoSpace,new Comparator<ParatoPoint>() {
            @Override
            public int compare(ParatoPoint o1, ParatoPoint o2) {
            	BigDecimal data1 = new BigDecimal(o1.getTotalUtility());
            	BigDecimal data2 = new BigDecimal(o2.getTotalUtility());
                return data2.compareTo(data1);
            }
        });
       
        nashPoint = paratoSpace.get(0);
        for(int i=0; i<3; i++){       	
        	if(paratoSpace.get(i).agentUtility > nashPoint.agentUtility){
        		nashPoint = paratoSpace.get(i);
        	}
        }


		return nashPoint;

	}


	




}
