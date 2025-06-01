    private static <T> void doFindType(List<ProcessorDefinition<?>> outputs, Class<T> type, List<T> found) {
        if (outputs == null || outputs.isEmpty()) {
            return;
        }

        for (ProcessorDefinition out : outputs) {
            if (type.isInstance(out)) {
                found.add((T)out);
            }

            // send is much common
            if (out instanceof SendDefinition) {
                SendDefinition send = (SendDefinition) out;
                List<ProcessorDefinition<?>> children = send.getOutputs();
                doFindType(children, type, found);
            }

            // special for choice
            if (out instanceof ChoiceDefinition) {
                ChoiceDefinition choice = (ChoiceDefinition) out;
                for (WhenDefinition when : choice.getWhenClauses()) {
                    List<ProcessorDefinition<?>> children = when.getOutputs();
                    doFindType(children, type, found);
                }

                // otherwise is optional
                if (choice.getOtherwise() != null) {
                    List<ProcessorDefinition<?>> children = choice.getOtherwise().getOutputs();
                    doFindType(children, type, found);
                }
            }

            // special for try ... catch ... finally
            if (out instanceof TryDefinition) {
                TryDefinition doTry = (TryDefinition) out;
                List<ProcessorDefinition<?>> doTryOut = doTry.getOutputsWithoutCatches();
                doFindType(doTryOut, type, found);

                List<CatchDefinition> doTryCatch = doTry.getCatchClauses();
                for (CatchDefinition doCatch : doTryCatch) {
                    doFindType(doCatch.getOutputs(), type, found);
                }

                if (doTry.getFinallyClause() != null) {
                    doFindType(doTry.getFinallyClause().getOutputs(), type, found);
                }

                // do not check children as we already did that
                continue;
            }

            // special for some types which has special outputs
            if (out instanceof OutputDefinition) {
                OutputDefinition outDef = (OutputDefinition) out;
                List<ProcessorDefinition<?>> outDefOut = outDef.getOutputs();
                doFindType(outDefOut, type, found);

                // do not check children as we already did that
                continue;
            }

            // try children as well
            List<ProcessorDefinition<?>> children = out.getOutputs();
            doFindType(children, type, found);
        }
    }
