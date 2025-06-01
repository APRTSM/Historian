	public boolean containsOperations(List<Operation> operations, OperationKind kind, String nodeKind,
			String nodeLabel) {
		return operations.stream()
				.anyMatch(operation -> operation.getAction().getClass().getSimpleName().equals(kind.name()) //
						&& context.getTypeLabel(operation.getAction().getNode()).equals(nodeKind)
						&& operation.getAction().getNode().getLabel().equals(nodeLabel));
	}
	public DiffImpl(TreeContext context, ITree rootSpoonLeft, ITree rootSpoonRight) {
		final MappingStore mappingsComp = new MappingStore();

		final Matcher matcher = new CompositeMatchers.ClassicGumtree(rootSpoonLeft, rootSpoonRight, mappingsComp);
		matcher.match();

		final ActionGenerator actionGenerator = new ActionGenerator(rootSpoonLeft, rootSpoonRight,
				matcher.getMappings());
		actionGenerator.generate();

		ActionClassifier actionClassifier = new ActionClassifier(matcher.getMappingsAsSet(),
				actionGenerator.getActions());
		// Bugfix: the Action classifier must be executed *BEFORE* the convertToSpoon
		// because it writes meta-data on the trees
		this.rootOperations = convertToSpoon(actionClassifier.getRootActions());
		this.allOperations = convertToSpoon(actionGenerator.getActions());

		this._mappingsComp = mappingsComp;
		this.context = context;

		for (int i = 0; i < this.getAllOperations().size(); i++) {
			Operation operation = this.getAllOperations().get(i);
			if (operation instanceof MoveOperation) {
				operation.getSrcNode().putMetadata("isMoved", true);
				operation.getDstNode().putMetadata("isMoved", true);
			}
		}
	}
	public CtElement commonAncestor() {
		final List<CtElement> copy = new ArrayList<>();
		for (Operation operation : rootOperations) {
			CtElement el = operation.getNode();
			if (operation instanceof InsertOperation) {
				// we take the corresponding node in the source tree
				el = (CtElement) _mappingsComp.getSrc(operation.getAction().getNode().getParent())
						.getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
			}
			copy.add(el);
		}
		while (copy.size() >= 2) {
			CtElement first = copy.remove(0);
			CtElement second = copy.remove(0);
			copy.add(commonAncestor(first, second));
		}
		return copy.get(0);
	}
	private String toDebugString() {
		String result = "";
		for (Operation operation : rootOperations) {
			ITree node = operation.getAction().getNode();
			final CtElement nodeElement = operation.getNode();
			String label = "\"" + node.getLabel() + "\"";
			if (operation instanceof UpdateOperation) {
				label += " to \"" + ((Update) operation.getAction()).getValue() + "\"";
			}
			String nodeType = context.getTypeLabel(node.getType());
			if (nodeElement != null) {
				nodeType += "(" + nodeElement.getClass().getSimpleName() + ")";
			}
			result += "\"" + operation.getAction().getClass().getSimpleName() + "\", \"" + nodeType + "\", " + label
					+ " (size: " + node.getDescendants().size() + ")" + node.toTreeString();
		}
		return result;
	}
