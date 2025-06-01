    private static List<ProcessorDefinition> getParentOutputs(ProcessorDefinition parent) {
        if (parent == null) {
            return null;
        }
        List<ProcessorDefinition> outputs = parent.getOutputs();
        if (outputs.size() >= 1) {
            // if the 1st output is abstract, then its onException,transacted,intercept etc so we should
            // get the 'actual' outputs from that
            if (outputs.get(0).isAbstract()) {
                outputs = outputs.get(0).getOutputs();
            }
        }
        return outputs;
    }
