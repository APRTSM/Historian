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
