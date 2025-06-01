	public void read(DataInputView in) throws IOException {

		final int addr_length = in.readInt();
		byte[] address = new byte[addr_length];
		in.readFully(address);
		
		this.dataPort = in.readInt();
		
		this.fqdnHostName = StringUtils.readNullableString(in);
		this.hostName = StringUtils.readNullableString(in);
		this.fqdnHostNameIsIP = in.readBoolean();

		try {
			this.inetAddress = InetAddress.getByAddress(address);
		} catch (UnknownHostException e) {
			throw new IOException("This lookup should never fail.", e);
		}
	}
	public void write(final DataOutputView out) throws IOException {
		out.writeInt(this.inetAddress.getAddress().length);
		out.write(this.inetAddress.getAddress());
		
		out.writeInt(this.dataPort);
		
		StringUtils.writeNullableString(fqdnHostName, out);
		StringUtils.writeNullableString(hostName, out);
		out.writeBoolean(fqdnHostNameIsIP);
	}
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
	public InstanceConnectionInfo(InetAddress inetAddress, int dataPort) {
		if (inetAddress == null) {
			throw new IllegalArgumentException("Argument inetAddress must not be null");
		}
		if (dataPort <= 0) {
			throw new IllegalArgumentException("Argument dataPort must be greater than zero");
		}

		this.dataPort = dataPort;
		this.inetAddress = inetAddress;
		
		// get FQDN hostname on this TaskManager.
		try {
			this.fqdnHostName = this.inetAddress.getCanonicalHostName();
		} catch (Throwable t) {
			LOG.warn("Unable to determine hostname for TaskManager. The performance might be degraded since HDFS input split assignment is not possible");
			if(LOG.isDebugEnabled()) {
				LOG.debug("getCanonicalHostName() Exception", t);
			}
			// could not determine host name, so take IP textual representation
			this.fqdnHostName = inetAddress.getHostAddress();
			this.fqdnHostNameIsIP = true;
		}
	}
