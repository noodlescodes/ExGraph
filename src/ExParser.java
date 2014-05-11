// Requires the solution to be in a different format to what the program will output currently. Going to figure out a better way to parse it.

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ExParser {
	public static void main(String[] args) {
		String file = "ExSolutionCompressed13.dat";
		BufferedReader br;
		String line;
		int[][] sol = new int[13][13];

		try {
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				int i = Integer.parseInt(line.split("\\s")[0].split(":")[0]) - 1;
				int j = Integer.parseInt(line.split("\\s")[0].split(":")[1]) - 1;
				// String test = line.split("\\s")[1];
				sol[i][j] = Integer.parseInt(line.split("\\s")[1]);
				System.out.println("");
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}

		for (int i = 0; i < sol.length; i++) {
			for (int j = 0; j < sol.length; j++) {
				if (j + 1 < sol.length) {
					System.out.print(sol[i][j] + ",");
				}
				else {
					System.out.print(sol[i][j]);
				}
			}
			System.out.println("");
		}
		
		try {
			PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter("ExSolution13CSV.csv")));
			for (int i = 0; i < sol.length; i++) {
				for (int j = 0; j < sol.length; j++) {
					if (j + 1 < sol.length) {
						w.print(sol[i][j] + ",");
					}
					else {
						w.print(sol[i][j]);
					}
				}
				w.println("");
			}
			w.close();
		} catch(IOException e) {
		}
	}
}
