package no.hiof.android.ambiguous;

import java.util.Random;

public class RandomAmountGenerator {
	private static Random crit = new Random();
	
	public static int GenerateAmount(int min, int max, int crit)
	{
		boolean isCrit = (RandomAmountGenerator.crit.nextFloat() < 0.25?true:false);
		Random rand = new Random();
		int range = max - min;
		int base = rand.nextInt(range) + min;
		return (isCrit?max+crit:base);
	}

}
