package jadx.dex.visitors.regions;

import jadx.dex.instructions.InsnType;
import jadx.dex.instructions.args.ArgType;
import jadx.dex.nodes.BlockNode;
import jadx.dex.nodes.IContainer;
import jadx.dex.nodes.InsnNode;
import jadx.dex.nodes.MethodNode;
import jadx.dex.regions.Region;
import jadx.dex.visitors.AbstractVisitor;
import jadx.utils.exceptions.JadxException;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostRegionVisitor extends AbstractVisitor {
	private final static Logger LOG = LoggerFactory.getLogger(PostRegionVisitor.class);

	@Override
	public void visit(MethodNode mth) throws JadxException {
		if (mth.isNoCode() || mth.getRegion() == null)
			return;

		DepthRegionTraverser.traverse(mth, new MarkTryCatchRegions(mth), mth.getRegion());

		DepthRegionTraverser.traverse(mth, new FinishRegions(), mth.getRegion());

		// removeReturn(mth);
	}

	/**
	 * Remove useless return at end
	 */
	private void removeReturn(MethodNode mth) {
		if (!mth.getMethodInfo().getReturnType().equals(ArgType.VOID))
			return;

		if (!(mth.getRegion() instanceof Region))
			return;

		Region rootRegion = (Region) mth.getRegion();
		if (rootRegion.getSubBlocks().isEmpty())
			return;

		IContainer lastCont = rootRegion.getSubBlocks().get(rootRegion.getSubBlocks().size() - 1);
		if (lastCont instanceof BlockNode) {
			BlockNode lastBlock = (BlockNode) lastCont;
			List<InsnNode> insns = lastBlock.getInstructions();
			int last = insns.size() - 1;
			if (last >= 0
					&& insns.get(last).getType() == InsnType.RETURN
					&& insns.get(last).getArgsCount() == 0) {
				insns.remove(last);
			}
		}
	}
}