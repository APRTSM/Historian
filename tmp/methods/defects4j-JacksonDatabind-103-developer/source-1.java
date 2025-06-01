    public JavaType resolveSubType(JavaType baseType, String subClass)
        throws JsonMappingException
    {
        // 30-Jan-2010, tatu: Most ids are basic class names; so let's first
        //    check if any generics info is added; and only then ask factory
        //    to do translation when necessary
        if (subClass.indexOf('<') > 0) {
            // note: may want to try combining with specialization (esp for EnumMap)?
            // 17-Aug-2017, tatu: As per [databind#1735] need to ensure assignment
            //    compatibility -- needed later anyway, and not doing so may open
            //    security issues.
            JavaType t = getTypeFactory().constructFromCanonical(subClass);
            if (t.isTypeOrSubTypeOf(baseType.getRawClass())) {
                return t;
            }
        } else {
            Class<?> cls;
            try {
                cls =  getTypeFactory().findClass(subClass);
            } catch (ClassNotFoundException e) { // let caller handle this problem
                return null;
            } catch (Exception e) {
                throw invalidTypeIdException(baseType, subClass, String.format(
                        "problem: (%s) %s",
                        e.getClass().getName(),
                        e.getMessage()));
            }
            if (baseType.isTypeOrSuperTypeOf(cls)) {
                return getTypeFactory().constructSpecializedType(baseType, cls);
            }
        }
        throw invalidTypeIdException(baseType, subClass, "Not a subtype");
    }
    public Date parseDate(String dateStr) throws IllegalArgumentException
    {
        try {
            DateFormat df = getDateFormat();
            return df.parse(dateStr);
        } catch (ParseException e) {
            throw new IllegalArgumentException(String.format(
                    "Failed to parse Date value '%s': %s", dateStr,
                    e.getMessage()));
        }
    }
    public JsonMappingException instantiationException(Class<?> instClass, Throwable cause) {
        // Most likely problem with Creator definition, right?
        final JavaType type = constructType(instClass);
        String excMsg;
        if (cause == null) {
            excMsg = "N/A";
        } else if ((excMsg = cause.getMessage()) == null) {
            excMsg = ClassUtil.nameOf(cause.getClass());
        }
        String msg = String.format("Cannot construct instance of %s, problem: %s",
                ClassUtil.nameOf(instClass), excMsg);
        InvalidDefinitionException e = InvalidDefinitionException.from(_parser, msg, type);
        e.initCause(cause);
        return e;
    }
