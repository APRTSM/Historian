        public Size apply(MethodVisitor methodVisitor, Context implementationContext, MethodDescription instrumentedMethod) {
            MethodDescription invokedMethod = methodLocator.resolve(implementationTarget.getInstrumentedType(), instrumentedMethod);
            if (!invokedMethod.isVisibleTo(implementationTarget.getInstrumentedType())) {
                throw new IllegalStateException("Cannot invoke " + invokedMethod + " from " + implementationContext.getInstrumentedType());
            }
            List<ArgumentLoader> argumentLoaders = new ArrayList<ArgumentLoader>(MethodCall.this.argumentLoaders.size());
            for (ArgumentLoader.Factory argumentLoader : MethodCall.this.argumentLoaders) {
                argumentLoaders.addAll(argumentLoader.make(implementationTarget.getInstrumentedType(), instrumentedMethod, invokedMethod));
            }
            ParameterList<?> parameters = invokedMethod.getParameters();
            Iterator<? extends ParameterDescription> parameterIterator = parameters.iterator();
            if (parameters.size() != argumentLoaders.size()) {
                throw new IllegalStateException(invokedMethod + " does not take " + argumentLoaders.size() + " arguments");
            }
            List<StackManipulation> argumentInstructions = new ArrayList<StackManipulation>(argumentLoaders.size());
            for (ArgumentLoader argumentLoader : argumentLoaders) {
                argumentInstructions.add(argumentLoader.resolve(parameterIterator.next(), assigner, typing));
            }
            StackManipulation.Size size = new StackManipulation.Compound(
                    targetHandler.resolve(invokedMethod, instrumentedMethod, implementationTarget.getInstrumentedType(), assigner, typing),
                    new StackManipulation.Compound(argumentInstructions),
                    methodInvoker.invoke(invokedMethod, implementationTarget),
                    terminationHandler.resolve(invokedMethod, instrumentedMethod, assigner, typing)
            ).apply(methodVisitor, implementationContext);
            return new Size(size.getMaximalSize(), instrumentedMethod.getStackSize());
        }
