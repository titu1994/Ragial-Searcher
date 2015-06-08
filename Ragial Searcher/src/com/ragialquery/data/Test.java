package com.ragialquery.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import com.ragialquery.data.RagialData.VendingNow;

/**
 * A class which tests the RagialQueryMatcher class.
 * @author Somshubra Majumdar
 * @version 2.0
 * @since 1.0
 */
public class Test {

	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
		
		RagialQueryMatcher matcher = RagialQueryMatcher.getMatcher();
		//matcher.setSearchURL(RagialQueryMatcher.RAGIAL_URL_CLASSIC);
		
		System.out.println("Enter the name");
		
		BufferedReader bb = new BufferedReader(new InputStreamReader(System.in));
		String name = bb.readLine();
		
		long a = System.currentTimeMillis();
		RagialData[] datas = matcher.searchRagial(name).get();
		long b = System.currentTimeMillis();
		RagialData specificData = RagialQueryMatcher.searchRagialSpecificly(name, datas);
		long c = System.currentTimeMillis();
		
		System.out.println("Time to parse ragial.com : " + ((b-a)));
		System.out.println("Time to search specific : " + ((c-b)));
	
		System.out.println(specificData + "\n");
		
		System.out.println("On Sale Items: \n");
		ArrayList<VendingNow> sales = matcher.getOnSaleItems(name, specificData).get();
		
		for(VendingNow vend : sales) {
			System.out.println(vend);
			System.out.println();
		}
		
		matcher.destroyExecutor();
	}

}
