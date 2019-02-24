package net.aeronica.libs.mml.core;


import java.util.HashMap;
import java.util.Map;

import static net.aeronica.libs.mml.core.MMLUtil.MML_LOGGER;

public enum TestData {
    MML0(0, "Mario", "0=MML@i1v13r4>e32r16.d+32r16.d16c16r8.<<g8r4r16>>e16r8.d16c32r16.e16r4.c16.d32e8e8d8c8e8f8g8a8r8c8c8<a8g8>c16c16r4<f8g8a8b16r8.>>g32r16.g32r8.r32<e16r8.d16c32r16.e16r4.c16.d32e8e8d8c8a8g8>c8<e16d32r16.c8.c8d8e8c16c16r4f8e8c8d8r16>g32r8r32<g32r4r16.a8b8>c8<b32r8r32g8r8.f8r8g16f16r8e8f8f+8g8r8a8b8>c8<b32r8r32g8r8.>d+8d8r8c8r2<e16r8.d16c32r16.e16r4.c16.d32e8e8d8c8e8f8g8a8r8c8c8<a8g8>c16c16r4<f8g8a8b16r8.>>g32r16.g32r8.r32<e16r8.d16c32r16.e16r4.c16.d32e8e8d8c8a8g8>c8<e16d32r16.c8.c8d8e8c16c16r4f8e8c8d8r16>g32r8r32<g32r4r16.a8b8>c8<b32r8r32g8r8.f8r8g16f16r8e8f8f+8g8r8a8b8>c8<b32r8r32g8r8.>d+8d8r8c8,v10r1r4.e8r8e8r8e8r8e8r8e8r8e8r8e8r8e8r8e8r8e8r8e8r8e8d8e8f8f+8r2r8e8r8e8r8e8r8e8r8e8r8e8r8e8r8e8r8e8r8e8r8e8r8e8d8e8f8f+8r2r8c8r8c8r8c8r8c8r8a8r8a8<e8f8f+8g8f8>c8<f8>c8<e8>c8<e8>c8<g+8>d+8<g+8>d+8g32r32g32r32f32r16.e32r16.d32r8.r32e8r8e8r8e8r8e8r8e8r8e8r8e8r8e8r8e8r8e8r8e8r8e8d8e8f8f+8r2r8e8r8e8r8e8r8e8r8e8r8e8r8e8r8e8r8e8r8e8r8e8r8e8d8e8f8f+8r2r8c8r8c8r8c8r8c8r8a8r8a8<e8f8f+8g8f8>c8<f8>c8<e8>c8<e8>c8<g+8>d+8<g+8>d+8g32r32g32r32f32r16.e32r16.d32r16.c2.,v10r1o2g8.&g32r32>c8r16.<g32>c8r8c8r8c8r8c8r8c8r8c8r8a8r8f8r8f8r8e8r8e8r8d8e8f8f+8r2c8r16.<g32>c8r8c8r8c8r8c8r8c8r8c8r8a8r8f8r8f8r8e8r8e8r8d8e8f8f+8r2f8r8f8r8e8r8e8r8d8r8d8r8e8f8f+8g8r1r1c8r16.<g32>c8r8c8r8c8r8c8r8c8r8c8r8a8r8f8r8f8r8e8r8e8r8d8e8f8f+8r2c8r16.<g32>c8r8c8r8c8r8c8r8c8r8c8r8a8r8f8r8f8r8e8r8e8r8d8e8f8f+8r2f8r8f8r8e8r8e8r8d8r8d8r8e8f8f+8g8;"),
	MML1(1, "Fruits Basket duet", "0=MML@t69r1v13>b2l8.agf+8e1b4.a16g16agf+e1l16r<bbb>cdd8.cc8c-c8r8<aaab>cd8.c<b8ab8r4b>ef+g8.f+e8a8.g4gf+ed&d2r4c-cdd8.cc8c-c8r8.<aab>cd8.c<b8ab8r4b>ef+g8.f+e8.a8g4&gf+ef+&f+4rbbbbggeecc<ba>cf+araaaaf+f+dd<aaaagb>gr8ggg8f+f+8.eed+8b+b8.a8a8gg8f+e4.rbbbbggeec<ba8>cf+araaaaf+f+d8c-da8gf+g&g4r8>d8c8<ba8g8.g8f+g&g1r4dd+ff8.d+d+8dd+8r4cdd+f8.d+d8cd8r4dgaa+8.ag8b+8.a+4&a+agf&f2r4dd+ff8.d+d+8dd+8r4cdd+f8.d+d8cd8r4dgaa+8.ag8b+8.a1g1r2.rbbbbggeecc<ba>cf+araaaaf+f+dd<aaaagb>gr8ggg8f+f+8.eed+8b+b8.a8a8gg8f+e4.rbbbbggeec<ba8>cf+araaaaf+f+d8c-da8gf+g&g4r8>d8c8<ba8g8.g8f+g&g1r4c-cdd8.cc8c-c8r4<ab>cd8.c<b8ab8r4b>ef+g8.f+e8a8.g4&gf+ed&d2r4c-cdd8.cc8c-c8r4<ab>cd8.c<b8ab8r4b>ef+g8.f+e8a8.g2f+2g1&g1r8br8.f+r8.gr8.dr8.er8.<ar8.br8.>dr8.br8.f+r8.gr8.dr8.er8.<ar8.br8.gr8.>br8.f+r8.gr8.dr8.er8.<ar8.br8.>d,v11r1g2l2.f+rg2f+l1rrrrrl16>degg8.ee8de8.l1.rrrrrrrrrl2rr.r8.v10fg4.l16agf4.>c4.cdd+8d4c4<a+2l1b+grl2.b>e4d<g4a2f+2g2g+2b>e4da4g1gr8l16c-cd1l8d<ab>cc4<bag2gef+gb4.l16b+ba4.b>cd1l8d<ab>cc4af+g2.rl16f+ed2.r<c<bag1.&g4&gv8c<bag2,l64g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g;|"+
	        "1=MML@I5t69l64g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&gr2v14l8>ec-ec-c<ab+an63bn63bn63bn63b>ec-ec-c<ab+an63bn63b>d+2<gc-gc-gcgcf+<an54a>acacgc-gc-a<a>a<a>e<a>e<an54an54a>gc-gc-gcgcf+<an54a>acacgc-gc-a<a>a<a>e<a>e<an54an54a>g<a>g<an54an54an54an54a>gc-gc-acacacacgc-gc-g+c-g+c-g<a>g<an54an54an54an54a>gc-gc-g<a>g<a>g<a>g<a>gc-gc-gc-gc-a+d>d<fa+d+a+d+acacb+cb+ca+da+dgcgca+ca+cacacfn46fn46a+d+a+d+acacb+cb+ca+da+dgcgcacacadadb+db+dbdbd>e<e>e<e>e<e>e<eg<a>g<an54an54an54an54a>gc-gc-acacacacgc-gc-g+c-g+c-g<a>g<an54an54an54an54a>gc-gc-g<a>g<a>g<a>g<a>gc-gc-gc-gc-gc-gc-gcgcf+<an54a>acacgc-gc-a<a>a<a>e<a>e<an54an54a>gc-gc-gcgcf+<an54a>acacgc-gc-g<a>g<a>g<a>g<an54an54a>gc-gc-gc-gc-gc-gc-gc-gc-gc-gc-gc-gc-v13gc-v12gc-v11gc-v10gc-v9gc-gc-gc-gc-gc-gc-gc-gc-gc-gc-gc-gc-gc-gc-gc-g<b1,v14r1>gg8.&g32<a64>d64f+f+g+g+g+g+8.&g+32c-64e64ggf+f+g+g+8.&g32c-64e64g+2<ddeeddd+d+eec+c+ccccddeeddd+d+eec+c+ccccccccddeef+f+d+d+eeeeccccddeeccccddddfa+ggfff+f+ggeeffffddggfff+f+ggeefff+f+ggggaag+g+ccccddeef+f+d+d+eeeeccccddeeccccddddddeeddd+d+eec+c+ccccddeeddd+d+eec+c+ddddddddddddddddv13dv12dv11dv10dv9dddddddddddddddd1,v14r1l2cde1<cde1gcdd+ec+dd<g>cdd+ec+dd<a>dc-ef+c-ee<a>dc-e<a>d<g4.d8g4.g16a16a+>d+ff+gcffn34d+ff+gcfd<gg>e<ea>dc-ef+c-ee<a>dc-e<a>d<g4.d8g>gcdd+ec+dd<g>cdd+ec+ddl4.<gd8gd8gd8gd8l2ggggv9gggg;|"),
	MML2(2, "Bach", "0=MML@i2122t120o5l8d2e-de-cd4r<gaa>dc<b-g>dd<b-g>e-2d2c4.<b-16a16b->cd4g4.c+d2c4d4rc+d4rdcccd16c16<b-b-b->c16<b-16a4.>d4.c4.<ab-4a2b-4rab-4r>c<aa>d4<gg>c4<f+f+g4.a16g16f.g16gab->cd4rdc+4d2c4d4rc+d4rdcccd16c16<b-b-b->c16<b-16aaab-16a16ggga16g16fa>d2c+4d4f4e-2d2c2<b-2a4b-2rab-4r>de-e-e-f16e-16ddde-16d16cccd16c16<b->cde-<a4.>d4.c4.<b-16a16b-4a2b-4rab-4r>c<aab-4gga4f+f+g2f+4g2a-ga-fg4rcddgfe-ca-2g2f4.de-4d2e-fg4f+4a4>d2e-de-cd4r<gaa>dc<b-g>dd<b-g>e-2d2c2<b-4a2.r>dcccd16c16<b-ag2f+4g2.rf+g1,l8o4r1r1r1r1r1r1r1r1d2e-de-cd4r<gaa>dc<b-g>dd<b-g>e-2g2c2<b-a16g16a2g2b-ab-ga4rdeeagfdaafdb-2a2g2f4e2d4rdge<aa>d4>rdcccd16c16<b-b-b->c16<b-16aaab-16a16ggga16g16f4<b->de-cf<fb-4>b-2a2g2f+4g4r4d2e-de-cd4r<gaa>dc<b-g>dd<b-g>e-2d2c2<b-4a2g4r4r2>e-fga-<b2>c4r>c<b-b-b->c16<b16a-a-a-b-16a16g4rcfdg<g>c4r>c4ccd16c16<b-ab-g>c4r<fb-ab-4>c4d2rdd4r<gfffg16f16e-e-e-f16e-16d4r>dcccd16c16<b-b-b->c16<b-16a4>d4<gab-a16g16a4d4e-e-e-f16e-16d2<g1,o4l2r1r1r1r1l8g2b-ab-ga4rdeeagfdaafdb-2a2g2f+4g2f+4g2f4g4rf+g4rgfffg16f16e-e-e-f16e-16ddd4c2<b->cd4g4rb-a4a4g4g4a4raa4g2a4f4g4e4f4d4c4d4rfe4e4a4b-2a2g2f2e-4.f16e-16d4cd16e-16dcd4d4c2<b-2a2g4>g2f+4g4g4f4g4dc4d4rf+4g4rgfffg16f16e-e-e-f16e-16ddde-16d16cccd16c16d<fe-ef4rb->e-4r<a-4.g4g4r>e-ddde-16d16cccd16c16b4c2b4cde-4d4f+4a4gb->c<b->c<aff+g4e-4d2rab-grb-aaab-16a16ggga16g16f+4g2f4d4g2f+4d4d2rc<b4>c4.d16c16<b-ab1<;"),
	MML3(3, "Cendrillan", "0=MML@I21t70l64g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&g&gr2t145v15>e8.d32d+32l16erdrf4e4dedrarerd4.<abb+8b>dcr<brarerarbr>e8l32dedl16c.r<arbr>dre4e8.d32d+32erdrf4e4dedrarerd4.<abb+8b>dcr<brarerarbr>e8l32dedl16c.rdrc4<b4e2l32edc<bagfedc<bagfedl4>a>e<a>el8.cde8d4c4cde8d4f32f+32gf4.g16f16e4r8e8dde8d+32e32f8ed8dcl8c-c4<b4a4ra16b16>cccdc-2e4.raeab+b4.rb+g>ced4<ab>c4.ec-c<bge2v13<a32a+32b16ag+ev15>aeab+b4rbb+g>ced4<ab>c4.d32d+32e16de<bga2v12l16ea>ceav13>ceav15l8<<ede<ar4.abb+ba4.r4>ede<a2a>dcgc+32d32e4.r.<a4>d<b2bbg+>ddc4.ce4.d4gffe2rfefe4r4v12l16<eg+b>eg+b>eg+v15l4<<a>e<a>el8.cde8d4c4cdl8ed4g4agab16>c4.r16c<b.b.bg+4b4a.b.>cl4c<ba.l16abb+4b4b2g+4.g+bl8aeab+bgb>dc<g>cel16f+d<af+d<af+df4.g4.a1,r1v15l2o2ag+gf+feag+a4l8b+ag+4>d<g+g4>e<gf+4>d<f+f4afe4aef4afe4a4b2b2<aa>e<aaa>e<aaa>e<aaa>e<aaa>e<abb>dc-ccecccec<bb>d<bg+g+bg+a.a.ag.g.g>ffafffafe.e.el16>g+e<bg+e<bg+el2o3cdef+4d4fgcc-l4.cc8dd8ee8l4f+df2ge<aaal8agf4ff2fe4ea2af4ff2fe4ec4c4cffafggbgggbg+aaggffafffafeeeeeeeeeeeee2<aa>e<aaa>e<aaa>e<aaa>e<aaa>e<abb>dc-ccecc.e.c<bb>e<bbb>ec-l4b+bagl8ffcfffcfe.e.e<b.b.baab+b4agb4>dc-c4<bag>ff4ee4a1,r1v15l2<c<bb>aagcdc<bb>aagcdg+el4eaeae2c-de2c-bb.>c8c.c8a2<b2feedl2>cag+<befgaadedefgaadc+1>a1l4.ged4a1gag4<d2f2g+ae4f2a2g+2l8r>ag+ag+1l4eaeae2c-gl8a.b.b+b4b4b+b>c.e4.r16ed2<b2l4edcc-e1l2dc-cc-gaa.c;"),
	MML4(4, "*Aishite", "0=MML@T100l64f&f&f&f&f&f&f&f&f&f&f&f&f&f&f&f&f&f&f&f&f&f&f&f&f&f&f&f&f&f&f&fl4r.r16t164d+32e32v15f2.ff+fd+c+8f2&f8v11fv15ff+fd+c+8c8rc8<a+.>c16.d32d+.c<a+8.l32&a+bb+8a+4.v11g+a+v13l16.g+v15g+2.&g+t160v10l4>f+t156fc+t164v15f2.ff+fd+c+8f2.&f8ff+fd+c+8c.c8<a+.>c16.v12e32v15f.d+c+c8c+2&c+8v10c+1cv12c+v15g+c+8c+8v10c+r8v15c+8cc+8c+2c+16.f32g+c+8c+.c8c.c+8c+2c+16.f32g+c+8c+.g+a+g+f+g+8g+1r8c+d+ff+8g+2.&g+8c+c+d+ff+8g+2.r8c+c+d+fc+8.v12l32a+>cv15c+1c2<g+4.v12f16.g+v13a+b+v15a+8.g+1.l4g+f+fd+c+8d+2&d+8rn44fd+c+c8c+2.&c+8n46c+n46c+ff+2f.g+2.r8c+c+d+ff+8g+2.&g+8c+c+d+ff+8g+2.&g+8c+c+d+fc+>c+2v5l24c+cv6c+cv10c+cv12c+cc+cc+dv15l4d+c+8c.c+8c1.&c.<g+f+g+a+>c+d+2c+2<g+2fl8g+>c+2.&c+<c+c+f4d+d+2n46t158c+4ct152<a+4.>cc+1.&c+,r1v11g+n44c+>c+1r<<f+>c+2>c+1r<c2d+g+1rc+rc+f2r2.g+n44c+>c+1l16rfc+<a+l4c+<fa+>c+2.<a+2n51d+g+>g+2<cg+2fg+r>f+2fc+n44c+<fra+>f<fb+2a+c+fa+2r>d+cc+<f>fn46fn44c2c+<c+a+2g+2a+2>fn44c+>c+2.<<g+2>b+n44c+f2rg+2fn46c+2g+2f2d+<fg+>cd+2c2c+<d+f+2b+d+>d+2c<fg+2gc+g+2f+d+f+f+2.f+2n49d+f+2f2>d+2fn44c+>f2.c+2c<<g+>fg+2.c+g+f<ga+gf+d+g+d+b+g+>d+2c+<fl2>fc+n46d+d+c4<f4g+g4e4>en46c+fn44g+1.,l1rv10<c+&c+<f+&f+g+&g+>c+&c+c+&c+<f+&f+g+&g+>c+&c+v12<a+g+f+.g+2a+g+f+g+>c+.&c+4<g+4>c+&c+<a+&a+f&fd+g+2.d+4fed+l4&d+n39d+a+g+1.&g+a+8>c8c+1&c+n32d+2l1c+&c+<gf+fa+d+f+fe2.c-8c+8d+2.a+4g+c+&c+;"),
	MML5(5, "*Annpan duet", "0=MML@i0t124l32c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&cv11>c8l16dd+l8dcc-cd4<gl16b>dl12fd+dl4.c<a+8g+l8gf+d+dcd2&dc-cd>d+d+crl16<g+g+g+a+l8b+g+>d.d+16dc<a+g+g4>d+d+crl16<g+g+g+a+b+8g+8>d8.d+d8c-8c2<g+8b+rg8b+rfd+fgd+8c8d8<gg>d8f8g8dd<gb>dgg+8b+rg8b+rfd+fgd+8c8dcdd+dcc-dc<bg+bgb>dgv6>>cdd+gcdd+gfgg+b+fgg+b+<g>cd+g<g>cd+gc-cdgc-cdgcdd+gcdd+gfgg+b+fgg+b+<gb>dg<gb>dgcdd+gcdd+gl8<<brbrbrbr>crcrcrcrererererf+rf+rv9<gl16abv11b8>cdv12d8d+ff8gg+v14l12b>cdd+dd+v9l16cdd+fgfd+dcdd+fgfd+dd+fgg+bg+gfd+fd+dcdcc-cdd+fgfd+dcdd+fgg+bb+bg+gfd+dcc-c<bg+gg+b>cdv12l8<d+rcr<g+rg+rb+rg+rgrbr>d+rcr<g+rg+r>dr<gr>crcrfl16gg+a+g+gfl8d+g>cd+dc<ba16b16>crc,r1v12o2ccgg<gb>cefff+f+<ggb>dccff<a+a+>d+gccff<gb>ccfd+dc<bg+gb>fd+dc<bg+gb>ccfd+dc<gb>ccfd+d<g+g>cddddeeeeggggddgg<ggggl8o5d+d+crl16<g+g+g+a+l8b+g+>d.d+16dc<a+g+g4>d+d+crl16<g+g+g+a+l8b+g+>d.d+16dc-c2cr<grd+rd+rg+rfrd+rgrb+rgrd+rd+rbrdrgrd+rfl16gg+a+g+gfl8d+g>cd+dc<ba16b16>crc,r1l1.rrrrrrrrrrrrrv9d+8l16fv11gg8abv12b8>cdd8d+fv14l12gabb+b>cl4o2ccff<a+a+>d+gccff<gb>ccccff<a+a+>d+gccff<gb>ccfde-c<gg>cc8;|"+
	        "1=MML@i5t124l32c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&cl2>cdc-ccd<bl8&bb>cdv13>d+d+crl16<g+g+g+a+l8b+g+>d.d+16dc<a+g+g4>d+d+crl16<g+g+g+a+b+8g+8>d8.d+d8c-8c2r1r1r1r1<cdd+fg8b+8g+8gfg4f8d+dd+8c8dddd+d4cdd+fg8b+8g+8gfg4f8d+dd+8c8d8.gc4dd+drdd+drd8g8d8r8eferefere8a8e8r8gagrgagrl8gag>ed<gb.g+16g2r1>d+d+crl16<g+g+g+a+l8b+g+>d.d+16dc<a+g+g4>d+d+crl16<g+g+g+a+l8b+g+>d.d+16dc-c2d+rcrl16<g+g+g+a+l8b+g+>d.d+16dc<a+g+g4>d+rcrl16<g+g+g+a+l8b+g+>d.d+16dc-c2v8<g+2d+2g4b4>crv13c,r1l2d+fdd+ff+g1>cd<a+g4b4>cdc-l16cdd+fgfd+dl4fd+dc<b2g2>fd+dc<b2gbgg>cc<g+gf+gggb+gg+gfd+f1l16>cdcrcdcrc8e8c8r8efereferl8efeb+beg.f16l2dr1cd<a+g4b4>cd<bl16b>cdd+fgg+bb+8r8g8r8d+d+d+fl8gd+a+.b+16a+g+gfd+4b+rgrl16d+d+d+fl8gd+b.b+16bgl2gd<gbl8crv13g,l1rrrrrl2gg+fd+gg+fd+l4>dc<bg+g2d2>dc<bg+g2dgd+d+gd+fd+dd+d+d+gd+fd+dcl1rgb+l2f+gr1gg+fd+gg+fl16d+fgg+b>cdd+g8r8d+8r8cccdl8d+cf.g16ffd+dc-4grd+rl16cccdl8d+cg.g+16gfl2d+fcdl8crv13d+;|"),
    MML6(6, "*Butterfly", "0=MML@i5t92l64<b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&br1.v0<<c4.l16&ct100c2.&c8.t89c2&c8t92c4.&ct100c2&cl1rrrrr4c8t80c8rr4.o4g+8t100v9g+8l16brv7a+64v9b.&b64l8bb4g+v7>c64v9c+.&c+32.<b4r4.g+g+4b16r16bbb.&b32v7l64a+av9g+8>c+4d+4v5d32c+cr8.v9<g+8f+8v7gv9g+16.&g+l8f+e4r4eef+32g.&g32f+ea4gf+e4.v5l64d+dv3c+cr1.l8r.v10g+b16r16bbb.&b32v7a+64a64v10g+>c32c+.&c+32<b4r4.g+g+4b16r16bb4g+g+>c+4d+4v6l64dc+cv3<ba+r8r32.v10g+8f+8v7gv10g+16.&g+l8f+e4r4eeg4f+ea4gf+v9e4&e16.r1r4.r32v10g+v11g+v12g+>d+32e.&e32d+4v9l64cv12c+16.&c+<b4v9f+gv12g+4&g+16.r4.v10l8g+f+g+f+eec+ef+g+a16.&a64g+64ag+4r4g+v11g+v12g+>d+32e.&e32d+4v9l64cv12c+16.&c+<b4v9f+gv12g+4&g+16.r2v10l8g+g+ag+ab4abb+4b>cd2&dc-1r1v15e4.f+4.g+v12g32f+32r16v15f+4g+f+4ed+e4.v6d+32d64c+64r4.r16v14c+eg+f+4eeef+e32f+32e4&e16re4d+c+<b4g+g+>c+4r4c+ev12l16c+rv14e8v12c+rv14e8c+rc+8l32gg+8.&g+g+8f+4e8f+4v8fe64d+64r16v15e4.f+4.g+8v12gf+r16v15f+4l8g+f+4ed+e4.r2v14c+eg+f+f+f+v11b16r16v13d+d+e4.re4d+c+<b4b>f+e32v11f+32v12e4&e16r4e2&er4.d2&dr4.e,l1.rrrrrrrr2l8rr64v4g+r2rg+rv3>c+4<b4r2v4g+r2bv3g+4>c+4d+4rv4l16.<g+r32f+r32l8g+f+rv3er4.v4ergf+eragf+r4v3e4.l64d+dc+cr1l4r.r16v4g+r.b8g+8r8v3n61br2v4g+rl8brg+rv3>c+4d+4rv4l16.<g+r32f+r32l8g+f+rv3er4.v4e16.rr32gf+eragf+r.r32v3l4er1r1.rr32g+rv4l16.g+r32f+r32g+8f+8r8er32l8c+ef+r4a4v3g+.l32&g+r1.r8rg+4r2v4g+16.ra16.rl8g+arb16.r32abrb+16.r32b>cr2v3d4r2.c-2r1r1.r4e4r4.v5c+eg+rf+r4l16.er4.r32er4r32l8ed+c+r<brg+r1r1l4.rr16.r64v14ef+l8g+v10g32f+32r16v14f+4g+f+4ed+e4.,l1.rrrrrrrrrrrrrrrrrrv7l4ba+g+8f+d+.r1r1.rba+g+8f+d+.r1r1r1r1.rv14e.f+.g+8v8g32f+32r16v12f+g+8f+e8d+8e.r1r1r1r.r64v4>c+8rv5l16c+.r32ec+.r.ec+.r.e8r8c+4r8g+4f+.r32e8l4rf+r1r1v3e.rv4l8c+eg+r4f+16br.d+r4v3e16.r4r32v4ed+c+r4c-r4.v3er2.e2&er4.d2e;|"+
	                     "1=MML@i0t92l64g+&g+&g+&g+&g+&g+&g+&g+&g+&g+&g+&g+&g+&g+&g+&g+&g+&g+&g+&g+&g+&g+&g+&g+&g+&g+&g+&g+&g+&g+&g+&g+r1.v0o1c+4.l16&c+t100c+2.&c+8.t89c+2&c+8t92c+4.&c+t100c+2&c+r1l8rv7>>b2.&bra4a2&arb2.&brv3a4t80v2a2.r2.v0>gt100gv6<c-4g+2&g+rl4c-f+.v7n30r8v6f+>f+.<f+r8g+>f+.<dc+8c+2.&c+8v9cc.av8>f+8<e.e2.&e8rv9f+er8v7c-2g+.r8c-f+.l8b<er>d+4b2n27rl4d+b.r.e2.<e16f16r8>grf+rc-g+g+c-r8b8v5e.v8d+g+.g+>c+v11<<e16f+16r8v8>f+b2l8&brv7a4.ra4.rg+4.rv10g+g+g+g+4.g+4v9>c+4v11<<e16f+16rv7>f+4b2v10n32rv9a>c+4r<f+4r4gb+l4aa.f+8f+2n54r8v7f+v10f+v12f+f+r8c-f+>e.r8n42f+.r.<<g+>e.r8v13<e16f+32g32r8v10>f+n51f+f+8r8aree8rbr8v9er.ar8e.rf+8f+r2v12c-f+>e.r8n42f+.r.<<g+>e.r8v13<e16f+32g32r8v12>f+v10n51f+f+8r>c+.r2r8f+2r2<dg>df+<ea>el8rv7<b2.&b>>d4r<a2&arv6<b2.&b>>d4r<a2&ar<b2.&b>>d4r<a2&arv3<b4.t90v0b.t75b.t60bv3>>c+4.r64e2&et100,l1rrv5>ed4.c+2&c+8ev3dl4rv6<e2.rd8e2&e8rl2.er4v4d8v3e1rr8v6<el4re.g+.rb.b.>f+c+2<a+aa2r8.r32.>c.&c64<d.v8>d<b8b8v7>e2.r.r16r64<a2rr32.e2ere.g+.rg+2f+rg+2r2a2.rb+raaerbf+.g+g+8v8g+c+8<g+8>d+8e.brg+.l8f+f+g+r4v7b4.r2rb2v8e4c+n32d+l4e.brv7g+.v9f+8g+>e.d+2e2d.drc-2bv7c-v9d+v11f+v12br<ebn56bb2b+rd+.g+8g+rv10>c+r2.c+.r2<g+r2.ebr8ar8n30r2.r8v12ebn56bb2b+rd+.g+8g+rv10>c+2.r8<a2aa8r8b2g+r8c.e>ce8<d.f+>df+.v6e2.l8aa>dc+2&c+r4v5<e2.aa>dc+2&c+r4<e2.aa>dc+2&c+r4v4<e2.rv3a>ec+2&c+,l1.rrrrrrrrrrrrr2l8o2bb4.e1e2.&erg+2.&g+rl2.g+g4f+&f+8r8g4.f+2&f+8e1ed+4c+l8&c+rv11g+1v9a4.a4.c+d+e2.d+4c+2.&c+rv11g+2.&g+ra4.b4.b>c2d2&dl4<b.b2f+f+v12f+v14f+v15l8f+v13e1b2&bb>c4c+2.&c+rl4.<g+g+g+4a1e>c+v11c+4l1<av13b2.f+4eb2&b8b8b+4rrrrr8v10g2.r4a2.;|"+
                         "2=MML@i0t92l64b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&br1.v10>g+4&g+16.l32g+.a.t100g+8.f+8.&f+e8&ef+4.t89a2&a8t92g+4&g+16.g+.a.t100g+8&g+.r64f+8.&f+e8&ev7f+1v11<g+4.l16g+ag+8.f+8.e8f+4.a2&a.l64f+gg+4.l16g+av10l8.g+f+v9e8v7l4.f+t80v6a1&a8rv0a8t100a8r4v6b2&b8rb2&b8bb2r8>c+d+2&d+8r<e2&e8r32g4&g16.a2&a8ag+2.&g+8r4v9e2v6ee2e1&e8f+b2r8f+f+d4c+ee4ga2&a8ag+2&g+8f+v5g+v8b4g+g+2&g+8v7b>d+v6<f+4eee4eev9b4g+g+2&g+8v8b>d+2&d+8<al2b>cdr8<b4.b&b8v7l4bv9>d+v11f+v13bv12l4.<f+v14>g+v15a4v12c-d+v11d+4<ev9ev11g+4d+f+d+4eee4v8c-c+2&c+16.c+64d64ee4f+g+e4f+v12f+>g+v13a4v12c-d+d+4<ev9ev11g+4d+f+d+4eee4d+ev9g+4g1f+1v11>g+g+16a16g+8.f+8.e8f+a2&a8v10g+g+16a16g+8.f+8.e8v9f+a2&a8g+g+16a16g+8.f+8.e8v8f+a2&a8v7g+g+16a16t90v6g+8.t75v5f+8.t60e8v4f+r32a2&a8t100,l1rrv5ba4.a2&a8bav7e2.l8&erd4.c+2&c+16.&c+64r64e2.&erv4d4.v3c+2.&c+r1rv5e2&el4.re2&e8f+f+2r8f+f+d4r<a2&a8r64>e4&e16.&e64f+2&f+8f+e2.&e8r8.r32.v7c+2r64v6<bb2a4g+2.&g+8>d+f+2r8d+d+<f4e>c+c+4ed2&d8ee2&e8ev5ev8d+4c+c+2&c+8v6f+f+c+4c+c+c+4<bbv9>d+4c+c+2&c+8v8f+f+2&f+8c+f+2g2a2&a16.&a64f+&f+64f+2&f+8v7f+4v9b4v11>d+4v13f+4v11<ev14>ev15e4v12<f+bg+4c+v9c+v10e4<bbb4>c+c+c+4<g+g+2&g+8>c+c+4c+d+<b4b>e>ev13e4v11<f+bg+4c+v8c+v10e4<bbb4>c+c+c+4<bbv8>e4e1d1v7<e1>ee<b4v6e1>ee<b4e1>ee<b4v4e1e4l16.&e&e64>e2&e&e64,l1rrv5ee4.e2&e8eev8<ee2.<b4ev5erv7el2.er4g+1g+g4f+4.f+2&f+8o4d8o2g&g8e4.e&e8r6r19v8>b2l1.rrrrrrrrrrr1l4b.r32r2r8v7>d+v9f+v11bv13>d+v11l4.<c-v14bb4v12d+d+d+4<g+v9g+>c+4<g+f+f+4aaa4ee2r8be4abg+4f+v11b>bb4d+d+d+4v12<c+2.&c+8r8<g+g+g+4a1g+>c+2&c+8v7b+1a1;|"),
    MML7(7, "Nights", "0=MML@i0t80v14<l32b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&br1r1r1r2.r8b8a8g8a16r16a2&a16r4r16b16r16b8a8.g16a2&a8.r4.r16>e16d8.c8d16<b2&b8.r4.a8g8a8g16e2&e8r4.r16b8a8g8a16r16a2&a16r4r16b16r16b8a8.g16a2&a8.r4.r16>e8d8c8d4<b2r4.a8g8a8g16e4.r16e8g8a8a1r8>e16r16e16d16e8d2&d16r16g4f+8.r16f+8e8e4.d1&d8r1r2.r8<b8a8g8a8a2&a16r4.r16b8a8g8a2&a8.r4.r16>e8d8c8d16d16<a2&a16r4r16a16r16a8g8a16r16g16e2&e8r4.r16b8a8g8a8g2r4b16r16b16r16b8a16r16g16r16g2&g8r2>e8d8c8d4<b2r4.a8g8a8g16e4.r16e8g8a8a1r8>e16r16e16d16e8d2&d16r16g4f+8.r16f+8e8e4.d1&d8r1r1r1r1<<l1rrrrrrrrrrrrrrrv14r2a.>c1&c2v11e2.d2.e2.d2.r2l1rrrrrrrrrrrrrv14c1&c2v11e2.d2.e2.d2.e1&e8r4.rrrrrrrrrrrre2.f+2.g2.e2.d2.e4.g4.a2.v14<e;|"+
                      "1=MML@i5t80v10>l32b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&bl1rrrrrrrrrr2.r8e2.d16e16f+16g16f+16d16e4.e2.d16e16f+16g16f+16d16c2.<b2.a2.g2&g8.r16>v8a1.g1&g2e4&e16r16e2&e8.r16d4&d16r16e4.e2.d4.r4.v10e2.d16e16f+16g16f+16d16e4.e2.d16e16f+16g16f+16d16c2.<b2.a2.g2&g8.r4.r16>e2.d16e16f+16g16f+16d16e4.e2.d16e16f+16g16f+16d16c2.<b2.a2.g2&g8.r16>v8a1&a2g1&g2e4.e2&e8.r16d4&d16r16e4.e2.d4.e1&e8r4<v12b8>e2f+8g8f+16e16d4.e8f+8e16d16c4.d8e8d+2&d+16r16<b8>e2f+8g8a16g16f+4.g8a8g16f+16e4.f+8g8f+2&f+8r8b2&b8a+16r16a16g16e2g8a+16b2r16a+8a16g16e2g8a2b8>c8<b2&b8.>c16<a2b8>c8<b2.r4.e2.d4.>v8e2.r4.v7e2.d4.e4.<b4&b16r16a2.e1v8l1rrrrrrrrrrrrrrrrr2.r8>c+1&c+8c1.<g2.f+4r8f+4r8g4.g4.f+4r8f+4.r4.l1rrrrrrrrrrrr>c+1&c+8c1.<g4.g4.f+4r8f+4r8g4.g4.f+4r8f+4.e1.r1r1r1e2.d2.c2.r2.e2&e8d8c2&c8d8e2&e8d8c2&c8b8a2.b2.a2.b2.e2.f+2.g2.e2.d2.e4.g4&g16r16a2.e1v8>l1rrrrrrrrrrrrrrrrr2.r8e1&e1.&e8<b4.b4.a2.b4.b4.a2.l1rrrrrrrrrrrrr4.>e1e1.&e8<b4.b4.a2.b4.b4.a2.b1&b4.r4.rrrrrrrrrrrrrr>e4.&e2.d4.e4.g4.f+4.f+4.b1;|"+
                      "2=MML@i6t80v8l1rrrrrrrrrrr2.r8e2.d16e16f+16g16f+16d16e4.e2.d16e16f+16g16f+16d16c2.<b2.a2.g2&g8.r16a1&a2>c1.e4&e16r16e2&e8.r16d4&d16r16e4.e2.d4.r4.e2.d16e16f+16g16f+16d16e4.e2.d16e16f+16g16f+16d16c2.<b2.a2.g2&g8.r4.r16e2.d16e16f+16g16f+16d16e4.e2.d16e16f+16g16f+16d16c2.<b2.a2.g2&g8.r16a1&a2>>v6g1&g2v8e4.e2&e8.r16d4&d16r16e4.e2.d4.<b1&b8r4.l1v6eeeeeeeeeeeev8e2.f+2.g2.e2.d2.e4.g4.a2.e1v10l32b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b<<l1rrrrrrrrrrrrrrrr2a.>c1v8&c2e2.d2.e2.d2.r2l1rrrrrrrrrrrrrc1&c2e2.d2.e2.d2.e1&e8v10<<l1rrrrrrrrrrrrrrrrrrrr2e2.d2.e2.d2.l1rrrrrrrrrrrrrr2>>c1.<<e2.d2.e2.d2.;|"+
                      "3=MML@i2t80v10l32b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&bl16r2<g8.rg8g8.rg8f+4f+8f+8.rf+8g8.rg8g4g8f+8.rf+8f+8.rf+8g4g8g8.rg8f+4f+8f+8.rf+8g8.rg8g8.rg8f+8.rf+rf+8.rf+8g4g8g8.rgrg8.rg8g8.rgrf8.rf8f8.rf8g4g8g8.rg8g8.rg8g8.rgrf+8.rf+8f+8.rf+rg8.rg8g8.rgrf+8.rf+8f+8.rf+rg8.rg8g8.rgrg8.rg8g8.rgrf8.rf8f8.rfrg8.rg8g8.rg8v13a4&ara4&ara4&ara4&arg4&grg4&grg4&grg4&grg4&grg8.rgrf+4&f+rf+4&f+rg4&grg4&grf+4&f+rf+4&f+rv10g8.rg8g8.rg8a8.rf+8f+8.rf+8g8.rg8g8.rg8f+8.rf+rf+8.rf+rg4g8g8.rgrg8.rg8g8.rgrf8.rf8f8.rf8g4g8g8.rg8g8.rg8g8.rgrf+8.rf+8f+8rf+f+rg8.rg8g8.ggrf+8.rf+8f+8.f+f+rg8.rg8g8.rgrg8.rg8g8.ggrf8.rf8f8.ffrg8.rgrg8.rg8a4&ara4&ara4&ara4&arg4&grg4&grg4&grg4&grg4&grg8.rgrf+4&f+rf+4&f+rg4&grg4&grf+4&f+rf+4&f+rg1r2v8>gergergergerf+drf+drgdrf+drecrecrecrecrf+d+rf+d+rf+d+rf+d+rgergergergerf+drf+drgdrf+drecrecrecrecrf+d+rf+d+rf+d+rf+d+rgergergergergergergergergergergergergergergergerecrecrecrecrf+d+rf+d+rf+d+rf+d+recrecrecrecrf+d+rf+d+rf+d+rf+drv10l16r2.r8.r32.r64.r2<b8.rbrb8.rbra4ara8.rarb8.rbrb4bra8.rara8.rarb4brb8.rb8a8.rara8.rarb8.rb8b8.rbra8.rara8.rar>c4crc8.rcr<b4brb8.rbra8.rara8.rarb4brb8.rbrb8.rbrb8.rbra8.rara8.rarb8.rbrb8.rbra8.rara8.rar>c8.rcre8.rcrd8.r<brb8.r>dr<a8.rara8.rar>e8.r<brb8.rb8l1rrrrrrl16b4brb8.rb8>d8.r<ara8.rarb8.rb8b8.rbra8.rara8.rar>c4crc8.rcr<b4brb8.rbra8.rara8.rarb4brb8.rbrb8.rbrb8.rbra8.rara8.rarb8.rbrb8.rbra8.rara8.rar>c8.rcrc8.rcr<b8.rbrb8.rbra8.rara8.rarb8.rbrb8.rb8r2r1r1r1r1r1r1r1l16v9rrbrrbrrbrrbrrarrarrarrarrgrrgrrgrrgrrbrrbrrbrrbrrbrrbrrbrrbrrarrarrarrarrgrrgrrgrrgrrbrrbrrbrrbrrbrrbrrbrrbrr>crrcrrcrrcrr<brrbrrbrrbrr>crrcrrcrrcrr<arrarrarrarrbrrbrrbrrbrrarrarrarrarrbrrbrrbrrbv8l16r1r64r2e8.rere8.rerd4drd8.rdre8.rere4erd8.rdrd8.rdre8.rere8.re8d8.rdrd8.rd8e8.rere8.re8d8.rdrd8.rdre4ere8.rerd8.rdrd8.rdrc8.rcrc8.rcre8.rere8.rere8.rere8.rerd8;|"+
                      "4=MML@i1t80v13l32b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&b&bl16r1r2.r8o1br>e2&e8b8d2r8<br>e2.d2&dr<br>e2.d2&drd8c2.g2&grg8f2&frf8e2&e8r8e2.d2.e2.d2&drd8c2&crc8g2&grg8f2&frf8e2&ere8<a4&ara4&ara4&ara8a8a8>c4&crc4&crc4&cr16l8ccceeeeeeddddddeeeeeedddddde2&e.r16d2&d.r16e4.&e16r16>e<b16r16d2&d16r16dc2.g2&g16r16gf2&f16r16fe2&ere2.d2&d.r16e2>e<b16r16d2&d16r16dc2.g2&g16r16gf2.e4.e4e<a4.a4&a16r16a4&a16r16aab>c4.c4&c16r16c4&c16r16ccdeeeeeeddddddeeeeeedddddde4&e16r16>e<b16r.e2.e2.d2.c2.<b4.>b8b8b8l16e2>er<brd2>dr<arc4.&cr<gr>cr<b4.&brbrbr>e2>er<brc4.&crcrdre2>er<brc4.&crcr<bra4.&arara8b4.&brbrb8a8a8a8a8a8a8b8b8b8b8b8b8>e8e8e8e8e8e8d8d8d8d8d8d8c8c8c8c8c8c8e8e8e8e8e8e8d8d8d8d8d8d8e8e8e8e8e8e8d8d8d8d8d8d8<e1v13l16r1r1r2.r8o1br>e2&e8b8d2r8<br>e2.d2&dr<br>e2.d2&drd8c2.g2&grg8f2&frf8e2&e8r8e2.d2.e2.d2&drd8c2&crc8g2&grg8f2&frf8e2&ere8<a4&ara4&ara4&ara8a8a8>c4&crc4&crc4&cr16l8ccceeeeeeddddddeeeeeedddddde2&e.r16d2&d.r16e4.&e16r16>e<b16r16d2&d16r16dc2.g2&g16r16gf2&f16r16fe2&ere2.d2&d.r16e2>e<b16r16d2&d16r16dc2.g2&g16r16gf2.e4.e4e<a4.a4&a16r16a4&a16r16aab>c4.c4&c16r16c4&c16r16ccdeeeeeeddddddeeeeeedddddde4&e16r16>e<b16r.e2.e2.d2.c2.<b4.>b8b8b8l16e2>er<brd2>dr<arc4.&cr<gr>cr<b4.&brbrbr>e2>er<brc4.&crcrdre2>er<brc4.&crcr<bra4.&arara8b4.&brbrb8a8a8a8a8a8a8b8b8b8b8b8b8>e8e8e8e8e8e8d8d8d8d8d8d8c8c8c8c8c8c8e8e8e8e8e8e8d8d8d8d8d8d8e8e8e8e8e8e8d8d8d8d8d8d8<e1;|"),
    MML8(8, "Mozart", "1=MML@i0t200v15l64f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+&f+l4t60v10>r2.r8c+8dv11a8a+v12d8c+v13b+8a+v14a8>dv15<a+8ge8fa8c+c+8da8a+d8c+a+8ac+8da8a+d8c+a+8a<v8d8<a>e8<av9>f8dg8cv10a8fb8ev11b+8a>d8<gv12>d+8ce8cv13f8cf+8<av14>g8c-g+8n58v15a8<aa8<a>e8<a>a+8ad8<a>>f8d<g8<a+>>d8e<a8<a>>c+8dd+8n51g8d+d+8<f>d8n58d8<d>f8ee8<f>d8g+g+8n56g8ff8<g>e8a+a+8n58a8gg8<a>f8>d<<f8bf8b+f8>d<f8>d<f8>d<f8b+f8bf8bg8bg8b+e8>dc8fd8f>d8c-c8<aa+8g<f8>f<f8>f<f8>f<f8>f<f8>ff8>dc8<aa+8ge8f>c+8d<d8c+a8c+c+8da8a+d8c+a+8ac+8da8a+d8c+a+8ac+8da+8av14c+8d>d8cv12<d8d+a+8av10f8ea8>dv8<e8d>d8<fv7c+8ef8>d<e8a>d8<fe8a>c+8<ev10c+8dv12g8f+v13a+8av15>d8c<a+8gg8dd8<a+a+8g8>f+1&f+2,t200r1t60r2.v10l4.dv11fv12ev13gv14fv15<gaa>dfegdfeg<v8fav9a>cv10c<g+v11abv12>ccv13cd+v14dfv15feec+<a>d<a+aa>dd+d+fffd4e8dffdccgeedfffffffffeefn46dc<efb+a+>d+dc-c<ef>d<a+a>dfgdfegff+g<av14gv13av12a+v11av10a+v9bv8av7a>c+ddc+dv10cv12<f+v13av15a+>g<ga+a4&a8a1&a2,r1.rl4.v10fv11av12g>v13c+v14dv15<edgl1.rrrrrrrrrrrrrrrrl4.rd+d<aa+g+aa+a>agen61>c+d<da+v14f+v13gv12gv11gv10fv9ev8dv7efefeev10fv12av13b+v15f+ga+dgd1&d2;|"+
                      "2=MML@i5t200v15>l64d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d&d<t60r1r1r1.r4v9a2v10>f8d8v11l4.dc+v9<a2v10>f8d8v11dc+v8<dev9fgv10abv11>cdv12d+ev13ff+v14gg+v15av11<aa2l8a+aa2>fdc+2del4.edv13d+2g8d+8d+dd2f8e8ev12dd2g8f8fv11ee2&e8<a8av10ab>cdddc<bbbv11>cv12dc4v13l8ff2dc-c2<a+gf4l1rrrrr4v9a2v10>f8d8v11l4.dc+v9<a2v10>f8d8v11dc+4&c+8r2.v15dv14cv13<a+v12av11gv10fv9ev8dv7c+defeel1r.v13>d&d2d&d2,t200l1rt60rrr.r4v9f2v10d8a8v11l4.ggf2d8a8ggv8<a>c+v9dev10fev11egv12ggv13fav14a>dv15dv11c+<g2l8&ggf2dag2&gc+l4.afv13a+2g8a+8a+a+a2&a8g8v12f2a+8g+8g+g4g8gv11ge2a8g8gv10fg+g+g+g+g+g+g+g+gv11gv12gv13cd2f8f8fe4e8f4r1r1r1r1r4v9f2v10d8a8v11ggv9f2v10d8a8v11ggr1.v15d+v13d+v11ev9dv8<a+v6bv4av7a>c+d2.c+l1r.v13g&g2f+&f+2,l1.rrrrrrrrrrrrrrrrrrrrrrr4v9d2v10<a8f8v11l4.a+a>v9d2<v10a8f8v11a+a4&a8r2.>v15dv14f+v13gv12<gv11a+v10av9gv8fv7efa1&a8al1r.v13a+&a+2a&a2;|"),
    MML9(9, "Stella-rium", "0=MML@i25t170v13l8o2c+c+c+2>g+2g+2g+4<d+d+d+2.&v10d+l4ffd+.v15c+>g+8c+l8c+g+c+<d+4>a+d+4d+a+d+<c4>g+c4cg+c<f4>b+f4fb+f<c+4>g+c+4c+g+c+<d+4>a+d+4d+a+d+<f>cf<f4f4g+4>>g+4.o1g+2v13>>c+g+>c+f4n44c+4<c+g+>c+f4c+g+4<cg>cd+4<g>c4cc+d+g+4d+4.<c+g+>c+f4n44c+4<c+g+>c+f4c+g+4<<f4>fg+4fc<f4d+4d+4n39d+n39c+4>g+c+4c+g+c+<c+4>g+c+4c+g+c+<c4>gc4cgc<c4>gc4cgc<<a+4>>f<a+4a+>f<a+d+4>a+d+4d+a+d+n32d+g+a+>cd+l4g+<g+2<g+2c+.>c+8g+c+<d+8d+>d+8a+d+<f.>f8b+f<c8c>c8gcn22f8<a+l8a+>f<a+c4>gc4cgc<c+4>g+c+4c+g+c+<d+a+>d+fgd+ga+d+2.<d+1&d+4c+4>g+c+4c+g+c+<d+4>a+d+4d+a+d+<c4>gc4cgc<f4>b+f4fb+f<c+4>g+c+4c+g+c+<d+4>a+d+4d+a+d+<f4>b+f4fb+fn32d+l4g+<g+n20c+>g+8c+l8c+g+c+<d+4>a+d+4d+a+d+<cgb+c4d+4f4>fb+f>fc<fb+<c+4>g+c+4c+g+c+<d+4>a+d+4d+a+d+<<g+4>>d+<g+4g+>d+4<<g+>d+g+>cd+g+d+<g+c+4>g+c+4c+g+c+<d+4>a+d+4d+a+d+<c4>g+c4cg+c<f4>b+f4fb+f<<a+a+a+l4>ccc+c+d+d+d+2.&d+8n15f8f8f8d+d+c+1&c+8<g+1,v13>g+8d+8g+2g+g8g+a+l8g+gg+g+d+g+2g+4v10a+l4g+g+g.v15g+.d+.d+8c+>cc+d+.<g+.d+.g+8a+g+g+2d+8g+8d+8gg+b+a+gg+a+g+.gg+d+l16g+d+c+<g+d+c+l4<g+.>>d+8v13d+g+8gd+d+.c+cc+d+g+8gd+d+1d+8d+g+8gd+d+.c+cc+d+g+8a+g+g+g+g+g+d+8d+g+8gd+d+.c+cc+d+g+8gd+d+2.&d+8d+d+.g+.a+>cc+8c<g+g+1&g+8<g8g+a+g+8g+>g.g+.d+g.g+.d+g.g+.a+g+2.cl8c+<fn63f>f<f>g<g>g+4d+l4d+.d+fgg+a+b+a+8a+.g+a+l32.n75a+g+d+16<a+l16g+d+<a+g+d+4d+ga+>d+ga+>d+ga+>d+4&d+l8<d+g+g+g+g+g+gg+g+g+a+b+a+4.n51d+a+4a+a+a+g+a+a+a+>cc+c4.<<f>d+g+d+gd+g+d+b+d+a+d+g+d+gd+g+d+a+4.g+a+4.>c+c4<gd+g+d+a+d+g+4g+4g+gg+g+g+a+b+a+4.n51d+a+a+a+a+4g+4>d+4c+cc+c4.n68d+n63c+n63cn63c+n63c<d+a+d+g+d+a+d+g+n56g+g+n80g+g+n56g+d+gd+g+d+a+d+l4g+.d+.d+8c+>cc+d+.<g+.d+.g+8a+g+g+2&g+8c+.d+d+ffgga+8l16n75a+g+d+<a+g+d+n63a+g+d+<a+4&a+l8>>g+g+g+g4g4g+1&g+<<g+1,v13<c+c+1&c+2.d+d+2.&v10d+8>>ccl4.<a+v15g+g+c+>c2d+<g+g+g+8l4a+>cc2&c8<<c+>c+g+>cd+<gg+a+g+.gg+g+g+.l1<<g+&g+&g+2v13>c&c&c&cl4f.>c2.a+a+a+d+8d+c+8gd+d+.c+g+g+g.gd+d+2.&d+8d+d+.g+.a+>cc+.<g+g+2.&g+8g+c2<g+2>>c+.c+2&c+8<a+.a+2&a+8>c.c.<a+g+1a+a+a+a+g+g+8g+.c>c+c+c+c+c<a+8a+.g+a+<g1&g2.>>c+c+c+c+c+1<a+a+a+8g+8a+a+8>cc2&c8<<c+>c+2c+n39d+2d+a+.g+2>c+8c2<<g+<g+>>g+g+g+g+g+8a+a+2&a+8a+a+8a+a+>d+c+8cc2<<c+>c+2.n39d+2.<<g+2r>g+1&g+>g+.g+.c+<d+.>>c+d+.<g+.g+.g+8a+g+g+2&g+8c+.d+d+ffgga+1&a+8g+g+8ggg+1&g+8;"),
    MML10(10, "Fly Me to the Moon", "0=MML@l64a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&ar2l8>f+4o3a<f+r>a4<f+r4>a<br>a4<f+r4>g+<er>g+4<er>b>c+<brg+4<ar.r32o5e64f64o3adra4dr4f+<gr>b4<gr4>b<g+r>b4<af+v7o4c+df+g+ab>c+r4v8o3a<br>a4<f+r4>g+<er>g+4<er4>bc+rb4c+r4acra4cr4a<br>a4<br.r32o5e64f64o3g+<er>g+4<er4>g+<ar>g+4<aro4dre4<b4<g+r4>a<f+r>a4<f+r4>a<br>a4<f+r4>g+<er>g+4<ero4e<g+<ar>g+4<ar.r32o5e64f64o3adra4dr4b<gr>b4<gr4>b<g+r>b4c+rf+a>c+e.f+.>dc+4o3a<br>a4<br4>g+<er>g+4<er4>bc+a+>c+4f+r4<a+<f+r>a+4<f+r4>a<br>a4<f+r.r32o5g64g+64o3g+<er>g+4<er4>g+<ar>g+4<ao5dc+2r4c-16c+16<b2.bg+2c+16d16e.c+.bf+2.r4t100<d4.d16.&d64r64o5c+.<b.g+r16bg+.e16c+16e1,r1rv14l8>ag+f+e4d2ef+a4g+2f+ed4c+4.v9o3g+<ao4f+.b.ag+4v13>f+edc+4c-2c+d4f+4f4dc+4<b4a1abv14>f+4f+2.ra4.g+4.e2.rv9c+c4<af+d+.<b.gv14>f+>d4d2.rf+4.e4.d4.c+4.v8<eg+ab4r4.b+a4rv15>ag+f+e4d2ef+a4g+2f+ed4c+4.v8c+c-c+dec+4.v14f+edc+4c-2c+4.df+f4dc+4<b4a2&arv8<a+4c+v15>b>f+4f+2.ra4.g+4.>c+2.rv8o2go5c+dc+c-4c+4v15>c+c+.<a.a2.ra4.g+4.a2v8<ea>c+er4o3adra4dr4g+<ar>g+4<ar4>adra4dr4g+<ar>g+4<ar4>ar4a4dr32g+1.,r1v9l2o2f+f+bf+eeaa>dd<ggg+>c+r8<a8r8a+r8bf+ee>c+c+cc<bbeeaag+4.>c+&c+8<f+f+bf+eeaa>dd<ggg+>c+<f+4r8f+r8bbee>c+<gf+f+bf+eeaa>dd<aa>dd<aar>d<a1.;"),
    MML11(11, "Heavenly Flight", "0=MML@t80l64c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&c&cr2l16rv7ct88ea>ceaer<c-egb>eger<<a>cefa>cer<<gb>degb>dr<<fa>cdfab+r<fg+>dfg+>cdr<<g+b>df+g+b>ef+g+ef+dec-dr<<cea>ceaer<ceg+b>eg+er<ceg>ceger<cea>ceaer<fa>ceaecrn46dg+b+g+fd+r<ef+at84b>ef+at80<eg+bt76>df+t72g+>c+et88l8o2a>e>ca<<g>eb>g<<f>cfb+<eb>eb<da>da<<a+>g+>fb+<eb>f+b>f+ed<b<a>e>ca<<g+>eb>g+<<g>e>cg16l64&g>dfgl8o2f+>e>ca<<f>a>ee<c-beb<a>c+t82a>c+t72at68>c+t64at60<c+4.r4t104c-l16f+rc-8f+r<a+8n54ra+8n54ra8n54ra8n54rg+8n54rg+8n54rf8b+rf8b+re8b+re8b+rd8b+rd8b+r<a+8>b+r<a+8>b+l8r.ab>deab>dr<<f+t82g+bt76>df+t72g+r64b2rr32.t82l16ceat88>ceaer<c-egb>eger<<a>cefa>cer<<gb>degb>dr<<fa>cdfab+r<fg+>dfg+>cdr<<g+b>df+g+b>ef+g+ef+dec-dr<<cea>ceaer<ceg+b>eg+er<ceg>ceger<cea>ceaer<fa>ceaecrn46dg+b+g+fd+r<ef+at84b>ef+at76<eg+bt70>df+t64g+>c+et78l8o2a>et88>ca<<g>eb>g<<f>cfb+<eb>eb<da>da<<a+>g+>fb+<eb>f+b>f+ed<b<a>e>ca<<g+>eb>g+<<g>e>cg16l64&g>fgg+l8o2f+>e>ca<<f>a>ee<c-beb<a>c+t82a>c+rt68>c+t64at60<c+4<a2.&a,r1v15>e1r8d8ge<bl12abge2g4e1l8b>cdec-4l16rcdel8c-cdea4.eeddceddc<b1r16v7<ca>ee<c-g>ee<<a>ea>e<<g>dg>d<<f>cfb+<f>dg+>d<<g+>dg+>eg+f+ed<ca>ee<cg+>ee<cg>ee<ca>ee<f>cac<e>d<g>d<c-eb>eb>e<e<b2l64&b>>defv10l8.f+e16a4f+2&f+e16a4f+4c+d16ed16g4e2&ed16l4gec<b1v8l8e<deg+b>deg+2rv15>e1rdl4ge<bl12abge2g4e1l8b>cdec-4l16rcdel8c-cde16.v8g64g+64v15a4.eeddceddc<b1r16v7<ca>ee<c-g>ee<<a>ea>e<<g>dg>d<<f>cfb+<f>dg+>d<<g+>dg+>eg+f+ed<ca>ee<cg+>ee<cg>ee<ca>ee<f>cac<e>d<g>d<c-eb>eb>e<e<b2&b16,r1l2<agfedn34e1<ag+gf+f>f<bev15o5e1r8d8l4ge<bl12abge2g4e1l8b>cdec-4l16rcdel8c-cdea4.eeddc<bag4a1.rv7brbrbrbrararbrbrerergrgrararfrf<<e1e1.l2>agfedn34e1<ag+gf+f>f<bev15o5e1r8d8l4ge<bl12abge2g4e1l8b>cdec-4l16rcdel8c-cdea4.eeddc<bag4a2v8a1;"),
    MML12(12, "Day After Tomorrow", "0=MML@i47v10<<a>e>c+<<g+>f+g+<f+>c+f+c-f+f+<a>e>c+<<g+>f+g+<f+>c+f+<eb>eeg+d+c+g+<ba>eac-c+d+eg+<g+a>e<g+f+>e<ab>d+ae2d+c+2<bf+>ea<g+>f+g+c+2g+<f+>c+f+c-f+d+<eb>edaaf+adc+g+>c+<<f>c+g+<f+>c+2<f+>c+2c-f+ac-2d+e2e<g+>ea<f+>c+f+<g+>f+g+<c+2>c+<f+>c+ac-f+a<eb>eba1&al8<g>dbdbd<f+>dadad<a+>fa+fa+f<a>f>gcfc<d-a->fd-a-f<e-b->ge-b-e-<cb->ge-ce-<f>cgfcf<d-a->fd-fe-<e-b->g<b->ge-<cb->e-<b->e-<b-f>ce-fge-<d-a->d-<a->e-<b-e-b->e-fge-<<f>cfa>cfafc<afc<a->e-b+e-b+e-<g>e-b-e-b-e-c-g-bg-bg-<b->g->a-d-g-d-<da>f+daf+<eb>g+ebe<c+b>g+ec+e<f+>c+g+f+c+f+<da>f+df+e<eb>g+c-g+e<c+b>ec-ec+<f+>c+ef+g+e<da>d<a>e<beb>ef+g+e<<f+>c+f+a+>c+f+a+2&a+.,v15bl8ag+f+el4f+g+ec-e2c-ed+bl8ag+f+el4f+g+e1.&ee2c-g+2ef+2g+f+2.g+ab>c+<bag+2ef+2.e2c-g+2ef+2g+f+2.g+ab>c+<bag+2f+e2.f+2g+a2f+g+2ab2.>c+<bag+f+ef+2g+f+2.e2c-g+2ef+2g+f+2.g+ab>c+<bag+2f+e1.&e2bb2>f+e2d<a2>ed2c<gf.>c8c<b-g8g8e-cgf2e-fga->fe-2<e-fg>fe-.c8<fe-.>e-8<fe-.>e-8f1&fcc2gf2e-<b-2>fe-2d-<g+f+.>c+8c+<bg+8g+8ec+g+f+2ef+g+a>f+e2<ef+g+>f+e.c+8<f+e.>e8<f+e.>e8f+1&f+.&f+16,v10l2.c+<b1aa2>c+<bb4>e4d+4el2c-d+4ec-4c+l4c-d+ef+e2ee2ee2c+c+2d+<bb>d+ed+c+c+2ed+<bb>ef+g+a2ec+c-d+<bf+g+>ddddf+dfc+f+g+c-c+<ab>c+ec+<aab>ed+c+c-ec-ee2<baa>ed+c+g+c+d+ee2ee<a>d+g+<f+g+2&g+8b8>c+8d8l16.ef+16gab16>c+l16def+32ga32l2.ba>dcff4g4f4e-fff4g4f4e-ffe-f1&f4l16<e-fg32a-b-32l2.b+b->e-d-f+f+4g+4f+4ef+f+f+4g+4f+4ef+f+ef+1&f+4.&f+16;"),
    MML13(13, "Touhou XMAS", "0=MML@i0l64a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&ar4<t60a+1t120b2.l8r>c+rc+c+4l2raaa+>d4c4<a+a+l8agab+4.a4g2g2f4>fedfe4l2defr4c4<aaa+>d4c4<a+a+l8agab+4.a4g2g2f4>fedfe4d2e2decl2d&d8<aaa+a+a+a+>cl8edc+<a+l1agagdegl2a.a+4aabbaabb>ddeeffdrf.d4cea.e4frf.d4n58dg.e4c+r<aaa+>d4c4<a+a+aaggaa4>c4defr4c4<aaa+>d4c4<a+a+aaggaa4>c4del1n46<t115dt110c-c+4.&c+16l32<gfd+4.&d+16.r<dfa>c+f>c1&c1.,r1d1f+2.l8rfrff4l2rfefa+4g4fff8l4r<a.b+b2>c+2dl8agf>dc4l2n58cdr4<a4fefa+4g4fff8l4r<a.b+b2>c+2dl8agf>dc4<a+2b+2a+b+aa+2&a+l2ffggfc+a+l8n61a+agl1fc-f<ba+b+a+l2>f.f4ddddddddffggaafr>d.<a+4g>ce.c4drd.<a+4fa+>d.<b4grfefa+4g4ffffeeff4g4a+>cdr4<a4fefa+4g4ffffeeff4g4a+b+l1dfdf4.&f16l32rd<a2<fa>c+fa>e1&e1.,r1a1d2.l8raraa4l2rdcdf4e4ddr4.f4.f4eerr8a8g4fga+r4e4dcdf4e4ddr4.f4.f4eerr8a8g4fgr1ccdddfeel1.rrrrrrrrrrr1l2a+.f4egb+.a4ara+.g4dfb.g4er>dcdf4e4ddccc-c+dd4e4fga+r4e4dcdf4e4ddccc-c+dd4e4fg<f1a1f+1gc+;|"+
    					     "1=MML@i2l64a&a&a&a&a&a&a&&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&a&av0a4t60l16av10>aga>d<agaraga>d<agat120v0l8av10aga>d<agaro2aa4r4ag+d4.dl4c<aa+.>f8a+ag.g8cef.c8ffe.e8aad.d8dc<a+.a+8aag.>a+8aed.d8c<aa+.>f8a+ag.g8cef.c8ffe.e8aad.d8dc<a+.a+8aaa+.a+8>f<a+>f.f8ffg.g8gga+.a+8a+a+>c.c8<aal8>ddd4r>gfr<ddd4r4>fg<ddd4r>>d4<a<ddd4r4>de<<a+a+a+4r>>gar<<aaa4r2ggg4o5aa+>edo2aaa4r2>ddd4r2ddd4r2ddd4r2ddd4r2<a+a+a+4r2aaa4r2>ddd4r2ddd4>defa<<a+4.a+l4a+a+a+.a+8a+a+a.a8aal8>ddd4>defa<<g4.gl4ggc.c8cce.e8eea8a8arad.d8c<aa+.>f8a+ag.g8cef.c8ffe.e8aad.d8dc<a+.a+8aag.>a+8aed.d8c<aa+.>f8a+ag.g8cef.c8ffe.e8aad.d8dc<a+.a+8aal8ro5aga>d<agav0t115av10aga>d<agat110v0av10aga>d<agarb+a+fc+c<a+a,v12r1r1r1.a>cdl8ffec<al4>d.r<fga>dl8cdc<a2r4.a>cddddedef2r4def4ffgecdl4d.r<a>cdl8ffec<al4>d.r<fga>dl8cdc<a2r4.a>cddddedef2r4def4ffgecdd4.l1rrrr.r4<a2l4.rgl2r.arg4g4rfr4.g.l8rfa1r1ra2rgfg2r4.fga2r>d4<ag4g4r4def2rgag2.red2.r1l4r.>cc8cdc.<a+a8g8a8a2r1.r8>cc8cdc.<a+l8agaa2r1ra4>c4d4ffec<al4>d.r<fga>dl8cdc<a2r4.a>cddddedef2r4def4ffgecdl4d.r<a>cdl8ffec<al4>d.r<fga>dl8cdc<a2r4.a>cddddedef2r4def4ffgecdd1,r1l16o1v10g2&g>dal8g.de2.&eal1.rrrrrrrrrrrl8ro5fefc4fedc<a+ag4>gffeedl4.df8c+f8ce8e2l1.rrrrr4l8dgr2>c+fga2&al1.rrrrrrrrrrrr2l8rfed2rr1<aga>c2rr1r4fedfe4r1r1r1rfed2rr1<aga>c2rr1r4fedfe4l1ro2a+ged+d&d&d;|"),
    MML14(14, "Birthday Rag", "0=MML@i5t132v14l8>b+fa>d4c<afb+f+a>d+4dc<aa+ga+>ed+d<a+ag2.r4eff+>d4c+c<f+agd+ec<ba+g+acdd+dcab+a+df>d4c+c4b+fa>d4c<afb+f+a>d+4dc<aa+ga+>ed+d<a+ag2.<g4>fdfg4fedcfa>d4c<a+g+a4a4ag4f2.&fr4v13<a+gaa+4a+g+f+f4.g4.c-4rd+ga+agf+f2.&fc4>a+gaa+4a+g+f+f4.g4.c-4rega+>d4dc2.&cr4l4.<<a+a+2&a+8fgc-4l8r>d+ga+agfa+4cc+d4.r4v12a+d+gb+4a+agfdfg4.c-4rd+ga+>c4d<a+2.&a+rd2.&dv15<d>d4c<a+aa+gr>c2.&c<cb+4a+ag+afrcb+c>ccr<crcb+c>ccr<cr>cb+c>ccc<c>ccc<c>ccr<cr4>c2&cdc<agfgd4efga2&agecd2.r4>c2&cdc<agfgd4c+def2&fgag2.&g<a+4>>c2&cdc<agfgd4efga2&agal4d.d+dcl8d<f>dfc+<f>c+fc<f>cf<bfb>f<a+fa+l4>f.ef2>v11f,v13l8>crcf4cr4crca4r4.gr4a+r4d+rd2.r4cl4ra+r1.r<c8r2f8rgr.l8>crcf4cr4crca4r4.gr4a+r4d+rd2.<d4a+fa+l4br.a8r>f+r.ffe8rc2.&c8r1.r8v11<c.r1.d+d<g+>>d8rd+r.c.c.r2.a+a+8g2.&g8r<g.f+2&f+8d.d.r1r8n63ra+.rv12a+8r>gr.<a+8r>d.r2.f+l8rf2.&fr4.v14<f+f+f+f+4ra+4r4.gr2eeee4re4r2.rf+rf+gr2f+rf+gr2>f+rf+gf+rf+gf+rf+gr2a2&aar4dl4rc-r.f2&f8e8r2<g+gr>a2&a8a8rd8rc-r.c+2&c+8re2.&e8ra2&a8a8rd8rc-r.d2&d8r8e8c.<ba+al8a+ra+fa+ra+farafg+rg+fgrgl4a+.>cc2f,;|"+
    					      "1=MML@i5r1r.v13>f+r.d8rg8rc8r8<a+2.r2r8>er1.r<<a8r2>d8rer1.r>f+r.d8rg8rc8r8<a+.a.<a+r1r.>>cr.ddc+8r<a2.&a8r1r1r1.v11c<a+r2r8>>cr1.r.ee8e2.&e8r<e.d+2&d+8c.c.r1r8>cr1rv9er2.c.r2.d+l8rd2.&dr4.v14<cccc4rf+4r4.dr2cccc4r1r4d+rd+er2d+rd+er2>d+rd+ed+rd+ed+rd+er2f2&ff+r4c-r2.rd2&dc+r2<f4e4r4>f2&ff+r4<br2.rb2&br2r>d4c4r4f2&ff+r4<br2.ra+2&a+rn61l4a.g+gf+r1r1r.g.ga2,l1.rrrrrrrr2.v13a+2r1r1r4b4b4a+8rrrrrrr4.v11d4.c2&c8rrrrrr4.v14d4rrrrr2.l8r>c2&cdr1r4<a+2&a+a+r1r4>c2&cdr1r4<f2&fr1r4.>c2&cdr1r1n58f2.&f,;|"+
    						  "2=MML@i6v10n29c<cb+<f+n50an50gn50dn50gl8>a+>cdcl4<a+<c>b+<g>b+<c>b+<e>b+f8rf+8r2f8ra+.<cf>b+cb+<f+n50an50gn50dn50g8>d8g.d8gggg+g+aa>ccn31dn24c<fc<f>a+v8>d.c2&c8c<a+<g>ga+a+d+d+<a+8>f8g+fd>ddccc<a+<g>ga+a+eel8<f>cfa4gf4a+gaa+4a+g+f+l4fa+g<g>a+a+d+d+g+fa+n34d+a+eb+fb+g>d<ca+faa+<f<a+r.v11l8>>dddd4r<d4eff+g4r4.>cccc4r<c4dd+ef4r4>cr4cr2cr4cr2cr4cr4.cr4.cr2l4c<a+an50gn50dn50gn50c>b+<f>ba+<cb+a+an50gn50d>>d<c+<g+c+>ba+<a+agrb+8a+8an50gn50dn50en50a>>c+c<ba+d+a8rg8r.f8r.f8r.f8r.c<c8b+fc<f,rv11<arar>crcrn46rcrl8<<gaa+al4gr>a+ra+ra+ra+<f8rf+8r2g8r>c.r2arar>crcrn46rcr1<<a+a+bb>ccaarbra+r2.gv8a+.a+2&a+8dr2.gg<ffr1>a+a+a+a+dr2.ggc+c+r1r1rdr2gg<ffa+r2.v9d+>g<en46fn46g>b<c>g<f>f<a+r2.l8rv12dddd4r1r4.cccc4r1r4cr4cr2cr4cr2cr4cr4.cr4.cr2l4fd+d>b+rbrbra+ra+rc+cr<fd+d>b+rbrbr2.c+cr1<f8d+8d>b+rbrbra+ra+ac+c<fa+8ra+8r.a8r.g+8r.g8,l1.rrrrrrrrrr2.v9l4<frer1v7e.d+2&d+8r1r1r1eed+d+l1.rrrrrr4v9e4rrrrrrrr2r2.v11l4f+rfrfrfrer1.rf+rfrfr1r1.rf+rfrfrere;|"+
    					      "3=MML@i18v10n29c<cb+<f+n50an50gn50dn50gl8>a+>cdcl4<a+<c>b+<g>b+<c>b+<e>b+f8rf+8r2f8ra+.<cf>b+cb+<f+n50an50gn50dn50g8>d8g.d8gggg+g+aa>ccn31dn24c<fc<f>a+v7>d.c2&c8c<a+<g>ga+a+d+d+<a+8>f8g+fd>ddccc<a+<g>ga+a+eel8<f>cfa4gf4a+gaa+4a+g+f+l4fa+g<g>a+a+d+d+g+fa+n34v9d+a+eb+fb+g>d<ca+faa+<f<a+r.v13l8>>dddd4r<d4eff+g4r4.>cccc4r<c4dd+ef4r4>cr4cr2cr4cr2cr4cr4.cr4.cr2l4c<a+an50gn50dn50gn50c>b+<f>ba+<cb+a+an50gn50d>>d<c+<g+c+>ba+<a+agrb+8a+8an50gn50dn50en50a>>c+c<ba+d+a8rg8r.f8r.f8r.f8r.c<c8b+fc<f,rv9<frfrf+rf+rgrf+r1rerererer1r1rfrfrf+rf+rgrf+r1r1.ddrgrgr2.cv7g.f+2&f+8r1ccr1.ggf+f+r1cc<f+f+r1r1r1>ccr1.rv9d+rgrfrfrd+rd+l1.rrrrrrr2.v11l4argrgrgrgr1.rargrgr1r1.rargrgrgrgd,;|"),
	;
//    MML15(15, "test", "MML@l16V10t60r4cecececececececet120cecececececececet240cececececececece;"),
//    MML16(16, "test", "MML@l16V10t60r4cecececececececet120cecececececececet240cececececececece;"),
//    MML17(17, "test", "MML@L4 C&C&O5<C&C&C&C,L4E&E&E&E&E&E,L4G&G&G&G&G&G;");

	private int index;
	private String title;
	private String mml;

	TestData(int index, String title, String mml) {
		this.index = index;
		this.title = title;
		this.mml = mml;
	}

	/**
	 * A mapping between the integer code and its corresponding Status to
	 * facilitate lookup by code.
	 */
	private static Map<Integer, TestData> codeToMMLMapping;

	public static TestData getMML(int i) {
		if (codeToMMLMapping == null) {
			initMapping();
		}
		return codeToMMLMapping.get(i);
	}

	public String getTitle() { return title; }

	private static void initMapping() {
		codeToMMLMapping = new HashMap<>();
		for (TestData s : values()) {
			codeToMMLMapping.put(s.index, s);
		}
	}

	public int getIndex() {
		return index;
	}

	public String getMML() {
		return mml;
	}

	@Override
	public String toString() {
		return "MMLData" + "{index=" + index + ", title'" + title + "', mml='" + mml + '\'' + '}';
	}

	public static void main(String[] args) {
		MML_LOGGER.info(TestData.MML5);
		MML_LOGGER.info(TestData.getMML(6));
	}
}
