    public Builder shared(@Nullable Boolean shared) {
      if (shared != null) return shared((boolean) shared);
      flags &= ~FLAG_SHARED_SET;
      return this;
    }
    public Builder debug(@Nullable Boolean debug) {
      if (debug != null) return debug((boolean) debug);
      flags &= ~FLAG_DEBUG_SET;
      return this;
    }
