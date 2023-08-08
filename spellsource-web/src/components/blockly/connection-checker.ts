import Blockly, { Connection } from "blockly";

export class SpellsourceConnectionChecker extends Blockly.ConnectionChecker {
  constructor() {
    super();
  }

  doTypeChecks(a: Connection, b: Connection): boolean {
    const allChecks = [...(a.getCheck() ?? []), ...(b.getCheck() ?? [])];

    if (allChecks.includes("Boolean") && allChecks.includes("ConditionDesc")) {
      return true;
    }

    return super.doTypeChecks(a, b);
  }
}
