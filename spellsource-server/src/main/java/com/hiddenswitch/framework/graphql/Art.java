package com.hiddenswitch.framework.graphql;


public class Art implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private ArtFont body;
    private ArtColor highlight;
    private ArtPrefab loop;
    private ArtPrefab missile;
    private ArtPrefab onCast;
    private ArtPrefab onHit;
    private ArtColor primary;
    private ArtColor secondary;
    private ArtColor shadow;
    private ArtPrefab spell;
    private ArtSprite sprite;
    private ArtSprite spriteShadow;

    public Art() {
    }

    public Art(ArtFont body, ArtColor highlight, ArtPrefab loop, ArtPrefab missile, ArtPrefab onCast, ArtPrefab onHit, ArtColor primary, ArtColor secondary, ArtColor shadow, ArtPrefab spell, ArtSprite sprite, ArtSprite spriteShadow) {
        this.body = body;
        this.highlight = highlight;
        this.loop = loop;
        this.missile = missile;
        this.onCast = onCast;
        this.onHit = onHit;
        this.primary = primary;
        this.secondary = secondary;
        this.shadow = shadow;
        this.spell = spell;
        this.sprite = sprite;
        this.spriteShadow = spriteShadow;
    }

    public ArtFont getBody() {
        return body;
    }
    public void setBody(ArtFont body) {
        this.body = body;
    }

    public ArtColor getHighlight() {
        return highlight;
    }
    public void setHighlight(ArtColor highlight) {
        this.highlight = highlight;
    }

    public ArtPrefab getLoop() {
        return loop;
    }
    public void setLoop(ArtPrefab loop) {
        this.loop = loop;
    }

    public ArtPrefab getMissile() {
        return missile;
    }
    public void setMissile(ArtPrefab missile) {
        this.missile = missile;
    }

    public ArtPrefab getOnCast() {
        return onCast;
    }
    public void setOnCast(ArtPrefab onCast) {
        this.onCast = onCast;
    }

    public ArtPrefab getOnHit() {
        return onHit;
    }
    public void setOnHit(ArtPrefab onHit) {
        this.onHit = onHit;
    }

    public ArtColor getPrimary() {
        return primary;
    }
    public void setPrimary(ArtColor primary) {
        this.primary = primary;
    }

    public ArtColor getSecondary() {
        return secondary;
    }
    public void setSecondary(ArtColor secondary) {
        this.secondary = secondary;
    }

    public ArtColor getShadow() {
        return shadow;
    }
    public void setShadow(ArtColor shadow) {
        this.shadow = shadow;
    }

    public ArtPrefab getSpell() {
        return spell;
    }
    public void setSpell(ArtPrefab spell) {
        this.spell = spell;
    }

    public ArtSprite getSprite() {
        return sprite;
    }
    public void setSprite(ArtSprite sprite) {
        this.sprite = sprite;
    }

    public ArtSprite getSpriteShadow() {
        return spriteShadow;
    }
    public void setSpriteShadow(ArtSprite spriteShadow) {
        this.spriteShadow = spriteShadow;
    }



    public static Art.Builder builder() {
        return new Art.Builder();
    }

    public static class Builder {

        private ArtFont body;
        private ArtColor highlight;
        private ArtPrefab loop;
        private ArtPrefab missile;
        private ArtPrefab onCast;
        private ArtPrefab onHit;
        private ArtColor primary;
        private ArtColor secondary;
        private ArtColor shadow;
        private ArtPrefab spell;
        private ArtSprite sprite;
        private ArtSprite spriteShadow;

        public Builder() {
        }

        public Builder setBody(ArtFont body) {
            this.body = body;
            return this;
        }

        public Builder setHighlight(ArtColor highlight) {
            this.highlight = highlight;
            return this;
        }

        public Builder setLoop(ArtPrefab loop) {
            this.loop = loop;
            return this;
        }

        public Builder setMissile(ArtPrefab missile) {
            this.missile = missile;
            return this;
        }

        public Builder setOnCast(ArtPrefab onCast) {
            this.onCast = onCast;
            return this;
        }

        public Builder setOnHit(ArtPrefab onHit) {
            this.onHit = onHit;
            return this;
        }

        public Builder setPrimary(ArtColor primary) {
            this.primary = primary;
            return this;
        }

        public Builder setSecondary(ArtColor secondary) {
            this.secondary = secondary;
            return this;
        }

        public Builder setShadow(ArtColor shadow) {
            this.shadow = shadow;
            return this;
        }

        public Builder setSpell(ArtPrefab spell) {
            this.spell = spell;
            return this;
        }

        public Builder setSprite(ArtSprite sprite) {
            this.sprite = sprite;
            return this;
        }

        public Builder setSpriteShadow(ArtSprite spriteShadow) {
            this.spriteShadow = spriteShadow;
            return this;
        }


        public Art build() {
            return new Art(body, highlight, loop, missile, onCast, onHit, primary, secondary, shadow, spell, sprite, spriteShadow);
        }

    }
}
