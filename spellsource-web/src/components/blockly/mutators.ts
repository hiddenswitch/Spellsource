import { Block, BlockSvg, Workspace } from "blockly";

type PartialBlock = Partial<Block>;

interface IPartialBlock extends PartialBlock {}

export abstract class MutatorMixin<
  Mixin,
  State extends object,
  ContainerBlock extends Block = Block,
  MutatedBlock = Mixin & Block
> implements IPartialBlock
{
  abstract mutationToDom(this: MutatedBlock): Element;

  abstract domToMutation(this: MutatedBlock, xmlElement: Element): void;

  abstract saveExtraState(this: MutatedBlock): State;

  abstract loadExtraState(this: MutatedBlock, state: State);

  abstract decompose(this: MutatedBlock, workspace: Workspace): ContainerBlock;

  abstract compose(this: MutatedBlock, containerBlock: ContainerBlock): void;

  abstract rebuildShape_(this: MutatedBlock): void;
}

export type MutatorFn<T> = (this: T & (Block | BlockSvg)) => void;
