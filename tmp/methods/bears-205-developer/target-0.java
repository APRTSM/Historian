  public void delete(@PathVariable("appId") String appId,
                     @PathVariable("clusterName") String clusterName, @RequestParam String operator) {

    Cluster entity = clusterService.findOne(appId, clusterName);

    if (entity == null) {
      throw new NotFoundException("cluster not found for clusterName " + clusterName);
    }

    if(ConfigConsts.CLUSTER_NAME_DEFAULT.equals(entity.getName())){
      throw new BadRequestException("default cluster can not be delete.");
    }

    clusterService.delete(entity.getId(), operator);
  }
