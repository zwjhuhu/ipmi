/*
 * ConnectionTest.java 
 * Created on 2011-09-20
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vximpi.test;


import com.github.kubesys.IPMIClient;
import com.veraxsystems.vxipmi.connection.Connection;

/**
 * Tests for the {@link Connection} class.
 */
public class UITTest  {

	public static void main(String[] args) throws Exception {
		IPMIClient client = new IPMIClient("133.133.131.217", "admin", "admin");
		System.out.println(client.getMachine().getCurrent());
		client.startMachine();
		System.out.println(client.getMachine().getCurrent());
		client.shutdownMachine();
	}
	
}
