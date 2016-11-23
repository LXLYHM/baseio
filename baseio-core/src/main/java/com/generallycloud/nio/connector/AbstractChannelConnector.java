package com.generallycloud.nio.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.AbstractChannelService;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.UnsafeSession;
import com.generallycloud.nio.component.concurrent.EventLoopThread;
import com.generallycloud.nio.configuration.ServerConfiguration;

public abstract class AbstractChannelConnector extends AbstractChannelService implements ChannelConnector {

	protected boolean			active		= false;
	protected ReentrantLock		activeLock	= new ReentrantLock();
	protected InetSocketAddress	serverAddress;
	protected UnsafeSession		session;
	protected long			timeout		= 3000;
	
	private Logger 			logger 		= LoggerFactory.getLogger(AbstractChannelConnector.class);
	
	public AbstractChannelConnector(BaseContext context) {
		super(context);
	}

	protected abstract EventLoopThread getSelectorLoopThread();

	public void close() throws IOException {
		if (session == null) {
			physicalClose();
			return;
		}
		CloseUtil.close(session);
	}
	
	public void physicalClose() throws IOException {
		
		//FIXME always true
		if (session.isInSelectorLoop()) {
			ThreadUtil.execute(new Runnable() {
				
				public void run() {
					doPhysicalClose();
				}
			});
			return;
		}
		
		doPhysicalClose();
		
	}

	private void doPhysicalClose(){

		ReentrantLock lock = this.activeLock;

		lock.lock();

		try {

			doPhysicalClose0();

		} finally {

			active = false;

			LifeCycleUtil.stop(context);

			lock.unlock();
		}
	}

	protected abstract void doPhysicalClose0();

	public Session connect() throws IOException {

		ReentrantLock lock = this.activeLock;

		lock.lock();

		try {

			if (active) {
				return getSession();
			}

			if (context == null) {
				throw new IllegalArgumentException("null nio context");
			}
			
			ServerConfiguration configuration = context.getServerConfiguration();
			
			configuration.setSERVER_CORE_SIZE(1);
			
			String SERVER_HOST = configuration.getSERVER_HOST();
			
			int SERVER_PORT = configuration.getSERVER_PORT();

			this.context.setChannelService(this);
			
			LifeCycleUtil.start(context);

			this.serverAddress = new InetSocketAddress(SERVER_HOST, SERVER_PORT);

			this.connect(context, serverAddress);
			
			LoggerUtil.prettyNIOServerLog(logger, "已连接到远程服务器 @{}",getServerSocketAddress());
			
			this.session.fireOpend();

			this.active = true;
			
			return getSession();

		} finally {

			lock.unlock();
		}
	}

	protected abstract void connect(BaseContext context, InetSocketAddress socketAddress) throws IOException;

	public Session getSession() {
		return session;
	}

	public boolean isConnected() {
		return session != null && session.isOpened();
	}

	public boolean isActive() {
		return isConnected();
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
}
