// Generated from MML.g4 by ANTLR 4.5.2

package net.aeronica.libs.mml.core;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class MMLParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.5.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		CMD=1, LEN=2, OCTAVE=3, NOTE=4, ACC=5, MIDI=6, DOT=7, TIE=8, REST=9, INT=10, 
		BEGIN=11, PART=12, END=13, WS=14, JUNK=15;
	public static final int
		RULE_band = 0, RULE_inst = 1, RULE_test = 2, RULE_mono = 3, RULE_rest = 4, 
		RULE_note = 5, RULE_midi = 6, RULE_anote = 7, RULE_tied = 8, RULE_octave = 9, 
		RULE_cmd = 10, RULE_len = 11, RULE_begin = 12, RULE_part = 13, RULE_end = 14;
	public static final String[] ruleNames = {
		"band", "inst", "test", "mono", "rest", "note", "midi", "anote", "tied", 
		"octave", "cmd", "len", "begin", "part", "end"
	};

	private static final String[] _LITERAL_NAMES = {
		null, null, null, null, null, null, null, "'.'", "'&'", null, null, "'MML@'", 
		"','", "';'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "CMD", "LEN", "OCTAVE", "NOTE", "ACC", "MIDI", "DOT", "TIE", "REST", 
		"INT", "BEGIN", "PART", "END", "WS", "JUNK"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "MML.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public MMLParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class BandContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(MMLParser.EOF, 0); }
		public List<InstContext> inst() {
			return getRuleContexts(InstContext.class);
		}
		public InstContext inst(int i) {
			return getRuleContext(InstContext.class,i);
		}
		public BandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_band; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MMLListener ) ((MMLListener)listener).enterBand(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MMLListener ) ((MMLListener)listener).exitBand(this);
		}
	}

	public final BandContext band() throws RecognitionException {
		BandContext _localctx = new BandContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_band);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(31); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(30);
				inst();
				}
				}
				setState(33); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==BEGIN );
			setState(35);
			match(EOF);
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

	public static class InstContext extends ParserRuleContext {
		public BeginContext begin() {
			return getRuleContext(BeginContext.class,0);
		}
		public EndContext end() {
			return getRuleContext(EndContext.class,0);
		}
		public List<RestContext> rest() {
			return getRuleContexts(RestContext.class);
		}
		public RestContext rest(int i) {
			return getRuleContext(RestContext.class,i);
		}
		public List<AnoteContext> anote() {
			return getRuleContexts(AnoteContext.class);
		}
		public AnoteContext anote(int i) {
			return getRuleContext(AnoteContext.class,i);
		}
		public List<TiedContext> tied() {
			return getRuleContexts(TiedContext.class);
		}
		public TiedContext tied(int i) {
			return getRuleContext(TiedContext.class,i);
		}
		public List<OctaveContext> octave() {
			return getRuleContexts(OctaveContext.class);
		}
		public OctaveContext octave(int i) {
			return getRuleContext(OctaveContext.class,i);
		}
		public List<PartContext> part() {
			return getRuleContexts(PartContext.class);
		}
		public PartContext part(int i) {
			return getRuleContext(PartContext.class,i);
		}
		public List<CmdContext> cmd() {
			return getRuleContexts(CmdContext.class);
		}
		public CmdContext cmd(int i) {
			return getRuleContext(CmdContext.class,i);
		}
		public List<LenContext> len() {
			return getRuleContexts(LenContext.class);
		}
		public LenContext len(int i) {
			return getRuleContext(LenContext.class,i);
		}
		public InstContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inst; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MMLListener ) ((MMLListener)listener).enterInst(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MMLListener ) ((MMLListener)listener).exitInst(this);
		}
	}

	public final InstContext inst() throws RecognitionException {
		InstContext _localctx = new InstContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_inst);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(37);
			begin();
			setState(47);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << CMD) | (1L << LEN) | (1L << OCTAVE) | (1L << NOTE) | (1L << MIDI) | (1L << REST) | (1L << PART))) != 0)) {
				{
				setState(45);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
				case 1:
					{
					setState(38);
					rest();
					}
					break;
				case 2:
					{
					setState(39);
					anote();
					}
					break;
				case 3:
					{
					setState(40);
					tied();
					}
					break;
				case 4:
					{
					setState(41);
					octave();
					}
					break;
				case 5:
					{
					setState(42);
					part();
					}
					break;
				case 6:
					{
					setState(43);
					cmd();
					}
					break;
				case 7:
					{
					setState(44);
					len();
					}
					break;
				}
				}
				setState(49);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(50);
			end();
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

	public static class TestContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(MMLParser.EOF, 0); }
		public List<BeginContext> begin() {
			return getRuleContexts(BeginContext.class);
		}
		public BeginContext begin(int i) {
			return getRuleContext(BeginContext.class,i);
		}
		public List<MonoContext> mono() {
			return getRuleContexts(MonoContext.class);
		}
		public MonoContext mono(int i) {
			return getRuleContext(MonoContext.class,i);
		}
		public List<PartContext> part() {
			return getRuleContexts(PartContext.class);
		}
		public PartContext part(int i) {
			return getRuleContext(PartContext.class,i);
		}
		public List<EndContext> end() {
			return getRuleContexts(EndContext.class);
		}
		public EndContext end(int i) {
			return getRuleContext(EndContext.class,i);
		}
		public TestContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_test; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MMLListener ) ((MMLListener)listener).enterTest(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MMLListener ) ((MMLListener)listener).exitTest(this);
		}
	}

	public final TestContext test() throws RecognitionException {
		TestContext _localctx = new TestContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_test);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(55);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(52);
					begin();
					}
					} 
				}
				setState(57);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			}
			setState(60); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					setState(60);
					switch (_input.LA(1)) {
					case CMD:
					case LEN:
					case OCTAVE:
					case NOTE:
					case MIDI:
					case REST:
					case BEGIN:
					case END:
						{
						setState(58);
						mono();
						}
						break;
					case PART:
						{
						setState(59);
						part();
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(62); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			setState(67);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==END) {
				{
				{
				setState(64);
				end();
				}
				}
				setState(69);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(70);
			match(EOF);
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

	public static class MonoContext extends ParserRuleContext {
		public List<RestContext> rest() {
			return getRuleContexts(RestContext.class);
		}
		public RestContext rest(int i) {
			return getRuleContext(RestContext.class,i);
		}
		public List<AnoteContext> anote() {
			return getRuleContexts(AnoteContext.class);
		}
		public AnoteContext anote(int i) {
			return getRuleContext(AnoteContext.class,i);
		}
		public List<TiedContext> tied() {
			return getRuleContexts(TiedContext.class);
		}
		public TiedContext tied(int i) {
			return getRuleContext(TiedContext.class,i);
		}
		public List<OctaveContext> octave() {
			return getRuleContexts(OctaveContext.class);
		}
		public OctaveContext octave(int i) {
			return getRuleContext(OctaveContext.class,i);
		}
		public List<CmdContext> cmd() {
			return getRuleContexts(CmdContext.class);
		}
		public CmdContext cmd(int i) {
			return getRuleContext(CmdContext.class,i);
		}
		public List<LenContext> len() {
			return getRuleContexts(LenContext.class);
		}
		public LenContext len(int i) {
			return getRuleContext(LenContext.class,i);
		}
		public List<BeginContext> begin() {
			return getRuleContexts(BeginContext.class);
		}
		public BeginContext begin(int i) {
			return getRuleContext(BeginContext.class,i);
		}
		public List<EndContext> end() {
			return getRuleContexts(EndContext.class);
		}
		public EndContext end(int i) {
			return getRuleContext(EndContext.class,i);
		}
		public MonoContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mono; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MMLListener ) ((MMLListener)listener).enterMono(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MMLListener ) ((MMLListener)listener).exitMono(this);
		}
	}

	public final MonoContext mono() throws RecognitionException {
		MonoContext _localctx = new MonoContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_mono);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(80); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					setState(80);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
					case 1:
						{
						setState(72);
						rest();
						}
						break;
					case 2:
						{
						setState(73);
						anote();
						}
						break;
					case 3:
						{
						setState(74);
						tied();
						}
						break;
					case 4:
						{
						setState(75);
						octave();
						}
						break;
					case 5:
						{
						setState(76);
						cmd();
						}
						break;
					case 6:
						{
						setState(77);
						len();
						}
						break;
					case 7:
						{
						setState(78);
						begin();
						}
						break;
					case 8:
						{
						setState(79);
						end();
						}
						break;
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(82); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
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

	public static class RestContext extends ParserRuleContext {
		public TerminalNode REST() { return getToken(MMLParser.REST, 0); }
		public TerminalNode INT() { return getToken(MMLParser.INT, 0); }
		public List<TerminalNode> DOT() { return getTokens(MMLParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(MMLParser.DOT, i);
		}
		public List<TerminalNode> ACC() { return getTokens(MMLParser.ACC); }
		public TerminalNode ACC(int i) {
			return getToken(MMLParser.ACC, i);
		}
		public List<TerminalNode> TIE() { return getTokens(MMLParser.TIE); }
		public TerminalNode TIE(int i) {
			return getToken(MMLParser.TIE, i);
		}
		public RestContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rest; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MMLListener ) ((MMLListener)listener).enterRest(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MMLListener ) ((MMLListener)listener).exitRest(this);
		}
	}

	public final RestContext rest() throws RecognitionException {
		RestContext _localctx = new RestContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_rest);
		int _la;
		try {
			setState(114);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(84);
				match(REST);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(85);
				match(REST);
				setState(86);
				match(INT);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(87);
				match(REST);
				setState(89); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(88);
					match(DOT);
					}
					}
					setState(91); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==DOT );
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(93);
				match(REST);
				setState(94);
				match(INT);
				setState(96); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(95);
					match(DOT);
					}
					}
					setState(98); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==DOT );
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(100);
				match(REST);
				setState(104);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ACC) {
					{
					{
					setState(101);
					match(ACC);
					}
					}
					setState(106);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(107);
				match(REST);
				setState(111);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==TIE) {
					{
					{
					setState(108);
					match(TIE);
					}
					}
					setState(113);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
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

	public static class NoteContext extends ParserRuleContext {
		public TerminalNode NOTE() { return getToken(MMLParser.NOTE, 0); }
		public TerminalNode ACC() { return getToken(MMLParser.ACC, 0); }
		public TerminalNode INT() { return getToken(MMLParser.INT, 0); }
		public List<TerminalNode> DOT() { return getTokens(MMLParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(MMLParser.DOT, i);
		}
		public NoteContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_note; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MMLListener ) ((MMLListener)listener).enterNote(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MMLListener ) ((MMLListener)listener).exitNote(this);
		}
	}

	public final NoteContext note() throws RecognitionException {
		NoteContext _localctx = new NoteContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_note);
		int _la;
		try {
			setState(152);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(116);
				match(NOTE);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(117);
				match(NOTE);
				setState(118);
				match(ACC);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(119);
				match(NOTE);
				setState(120);
				match(INT);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(121);
				match(NOTE);
				setState(123); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(122);
					match(DOT);
					}
					}
					setState(125); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==DOT );
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(127);
				match(NOTE);
				setState(128);
				match(INT);
				setState(130); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(129);
					match(DOT);
					}
					}
					setState(132); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==DOT );
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(134);
				match(NOTE);
				setState(135);
				match(ACC);
				setState(136);
				match(INT);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(137);
				match(NOTE);
				setState(138);
				match(ACC);
				setState(140); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(139);
					match(DOT);
					}
					}
					setState(142); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==DOT );
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(144);
				match(NOTE);
				setState(145);
				match(ACC);
				setState(146);
				match(INT);
				setState(148); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(147);
					match(DOT);
					}
					}
					setState(150); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==DOT );
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

	public static class MidiContext extends ParserRuleContext {
		public TerminalNode MIDI() { return getToken(MMLParser.MIDI, 0); }
		public TerminalNode INT() { return getToken(MMLParser.INT, 0); }
		public MidiContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_midi; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MMLListener ) ((MMLListener)listener).enterMidi(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MMLListener ) ((MMLListener)listener).exitMidi(this);
		}
	}

	public final MidiContext midi() throws RecognitionException {
		MidiContext _localctx = new MidiContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_midi);
		try {
			setState(158);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(154);
				match(MIDI);
				setState(155);
				match(INT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(156);
				match(MIDI);
				notifyErrorListeners("midi note missing value");
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

	public static class AnoteContext extends ParserRuleContext {
		public NoteContext note() {
			return getRuleContext(NoteContext.class,0);
		}
		public MidiContext midi() {
			return getRuleContext(MidiContext.class,0);
		}
		public AnoteContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_anote; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MMLListener ) ((MMLListener)listener).enterAnote(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MMLListener ) ((MMLListener)listener).exitAnote(this);
		}
	}

	public final AnoteContext anote() throws RecognitionException {
		AnoteContext _localctx = new AnoteContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_anote);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(162);
			switch (_input.LA(1)) {
			case NOTE:
				{
				setState(160);
				note();
				}
				break;
			case MIDI:
				{
				setState(161);
				midi();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
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

	public static class TiedContext extends ParserRuleContext {
		public List<AnoteContext> anote() {
			return getRuleContexts(AnoteContext.class);
		}
		public AnoteContext anote(int i) {
			return getRuleContext(AnoteContext.class,i);
		}
		public List<TerminalNode> TIE() { return getTokens(MMLParser.TIE); }
		public TerminalNode TIE(int i) {
			return getToken(MMLParser.TIE, i);
		}
		public List<CmdContext> cmd() {
			return getRuleContexts(CmdContext.class);
		}
		public CmdContext cmd(int i) {
			return getRuleContext(CmdContext.class,i);
		}
		public List<LenContext> len() {
			return getRuleContexts(LenContext.class);
		}
		public LenContext len(int i) {
			return getRuleContext(LenContext.class,i);
		}
		public List<OctaveContext> octave() {
			return getRuleContexts(OctaveContext.class);
		}
		public OctaveContext octave(int i) {
			return getRuleContext(OctaveContext.class,i);
		}
		public TiedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tied; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MMLListener ) ((MMLListener)listener).enterTied(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MMLListener ) ((MMLListener)listener).exitTied(this);
		}
	}

	public final TiedContext tied() throws RecognitionException {
		TiedContext _localctx = new TiedContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_tied);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(164);
			anote();
			setState(183); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(170);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << CMD) | (1L << LEN) | (1L << OCTAVE))) != 0)) {
						{
						setState(168);
						switch (_input.LA(1)) {
						case CMD:
							{
							setState(165);
							cmd();
							}
							break;
						case LEN:
							{
							setState(166);
							len();
							}
							break;
						case OCTAVE:
							{
							setState(167);
							octave();
							}
							break;
						default:
							throw new NoViableAltException(this);
						}
						}
						setState(172);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(173);
					match(TIE);
					setState(179);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << CMD) | (1L << LEN) | (1L << OCTAVE))) != 0)) {
						{
						setState(177);
						switch (_input.LA(1)) {
						case CMD:
							{
							setState(174);
							cmd();
							}
							break;
						case LEN:
							{
							setState(175);
							len();
							}
							break;
						case OCTAVE:
							{
							setState(176);
							octave();
							}
							break;
						default:
							throw new NoViableAltException(this);
						}
						}
						setState(181);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(182);
					anote();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(185); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,25,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
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

	public static class OctaveContext extends ParserRuleContext {
		public TerminalNode OCTAVE() { return getToken(MMLParser.OCTAVE, 0); }
		public OctaveContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_octave; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MMLListener ) ((MMLListener)listener).enterOctave(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MMLListener ) ((MMLListener)listener).exitOctave(this);
		}
	}

	public final OctaveContext octave() throws RecognitionException {
		OctaveContext _localctx = new OctaveContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_octave);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(187);
			match(OCTAVE);
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

	public static class CmdContext extends ParserRuleContext {
		public TerminalNode CMD() { return getToken(MMLParser.CMD, 0); }
		public TerminalNode INT() { return getToken(MMLParser.INT, 0); }
		public CmdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cmd; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MMLListener ) ((MMLListener)listener).enterCmd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MMLListener ) ((MMLListener)listener).exitCmd(this);
		}
	}

	public final CmdContext cmd() throws RecognitionException {
		CmdContext _localctx = new CmdContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_cmd);
		try {
			setState(193);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(189);
				match(CMD);
				setState(190);
				match(INT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(191);
				match(CMD);
				notifyErrorListeners("[octave | tempo | volume] missing value");
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

	public static class LenContext extends ParserRuleContext {
		public TerminalNode LEN() { return getToken(MMLParser.LEN, 0); }
		public TerminalNode INT() { return getToken(MMLParser.INT, 0); }
		public List<TerminalNode> DOT() { return getTokens(MMLParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(MMLParser.DOT, i);
		}
		public LenContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_len; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MMLListener ) ((MMLListener)listener).enterLen(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MMLListener ) ((MMLListener)listener).exitLen(this);
		}
	}

	public final LenContext len() throws RecognitionException {
		LenContext _localctx = new LenContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_len);
		int _la;
		try {
			setState(206);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,28,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(195);
				match(LEN);
				setState(196);
				match(INT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(197);
				match(LEN);
				setState(198);
				match(INT);
				setState(200); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(199);
					match(DOT);
					}
					}
					setState(202); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==DOT );
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(204);
				match(LEN);
				notifyErrorListeners("length missing value");
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

	public static class BeginContext extends ParserRuleContext {
		public TerminalNode BEGIN() { return getToken(MMLParser.BEGIN, 0); }
		public BeginContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_begin; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MMLListener ) ((MMLListener)listener).enterBegin(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MMLListener ) ((MMLListener)listener).exitBegin(this);
		}
	}

	public final BeginContext begin() throws RecognitionException {
		BeginContext _localctx = new BeginContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_begin);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(208);
			match(BEGIN);
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

	public static class PartContext extends ParserRuleContext {
		public TerminalNode PART() { return getToken(MMLParser.PART, 0); }
		public PartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_part; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MMLListener ) ((MMLListener)listener).enterPart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MMLListener ) ((MMLListener)listener).exitPart(this);
		}
	}

	public final PartContext part() throws RecognitionException {
		PartContext _localctx = new PartContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_part);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(210);
			match(PART);
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

	public static class EndContext extends ParserRuleContext {
		public TerminalNode END() { return getToken(MMLParser.END, 0); }
		public EndContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_end; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MMLListener ) ((MMLListener)listener).enterEnd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MMLListener ) ((MMLListener)listener).exitEnd(this);
		}
	}

	public final EndContext end() throws RecognitionException {
		EndContext _localctx = new EndContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_end);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(212);
			match(END);
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

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\21\u00d9\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\3\2\6\2\"\n\2\r\2"+
		"\16\2#\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\7\3\60\n\3\f\3\16\3\63"+
		"\13\3\3\3\3\3\3\4\7\48\n\4\f\4\16\4;\13\4\3\4\3\4\6\4?\n\4\r\4\16\4@\3"+
		"\4\7\4D\n\4\f\4\16\4G\13\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\6\5"+
		"S\n\5\r\5\16\5T\3\6\3\6\3\6\3\6\3\6\6\6\\\n\6\r\6\16\6]\3\6\3\6\3\6\6"+
		"\6c\n\6\r\6\16\6d\3\6\3\6\7\6i\n\6\f\6\16\6l\13\6\3\6\3\6\7\6p\n\6\f\6"+
		"\16\6s\13\6\5\6u\n\6\3\7\3\7\3\7\3\7\3\7\3\7\3\7\6\7~\n\7\r\7\16\7\177"+
		"\3\7\3\7\3\7\6\7\u0085\n\7\r\7\16\7\u0086\3\7\3\7\3\7\3\7\3\7\3\7\6\7"+
		"\u008f\n\7\r\7\16\7\u0090\3\7\3\7\3\7\3\7\6\7\u0097\n\7\r\7\16\7\u0098"+
		"\5\7\u009b\n\7\3\b\3\b\3\b\3\b\5\b\u00a1\n\b\3\t\3\t\5\t\u00a5\n\t\3\n"+
		"\3\n\3\n\3\n\7\n\u00ab\n\n\f\n\16\n\u00ae\13\n\3\n\3\n\3\n\3\n\7\n\u00b4"+
		"\n\n\f\n\16\n\u00b7\13\n\3\n\6\n\u00ba\n\n\r\n\16\n\u00bb\3\13\3\13\3"+
		"\f\3\f\3\f\3\f\5\f\u00c4\n\f\3\r\3\r\3\r\3\r\3\r\6\r\u00cb\n\r\r\r\16"+
		"\r\u00cc\3\r\3\r\5\r\u00d1\n\r\3\16\3\16\3\17\3\17\3\20\3\20\3\20\2\2"+
		"\21\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36\2\2\u00fe\2!\3\2\2\2\4\'\3"+
		"\2\2\2\69\3\2\2\2\bR\3\2\2\2\nt\3\2\2\2\f\u009a\3\2\2\2\16\u00a0\3\2\2"+
		"\2\20\u00a4\3\2\2\2\22\u00a6\3\2\2\2\24\u00bd\3\2\2\2\26\u00c3\3\2\2\2"+
		"\30\u00d0\3\2\2\2\32\u00d2\3\2\2\2\34\u00d4\3\2\2\2\36\u00d6\3\2\2\2 "+
		"\"\5\4\3\2! \3\2\2\2\"#\3\2\2\2#!\3\2\2\2#$\3\2\2\2$%\3\2\2\2%&\7\2\2"+
		"\3&\3\3\2\2\2\'\61\5\32\16\2(\60\5\n\6\2)\60\5\20\t\2*\60\5\22\n\2+\60"+
		"\5\24\13\2,\60\5\34\17\2-\60\5\26\f\2.\60\5\30\r\2/(\3\2\2\2/)\3\2\2\2"+
		"/*\3\2\2\2/+\3\2\2\2/,\3\2\2\2/-\3\2\2\2/.\3\2\2\2\60\63\3\2\2\2\61/\3"+
		"\2\2\2\61\62\3\2\2\2\62\64\3\2\2\2\63\61\3\2\2\2\64\65\5\36\20\2\65\5"+
		"\3\2\2\2\668\5\32\16\2\67\66\3\2\2\28;\3\2\2\29\67\3\2\2\29:\3\2\2\2:"+
		">\3\2\2\2;9\3\2\2\2<?\5\b\5\2=?\5\34\17\2><\3\2\2\2>=\3\2\2\2?@\3\2\2"+
		"\2@>\3\2\2\2@A\3\2\2\2AE\3\2\2\2BD\5\36\20\2CB\3\2\2\2DG\3\2\2\2EC\3\2"+
		"\2\2EF\3\2\2\2FH\3\2\2\2GE\3\2\2\2HI\7\2\2\3I\7\3\2\2\2JS\5\n\6\2KS\5"+
		"\20\t\2LS\5\22\n\2MS\5\24\13\2NS\5\26\f\2OS\5\30\r\2PS\5\32\16\2QS\5\36"+
		"\20\2RJ\3\2\2\2RK\3\2\2\2RL\3\2\2\2RM\3\2\2\2RN\3\2\2\2RO\3\2\2\2RP\3"+
		"\2\2\2RQ\3\2\2\2ST\3\2\2\2TR\3\2\2\2TU\3\2\2\2U\t\3\2\2\2Vu\7\13\2\2W"+
		"X\7\13\2\2Xu\7\f\2\2Y[\7\13\2\2Z\\\7\t\2\2[Z\3\2\2\2\\]\3\2\2\2][\3\2"+
		"\2\2]^\3\2\2\2^u\3\2\2\2_`\7\13\2\2`b\7\f\2\2ac\7\t\2\2ba\3\2\2\2cd\3"+
		"\2\2\2db\3\2\2\2de\3\2\2\2eu\3\2\2\2fj\7\13\2\2gi\7\7\2\2hg\3\2\2\2il"+
		"\3\2\2\2jh\3\2\2\2jk\3\2\2\2ku\3\2\2\2lj\3\2\2\2mq\7\13\2\2np\7\n\2\2"+
		"on\3\2\2\2ps\3\2\2\2qo\3\2\2\2qr\3\2\2\2ru\3\2\2\2sq\3\2\2\2tV\3\2\2\2"+
		"tW\3\2\2\2tY\3\2\2\2t_\3\2\2\2tf\3\2\2\2tm\3\2\2\2u\13\3\2\2\2v\u009b"+
		"\7\6\2\2wx\7\6\2\2x\u009b\7\7\2\2yz\7\6\2\2z\u009b\7\f\2\2{}\7\6\2\2|"+
		"~\7\t\2\2}|\3\2\2\2~\177\3\2\2\2\177}\3\2\2\2\177\u0080\3\2\2\2\u0080"+
		"\u009b\3\2\2\2\u0081\u0082\7\6\2\2\u0082\u0084\7\f\2\2\u0083\u0085\7\t"+
		"\2\2\u0084\u0083\3\2\2\2\u0085\u0086\3\2\2\2\u0086\u0084\3\2\2\2\u0086"+
		"\u0087\3\2\2\2\u0087\u009b\3\2\2\2\u0088\u0089\7\6\2\2\u0089\u008a\7\7"+
		"\2\2\u008a\u009b\7\f\2\2\u008b\u008c\7\6\2\2\u008c\u008e\7\7\2\2\u008d"+
		"\u008f\7\t\2\2\u008e\u008d\3\2\2\2\u008f\u0090\3\2\2\2\u0090\u008e\3\2"+
		"\2\2\u0090\u0091\3\2\2\2\u0091\u009b\3\2\2\2\u0092\u0093\7\6\2\2\u0093"+
		"\u0094\7\7\2\2\u0094\u0096\7\f\2\2\u0095\u0097\7\t\2\2\u0096\u0095\3\2"+
		"\2\2\u0097\u0098\3\2\2\2\u0098\u0096\3\2\2\2\u0098\u0099\3\2\2\2\u0099"+
		"\u009b\3\2\2\2\u009av\3\2\2\2\u009aw\3\2\2\2\u009ay\3\2\2\2\u009a{\3\2"+
		"\2\2\u009a\u0081\3\2\2\2\u009a\u0088\3\2\2\2\u009a\u008b\3\2\2\2\u009a"+
		"\u0092\3\2\2\2\u009b\r\3\2\2\2\u009c\u009d\7\b\2\2\u009d\u00a1\7\f\2\2"+
		"\u009e\u009f\7\b\2\2\u009f\u00a1\b\b\1\2\u00a0\u009c\3\2\2\2\u00a0\u009e"+
		"\3\2\2\2\u00a1\17\3\2\2\2\u00a2\u00a5\5\f\7\2\u00a3\u00a5\5\16\b\2\u00a4"+
		"\u00a2\3\2\2\2\u00a4\u00a3\3\2\2\2\u00a5\21\3\2\2\2\u00a6\u00b9\5\20\t"+
		"\2\u00a7\u00ab\5\26\f\2\u00a8\u00ab\5\30\r\2\u00a9\u00ab\5\24\13\2\u00aa"+
		"\u00a7\3\2\2\2\u00aa\u00a8\3\2\2\2\u00aa\u00a9\3\2\2\2\u00ab\u00ae\3\2"+
		"\2\2\u00ac\u00aa\3\2\2\2\u00ac\u00ad\3\2\2\2\u00ad\u00af\3\2\2\2\u00ae"+
		"\u00ac\3\2\2\2\u00af\u00b5\7\n\2\2\u00b0\u00b4\5\26\f\2\u00b1\u00b4\5"+
		"\30\r\2\u00b2\u00b4\5\24\13\2\u00b3\u00b0\3\2\2\2\u00b3\u00b1\3\2\2\2"+
		"\u00b3\u00b2\3\2\2\2\u00b4\u00b7\3\2\2\2\u00b5\u00b3\3\2\2\2\u00b5\u00b6"+
		"\3\2\2\2\u00b6\u00b8\3\2\2\2\u00b7\u00b5\3\2\2\2\u00b8\u00ba\5\20\t\2"+
		"\u00b9\u00ac\3\2\2\2\u00ba\u00bb\3\2\2\2\u00bb\u00b9\3\2\2\2\u00bb\u00bc"+
		"\3\2\2\2\u00bc\23\3\2\2\2\u00bd\u00be\7\5\2\2\u00be\25\3\2\2\2\u00bf\u00c0"+
		"\7\3\2\2\u00c0\u00c4\7\f\2\2\u00c1\u00c2\7\3\2\2\u00c2\u00c4\b\f\1\2\u00c3"+
		"\u00bf\3\2\2\2\u00c3\u00c1\3\2\2\2\u00c4\27\3\2\2\2\u00c5\u00c6\7\4\2"+
		"\2\u00c6\u00d1\7\f\2\2\u00c7\u00c8\7\4\2\2\u00c8\u00ca\7\f\2\2\u00c9\u00cb"+
		"\7\t\2\2\u00ca\u00c9\3\2\2\2\u00cb\u00cc\3\2\2\2\u00cc\u00ca\3\2\2\2\u00cc"+
		"\u00cd\3\2\2\2\u00cd\u00d1\3\2\2\2\u00ce\u00cf\7\4\2\2\u00cf\u00d1\b\r"+
		"\1\2\u00d0\u00c5\3\2\2\2\u00d0\u00c7\3\2\2\2\u00d0\u00ce\3\2\2\2\u00d1"+
		"\31\3\2\2\2\u00d2\u00d3\7\r\2\2\u00d3\33\3\2\2\2\u00d4\u00d5\7\16\2\2"+
		"\u00d5\35\3\2\2\2\u00d6\u00d7\7\17\2\2\u00d7\37\3\2\2\2\37#/\619>@ERT"+
		"]djqt\177\u0086\u0090\u0098\u009a\u00a0\u00a4\u00aa\u00ac\u00b3\u00b5"+
		"\u00bb\u00c3\u00cc\u00d0";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}