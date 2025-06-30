package net.IneiTsuki.regen.block.custom;

import com.mojang.serialization.MapCodec;
import net.IneiTsuki.regen.block.entity.ModBlockEntities;
import net.IneiTsuki.regen.block.entity.SpellInscriberBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * A custom block that serves as a magical inscriber, storing and managing spells through interaction with a BlockEntity.
 * It opens a GUI on interaction and supports a rotated bounding box and ticking behavior.
 */
public class SpellInscriberBlock extends BlockWithEntity {

    /**
     * Codec for data-driven block instantiation.
     */
    public static final MapCodec<SpellInscriberBlock> CODEC = createCodec(SpellInscriberBlock::new);

    /**
     * Custom voxel shape defining the hitbox and visual bounds of the block.
     */
    private static final VoxelShape SHAPE = Block.createCuboidShape(-8, 0, 0, 24, 16, 16);

    /**
     * Directional property used to track which horizontal direction the block is facing.
     */
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

    /**
     * Constructs the SpellInscriberBlock with the given settings.
     *
     * @param settings The block settings (e.g., strength, material).
     */
    public SpellInscriberBlock(Settings settings) {
        super(settings);
    }

    /**
     * Returns the shape of the block based on its facing direction for visual and collision purposes.
     *
     * @param state   The current block state.
     * @param world   The world the block is in.
     * @param pos     The position of the block.
     * @param context The shape context.
     * @return The VoxelShape representing the block's outline.
     */
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Direction facing = state.get(FACING);
        return rotateShape(facing, SHAPE);
    }

    /**
     * Rotates the block shape based on its facing direction.
     *
     * @param direction The direction the block is facing.
     * @param shape     The original voxel shape.
     * @return The rotated voxel shape.
     */
    private VoxelShape rotateShape(Direction direction, VoxelShape shape) {
        VoxelShape[] buffer = new VoxelShape[]{shape, VoxelShapes.empty()};

        shape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
            switch (direction) {
                case NORTH -> buffer[1] = VoxelShapes.union(buffer[1],
                        VoxelShapes.cuboid(minX, minY, minZ, maxX, maxY, maxZ));
                case SOUTH -> buffer[1] = VoxelShapes.union(buffer[1],
                        VoxelShapes.cuboid(1 - maxX, minY, 1 - maxZ, 1 - minX, maxY, 1 - minZ));
                case WEST -> buffer[1] = VoxelShapes.union(buffer[1],
                        VoxelShapes.cuboid(minZ, minY, 1 - maxX, maxZ, maxY, 1 - minX));
                case EAST -> buffer[1] = VoxelShapes.union(buffer[1],
                        VoxelShapes.cuboid(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX));
            }
        });

        return buffer[1];
    }

    /**
     * Returns the codec used to serialize this block in data packs.
     *
     * @return The block's codec.
     */
    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    /**
     * Determines the initial block state upon placement, setting the facing based on the player's direction.
     *
     * @param ctx The item placement context.
     * @return The block state to place.
     */
    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing());
    }

    /**
     * Appends the block's properties to the state manager for use in block states.
     *
     * @param builder The state manager builder.
     */
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /**
     * Specifies that this block should be rendered using a model, not invisible or animated types.
     *
     * @param state The block state.
     * @return The render type.
     */
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    /**
     * Called when a player right-clicks the block.
     * If on the server, it will attempt to open the associated screen handler.
     *
     * @param state  The block state.
     * @param world  The world the block is in.
     * @param pos    The position of the block.
     * @param player The player interacting with the block.
     * @param hit    The raycast result of the interaction.
     * @return ActionResult.SUCCESS on interaction.
     */
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                              PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            NamedScreenHandlerFactory screenFactory = state.createScreenHandlerFactory(world, pos);
            if (screenFactory != null) {
                player.openHandledScreen(screenFactory);
            }
        }
        return ActionResult.SUCCESS;
    }

    /**
     * Creates a new block entity instance for this block type.
     *
     * @param pos   The position of the block.
     * @param state The current block state.
     * @return A new instance of {@link SpellInscriberBlockEntity}.
     */
    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SpellInscriberBlockEntity(pos, state);
    }

    /**
     * Provides the server-side ticker for the block entity to perform ticking logic.
     *
     * @param world The world the block is in.
     * @param state The current block state.
     * @param type  The expected block entity type.
     * @return A ticker if the block is on the server, or null otherwise.
     */
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient) {
            return null;
        }
        return (world1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof SpellInscriberBlockEntity entity) {
                entity.tick(world1, pos, state1);
            }
        };
    }

    /**
     * Creates a screen handler factory to open the block's UI.
     *
     * @param state The current block state.
     * @param world The world the block is in.
     * @param pos   The position of the block.
     * @return A {@link NamedScreenHandlerFactory} to handle UI creation, or null if none.
     */
    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state,
                                                                World world,
                                                                BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity instanceof SpellInscriberBlockEntity ? (SpellInscriberBlockEntity) blockEntity : null;
    }
}
