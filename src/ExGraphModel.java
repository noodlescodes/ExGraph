import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;

public class ExGraphModel {

	// problem variables
	public int deltaL = 3; // minimum degree
	public int deltaU = 4; // maximum degree
	public int nVertices = deltaL * deltaU + 1; // number of vertices

	// model variables
	LpSolve lp;
	double[] row;
	int[] colno;

	// output options
	public static final boolean VERBOSE = true;
	public static final boolean OUTPUT = false;

	public static void dumpTime(long t) {
		try {
			PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter("timeDump.txt", true)));
			w.println("EX: " + t);
			w.close();
		} catch (IOException e) {
		}
	}

	public String createString(int i, int j) {
		return "B" + i + "," + j + "E";
	}

	public int getIndex(int i, int j) {
		return nVertices * (i - 1) + j;
	}

	public int execute() throws LpSolveException {
		int ret = 0;
		int j = 0;
		colno = new int[nVertices * nVertices];
		row = new double[nVertices * nVertices];

		lp = LpSolve.makeLp(0, nVertices * nVertices);
		if (lp.getLp() == 0) {
			ret = 1;
		}

		if (ret == 0) {
			if (VERBOSE) {
				System.out.println("Setting up model name.");
			}
			// setup model name
			lp.setLpName("Ex graph");

			if (VERBOSE) {
				System.out.println("Setting up variable names.");
			}
			// setup variable names
			int loop = 1;
			for (int i = 1; i <= nVertices; i++) {
				for (int j1 = 1; j1 <= nVertices; j1++) {
					lp.setColName(loop, createString(i, j1));
					lp.setBinary(loop++, true);
				}
			}

			double[] weights = new double[nVertices];
			for (int i = 0; i < weights.length; i++) {
				weights[i] = 1;
			}

			if (VERBOSE) {
				System.out.println("Setting build mode.");
			}
			// setup build mode
			lp.setAddRowmode(true);

			// ---begin adding predefined constraints---
			if (VERBOSE) {
				System.out.println("Adding predefined constraints.");
			}
			for (int i = 2; i <= deltaU; i++) {
				for (int j1 = 1; j1 <= deltaL; j1++) {
					lp.setBounds(getIndex(i, j1 + (i - 1) * deltaL + 1), 1, 1.5);
				}
			}
			for (int i = 2; i <= deltaU; i++) {
				lp.setBounds(getIndex(1, i), 1, 1.5);
			}

			j = deltaL + deltaU + 1;
			for (int i = deltaU + 1; i <= deltaL + deltaU; i++) {
				for (int b = 1; b < deltaL; b++) {
					lp.setBounds(getIndex(i, j + (b - 1) * deltaL), 1, 1.5);
				}
				j++;
			}
			// ---end adding predefined constraints---

			// ---begin a_{i,i}=0 constraints---
			if (VERBOSE) {
				System.out.println("Beginning a_{i,i}=0 constraints.");
			}
			for (int i = 1; i <= nVertices; i++) {
				colno[j] = getIndex(i, i);
				row[j++] = 1;
				lp.addConstraintex(j, row, colno, LpSolve.EQ, 0);
				j = 0;
			}
			// ---end a_{i,i}=0 constraints---

			// ---begin a_{i,j}=a_{j,i} constraints---
			if (VERBOSE) {
				System.out.println("Beginning a_{i,j}=a_{j,i} constraints.");
			}
			j = 0;
			for (int i = 1; i <= nVertices; i++) {
				for (int j1 = 1; j1 <= nVertices; j1++) {
					if (i != j1) {
						colno[j] = getIndex(i, j1);
						row[j++] = 1;
						colno[j] = getIndex(j1, i);
						row[j++] = -1;
						lp.addConstraintex(j, row, colno, LpSolve.EQ, 0);
					}
					j = 0;
				}
			}
			// ---end a_{i,j}=a_{j,i} constraints---

			// ---begin no triangles constraint---
			// if (VERBOSE) {
			// System.out.println("Beginning triangle constraints.");
			// }
			// j = 0;
			// for (int i = 1; i <= nVertices; i++) {
			// for (int j1 = i; j1 <= nVertices; j1++) {
			// for (int k = j1; k <= nVertices; k++) {
			// if (i != j1 && j1 != k && i != k) {
			// colno[j] = getIndex(i, j1);
			// row[j++] = 1;
			// colno[j] = getIndex(j1, k);
			// row[j++] = 1;
			// colno[j] = getIndex(i, k);
			// row[j++] = 1;
			// lp.addConstraintex(j, row, colno, LpSolve.LE, 2);
			// j = 0;
			// }
			// }
			// }
			// }
			// ---end no triangles constraint---

			// ---begin no squares constraint---
			// if (VERBOSE) {
			// System.out.println("Beginning square constraints.");
			// }
			// j = 0;
			// for (int i = 1; i <= nVertices; i++) {
			// for (int j1 = 1; j1 <= nVertices; j1++) {
			// for (int k = 1; k <= nVertices; k++) {
			// for (int l = 1; l <= nVertices; l++) {
			// if (i != j1 && i != k && i != l && j1 != k && j1 != l && k != l)
			// {
			// colno[j] = getIndex(i, j1);
			// row[j++] = 1;
			// colno[j] = getIndex(j1, k);
			// row[j++] = 1;
			// colno[j] = getIndex(k, l);
			// row[j++] = 1;
			// colno[j] = getIndex(l, i);
			// row[j++] = 1;
			// lp.addConstraintex(j, row, colno, LpSolve.LE, 3);
			// j = 0;
			// }
			// }
			// }
			// }
			// }
			// ---end no squares constraint---

			// ---begin correct column sum constraint---
			// if (VERBOSE) {
			// System.out.println("Beginning column sum constraints.");
			// }
			// j = 0;
			// for (int j1 = deltaU + 1; j1 <= nVertices; j1++) {
			// for (int i = 1; i <= nVertices; i++) {
			// colno[j] = getIndex(i, j1);
			// row[j++] = 1;
			// }
			// lp.addConstraintex(j, row, colno, LpSolve.EQ, deltaL);
			// j = 0;
			// }
			// // column between column 2 and column deltaU
			// j = 0;
			// for (int j1 = 2; j1 <= deltaU; j1++) {
			// for (int i = 1; i <= nVertices; i++) {
			// colno[j] = getIndex(i, j1);
			// row[j++] = 1;
			// }
			// lp.addConstraintex(j, row, colno, LpSolve.EQ, deltaU);
			// j = 0;
			// }
			// j = 0;
			// for (int i = 1; i <= nVertices; i++) {
			// colno[j] = getIndex(i, 1);
			// row[j++] = 1;
			// }
			// // first column
			// lp.addConstraintex(j, row, colno, LpSolve.EQ, deltaL);
			// ---end correct column sum constraint---

			// ---begin correct row sum constraint---
			if (VERBOSE) {
				System.out.println("Beginning row sum constraints.");
			}
			j = 0;
			for (int i = deltaU + 1; i <= nVertices; i++) {
				for (int j1 = 1; j1 <= nVertices; j1++) {
					colno[j] = getIndex(i, j1);
					row[j++] = 1;
				}
				lp.addConstraintex(j, row, colno, LpSolve.EQ, deltaL);
				j = 0;
			}
			// rows between row 2 and row deltaU
			j = 0;
			for (int i = 2; i <= deltaU; i++) {
				for (int j1 = 1; j1 <= nVertices; j1++) {
					colno[j] = getIndex(i, j1);
					row[j++] = 1;
				}
				lp.addConstraintex(j, row, colno, LpSolve.EQ, deltaU);
				j = 0;
			}
			j = 0;
			for (int j1 = 1; j1 <= nVertices; j1++) {
				colno[j] = getIndex(1, j1);
				row[j++] = 1;
			}
			// first row
			lp.addConstraintex(j, row, colno, LpSolve.EQ, deltaL);
			// ---end correct row sum constraint---

			// ---begin a_{i,j}+a_{i+deltaL,j}<=1 constraints---
			// if (VERBOSE) {
			// System.out.println("Beginning a_{i,j}+a_{i+deltaL,j}<=1 constraints.");
			// }
			// j = 0;
			// for (int i = 2 + deltaL; i <= nVertices - deltaL; i++) {
			// for (int j1 = 2 + deltaL; j1 <= nVertices - deltaL; j1++) {
			// colno[j] = getIndex(i, j1);
			// row[j++] = 1;
			// colno[j] = getIndex(i, j1 + deltaL);
			// row[j++] = 1;
			// lp.addConstraintex(j, row, colno, LpSolve.LE, 1);
			// j = 0;
			// }
			// }
			// ---end a_{i,j}+a_{i+deltaL,j}<=1 constraints---

			if (VERBOSE) {
				System.out.println("Turning off build mode.");
			}
			// turn off build mode
			lp.setAddRowmode(false);

			if (VERBOSE) {
				System.out.println("Beginning objective function.");
			}
			// ---begin objective function---
			j = 0;
			colno[j] = 1; // first column
			row[j++] = 0; // value of first column

			// set the objective function in lpsolve
			lp.setObjFnex(j, row, colno);

			// set the object direction to maximise
			lp.setMaxim();
			// ---end objective function---

			if (VERBOSE) {
				System.out.println("Writing model to file.");
			}
			// write model to file
			lp.writeLp("modelEx.lp");

			// set message type
			lp.setVerbose(LpSolve.NORMAL);

			// ---begin solving---
			if (VERBOSE) {
				System.out.println("Presovling.");
			}
			lp.setPresolve(LpSolve.PRESOLVE_BOUNDS | LpSolve.PRESOLVE_COLS | LpSolve.PRESOLVE_ROWS, lp.getPresolveloops());

			if (VERBOSE) {
				System.out.println("Starting to sovle the model. This may take a while.");
			}

			ret = lp.solve();
			if (ret == LpSolve.OPTIMAL) {
				ret = 0;
			} else {
				ret = 5;
			}
			// ---end solving---
		}

		if (VERBOSE) {
			System.out.println("Beginning output.");
		}

		if (ret == 0) {
			lp.printObjective();
			lp.setOutputfile("ExSolution.dat");
			lp.printSolution(1);
			lp.printConstraints(1);
		} else {
			System.out.println("No solution found.");
		}
		// ---end output---

		return ret;
	}

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date(startTime));
		System.out.println("Start time: " + date);
		try {
			new ExGraphModel().execute();
		} catch (LpSolveException e) {
		}
		long totalTime = (System.currentTimeMillis() - startTime);
		System.out.println("Total time: " + totalTime);
		dumpTime(totalTime);
	}
}
