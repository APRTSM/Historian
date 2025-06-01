	public <R> SingleOutputStreamOperator<R, ?> transform(String operatorName,
			TypeInformation<R> outTypeInfo, OneInputStreamOperator<OUT, R> operator) {
		DataStream<OUT> inputStream = this.copy();
		@SuppressWarnings({ "unchecked", "rawtypes" })
		SingleOutputStreamOperator<R, ?> returnStream = new SingleOutputStreamOperator(environment,
				operatorName, outTypeInfo, operator);

		return returnStream;
	}
