package ananas.axk2.engine.impl;

import ananas.axk2.core.XmppStatus;
import ananas.axk2.engine.dom_wrapper.DOMWrapperImplementation;

public interface XThreadRuntime extends XLifeCycle {

	XSuperConnection getParent();

	XmppStatus getPhase();

	void setPhase(XmppStatus newPhase);

	DOMWrapperImplementation getDOMWrapperImplementation();

}