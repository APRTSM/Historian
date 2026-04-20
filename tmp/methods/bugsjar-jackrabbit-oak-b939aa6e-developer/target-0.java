    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException {
        // check element name
        ImportState state = stack.peek();
        if (namespaceURI.equals(NamespaceConstants.NAMESPACE_SV) && "node".equals(localName)) {
            // sv:node element
            if (!state.started) {
                // need to start & end current node
                processNode(state, true, true);
                state.started = true;
            } else {
                // need to end current node
                processNode(state, false, true);
            }
            // pop current state from stack
            stack.pop();
        } else if (namespaceURI.equals(NamespaceConstants.NAMESPACE_SV) && "property".equals(localName)) {
            // sv:property element

            // check if all system properties (jcr:primaryType, jcr:uuid etc.)
            // have been collected and create node as necessary primaryType
            if (currentPropName != null
                    && currentPropName.getNamespaceUri().equals(NamespaceRegistry.NAMESPACE_JCR)
                    && currentPropName.getLocalName().equals("primaryType")) {
                BufferedStringValue val = currentPropValues.get(0);
                String s = null;
                try {
                    s = val.retrieve();
                    state.nodeTypeName = new NameInfo(s).getRepoQualifiedName();
                } catch (IOException e) {
                    throw new SAXException(new InvalidSerializedDataException("illegal node type name: " + s, e));
                } catch (RepositoryException e) {
                    throw new SAXException(new InvalidSerializedDataException("illegal node type name: " + s, e));
                }
            } else if (currentPropName != null
                    && currentPropName.getNamespaceUri().equals(NamespaceRegistry.NAMESPACE_JCR)
                    && currentPropName.getLocalName().equals("mixinTypes")) {
                if (state.mixinNames == null) {
                    state.mixinNames = new ArrayList<String>(currentPropValues.size());
                }
                for (BufferedStringValue val : currentPropValues) {
                    String s = null;
                    try {
                        s = val.retrieve();
                        state.mixinNames.add(new NameInfo(s).getRepoQualifiedName());
                    } catch (IOException ioe) {
                        throw new SAXException("error while retrieving value", ioe);
                    } catch (RepositoryException e) {
                        throw new SAXException(new InvalidSerializedDataException("illegal mixin type name: " + s, e));
                    }
                }
            } else if (currentPropName != null
                    && currentPropName.getNamespaceUri().equals(NamespaceRegistry.NAMESPACE_JCR)
                    && currentPropName.getLocalName().equals("uuid")) {
                BufferedStringValue val = currentPropValues.get(0);
                try {
                    state.uuid = val.retrieve();
                } catch (IOException ioe) {
                    throw new SAXException("error while retrieving value", ioe);
                }
            } else {
                if (currentPropMultipleStatus == PropInfo.MultipleStatus.UNKNOWN
                        && currentPropValues.size() != 1) {
                    currentPropMultipleStatus = PropInfo.MultipleStatus.MULTIPLE;
                }
                PropInfo prop = new PropInfo(
                        currentPropName == null ? null : currentPropName.getRepoQualifiedName(),
                        currentPropType,
                        currentPropValues,
                        currentPropMultipleStatus);
                state.props.add(prop);
            }
            // reset temp fields
            currentPropValues.clear();
        } else if (namespaceURI.equals(NamespaceConstants.NAMESPACE_SV) && "value".equals(localName)) {
            // sv:value element
            currentPropValues.add(currentPropValue);
            // reset temp fields
            currentPropValue = null;
        } else {
            throw new SAXException(new InvalidSerializedDataException("invalid element in system view xml document: " + localName));
        }
    }
