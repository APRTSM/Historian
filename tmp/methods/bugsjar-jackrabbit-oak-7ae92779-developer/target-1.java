    public int compareTo(MapEntry that) {
        return ComparisonChain.start()
                .compare(getHash() & HASH_MASK, that.getHash() & HASH_MASK)
                .compare(name, that.name)
                .compare(value, that.value)
                .result();
    }
    private static int compare(MapEntry before, MapEntry after) {
        if (before == null) {
            // A null value signifies the end of the list of entries,
            // which is why the return value here is a bit counter-intuitive
            // (null > non-null). The idea is to make a virtual end-of-list
            // sentinel value appear greater than any normal value.
            return 1;
        } else if (after == null) {
            return -1;  // see above
        } else {
            return ComparisonChain.start()
                    .compare(before.getHash() & HASH_MASK, after.getHash() & HASH_MASK)
                    .compare(before.getName(), after.getName())
                    .result();
        }
    }
    MapEntry getEntry(String key) {
        checkNotNull(key);
        Segment segment = getSegment();

        int head = segment.readInt(getOffset(0));
        int size = getSize(head);
        if (size == 0) {
            return null; // shortcut
        }

        int hash = getHash(key);
        int level = getLevel(head);
        if (isBranch(size, level)) {
            // this is an intermediate branch record
            // check if a matching bucket exists, and recurse 
            int bitmap = segment.readInt(getOffset(4));
            int mask = BUCKETS_PER_LEVEL - 1;
            int shift = 32 - (level + 1) * LEVEL_BITS;
            int index = (int) (hash >> shift) & mask;
            int bit = 1 << index;
            if ((bitmap & bit) != 0) {
                int ids = bitCount(bitmap & (bit - 1));
                RecordId id = segment.readRecordId(getOffset(8, ids));
                return new MapRecord(segment, id).getEntry(key);
            } else {
                return null;
            }
        }

        // this is a leaf record; scan the list to find a matching entry
        int d = -1;
        for (int i = 0; i < size && d < 0; i++) {
            d = Long.valueOf(segment.readInt(getOffset(4 + i * 4)) & HASH_MASK)
                    .compareTo(Long.valueOf(hash & HASH_MASK));
            if (d == 0) {
                RecordId keyId = segment.readRecordId(
                        getOffset(4 + size * 4, i));
                d = segment.readString(keyId).compareTo(key);
                if (d == 0) {
                    RecordId valueId = segment.readRecordId(
                            getOffset(4 + size * 4, size + i));
                    return new MapEntry(segment, key, keyId, valueId);
                }
            }
        }

        return null;
    }
