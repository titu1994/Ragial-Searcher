package com.ragialquery.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
	public Vector<VendingNow> vendingList = new Vector<VendingNow>();
	
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
		public int isTypeBuying;
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Vender Name : " + venderName + "\n");
			sb.append("Shop Name : " + shopName + "\n");
			sb.append("Vend Count : " + vendCount + "\n");
			sb.append("Vend Price : " + vendPrice + "\n");
			sb.append("Standard Change : " + stdCh + "\n");
			sb.append("Is Buying : " + isTypeBuying + "\n");
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
		case RagialQueryMatcher.RAGIAL_URL_ODIN: {
			return "iRO-Odin";
		}
		default: {
			return "iRO-Renewal";
		}
		}
	}

	public String parseName(Document doc) throws IOException {
		Element nameElement = doc.select("a[href*=ragial.org/item/" + parseRagialUrlCode() + "]").first();
		this.name = nameElement.text();
		return name;
	}

	/**
	 * Parses through the Document to obtain the values for all the attributes
	 * @param doc - A Jsoup Document
	 * @throws IOException - IOExceptions can be generated by Document
	 */
	public void parseDocument(Document doc) throws IOException {
		// t1 = System.currentTimeMillis();

		Element nameElement = doc.select("a[href*=ragial.org/item/" + parseRagialUrlCode() + "]").first();
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
		
		Element vend = null;
		String url;
		Elements vends = doc.select("a[href*=ragi.al/shop/" + parseRagialUrlCode() + "]");

		ExecutorService executor = Executors.newCachedThreadPool();

		for(Element e : vends) {
			if(e.text().equals("Vending Now")) {
				executor.submit(parseVendShop(e));
			}
			else if(e.text().equals("Buying Now")) {
                executor.submit(parseBuyingItem(e));
			}
		}

		try {
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if(vendingList.size() > 0)
			isBeingVended = true;

		//long t3 = System.currentTimeMillis();
		//System.out.println("Time to parse document : " + (t3-t1));

        //System.out.println("Finished parsing document");
    }

	private Runnable parseVendShop(final Element e) throws IOException {

		return new Runnable() {
			@Override
			public void run() {
                Element vend = null;
                String url;
                url = e.attr("href");

                //long t4 = System.currentTimeMillis();

                try {
                    vend = Jsoup.connect(url).userAgent("Mozilla").timeout(0).get();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                //long t5 = System.currentTimeMillis();

                //System.out.println("Time to download vend store : " + (t5 - t4));

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

						vdn.isTypeBuying = 0;

						vendingList.add(vdn);

						break;
					}
				}

				//long t6 = System.currentTimeMillis();

				//System.out.println("Time to parse vend items : " + (t6 - t4));
			}
		};
	}

	private Runnable parseBuyingItem(final Element e) throws IOException {
		return new Runnable() {
			@Override
			public void run() {
                Element vend = null;
                String url;
                url = e.attr("href");

                //long t4 = System.currentTimeMillis();

                try {
                    vend = Jsoup.connect(url).userAgent("Mozilla").timeout(0).get();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                //long t5 = System.currentTimeMillis();

                //System.out.println("Time to download buying store : " + (t5 - t4));

                VendingNow vdn = new VendingNow();
				String venderName = vend.select("dt").get(1).text();
				vdn.venderName = venderName;

				String shopName = vend.select("h2").first().text();
				vdn.shopName = shopName;

				Elements items = vend.select("tr");
				for (Element item : items) {

					String testName = item.getElementsByClass("name").text();

					if (testName.trim().equals(name)) {
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
						s = s.substring(0, s.length() - 1);
						if (s.contains("+")) {
							s = s.replace("+", "");
						}
						vdn.stdCh = Integer.parseInt(s);

						vdn.isTypeBuying = 1;

						vendingList.add(vdn);

                        //t5 = System.currentTimeMillis();
                        //System.out.println("Time to parse buying store : " + (t5 - t4));

						break;
					}
				}
			}
		};
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
			sb.append("Standard Difference : " + vd.stdCh + "%\n");
			sb.append("Is Buying : " + vd.isTypeBuying + "\n\n");
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
				sb.append("Standard Difference : " + vd.stdCh + "%\n");
				sb.append("Is Buying : " + vd.isTypeBuying + "\n\n");
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
