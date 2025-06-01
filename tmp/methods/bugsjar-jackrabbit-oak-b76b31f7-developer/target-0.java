    BackgroundWriteStats backgroundWrite() {
        return unsavedLastRevisions.persist(this, new UnsavedModifications.Snapshot() {
            @Override
            public void acquiring(Revision mostRecent) {
                if (store.create(JOURNAL,
                        singletonList(changes.asUpdateOp(mostRecent)))) {
                    changes = JOURNAL.newDocument(getDocumentStore());
                }
            }
        }, backgroundOperationLock.writeLock());
    }
