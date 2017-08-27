//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.reactivej.dcf.worker.util;

import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.net.ServerSocketFactory;
import org.springframework.util.Assert;

public class SocketUtils {
    public static final int PORT_RANGE_MIN = 1024;
    public static final int PORT_RANGE_MAX = 65535;
    private static final Random random = new Random(System.currentTimeMillis());

    public SocketUtils() {
    }

    public static int findAvailableTcpPort() {
        return findAvailableTcpPort(1024);
    }

    public static int findAvailableTcpPort(int minPort) {
        return findAvailableTcpPort(minPort, '\uffff');
    }

    public static int findAvailableTcpPort(int minPort, int maxPort) {
        return SocketUtils.SocketType.TCP.findAvailablePort(minPort, maxPort);
    }

    public static SortedSet<Integer> findAvailableTcpPorts(int numRequested) {
        return findAvailableTcpPorts(numRequested, 1024, '\uffff');
    }

    public static SortedSet<Integer> findAvailableTcpPorts(int numRequested, int minPort, int maxPort) {
        return SocketUtils.SocketType.TCP.findAvailablePorts(numRequested, minPort, maxPort);
    }

    public static int findAvailableUdpPort() {
        return findAvailableUdpPort(1024);
    }

    public static int findAvailableUdpPort(int minPort) {
        return findAvailableUdpPort(minPort, '\uffff');
    }

    public static int findAvailableUdpPort(int minPort, int maxPort) {
        return SocketUtils.SocketType.UDP.findAvailablePort(minPort, maxPort);
    }

    public static SortedSet<Integer> findAvailableUdpPorts(int numRequested) {
        return findAvailableUdpPorts(numRequested, 1024, '\uffff');
    }

    public static SortedSet<Integer> findAvailableUdpPorts(int numRequested, int minPort, int maxPort) {
        return SocketUtils.SocketType.UDP.findAvailablePorts(numRequested, minPort, maxPort);
    }

    private static enum SocketType {
        TCP {
            protected boolean isPortAvailable(int port) {
                try {
                    ServerSocket ex = ServerSocketFactory.getDefault().createServerSocket(port);
                    ex.close();
                    return true;
                } catch (Exception var3) {
                    return false;
                }
            }
        },
        UDP {
            protected boolean isPortAvailable(int port) {
                try {
                    DatagramSocket ex = new DatagramSocket(port);
                    ex.close();
                    return true;
                } catch (Exception var3) {
                    return false;
                }
            }
        };

        private SocketType() {
        }

        protected abstract boolean isPortAvailable(int var1);

        private int findRandomPort(int minPort, int maxPort) {
            int portRange = maxPort - minPort;
            return minPort + SocketUtils.random.nextInt(portRange);
        }

        int findAvailablePort(int minPort, int maxPort) {
            Assert.isTrue(minPort > 0, "\'minPort\' must be greater than 0");
            Assert.isTrue(maxPort > minPort, "\'maxPort\' must be greater than \'minPort\'");
            Assert.isTrue(maxPort <= '\uffff', "\'maxPort\' must be less than or equal to 65535");
            int portRange = maxPort - minPort;
            int searchCounter = 0;

            int candidatePort;
            do {
                ++searchCounter;
                if(searchCounter > portRange) {
                    throw new IllegalStateException(String.format("Could not find an available %s port in the range [%d, %d] after %d attempts", new Object[]{this.name(), Integer.valueOf(minPort), Integer.valueOf(maxPort), Integer.valueOf(searchCounter)}));
                }

                candidatePort = this.findRandomPort(minPort, maxPort);
            } while(!this.isPortAvailable(candidatePort));

            return candidatePort;
        }

        SortedSet<Integer> findAvailablePorts(int numRequested, int minPort, int maxPort) {
            Assert.isTrue(minPort > 0, "\'minPort\' must be greater than 0");
            Assert.isTrue(maxPort > minPort, "\'maxPort\' must be greater than \'minPort\'");
            Assert.isTrue(maxPort <= '\uffff', "\'maxPort\' must be less than or equal to 65535");
            Assert.isTrue(numRequested > 0, "\'numRequested\' must be greater than 0");
            Assert.isTrue(maxPort - minPort >= numRequested, "\'numRequested\' must not be greater than \'maxPort\' - \'minPort\'");
            TreeSet availablePorts = new TreeSet();
            int attemptCount = 0;

            while(true) {
                ++attemptCount;
                if(attemptCount > numRequested + 100 || availablePorts.size() >= numRequested) {
                    if(availablePorts.size() != numRequested) {
                        throw new IllegalStateException(String.format("Could not find %d available %s ports in the range [%d, %d]", new Object[]{Integer.valueOf(numRequested), this.name(), Integer.valueOf(minPort), Integer.valueOf(maxPort)}));
                    } else {
                        return availablePorts;
                    }
                }

                availablePorts.add(Integer.valueOf(this.findAvailablePort(minPort, maxPort)));
            }
        }
    }
}
