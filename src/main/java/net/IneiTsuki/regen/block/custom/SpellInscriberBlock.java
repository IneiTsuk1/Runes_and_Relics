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

public class SpellInscriberBlock extends BlockWithEntity {

    public static final MapCodec<SpellInscriberBlock> CODEC = createCodec(SpellInscriberBlock::new);

    // Custom bounding box shape extending beyond the usual block bounds
    private static final VoxelShape SHAPE = Block.createCuboidShape(-8, 0, 0, 24, 16, 16);

    // Facing property to track block orientation (horizontal directions only)
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

    public SpellInscriberBlock(Settings settings) {
        super(settings);
    }

    /**
     * Returns the outline shape of the block based on its facing direction.
     */
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Direction facing = state.get(FACING);
        return rotateShape(facing, SHAPE);
    }

    /**
     * Rotates the shape according to the given direction.
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

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    /**
     * Determines the block state upon placement, facing the player.
     */
    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing());
    }

    /**
     * Adds the facing property to the block's state manager.
     */
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /**
     * The block is rendered using a model.
     */
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    /**
     * Called when the player interacts with the block.
     * Opens the screen handler if on the server.
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
     * Creates the block entity associated with this block.
     */
    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SpellInscriberBlockEntity(pos, state);
    }

    /**
     * Returns the ticker for the block entity if on the server.
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
     * Provides the screen handler factory from the block entity.
     */
    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state,
                                                                World world,
                                                                BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity instanceof SpellInscriberBlockEntity ? (SpellInscriberBlockEntity) blockEntity : null;
    }
}
