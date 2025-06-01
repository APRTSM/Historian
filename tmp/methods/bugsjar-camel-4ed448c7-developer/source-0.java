    public void updateRouteFromXml(String xml) throws Exception {
        // convert to model from xml
        RouteDefinition def = ModelHelper.createModelFromXml(xml, RouteDefinition.class);
        if (def == null) {
            return;
        }

        // add will remove existing route first
        context.addRouteDefinition(def);
    }
