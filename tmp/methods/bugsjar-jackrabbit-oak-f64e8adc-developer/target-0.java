        int getEstimatedCount() {
            if (count < maxCount) {
                return count;
            }
            double averageDepth = (int) (depthTotal / count);
            // the number of estimated matches is higher
            // the higher the average depth of the first hits
            long estimatedNodes = (long) (count * Math.pow(1.1, averageDepth));
            estimatedNodes = Math.min(estimatedNodes, Integer.MAX_VALUE);
            return Math.max(count, (int) estimatedNodes);
        }
        public void visit(NodeState state) {
            if (state.hasProperty("match")) {
                count++;
                depthTotal += depth;
            }
            if (count < maxCount) {
                depth++;
                for (ChildNodeEntry entry : state.getChildNodeEntries()) {
                    if (count >= maxCount) {
                        break;
                    }
                    visit(entry.getNodeState());
                }
                depth--;
            }
        }
