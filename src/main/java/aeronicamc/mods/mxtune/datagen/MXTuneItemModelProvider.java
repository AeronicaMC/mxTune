package aeronicamc.mods.mxtune.datagen;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.items.*;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.Arrays;

import static aeronicamc.mods.mxtune.init.ModItems.*;

public class MXTuneItemModelProvider extends ItemModelProvider
{
    public MXTuneItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper)
    {
        super(generator, Reference.MOD_ID, existingFileHelper);
    }

    @Nonnull
    @Override
    public String getName()
    {
        return Reference.MOD_NAME + "ItemModels";
    }

    @Override
    protected void registerModels()
    {
        withExistingParent(MUSIC_VENUE_TOOL.getId().getPath(), mcLoc("generated"))
                .texture("layer0", "item/music_venue_tool");

        withExistingParent(MUSIC_PAPER.getId().getPath(), mcLoc("generated"))
                .texture("layer0", "item/music_paper");

        withExistingParent(MUSIC_VENUE_INFO.getId().getPath(), mcLoc("generated"))
                .texture("layer0", "item/music_venue_info");

        withExistingParent(WRENCH.getId().getPath(), mcLoc("generated"))
                .texture("layer0", "item/wrench");

        {
            // AGE here in this context represents the percentage of a maximum number of days sheet music is viable.
            // At 0% the sheet music is considered unusable. 1-20% is well-worn. 21-50% is used. 51%+ like new.
            final int maxAge = 50;
            final int[] ages = {0, 1, 20, maxAge};
            ItemModelBuilder parentModel = withExistingParent(SHEET_MUSIC.getId().getPath(), mcLoc("generated"))
                    .texture("layer0", "item/sheet_music_age" + maxAge);

            Arrays.stream(ages)
                .mapToObj(index ->
                    {
                        final ItemModelBuilder subModel = withExistingParent(SHEET_MUSIC.getId().toString() + "_age" + index, mcLoc(SHEET_MUSIC.getId().toString()))
                            .texture("layer0", "item/" + SHEET_MUSIC.getId().getPath() + "_age" + index);

                        return Pair.of(index, subModel);
                    })
                .forEachOrdered(child ->
                    parentModel
                        .override()
                        .predicate(SheetMusicAgePropertyGetter.NAME, child.getKey())
                        .model(child.getValue())
                        .end()
            );
        }

        {
            // AGE here in this context represents the percentage of a maximum number of days music score is viable.
            // At 0% the music score is considered unusable. 1-20% is well-worn. 21-50% is used. 51%+ like new.
            final int maxAge = 50;
            final int[] ages = {0, 1, 20, maxAge};
            ItemModelBuilder parentModel = withExistingParent(MUSIC_SCORE.getId().getPath(), mcLoc("generated"))
                .texture("layer0", "item/music_score_age" + maxAge);

            Arrays.stream(ages)
                .mapToObj(index ->
                      {
                          final ItemModelBuilder subModel = withExistingParent(MUSIC_SCORE.getId().toString() + "_age" + index, mcLoc(MUSIC_SCORE.getId().toString()))
                              .texture("layer0", "item/" + MUSIC_SCORE.getId().getPath() + "_age" + index);

                          return Pair.of(index, subModel);
                      })
                .forEachOrdered(child ->
                    parentModel
                            .override()
                            .predicate(MusicScoreAgePropertyGetter.NAME, child.getKey())
                            .model(child.getValue())
                            .end()
           );
        }

        {
            final int maxStage = 80;
            final int[] stages = {0, 20, 40, 60, maxStage};
            ItemModelBuilder parentModel = withExistingParent(SCRAP_ITEM.getId().getPath(), mcLoc("generated"))
                    .texture("layer0", "item/scrap_item_stage" + maxStage);

            Arrays.stream(stages)
                .mapToObj(index ->
                {
                    final ItemModelBuilder subModel = withExistingParent(SCRAP_ITEM.getId().toString() + "_stage" + index, mcLoc(SCRAP_ITEM.getId().toString()))
                        .texture("layer0", "item/" + SCRAP_ITEM.getId().getPath() + "_stage" + index);

                    return Pair.of(index, subModel);
                })
                .forEachOrdered(child ->
                    parentModel
                        .override()
                        .predicate(ScrapAnimationPropertyGetter.NAME, child.getKey())
                        .model(child.getValue())
                        .end()
            );
        }

        {
            final int maxState = 6;
            final int[] stages = {0, 1, 2, 4, 5, maxState};
            ItemModelBuilder parentModel = withExistingParent(PLACARD_ITEM.getId().getPath(), mcLoc("generated"))
                    .texture("layer0", "item/placard_item_state" + maxState);

            Arrays.stream(stages)
                    .mapToObj(index ->
                              {
                                  final ItemModelBuilder subModel = withExistingParent(PLACARD_ITEM.getId().toString() + "_state" + index, mcLoc(PLACARD_ITEM.getId().toString()))
                                          .texture("layer0", "item/" + PLACARD_ITEM.getId().getPath() + "_state" + index);

                                  return Pair.of(index, subModel);
                              })
                    .forEachOrdered(child ->
                                            parentModel
                                                    .override()
                                                    .predicate(PlacardPropertyGetter.NAME, child.getKey())
                                                    .model(child.getValue())
                                                    .end()
                                   );
        }

        {
            ItemModelBuilder parentModel = withExistingParent(MULTI_INST.getId().getPath(), mcLoc("generated"));
            SoundFontProxyManager.getProxies().stream()
                    .map(proxy ->
                              {
                                  final ItemModelBuilder subModel = withExistingParent(proxy.id, mcLoc("generated"))
                                          .texture("layer0", "item/" + proxy.id);

                                  return Pair.of(proxy.index, subModel);
                              })
                    .forEachOrdered(child ->
                                            parentModel
                                                    .override()
                                                    .predicate(MultiInstModelPropertyGetter.NAME, child.getKey())
                                                    .model(child.getValue())
                                                    .end()
                                   );
        }
    }
}
