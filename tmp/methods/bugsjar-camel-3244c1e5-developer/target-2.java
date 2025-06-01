    public String getEndpointUri() {
        return super.getEndpointUri();
    }
    public ModelMBean assemble(MBeanServer mBeanServer, Object obj, ObjectName name) throws JMException {
        ModelMBeanInfo mbi = null;

        // prefer to use the managed instance if it has been annotated with JMX annotations
        if (obj instanceof ManagedInstance) {
            // there may be a custom embedded instance which have additional methods
            Object custom = ((ManagedInstance) obj).getInstance();
            if (custom != null && ObjectHelper.hasAnnotation(custom.getClass().getAnnotations(), ManagedResource.class)) {
                LOG.trace("Assembling MBeanInfo for: {} from custom @ManagedResource object: {}", name, custom);
                // get the mbean info from the custom managed object
                mbi = assembler.getMBeanInfo(null, custom, name.toString());
                // and let the custom object be registered in JMX
                obj = custom;
            }
        }

        if (mbi == null) {
            // use the default provided mbean which has been annotated with JMX annotations
            LOG.trace("Assembling MBeanInfo for: {} from @ManagedResource object: {}", name, obj);
            mbi = assembler.getMBeanInfo(obj, null, name.toString());
        }

        if (mbi == null) {
            return null;
        }

        RequiredModelMBean mbean;
        boolean sanitize = camelContext.getManagementStrategy().getManagementAgent().getMask() != null && camelContext.getManagementStrategy().getManagementAgent().getMask();
        if (sanitize) {
            mbean = new MaskRequiredModelMBean(mbi, sanitize);
        } else {
            mbean = (RequiredModelMBean) mBeanServer.instantiate(RequiredModelMBean.class.getName());
            mbean.setModelMBeanInfo(mbi);
        }

        try {
            mbean.setManagedResource(obj, "ObjectReference");
        } catch (InvalidTargetObjectTypeException e) {
            throw new JMException(e.getMessage());
        }

        // Allows the managed object to send notifications
        if (obj instanceof NotificationSenderAware) {
            ((NotificationSenderAware)obj).setNotificationSender(new NotificationSenderAdapter(mbean));
        }

        return mbean;
    }
    public ModelMBeanInfo getMBeanInfo(Object defaultManagedBean, Object customManagedBean, String objectName) throws JMException {
        // skip proxy classes
        if (defaultManagedBean != null && Proxy.isProxyClass(defaultManagedBean.getClass())) {
            LOG.trace("Skip creating ModelMBeanInfo due proxy class {}", defaultManagedBean.getClass());
            return null;
        }

        // maps and lists to contain information about attributes and operations
        Map<String, ManagedAttributeInfo> attributes = new LinkedHashMap<String, ManagedAttributeInfo>();
        Set<ManagedOperationInfo> operations = new LinkedHashSet<ManagedOperationInfo>();
        Set<ModelMBeanAttributeInfo> mBeanAttributes = new LinkedHashSet<ModelMBeanAttributeInfo>();
        Set<ModelMBeanOperationInfo> mBeanOperations = new LinkedHashSet<ModelMBeanOperationInfo>();
        Set<ModelMBeanNotificationInfo> mBeanNotifications = new LinkedHashSet<ModelMBeanNotificationInfo>();

        // extract details from default managed bean
        if (defaultManagedBean != null) {
            extractAttributesAndOperations(defaultManagedBean.getClass(), attributes, operations);
            extractMbeanAttributes(defaultManagedBean, attributes, mBeanAttributes, mBeanOperations);
            extractMbeanOperations(defaultManagedBean, operations, mBeanOperations);
            extractMbeanNotifications(defaultManagedBean, mBeanNotifications);
        }

        // extract details from custom managed bean
        if (customManagedBean != null) {
            extractAttributesAndOperations(customManagedBean.getClass(), attributes, operations);
            extractMbeanAttributes(customManagedBean, attributes, mBeanAttributes, mBeanOperations);
            extractMbeanOperations(customManagedBean, operations, mBeanOperations);
            extractMbeanNotifications(customManagedBean, mBeanNotifications);
        }

        // create the ModelMBeanInfo
        String name = getName(customManagedBean != null ? customManagedBean : defaultManagedBean, objectName);
        String description = getDescription(customManagedBean != null ? customManagedBean : defaultManagedBean, objectName);
        ModelMBeanAttributeInfo[] arrayAttributes = mBeanAttributes.toArray(new ModelMBeanAttributeInfo[mBeanAttributes.size()]);
        ModelMBeanOperationInfo[] arrayOperations = mBeanOperations.toArray(new ModelMBeanOperationInfo[mBeanOperations.size()]);
        ModelMBeanNotificationInfo[] arrayNotifications = mBeanNotifications.toArray(new ModelMBeanNotificationInfo[mBeanNotifications.size()]);

        ModelMBeanInfo info = new ModelMBeanInfoSupport(name, description, arrayAttributes, null, arrayOperations, arrayNotifications);
        LOG.trace("Created ModelMBeanInfo {}", info);
        return info;
    }
