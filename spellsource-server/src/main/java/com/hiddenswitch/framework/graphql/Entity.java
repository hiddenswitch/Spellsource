package com.hiddenswitch.framework.graphql;


public class Entity implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String description;
    private String cardId;
    private CardType cardType;
    private EntityType entityType;
    private Rarity rarity;
    private EntityLocation location;
    private Art art;
    private int owner;
    private int boardPosition;
    private Integer attack;
    private Integer baseAttack;
    private Integer hp;
    private Integer baseHp;
    private Integer maxHp;
    private Integer armor;
    private Integer manaCost;
    private Integer baseManaCost;
    private Integer durability;
    private Integer spellDamage;
    private Integer overload;
    private Integer extraAttack;
    private int mana;
    private int maxMana;
    private int lockedMana;
    private boolean battlecry;
    private boolean cannotAttack;
    private boolean charge;
    private boolean chooseOne;
    private boolean collectible;
    private boolean combo;
    private boolean conditionMet;
    private boolean deathrattles;
    private boolean deflect;
    private boolean destroyed;
    private boolean discarded;
    private boolean divineShield;
    private boolean enraged;
    private boolean frozen;
    private boolean gameStarted;
    private boolean gold;
    private boolean hostsTrigger;
    private boolean immune;
    private boolean isStartingTurn;
    private boolean lifesteal;
    private boolean permanent;
    private boolean playable;
    private boolean poisonous;
    private boolean roasted;
    private boolean rush;
    private boolean silenced;
    private boolean stealth;
    private boolean summoningSickness;
    private boolean taunt;
    private boolean uncensored;
    private boolean underAura;
    private boolean untargetableBySpells;
    private boolean windfury;
    private Integer charges;
    private Integer countUntilCast;
    private Integer fires;
    private int host;
    private java.util.List<String> heroClasses;
    private String cardSet;
    private java.util.List<String> cardSets;
    private String enchantmentType;
    private java.util.List<String> tribes;
    private java.util.List<Tooltip> tooltips;
    private String note;

    public Entity() {
    }

    public Entity(int id, String name, String description, String cardId, CardType cardType, EntityType entityType, Rarity rarity, EntityLocation location, Art art, int owner, int boardPosition, Integer attack, Integer baseAttack, Integer hp, Integer baseHp, Integer maxHp, Integer armor, Integer manaCost, Integer baseManaCost, Integer durability, Integer spellDamage, Integer overload, Integer extraAttack, int mana, int maxMana, int lockedMana, boolean battlecry, boolean cannotAttack, boolean charge, boolean chooseOne, boolean collectible, boolean combo, boolean conditionMet, boolean deathrattles, boolean deflect, boolean destroyed, boolean discarded, boolean divineShield, boolean enraged, boolean frozen, boolean gameStarted, boolean gold, boolean hostsTrigger, boolean immune, boolean isStartingTurn, boolean lifesteal, boolean permanent, boolean playable, boolean poisonous, boolean roasted, boolean rush, boolean silenced, boolean stealth, boolean summoningSickness, boolean taunt, boolean uncensored, boolean underAura, boolean untargetableBySpells, boolean windfury, Integer charges, Integer countUntilCast, Integer fires, int host, java.util.List<String> heroClasses, String cardSet, java.util.List<String> cardSets, String enchantmentType, java.util.List<String> tribes, java.util.List<Tooltip> tooltips, String note) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.cardId = cardId;
        this.cardType = cardType;
        this.entityType = entityType;
        this.rarity = rarity;
        this.location = location;
        this.art = art;
        this.owner = owner;
        this.boardPosition = boardPosition;
        this.attack = attack;
        this.baseAttack = baseAttack;
        this.hp = hp;
        this.baseHp = baseHp;
        this.maxHp = maxHp;
        this.armor = armor;
        this.manaCost = manaCost;
        this.baseManaCost = baseManaCost;
        this.durability = durability;
        this.spellDamage = spellDamage;
        this.overload = overload;
        this.extraAttack = extraAttack;
        this.mana = mana;
        this.maxMana = maxMana;
        this.lockedMana = lockedMana;
        this.battlecry = battlecry;
        this.cannotAttack = cannotAttack;
        this.charge = charge;
        this.chooseOne = chooseOne;
        this.collectible = collectible;
        this.combo = combo;
        this.conditionMet = conditionMet;
        this.deathrattles = deathrattles;
        this.deflect = deflect;
        this.destroyed = destroyed;
        this.discarded = discarded;
        this.divineShield = divineShield;
        this.enraged = enraged;
        this.frozen = frozen;
        this.gameStarted = gameStarted;
        this.gold = gold;
        this.hostsTrigger = hostsTrigger;
        this.immune = immune;
        this.isStartingTurn = isStartingTurn;
        this.lifesteal = lifesteal;
        this.permanent = permanent;
        this.playable = playable;
        this.poisonous = poisonous;
        this.roasted = roasted;
        this.rush = rush;
        this.silenced = silenced;
        this.stealth = stealth;
        this.summoningSickness = summoningSickness;
        this.taunt = taunt;
        this.uncensored = uncensored;
        this.underAura = underAura;
        this.untargetableBySpells = untargetableBySpells;
        this.windfury = windfury;
        this.charges = charges;
        this.countUntilCast = countUntilCast;
        this.fires = fires;
        this.host = host;
        this.heroClasses = heroClasses;
        this.cardSet = cardSet;
        this.cardSets = cardSets;
        this.enchantmentType = enchantmentType;
        this.tribes = tribes;
        this.tooltips = tooltips;
        this.note = note;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getCardId() {
        return cardId;
    }
    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public CardType getCardType() {
        return cardType;
    }
    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public EntityType getEntityType() {
        return entityType;
    }
    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public Rarity getRarity() {
        return rarity;
    }
    public void setRarity(Rarity rarity) {
        this.rarity = rarity;
    }

    public EntityLocation getLocation() {
        return location;
    }
    public void setLocation(EntityLocation location) {
        this.location = location;
    }

    public Art getArt() {
        return art;
    }
    public void setArt(Art art) {
        this.art = art;
    }

    public int getOwner() {
        return owner;
    }
    public void setOwner(int owner) {
        this.owner = owner;
    }

    public int getBoardPosition() {
        return boardPosition;
    }
    public void setBoardPosition(int boardPosition) {
        this.boardPosition = boardPosition;
    }

    /**
     * stats
     */
    public Integer getAttack() {
        return attack;
    }
    /**
     * stats
     */
    public void setAttack(Integer attack) {
        this.attack = attack;
    }

    public Integer getBaseAttack() {
        return baseAttack;
    }
    public void setBaseAttack(Integer baseAttack) {
        this.baseAttack = baseAttack;
    }

    public Integer getHp() {
        return hp;
    }
    public void setHp(Integer hp) {
        this.hp = hp;
    }

    public Integer getBaseHp() {
        return baseHp;
    }
    public void setBaseHp(Integer baseHp) {
        this.baseHp = baseHp;
    }

    public Integer getMaxHp() {
        return maxHp;
    }
    public void setMaxHp(Integer maxHp) {
        this.maxHp = maxHp;
    }

    public Integer getArmor() {
        return armor;
    }
    public void setArmor(Integer armor) {
        this.armor = armor;
    }

    public Integer getManaCost() {
        return manaCost;
    }
    public void setManaCost(Integer manaCost) {
        this.manaCost = manaCost;
    }

    public Integer getBaseManaCost() {
        return baseManaCost;
    }
    public void setBaseManaCost(Integer baseManaCost) {
        this.baseManaCost = baseManaCost;
    }

    public Integer getDurability() {
        return durability;
    }
    public void setDurability(Integer durability) {
        this.durability = durability;
    }

    public Integer getSpellDamage() {
        return spellDamage;
    }
    public void setSpellDamage(Integer spellDamage) {
        this.spellDamage = spellDamage;
    }

    public Integer getOverload() {
        return overload;
    }
    public void setOverload(Integer overload) {
        this.overload = overload;
    }

    public Integer getExtraAttack() {
        return extraAttack;
    }
    public void setExtraAttack(Integer extraAttack) {
        this.extraAttack = extraAttack;
    }

    /**
     * player resource fields
     */
    public int getMana() {
        return mana;
    }
    /**
     * player resource fields
     */
    public void setMana(int mana) {
        this.mana = mana;
    }

    public int getMaxMana() {
        return maxMana;
    }
    public void setMaxMana(int maxMana) {
        this.maxMana = maxMana;
    }

    public int getLockedMana() {
        return lockedMana;
    }
    public void setLockedMana(int lockedMana) {
        this.lockedMana = lockedMana;
    }

    /**
     * boolean flags
     */
    public boolean getBattlecry() {
        return battlecry;
    }
    /**
     * boolean flags
     */
    public void setBattlecry(boolean battlecry) {
        this.battlecry = battlecry;
    }

    public boolean getCannotAttack() {
        return cannotAttack;
    }
    public void setCannotAttack(boolean cannotAttack) {
        this.cannotAttack = cannotAttack;
    }

    public boolean getCharge() {
        return charge;
    }
    public void setCharge(boolean charge) {
        this.charge = charge;
    }

    public boolean getChooseOne() {
        return chooseOne;
    }
    public void setChooseOne(boolean chooseOne) {
        this.chooseOne = chooseOne;
    }

    public boolean getCollectible() {
        return collectible;
    }
    public void setCollectible(boolean collectible) {
        this.collectible = collectible;
    }

    public boolean getCombo() {
        return combo;
    }
    public void setCombo(boolean combo) {
        this.combo = combo;
    }

    public boolean getConditionMet() {
        return conditionMet;
    }
    public void setConditionMet(boolean conditionMet) {
        this.conditionMet = conditionMet;
    }

    public boolean getDeathrattles() {
        return deathrattles;
    }
    public void setDeathrattles(boolean deathrattles) {
        this.deathrattles = deathrattles;
    }

    public boolean getDeflect() {
        return deflect;
    }
    public void setDeflect(boolean deflect) {
        this.deflect = deflect;
    }

    public boolean getDestroyed() {
        return destroyed;
    }
    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    public boolean getDiscarded() {
        return discarded;
    }
    public void setDiscarded(boolean discarded) {
        this.discarded = discarded;
    }

    public boolean getDivineShield() {
        return divineShield;
    }
    public void setDivineShield(boolean divineShield) {
        this.divineShield = divineShield;
    }

    public boolean getEnraged() {
        return enraged;
    }
    public void setEnraged(boolean enraged) {
        this.enraged = enraged;
    }

    public boolean getFrozen() {
        return frozen;
    }
    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public boolean getGameStarted() {
        return gameStarted;
    }
    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public boolean getGold() {
        return gold;
    }
    public void setGold(boolean gold) {
        this.gold = gold;
    }

    public boolean getHostsTrigger() {
        return hostsTrigger;
    }
    public void setHostsTrigger(boolean hostsTrigger) {
        this.hostsTrigger = hostsTrigger;
    }

    public boolean getImmune() {
        return immune;
    }
    public void setImmune(boolean immune) {
        this.immune = immune;
    }

    public boolean getIsStartingTurn() {
        return isStartingTurn;
    }
    public void setIsStartingTurn(boolean isStartingTurn) {
        this.isStartingTurn = isStartingTurn;
    }

    public boolean getLifesteal() {
        return lifesteal;
    }
    public void setLifesteal(boolean lifesteal) {
        this.lifesteal = lifesteal;
    }

    public boolean getPermanent() {
        return permanent;
    }
    public void setPermanent(boolean permanent) {
        this.permanent = permanent;
    }

    public boolean getPlayable() {
        return playable;
    }
    public void setPlayable(boolean playable) {
        this.playable = playable;
    }

    public boolean getPoisonous() {
        return poisonous;
    }
    public void setPoisonous(boolean poisonous) {
        this.poisonous = poisonous;
    }

    public boolean getRoasted() {
        return roasted;
    }
    public void setRoasted(boolean roasted) {
        this.roasted = roasted;
    }

    public boolean getRush() {
        return rush;
    }
    public void setRush(boolean rush) {
        this.rush = rush;
    }

    public boolean getSilenced() {
        return silenced;
    }
    public void setSilenced(boolean silenced) {
        this.silenced = silenced;
    }

    public boolean getStealth() {
        return stealth;
    }
    public void setStealth(boolean stealth) {
        this.stealth = stealth;
    }

    public boolean getSummoningSickness() {
        return summoningSickness;
    }
    public void setSummoningSickness(boolean summoningSickness) {
        this.summoningSickness = summoningSickness;
    }

    public boolean getTaunt() {
        return taunt;
    }
    public void setTaunt(boolean taunt) {
        this.taunt = taunt;
    }

    public boolean getUncensored() {
        return uncensored;
    }
    public void setUncensored(boolean uncensored) {
        this.uncensored = uncensored;
    }

    public boolean getUnderAura() {
        return underAura;
    }
    public void setUnderAura(boolean underAura) {
        this.underAura = underAura;
    }

    public boolean getUntargetableBySpells() {
        return untargetableBySpells;
    }
    public void setUntargetableBySpells(boolean untargetableBySpells) {
        this.untargetableBySpells = untargetableBySpells;
    }

    public boolean getWindfury() {
        return windfury;
    }
    public void setWindfury(boolean windfury) {
        this.windfury = windfury;
    }

    /**
     * misc
     */
    public Integer getCharges() {
        return charges;
    }
    /**
     * misc
     */
    public void setCharges(Integer charges) {
        this.charges = charges;
    }

    public Integer getCountUntilCast() {
        return countUntilCast;
    }
    public void setCountUntilCast(Integer countUntilCast) {
        this.countUntilCast = countUntilCast;
    }

    public Integer getFires() {
        return fires;
    }
    public void setFires(Integer fires) {
        this.fires = fires;
    }

    public int getHost() {
        return host;
    }
    public void setHost(int host) {
        this.host = host;
    }

    public java.util.List<String> getHeroClasses() {
        return heroClasses;
    }
    public void setHeroClasses(java.util.List<String> heroClasses) {
        this.heroClasses = heroClasses;
    }

    public String getCardSet() {
        return cardSet;
    }
    public void setCardSet(String cardSet) {
        this.cardSet = cardSet;
    }

    public java.util.List<String> getCardSets() {
        return cardSets;
    }
    public void setCardSets(java.util.List<String> cardSets) {
        this.cardSets = cardSets;
    }

    public String getEnchantmentType() {
        return enchantmentType;
    }
    public void setEnchantmentType(String enchantmentType) {
        this.enchantmentType = enchantmentType;
    }

    public java.util.List<String> getTribes() {
        return tribes;
    }
    public void setTribes(java.util.List<String> tribes) {
        this.tribes = tribes;
    }

    public java.util.List<Tooltip> getTooltips() {
        return tooltips;
    }
    public void setTooltips(java.util.List<Tooltip> tooltips) {
        this.tooltips = tooltips;
    }

    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }



    public static Entity.Builder builder() {
        return new Entity.Builder();
    }

    public static class Builder {

        private int id;
        private String name;
        private String description;
        private String cardId;
        private CardType cardType;
        private EntityType entityType;
        private Rarity rarity;
        private EntityLocation location;
        private Art art;
        private int owner;
        private int boardPosition;
        private Integer attack;
        private Integer baseAttack;
        private Integer hp;
        private Integer baseHp;
        private Integer maxHp;
        private Integer armor;
        private Integer manaCost;
        private Integer baseManaCost;
        private Integer durability;
        private Integer spellDamage;
        private Integer overload;
        private Integer extraAttack;
        private int mana;
        private int maxMana;
        private int lockedMana;
        private boolean battlecry;
        private boolean cannotAttack;
        private boolean charge;
        private boolean chooseOne;
        private boolean collectible;
        private boolean combo;
        private boolean conditionMet;
        private boolean deathrattles;
        private boolean deflect;
        private boolean destroyed;
        private boolean discarded;
        private boolean divineShield;
        private boolean enraged;
        private boolean frozen;
        private boolean gameStarted;
        private boolean gold;
        private boolean hostsTrigger;
        private boolean immune;
        private boolean isStartingTurn;
        private boolean lifesteal;
        private boolean permanent;
        private boolean playable;
        private boolean poisonous;
        private boolean roasted;
        private boolean rush;
        private boolean silenced;
        private boolean stealth;
        private boolean summoningSickness;
        private boolean taunt;
        private boolean uncensored;
        private boolean underAura;
        private boolean untargetableBySpells;
        private boolean windfury;
        private Integer charges;
        private Integer countUntilCast;
        private Integer fires;
        private int host;
        private java.util.List<String> heroClasses;
        private String cardSet;
        private java.util.List<String> cardSets;
        private String enchantmentType;
        private java.util.List<String> tribes;
        private java.util.List<Tooltip> tooltips;
        private String note;

        public Builder() {
        }

        public Builder setId(int id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setCardId(String cardId) {
            this.cardId = cardId;
            return this;
        }

        public Builder setCardType(CardType cardType) {
            this.cardType = cardType;
            return this;
        }

        public Builder setEntityType(EntityType entityType) {
            this.entityType = entityType;
            return this;
        }

        public Builder setRarity(Rarity rarity) {
            this.rarity = rarity;
            return this;
        }

        public Builder setLocation(EntityLocation location) {
            this.location = location;
            return this;
        }

        public Builder setArt(Art art) {
            this.art = art;
            return this;
        }

        public Builder setOwner(int owner) {
            this.owner = owner;
            return this;
        }

        public Builder setBoardPosition(int boardPosition) {
            this.boardPosition = boardPosition;
            return this;
        }

        /**
         * stats
         */
        public Builder setAttack(Integer attack) {
            this.attack = attack;
            return this;
        }

        public Builder setBaseAttack(Integer baseAttack) {
            this.baseAttack = baseAttack;
            return this;
        }

        public Builder setHp(Integer hp) {
            this.hp = hp;
            return this;
        }

        public Builder setBaseHp(Integer baseHp) {
            this.baseHp = baseHp;
            return this;
        }

        public Builder setMaxHp(Integer maxHp) {
            this.maxHp = maxHp;
            return this;
        }

        public Builder setArmor(Integer armor) {
            this.armor = armor;
            return this;
        }

        public Builder setManaCost(Integer manaCost) {
            this.manaCost = manaCost;
            return this;
        }

        public Builder setBaseManaCost(Integer baseManaCost) {
            this.baseManaCost = baseManaCost;
            return this;
        }

        public Builder setDurability(Integer durability) {
            this.durability = durability;
            return this;
        }

        public Builder setSpellDamage(Integer spellDamage) {
            this.spellDamage = spellDamage;
            return this;
        }

        public Builder setOverload(Integer overload) {
            this.overload = overload;
            return this;
        }

        public Builder setExtraAttack(Integer extraAttack) {
            this.extraAttack = extraAttack;
            return this;
        }

        /**
         * player resource fields
         */
        public Builder setMana(int mana) {
            this.mana = mana;
            return this;
        }

        public Builder setMaxMana(int maxMana) {
            this.maxMana = maxMana;
            return this;
        }

        public Builder setLockedMana(int lockedMana) {
            this.lockedMana = lockedMana;
            return this;
        }

        /**
         * boolean flags
         */
        public Builder setBattlecry(boolean battlecry) {
            this.battlecry = battlecry;
            return this;
        }

        public Builder setCannotAttack(boolean cannotAttack) {
            this.cannotAttack = cannotAttack;
            return this;
        }

        public Builder setCharge(boolean charge) {
            this.charge = charge;
            return this;
        }

        public Builder setChooseOne(boolean chooseOne) {
            this.chooseOne = chooseOne;
            return this;
        }

        public Builder setCollectible(boolean collectible) {
            this.collectible = collectible;
            return this;
        }

        public Builder setCombo(boolean combo) {
            this.combo = combo;
            return this;
        }

        public Builder setConditionMet(boolean conditionMet) {
            this.conditionMet = conditionMet;
            return this;
        }

        public Builder setDeathrattles(boolean deathrattles) {
            this.deathrattles = deathrattles;
            return this;
        }

        public Builder setDeflect(boolean deflect) {
            this.deflect = deflect;
            return this;
        }

        public Builder setDestroyed(boolean destroyed) {
            this.destroyed = destroyed;
            return this;
        }

        public Builder setDiscarded(boolean discarded) {
            this.discarded = discarded;
            return this;
        }

        public Builder setDivineShield(boolean divineShield) {
            this.divineShield = divineShield;
            return this;
        }

        public Builder setEnraged(boolean enraged) {
            this.enraged = enraged;
            return this;
        }

        public Builder setFrozen(boolean frozen) {
            this.frozen = frozen;
            return this;
        }

        public Builder setGameStarted(boolean gameStarted) {
            this.gameStarted = gameStarted;
            return this;
        }

        public Builder setGold(boolean gold) {
            this.gold = gold;
            return this;
        }

        public Builder setHostsTrigger(boolean hostsTrigger) {
            this.hostsTrigger = hostsTrigger;
            return this;
        }

        public Builder setImmune(boolean immune) {
            this.immune = immune;
            return this;
        }

        public Builder setIsStartingTurn(boolean isStartingTurn) {
            this.isStartingTurn = isStartingTurn;
            return this;
        }

        public Builder setLifesteal(boolean lifesteal) {
            this.lifesteal = lifesteal;
            return this;
        }

        public Builder setPermanent(boolean permanent) {
            this.permanent = permanent;
            return this;
        }

        public Builder setPlayable(boolean playable) {
            this.playable = playable;
            return this;
        }

        public Builder setPoisonous(boolean poisonous) {
            this.poisonous = poisonous;
            return this;
        }

        public Builder setRoasted(boolean roasted) {
            this.roasted = roasted;
            return this;
        }

        public Builder setRush(boolean rush) {
            this.rush = rush;
            return this;
        }

        public Builder setSilenced(boolean silenced) {
            this.silenced = silenced;
            return this;
        }

        public Builder setStealth(boolean stealth) {
            this.stealth = stealth;
            return this;
        }

        public Builder setSummoningSickness(boolean summoningSickness) {
            this.summoningSickness = summoningSickness;
            return this;
        }

        public Builder setTaunt(boolean taunt) {
            this.taunt = taunt;
            return this;
        }

        public Builder setUncensored(boolean uncensored) {
            this.uncensored = uncensored;
            return this;
        }

        public Builder setUnderAura(boolean underAura) {
            this.underAura = underAura;
            return this;
        }

        public Builder setUntargetableBySpells(boolean untargetableBySpells) {
            this.untargetableBySpells = untargetableBySpells;
            return this;
        }

        public Builder setWindfury(boolean windfury) {
            this.windfury = windfury;
            return this;
        }

        /**
         * misc
         */
        public Builder setCharges(Integer charges) {
            this.charges = charges;
            return this;
        }

        public Builder setCountUntilCast(Integer countUntilCast) {
            this.countUntilCast = countUntilCast;
            return this;
        }

        public Builder setFires(Integer fires) {
            this.fires = fires;
            return this;
        }

        public Builder setHost(int host) {
            this.host = host;
            return this;
        }

        public Builder setHeroClasses(java.util.List<String> heroClasses) {
            this.heroClasses = heroClasses;
            return this;
        }

        public Builder setCardSet(String cardSet) {
            this.cardSet = cardSet;
            return this;
        }

        public Builder setCardSets(java.util.List<String> cardSets) {
            this.cardSets = cardSets;
            return this;
        }

        public Builder setEnchantmentType(String enchantmentType) {
            this.enchantmentType = enchantmentType;
            return this;
        }

        public Builder setTribes(java.util.List<String> tribes) {
            this.tribes = tribes;
            return this;
        }

        public Builder setTooltips(java.util.List<Tooltip> tooltips) {
            this.tooltips = tooltips;
            return this;
        }

        public Builder setNote(String note) {
            this.note = note;
            return this;
        }


        public Entity build() {
            return new Entity(id, name, description, cardId, cardType, entityType, rarity, location, art, owner, boardPosition, attack, baseAttack, hp, baseHp, maxHp, armor, manaCost, baseManaCost, durability, spellDamage, overload, extraAttack, mana, maxMana, lockedMana, battlecry, cannotAttack, charge, chooseOne, collectible, combo, conditionMet, deathrattles, deflect, destroyed, discarded, divineShield, enraged, frozen, gameStarted, gold, hostsTrigger, immune, isStartingTurn, lifesteal, permanent, playable, poisonous, roasted, rush, silenced, stealth, summoningSickness, taunt, uncensored, underAura, untargetableBySpells, windfury, charges, countUntilCast, fires, host, heroClasses, cardSet, cardSets, enchantmentType, tribes, tooltips, note);
        }

    }
}
