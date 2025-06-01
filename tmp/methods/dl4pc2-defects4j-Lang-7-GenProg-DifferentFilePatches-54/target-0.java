    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        ArrayList<L> serializableListeners = new ArrayList<L>();

        // don't just rely on instanceof Serializable:
        ObjectOutputStream testObjectOutputStream = new ObjectOutputStream(new ByteArrayOutputStream());
        for (L listener : listeners) {
            try {
                testObjectOutputStream.writeObject(listener);
                serializableListeners.add(listener);
            } catch (IOException exception) {
            }
        }
        /*
         * we can reconstitute everything we need from an array of our listeners,
         * which has the additional advantage of typically requiring less storage than a list:
         */
        objectOutputStream.writeObject(serializableListeners.toArray(prototypeArray));
    }
