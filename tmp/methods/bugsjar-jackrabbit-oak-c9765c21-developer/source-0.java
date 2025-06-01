    public Iterator<DataIdentifier> getAllIdentifiers() {
        final String path = FilenameUtils.normalizeNoEndSeparator(getPath());
        return Files.fileTreeTraverser().postOrderTraversal(new File(getPath()))
                .filter(new Predicate<File>() {
                    @Override
                    public boolean apply(File input) {
                        return input.isFile() && !input.getParent().equals(path);
                    }
                })
                .transform(new Function<File, DataIdentifier>() {
                    @Override
                    public DataIdentifier apply(File input) {
                        return new DataIdentifier(input.getName());
                    }
                }).iterator();
    }
