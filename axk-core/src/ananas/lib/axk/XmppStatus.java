package ananas.lib.axk;

public interface XmppStatus {

	XmppStatus init = F._new("init");
	XmppStatus online = F._new("online");
	XmppStatus offline = F._new("offline");
	XmppStatus dropped = F._new("dropped");
	XmppStatus logining = F._new("logining");
	XmppStatus error = F._new("error");
	XmppStatus closed = F._new("closed");

	class F {

		private static XmppStatus _new(String string) {
			return new MyImpl(string);
		}

		private static class MyImpl implements XmppStatus {

			private final String mText;

			public MyImpl(String string) {
				this.mText = string;
			}

			public String toString() {
				return this.mText;
			}
		}
	}
}