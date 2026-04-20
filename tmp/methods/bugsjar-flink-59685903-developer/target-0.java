	public void setOutputType(TypeInformation<OUT> outTypeInfo, ExecutionConfig executionConfig) {
		if (userFunction instanceof OutputTypeConfigurable) {
			OutputTypeConfigurable<OUT> outputTypeConfigurable = (OutputTypeConfigurable<OUT>) userFunction;

			outputTypeConfigurable.setOutputType(outTypeInfo, executionConfig);
		}
	}
