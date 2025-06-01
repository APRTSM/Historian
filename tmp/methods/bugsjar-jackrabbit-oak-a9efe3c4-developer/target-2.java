    protected String normalizePropertyName(String propertyName) {
        // TODO normalize the path (remove superfluous ".." and "." 
        // where possible)
        if (query == null) {
            return propertyName;
        }
        if (propertyName == null) {
            return null;
        }
        int slash = propertyName.indexOf('/');
        if (slash < 0) {
            return normalizeNonRelativePropertyName(propertyName);
        }
        // relative properties
        String relativePath = PathUtils.getParentPath(propertyName);
        if (relativePath.indexOf('*') >= 0) {
            StringBuilder buff = new StringBuilder();
            for (String p : PathUtils.elements(relativePath)) {
                if (!p.equals("*")) {
                    p = query.getOakPath(p);
                }
                if (p.length() > 0) {
                    if (buff.length() > 0) {
                        buff.append('/');
                    }
                    buff.append(p);
                }
            }
            relativePath = buff.toString();
        } else {
            relativePath = query.getOakPath(relativePath);
        }
        propertyName = PathUtils.getName(propertyName);
        propertyName = normalizeNonRelativePropertyName(propertyName);
        return PathUtils.concat(relativePath, propertyName);
    }
    public PropertyValue currentProperty() {
        PropertyValue p;
        if (propertyType == PropertyType.UNDEFINED) {
            p = selector.currentProperty(propertyName);
        } else {
            p = selector.currentProperty(propertyName, propertyType);
        }
        return p;
    }
    private void readOakProperties(ArrayList<PropertyValue> target, Tree t, String oakPropertyName, Integer propertyType) {
        while (true) {
            if (t == null || !t.exists()) {
                return;
            }
            int slash = oakPropertyName.indexOf('/');
            if (slash < 0) {
                break;
            }
            String parent = oakPropertyName.substring(0, slash);
            oakPropertyName = oakPropertyName.substring(slash + 1);
            if (parent.equals("..")) {
                t = t.isRoot() ? null : t.getParent();
            } else if (parent.equals(".")) {
                // same node
            } else if (parent.equals("*")) {
                for (Tree child : t.getChildren()) {
                    readOakProperties(target, child, oakPropertyName, propertyType);
                }
            } else {
                t = t.getChild(parent);
            }
        }
        if (!"*".equals(oakPropertyName)) {
            PropertyValue value = currentOakProperty(t, oakPropertyName, propertyType);
            if (value != null) {
                target.add(value);
            }
            return;
        }
          for (PropertyState p : t.getProperties()) {
              if (propertyType == null || p.getType().tag() == propertyType) {
                  PropertyValue v = PropertyValues.create(p);
                  target.add(v);
              }
          }
    }
    public PropertyValue currentOakProperty(String oakPropertyName) {
        return currentOakProperty(oakPropertyName, null);
    }
    public PropertyValue currentProperty(String propertyName, int propertyType) {
        String pn = normalizePropertyName(propertyName);
        return currentOakProperty(pn, propertyType);
    }
    private PropertyValue currentOakProperty(String oakPropertyName, Integer propertyType) {
        boolean asterisk = oakPropertyName.indexOf('*') >= 0;
        if (asterisk) {
            Tree t = currentTree();
            ArrayList<PropertyValue> list = new ArrayList<PropertyValue>();
            readOakProperties(list, t, oakPropertyName, propertyType);
            if (list.size() == 0) {
                return null;
            }
            ArrayList<String> strings = new ArrayList<String>();
            for (PropertyValue p : list) {
                Iterables.addAll(strings, p.getValue(Type.STRINGS));
            }
            return PropertyValues.newString(strings);
        }
        boolean relative = oakPropertyName.indexOf('/') >= 0;
        Tree t = currentTree();
        if (relative) {
            for (String p : PathUtils.elements(PathUtils.getParentPath(oakPropertyName))) {
                if (t == null) {
                    return null;
                }
                if (p.equals("..")) {
                    t = t.isRoot() ? null : t.getParent();
                } else if (p.equals(".")) {
                    // same node
                } else {
                    t = t.getChild(p);
                }
            }
            oakPropertyName = PathUtils.getName(oakPropertyName);
        }
        return currentOakProperty(t, oakPropertyName, propertyType);
    }
    private PropertyValue currentOakProperty(Tree t, String oakPropertyName, Integer propertyType) {
        PropertyValue result;
        if (t == null || !t.exists()) {
            return null;
        }
        if (oakPropertyName.equals(QueryImpl.JCR_PATH)) {
            String path = currentPath();
            String local = getLocalPath(path);
            if (local == null) {
                // not a local path
                return null;
            }
            result = PropertyValues.newString(local);
        } else if (oakPropertyName.equals(QueryImpl.JCR_SCORE)) {
            result = currentRow.getValue(QueryImpl.JCR_SCORE);
        } else if (oakPropertyName.equals(QueryImpl.REP_EXCERPT)) {
            result = currentRow.getValue(QueryImpl.REP_EXCERPT);
        } else {
            result = PropertyValues.create(t.getProperty(oakPropertyName));
        }
        if (result == null) {
            return null;
        }
        if (propertyType != null && result.getType().tag() != propertyType) {
            return null;
        }
        return result;
    }
