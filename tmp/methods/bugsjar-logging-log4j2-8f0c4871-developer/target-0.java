    private void writeObject(ObjectOutputStream aOutputStream
    ) throws IOException {
        //perform the default serialization for all non-transient, non-static fields
        aOutputStream.defaultWriteObject();
    }
    private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
        //always perform the default de-serialization first
        aInputStream.defaultReadObject();
        logger = new AbstractLoggerWrapper((AbstractLogger) LogManager.getLogger(name), name, null);
        converter = createConverter();
    }
