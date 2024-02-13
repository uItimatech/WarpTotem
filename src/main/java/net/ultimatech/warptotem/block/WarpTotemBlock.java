package net.ultimatech.warptotem.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import net.ultimatech.warptotem.block.entity.WarpTotemBlockEntity;
import net.ultimatech.warptotem.item.WTItems;
import net.ultimatech.warptotem.sound.WTSounds;
import org.jetbrains.annotations.Nullable;

public final class WarpTotemBlock extends BlockWithEntity implements BlockEntityProvider {

    public WarpTotemBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    public static final MapCodec<WarpTotemBlock> CODEC = createCodec(WarpTotemBlock::new);

    @Override
    public MapCodec<WarpTotemBlock> getCodec() {
        return CODEC;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new WarpTotemBlockEntity(blockPos, blockState);
    }




    // ----- PROPERTIES ----- //
    public static final DirectionProperty FACING = DirectionProperty.of("facing", Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
    public static final BooleanProperty LODESTONE_TRACKED = BooleanProperty.of("lodestone_tracked");
    public static final BooleanProperty LODESTONE_AVAILABLE = BooleanProperty.of("lodestone_available");
    public static final BooleanProperty TRIGGERED = BooleanProperty.of("triggered");
    public static final String LODESTONE_POS_KEY = "LodestonePos";
    private static final int CHECK_TICKS = 5;

    private static final int MAX_REDSTONE_WARP_DISTANCE = 5;

    public static final VoxelShape BASE_SHAPE = Block.createCuboidShape(0,0,0,16,2,16);
    public static final VoxelShape TOP_SHAPE = Block.createCuboidShape(0,14,0,16,16,16);
    public static final VoxelShape MIDDLE_SHAPE = Block.createCuboidShape(2,2,2,14,14,14);
    public static final VoxelShape SHAPE = VoxelShapes.union(BASE_SHAPE, TOP_SHAPE, MIDDLE_SHAPE);


    public void onPlaced(World world, BlockPos blockPos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, blockPos, state, placer, itemStack);
        world.scheduleBlockTick(blockPos, this, CHECK_TICKS);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos blockPos, Random random) {
        if (world.getBlockEntity(blockPos.up()) != null) {
            DecoratedPotBlockEntity potBlockEntity = (world.getBlockEntity(blockPos.up()).getClass() == DecoratedPotBlockEntity.class) ? (DecoratedPotBlockEntity) world.getBlockEntity(blockPos.up()) : null;
            ItemStack itemStack = potBlockEntity != null ? potBlockEntity.getStack() : ItemStack.EMPTY;
            world.setBlockState(blockPos, state.with(LODESTONE_AVAILABLE, !(isLodestoneObstructed(world, blockPos) || !(itemStack.itemMatches(Items.ENDER_PEARL.getRegistryEntry())))));
        } else {
            world.setBlockState(blockPos, state.with(LODESTONE_AVAILABLE, false));
        }
        world.scheduleBlockTick(blockPos, this, CHECK_TICKS);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos blockPos, Block sourceBlock, BlockPos sourcePos, boolean notify) {

        boolean isPowered = world.isReceivingRedstonePower(blockPos) || world.isReceivingRedstonePower(blockPos.up());
        boolean isTriggered = state.get(TRIGGERED);

        BlockState blockState = world.getBlockState(blockPos);

        if (!world.isClient) {

            BlockEntity associatedBlockEntity = world.getBlockEntity(blockPos);
            PlayerEntity nearestPlayer = world.getClosestPlayer(blockPos.getX(), blockPos.getY(), blockPos.getZ(), MAX_REDSTONE_WARP_DISTANCE, false);

            if (associatedBlockEntity instanceof WarpTotemBlockEntity FFWarpTotemBlockEntity && nearestPlayer != null  && isPowered && !isTriggered) {

                    if (world.getBlockState(blockPos.up()).getBlock().getClass() == DecoratedPotBlock.class) {

                        DecoratedPotBlockEntity potBlockEntity = (DecoratedPotBlockEntity) world.getBlockEntity(blockPos.up());
                        ItemStack oldItemStack = potBlockEntity.getStack();
                        ItemStack newItemStack = oldItemStack.copyWithCount(oldItemStack.getCount() - 1);

                        if (oldItemStack.itemMatches(Items.ENDER_PEARL.getRegistryEntry()) && blockState.get(LODESTONE_TRACKED)) {

                            if (!isLodestoneObstructed(world, blockPos)) {

                                potBlockEntity.setStack(0, newItemStack);

                                if (world instanceof ServerWorld serverWorld) {
                                    serverWorld.spawnParticles(ParticleTypes.WITCH, (double) blockPos.getX() + 0.5, (double) blockPos.getY() + 2.05, (double) blockPos.getZ() + 0.5, 10, 0.05, 0.25, 0.05, 0);
                                    serverWorld.spawnParticles(ParticleTypes.GUST, (double) blockPos.getX() + 0.5, (double) blockPos.getY() + 2.2, (double) blockPos.getZ() + 0.5, 1, 0, 0, 0, 0.05);

                                    serverWorld.spawnParticles(ParticleTypes.WITCH, (double) nearestPlayer.getPos().getX(), (double) nearestPlayer.getPos().getY() + 0.1, (double) nearestPlayer.getPos().getZ(), 6, 0.2, 0.3, 0.2, 0.1);
                                    serverWorld.spawnParticles(ParticleTypes.POOF, (double) nearestPlayer.getPos().getX(), (double) nearestPlayer.getPos().getY() + 1, (double) nearestPlayer.getPos().getZ(), 20, 0.22, 0.5, 0.22, 0.005);
                                }

                                world.playSound(null, blockPos, SoundEvents.ITEM_TOTEM_USE, SoundCategory.BLOCKS, 0.4f, 0.7f);
                                world.playSound(null, blockPos, WTSounds.WARP_TOTEM_ENABLE, SoundCategory.BLOCKS, 1.25f, 1f);
                                world.playSound(null, blockPos, SoundEvents.ENTITY_ENDER_EYE_DEATH, SoundCategory.BLOCKS, 1f, 1f);

                                teleportPlayerToLodestone(nearestPlayer, world, blockPos);
                            } else {
                                world.playSound(null, blockPos, SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.BLOCKS, 1.25f, 1f);
                            }
                        }
                    }

                    world.emitGameEvent(nearestPlayer, GameEvent.BLOCK_CHANGE, blockPos);
            } else if (!isPowered && isTriggered) {
                world.setBlockState(blockPos, state.with(TRIGGERED, Boolean.valueOf(false)), Block.NOTIFY_LISTENERS);
            }
        }
    }


    @Override
    public ItemStack getPickStack(WorldView world, BlockPos blockPos, BlockState blockState) {
        return WTItems.WARP_TOTEM.getDefaultStack();
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState()
                .with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())
                .with(LODESTONE_TRACKED,false)
                .with(LODESTONE_AVAILABLE, false)
                .with(TRIGGERED, false);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, LODESTONE_TRACKED, LODESTONE_AVAILABLE, TRIGGERED);
    }

    @Override
    public ActionResult onUse(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockHitResult hit) {

        BlockEntity associatedBlockEntity = world.getBlockEntity(blockPos);

        if (associatedBlockEntity instanceof WarpTotemBlockEntity FFWarpTotemBlockEntity) {
            if (world.isClient) {
                return ActionResult.CONSUME;

            } else {

                ItemStack itemInHand = player.getStackInHand(hand);

                if ((world.getBlockState(blockPos.up()).getBlock().getClass() == DecoratedPotBlock.class) && itemInHand.getItem() != Items.COMPASS) {

                    DecoratedPotBlockEntity potBlockEntity = (DecoratedPotBlockEntity) world.getBlockEntity(blockPos.up());
                    ItemStack oldItemStack = potBlockEntity.getStack();
                    ItemStack newItemStack = oldItemStack.copyWithCount(oldItemStack.getCount() - 1);

                    if (oldItemStack.itemMatches(Items.ENDER_PEARL.getRegistryEntry()) && player != null && blockState.get(LODESTONE_TRACKED)) {

                        if (!isLodestoneObstructed(world, blockPos)) {

                            potBlockEntity.setStack(0, newItemStack);

                            if (world instanceof ServerWorld serverWorld) {
                                serverWorld.spawnParticles(ParticleTypes.WITCH, (double) blockPos.getX() + 0.5, (double) blockPos.getY() + 2.05, (double) blockPos.getZ() + 0.5, 10, 0.05, 0.25, 0.05, 0);
                                serverWorld.spawnParticles(ParticleTypes.GUST, (double) blockPos.getX() + 0.5, (double) blockPos.getY() + 2.2, (double) blockPos.getZ() + 0.5, 1, 0, 0, 0, 0.05);

                                serverWorld.spawnParticles(ParticleTypes.WITCH, (double) player.getPos().getX(), (double) player.getPos().getY() + 0.1, (double) player.getPos().getZ(), 6, 0.2, 0.3, 0.2, 0.1);
                                serverWorld.spawnParticles(ParticleTypes.POOF, (double) player.getPos().getX(), (double) player.getPos().getY() + 1, (double) player.getPos().getZ(), 20, 0.22, 0.5, 0.22, 0.005);
                            }

                            world.playSound(null, blockPos, SoundEvents.ITEM_TOTEM_USE, SoundCategory.BLOCKS, 0.4f, 0.7f);
                            world.playSound(null, blockPos, WTSounds.WARP_TOTEM_ENABLE, SoundCategory.BLOCKS, 1.25f, 1f);
                            world.playSound(null, blockPos, SoundEvents.ENTITY_ENDER_EYE_DEATH, SoundCategory.BLOCKS, 1f, 1f);

                            teleportPlayerToLodestone(player, world, blockPos);
                        } else {
                            world.playSound(null, blockPos, SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.BLOCKS, 1.25f, 1f);
                        }
                    }
                }

                if (itemInHand.getItem() == Items.COMPASS && itemInHand.getNbt() != null && isDifferent(itemInHand, world, blockPos)) {
                    updateLodestoneInfo(itemInHand, blockState, blockPos, world);

                    world.playSound(null, blockPos, SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.BLOCKS, 1.25f, 1f);

                    if (world instanceof ServerWorld serverWorld) {
                        if (world.getBlockState(blockPos.up()).getBlock().getClass() == DecoratedPotBlock.class) {
                            serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, (double) blockPos.getX() + 0.5, (double) blockPos.getY() + 2.1, (double) blockPos.getZ() + 0.5, 10, 0.05, 0.35, 0.05, 0);
                        } else {
                            serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, (double) blockPos.getX(), (double) blockPos.getY() + 0.5, (double) blockPos.getZ(), 6, 0.4, 0.4, 0.4, 0);
                        }
                    }
                }

                world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, blockPos);
                return ActionResult.SUCCESS;
            }
        } else {
            return ActionResult.PASS;
        }
    }

    public Void teleportPlayerToLodestone(PlayerEntity player, World world, BlockPos blockPos) {

        BlockPos lodestonePos = ((WarpTotemBlockEntity) world.getBlockEntity(blockPos)).getLodestonePos();

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        mutable.set(lodestonePos.getX(), lodestonePos.getY(), lodestonePos.getZ());

        Vec3d teleportTarget = new Vec3d(lodestonePos.getX(), lodestonePos.getY(), lodestonePos.getZ());

        if (teleportTarget != null) {
            player.teleport(teleportTarget.getX() + 0.5, teleportTarget.getY() + 1, teleportTarget.getZ() + 0.5);

            world.playSound(null, BlockPos.ofFloored(teleportTarget), SoundEvents.ITEM_TOTEM_USE, SoundCategory.BLOCKS, 0.4f, 0.7f);
            world.playSound(null, BlockPos.ofFloored(teleportTarget), WTSounds.WARP_TOTEM_ENABLE, SoundCategory.BLOCKS, 1.25f, 1f);

            if (world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.WITCH, (double) player.getPos().getX(), (double) player.getPos().getY() + 0.1, (double) player.getPos().getZ(), 6, 0.2, 0.3, 0.2, 0.1);
                serverWorld.spawnParticles(ParticleTypes.POOF, (double) player.getPos().getX(), (double) player.getPos().getY() + 1, (double) player.getPos().getZ(), 20, 0.22, 0.5, 0.22, 0.005);
            }

            ((PlayerEntity) player).incrementStat(Stats.USED.getOrCreateStat(Item.fromBlock(this)));
        }

        return null;
    }

    public boolean isLodestoneObstructed(World world, BlockPos blockPos) {

        BlockPos lodestonePos = ((WarpTotemBlockEntity) world.getBlockEntity(blockPos)).getLodestonePos();

        BlockState lodestoneBlockState = world.getBlockState(lodestonePos);

        if (lodestoneBlockState.getBlock() != Blocks.LODESTONE || !isBlockNonSolid(world, lodestonePos.up(1)) || !isBlockNonSolid(world, lodestonePos.up(2))) {
            return true;
        }

        return false;
    }

    // Returns true if the block has no collisions OR is a carpet / trapdoor / etc.
    public boolean isBlockNonSolid(World world, BlockPos blockPos) {
        return (world.getBlockState(blockPos).getCollisionShape(world, blockPos).isEmpty() || world.getBlockState(blockPos).getBlock() instanceof CarpetBlock || world.getBlockState(blockPos).getBlock() instanceof TrapdoorBlock || world.getBlockState(blockPos).getBlock() instanceof AbstractRedstoneGateBlock);
    }

    public boolean isDifferent(ItemStack itemInHand, World world, BlockPos blockPos) {

        BlockPos lodestonePos = ((WarpTotemBlockEntity) world.getBlockEntity(blockPos)).getLodestonePos();

        NbtCompound compassItemNbt = itemInHand.getNbt();

        if (compassItemNbt.getCompound(LODESTONE_POS_KEY).getInt("X") == lodestonePos.getX() && compassItemNbt.getCompound(LODESTONE_POS_KEY).getInt("Y") == lodestonePos.getY() && compassItemNbt.getCompound(LODESTONE_POS_KEY).getInt("Z") == lodestonePos.getZ()) {
            return false;
        }

        return true;
    }

    public void updateLodestoneInfo(ItemStack compassItem, BlockState blockState, BlockPos blockPos, World world) {

        assert compassItem.getNbt() != null;
        NbtCompound compassItemNbt = compassItem.getNbt();

        BlockEntity entityInfo = world.getBlockEntity(blockPos);
        if (entityInfo instanceof WarpTotemBlockEntity warpTotemBlockEntity) {
            warpTotemBlockEntity.setLodestonePos(new BlockPos(compassItemNbt.getCompound(LODESTONE_POS_KEY).getInt("X"), compassItemNbt.getCompound(LODESTONE_POS_KEY).getInt("Y"), compassItemNbt.getCompound(LODESTONE_POS_KEY).getInt("Z")));
        }

        world.setBlockState(blockPos, blockState.with(LODESTONE_TRACKED, true)); // Theoretically always set to true
    }





    // ----- RENDERING ----- //
    @Override
    public BlockRenderType getRenderType(BlockState blockState) {return BlockRenderType.MODEL;}

    @Override
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean hasSidedTransparency(BlockState state) {
        return true;
    }

    private VoxelShape getShape(BlockState state) {
        return SHAPE;
    }
    @Override
    public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.getShape(state);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.getShape(state);
    }
}
