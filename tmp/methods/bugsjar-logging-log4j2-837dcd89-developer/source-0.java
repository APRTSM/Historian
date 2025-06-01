    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        formattedMessage = in.readUTF();
        messagePattern = in.readUTF();
        baseName = in.readUTF();
        final int length = in.readInt();
        stringArgs = new String[length];
        for (int i = 0; i < length; ++i) {
            stringArgs[i] = in.readUTF();
        }
        logger = StatusLogger.getLogger();
        resourceBundle = null;
        argArray = null;
    }
    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        getFormattedMessage();
        out.writeUTF(formattedMessage);
        out.writeUTF(messagePattern);
        out.writeUTF(baseName);
        out.writeInt(argArray.length);
        stringArgs = new String[argArray.length];
        int i = 0;
        for (final Object obj : argArray) {
            stringArgs[i] = obj.toString();
            ++i;
        }
    }
