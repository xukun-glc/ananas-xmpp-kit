package test.ananas.lib.axk_with_bp2ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import ananas.lib.axk.XmppClient;
import ananas.lib.axk.api.IExConnection;
import ananas.lib.axk.api.IExCore;
import ananas.lib.blueprint2.Blueprint2;
import ananas.lib.blueprint2.awt.util.IResponseChainNode;
import ananas.lib.blueprint2.dom.IDocument;
import ananas.lib.io.IConnector;
import ananas.lib.io.IStreamConnection;

public class MainFrame {

	private JFrame mFrame;
	private JTextArea mTextSend;
	private JTextArea mTextPrevSend;
	private JButton mBtnCopy2Send;
	private JButton mBtnSend;
	private final XmppClient mClient;
	private JTextField mTextTo;

	private StanzaTemplateSet mTempSet;

	public MainFrame(XmppClient client) {

		this.mClient = client;
		final IDocument doc = this.loadDocument(R.file.mainframe_xml);
		this.mFrame = (JFrame) doc.findTargetById(R.id.root);
		this.mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.mBtnSend = (JButton) doc.findTargetById(R.id.btn_send);
		this.mTextSend = (JTextArea) doc.findTargetById(R.id.text_send);
		this.mBtnCopy2Send = (JButton) doc
				.findTargetById(R.id.btn_copy_to_send);
		this.mTextPrevSend = (JTextArea) doc
				.findTargetById(R.id.text_prev_send);
		this.mTextTo = (JTextField) doc.findTargetById(R.id.text_to);

		MyCommandReg cmdReg = new MyCommandReg();
		IResponseChainNode chainNode = (IResponseChainNode) doc
				.findTargetById(R.id.resp_chain_node_3);
		chainNode.setHook(cmdReg);
		this.installCommands(cmdReg);
		this.installListeners();

	}

	private void installListeners() {
		this.mBtnSend.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				final String text = MainFrame.this.mTextSend.getText();
				MainFrame.this.mTextSend.setText("");
				MainFrame.this.mTextPrevSend.setText(text);
				if (text.length() > 0) {
					IExConnection conn = MainFrame.this._getConnectionAPI();
					conn.sendStanza(text);
				}
			}
		});
		this.mBtnCopy2Send.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String text = MainFrame.this.mTextPrevSend.getText();
				MainFrame.this.mTextSend.setText(text);
			}
		});
	}

	public IDocument loadDocument(String uri) {
		try {
			return Blueprint2.getInstance().loadDocument(uri);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public StanzaTemplateSet getTemplateSet() {
		StanzaTemplateSet ts = this.mTempSet;
		if (ts == null) {
			ts = new StanzaTemplateSet();
			ts.load();
			this.mTempSet = ts;
		}
		return ts;
	}

	private void installCommands(ICommandRegistrar cr) {

		cr.reg(R.command.conn_reset, new Runnable() {

			@Override
			public void run() {
				IExConnection conn = MainFrame.this._getConnectionAPI();
				conn.reset();
			}
		});
		cr.reg(R.command.conn_connect, new Runnable() {

			@Override
			public void run() {
				IExConnection conn = MainFrame.this._getConnectionAPI();
				conn.connect();
			}
		});
		cr.reg(R.command.conn_disconnect, new Runnable() {

			@Override
			public void run() {
				IExConnection conn = MainFrame.this._getConnectionAPI();
				conn.disconnect();
			}
		});
		cr.reg(R.command.conn_close, new Runnable() {

			@Override
			public void run() {
				IExConnection conn = MainFrame.this._getConnectionAPI();
				conn.close();
			}
		});
		cr.reg(R.command.view_clear_console, new Runnable() {

			@Override
			public void run() {
				System.out.println();
				System.out.println();
				System.out
						.println("************************************************************************************************************");
				System.out.println();
				System.out.println();
			}
		});
		// view ////////////////////////////////////////////////////
		cr.reg(R.command.view_print_roster, new Runnable() {

			@Override
			public void run() {
				System.out.println("...");
			}
		});
		cr.reg(R.command.view_print_presence_table, new Runnable() {

			@Override
			public void run() {
				System.out.println("...");
			}
		});
		// end ////////////////////////////////////////////////////

	}

	interface MyMacro {

		String stanza_attr_to = "${stanza_attr_to}";

	}

	protected void _loadXmlToSendBox(String url) {
		String to = this.mTextTo.getText();
		Properties prop = this.mTempLoader.getProperties();
		prop.setProperty(MyMacro.stanza_attr_to, to);
		String str = this.mTempLoader.get(url);
		this.mTextSend.setText(str);
	}

	protected IExConnection _getConnectionAPI() {
		IExConnection api = (IExConnection) this.mClient
				.getExAPI(IExConnection.class);
		return api;
	}

	interface ICommandRegistrar {

		void reg(String cmd, Runnable runn);
	}

	final MyTemplateCachedLoader mTempLoader = new MyTemplateCachedLoader();

	private class MyTemplateCachedLoader {

		final Map<String, String> mCache = new HashMap<String, String>();
		private final Properties mProperties = new Properties();

		public String get(String url) {
			final String raw = this.getRawString(url);
			return this._processMacro(raw);
		}

		private String _processMacro(String raw) {
			// "${xxxxx}"
			StringBuilder sb = new StringBuilder();
			StringBuilder sbm = new StringBuilder();
			char[] chs = raw.toCharArray();
			for (char ch : chs) {
				final int sbmLen = sbm.length();
				if (sbmLen > 0) {
					sbm.append(ch);
					if (sbmLen == 1) {
						if (ch == '{') {
							// start
						} else {
							// end
							sbm.setLength(0);
							sb.append(ch);
						}
					} else {
						if (ch == '}') {
							String key = sbm.toString();
							sbm.setLength(0);
							String value = this._processMacroWord(key);
							sb.append(value);
						}
					}
				} else {
					if (ch == '$') {
						sbm.append(ch);
					} else {
						sb.append(ch);
					}
				}

			}
			return sb.toString();
		}

		private String _processMacroWord(String key) {
			String value = this.mProperties.getProperty(key);
			if (value == null) {
				value = key;
			}
			return value;
		}

		public Properties getProperties() {
			return this.mProperties;
		}

		public String getRawString(String url) {
			String txt = this.mCache.get(url);
			if (txt == null) {
				txt = this.loadRawString(url);
				if (txt == null) {
					txt = txt + "";
				}
				this.mCache.put(url, txt);
			}
			return txt;
		}

		public String loadRawString(String url) {

			try {
				IExCore core = (IExCore) MainFrame.this.mClient
						.getExAPI(IExCore.class);
				IConnector con = core.getEnvironment().getBPEnvironment()
						.getConnector();
				IStreamConnection conn = (IStreamConnection) con.open(url);
				InputStream in = conn.getInputStream();
				byte[] buff = new byte[256];
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				for (int cb = in.read(buff); cb > 0; cb = in.read(buff)) {
					baos.write(buff, 0, cb);
				}
				String s = new String(baos.toByteArray(), "UTF-8");
				in.close();
				conn.close();
				return s;
			} catch (Exception e) {
				e.printStackTrace();
				return e.getLocalizedMessage();
			}
		}
	}

	private class MyCommandReg implements IResponseChainNode, ICommandRegistrar {

		private final Map<String, Runnable> mTable;

		public MyCommandReg() {
			this.mTable = new HashMap<String, Runnable>();
		}

		@Override
		public IResponseChainNode getNext() {
			return null;
		}

		@Override
		public void setNext(IResponseChainNode next) {
		}

		@Override
		public IResponseChainNode getHook() {
			return null;
		}

		@Override
		public void setHook(IResponseChainNode hook) {
		}

		@Override
		public boolean processEvent(ActionEvent e) {

			if (this._tryAsTemplate(e)) {
				return true;
			}

			String cmd = e.getActionCommand();
			Runnable runn = this.mTable.get(cmd);
			if (runn == null) {
				return false;
			} else {
				runn.run();
				return true;
			}
		}

		private boolean _tryAsTemplate(ActionEvent e) {
			String cmd = e.getActionCommand();
			StanzaTemplateSet ts = MainFrame.this.getTemplateSet();
			String raw = ts.findRawString(cmd);
			if (raw == null) {
				return false;
			}
			MainFrame.this._updateTempLoaderProperties();
			raw = MainFrame.this.mTempLoader._processMacro(raw);
			MainFrame.this.mTextSend.setText(raw);
			return true;
		}

		@Override
		public void reg(String cmd, Runnable runn) {
			if (this.mTable.containsKey(cmd)) {
				throw new RuntimeException("re-reg-command:" + cmd);
			}
			this.mTable.put(cmd, runn);
		}
	};

	public void show() {
		this.mFrame.setVisible(true);
	}

	public void _updateTempLoaderProperties() {
		Properties prop = this.mTempLoader.getProperties();
		prop.setProperty("${stanza_attr_to}", this.mTextTo.getText());
	}
}
