	public void serialize(T record, DataOutputView target) throws IOException {
		checkKryoInitialized();
		if (target != previousOut) {
			DataOutputViewStream outputStream = new DataOutputViewStream(target);
			output = new Output(outputStream);
			previousOut = target;
		}

		// Sanity check: Make sure that the output is cleared/has been flushed by the last call
		// otherwise data might be written multiple times in case of a previous EOFException
		if (output.position() != 0) {
			throw new IllegalStateException("The Kryo Output still contains data from a previous " +
				"serialize call. It has to be flushed or cleared at the end of the serialize call.");
		}

		try {
			kryo.writeClassAndObject(output, record);
			output.flush();
		}
		catch (KryoException ke) {
			// make sure that the Kryo output buffer is cleared in case that we can recover from
			// the exception (e.g. EOFException which denotes buffer full)
			output.clear();

			Throwable cause = ke.getCause();
			if (cause instanceof EOFException) {
				throw (EOFException) cause;
			}
			else {
				throw ke;
			}
		}
	}
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

			if (cause instanceof EOFException) {
				throw (EOFException) cause;
			} else {
				throw ke;
			}
		}
	}
