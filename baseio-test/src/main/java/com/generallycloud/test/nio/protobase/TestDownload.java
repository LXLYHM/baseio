package com.generallycloud.test.nio.protobase;

import com.alibaba.fastjson.JSONObject;
import com.generallycloud.nio.codec.protobase.ProtobaseProtocolFactory;
import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.OnReadFuture;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.container.FileReceiveUtil;
import com.generallycloud.nio.container.FixedSession;
import com.generallycloud.nio.container.SimpleIOEventHandle;
import com.generallycloud.nio.container.protobase.example.TestDownloadServlet;
import com.generallycloud.nio.protocol.ReadFuture;
import com.generallycloud.test.nio.common.IoConnectorUtil;

public class TestDownload {
	
	public static void main(String[] args) throws Exception {

		String serviceName = TestDownloadServlet.SERVICE_NAME;
		
		String fileName = "upload-flashmail-2.4.exe";
		
		JSONObject j = new JSONObject();
		j.put(FileReceiveUtil.FILE_NAME, fileName);
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IoConnectorUtil.getTCPConnector(eventHandle);
		
		connector.getContext().setProtocolFactory(new ProtobaseProtocolFactory());

		FixedSession session = new FixedSession(connector.connect());
		
		final FileReceiveUtil fileReceiveUtil = new FileReceiveUtil("download-");
		
		session.listen(serviceName, new OnReadFuture() {
			
			@Override
			public void onResponse(SocketSession session, ReadFuture future) {
				
				try {
					fileReceiveUtil.accept(session, (ProtobaseReadFuture) future,false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		long old = System.currentTimeMillis();
		
		session.write(serviceName, j.toJSONString());
		
		System.out.println("Time:"+(System.currentTimeMillis() - old));
		
		ThreadUtil.sleep(5000);
		
		CloseUtil.close(connector);
		
	}
}