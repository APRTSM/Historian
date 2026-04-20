		public void postVisit(PlanNode visitable) {
			
			if (visitable instanceof BinaryUnionPlanNode) {
				final BinaryUnionPlanNode unionNode = (BinaryUnionPlanNode) visitable;
				final Channel in1 = unionNode.getInput1();
				final Channel in2 = unionNode.getInput2();
			
				PlanNode newUnionNode;
				
				// if any input is cached, we keep this as a binary union and do not collapse it into a
				// n-ary union
//				if (in1.getTempMode().isCached() || in2.getTempMode().isCached()) {
//					// replace this node by an explicit operator
//					Channel cached, pipelined;
//					if (in1.getTempMode().isCached()) {
//						cached = in1;
//						pipelined = in2;
//					} else {
//						cached = in2;
//						pipelined = in1;
//					}
//					
//					newUnionNode = new DualInputPlanNode(unionNode.getOriginalOptimizerNode(), cached, pipelined,
//						DriverStrategy.UNION_WITH_CACHED);
//					newUnionNode.initProperties(unionNode.getGlobalProperties(), new LocalProperties());
//					
//					in1.setTarget(newUnionNode);
//					in2.setTarget(newUnionNode);
//				} else {
					// collect the union inputs to collapse this operator with 
					// its collapsed predecessors. check whether an input is materialized to prevent
					// collapsing
					List<Channel> inputs = new ArrayList<Channel>();
					collect(in1, inputs);
					collect(in2, inputs);
					
					newUnionNode = new NAryUnionPlanNode(unionNode.getOptimizerNode(), inputs, unionNode.getGlobalProperties());
					
					// adjust the input channels to have their target point to the new union node
					for (Channel c : inputs) {
						c.setTarget(newUnionNode);
					}
//				}
				
				unionNode.getOutgoingChannels().get(0).swapUnionNodes(newUnionNode);
			}
		}
