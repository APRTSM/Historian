	private void addLocalInfoFromChannelToConfig(Channel channel, TaskConfig config, int inputNum, boolean isBroadcastChannel) {
		// serializer
		if (isBroadcastChannel) {
			config.setBroadcastInputSerializer(channel.getSerializer(), inputNum);
			
			if (channel.getLocalStrategy() != LocalStrategy.NONE || (channel.getTempMode() != null && channel.getTempMode() != TempMode.NONE)) {
				throw new CompilerException("Found local strategy or temp mode on a broadcast variable channel.");
			} else {
				return;
			}
		} else {
			config.setInputSerializer(channel.getSerializer(), inputNum);
		}
		
		// local strategy
		if (channel.getLocalStrategy() != LocalStrategy.NONE) {
			config.setInputLocalStrategy(inputNum, channel.getLocalStrategy());
			if (channel.getLocalStrategyComparator() != null) {
				config.setInputComparator(channel.getLocalStrategyComparator(), inputNum);
			}
		}
		
		assignLocalStrategyResources(channel, config, inputNum);
		
		// materialization / caching
		if (channel.getTempMode() != null) {
			final TempMode tm = channel.getTempMode();

			boolean needsMemory = false;
			// Don't add a pipeline breaker if the data exchange is already blocking.
			if (tm.breaksPipeline() && channel.getDataExchangeMode() != DataExchangeMode.BATCH) {
				config.setInputAsynchronouslyMaterialized(inputNum, true);
				needsMemory = true;
			}
			if (tm.isCached()) {
				config.setInputCached(inputNum, true);
				needsMemory = true;
			}
			
			if (needsMemory) {
				// sanity check
				if (tm == null || tm == TempMode.NONE || channel.getRelativeTempMemory() <= 0) {
					throw new CompilerException("Bug in compiler: Inconsistent description of input materialization.");
				}
				config.setRelativeInputMaterializationMemory(inputNum, channel.getRelativeTempMemory());
			}
		}
	}
