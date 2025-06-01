    public Type id(String id) {
        if (isOutputSupported() && getOutputs().isEmpty()) {
            // set id on this
            setId(id);
        } else {

            // set it on last output as this is what the user means to do
            // for Block(s) with non empty getOutputs() the id probably refers
            //  to the last definition in the current Block
            List<ProcessorDefinition<?>> outputs = getOutputs();
            if (!blocks.isEmpty()) {
                if (blocks.getLast() instanceof ProcessorDefinition) {
                    ProcessorDefinition<?> block = (ProcessorDefinition<?>)blocks.getLast();
                    if (!block.getOutputs().isEmpty()) {
                        outputs = block.getOutputs();
                    }
                }
            }
            if (!getOutputs().isEmpty()) {
                outputs.get(outputs.size() - 1).setId(id);
            } else {
                // the output could be empty
                setId(id);
            }
        }

        return (Type) this;
    }
