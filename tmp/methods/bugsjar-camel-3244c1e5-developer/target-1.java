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
