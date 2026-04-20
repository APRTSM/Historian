    private void compress(@Nonnull Set<UUID> removed) {
        if (recent.isEmpty() && removed.isEmpty()) {
            return;
        }

        SegmentWriter writer = null;
        Map<String, RecordId> segmentIdMap = newHashMap();
        for (Entry<UUID, RecordIdMap> recentEntry : recent.entrySet()) {
            UUID uuid = recentEntry.getKey();
            RecordIdMap newSegment = recentEntry.getValue();

            if (removed.contains(uuid)) {
                continue;
            }

            MapRecord base;
            MapEntry baseEntry = entries == null ? null : entries.getEntry(uuid.toString());
            base = baseEntry == null ? null : new MapRecord(baseEntry.getValue());

            if (writer == null) {
                writer = store.createSegmentWriter();
            }

            Map<String, RecordId> offsetMap = newHashMap();
            for (int k = 0; k < newSegment.size(); k++) {
                offsetMap.put(String.valueOf(newSegment.getKey(k)),
                        writer.writeString(newSegment.getRecordId(k).toString10()));
            }
            RecordId newEntryId = writer.writeMap(base, offsetMap).getRecordId();
            segmentIdMap.put(uuid.toString(), newEntryId);
            recordCount += offsetMap.size();
        }

        if (entries != null) {
            for (UUID uuid : removed) {
                MapEntry toRemove = entries.getEntry(uuid.toString());
                if (toRemove != null) {
                    segmentIdMap.put(uuid.toString(), null);
                    recordCount -= new MapRecord(toRemove.getValue()).size();
                }
            }
        }

        if (!segmentIdMap.isEmpty()) {
            if (writer == null) {
                writer = store.createSegmentWriter();
            }

            RecordId previousBaseId = entries == null ? null : entries.getRecordId();
            entries = writer.writeMap(entries, segmentIdMap);
            entries.getSegment().getSegmentId().pin();
            String mapInfo = PERSISTED_COMPACTION_MAP + '{' +
                    "id=" + entries.getRecordId() +
                    ", baseId=" + previousBaseId + '}';
            writer.writeString(mapInfo);
            writer.flush();
            recent.clear();
        }

        if (recordCount == 0) {
            entries = null;
        }
    }
