package com.ragialquery.data;

import com.ragialquery.data.RagialData.VendingNow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

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
		RagialData[] datas = matcher.searchRagial(name, true).get();
		long b = System.currentTimeMillis();
		RagialData specificData = RagialQueryMatcher.searchRagialSpecificly(name, datas);
		long c = System.currentTimeMillis();
		
		System.out.println("Time to parse ragial.com : " + ((b-a)));
		System.out.println("Time to search specific : " + ((c-b)));

        long d = System.currentTimeMillis();
        Vector<VendingNow> sales = matcher.getOnSaleItems(name, specificData).get();
        long e = System.currentTimeMillis();

        System.out.println("Time to get On Sale Items : " + (e-d));

		System.out.println("\n" + specificData);

        System.out.println("\nOn Sale Items: \n");

		for(VendingNow vend : sales) {
			System.out.println(vend);
			System.out.println();
		}
		
		matcher.destroyExecutor();
	}

}
