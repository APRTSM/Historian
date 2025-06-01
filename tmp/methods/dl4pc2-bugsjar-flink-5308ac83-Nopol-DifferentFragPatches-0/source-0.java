	public String getHostname() {
		if(hostName == null) {
			String fqdn = getFQDNHostname();
			if(this.fqdnHostNameIsIP) { // fqdn to hostname translation is pointless if FQDN is an ip address.
				hostName = fqdn;
			} else {
				hostName = NetUtils.getHostnameFromFQDN(fqdn);
			}
		}
		return hostName;
	}
