    public ChoiceDefinition endChoice() {
        // are we nested choice?
        ProcessorDefinition<?> def = this;
        if (def.getParent() instanceof WhenDefinition) {
            return (ChoiceDefinition) def.getParent().getParent();
        }

        // are we already a choice?
        if (def instanceof ChoiceDefinition) {
            return (ChoiceDefinition) def;
        }

        // okay end this and get back to the choice
        def = end();
        if (def instanceof WhenDefinition) {
            return (ChoiceDefinition) def.getParent();
        } else if (def instanceof OtherwiseDefinition) {
            return (ChoiceDefinition) def.getParent();
        } else {
            return (ChoiceDefinition) def;
        }
    }
