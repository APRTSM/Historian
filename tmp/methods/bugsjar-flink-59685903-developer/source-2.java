	public final void setOutputType(TypeInformation<OUT> outTypeInfo, ExecutionConfig executionConfig) {
		if (userFunction instanceof OutputTypeConfigurable) {
			@SuppressWarnings("unchecked")
			OutputTypeConfigurable<OUT> typeConfigurable = (OutputTypeConfigurable<OUT>) userFunction;
			typeConfigurable.setOutputType(outTypeInfo, executionConfig);
		}
	}
	public final void setOutputType(TypeInformation<OUT> outTypeInfo, ExecutionConfig executionConfig) {
		if (userFunction instanceof OutputTypeConfigurable) {
			@SuppressWarnings("unchecked")
			OutputTypeConfigurable<OUT> typeConfigurable = (OutputTypeConfigurable<OUT>) userFunction;
			typeConfigurable.setOutputType(outTypeInfo, executionConfig);
		}
	}
