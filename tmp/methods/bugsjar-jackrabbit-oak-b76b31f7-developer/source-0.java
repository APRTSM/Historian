    BackgroundWriteStats backgroundWrite() {
        return unsavedLastRevisions.persist(this, new UnsavedModifications.Snapshot() {
            @Override
            public void acquiring() {
                if (store.create(JOURNAL,
                        singletonList(changes.asUpdateOp(getHeadRevision())))) {
                    changes = JOURNAL.newDocument(getDocumentStore());
                }
            }
        }, backgroundOperationLock.writeLock());
    }
