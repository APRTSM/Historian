		public void postVisit(PlanNode visitable) {
			
			if (visitable instanceof BinaryUnionPlanNode) {
				final BinaryUnionPlanNode unionNode = (BinaryUnionPlanNode) visitable;
				final Channel in1 = unionNode.getInput1();
				final Channel in2 = unionNode.getInput2();
			
				PlanNode newUnionNode;

				List<Channel> inputs = new ArrayList<Channel>();
				collect(in1, inputs);
				collect(in2, inputs);

				newUnionNode = new NAryUnionPlanNode(unionNode.getOptimizerNode(), inputs, unionNode.getGlobalProperties());

				for (Channel c : inputs) {
					c.setTarget(newUnionNode);
				}

				for(Channel channel : unionNode.getOutgoingChannels()){
					channel.swapUnionNodes(newUnionNode);
				}
			}
		}
