    private static List<ProcessorDefinition> getParentOutputs(ProcessorDefinition parent) {
        if (parent == null) {
            return null;
        }
        List<ProcessorDefinition> outputs = parent.getOutputs();
        if (outputs.size() == 1 && outputs.get(0).isAbstract()) {
            // if the output is abstract then get its output, as
            outputs = outputs.get(0).getOutputs();
        }
        return outputs;
    }
