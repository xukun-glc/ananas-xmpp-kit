package ananas.axk2.engine.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLSocketFactory;

import ananas.axk2.core.XmppAccount;
import ananas.axk2.engine.XEngineConnector;
import ananas.axk2.engine.XEngineContext;
import ananas.axk2.engine.api.XEngineRuntimeContext;
import ananas.axk2.engine.api.XSubConnection;

public class ErcTLS implements XEngineRuntimeContext {

	private final XSubConnection _sub_conn;
	private SocketAgent _sock_agent;
	private final XEngineRuntimeContext _parent;

	public ErcTLS(XEngineRuntimeContext erc) {
		this._sub_conn = erc.getSubConnection();
		this._parent = erc;
	}

	@Override
	public SocketAgent openSocket() throws IOException,
			GeneralSecurityException {
		SocketAgent sa = this._sock_agent;
		if (sa == null) {
			Socket sock = this.__openTLS();
			sa = new DefaultSocketAgent(sock);
			this._sock_agent = sa;
		}
		return sa;
	}

	private Socket __openTLS() throws IOException, GeneralSecurityException {
		final SocketAgent sockOld = this._parent.openSocket();
		final XEngineContext context = this._parent.getSubConnection()
				.getParent().getParent().getContext();
		final XEngineConnector connector = context.getConnector();
		SSLSocketFactory sockFactory = connector.getSSLContext()
				.getSocketFactory();
		XmppAccount account = context.getAccount();
		InetSocketAddress addr = connector.getAddressByAccount(account);
		String host = addr.getHostName();
		int port = addr.getPort();
		Socket sockNew = sockFactory.createSocket(sockOld.getSocket(), host,
				port, true);
		return sockNew;
	}

	@Override
	public XSubConnection getSubConnection() {
		return this._sub_conn;
	}

}