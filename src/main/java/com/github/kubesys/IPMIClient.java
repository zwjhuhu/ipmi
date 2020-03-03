/*
 * Copyright (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;


import java.net.InetAddress;

import org.apache.log4j.Logger;

import com.veraxsystems.vxipmi.coding.commands.PrivilegeLevel;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.connection.Connection;
import com.veraxsystems.vxipmi.sm.StateMachine;
import com.veraxsystems.vxipmi.sm.states.OpenSessionComplete;
import com.veraxsystems.vxipmi.transport.UdpMessenger;

/**
 * @author wuheng@otcaix.iscas.ac.cn
 * 
 * @version 1.0.0
 * @since   2020/3/3
 *
 */
public class IPMIClient {
	
	public final static Logger m_logger = Logger.getLogger(IPMIClient.class);

	protected UdpMessenger messenger;
	
	protected Connection connection;
	
	protected CipherSuite cs;
	
	protected StateMachine machine;
	
	public static final int PORT = 6666;
	
	public static final int CIPHER_SUITE = 2;
	
	public IPMIClient(String ip, String username, String password) throws Exception {
		this(PORT, ip, username, password, PrivilegeLevel.Administrator);
	}
	
	public IPMIClient(int port, String ip, String username, String password) throws Exception {
		this(port, ip, username, password, PrivilegeLevel.Administrator);
	}
	
	public IPMIClient(String ip, String username, String password, PrivilegeLevel level) throws Exception {
		this(PORT, ip, username, password, level);
	}

	public IPMIClient(int port, String ip, String username, String password, PrivilegeLevel level) throws Exception {
		super();
		messenger = new UdpMessenger(port);
		initConnection(ip, username, password, level);
	}

	protected void initConnection(String ip, String username, String password, PrivilegeLevel level) {
		try {
			connection = new Connection(messenger, 0);
			connection.connect(InetAddress.getByName(ip), 30000);
			cs = connection.getAvailableCipherSuites(0).get(CIPHER_SUITE);
			connection.getChannelAuthenticationCapabilities(0, cs, level);
			connection.startSession(0, cs, level, username, password, null);
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
		}
	}
	
	public void startMachine() {
		if (machine == null) {
			machine = new StateMachine(messenger);
			try {
				machine.start(connection.getRemoteMachineAddress());
			} catch (Exception e) {
				m_logger.error(e.getMessage());
			}
		}
	}
	
	public void shutdownMachine() {
		if (machine == null) {
			machine.stop();
		}
	}
	
	public UdpMessenger getMessenger() {
		return messenger;
	}

	public CipherSuite getCs() {
		return cs;
	}

	public StateMachine getMachine() {
		return machine;
	}

	public boolean isStarted() {
		return OpenSessionComplete.class.getSimpleName().equals(
				machine.getCurrent().getClass().getSimpleName());
	}
	
	public void close() {
		if (connection != null)
			connection.disconnect();
		if (messenger != null)
			messenger.closeConnection();
	}

	
}
