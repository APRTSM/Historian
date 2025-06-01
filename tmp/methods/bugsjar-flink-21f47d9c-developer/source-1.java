	public T deserialize(DataInputView source) throws IOException {
		checkKryoInitialized();
		if (source != previousIn) {
			DataInputViewStream inputStream = new DataInputViewStream(source);
			input = new NoFetchingInput(inputStream);
			previousIn = source;
		}
		return (T) kryo.readClassAndObject(input);
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
					throw new KryoException("Buffer underflow");
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
				throw new KryoException("Buffer underflow");
			}

			bytesRead += count;
			if(bytesRead == required){
				break;
			}
		}
		limit = required;
		return required;
	}
