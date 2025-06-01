    public List<RecordId> getEntries() {
        if (size == 0) {
            return emptyList();
        } else if (size == 1) {
            return singletonList(getRecordId());
        } else {
            List<RecordId> list = newArrayListWithCapacity(size);
            Segment segment = getSegment();
            int offset = getOffset();
            for (int i = 0; i < size; i += bucketSize) {
                RecordId id = segment.readRecordId(offset);
                if (bucketSize == 1) {
                    list.add(id);
                } else {
                    ListRecord bucket = new ListRecord(
                            segment, id, Math.min(bucketSize, size - i));
                    list.addAll(bucket.getEntries());
                }
                offset += Segment.RECORD_ID_BYTES;
            }
            return list;
        }
    }
    public RecordId getEntry(int index) {
        checkElementIndex(index, size);

        if (size == 1) {
            return getRecordId();
        } else {
            int bucketIndex = index / bucketSize;
            int bucketOffset = index % bucketSize;
            Segment segment = getSegment();
            RecordId id = segment.readRecordId(getOffset(0, bucketIndex));
            ListRecord bucket = new ListRecord(
                    segment, id,
                    Math.min(bucketSize, size - bucketIndex * bucketSize));
            return bucket.getEntry(bucketOffset);
        }
    }
