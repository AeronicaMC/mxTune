/** This is one possible grammar for Mabinogi's MML implementation.
* I'm unsure if the Listener or Visitor pattern will be more useful
* at this time. I'll try each and see how those patterns fit my needs.
*
* antlr4 -encoding US-ASCII MML.g4
*/
grammar MML;
@header {
package net.aeronica.libs.mml.core;
}


// Parser rules
band	: (inst)+ EOF;          // support appended MML files

inst	: begin                 // treat each as a separate instrument
        ( rest
        | anote
		| tied
		| octave
		| part
		| cmd
		| len
		)* end
		;

test    : begin* (mono|part)+ end* EOF; // parse testing: ignore MML@ and ; tokens

mono    : 
        ( rest
        | anote
        | tied
        | octave
        | cmd
        | len
        | begin                 // for ArcheAge we'll allow silly things
        | end                   // for ArcheAge we'll allow silly things
        )+
        ;

rest    : REST                  // Possible rest formats
        | REST INT
        | REST DOT+
        | REST INT DOT+
        | REST ACC              // for ArcheAge we'll allow silly things
        | REST ACC INT          // for ArcheAge we'll allow silly things
        | REST ACC DOT+         // for ArcheAge we'll allow silly things
        | REST ACC INT DOT+     // for ArcheAge we'll allow silly things
        ;

note	: NOTE					// possible note formats
		| NOTE ACC
		| NOTE INT
		| NOTE DOT+
		| NOTE INT DOT+
		| NOTE ACC INT
		| NOTE ACC DOT+
		| NOTE ACC INT DOT+
		;	

midi	: MIDI INT  			// match MIDI note
        | MIDI {notifyErrorListeners("midi note missing value");}
        ;

anote   : (note|midi) ;

tied    : anote ((cmd|len|octave|rest)* TIE+ (cmd|len|octave|rest)* anote)+     // match tied note
        ;
        
octave  : OCTAVE ;
         
cmd		: CMD INT
        | CMD {notifyErrorListeners("[Octave|Perform|Sustain|Tempo|Volume] missing value");}
		;

len     : LEN INT
        | LEN INT DOT+
        | LEN {notifyErrorListeners("length missing value");}
		;

begin   : BEGIN ;
part    : PART ;
end     : END ;

// Lexer rules		
CMD		: [iopstvIOPSTV] ;          // MML commands Instrument, Octave, Perform, Sustain, Tempo, Volume
LEN     : [lL] ;                // MML Length command
OCTAVE	: [<>] ;				// Octave down/up
NOTE	: [a-gA-G] ;			// Notes
ACC		: [+#-] ; 				// Accidental
MIDI	: [nN] ;				// MIDI note
DOT		: '.' ;					// dotted
TIE		: '&' ;					// Tie
REST	: [rR] ;				// Rests
INT		: [0-9]+ ;				// match integers
BEGIN	: 'MML@' ;				// MML File Begin
PART	: ',' ;					// Part separator
END		: ';' ;					// MML File End
WS		: [ \t\r\n]+ -> skip ;	// toss out whitespace
JUNK    : [\u0000-~] -> skip ;  // anything leftover