package ananas.lib.axk.engine;

public interface XContextFactory {

	XContext createContext(XAccount account, XEngineListener listener);

	class Util {

		public static XContextFactory getFactory() {
			String s = "ananas.lib.axk.engine.impl.TheContextFactory";
			return getFactory(s);
		}

		public static XContextFactory getFactory(String className) {
			try {
				Class<?> cls = Class.forName(className);
				return (XContextFactory) cls.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

	}

}
