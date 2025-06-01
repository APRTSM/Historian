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
    public PatternBuilder number(String s) {
        s = s.replace("dddd", "d{4}").replace("ddd", "d{3}").replace("dd", "d{2}");
        s = s.replace("xxxx", "x{4}").replace("xxx", "x{3}").replace("xx", "x{2}");

        s = s.replace("d", "\\d").replace("x", "[0-9a-fA-F]").replaceAll("([\\.])", "\\\\$1");
        s = s.replaceAll("\\|$", "\\\\|").replaceAll("^\\|", "\\\\|"); // special case for delimiter

        fragments.add(s);
        return this;
    }
