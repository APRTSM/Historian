        public void collectResults(NodeInclude rootInclude, String rootIncludePath, String nodePath,
                                   NodeState nodeState, ResultCollector results) throws CommitFailedException {
            //For supporting jcr:contains(jcr:content, 'foo')
            if (rootInclude != this && rootInclude.relativeNode){
                results.onResult(new NodeIncludeResult(nodePath, rootIncludePath, nodeState));
            }

            //For supporting jcr:contains(., 'foo')
            results.onResult(new NodeIncludeResult(nodePath, nodeState));
        }
        public void collectResults(ResultCollector results)
                throws CommitFailedException {
            checkArgument(status == Status.MATCH_FOUND);
            String rootIncludePath = aggregateStack.isEmpty() ?  null : aggregateStack.get(0);
            currentInclude.collectResults(rootState.rootInclude, rootIncludePath,
                    currentPath, matchedNodeState, results);
        }
