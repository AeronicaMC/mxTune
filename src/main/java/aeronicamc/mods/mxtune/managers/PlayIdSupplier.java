
package aeronicamc.mods.mxtune.managers;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.function.IntSupplier;

public class PlayIdSupplier
{
    public enum PlayType implements IntSupplier, Comparator<PlayType>
    {
        EVENT(400000, 499999) {@Override protected PlayIdSource next(PlayIdSource playIdSource) { return playIdSource;}},
        PERSONAL(300000, 399999) {@Override protected PlayIdSource next(PlayIdSource playIdSource) { return playIdSource;}},
        PLAYERS(200000, 299999) {@Override protected PlayIdSource next(PlayIdSource playIdSource) { return playIdSource;}},
        BACKGROUND(100000, 199999) {@Override protected PlayIdSource next(PlayIdSource playIdSource) { return playIdSource;}},
        INVALID(-1, -1) {@Override protected PlayIdSource next(PlayIdSource playIdSource) { return playIdSource;}},
        ;

        final int start;
        final int end;
        final PlayIdSource playIdSource;
        final PlayType playType;

        PlayType(int start, int end)
        {
            this.start = start;
            this.end = end;
            playIdSource = new PlayIdSource(start, end);
            this.playType = this;
        }

        private PlayType getTypeForPlayId(int playId)
        {
            for (PlayType type : values())
            {
                if (playId >= type.start && playId <= type.end)
                    return type;
            }
            return PlayType.INVALID;
        }

        protected abstract PlayIdSource next(PlayIdSource playIdSource);

        @Override
        public int getAsInt()
        {
            return playIdSource.getAsInt();
        }

        @Nullable
        Integer compare(int playId1, int playId2)
        {
            PlayType p1 = getTypeForPlayId(playId1);
            PlayType p2 = getTypeForPlayId(playId2);
            return p1 != PlayType.INVALID && p2 != PlayType.INVALID ? compare(p1, p2) : null;
        }

        @Override
        public int compare(PlayType o1, PlayType o2)
        {
            return o2.compareTo(o1);
        }
    }

    private static class PlayIdSource implements IntSupplier
    {
        int start;
        int end;
        int range;
        int counter;

        PlayIdSource(int startIn, int endIn)
        {
            start =  Math.min(startIn, endIn);
            end =  Math.max(startIn, endIn) + 1;
            // range = end + 1 - start
            range = Math.abs(end - start);
        }

        // bounded inclusive integer from start to end that rolls over
        @Override
        public int getAsInt()
        {
            return (counter++ % range) + start;
        }
    }

    @Nullable
    public static Integer compare(int playId1, int playId2)
    {
        return PlayType.EVENT.compare(playId1, playId2);
    }

    public static int compare(PlayType playType1, PlayType playType2)
    {
        return PlayType.EVENT.compare(playType1, playType2);
    }

    public static PlayType getTypeForPlayId(int playId)
    {
        return ((playId >= 100000) && (playId <= 499999)) ? PlayType.EVENT.getTypeForPlayId(playId) : PlayType.INVALID;
    }

    public static final int INVALID = -1;
}
