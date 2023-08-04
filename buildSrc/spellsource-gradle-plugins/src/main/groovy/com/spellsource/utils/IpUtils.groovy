package com.spellsource.utils

import javax.annotation.Nonnull

class IpUtils {
  /**
   * Heuristically retrieves the primary networking interface for this device.
   *
   * @return A Java {@link NetworkInterface} object that can be used by {@link io.vertx.core.Vertx}.
   */
  public static NetworkInterface mainInterface() {
    ArrayList<NetworkInterface> interfaces;
    try {
      interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
    } catch (SocketException e) {
      throw new RuntimeException(e);
    }
    return interfaces.stream().filter(ni -> {
      var isLoopback = false;
      var supportsMulticast = false;
      var isVirtualbox = false;
      var isSelfAssigned = false;
      var isHyperV = false;
      var isVmWare = false;
      try {
        isSelfAssigned = ni.inetAddresses().anyMatch(i -> i.getHostAddress().startsWith("169"));
        isLoopback = ni.isLoopback();
        supportsMulticast = ni.supportsMulticast();
        isVirtualbox = ni.getDisplayName().contains("VirtualBox") || ni.getDisplayName().contains("Host-Only");
        isHyperV = ni.getDisplayName().contains("Hyper-V");
        isVmWare = ni.getDisplayName().contains("VMware");
      } catch (IOException failure) {
      }
      var hasIPv4 = ni.getInterfaceAddresses().stream().anyMatch(ia -> ia.getAddress() instanceof Inet4Address);
      return supportsMulticast && !isSelfAssigned && !isLoopback && !ni.isVirtual() && hasIPv4 && !isVirtualbox && !isHyperV && !isVmWare;
    }).sorted(Comparator.comparing(NetworkInterface::getName)).findFirst().orElse(null);
  }
  /**
   * Retrieves a local-network-accessible IPv4 address for this instance by heuristically picking the "primary" network
   * interface on this device.
   *
   * @return A string in the form of "192.168.0.1"
   */
  @Nonnull
  public static String getHostIpAddress() {
    try {
      var hostAddress = getInetv4Address();
      if (hostAddress == null) {
        return "127.0.0.1";
      }
      return hostAddress.getAddress().getHostAddress();
    } catch (Throwable ex) {
      throw new RuntimeException(ex);
    }
  }

  public static InterfaceAddress getInetv4Address() {
    return mainInterface().getInterfaceAddresses().stream().filter(ia -> ia.getAddress() instanceof Inet4Address).findFirst().orElse(null);
  }
}
