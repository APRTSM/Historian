    public void notify(DelegateExecution execution) {
        if (!isSourceTransitionNotExecutionActivityAndNonInterrupting((ExecutionEntity) execution)) {
            Context.getCommandContext().getHistoryManager()
                    .recordActivityEnd((ExecutionEntity) execution);
        }
    }
    private boolean isSourceTransitionNotExecutionActivityAndNonInterrupting(InterpretableExecution execution) {
        TransitionImpl transition = execution.getTransition();
        if (transition != null) {
            ActivityBehavior activityBehavior = transition.getSource().getActivityBehavior();

            return (!(execution.getActivity().getId().equals(execution.getTransition().getSource().getId())) &&
                    activityBehavior instanceof BoundaryEventActivityBehavior &&
                    !(((BoundaryEventActivityBehavior) activityBehavior).isInterrupting()));
        }
        return false;
    }
