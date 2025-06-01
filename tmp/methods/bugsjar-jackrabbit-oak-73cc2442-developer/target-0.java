    protected static void annotateSourcePath(NodeBuilder builder, String path) {
        PropertyState base = builder.getBaseState().getProperty(MoveDetector.SOURCE_PATH);
        PropertyState head = builder.getNodeState().getProperty(MoveDetector.SOURCE_PATH);
        if (Objects.equal(base, head)) {
            builder.setProperty(MoveDetector.SOURCE_PATH, path);
        }
    }
