package com.ragialquery.data;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.ragialquery.data.RagialData.VendingNow;

/**
 * The class that handles the queries to Ragial.com and also prepares RagialData and its VendingNow list for use.
 * @author Somshubra Majumdar
 * @version 2.0
 * @since 1.0
 */
public class RagialQueryMatcher {
	private static ExecutorService executer = Executors.newCachedThreadPool();
	private static final String RAGIAL_SEARCH_URL = "http://ragial.com/search/iRO-Renewal/";
	private static RagialQueryMatcher matcher = new RagialQueryMatcher();

	private RagialQueryMatcher() {}

	/**
	 * Provides a Singleton object of RagialQueryMatcher
	 * @return RagialQueryMatcher object
	 */
	public static final RagialQueryMatcher getMatcher() {
		if(matcher != null)
			return matcher;
		else {
			matcher = new RagialQueryMatcher();
			return matcher;
		}
	}

	/**
	 * Returns a future object which contains an array of RagialData with name similar to specified name.
	 * 
	 * @param name - Name of the item to be searched
	 * @return RagialData[] of items with name similar to parameter name or null if Executor is null
	 */
	public Future<RagialData[]> searchRagial(final String name) {
		Callable<RagialData[]> callable = getSearchRagialCallable(name);
		if(isExecutorAvailable())
			return executer.submit(callable);
		else {
			restartExecutor();
			return executer.submit(callable);
		}
	}

	/**
	 * Generates a callable for the searchRagial function.
	 * @param name
	 * @return Callable<RagialData[]>
	 */
	private Callable<RagialData[]> getSearchRagialCallable(final String name) {
		Callable<RagialData[]> callable = new Callable<RagialData[]>() {

			@Override
			public RagialData[] call() throws Exception {
				StringBuilder urlBuilder = new StringBuilder();
				String exactName = name.trim();

				if(exactName.contains(" ")){
					String urlName = exactName.replaceAll(" ", "+");
					urlBuilder.append(urlName);
				}
				else {
					urlBuilder.append(exactName);
				}

				if(exactName.contains("[")) {
					urlBuilder.append("%5B");
					int pos = exactName.lastIndexOf("]") - 1;
					urlBuilder.append(exactName.charAt(pos));
					urlBuilder.append("%5D");
				}

				Document doc = Jsoup.connect(RAGIAL_SEARCH_URL + urlBuilder.toString())
						.timeout(0)
						.get();
				ArrayList<RagialData> list = new ArrayList<RagialData>();
				Elements hrefs = doc.select("tr");
				hrefs.remove(0);

				for(Element e : hrefs) {
					String url = e.select("td").first().select("a[href]").first().attr("href");
					doc = Jsoup.connect(url).timeout(0).get();

					RagialData store = new RagialData();
					store.parseDocument(doc);

					list.add(store);
				}

				return list.toArray(new RagialData[list.size()]);
			}
		};
		return callable;
	}

	/**
	 * Ensures that only one correct result will be returned for a given name. It must be run on a different thread since 
	 * it has a blocking call to the ExecuterService.
	 * 
	 * @param exactName - The exact name, word for word, character for character of the item to be searched
	 * @return RagialData - Forces only one correct result to be returned from the array. If data is not found, returns null
	 */
	public RagialData searchRagialSpecificly(final String exactName) {
		RagialData[] datas = null;
		try {
			datas = searchRagial(exactName).get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		for(RagialData data : datas) {
			if(data.name.equalsIgnoreCase(exactName)) {
				return data;
			}
		}
		return null;
	}

	/**
	 * Helper method for cases when RagialData[] is already available from prior call. Merely finds the specific item, or returns null.
	 * @param exactName - The exact name, word for word, character for character of the item to be searched
	 * @param datas - A RagialData[] obtained from a prior call. 
	 * @return RagialData - forces only one correct result to be returned from the array. If data is not found, returns null.
	 */
	public static RagialData searchRagialSpecificly(String exactName, RagialData[] datas) {
		for(RagialData data : datas) {
			if(data.name.equalsIgnoreCase(exactName)) {
				return data;
			}
		}
		return null;
	}

	/**
	 * <UNIMPLIMENTED>
	 * Wanted to create a method which directly requests Ragial.com to transfer only one result. Seems that cannot be done. 
	 * Thus, this method is remaining unimplemented until a better method comes to query Ragial.com for specific data. 
	 * Will only return null until that time.
	 * @param exactName
	 * @param datas
	 * @return RagialData : forces only one correct result to be returned from the array. If data is not found, returns null.
	 */
	private static RagialData searchRagialClosely(String exactName, RagialData[] datas) {
		/*
		int index = 0, plusIndex = 0, cardIndex = 0;
		String name = "";
		for(RagialData data : datas) {

			name = data.name;
			index = name.indexOf(exactName);
			plusIndex = name.indexOf('+');
			cardIndex = name.lastIndexOf('[');

			if(plusIndex == -1) {
				name = name.substring(2, name.length()).trim();
			}

			if(cardIndex == -1) {

			}

		}
		 */
		return null;
	}

	/**
	 * Calculates and returns a list of VendingNow items which satisfy the criteria that all items on this list
	 * must have a price lower than the average in the short term. ie : The item is on sale.
	 * @param name - Name of the item to be checked if it is on sale.
	 * @param datas - RagialData[] obtained from a prior call.
	 * @return A Future of an ArrayList of VendingNow items - List of items that are on sale in the short term..
	 */
	public Future<ArrayList<VendingNow>> getOnSaleItems(String name, final RagialData datas[]) {
		return getOnSaleItems(name, datas, false);
	}

	/**
	 * Calculates and returns a list of VendingNow items which satisfy the criteria that all items on this list
	 * must have a price lower than the average in the short term. ie : The item is on sale.
	 * @param name - Name of the item to be checked if it is on sale.
	 * @param data - RagialData obtained from a prior call.
	 * @return A Future of an ArrayList of VendingNow items : List of items that are on sale in the short term..
	 */
	public Future<ArrayList<VendingNow>> getOnSaleItems(String name, final RagialData data) {
		return getOnSaleItems(name, new RagialData[] { data });
	}


	/**
	 * Calculates and returns a list of VendingNow items which satisfy the criteria that all items on this list
	 * must have a price lower than the average in the short term (and long term as well, if checkOverLongTerm = true). ie : The item is on sale.
	 * @param name - Name of the item to be checked if it is on sale.
	 * @param datas - RagialData[] obtained from a prior call.
	 * @param checkOverLongTermAsWell - A boolean to request checking over long term prices as well.
	 * @return A Future of an ArrayList of VendingNow items - List of items that are on sale in the long term.
	 */
	public Future<ArrayList<VendingNow>> getOnSaleItems(final String name, final RagialData datas[], final boolean checkOverLongTermAsWell) {
		Callable<ArrayList<VendingNow>> callable = getOnSaleItemsCallable(name, datas, checkOverLongTermAsWell);

		if(isExecutorAvailable()) {
			return executer.submit(callable);
		}
		else {
			restartExecutor();
			return executer.submit(callable);
		}
	}

	/**
	 * Calculates and returns a list of VendingNow items which satisfy the criteria that all items on this list
	 * must have a price lower than the average in the short term (and long term as well, if checkOverLongTerm = true). eg : The item is on sale.
	 * @param name - Name of the item to be checked if it is on sale.
	 * @param datas - RagialData obtained from a prior call.
	 * @param checkOverLongTermAsWell - A boolean to request checking over long term prices as well.
	 * @return A Future of an ArrayList of VendingNow items - List of items that are on sale in the long term.
	 */
	public Future<ArrayList<VendingNow>> getOnSaleItems(final String name, final RagialData datas, final boolean checkOverLongTermAsWell) {
		Callable<ArrayList<VendingNow>> callable = getOnSaleItemsCallable(name, new RagialData[] { datas }, checkOverLongTermAsWell);

		if(isExecutorAvailable()) {
			return executer.submit(callable);
		}
		else {
			restartExecutor();
			return executer.submit(callable);
		}
	}

	/**
	 * Creates a callable for the getOnSalesItems functins
	 * @param name - Name of the item to be checked if it is on sale.
	 * @param datas - RagialData obtained from a prior call.
	 * @param checkOverLongTermAsWell - A boolean to request checking over long term prices as well.
	 * @return A Callable of an ArrayList of VendingNow items
	 */
	private Callable<ArrayList<VendingNow>> getOnSaleItemsCallable(final String name, final RagialData[] datas, final boolean checkOverLongTermAsWell) {
		Callable<ArrayList<VendingNow>> callable = new Callable<ArrayList<VendingNow>>() {

			@Override
			public ArrayList<VendingNow> call() throws Exception {
				ArrayList<VendingNow> list = new ArrayList<VendingNow>();
				ArrayList<VendingNow> dataList = null;

				for(RagialData data : datas) {
					dataList = data.vendingList;

					if(name.equals(data.name)) {
						if(!checkOverLongTermAsWell) {
							for(VendingNow test : dataList) {
								if(test.vendPrice <= data.shortAverage) {
									list.add(test);
								}
							}
						}
						else {
							for(VendingNow test : dataList) {
								if((data.shortAverage <= test.vendPrice && test.vendPrice <= data.longAverage) || (test.vendPrice <= data.shortAverage)) {
									list.add(test);
								}
							}
						}
						break;
					}
				}

				return list;
			}
		};
		return callable;
	}


	/**
	 * ExecutorService needs to be killed or it will run forever. Do not forget to call this method!
	 * Kills both its object as well as shutdowns all executing threads.
	 * @return true if executor was running and is now shutdown. False if already shutdown.
	 */
	public boolean destroyExecutor() {
		if(executer != null || !executer.isShutdown()) {
			executer.shutdown();
			executer = null;
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Restart Executor to process some new work on older reference to this object.
	 * @return true if executor was null and now a new Executor has been created, false if could not create or Executor was available
	 */
	private static boolean restartExecutor() {
		if(executer == null) {
			executer = Executors.newCachedThreadPool();
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Use this to check if a query can be made on Ragial.com with the ExecutorService
	 * @return true if Executor is available, else return false.
	 */
	public static boolean isExecutorAvailable() {
		return executer != null && !executer.isShutdown();
	}

	/**
	 * Ensures that the ExecutorService is destroyed when the RagialQueryMatcher object is killed. 
	 * This is only a fail safe method. It is still recommended to manually shutdown the executor service with {@link #destroyExecutor()} 
	 */
	@Override
	protected void finalize() throws Throwable {
		destroyExecutor();
		super.finalize();
	}


}
