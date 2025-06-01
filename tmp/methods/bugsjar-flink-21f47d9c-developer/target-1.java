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
	protected int require(int required) throws KryoException {
		if(required > capacity) {
			throw new KryoException("Buffer too small: capacity: " + capacity + ", " +
					"required: " + required);
		}

		position = 0;
		int bytesRead = 0;
		int count;
		while(true){
			count = fill(buffer, bytesRead, required - bytesRead);

			if(count == -1){
				throw new KryoException(new EOFException("No more bytes left."));
			}

			bytesRead += count;
			if(bytesRead == required){
				break;
			}
		}
		limit = required;
		return required;
	}
	public void readBytes(byte[] bytes, int offset, int count) throws KryoException {
		if(bytes == null){
			throw new IllegalArgumentException("bytes cannot be null.");
		}

		try{
			int bytesRead = 0;
			int c;

			while(true){
				c = inputStream.read(bytes, offset+bytesRead, count-bytesRead);

				if(c == -1){
					throw new KryoException(new EOFException("No more bytes left."));
				}

				bytesRead += c;

				if(bytesRead == count){
					break;
				}
			}
		}catch(IOException ex){
			throw new KryoException(ex);
		}
	}
