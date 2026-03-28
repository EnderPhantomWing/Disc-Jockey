package semmiedev.disc_jockey;

import java.util.HashMap;
import java.util.List;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

public record Note(NoteBlockInstrument instrument, byte note) {
    public static final HashMap<NoteBlockInstrument, Block> INSTRUMENT_BLOCKS = new HashMap<>();

//    static List<Block> copperBlock = List.of(
//            Blocks.COPPER_BLOCK,
//            Blocks.CHISELED_COPPER,
//            Blocks.CUT_COPPER,
//            Blocks.CUT_COPPER_STAIRS,
//            Blocks.CUT_COPPER_SLAB
//    );
//    static List<Block> exposedCopperBlock = List.of(
//            Blocks.EXPOSED_COPPER,
//            Blocks.EXPOSED_CHISELED_COPPER,
//            Blocks.EXPOSED_CUT_COPPER,
//            Blocks.EXPOSED_CUT_COPPER_STAIRS,
//            Blocks.EXPOSED_CUT_COPPER_SLAB
//    );
//    static List<Block> weatheredCopperBlock = List.of(
//            Blocks.WEATHERED_COPPER,
//            Blocks.WEATHERED_CHISELED_COPPER,
//            Blocks.WEATHERED_CUT_COPPER,
//            Blocks.WEATHERED_CUT_COPPER_STAIRS,
//            Blocks.WEATHERED_CUT_COPPER_SLAB
//    );
//    static List<Block> oxidizedCopperBlock = List.of(
//            Blocks.OXIDIZED_COPPER,
//            Blocks.OXIDIZED_CHISELED_COPPER,
//            Blocks.OXIDIZED_CUT_COPPER,
//            Blocks.OXIDIZED_CUT_COPPER_STAIRS,
//            Blocks.OXIDIZED_CUT_COPPER_SLAB
//    );

    public static final byte LAYER_SHIFT = Short.SIZE;
    public static final byte INSTRUMENT_SHIFT = Short.SIZE * 2;
    public static final byte NOTE_SHIFT = Short.SIZE * 2 + Byte.SIZE;

    public static final NoteBlockInstrument[] INSTRUMENTS = new NoteBlockInstrument[]{
            NoteBlockInstrument.HARP,
            NoteBlockInstrument.BASS,
            NoteBlockInstrument.BASEDRUM,
            NoteBlockInstrument.SNARE,
            NoteBlockInstrument.HAT,
            NoteBlockInstrument.GUITAR,
            NoteBlockInstrument.FLUTE,
            NoteBlockInstrument.BELL,
            NoteBlockInstrument.CHIME,
            NoteBlockInstrument.XYLOPHONE,
            NoteBlockInstrument.IRON_XYLOPHONE,
            NoteBlockInstrument.COW_BELL,
            NoteBlockInstrument.DIDGERIDOO,
            NoteBlockInstrument.BIT,
            NoteBlockInstrument.BANJO,
            NoteBlockInstrument.PLING,
            NoteBlockInstrument.TRUMPET,
            NoteBlockInstrument.TRUMPET_EXPOSED,
            NoteBlockInstrument.TRUMPET_WEATHERED,
            NoteBlockInstrument.TRUMPET_OXIDIZED
    };

    static {
        INSTRUMENT_BLOCKS.put(NoteBlockInstrument.HARP, Blocks.AIR);
        INSTRUMENT_BLOCKS.put(NoteBlockInstrument.BASEDRUM, Blocks.STONE);
        INSTRUMENT_BLOCKS.put(NoteBlockInstrument.SNARE, Blocks.SAND);
        INSTRUMENT_BLOCKS.put(NoteBlockInstrument.HAT, Blocks.GLASS);
        INSTRUMENT_BLOCKS.put(NoteBlockInstrument.BASS, Blocks.OAK_PLANKS);
        INSTRUMENT_BLOCKS.put(NoteBlockInstrument.FLUTE, Blocks.CLAY);
        INSTRUMENT_BLOCKS.put(NoteBlockInstrument.BELL, Blocks.GOLD_BLOCK);
        INSTRUMENT_BLOCKS.put(NoteBlockInstrument.GUITAR, Blocks.WHITE_WOOL);
        INSTRUMENT_BLOCKS.put(NoteBlockInstrument.CHIME, Blocks.PACKED_ICE);
        INSTRUMENT_BLOCKS.put(NoteBlockInstrument.XYLOPHONE, Blocks.BONE_BLOCK);
        INSTRUMENT_BLOCKS.put(NoteBlockInstrument.IRON_XYLOPHONE, Blocks.IRON_BLOCK);
        INSTRUMENT_BLOCKS.put(NoteBlockInstrument.COW_BELL, Blocks.SOUL_SAND);
        INSTRUMENT_BLOCKS.put(NoteBlockInstrument.DIDGERIDOO, Blocks.PUMPKIN);
        INSTRUMENT_BLOCKS.put(NoteBlockInstrument.BIT, Blocks.EMERALD_BLOCK);
        INSTRUMENT_BLOCKS.put(NoteBlockInstrument.BANJO, Blocks.HAY_BLOCK);
        INSTRUMENT_BLOCKS.put(NoteBlockInstrument.PLING, Blocks.GLOWSTONE);
        INSTRUMENT_BLOCKS.put(NoteBlockInstrument.TRUMPET, Blocks.COPPER_BLOCK);
        INSTRUMENT_BLOCKS.put(NoteBlockInstrument.TRUMPET_EXPOSED, Blocks.EXPOSED_COPPER);
        INSTRUMENT_BLOCKS.put(NoteBlockInstrument.TRUMPET_WEATHERED, Blocks.WEATHERED_COPPER);
        INSTRUMENT_BLOCKS.put(NoteBlockInstrument.TRUMPET_OXIDIZED, Blocks.OXIDIZED_COPPER);
    }
}
