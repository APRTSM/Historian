	public T deserialize(DataInputView source) throws IOException {
		checkKryoInitialized();
		if (source != previousIn) {
			DataInputViewStream inputStream = new DataInputViewStream(source);
			input = new NoFetchingInput(inputStream);
			previousIn = source;
		}

		try {
			return (T) kryo.readClassAndObject(input);
		} catch (KryoException ke) {
			Throwable cause = ke.getCause();

			if(cause instanceof EOFException) {
				throw (EOFException) cause;
			} else {
				throw ke;
			}
		}
	}
