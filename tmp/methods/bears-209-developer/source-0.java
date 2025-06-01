    private void acquireLock(final String lockId, final ReentrantLock lock) {
        try {
            if (!lock.tryLock(timeout, TimeUnit.MILLISECONDS)) {
                throw new CouldNotAcquireLockException("Couldn't acquire lock for key:" + lockId);
            }
            LOGGER.debug("Lock acquired for id:{}", lockId);
        } catch (InterruptedException ex) {
            final Thread currentThread = Thread.currentThread();
            LOGGER.error("Thread {} where interrupted when acquire lock for id:{}", currentThread.getName(), lockId);
            currentThread.interrupt();
            throw new CouldNotAcquireLockException("Lock not acquired due to interruption of thread, id:" + lockId, ex);
        }
    }
    public void acquire(final String lockId) {
        final ReentrantLock lock = locks.computeIfAbsent(lockId, key -> new ReentrantLock(true));
        acquireLock(lockId, lock);
    }
    private void releaseLock(final String lockId, final ReentrantLock lock) {
        if (lock.tryLock()) {
            locks.computeIfPresent(lockId, (key, value) -> {
                int holdCount = lock.getHoldCount();
                if (holdCount > 1) {
                    lock.unlock();
                    LOGGER.debug("Lock released for id:{}", lockId);
                }
                return holdCount == 1 ? null : lock;
            });
            lock.unlock();
        }
    }
