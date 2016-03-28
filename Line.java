

public class Line {

	String line;

	public Line(String l) {
		this.line = l;
	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public String toString() {
		return line;
	}

	public double count(String string) {
		int count = 0;
		System.out.println("line to count: " + line);
		for (String w : line.split(" ")) {
			if (w.equalsIgnoreCase(string))
				count++;
		}
		return count;
	}

}