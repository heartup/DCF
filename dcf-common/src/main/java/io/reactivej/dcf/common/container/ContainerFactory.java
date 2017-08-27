package io.reactivej.dcf.common.container;

import io.reactivej.dcf.common.container.process.JavaProcessContainer;
import io.reactivej.dcf.common.container.process.SubProcessExecuteParams;

/***
 * @author heartup@gmail.com
 */
public class ContainerFactory {

    public static IContainer createJavaProcessContainer(SubProcessExecuteParams params) {
        return new JavaProcessContainer(params);
    }
}
