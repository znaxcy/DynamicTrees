package com.ferreusveritas.dynamictrees.inspectors;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockGrowingLeaves;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.Block;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import net.minecraft.world.World;

public class NodeTransform implements INodeInspector {

	DynamicTree fromTree;
	DynamicTree toTree;
	
	public NodeTransform(DynamicTree fromTree, DynamicTree toTree) {
		this.fromTree = fromTree;
		this.toTree = toTree;
	}
	
	@Override
	public boolean run(World world, Block block, BlockPos pos, EnumFacing fromDir) {
		BlockBranch branch = TreeHelper.getBranch(block);
		
		if(branch != null && fromTree == branch.getTree()) {
			int radius = branch.getRadius(world, pos);
			if(radius > 0) {
				world.setBlock(pos.getX(), pos.getY(), pos.getZ(), toTree.getGrowingBranch(), toTree.getGrowingBranch().radiusToMeta(radius), 3);
				if(radius == 1) {
					transformSurroundingLeaves(world, pos);
				}
			}
		}

		return true;
	}

	@Override
	public boolean returnRun(World world, Block block, BlockPos pos, EnumFacing fromDir) {
		return false;
	}

	public void transformSurroundingLeaves(World world, BlockPos twigPos) {
		if (!world.isRemote) {
			for(BlockPos leavesPos : BlockPos.getAllInBox(twigPos.add(-3, -3, -3), twigPos.add(3, 3, 3))) {
				if(fromTree.getLeafClusterPoint(twigPos, leavesPos) != 0) {//We're only interested in where leaves could possibly be
					if(fromTree.isCompatibleGenericLeaves(world, leavesPos)) {
						int hydro = 2;
						IBlockState state = leavesPos.getBlockState(world);
						if(state.getBlock() instanceof BlockGrowingLeaves) {
							BlockGrowingLeaves growingLeaves = (BlockGrowingLeaves) state.getBlock();
							hydro = growingLeaves.getHydrationLevel(state);
						}
						toTree.getGrowingLeaves().setBlockToLeaves(world, toTree, leavesPos, hydro);
					}
				}
			}
		}
	}
	
}
