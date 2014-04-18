package no.hiof.android.ambiguous;

import java.util.Random;

public class RandomAmountGenerator {
	private static Random crit = new Random();

	/**
	 * Generate random number in a range with a potential additional critical
	 * number.
	 * 
	 * @param min
	 * @param max
	 * @param crit
	 *            The critical value will be added as a flat modifier on
	 *            critical hits.
	 * @return
	 */
	public static int GenerateAmount(int min, int max, int crit) {
		boolean isCrit = (RandomAmountGenerator.crit.nextFloat() < 0.25 ? true
				: false);
		if (isCrit) {
			return max + crit;
		} else {
			if(max == min){return min;}
			Random rand = new Random();
			int range = max - min;
			int base = rand.nextInt(range) + min;
			return base;
		}
	}

}
