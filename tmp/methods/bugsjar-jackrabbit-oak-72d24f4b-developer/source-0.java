        private void copy(final FileReference reference) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    String name = reference.name;
                    try {
                        if (!local.fileExists(name)) {
                            long start = System.currentTimeMillis();
                            remote.copy(local, name, name, IOContext.READ);
                            reference.markValid();
                            downloadTime.addAndGet(System.currentTimeMillis() - start);
                            downloadSize.addAndGet(remote.fileLength(name));
                        } else {
                            long localLength = local.fileLength(name);
                            long remoteLength = remote.fileLength(name);

                            //Do a simple consistency check. Ideally Lucene index files are never
                            //updated but still do a check if the copy is consistent
                            if (localLength != remoteLength) {
                                log.warn("Found local copy for {} in {} but size of local {} differs from remote {}. " +
                                                "Content would be read from remote file only",
                                        name, local, localLength, remoteLength);
                                invalidFileCount.incrementAndGet();
                            } else {
                                reference.markValid();
                            }
                        }
                    } catch (IOException e) {
                        //TODO In case of exception there would not be any other attempt
                        //to download the file. Look into support for retry
                        log.warn("Error occurred while copying file [{}] " +
                                "from {} to {}", name, remote, local, e);
                    }
                }
            });
        }
