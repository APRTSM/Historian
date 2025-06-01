  public void delete(@PathVariable("appId") String appId,
                     @PathVariable("clusterName") String clusterName, @RequestParam String operator) {
    Cluster entity = clusterService.findOne(appId, clusterName);
    if (entity == null) {
      throw new NotFoundException("cluster not found for clusterName " + clusterName);
    }
    clusterService.delete(entity.getId(), operator);
  }
