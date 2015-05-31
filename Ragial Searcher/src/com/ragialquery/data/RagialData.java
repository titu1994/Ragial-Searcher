package com.ragialquery.data;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Class which stores the data obtained from Ragial.
 * @author Somshubra Majumdar
 * @version 2.0
 * @since 1.0
 */
public class RagialData {

	/**
	 * Name of the item that was queried
	 */
	public String name;

	public int shortNumber;
	public long shortMin;
	public long shortMax;
	public long shortAverage;
	public long shortStdDev;
	public double shortConfidence;

	public int longNumber;
	public long longMin;
	public long longMax;
	public long longAverage;
	public long longStdDev;
	public double longConfidence;
	
	/**
	 * Contains the list of venders which are selling the item now.
	 * May contain 0 items. Check with boolean value first before iterating.
	 */
	public ArrayList<VendingNow> vendingList = new ArrayList<VendingNow>();
	
	/**
	 * Boolean to denote if any vender is currently selling this item.
	 */
	public boolean isBeingVended;
	
	/**
	 * Class which contains the details of each vender currently selling some item.
	 */
	public static class VendingNow {
		
		public String venderName;
		public String shopName;
		public int vendCount;
		public long vendPrice;
		public int stdCh;
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Vender Name : " + venderName + "\n");
			sb.append("Shop Name : " + shopName + "\n");
			sb.append("Vend Count : " + vendCount + "\n");
			sb.append("Vend Price : " + vendPrice + "\n");
			sb.append("Standard Change : " + stdCh + "\n");
			return sb.toString();
		}
		
	}
	
	private String parseRagialUrlCode() {
		int code = RagialQueryMatcher.getSearchChoiceCode();
		switch(code) {
		case RagialQueryMatcher.RAGIAL_URL_RENEWAL: {
			return "iRO-Renewal";
		}
		case RagialQueryMatcher.RAGIAL_URL_CLASSIC: {
			return "iRO-Classic";
		}
		case RagialQueryMatcher.RAGIAL_URL_THOR: {
			return "iRO-Thor";
		}
		default: {
			return "iRO-Renewal";
		}
		}
	}

	/**
	 * Parses through the Document to obtain the values for all the attributes
	 * @param doc - A Jsoup Document
	 * @throws IOException - IOExceptions can be generated by Document
	 */
	public void parseDocument(Document doc) throws IOException {
		Element nameElement = doc.select("a[href*=ragial.com/item/" + parseRagialUrlCode() + "]").first();
		name = nameElement.text();
		
		Element trShort = doc.select("tr").get(1);
		Element trLong = doc.select("tr").get(2);
		
		Elements td = trShort.select("td");
		String fix = null;
		String number = td.get(0).text();
		if(number.contains(",")) {
			number = number.replace(",", "");
		}
		shortNumber = Integer.parseInt(number);

		fix = td.get(1).text();
		fix = fix.replaceAll(",", "");
		fix = fix.substring(0, fix.length()-1);
		shortMin = Long.parseLong(fix);

		fix = td.get(2).text();
		fix = fix.replaceAll(",", "");
		fix = fix.substring(0, fix.length()-1);
		shortMax = Long.parseLong(fix);

		fix = td.get(3).text();
		fix = fix.replaceAll(",", "");
		fix = fix.substring(0, fix.length()-1);
		shortAverage = Long.parseLong(fix);

		fix = td.get(4).text();
		fix = fix.replaceAll(",", "");
		fix = fix.substring(1, fix.length()-1);
		shortStdDev = Long.parseLong(fix);

		fix = td.get(5).text();
		shortConfidence = Double.parseDouble(fix);

		td = trLong.select("td");
		number = td.get(0).text();
		if(number.contains(",")) {
			number = number.replace(",", "");
		}
		longNumber = Integer.parseInt(number);

		fix = td.get(1).text();
		fix = fix.replaceAll(",", "");
		fix = fix.substring(0, fix.length()-1);
		longMin = Long.parseLong(fix);

		fix = td.get(2).text();
		fix = fix.replaceAll(",", "");
		fix = fix.substring(0, fix.length()-1);
		longMax = Long.parseLong(fix);

		fix = td.get(3).text();
		fix = fix.replaceAll(",", "");
		fix = fix.substring(0, fix.length()-1);
		longAverage = Long.parseLong(fix);

		fix = td.get(4).text();
		fix = fix.replaceAll(",", "");
		fix = fix.substring(1, fix.length()-1);
		longStdDev = Long.parseLong(fix);

		fix = td.get(5).text();
		longConfidence = Double.parseDouble(fix);
		
		Element vend;
		String url;
		Elements vends = doc.select("a[href*=ragial.com/shop/" + parseRagialUrlCode() + "]");
		for(Element e : vends) {
			if(e.text().equals("Vending Now")) {
				url = e.attr("href");
				vend = Jsoup.connect(url).timeout(0).get();		
				
				VendingNow vdn = new VendingNow();
				String venderName = vend.select("dt").get(1).text();
				vdn.venderName = venderName;
				
				String shopName = vend.select("h2").first().text();
				vdn.shopName = shopName;
				
				Elements items = vend.select("tr");
				for(Element item : items) {
					
					String testName = item.getElementsByClass("name").text();
					
					if(testName.trim().equals(name)) {
						Elements testAmount = item.select("td");
						
						String co = testAmount.get(1).text();
						co = co.substring(0, co.length() - 1);
						co = co.replace(",", "");
						vdn.vendCount = Integer.parseInt(co);
						
						Element pre = testAmount.get(2);
						String pr = pre.getElementsByAttribute("href").first().text();
						pr = pr.substring(0, pr.length() - 1);
						pr = pr.replaceAll(",", "");
						vdn.vendPrice = Long.parseLong(pr);
						
						Element st = testAmount.get(3);
						String s = st.text();
						s = s.substring(0, s.length()-1);
						if(s.contains("+")) {
							s = s.replace("+", "");
						}
						vdn.stdCh = Integer.parseInt(s);
						
						vendingList.add(vdn);
						
						break;
					}
				}
				
			}
		}
		
		if(vendingList.size() > 0)
			isBeingVended = true;
		
	}

	/**
	 * Displays all the the details
	 * @return All the information about the RagialData
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Name : " + name + "\n");
		sb.append("Short :\n");
		sb.append("Number : " + shortNumber + " ");
		sb.append("Min : " + shortMin + " ");
		sb.append("Max : " + shortMax + " ");
		sb.append("Avg : " + shortAverage + " ");
		sb.append("Std. Dev : " + shortStdDev + " ");
		sb.append("Confidence : " + shortConfidence + "\n");
		
		sb.append("Long :\n");
		sb.append("Number : " + longNumber + " ");
		sb.append("Min : " + longMin + " ");
		sb.append("Max : " + longMax + " ");
		sb.append("Avg : " + longAverage + " ");
		sb.append("Std. Dev : " + longStdDev + " ");
		sb.append("Confidence : " + longConfidence + "\n\n");
		
		for(VendingNow vd : vendingList) {
			sb.append("Vender Name : " + vd.venderName + "\n");
			sb.append("Shop name : " + vd.shopName + "\n");
			sb.append("Count : " + vd.vendCount + "\n");
			sb.append("Vend price : " + vd.vendPrice + "\n");
			sb.append("Standard Difference : " + vd.stdCh + "%\n\n");
			
		}

		return sb.toString();
	}
	
	/**
	 * Displays either all of the information or only vender information, depending on users choice.
	 * @param displayOnlyVenders - If set to true, displays only the venders. Else, displays only the informatio about the item.
	 * @return respective string
	 */
	public String toString(boolean displayOnlyVenders) {
		StringBuilder sb = new StringBuilder();
		
		if(displayOnlyVenders) {
			for(VendingNow vd : vendingList) {
				sb.append("Vender Name : " + vd.venderName + "\n");
				sb.append("Shop name : " + vd.shopName + "\n");
				sb.append("Count : " + vd.vendCount + "\n");
				sb.append("Vend price : " + vd.vendPrice + "\n");
				sb.append("Standard Difference : " + vd.stdCh + "%\n\n");
			}
		}
		else {
			sb.append("Name : " + name + "\n");
			sb.append("Short :\n");
			sb.append("Number : " + shortNumber + " ");
			sb.append("Min : " + shortMin + " ");
			sb.append("Max : " + shortMax + " ");
			sb.append("Avg : " + shortAverage + " ");
			sb.append("Std. Dev : " + shortStdDev + " ");
			sb.append("Confidence : " + shortConfidence + "\n");
			
			sb.append("Long :\n");
			sb.append("Number : " + longNumber + " ");
			sb.append("Min : " + longMin + " ");
			sb.append("Max : " + longMax + " ");
			sb.append("Avg : " + longAverage + " ");
			sb.append("Std. Dev : " + longStdDev + " ");
			sb.append("Confidence : " + longConfidence + "\n\n");
		}
		
		return sb.toString();
	}
	
	/**
	 * Create a gson string object for this data. Ensure the data is not null.
	 * @return GSON string 
	 */
	public String getGSONData() {
		Gson gson = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create();
		return gson.toJson(this);
	}
	
	/**
	 * Creates a RagialData object from the Gson String
	 * @param gsonData - A RagialData converted in JSON format using GSON and stored as a String
	 * @return RagialData object - A RagialData object created from the Gson String data.
	 */
	public static RagialData initialiseFromGSONData(String gsonData) {
		Gson gson = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create();
		
		RagialData data = gson.fromJson(gsonData, RagialData.class);
		return data;
	}
	
}
