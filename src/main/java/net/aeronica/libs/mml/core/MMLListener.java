// Generated from MML.g4 by ANTLR 4.5.2

package net.aeronica.libs.mml.core;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link MMLParser}.
 */
public interface MMLListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link MMLParser#band}.
	 * @param ctx the parse tree
	 */
	void enterBand(MMLParser.BandContext ctx);
	/**
	 * Exit a parse tree produced by {@link MMLParser#band}.
	 * @param ctx the parse tree
	 */
	void exitBand(MMLParser.BandContext ctx);
	/**
	 * Enter a parse tree produced by {@link MMLParser#inst}.
	 * @param ctx the parse tree
	 */
	void enterInst(MMLParser.InstContext ctx);
	/**
	 * Exit a parse tree produced by {@link MMLParser#inst}.
	 * @param ctx the parse tree
	 */
	void exitInst(MMLParser.InstContext ctx);
	/**
	 * Enter a parse tree produced by {@link MMLParser#test}.
	 * @param ctx the parse tree
	 */
	void enterTest(MMLParser.TestContext ctx);
	/**
	 * Exit a parse tree produced by {@link MMLParser#test}.
	 * @param ctx the parse tree
	 */
	void exitTest(MMLParser.TestContext ctx);
	/**
	 * Enter a parse tree produced by {@link MMLParser#mono}.
	 * @param ctx the parse tree
	 */
	void enterMono(MMLParser.MonoContext ctx);
	/**
	 * Exit a parse tree produced by {@link MMLParser#mono}.
	 * @param ctx the parse tree
	 */
	void exitMono(MMLParser.MonoContext ctx);
	/**
	 * Enter a parse tree produced by {@link MMLParser#rest}.
	 * @param ctx the parse tree
	 */
	void enterRest(MMLParser.RestContext ctx);
	/**
	 * Exit a parse tree produced by {@link MMLParser#rest}.
	 * @param ctx the parse tree
	 */
	void exitRest(MMLParser.RestContext ctx);
	/**
	 * Enter a parse tree produced by {@link MMLParser#note}.
	 * @param ctx the parse tree
	 */
	void enterNote(MMLParser.NoteContext ctx);
	/**
	 * Exit a parse tree produced by {@link MMLParser#note}.
	 * @param ctx the parse tree
	 */
	void exitNote(MMLParser.NoteContext ctx);
	/**
	 * Enter a parse tree produced by {@link MMLParser#midi}.
	 * @param ctx the parse tree
	 */
	void enterMidi(MMLParser.MidiContext ctx);
	/**
	 * Exit a parse tree produced by {@link MMLParser#midi}.
	 * @param ctx the parse tree
	 */
	void exitMidi(MMLParser.MidiContext ctx);
	/**
	 * Enter a parse tree produced by {@link MMLParser#anote}.
	 * @param ctx the parse tree
	 */
	void enterAnote(MMLParser.AnoteContext ctx);
	/**
	 * Exit a parse tree produced by {@link MMLParser#anote}.
	 * @param ctx the parse tree
	 */
	void exitAnote(MMLParser.AnoteContext ctx);
	/**
	 * Enter a parse tree produced by {@link MMLParser#tied}.
	 * @param ctx the parse tree
	 */
	void enterTied(MMLParser.TiedContext ctx);
	/**
	 * Exit a parse tree produced by {@link MMLParser#tied}.
	 * @param ctx the parse tree
	 */
	void exitTied(MMLParser.TiedContext ctx);
	/**
	 * Enter a parse tree produced by {@link MMLParser#octave}.
	 * @param ctx the parse tree
	 */
	void enterOctave(MMLParser.OctaveContext ctx);
	/**
	 * Exit a parse tree produced by {@link MMLParser#octave}.
	 * @param ctx the parse tree
	 */
	void exitOctave(MMLParser.OctaveContext ctx);
	/**
	 * Enter a parse tree produced by {@link MMLParser#cmd}.
	 * @param ctx the parse tree
	 */
	void enterCmd(MMLParser.CmdContext ctx);
	/**
	 * Exit a parse tree produced by {@link MMLParser#cmd}.
	 * @param ctx the parse tree
	 */
	void exitCmd(MMLParser.CmdContext ctx);
	/**
	 * Enter a parse tree produced by {@link MMLParser#len}.
	 * @param ctx the parse tree
	 */
	void enterLen(MMLParser.LenContext ctx);
	/**
	 * Exit a parse tree produced by {@link MMLParser#len}.
	 * @param ctx the parse tree
	 */
	void exitLen(MMLParser.LenContext ctx);
	/**
	 * Enter a parse tree produced by {@link MMLParser#begin}.
	 * @param ctx the parse tree
	 */
	void enterBegin(MMLParser.BeginContext ctx);
	/**
	 * Exit a parse tree produced by {@link MMLParser#begin}.
	 * @param ctx the parse tree
	 */
	void exitBegin(MMLParser.BeginContext ctx);
	/**
	 * Enter a parse tree produced by {@link MMLParser#part}.
	 * @param ctx the parse tree
	 */
	void enterPart(MMLParser.PartContext ctx);
	/**
	 * Exit a parse tree produced by {@link MMLParser#part}.
	 * @param ctx the parse tree
	 */
	void exitPart(MMLParser.PartContext ctx);
	/**
	 * Enter a parse tree produced by {@link MMLParser#end}.
	 * @param ctx the parse tree
	 */
	void enterEnd(MMLParser.EndContext ctx);
	/**
	 * Exit a parse tree produced by {@link MMLParser#end}.
	 * @param ctx the parse tree
	 */
	void exitEnd(MMLParser.EndContext ctx);
}
