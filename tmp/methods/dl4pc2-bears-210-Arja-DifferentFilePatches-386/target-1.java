	private FactorContext factor(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		FactorContext _localctx = new FactorContext(_ctx, _parentState);
		FactorContext _prevctx = _localctx;
		int _startState = 6;
		enterRecursionRule(_localctx, 6, RULE_factor, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(93);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DOUBLE:
				{
				_localctx = new NumberExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(81);
				match(DOUBLE);
				}
				break;
			case INTEGER:
				{
				_localctx = new NumberExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(82);
				match(INTEGER);
				}
				break;
			case STRING:
				{
				_localctx = new StringExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(83);
				match(STRING);
				}
				break;
			case TRUE:
				{
				_localctx = new BooleanExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(84);
				match(TRUE);
				}
				break;
			case FALSE:
				{
				_localctx = new BooleanExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(85);
				match(FALSE);
				}
				break;
			case UNDEFINED:
				{
				_localctx = new NullExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(86);
				match(UNDEFINED);
				}
				break;
			case NULL:
				{
				_localctx = new NullExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(87);
				match(NULL);
				}
				break;
			case VARIABLE:
				{
				_localctx = new VariableExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				match(VARIABLE);
				}
				break;
			case LPAREN:
				{
				_localctx = new ParenExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(89);
				match(LPAREN);
				setState(90);
				expression(0);
				setState(91);
				match(RPAREN);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(115);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(113);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
					case 1:
						{
						_localctx = new MathExprContext(new FactorContext(_parentctx, _parentState));
						((MathExprContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_factor);
						setState(95);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(96);
						((MathExprContext)_localctx).op = match(POW);
						setState(97);
						((MathExprContext)_localctx).right = factor(7);
						}
						break;
					case 2:
						{
						_localctx = new MathExprContext(new FactorContext(_parentctx, _parentState));
						((MathExprContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_factor);
						setState(98);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(99);
						((MathExprContext)_localctx).op = match(TIMES);
						setState(100);
						((MathExprContext)_localctx).right = factor(6);
						}
						break;
					case 3:
						{
						_localctx = new MathExprContext(new FactorContext(_parentctx, _parentState));
						((MathExprContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_factor);
						setState(101);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(102);
						((MathExprContext)_localctx).op = match(DIVIDE);
						setState(103);
						((MathExprContext)_localctx).right = factor(5);
						}
						break;
					case 4:
						{
						_localctx = new MathExprContext(new FactorContext(_parentctx, _parentState));
						((MathExprContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_factor);
						setState(104);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(105);
						((MathExprContext)_localctx).op = match(PLUS);
						setState(106);
						((MathExprContext)_localctx).right = factor(4);
						}
						break;
					case 5:
						{
						_localctx = new MathExprContext(new FactorContext(_parentctx, _parentState));
						((MathExprContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_factor);
						setState(107);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(108);
						((MathExprContext)_localctx).op = match(MINUS);
						setState(109);
						((MathExprContext)_localctx).right = factor(3);
						}
						break;
					case 6:
						{
						_localctx = new MathExprContext(new FactorContext(_parentctx, _parentState));
						((MathExprContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_factor);
						setState(110);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(111);
						((MathExprContext)_localctx).op = match(MOD);
						setState(112);
						((MathExprContext)_localctx).right = factor(2);
						}
						break;
					}
					} 
				}
				setState(117);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}
	public final TermContext term() throws RecognitionException {
		TermContext _localctx = new TermContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_term);
		try {
			setState(78);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				_localctx = new CompareExprContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(27);
				((CompareExprContext)_localctx).left = factor(0);
				setState(28);
				((CompareExprContext)_localctx).op = match(GREATER_THAN);
				setState(29);
				((CompareExprContext)_localctx).right = factor(0);
				}
				break;
			case 2:
				_localctx = new CompareExprContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(31);
				((CompareExprContext)_localctx).left = factor(0);
				setState(32);
				((CompareExprContext)_localctx).op = match(GREATER_THAN_EQUALS);
				setState(33);
				((CompareExprContext)_localctx).right = factor(0);
				}
				break;
			case 3:
				_localctx = new CompareExprContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(35);
				((CompareExprContext)_localctx).left = factor(0);
				setState(36);
				((CompareExprContext)_localctx).op = match(LESS_THAN);
				setState(37);
				((CompareExprContext)_localctx).right = factor(0);
				}
				break;
			case 4:
				_localctx = new CompareExprContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(39);
				((CompareExprContext)_localctx).left = factor(0);
				setState(40);
				((CompareExprContext)_localctx).op = match(LESS_THAN_EQUALS);
				setState(41);
				((CompareExprContext)_localctx).right = factor(0);
				}
				break;
			case 5:
				_localctx = new EqualExprContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(43);
				((EqualExprContext)_localctx).left = factor(0);
				setState(44);
				((EqualExprContext)_localctx).op = match(EQUALS);
				setState(45);
				((EqualExprContext)_localctx).right = factor(0);
				}
				break;
			case 6:
				_localctx = new EqualExprContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(47);
				((EqualExprContext)_localctx).left = factor(0);
				setState(48);
				((EqualExprContext)_localctx).op = match(DBL_EQUALS);
				setState(49);
				((EqualExprContext)_localctx).right = factor(0);
				}
				break;
			case 7:
				_localctx = new NotEqualExprContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(51);
				((NotEqualExprContext)_localctx).left = factor(0);
				setState(52);
				((NotEqualExprContext)_localctx).op = match(NOT_EQUALS);
				setState(53);
				((NotEqualExprContext)_localctx).right = factor(0);
				}
				break;
			case 8:
				_localctx = new NotEqualExprContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(55);
				((NotEqualExprContext)_localctx).left = factor(0);
				setState(56);
				((NotEqualExprContext)_localctx).op = match(IS_EQUALS_NOT);
				setState(57);
				((NotEqualExprContext)_localctx).right = factor(0);
				}
				break;
			case 9:
				_localctx = new EqualExprContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(59);
				((EqualExprContext)_localctx).left = factor(0);
				setState(60);
				((EqualExprContext)_localctx).op = match(IS_EQUALS);
				setState(61);
				((EqualExprContext)_localctx).right = factor(0);
				}
				break;
			case 10:
				_localctx = new EmptyExprContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(63);
				((EmptyExprContext)_localctx).left = factor(0);
				setState(64);
				((EmptyExprContext)_localctx).op = match(IS_EMPTY);
				}
				break;
			case 11:
				_localctx = new EmptyExprContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(66);
				((EmptyExprContext)_localctx).left = factor(0);
				setState(67);
				((EmptyExprContext)_localctx).op = match(IS_NOT_EMPTY);
				}
				break;
			case 12:
				_localctx = new ContainsExprContext(_localctx);
				enterOuterAlt(_localctx, 12);
				{
				setState(69);
				((ContainsExprContext)_localctx).left = factor(0);
				setState(70);
				((ContainsExprContext)_localctx).op = match(CONTAINS);
				setState(71);
				((ContainsExprContext)_localctx).right = factor(0);
				}
				break;
			case 13:
				_localctx = new MatchesExprContext(_localctx);
				enterOuterAlt(_localctx, 13);
				{
				setState(73);
				((MatchesExprContext)_localctx).left = factor(0);
				setState(74);
				((MatchesExprContext)_localctx).op = match(MATCHES);
				setState(75);
				((MatchesExprContext)_localctx).right = factor(0);
				}
				break;
			case 14:
				_localctx = new FactorExprContext(_localctx);
				;
				{
				setState(77);
				factor(0);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}
    public Object getValue(final PredicateContext context) {
        if (context == null)
            return null;
        Map<String, Object> cachedValues = context.getCachedValues();
        Object value = cachedValues.get(variableName);
        return value;
    }
