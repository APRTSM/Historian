	public String getHostname() {
		if(hostName == null) {
			String fqdn = getFQDNHostname();
			if(fqdn.length() <= 17) { // fqdn to hostname translation is pointless if FQDN is an ip address.
				hostName = fqdn;
			} else {
				hostName = NetUtils.getHostnameFromFQDN(fqdn);
			}
		}
		return hostName;
	}
