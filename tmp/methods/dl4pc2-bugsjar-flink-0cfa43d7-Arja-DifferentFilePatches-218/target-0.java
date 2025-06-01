	public <R> SingleOutputStreamOperator<R, ?> transform(String operatorName,
			TypeInformation<R> outTypeInfo, OneInputStreamOperator<OUT, R> operator) {
		DataStream<OUT> inputStream = this.copy();
		@SuppressWarnings({ "unchecked", "rawtypes" })
		SingleOutputStreamOperator<R, ?> returnStream = new SingleOutputStreamOperator(environment,
				operatorName, outTypeInfo, operator);

		streamGraph.addOperator(returnStream.getId(), operator, getType(), outTypeInfo,
				operatorName);

		if (iterationID != null) {
			//This data stream is an input to some iteration
			addIterationSource(returnStream);
		}

		return returnStream;
	}
