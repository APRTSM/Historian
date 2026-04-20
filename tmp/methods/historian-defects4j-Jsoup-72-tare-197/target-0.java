    private void bufferUp() {
        if (bufPos < bufSplitPoint)
            return;

        try {
            readerPos += bufPos;
            reader.skip(bufPos);
            reader.mark(maxBufferLen);
            bufLength = reader.read(charBuf);
            reader.reset();
            bufPos = 0;
            bufMark = 0;
	bufSplitPoint = ((bufLength > readAheadLimit))?readAheadLimit:readAheadLimit;

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
