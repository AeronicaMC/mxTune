// Generated from MML.g4 by ANTLR 4.5.2

package net.aeronica.libs.mml.core;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class MMLLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.5.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		CMD=1, LEN=2, OCTAVE=3, NOTE=4, ACC=5, MIDI=6, DOT=7, TIE=8, REST=9, INT=10, 
		BEGIN=11, PART=12, END=13, WS=14, JUNK=15;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"CMD", "LEN", "OCTAVE", "NOTE", "ACC", "MIDI", "DOT", "TIE", "REST", "INT", 
		"BEGIN", "PART", "END", "WS", "JUNK"
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


	public MMLLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "MML.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\21L\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\3\2\3\2\3\3\3\3\3\4"+
		"\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\13\6\13\65\n\13"+
		"\r\13\16\13\66\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\16\3\16\3\17\6\17C\n\17\r"+
		"\17\16\17D\3\17\3\17\3\20\3\20\3\20\3\20\2\2\21\3\3\5\4\7\5\t\6\13\7\r"+
		"\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21\3\2\f\n\2KKQQVVX"+
		"Xkkqqvvxx\4\2NNnn\4\2>>@@\4\2CIci\5\2%%--//\4\2PPpp\4\2TTtt\3\2\62;\5"+
		"\2\13\f\17\17\"\"\3\2\2\u0080M\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t"+
		"\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2"+
		"\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2"+
		"\37\3\2\2\2\3!\3\2\2\2\5#\3\2\2\2\7%\3\2\2\2\t\'\3\2\2\2\13)\3\2\2\2\r"+
		"+\3\2\2\2\17-\3\2\2\2\21/\3\2\2\2\23\61\3\2\2\2\25\64\3\2\2\2\278\3\2"+
		"\2\2\31=\3\2\2\2\33?\3\2\2\2\35B\3\2\2\2\37H\3\2\2\2!\"\t\2\2\2\"\4\3"+
		"\2\2\2#$\t\3\2\2$\6\3\2\2\2%&\t\4\2\2&\b\3\2\2\2\'(\t\5\2\2(\n\3\2\2\2"+
		")*\t\6\2\2*\f\3\2\2\2+,\t\7\2\2,\16\3\2\2\2-.\7\60\2\2.\20\3\2\2\2/\60"+
		"\7(\2\2\60\22\3\2\2\2\61\62\t\b\2\2\62\24\3\2\2\2\63\65\t\t\2\2\64\63"+
		"\3\2\2\2\65\66\3\2\2\2\66\64\3\2\2\2\66\67\3\2\2\2\67\26\3\2\2\289\7O"+
		"\2\29:\7O\2\2:;\7N\2\2;<\7B\2\2<\30\3\2\2\2=>\7.\2\2>\32\3\2\2\2?@\7="+
		"\2\2@\34\3\2\2\2AC\t\n\2\2BA\3\2\2\2CD\3\2\2\2DB\3\2\2\2DE\3\2\2\2EF\3"+
		"\2\2\2FG\b\17\2\2G\36\3\2\2\2HI\t\13\2\2IJ\3\2\2\2JK\b\20\2\2K \3\2\2"+
		"\2\5\2\66D\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
