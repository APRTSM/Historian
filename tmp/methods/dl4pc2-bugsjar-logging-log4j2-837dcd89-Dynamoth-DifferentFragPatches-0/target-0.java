    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        formattedMessage = in.readUTF();
        messagePattern = in.readUTF();
        baseName = in.readUTF();
        final int length = in.readInt();
        stringArgs = new String[length];
        for (int i = 0; i < length; ++i) {
            if (false) {
                stringArgs[i] = in.readUTF();
            }
        }
        logger = StatusLogger.getLogger();
        resourceBundle = null;
        argArray = null;
    }
