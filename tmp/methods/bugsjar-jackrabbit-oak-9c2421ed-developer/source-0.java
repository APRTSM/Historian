    public void releaseRecoveryLock(int clusterId){
        UpdateOp update = new UpdateOp(Integer.toString(clusterId), true);
        update.set(ClusterNodeInfo.REV_RECOVERY_LOCK, null);
        store.createOrUpdate(Collection.CLUSTER_NODES, update);
    }
