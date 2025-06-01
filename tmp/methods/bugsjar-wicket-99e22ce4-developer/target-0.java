	public final Iterator<Component<?>> iterator()
	{
		return new Iterator<Component<?>>()
		{
			int index = 0;

			public boolean hasNext()
			{
				return index < children_size();
			}

			public Component<?> next()
			{
				return children_get(index++);
			}

			public void remove()
			{
				removedComponent(children_remove(--index));
			}
		};
	}
	private final Component<?> children_remove(Component<?> component)
	{
		int index = children_indexOf(component);
		if (index != -1)
		{
			return children_remove(index);
		}
		return null;
	}
	public MarkupContainer(MarkupContainer<?> parent, final String id, IModel<T> model)
	{
		super(parent, id, model);
	}
	private final void children_add(final Component<?> child)
	{
		if (this.children == null)
		{
			this.children = child;
		}
		else
		{
			// Get current list size
			final int size = children_size();

			// Create array that holds size + 1 elements
			final Component<?>[] children = new Component[size + 1];

			// Loop through existing children copying them
			for (int i = 0; i < size; i++)
			{
				children[i] = children_get(i);
			}

			// Add new child to the end
			children[size] = child;

			// Save new children
			this.children = children;
		}
	}
	public MarkupContainer(MarkupContainer<?> parent, final String id)
	{
		super(parent, id);
	}
	protected final MarkupStream findMarkupStream()
	{
		// Start here
		MarkupContainer<?> c = this;

		// Walk up hierarchy until markup found
		while (c.getMarkupStream() == null)
		{
			// Check parent
			c = c.getParent();

			// Are we at the top of the hierarchy?
			if (c == null)
			{
				// Failed to find markup stream
				throw new WicketRuntimeException(exceptionMessage("No markup found"));
			}
		}

		return c.getMarkupStream();
	}
	public void remove(final Component<?> component)
	{
		if (component == null)
		{
			throw new IllegalArgumentException("argument component may not be null");
		}

		if (children_remove(component) != null)
		{
			component.setFlag(FLAG_REMOVED_FROM_PARENT, true);
			removedComponent(component);
		}
	}
	public final Object visitChildren(final Class<?> clazz, final IVisitor visitor)
	{
		if (visitor == null)
		{
			throw new IllegalArgumentException("argument visitor may not be null");
		}

		// Iterate through children of this container
		for (int i = 0; i < children_size(); i++)
		{
			// Get next child component
			final Component<?> child = children_get(i);
			Object value = null;

			// Is the child of the correct class (or was no class specified)?
			if (clazz == null || clazz.isInstance(child))
			{
				// Call visitor
				value = visitor.component(child);

				// If visitor returns a non-null value, it halts the traversal
				if ((value != IVisitor.CONTINUE_TRAVERSAL)
						&& (value != IVisitor.CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER))
				{
					return value;
				}
			}

			// If child is a container
			if ((child instanceof MarkupContainer)
					&& (value != IVisitor.CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER))
			{
				// visit the children in the container
				value = ((MarkupContainer<?>)child).visitChildren(clazz, visitor);

				// If visitor returns a non-null value, it halts the traversal
				if ((value != IVisitor.CONTINUE_TRAVERSAL)
						&& (value != IVisitor.CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER))
				{
					return value;
				}
			}
		}

		return null;
	}
	private final Component<?> children_get(int index)
	{
		if (index == 0)
		{
			if (children instanceof Component)
			{
				return (Component<?>)children;
			}
			else
			{
				return ((Component[])children)[index];
			}
		}
		else
		{
			return ((Component[])children)[index];
		}
	}
	private final int children_indexOf(String id)
	{
		if (children instanceof Component)
		{
			if (((Component<?>)children).getId().equals(id))
			{
				return 0;
			}
		}
		else
		{
			if (children != null)
			{
				final Component<?>[] components = (Component[])children;
				for (int i = 0; i < components.length; i++)
				{
					if (components[i].getId().equals(id))
					{
						return i;
					}
				}
			}
		}
		return -1;
	}
	public final Iterator<Component<?>> iterator(Comparator<Component<?>> comparator)
	{
		final List<Component<?>> sorted;
		if (children == null)
		{
			sorted = Collections.emptyList();
		}
		else
		{
			if (children instanceof Component)
			{
				sorted = new ArrayList<Component<?>>(1);
				sorted.add((Component<?>)children);
			}
			else
			{
				sorted = Arrays.asList((Component<?>[])children);
			}
		}
		Collections.sort(sorted, comparator);
		return sorted.iterator();
	}
	public void internalAdd(final Component<?> child)
	{
		if (log.isDebugEnabled())
		{
			log.debug("internalAdd " + child.getId() + " to " + this);
		}

		// Add to map
		addedComponent(child);
		put(child);
	}
	public void internalDetach()
	{
		// Handle end request for the container itself
		super.internalDetach();

		// Loop through child components
		for (Component<?> child : this)
		{
			// Call end request on the child
			child.internalDetach();
		}
	}
	private final int children_indexOf(Component<?> child)
	{
		if (children instanceof Component)
		{
			if (children == child)
			{
				return 0;
			}
		}
		else
		{
			if (children != null)
			{
				final Component<?>[] components = (Component[])children;
				for (int i = 0; i < components.length; i++)
				{
					if (components[i] == child)
					{
						return i;
					}
				}
			}
		}
		return -1;
	}
	private final Component<?> put(final Component<?> child)
	{
		// search for the child by id. So that it will
		// find the right index for the id instead of looking
		// if the component itself is already children.
		int index = children_indexOf(child.getId());
		if (index == -1)
		{
			children_add(child);
			return null;
		}
		else
		{
			return children_set(index, child);
		}
	}
	public void renderHead(final IHeaderResponse response)
	{
		if (isVisible())
		{
			super.renderHead(response);

			for (Component<?> child : this)
			{
				child.renderHead(response);
			}
		}
	}
	public final Component<?> get(final String path)
	{
		// Reference to this container
		if (path == null || path.trim().equals(""))
		{
			return this;
		}

		// Get child's id, if any
		final String id = Strings.firstPathComponent(path, Component.PATH_SEPARATOR);

		// Get child by id
		Component<?> child = children_get(id);

		// If the container is transparent, than ask its parent.
		// ParentResolver does something quite similar, but because of <head>,
		// <body>, <wicket:panel> etc. it is quite common to have transparent
		// components. Hence, this is little short cut for a tiny performance
		// optimization.
		if ((child == null) && isTransparentResolver() && (getParent() != null))
		{
			// Special tags like "_body", "_panel" must implement
			// IComponentResolver if they want to be transparent.
			if (path.startsWith("_") == false)
			{
				child = getParent().get(path);
			}
		}

		// Found child?
		final String path2 = Strings.afterFirstPathComponent(path, Component.PATH_SEPARATOR);
		if (child != null)
		{
			// Recurse on latter part of path
			return child.get(path2);
		}

		return child;
	}
	public String toString(final boolean detailed)
	{
		final StringBuffer buffer = new StringBuffer();
		buffer.append("[MarkupContainer ");
		buffer.append(super.toString(true));
		if (detailed)
		{
			if (getMarkupStream() != null)
			{
				buffer.append(", markupStream = " + getMarkupStream());
			}

			if (children_size() != 0)
			{
				buffer.append(", children = ");

				// Loop through child components
				final int size = children_size();
				for (int i = 0; i < size; i++)
				{
					// Get next child
					final Component<?> child = children_get(i);
					if (i != 0)
					{
						buffer.append(' ');
					}
					buffer.append(child.toString());
				}
			}
		}
		buffer.append(']');
		return buffer.toString();
	}
	public void internalAttach()
	{
		// Handle begin request for the container itself
		try
		{
			super.internalAttach();

			// Loop through child components
			final int size = children_size();
			for (int i = 0; i < size; i++)
			{
				// Get next child
				final Component<?> child = children_get(i);

				// Ignore feedback as that was done in Page
				if (!(child instanceof IFeedback))
				{
					// Call begin request on the child
					child.internalAttach();
				}
			}
		}
		catch (RuntimeException ex)
		{
			if (ex instanceof WicketRuntimeException)
			{
				throw ex;
			}
			else
			{
				throw new WicketRuntimeException("Error attaching this container for rendering: "
						+ this, ex);
			}
		}
	}
	private final Component<?> children_get(final String id)
	{
		if (children instanceof Component)
		{
			final Component<?> component = (Component<?>)children;
			if (component.getId().equals(id))
			{
				return component;
			}
		}
		else
		{
			if (children != null)
			{
				final Component<?>[] components = (Component[])children;
				for (Component<?> element : components)
				{
					if (element.getId().equals(id))
					{
						return element;
					}
				}
			}
		}
		return null;
	}
	final MarkupContainer<?> add(final Component<?> child)
	{
		if (child == null)
		{
			throw new IllegalArgumentException("argument child may not be null");
		}

		if (log.isDebugEnabled())
		{
			log.debug("Add " + child.getId() + " to component " + this.getClass().getName()
					+ " with path " + getPath());
		}

		// Add to map
		Component<?> replaced = put(child);
		child.setFlag(FLAG_REMOVED_FROM_PARENT, false);
		if (replaced != null)
		{
			replaced.setFlag(FLAG_REMOVED_FROM_PARENT, true);
			removedComponent(replaced);
			// The position of the associated markup remains the same
			child.markupIndex = replaced.markupIndex;


			// The generated markup id remains the same
			String replacedId = (replaced.hasMarkupIdMetaData()) ? replaced.getMarkupId() : null;
			child.setMarkupIdMetaData(replacedId);
		}
		// now call addedComponent (after removedComponent)
		addedComponent(child);

		return this;
	}
	public final boolean contains(final Component<?> component, final boolean recurse)
	{
		if (component == null)
		{
			throw new IllegalArgumentException("argument component may not be null");
		}

		if (recurse)
		{
			// Start at component and continue while we're not out of parents
			for (Component<?> current = component; current != null;)
			{
				// Get parent
				final MarkupContainer<?> parent = current.getParent();

				// If this container is the parent, then the component is
				// recursively contained by this container
				if (parent == this)
				{
					// Found it!
					return true;
				}

				// Move up the chain to the next parent
				current = parent;
			}

			// Failed to find this container in component's ancestry
			return false;
		}
		else
		{
			// Is the component contained in this container?
			return component.getParent() == this;
		}
	}
	private final Component<?> children_remove(int index)
	{
		if (children instanceof Component)
		{
			if (index == 0)
			{
				final Component<?> removed = (Component<?>)children;
				this.children = null;
				return removed;
			}
			else
			{
				throw new IndexOutOfBoundsException();
			}
		}
		else
		{
			Component<?>[] c = ((Component[])children);
			final Component<?> removed = c[index];
			if (c.length == 2)
			{
				if (index == 0)
				{
					this.children = c[1];
				}
				else if (index == 1)
				{
					this.children = c[0];
				}
				else
				{
					throw new IndexOutOfBoundsException();
				}
			}
			else
			{
				Component<?>[] newChildren = new Component[c.length - 1];
				int j = 0;
				for (int i = 0; i < c.length; i++)
				{
					if (i != index)
					{
						newChildren[j++] = c[i];
					}
				}
				this.children = newChildren;
			}
			return removed;
		}
	}
	private final Component<?> children_set(int index, Component<?> child)
	{
		final Component<?> replaced;
		if (index < children_size())
		{
			if (children == null || children instanceof Component)
			{
				replaced = (Component<?>)children;
				children = child;
			}
			else
			{
				final Component<?>[] children = (Component[])this.children;
				replaced = children[index];
				children[index] = child;
			}
		}
		else
		{
			throw new IndexOutOfBoundsException();
		}
		return replaced != child ? replaced : null;
	}
	public final void remove(final String id)
	{
		if (id == null)
		{
			throw new IllegalArgumentException("argument id may not be null");
		}

		final Component<?> component = get(id);
		if (component != null)
		{
			remove(component);
		}
		else
		{
			throw new WicketRuntimeException("Unable to find a component with id '" + id
					+ "' to remove");
		}
	}
