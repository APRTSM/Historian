    public Expression createExpression(CamelContext camelContext, Annotation annotation, LanguageAnnotation languageAnnotation, Class<?> expressionReturnType) {
        String languageName = languageAnnotation.language();
        if (languageName == null) {
            throw new IllegalArgumentException("Cannot determine the language from the annotation: " + annotation);
        }
        Language language = camelContext.resolveLanguage(languageName);
        if (language == null) {
            throw new IllegalArgumentException("Cannot find the language: " + languageName + " on the classpath");
        }
        String expression = getExpressionFromAnnotation(annotation);
        return language.createExpression(expression);
    }
