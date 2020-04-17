/*
 * Copyright (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;



/**
 * @author wuheng@otcaix.iscas.ac.cn
 * 
 * @version 1.0.0
 * @since   2020/3/3
 *
 */
public class IPMIClientTest {
	
	private static final String hostname = "192.168.96.22";
	 
    private static final String username = "admin";
 
    private static final String password = "password";
    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
//    	testGetStatus();
    	IPMILifecycleClient client = new IPMILifecycleClient(hostname, username, password);
    	System.out.println(client.powerOn());
//    	client.startMachine();
    	client.close();
    }
//	protected static void testGetStatus() throws Exception {
//		IPMIStatusClient client = new IPMIStatusClient(hostname, username, password);
//        System.out.println(client.getSensorData());
//        client.close();
//	}
 
}
