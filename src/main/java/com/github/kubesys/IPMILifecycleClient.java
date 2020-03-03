/*
 * Copyright (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;


import java.net.InetAddress;

import com.veraxsystems.vxipmi.api.async.ConnectionHandle;
import com.veraxsystems.vxipmi.api.sync.IpmiConnector;
import com.veraxsystems.vxipmi.coding.commands.IpmiVersion;
import com.veraxsystems.vxipmi.coding.commands.PrivilegeLevel;
import com.veraxsystems.vxipmi.coding.commands.chassis.ChassisControl;
import com.veraxsystems.vxipmi.coding.commands.chassis.GetChassisStatus;
import com.veraxsystems.vxipmi.coding.commands.chassis.GetChassisStatusResponseData;
import com.veraxsystems.vxipmi.coding.commands.chassis.PowerCommand;
import com.veraxsystems.vxipmi.coding.commands.session.SetSessionPrivilegeLevel;
import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;

/**
 * @author wuheng@otcaix.iscas.ac.cn
 * 
 * @version 1.0.0
 * @since   2020/3/3
 *
 */
public class IPMILifecycleClient {
	
    
    protected final IpmiConnector connector;
    
    protected final ConnectionHandle handle;
    
    protected final CipherSuite cs;
    
    protected final String username;
    
    protected final String password;
    
	public IPMILifecycleClient(String hostname, String username, String password) throws Exception {
		this(hostname, username, password, PrivilegeLevel.Administrator);
	}
	
	public IPMILifecycleClient(String hostname, String username, String password, PrivilegeLevel level) throws Exception {
		// 创建连接器
		this.username = username;
		this.password = password;
        this.connector = new IpmiConnector(0);
        this.handle = connector.createConnection(InetAddress.getByName(hostname));
        this.cs = connector.getAvailableCipherSuites(handle).get(3);
	}

	
	public void close() {
		 if (connector != null) {
             // 关闭会话
             try {
                 connector.closeSession(handle);
                 System.out.println("Session closed");
             } catch (Exception e) {
                 e.printStackTrace();
             }

             // 关闭连接管理器并且释放监听端口
             connector.tearDown();
             System.out.println("Connection manager closed");
         }
	}
	
	/*****************************************************************
	 * 
	 *                 Basic 
	 * @throws Exception 
	 * 
	 ******************************************************************/
	
	public boolean powerOn() throws Exception {

        /*
         * 向远程主机提供选定的密码套件和特权级别。从现在起，连接句柄将包含这些信息。
         */
        connector.getChannelAuthenticationCapabilities(handle, cs, PrivilegeLevel.Administrator);

        // 开始会话，提供用户名和密码，以及可选的BMC key(当且仅当远程主机拥有双key校验时使用)，否则这一参数填写null即可。
        connector.openSession(handle, username, password, null);

        // 发送一些信息并且读取响应
        GetChassisStatusResponseData rd = (GetChassisStatusResponseData) connector.sendMessage(handle,
                new GetChassisStatus(IpmiVersion.V20, cs, AuthenticationType.RMCPPlus));

        return rd.isPowerOn();

	}
	
	public void startMachine() throws Exception {
		if (powerOn()) {
			return;
		}
		
		connector.sendMessage(handle, new SetSessionPrivilegeLevel(IpmiVersion.V20, cs, AuthenticationType.RMCPPlus,
                PrivilegeLevel.Administrator));
		ChassisControl chassisControl = new ChassisControl(IpmiVersion.V20, cs, AuthenticationType.RMCPPlus, PowerCommand.PowerUp);;
		// 发送上下电请求消息
        connector.sendMessage(handle, chassisControl);
        
	}
	
	public void shutdownMachine() throws Exception {
		
		if (!powerOn()) {
			return;
		}
		
		connector.sendMessage(handle, new SetSessionPrivilegeLevel(IpmiVersion.V20, cs, AuthenticationType.RMCPPlus,
                PrivilegeLevel.Administrator));
		ChassisControl chassisControl = new ChassisControl(IpmiVersion.V20, cs, AuthenticationType.RMCPPlus, PowerCommand.PowerDown);
		// 发送上下电请求消息
        connector.sendMessage(handle, chassisControl);
		

        
	}
	
	
	// from https://www.jianshu.com/p/71e8bdc86715
    public void testChassisControll() {
        try {

            // 通过getAvailableCipherSuites()方法获取可用的密码套件列表, 并选择其中的一个将在会话中进一步使用
            CipherSuite cs = connector.getAvailableCipherSuites(handle).get(3);
            System.out.println("Cipher suite picked");

            /*
             * 向远程主机提供选定的密码套件和特权级别。从现在起，连接句柄将包含这些信息。
             */
            connector.getChannelAuthenticationCapabilities(handle, cs, PrivilegeLevel.Administrator);
            System.out.println("Channel authentication capabilities receivied");

            // 开始会话，提供用户名和密码，以及可选的BMC key(当且仅当远程主机拥有双key校验时使用)，否则这一参数填写null即可。
            connector.openSession(handle, username, password, null);
            System.out.println("Session open");

            // 发送一些信息并且读取响应
            GetChassisStatusResponseData rd = (GetChassisStatusResponseData) connector.sendMessage(handle,
                    new GetChassisStatus(IpmiVersion.V20, cs, AuthenticationType.RMCPPlus));

            System.out.println("Received answer");
            System.out.println("System power state is " + (rd.isPowerOn() ? "up" : "down"));

            // 设置会话权限级别为管理员，因为底层控制指令需要用到这一级别
            connector.sendMessage(handle, new SetSessionPrivilegeLevel(IpmiVersion.V20, cs, AuthenticationType.RMCPPlus,
                    PrivilegeLevel.Administrator));

            ChassisControl chassisControl = null;

            // 上电或者下电
            if (!rd.isPowerOn()) {
                chassisControl = new ChassisControl(IpmiVersion.V20, cs, AuthenticationType.RMCPPlus, PowerCommand.PowerUp);
            } else {
                chassisControl = new ChassisControl(IpmiVersion.V20, cs, AuthenticationType.RMCPPlus, PowerCommand.PowerDown);
            }

            // 发送上下电请求消息
            connector.sendMessage(handle, chassisControl);

        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
}
