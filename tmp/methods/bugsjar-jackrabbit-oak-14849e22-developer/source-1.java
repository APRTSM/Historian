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
                            segment, id, Math.min(bucketSize, size - offset));
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
            RecordId bucketId = segment.readRecordId(getOffset(0, bucketIndex));
            ListRecord bucket = new ListRecord(segment, bucketId, bucketSize);
            return bucket.getEntry(bucketOffset);
        }
    }
    private synchronized RecordId writeListBucket(List<RecordId> bucket) {
        RecordId bucketId = prepare(RecordType.BUCKET, 0, bucket);
        for (RecordId id : bucket) {
            writeRecordId(id);
        }
        return bucketId;
    }
    public RecordId writeList(List<RecordId> list) {
        checkNotNull(list);
        checkArgument(list.size() > 0);

        List<RecordId> thisLevel = list;
        while (thisLevel.size() > 1) {
            List<RecordId> nextLevel = Lists.newArrayList();
            for (List<RecordId> bucket :
                    Lists.partition(thisLevel, ListRecord.LEVEL_SIZE)) {
                nextLevel.add(writeListBucket(bucket));
            }
            thisLevel = nextLevel;
        }
        return thisLevel.iterator().next();
    }
