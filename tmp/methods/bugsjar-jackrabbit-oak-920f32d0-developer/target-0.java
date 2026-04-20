        public void collectResults(NodeInclude rootInclude, String rootIncludePath, String nodePath,
                                   NodeState nodeState, ResultCollector results) throws CommitFailedException {
            //For supporting jcr:contains(jcr:content, 'foo')
            if (rootInclude.relativeNode){
                results.onResult(new NodeIncludeResult(nodePath, rootIncludePath, nodeState));
            }

            //For supporting jcr:contains(., 'foo')
            results.onResult(new NodeIncludeResult(nodePath, nodeState));
        }
        public void collectResults(ResultCollector results)
                throws CommitFailedException {
            checkArgument(status == Status.MATCH_FOUND);

            //If result being collected as part of reaggregation then take path
            //from the stack otherwise its the current path
            String rootIncludePath = aggregateStack.isEmpty() ?  currentPath : aggregateStack.get(0);
            currentInclude.collectResults(rootState.rootInclude, rootIncludePath,
                    currentPath, matchedNodeState, results);
        }
