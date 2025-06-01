    public static <S extends Serializable> SocketAppender<S> createAppender(@PluginAttr("host") final String host,
                                                @PluginAttr("port") final String portNum,
                                                @PluginAttr("protocol") final String protocol,
                                                @PluginAttr("reconnectionDelay") final String delay,
                                                @PluginAttr("immediateFail") final String immediateFail,
                                                @PluginAttr("name") final String name,
                                                @PluginAttr("immediateFlush") final String immediateFlush,
                                                @PluginAttr("suppressExceptions") final String suppress,
                                                @PluginElement("layout") Layout<S> layout,
                                                @PluginElement("filters") final Filter filter,
                                                @PluginAttr("advertise") final String advertise,
                                                @PluginConfiguration final Configuration config) {

        boolean isFlush = immediateFlush == null ? true : Boolean.valueOf(immediateFlush);
        boolean isAdvertise = advertise == null ? false : Boolean.valueOf(advertise);
        final boolean handleExceptions = suppress == null ? true : Boolean.valueOf(suppress);
        final boolean fail = immediateFail == null ? true : Boolean.valueOf(immediateFail);
        final int reconnectDelay = delay == null ? 0 : Integer.parseInt(delay);
        final int port = portNum == null ? 0 : Integer.parseInt(portNum);
        if (layout == null) {
            @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
            Layout<S> l = (Layout<S>) SerializedLayout.createLayout();
            layout = l;
        }

        if (name == null) {
            LOGGER.error("No name provided for SocketAppender");
            return null;
        }

        final Protocol p = EnglishEnums.valueOf(Protocol.class, protocol != null ? protocol : Protocol.TCP.name());
        if (p.equals(Protocol.UDP)) {
            isFlush = true;
        }

        final AbstractSocketManager manager = createSocketManager(p, host, port, reconnectDelay, fail, layout);
        if (manager == null) {
            return null;
        }

        return new SocketAppender<S>(name, layout, filter, manager, handleExceptions, isFlush,
                isAdvertise ? config.getAdvertiser() : null);
    }
