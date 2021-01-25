package net.demilich.metastone.game.spells.desc.valueprovider;

public enum AlgebraicOperation {

	ADD,
	SUBTRACT,
	MULTIPLY,
	DIVIDE,
	DIVIDE_ROUNDED,
	POWER,
	SET,
	NEGATE,
	MODULO,
	MINIMUM,
	MAXIMUM;

	public int performOperation(int num1, int num2) {
		switch (this) {
			case ADD:
				return num1 + num2;
			case DIVIDE:
				if (num2 == 0) {
					num2 = 1;
				}
				return num1 / num2;
			case DIVIDE_ROUNDED:
				if (num2 == 0) {
					num2 = 1;
				}
				return Math.round((float) num1 / (float) (num2));
			case MAXIMUM:
				return Math.max(num1, num2);
			case MINIMUM:
				return Math.min(num1, num2);
			case MODULO:
				if (num2 == 0) {
					num2 = 1;
				}
				return num1 % num2;
			case MULTIPLY:
				return num1 * num2;
			case NEGATE:
				return -num1;
			case SET:
				return num2;
			case SUBTRACT:
				return num1 - num2;
			case POWER:
				return (int)Math.pow(num1,num2);
			default:
				return num1;
		}
	}
}
