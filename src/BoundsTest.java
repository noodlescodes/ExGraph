public class BoundsTest {
	public static void main(String[] args) {
		int deltaL = 6;
		int deltaU = 7;
		for (int c = 0; c < deltaL; c++) {
			for(int i = deltaU + 1 + c * deltaL; i < deltaU + deltaL * (c+ 1); i++) {
				for(int j = i + 1; j <= deltaU + deltaL * (c + 1); j++) {
					System.out.println(i + ", " + j);
				}
			}
		}
	}

}
