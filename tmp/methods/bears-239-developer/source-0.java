    protected Either<ObjectProperty, AssignmentTargetProperty> parsePropertyDefinition() throws JsError {
        AdditionalStateT startState = this.startNode();
        SourceLocation startLocation = this.getLocation();
        Token token = this.lookahead;

        Either<PropertyName, MethodDefinition> keyOrMethod = this.parseMethodDefinition();

        if (keyOrMethod.isRight()) {
            this.isBindingElement = this.isAssignmentTarget = false;
            return Either.left(keyOrMethod.right().fromJust());
        } else if (keyOrMethod.isLeft()) {
            PropertyName propName = keyOrMethod.left().fromJust();
            if (propName instanceof StaticPropertyName) {
                StaticPropertyName staticPropertyName = (StaticPropertyName) propName;
                if (this.eat(TokenType.ASSIGN)) {
                    Expression init = this.isolateCoverGrammar(this::parseAssignmentExpression).left().fromJust();
                    this.firstExprError = this.createErrorWithLocation(startLocation, ErrorMessages.ILLEGAL_PROPERTY);
                    AssignmentTargetPropertyIdentifier toReturn = new AssignmentTargetPropertyIdentifier(this.transformDestructuring(staticPropertyName), Maybe.of(init));
                    return Either.right(this.finishNode(startState, toReturn));
                }
                if (!this.match(TokenType.COLON)) {
                    if (token.type != TokenType.IDENTIFIER && token.type != TokenType.YIELD && token.type != TokenType.LET && token.type != TokenType.ASYNC) {
                        throw this.createUnexpected(token);
                    }
                    ShorthandProperty toReturn = new ShorthandProperty(this.finishNode(startState, new IdentifierExpression(staticPropertyName.value)));
                    return Either.left(this.finishNode(startState, toReturn));
                }
            }
        }

        this.expect(TokenType.COLON);

        PropertyName name = keyOrMethod.left().fromJust();
        Either<Expression, AssignmentTarget> val = this.inheritCoverGrammar(this::parseAssignmentExpressionOrTarget);

        return val.map(
                expr -> this.finishNode(startState, new DataProperty(name, expr)),
                binding -> this.finishNode(startState, new AssignmentTargetPropertyProperty(name, binding))
        );
    }
