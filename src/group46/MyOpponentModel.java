package group46;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import genius.core.Bid;
import genius.core.issue.Issue;

public class MyOpponentModel {
	private Map<String, String> nameMap = new HashMap<>();
	private Map<String, Integer> freMap = new HashMap<>();// ���С����ִ���
	private Map<String, Double> CfreMap = new HashMap<>();// ���С��Ƶ��
	private Map<String, Map<String, Integer>> totalMap = new HashMap<>();
	private Map<String, Integer> noMap = new HashMap<>();// ���С������
	private Map<String, Double> CnoMap = new HashMap<>();// ���С��Ȩ��
	private Map<String, Double> IssfreMap = new HashMap<>();// ��Ŵ���Ƶ��

	public MyOpponentModel(Map m) {
		this.nameMap = m;
	}

	public void recievedBid(Bid lastOffer) {
		for (Issue issue : lastOffer.getIssues()) {
			if (lastOffer.getValue(issue) != null)
				if (!freMap.containsKey(issue + ":" + lastOffer.getValue(issue))) {
					freMap.put(issue + ":" + lastOffer.getValue(issue), 1);
					CfreMap.put(issue + ":" + lastOffer.getValue(issue), Double.valueOf(1));
				} else {
					freMap.put(issue + ":" + lastOffer.getValue(issue),
							freMap.get(issue + ":" + lastOffer.getValue(issue)) + 1);
					CfreMap.put(issue + ":" + lastOffer.getValue(issue),
							Double.valueOf(CfreMap.get(issue + ":" + lastOffer.getValue(issue)) + 1));

				}
		}

		// ��С����з��飬�γ�<Minitor=<15=5��17=3>��price=<...>,...>���ָ�ʽ��map
		for (Map.Entry<String, Integer> entry : freMap.entrySet()) {
			String issueName = "";
			Map<String, Integer> valueMapTemp = new HashMap<>();
			if (totalMap.containsKey(nameMap.get(entry.getKey()))) {// ͨ��namemap����ƥ�䣬Ȼ�����
				issueName = nameMap.get(entry.getKey());
				// ����С���map�������map
				valueMapTemp = totalMap.get(nameMap.get(entry.getKey()));
				valueMapTemp.put(entry.getKey(), entry.getValue());
				totalMap.put(issueName, valueMapTemp);

			} else {
				issueName = nameMap.get(entry.getKey());
				valueMapTemp.put(entry.getKey(), entry.getValue());
				totalMap.put(issueName, valueMapTemp);
			}
		}

		for (Map.Entry<String, Map<String, Integer>> entry : totalMap.entrySet()) {
			List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(
					entry.getValue().entrySet());
			list.sort(new Comparator<Map.Entry<String, Integer>>() {// ��ͨ�����ִ���������������
				public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
					return o2.getValue().compareTo(o1.getValue());
				}
			});
			for (int i = 0; i < list.size(); i++) {
				// ��С���valueֵ����Ϊ����
				noMap.put(list.get(i).getKey(), i + 1);
				CnoMap.put(list.get(i).getKey(), Double.valueOf(i + 1));
			}
		}

		for (Map.Entry<String, Map<String, Integer>> entry : totalMap.entrySet()) {// ��������
			int k = 0;
			int g = 0;
			IssfreMap.put(entry.getKey(), 0.0);
			for (Map.Entry<String, Integer> entry2 : entry.getValue().entrySet()) {// ����ÿ��С�����ÿ��������ܴ���
				k = k + entry2.getValue();
				for (String key : noMap.keySet()) {
					if (nameMap.get(key).equals(entry.getKey()) && key.equals(entry2.getKey())) {// ����ÿ��������ܵ������ܺ�
						g = g + noMap.get(key);
					}
				}
			}

			for (String key : freMap.keySet()) {// ����w��С�ࣩ �� w�����ࣩ
				if (nameMap.get(key).equals(entry.getKey())) {
					BigDecimal bignum1 = new BigDecimal(Math.pow(freMap.get(key), 2));
					BigDecimal bignum2 = new BigDecimal(Math.pow(k, 2));
					double fre = Double.parseDouble(bignum1.divide(bignum2, 4, RoundingMode.HALF_UP).toString());
					CfreMap.put(key, fre);
					IssfreMap.put(entry.getKey(), IssfreMap.get(entry.getKey()) + fre);
				}
			}

			for (String key : noMap.keySet()) {
				if (nameMap.get(key).equals(entry.getKey())) {// ����v
					BigDecimal bignum1 = new BigDecimal(g - noMap.get(key) + 1);
					BigDecimal bignum2 = new BigDecimal(g);
					double fre2 = Double.parseDouble(bignum1.divide(bignum2, 4, RoundingMode.HALF_UP).toString());
					CnoMap.put(key, fre2);
				}
			}

			for (String key : totalMap.get(entry.getKey()).keySet()) {
				BigDecimal bignum1 = new BigDecimal(CfreMap.get(key));
				BigDecimal bignum2 = new BigDecimal(IssfreMap.get(nameMap.get(key)));
				CfreMap.put(key, Double.parseDouble(bignum1.divide(bignum2, 4, RoundingMode.HALF_UP).toString()));
			}

		}

	}

	public Double getUtility(Bid b) {
		int num = 0;
		Double utility = 0.0;
//		 System.out.println("CfreMap is" +CfreMap);
//		 System.out.println("CnoMap is" +CnoMap);
		for (Issue issue : b.getIssues()) {
			num++;
			if (CfreMap.containsKey(issue + ":" + b.getValue(issue))
					&& CnoMap.containsKey(issue + ":" + b.getValue(issue))) {
//				System.out.println(" issue is " + issue + ":" + b.getValue(issue) + "  cfremap value is "
//						+ CfreMap.get(issue + ":" + b.getValue(issue)));
//				System.out.println(" issue is " + issue + ":" + b.getValue(issue) + "  cnomap value is "
//						+ CnoMap.get(issue + ":" + b.getValue(issue)));

				utility = utility
						+ CfreMap.get(issue + ":" + b.getValue(issue)) * CnoMap.get(issue + ":" + b.getValue(issue));
			}
		}
		BigDecimal bignum3 = new BigDecimal(num);
		BigDecimal bignum4 = new BigDecimal(utility);
		utility = Double.parseDouble(bignum4.divide(bignum3, 4, RoundingMode.HALF_UP).toString());

		return utility;
	}

}
