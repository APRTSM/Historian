	public void read(DataInputView in) throws IOException {

		final int addr_length = in.readInt();
		byte[] address = new byte[addr_length];
		in.readFully(address);
		
		this.dataPort = in.readInt();
		
		this.fqdnHostName = StringUtils.readNullableString(in);
		this.hostName = StringUtils.readNullableString(in);

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
		}
		catch (Throwable t) {
			LOG.warn("Unable to determine the canonical hostname. Input split assignment (such as " +
					"for HDFS files) may be non-local when the canonical hostname is missing.");
			LOG.debug("getCanonicalHostName() Exception:", t);
			this.fqdnHostName = this.inetAddress.getHostAddress();
		}

		if (this.fqdnHostName.equals(this.inetAddress.getHostAddress())) {
			// this happens when the name lookup fails, either due to an exception,
			// or because no hostname can be found for the address
			// take IP textual representation
			this.hostName = this.fqdnHostName;
			LOG.warn("No hostname could be resolved for the IP address {}, using IP address as host name. "
					+ "Local input split assignment (such as for HDFS files) may be impacted.");
		}
		else {
			this.hostName = NetUtils.getHostnameFromFQDN(this.fqdnHostName);
		}
	}
