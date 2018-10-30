package ex1;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Generator {
	static int f(int x) {
		return (int)(x * Math.sin(x * 2 * Math.PI / 360));
	}

	public static void main(String[] args) {
		String[] list = new String[200];
		for (int i = 0; i < 200; i++) {
			list[i] = String.valueOf(f(i));
		}
		String text = String.join(",", list);

		try {
			PrintWriter p = new PrintWriter(new BufferedWriter(new FileWriter("./src/ex1/Data.csv", false)));
			p.print(text);
			p.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
