    public DeviceSession getDeviceSession(Channel channel, SocketAddress remoteAddress, String... uniqueIds) {
        if (channel.getPipeline().get(HttpRequestDecoder.class) != null
                || Context.getConfig().getBoolean("decoder.ignoreSessionCache")) {
            long deviceId = findDeviceId(remoteAddress, uniqueIds);
            if (deviceId != 0) {
                if (Context.getConnectionManager() != null) {
                    Context.getConnectionManager().addActiveDevice(deviceId, protocol, channel, remoteAddress);
                }
                return new DeviceSession(deviceId);
            } else {
                return null;
            }
        }
        if (channel instanceof DatagramChannel) {
            long deviceId = findDeviceId(remoteAddress, uniqueIds);
            DeviceSession deviceSession = addressDeviceSessions.get(remoteAddress);
            if (deviceSession != null && (deviceSession.getDeviceId() == deviceId || uniqueIds.length == 0)) {
                return deviceSession;
            } else if (deviceId != 0) {
                deviceSession = new DeviceSession(deviceId);
                addressDeviceSessions.put(remoteAddress, deviceSession);
                if (Context.getConnectionManager() != null) {
                    Context.getConnectionManager().addActiveDevice(deviceId, protocol, channel, remoteAddress);
                }
                return deviceSession;
            } else {
                return null;
            }
        } else {
            if (channelDeviceSession == null) {
                long deviceId = findDeviceId(remoteAddress, uniqueIds);
                if (deviceId != 0) {
                    channelDeviceSession = new DeviceSession(deviceId);
                    if (Context.getConnectionManager() != null) {
                        Context.getConnectionManager().addActiveDevice(deviceId, protocol, channel, remoteAddress);
                    }
                }
            }
            return channelDeviceSession;
        }
    }
    public PatternBuilder expression(String s) {
        s = s.replaceAll("\\|$", "\\\\|"); // special case for delimiter

        fragments.add(s);
        return this;
    }
