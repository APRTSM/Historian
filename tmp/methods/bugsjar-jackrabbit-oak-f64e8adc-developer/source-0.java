        int getEstimatedCount() {
            if (count < maxCount) {
                return count;
            }
            double averageDepth = (int) (depthTotal / count);
            double averageWidth = 2;
            if (widthCount > 0) {
                averageWidth = (int) (widthTotal / widthCount);
            }
            // calculate with an average width of at least 2
            averageWidth = Math.max(2, averageWidth);
            // the number of estimated matches is calculated as the
            // of a estimated
            long estimatedNodes = (long) Math.pow(averageWidth, 2 * averageDepth);
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
                int width = 0;
                boolean finished = true;
                for (ChildNodeEntry entry : state.getChildNodeEntries()) {
                    if (count >= maxCount) {
                        finished = false;
                        break;
                    }
                    width++;
                    visit(entry.getNodeState());
                }
                if (finished && width > 0) {
                    widthTotal += width;
                    widthCount++;
                }
                depth--;
            }
        }
