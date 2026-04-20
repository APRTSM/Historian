    public static ClusterNodeInfo getInstance(DocumentStore store, String machineId,
            String instanceId, int configuredClusterId, boolean updateLease) {

        // defaults for machineId and instanceID
        if (machineId == null) {
            machineId = MACHINE_ID;
        }
        if (instanceId == null) {
            instanceId = WORKING_DIR;
        }

        int retries = 10;
        for (int i = 0; i < retries; i++) {
            ClusterNodeInfo clusterNode = createInstance(store, machineId, instanceId, configuredClusterId);
            String key = String.valueOf(clusterNode.id);
            UpdateOp update = new UpdateOp(key, true);
            update.set(ID, key);
            update.set(MACHINE_ID_KEY, clusterNode.machineId);
            update.set(INSTANCE_ID_KEY, clusterNode.instanceId);
            if (updateLease) {
                update.set(LEASE_END_KEY, getCurrentTime() + clusterNode.leaseTime);
            } else {
                update.set(LEASE_END_KEY, clusterNode.leaseEndTime);
            }
            update.set(INFO_KEY, clusterNode.toString());
            update.set(STATE, clusterNode.state.name());
            update.set(REV_RECOVERY_LOCK, clusterNode.revRecoveryLock.name());
            update.set(OAK_VERSION_KEY, OAK_VERSION);

            final boolean success;
            if (clusterNode.newEntry) {
                // For new entry do a create. This ensures that if two nodes
                // create entry with same id then only one would succeed
                success = store.create(Collection.CLUSTER_NODES, Collections.singletonList(update));
            } else {
                // No expiration of earlier cluster info, so update
                store.createOrUpdate(Collection.CLUSTER_NODES, update);
                success = true;
            }

            if (success) {
                return clusterNode;
            }
        }
        throw new DocumentStoreException("Could not get cluster node info (retried " + retries + " times)");
    }
    private static ClusterNodeInfo createInstance(DocumentStore store, String machineId,
            String instanceId, int configuredClusterId) {

        long now = getCurrentTime();
        int clusterNodeId = 0;
        int maxId = 0;
        ClusterNodeState state = ClusterNodeState.NONE;
        Long prevLeaseEnd = null;
        boolean newEntry = false;

        ClusterNodeInfoDocument alreadyExistingConfigured = null;
        String reuseFailureReason = "";
        List<ClusterNodeInfoDocument> list = ClusterNodeInfoDocument.all(store);

        for (ClusterNodeInfoDocument doc : list) {

            String key = doc.getId();

            int id;
            try {
                id = doc.getClusterId();
            } catch (Exception e) {
                LOG.debug("Skipping cluster node info document {} because ID is invalid", key);
                continue;
            }

            maxId = Math.max(maxId, id);

            // if a cluster id was configured: check that and abort if it does
            // not match
            if (configuredClusterId != 0) {
                if (configuredClusterId != id) {
                    continue;
                } else {
                    alreadyExistingConfigured = doc;
                }
            }

            Long leaseEnd = (Long) doc.get(LEASE_END_KEY);

            if (leaseEnd != null && leaseEnd > now) {
                // TODO wait for lease end, see OAK-3449
                reuseFailureReason = "leaseEnd " + leaseEnd + " > " + now + " - " + (leaseEnd - now) + "ms in the future";
                continue;
            }

            String mId = "" + doc.get(MACHINE_ID_KEY);
            String iId = "" + doc.get(INSTANCE_ID_KEY);

            // remove entries with "random:" keys if not in use (no lease at all) 
            if (mId.startsWith(RANDOM_PREFIX) && leaseEnd == null) {
                store.remove(Collection.CLUSTER_NODES, key);
                LOG.debug("Cleaned up cluster node info for clusterNodeId {} [machineId: {}, leaseEnd: {}]", id, mId,
                        leaseEnd == null ? "n/a" : Utils.timestampToString(leaseEnd));
                if (alreadyExistingConfigured == doc) {
                    // we removed it, so we can't re-use it after all
                    alreadyExistingConfigured = null;
                }
                continue;
            }

            if (!mId.equals(machineId) || !iId.equals(instanceId)) {
                // a different machine or instance
                reuseFailureReason = "machineId/instanceId do not match: " + mId + "/" + iId + " != " + machineId + "/" + instanceId;
                continue;
            }

            // a cluster node which matches current machine identity but
            // not being used
            if (clusterNodeId == 0 || id < clusterNodeId) {
                // if there are multiple, use the smallest value
                clusterNodeId = id;
                state = ClusterNodeState.fromString((String) doc.get(STATE));
                prevLeaseEnd = leaseEnd;
            }
        }

        // No usable existing entry with matching signature found so
        // create a new entry
        if (clusterNodeId == 0) {
            newEntry = true;
            if (configuredClusterId != 0) {
                if (alreadyExistingConfigured != null) {
                    throw new DocumentStoreException(
                            "Configured cluster node id " + configuredClusterId + " already in use: " + reuseFailureReason);
                }
                clusterNodeId = configuredClusterId;
            } else {
                clusterNodeId = maxId + 1;
            }
        }

        // Do not expire entries and stick on the earlier state, and leaseEnd so,
        // that _lastRev recovery if needed is done.
        return new ClusterNodeInfo(clusterNodeId, store, machineId, instanceId, state,
                RecoverLockState.NONE, prevLeaseEnd, newEntry);
    }
