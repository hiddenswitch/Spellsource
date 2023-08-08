import * as WorkspaceUtils from "../workspace-utils";
import Blockly from "blockly";
import { beforeAll } from "@jest/globals";
import { getAllBlockJson, getAllIcons } from "../fs-utils";
import { keyBy } from "lodash";
import { ContextType } from "react";
import { BlocklyDataContext } from "../../pages/card-editor";
import * as BlocklyMiscUtils from "../blockly-misc-utils";
import * as BlocklyRegister from "../blockly-register";

function expectConversion(str, json) {
  const xml = Blockly.utils.xml.textToDom(str);

  expect(WorkspaceUtils.xmlToCardScript(xml)).toEqual(json);
}

describe("WorkspaceUtils", () => {
  beforeAll(async () => {
    const allBlocks = await getAllBlockJson();
    const blocksByType = keyBy(allBlocks, (block) => block.type);
    const allIcons = await getAllIcons();

    const data: ContextType<typeof BlocklyDataContext> = {
      allBlocks,
      blocksByType,
      allIcons,
      userId: "",
      myCards: [],
      allArt: [],
      classes: {},
    };
    BlocklyRegister.registerAll();
    BlocklyMiscUtils.initBlocks(data);
    BlocklyMiscUtils.initHeroClassColors(data);
    // BlocklyMiscUtils.initCardBlocks(data);
  });

  it("converts correctly", () => {
    const json = {
      name: "Name",
      baseManaCost: 4,
      description: "Description",
      spell: { class: "DamageSpell", value: 4, target: "ENEMY_MINIONS" },
    };
    const xml = `<xml>
    <block type="Starter_SPELL" id="zvVTaR5UEE1iMnWZ7-+K" x="65" y="40">
        <field name="name">Name</field>
        <field name="baseManaCost">4</field>
        <field name="description">Description</field>
        <value name="spell">
            <block type="Spell_DamageSpell2" id="ac%L]T-#e*Zyl+oyr6K.">
                <field name="class">DamageSpell</field>
                <value name="value">
                    <block type="ValueProvider_int" id="X%ye1igF^q(l^W3MHYCZ">
                        <field name="int">4</field>
                        <data>{int}</data>
                    </block>
                </value>
                <value name="target">
                    <block type="EntityReference_ENEMY_MINIONS" id="K|^UPm_!pgjUofr*]?@V">
                        <data>ENEMY_MINIONS</data>
                    </block>
                </value>
            </block>
        </value>
    </block></xml>`;

    expectConversion(xml, json);
  });

  it("converts correctly", () => {
    const json = {
      type: "MINION",
      set: "CUSTOM",
      fileFormatVersion: 1,
      name: "test",
      baseAttack: 0,
      baseHp: 0,
      baseManaCost: 0,
      description: "description",
      rarity: "COMMON",
      battlecry: {
        condition: {
          class: "ComparisonCondition",
          operation: "GREATER_OR_EQUAL",
          value1: {
            class: "CardCountValueProvider",
            targetPlayer: "SELF",
          },
          value2: 3,
        },
        targetSelection: "NONE",
        spell: {
          class: "DamageSpell",
          target: "ENEMY_MINIONS",
          value: 2,
        },
      },
      attributes: {
        BATTLECRY: true,
      },
    };
    const xml = `<xml>
    <block type="Starter_MINION" id="icQZwG,}r{X_J%.DODjA" x="-112" y="148">
        <field name="type">MINION</field>
        <field name="fileFormatVersion">1</field>
        <field name="set">CUSTOM</field>
        <field name="name">test</field>
        <field name="baseAttack">0</field>
        <field name="baseHp">0</field>
        <field name="baseManaCost">0</field>
        <field name="description">description</field>
        <value name="rarity">
            <block type="Rarity_COMMON" id="tB{p%s$+vDb\`d+~z%#kr">
                <data>COMMON</data>
            </block>
        </value>
        <next>
            <block type="Property_opener2" id="dMum@aM*sVD!+3pS]t.m" x="-463" y="325">
        <value name="battlecry.condition">
            <block type="Condition_Comparison" id="K)[jk*DQG7mhxH6_AIN,">
                <field name="class">ComparisonCondition</field>
                <field name="operation">GREATER_OR_EQUAL</field>
                <value name="value1">
                    <block type="ValueProvider_CardCount" id="1mbj~zeuU[Q(iFJ!nRJp">
                        <field name="class">CardCountValueProvider</field>
                        <value name="targetPlayer">
                            <block type="TargetPlayer_SELF" id="Q,Xu/#qd8E(,79(%g-j/">
                                <data>SELF</data>
                            </block>
                        </value>
                    </block>
                </value>
                <value name="value2">
                    <block type="ValueProvider_int" id="f8*DZQyCS@q#z#]V6aEH">
                        <field name="int">3</field>
                        <data>{int}</data>
                    </block>
                </value>
            </block>
        </value>
        <value name="battlecry.targetSelection">
            <block type="TargetSelection_NONE" id="$6i8y!{uyPFPQbaDIZGo">
                <data>NONE</data>
            </block>
        </value>
        <value name="battlecry.spell">
            <block type="Spell_DamageSpell2" id="7YenHRqYd.),y9qN;-t:">
                <field name="class">DamageSpell</field>
                <value name="value">
                    <block type="ValueProvider_int" id=")U8!a/oH+tXlAAB4HmMs">
                        <field name="int">2</field>
                        <data>{int}</data>
                    </block>
                </value>
                <value name="target">
                    <block type="EntityReference_ENEMY_MINIONS" id="e:mv3{s)s4+PqgeW6[Z3">
                        <data>ENEMY_MINIONS</data>
                    </block>
                </value>
            </block>
        </value>
        <data>BLOCKLY_EXTEND_PREVIOUS</data>
    </block></next>
    </block>
    
</xml>`;

    expectConversion(xml, json);
  });
});
