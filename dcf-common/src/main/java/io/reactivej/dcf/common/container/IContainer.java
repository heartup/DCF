package io.reactivej.dcf.common.container;

import java.io.IOException;

/***
 * @author heartup@gmail.com
 */
public interface IContainer {

	public void startContainer() throws IOException;

	public void killContainer();

}
