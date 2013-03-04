package test.ananas.lib.axk;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ananas.lib.axk.DefaultXmppAccount;
import ananas.lib.axk.DefaultXmppAddress;
import ananas.lib.axk.XmppAccount;
import ananas.lib.axk.XmppClient;
import ananas.lib.axk.XmppClientFactory;
import ananas.lib.axk.XmppEnvironment;
import ananas.lib.axk.XmppEvent;
import ananas.lib.axk.XmppEventListener;
import ananas.lib.axk.XmppUtil;
import ananas.lib.axk.api.IExShell;
import ananas.lib.axk.command.TraceCommand;
import ananas.lib.axk.security.AXKSecurityListener;
import ananas.lib.axk.security.AXKSecurityManager;
import ananas.lib.util.log4j.AbstractLoggerFactory;

public class Main implements XmppEventListener, Runnable {

	private final static Logger logger = (new AbstractLoggerFactory() {
	}).getLogger();

	// ..getLogger(new Object() { });

	public static void main(String[] arg) {

		logger.setLevel(Level.INFO);

		Main main = new Main();
		javax.swing.SwingUtilities.invokeLater(main);
	}

	@Override
	public void run() {

		logger.trace(this + ".begin");

		Properties prop = new Properties();
		try {
			prop.load(this.getClass().getResourceAsStream("account.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<XmppAccount> accountList = new ArrayList<XmppAccount>();
		{
			// 0: axktest@jabber.org ,normal
			DefaultXmppAccount account = new DefaultXmppAccount();
			account.address = new DefaultXmppAddress("axktest@jabber.org");
			account.password = "12345678";
			account.host = "jabber.org";
			account.resource = this.getClass().getName();
			account.useSSL = false;
			account.port = account.useSSL ? 5223 : 5222;
			accountList.add(account);
		}
		{
			// 1: axktest@jabber.org ,useSSL
			DefaultXmppAccount account = new DefaultXmppAccount();
			account.address = new DefaultXmppAddress("axktest@jabber.org");
			account.password = "12345678";
			account.host = "jabber.org";
			account.resource = this.getClass().getName();
			account.useSSL = true;
			account.port = account.useSSL ? 5223 : 5222;
			accountList.add(account);
		}
		{
			// 2: xk19850217@gmail.com ,normal
			DefaultXmppAccount account = new DefaultXmppAccount();
			account.address = new DefaultXmppAddress("xk19850217@gmail.com");
			account.password = "xk12345678";
			account.host = "talk.google.com";
			account.resource = this.getClass().getName();
			account.useSSL = false;
			account.port = account.useSSL ? 5223 : 5222;
			accountList.add(account);
		}
		{
			// 3: xk19850217@gmail.com ,useSSL
			DefaultXmppAccount account = new DefaultXmppAccount();
			account.address = new DefaultXmppAddress("xk19850217@gmail.com");
			account.password = "xk12345678";
			account.host = "talk.google.com";
			account.resource = this.getClass().getName();
			account.useSSL = true;
			account.port = account.useSSL ? 5223 : 5222;
			accountList.add(account);
		}
		{
			// 4: other , normal

			DefaultXmppAccount account = new DefaultXmppAccount();
			account.address = new DefaultXmppAddress(prop.getProperty("jid"));
			account.password = prop.getProperty("password");
			account.host = prop.getProperty("host");
			account.resource = prop.getProperty("resource");
			account.port = Integer.parseInt(prop.getProperty("port"));
			account.useSSL = (account.port == 5223);
			accountList.add(account);
		}
		XmppAccount account = accountList.get(Integer.parseInt(prop
				.getProperty("select")));

		XmppEnvironment envi = XmppUtil.getDefaultEnvironment();
		this.initSecurity(envi.getSecurityManager());
		XmppClientFactory factory = envi.getClientFactory();
		XmppClient client = factory.newClient(account);
		client.setXmppEventListener(this);

		// trace
		TraceCommand cmd = new TraceCommand();
		client.dispatch(cmd);

		IExShell shell = (IExShell) client.getExAPI(IExShell.class);

		// show account
		XmppAccount acc = shell.getAccount();
		logger.info("account = " + acc);

		// connect
		shell.connect();

		MainFrame mf = new MainFrame(client);
		mf.setVisible(true);

		// end
		logger.trace(this + ".end");
	}

	private void initSecurity(AXKSecurityManager sm) {
		sm.setSecurityListener(new AXKSecurityListener() {

			@Override
			public void onX509CertificateException(CertificateException e,
					X509Certificate[] certificates, String authType)
					throws CertificateException {
			}
		});
	}

	@Override
	public void onEvent(XmppEvent event) {
		logger.trace("onEvent:" + event);
	}
}
