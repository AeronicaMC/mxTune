package net.aeronica.mods.mxtune.util;


import java.util.HashMap;
import java.util.Map;

public enum SheetMusicSongs
{
	SONG01(0, "Eight Little Organ... - Fugue 6 - J.S.Bach", "MML@t120o5l8d2e-de-cd4r<gaa>dc<b-g>dd<b-g>e-2d2c4.<b-16a16b->cd4g4.c+d2c4d4rc+d4rdcccd16c16<b-b-b->c16<b-16a4.>d4.c4.<ab-4a2b-4rab-4r>c<aa>d4<gg>c4<f+f+g4.a16g16f.g16gab->cd4rdc+4d2c4d4rc+d4rdcccd16c16<b-b-b->c16<b-16aaab-16a16ggga16g16fa>d2c+4d4f4e-2d2c2<b-2a4b-2rab-4r>de-e-e-f16e-16ddde-16d16cccd16c16<b->cde-<a4.>d4.c4.<b-16a16b-4a2b-4rab-4r>c<aab-4gga4f+f+g2f+4g2a-ga-fg4rcddgfe-ca-2g2f4.de-4d2e-fg4f+4a4>d2e-de-cd4r<gaa>dc<b-g>dd<b-g>e-2d2c2<b-4a2.r>dcccd16c16<b-ag2f+4g2.rf+g1,l8o4r1r1r1r1r1r1r1r1d2e-de-cd4r<gaa>dc<b-g>dd<b-g>e-2g2c2<b-a16g16a2g2b-ab-ga4rdeeagfdaafdb-2a2g2f4e2d4rdge<aa>d4>rdcccd16c16<b-b-b->c16<b-16aaab-16a16ggga16g16f4<b->de-cf<fb-4>b-2a2g2f+4g4r4d2e-de-cd4r<gaa>dc<b-g>dd<b-g>e-2d2c2<b-4a2g4r4r2>e-fga-<b2>c4r>c<b-b-b->c16<b16a-a-a-b-16a16g4rcfdg<g>c4r>c4ccd16c16<b-ab-g>c4r<fb-ab-4>c4d2rdd4r<gfffg16f16e-e-e-f16e-16d4r>dcccd16c16<b-b-b->c16<b-16a4>d4<gab-a16g16a4d4e-e-e-f16e-16d2<g1,o4l2r1r1r1r1l8g2b-ab-ga4rdeeagfdaafdb-2a2g2f+4g2f+4g2f4g4rf+g4rgfffg16f16e-e-e-f16e-16ddd4c2<b->cd4g4rb-a4a4g4g4a4raa4g2a4f4g4e4f4d4c4d4rfe4e4a4b-2a2g2f2e-4.f16e-16d4cd16e-16dcd4d4c2<b-2a2g4>g2f+4g4g4f4g4dc4d4rf+4g4rgfffg16f16e-e-e-f16e-16ddde-16d16cccd16c16d<fe-ef4rb->e-4r<a-4.g4g4r>e-ddde-16d16cccd16c16b4c2b4cde-4d4f+4a4gb->c<b->c<aff+g4e-4d2rab-grb-aaab-16a16ggga16g16f+4g2f4d4g2f+4d4d2rc<b4>c4.d16c16<b-ab1<;"),
	SONG02(1, "Für Elise - Beethoven", "MML@t140l8>ed+ed+ec-dc<a4rceab4reg+bb+4re>ed+ed+ec-dc<a4rceab4reb+ba4r4>ed+ed+ec-dc<a4rceab4reg+bb+4re>ed+ed+ec-dc<a4rceab4reb+ba4rb>cde4r<g>fed4r<f>edc4r<e>dc<b4ee>e<e>ee>e<d+ed+ed+ed+ed+ec-dc<a4rceab4reg+bb+4re>ed+ed+ec-dc<a4rceab4reb+ba1.,t140r1l8o2a>ear4.eg+br4.<a>ear1r<a>ear4.eg+br4.<a>ea4r1<a>ear4.eg+br4.<a>ear1r<a>ear4.eg+br4.<a>ear4.cgb+r4.c-gbr4.<a>ear4.<e>er1r1.<a>ear4.eg+br4.<a>ear1r<a>ear4.eg+br4.<a1.,t140l2.o1r1a>e<a&aa>e<a&aa>e<a&aa>e<a>c<gae1.r2ra>e<a&aa>e<a1.;"),
	;

	private int index;
	private String title;
	private String mml;

	SheetMusicSongs(int index, String title, String mml) {
		this.index = index;
		this.title = title;
		this.mml = mml;
	}

	/**
	 * A mapping between the integer code and its corresponding Status to
	 * facilitate lookup by code.
	 */
	private static Map<Integer, SheetMusicSongs> codeToMMLMapping;

	public static SheetMusicSongs getMML(int i) {
		if (codeToMMLMapping == null) {
			initMapping();
		}
		return codeToMMLMapping.get(i);
	}

	public String getTitle() { return title; }

	private static void initMapping() {
		codeToMMLMapping = new HashMap<>();
		for (SheetMusicSongs s : values()) {
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

}
