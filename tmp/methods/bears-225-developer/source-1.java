    private DeclaredCommandMethod(Method method, ExecutionSpecificParameters parameters) {

        LettuceAssert.notNull(method, "Method must not be null");
        LettuceAssert.notNull(parameters, "Parameters must not be null");

        this.method = method;
        this.returnType = ResolvableType.forMethodReturnType(method);
        this.parameters = parameters;
        this.futureExecution = Future.class.isAssignableFrom(getReturnType().getRawClass());
        this.reactiveExecution = ReactiveTypes.supports(getReturnType().getRawClass());

        Collections.addAll(arguments, method.getParameterTypes());

        ResolvableType actualReturnType = this.returnType;

        while (Future.class.isAssignableFrom(actualReturnType.getRawClass())
                || ReactiveTypes.supports(actualReturnType.getRawClass())) {
            ResolvableType[] generics = actualReturnType.getGenerics();

            if (generics.length != 1) {
                break;
            }

            actualReturnType = generics[0];
        }

        this.actualReturnType = actualReturnType;
    }
    public boolean isStreamingExecution() {
        return streamingExecution;
    }
    public ReactiveCommandSegmentCommandFactory(CommandSegments commandSegments, CommandMethod commandMethod,
            RedisCodec<?, ?> redisCodec, CommandOutputFactoryResolver outputResolver) {

        super(commandSegments, commandMethod, redisCodec, outputResolver);

        if (commandMethod.getParameters() instanceof ExecutionSpecificParameters) {

            ExecutionSpecificParameters executionAwareParameters = (ExecutionSpecificParameters) commandMethod.getParameters();

            if (executionAwareParameters.hasTimeoutIndex()) {
                throw new CommandCreationException(commandMethod, "Reactive command methods do not support Timeout parameters");
            }
        }
    }
    protected CommandOutputFactory resolveCommandOutputFactory(OutputSelector outputSelector) {

        CommandOutputFactory factory = getOutputResolver().resolveStreamingCommandOutput(outputSelector);

        if (factory != null) {
            streamingExecution = true;
            return factory;
        }

        return super.resolveCommandOutputFactory(outputSelector);
    }
