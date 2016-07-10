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
		RULE_band = 0, RULE_inst = 1, RULE_rest = 2, RULE_note = 3, RULE_midi = 4, 
		RULE_anote = 5, RULE_tied = 6, RULE_octave = 7, RULE_cmd = 8, RULE_len = 9, 
		RULE_begin = 10, RULE_part = 11, RULE_end = 12;
	public static final String[] ruleNames = {
		"band", "inst", "rest", "note", "midi", "anote", "tied", "octave", "cmd", 
		"len", "begin", "part", "end"
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
			setState(27); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(26);
				inst();
				}
				}
				setState(29); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==BEGIN );
			setState(31);
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
			setState(33);
			begin();
			setState(43);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << CMD) | (1L << LEN) | (1L << OCTAVE) | (1L << NOTE) | (1L << MIDI) | (1L << REST) | (1L << PART))) != 0)) {
				{
				setState(41);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
				case 1:
					{
					setState(34);
					rest();
					}
					break;
				case 2:
					{
					setState(35);
					anote();
					}
					break;
				case 3:
					{
					setState(36);
					tied();
					}
					break;
				case 4:
					{
					setState(37);
					octave();
					}
					break;
				case 5:
					{
					setState(38);
					part();
					}
					break;
				case 6:
					{
					setState(39);
					cmd();
					}
					break;
				case 7:
					{
					setState(40);
					len();
					}
					break;
				}
				}
				setState(45);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(46);
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

	public static class RestContext extends ParserRuleContext {
		public TerminalNode REST() { return getToken(MMLParser.REST, 0); }
		public TerminalNode INT() { return getToken(MMLParser.INT, 0); }
		public List<TerminalNode> DOT() { return getTokens(MMLParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(MMLParser.DOT, i);
		}
		public TerminalNode ACC() { return getToken(MMLParser.ACC, 0); }
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
		enterRule(_localctx, 4, RULE_rest);
		int _la;
		try {
			setState(67);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(48);
				match(REST);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(49);
				match(REST);
				setState(50);
				match(INT);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(51);
				match(REST);
				setState(53); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(52);
					match(DOT);
					}
					}
					setState(55); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==DOT );
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(57);
				match(REST);
				setState(58);
				match(INT);
				setState(60); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(59);
					match(DOT);
					}
					}
					setState(62); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==DOT );
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(64);
				match(REST);
				setState(65);
				match(ACC);
				notifyErrorListeners("unexpected accidental '+#-'");
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
		enterRule(_localctx, 6, RULE_note);
		int _la;
		try {
			setState(105);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(69);
				match(NOTE);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(70);
				match(NOTE);
				setState(71);
				match(ACC);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(72);
				match(NOTE);
				setState(73);
				match(INT);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(74);
				match(NOTE);
				setState(76); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(75);
					match(DOT);
					}
					}
					setState(78); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==DOT );
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(80);
				match(NOTE);
				setState(81);
				match(INT);
				setState(83); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(82);
					match(DOT);
					}
					}
					setState(85); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==DOT );
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(87);
				match(NOTE);
				setState(88);
				match(ACC);
				setState(89);
				match(INT);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(90);
				match(NOTE);
				setState(91);
				match(ACC);
				setState(93); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(92);
					match(DOT);
					}
					}
					setState(95); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==DOT );
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(97);
				match(NOTE);
				setState(98);
				match(ACC);
				setState(99);
				match(INT);
				setState(101); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(100);
					match(DOT);
					}
					}
					setState(103); 
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
		enterRule(_localctx, 8, RULE_midi);
		try {
			setState(111);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(107);
				match(MIDI);
				setState(108);
				match(INT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(109);
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
		enterRule(_localctx, 10, RULE_anote);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(115);
			switch (_input.LA(1)) {
			case NOTE:
				{
				setState(113);
				note();
				}
				break;
			case MIDI:
				{
				setState(114);
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
		public List<TerminalNode> TIE() { return getTokens(MMLParser.TIE); }
		public TerminalNode TIE(int i) {
			return getToken(MMLParser.TIE, i);
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
		enterRule(_localctx, 12, RULE_tied);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(117);
			anote();
			setState(123);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << CMD) | (1L << LEN) | (1L << OCTAVE))) != 0)) {
				{
				setState(121);
				switch (_input.LA(1)) {
				case CMD:
					{
					setState(118);
					cmd();
					}
					break;
				case LEN:
					{
					setState(119);
					len();
					}
					break;
				case OCTAVE:
					{
					setState(120);
					octave();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(125);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(136); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(126);
				match(TIE);
				setState(132);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << CMD) | (1L << LEN) | (1L << OCTAVE))) != 0)) {
					{
					setState(130);
					switch (_input.LA(1)) {
					case CMD:
						{
						setState(127);
						cmd();
						}
						break;
					case LEN:
						{
						setState(128);
						len();
						}
						break;
					case OCTAVE:
						{
						setState(129);
						octave();
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					setState(134);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(135);
				anote();
				}
				}
				setState(138); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==TIE );
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
		enterRule(_localctx, 14, RULE_octave);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(140);
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
		enterRule(_localctx, 16, RULE_cmd);
		try {
			setState(146);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(142);
				match(CMD);
				setState(143);
				match(INT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(144);
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
		enterRule(_localctx, 18, RULE_len);
		int _la;
		try {
			setState(159);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,20,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(148);
				match(LEN);
				setState(149);
				match(INT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(150);
				match(LEN);
				setState(151);
				match(INT);
				setState(153); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(152);
					match(DOT);
					}
					}
					setState(155); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==DOT );
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(157);
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
		enterRule(_localctx, 20, RULE_begin);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(161);
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
		enterRule(_localctx, 22, RULE_part);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(163);
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
		enterRule(_localctx, 24, RULE_end);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(165);
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\21\u00aa\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\3\2\6\2\36\n\2\r\2\16\2\37\3\2\3\2\3\3"+
		"\3\3\3\3\3\3\3\3\3\3\3\3\3\3\7\3,\n\3\f\3\16\3/\13\3\3\3\3\3\3\4\3\4\3"+
		"\4\3\4\3\4\6\48\n\4\r\4\16\49\3\4\3\4\3\4\6\4?\n\4\r\4\16\4@\3\4\3\4\3"+
		"\4\5\4F\n\4\3\5\3\5\3\5\3\5\3\5\3\5\3\5\6\5O\n\5\r\5\16\5P\3\5\3\5\3\5"+
		"\6\5V\n\5\r\5\16\5W\3\5\3\5\3\5\3\5\3\5\3\5\6\5`\n\5\r\5\16\5a\3\5\3\5"+
		"\3\5\3\5\6\5h\n\5\r\5\16\5i\5\5l\n\5\3\6\3\6\3\6\3\6\5\6r\n\6\3\7\3\7"+
		"\5\7v\n\7\3\b\3\b\3\b\3\b\7\b|\n\b\f\b\16\b\177\13\b\3\b\3\b\3\b\3\b\7"+
		"\b\u0085\n\b\f\b\16\b\u0088\13\b\3\b\6\b\u008b\n\b\r\b\16\b\u008c\3\t"+
		"\3\t\3\n\3\n\3\n\3\n\5\n\u0095\n\n\3\13\3\13\3\13\3\13\3\13\6\13\u009c"+
		"\n\13\r\13\16\13\u009d\3\13\3\13\5\13\u00a2\n\13\3\f\3\f\3\r\3\r\3\16"+
		"\3\16\3\16\2\2\17\2\4\6\b\n\f\16\20\22\24\26\30\32\2\2\u00c2\2\35\3\2"+
		"\2\2\4#\3\2\2\2\6E\3\2\2\2\bk\3\2\2\2\nq\3\2\2\2\fu\3\2\2\2\16w\3\2\2"+
		"\2\20\u008e\3\2\2\2\22\u0094\3\2\2\2\24\u00a1\3\2\2\2\26\u00a3\3\2\2\2"+
		"\30\u00a5\3\2\2\2\32\u00a7\3\2\2\2\34\36\5\4\3\2\35\34\3\2\2\2\36\37\3"+
		"\2\2\2\37\35\3\2\2\2\37 \3\2\2\2 !\3\2\2\2!\"\7\2\2\3\"\3\3\2\2\2#-\5"+
		"\26\f\2$,\5\6\4\2%,\5\f\7\2&,\5\16\b\2\',\5\20\t\2(,\5\30\r\2),\5\22\n"+
		"\2*,\5\24\13\2+$\3\2\2\2+%\3\2\2\2+&\3\2\2\2+\'\3\2\2\2+(\3\2\2\2+)\3"+
		"\2\2\2+*\3\2\2\2,/\3\2\2\2-+\3\2\2\2-.\3\2\2\2.\60\3\2\2\2/-\3\2\2\2\60"+
		"\61\5\32\16\2\61\5\3\2\2\2\62F\7\13\2\2\63\64\7\13\2\2\64F\7\f\2\2\65"+
		"\67\7\13\2\2\668\7\t\2\2\67\66\3\2\2\289\3\2\2\29\67\3\2\2\29:\3\2\2\2"+
		":F\3\2\2\2;<\7\13\2\2<>\7\f\2\2=?\7\t\2\2>=\3\2\2\2?@\3\2\2\2@>\3\2\2"+
		"\2@A\3\2\2\2AF\3\2\2\2BC\7\13\2\2CD\7\7\2\2DF\b\4\1\2E\62\3\2\2\2E\63"+
		"\3\2\2\2E\65\3\2\2\2E;\3\2\2\2EB\3\2\2\2F\7\3\2\2\2Gl\7\6\2\2HI\7\6\2"+
		"\2Il\7\7\2\2JK\7\6\2\2Kl\7\f\2\2LN\7\6\2\2MO\7\t\2\2NM\3\2\2\2OP\3\2\2"+
		"\2PN\3\2\2\2PQ\3\2\2\2Ql\3\2\2\2RS\7\6\2\2SU\7\f\2\2TV\7\t\2\2UT\3\2\2"+
		"\2VW\3\2\2\2WU\3\2\2\2WX\3\2\2\2Xl\3\2\2\2YZ\7\6\2\2Z[\7\7\2\2[l\7\f\2"+
		"\2\\]\7\6\2\2]_\7\7\2\2^`\7\t\2\2_^\3\2\2\2`a\3\2\2\2a_\3\2\2\2ab\3\2"+
		"\2\2bl\3\2\2\2cd\7\6\2\2de\7\7\2\2eg\7\f\2\2fh\7\t\2\2gf\3\2\2\2hi\3\2"+
		"\2\2ig\3\2\2\2ij\3\2\2\2jl\3\2\2\2kG\3\2\2\2kH\3\2\2\2kJ\3\2\2\2kL\3\2"+
		"\2\2kR\3\2\2\2kY\3\2\2\2k\\\3\2\2\2kc\3\2\2\2l\t\3\2\2\2mn\7\b\2\2nr\7"+
		"\f\2\2op\7\b\2\2pr\b\6\1\2qm\3\2\2\2qo\3\2\2\2r\13\3\2\2\2sv\5\b\5\2t"+
		"v\5\n\6\2us\3\2\2\2ut\3\2\2\2v\r\3\2\2\2w}\5\f\7\2x|\5\22\n\2y|\5\24\13"+
		"\2z|\5\20\t\2{x\3\2\2\2{y\3\2\2\2{z\3\2\2\2|\177\3\2\2\2}{\3\2\2\2}~\3"+
		"\2\2\2~\u008a\3\2\2\2\177}\3\2\2\2\u0080\u0086\7\n\2\2\u0081\u0085\5\22"+
		"\n\2\u0082\u0085\5\24\13\2\u0083\u0085\5\20\t\2\u0084\u0081\3\2\2\2\u0084"+
		"\u0082\3\2\2\2\u0084\u0083\3\2\2\2\u0085\u0088\3\2\2\2\u0086\u0084\3\2"+
		"\2\2\u0086\u0087\3\2\2\2\u0087\u0089\3\2\2\2\u0088\u0086\3\2\2\2\u0089"+
		"\u008b\5\f\7\2\u008a\u0080\3\2\2\2\u008b\u008c\3\2\2\2\u008c\u008a\3\2"+
		"\2\2\u008c\u008d\3\2\2\2\u008d\17\3\2\2\2\u008e\u008f\7\5\2\2\u008f\21"+
		"\3\2\2\2\u0090\u0091\7\3\2\2\u0091\u0095\7\f\2\2\u0092\u0093\7\3\2\2\u0093"+
		"\u0095\b\n\1\2\u0094\u0090\3\2\2\2\u0094\u0092\3\2\2\2\u0095\23\3\2\2"+
		"\2\u0096\u0097\7\4\2\2\u0097\u00a2\7\f\2\2\u0098\u0099\7\4\2\2\u0099\u009b"+
		"\7\f\2\2\u009a\u009c\7\t\2\2\u009b\u009a\3\2\2\2\u009c\u009d\3\2\2\2\u009d"+
		"\u009b\3\2\2\2\u009d\u009e\3\2\2\2\u009e\u00a2\3\2\2\2\u009f\u00a0\7\4"+
		"\2\2\u00a0\u00a2\b\13\1\2\u00a1\u0096\3\2\2\2\u00a1\u0098\3\2\2\2\u00a1"+
		"\u009f\3\2\2\2\u00a2\25\3\2\2\2\u00a3\u00a4\7\r\2\2\u00a4\27\3\2\2\2\u00a5"+
		"\u00a6\7\16\2\2\u00a6\31\3\2\2\2\u00a7\u00a8\7\17\2\2\u00a8\33\3\2\2\2"+
		"\27\37+-9@EPWaikqu{}\u0084\u0086\u008c\u0094\u009d\u00a1";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}