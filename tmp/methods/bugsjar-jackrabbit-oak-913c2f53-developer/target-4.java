    boolean wasCompacted(SegmentId id) {
        long msb = id.getMostSignificantBits();
        long lsb = id.getLeastSignificantBits();
        return findEntry(msb, lsb) != -1;
    }
    public Compactor(SegmentWriter writer) {
        this.writer = writer;
        this.builder =
                new SegmentNodeBuilder(writer.writeNode(EMPTY_NODE), writer);
    }
    protected SegmentTracker getTracker() {
        return segmentId.getTracker();
    }
    SegmentNodeBuilder(SegmentNodeState base, SegmentWriter writer) {
        super(base);
        this.writer = writer;
    }
    SegmentNodeBuilder(SegmentNodeState base) {
        this(base, base.getTracker().getWriter());
    }
    public SegmentNodeState writeNode(NodeState state) {
        if (state instanceof SegmentNodeState) {
            SegmentNodeState sns = uncompact((SegmentNodeState) state);
            if (sns != state || store.containsSegment(
                    sns.getRecordId().getSegmentId())) {
                return sns;
            }
        }

        SegmentNodeState before = null;
        Template beforeTemplate = null;
        ModifiedNodeState after = null;
        if (state instanceof ModifiedNodeState) {
            after = (ModifiedNodeState) state;
            NodeState base = after.getBaseState();
            if (base instanceof SegmentNodeState) {
                SegmentNodeState sns = uncompact((SegmentNodeState) base);
                if (sns != base || store.containsSegment(
                        sns.getRecordId().getSegmentId())) {
                    before = sns;
                    beforeTemplate = before.getTemplate();
                }
            }
        }

        Template template = new Template(state);
        RecordId templateId;
        if (before != null && template.equals(beforeTemplate)) {
            templateId = before.getTemplateId();
        } else {
            templateId = writeTemplate(template);
        }

        List<RecordId> ids = Lists.newArrayList();
        ids.add(templateId);

        String childName = template.getChildName();
        if (childName == Template.MANY_CHILD_NODES) {
            MapRecord base;
            final Map<String, RecordId> childNodes = Maps.newHashMap();
            if (before != null
                    && before.getChildNodeCount(2) > 1
                    && after.getChildNodeCount(2) > 1) {
                base = before.getChildNodeMap();
                after.compareAgainstBaseState(before, new DefaultNodeStateDiff() {
                    @Override
                    public boolean childNodeAdded(String name, NodeState after) {
                        childNodes.put(name, writeNode(after).getRecordId());
                        return true;
                    }
                    @Override
                    public boolean childNodeChanged(
                            String name, NodeState before, NodeState after) {
                        childNodes.put(name, writeNode(after).getRecordId());
                        return true;
                    }
                    @Override
                    public boolean childNodeDeleted(String name, NodeState before) {
                        childNodes.put(name, null);
                        return true;
                    }
                });
            } else {
                base = null;
                for (ChildNodeEntry entry : state.getChildNodeEntries()) {
                    childNodes.put(
                            entry.getName(),
                            writeNode(entry.getNodeState()).getRecordId());
                }
            }
            ids.add(writeMap(base, childNodes).getRecordId());
        } else if (childName != Template.ZERO_CHILD_NODES) {
            ids.add(writeNode(state.getChildNode(template.getChildName())).getRecordId());
        }

        for (PropertyTemplate pt : template.getPropertyTemplates()) {
            String name = pt.getName();
            PropertyState property = state.getProperty(name);

            if (property instanceof SegmentPropertyState
                    && store.containsSegment(((SegmentPropertyState) property).getRecordId().getSegmentId())) {
                ids.add(((SegmentPropertyState) property).getRecordId());
            } else if (!(before instanceof SegmentNodeState)
                    || store.containsSegment(((SegmentNodeState) before).getRecordId().getSegmentId())) {
                ids.add(writeProperty(property));
            } else {
                // reuse previously stored property, if possible
                PropertyTemplate bt = beforeTemplate.getPropertyTemplate(name);
                if (bt == null) {
                    ids.add(writeProperty(property)); // new property
                } else {
                    SegmentPropertyState bp = beforeTemplate.getProperty(
                            before.getRecordId(), bt.getIndex());
                    if (property.equals(bp)) {
                        ids.add(bp.getRecordId()); // no changes
                    } else if (bp.isArray() && bp.getType() != BINARIES) {
                        // reuse entries from the previous list
                        ids.add(writeProperty(property, bp.getValueRecords()));
                    } else {
                        ids.add(writeProperty(property));
                    }
                }
            }
        }

        synchronized (this) {
            RecordId recordId = prepare(RecordType.NODE, 0, ids);
            for (RecordId id : ids) {
                writeRecordId(id);
            }
            return new SegmentNodeState(recordId);
        }
    }
    private SegmentNodeState uncompact(SegmentNodeState state) {
        RecordId id = tracker.getCompactionMap().get(state.getRecordId());
        if (id != null) {
            return new SegmentNodeState(id);
        } else {
            return state;
        }
    }
