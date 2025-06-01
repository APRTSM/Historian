    public int recv_addConstraint() throws AccumuloException, AccumuloSecurityException, TableNotFoundException, org.apache.thrift.TException
    {
      addConstraint_result result = new addConstraint_result();
      receiveBase(result, "addConstraint");
      if (result.isSetSuccess()) {
        return result.success;
      }
      if (result.ouch1 != null) {
        throw result.ouch1;
      }
      if (result.ouch2 != null) {
        throw result.ouch2;
      }
      throw new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.MISSING_RESULT, "addConstraint failed: unknown result");
    }
