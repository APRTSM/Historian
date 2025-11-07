    public <T extends TreeNode> T readTree(JsonParser p) throws IOException {
        _config.initialize(p);
if (_schema != null) {
p.setSchema(_schema);
}
JsonToken t = p.getCurrentToken();
if (t == null) {
t = p.nextToken();
if (t == null) {
return null;
}

}


return (T) _bindAsTree(p);
    }
