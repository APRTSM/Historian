	public AbstractPersistentProperty(Field field, PropertyDescriptor propertyDescriptor, PersistentEntity<?, P> owner,
			SimpleTypeHolder simpleTypeHolder) {

		Assert.notNull(simpleTypeHolder, "SimpleTypeHolder must not be null!");
		Assert.notNull(owner, "Owner entity must not be null!");

		this.name = field == null ? propertyDescriptor.getName() : field.getName();
		this.information = owner.getTypeInformation().getProperty(this.name);
		this.rawType = this.information != null ? information.getType()
				: field == null ? propertyDescriptor.getPropertyType() : field.getType();
		this.propertyDescriptor = propertyDescriptor;
		this.field = field;
		this.association = isAssociation() ? createAssociation() : null;
		this.owner = owner;
		this.simpleTypeHolder = simpleTypeHolder;
		this.hashCode = this.field == null ? this.propertyDescriptor.hashCode() : this.field.hashCode();
	}
