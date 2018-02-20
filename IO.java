package os;
import java.io.*;

public class IO {

	public static StringBuffer result = new StringBuffer("init ");

	public static void readFile() throws Exception {

		BufferedReader bufferedReader = new BufferedReader(Manager.fileReader);
		PrintWriter writer = new PrintWriter("output.txt", "UTF-8");
		String line;
		try {
			while ((line = bufferedReader.readLine()) != null) {
				if (line.isEmpty()) {
					result.append("\r\n");
				} else {
					readCommand(line);
				}
			}
			writer.println(result);
			writer.close();
		} catch (IOException e) {
			System.exit(-2);
		}	
	}

	public static void readInput() {

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try {
			String input = reader.readLine();
			while (!input.equals("")) {
			readCommand(input);
			input = reader.readLine();
			}
			System.out.println(result);
		} catch (IOException e) {
			System.exit(-2);
		}

	}

	public static void readCommand(String s) {
		System.out.println(s);
		String[] splitString = s.split(" ");

		try {

			if (splitString[0].equals("cr")) {
				Manager.current.create(splitString[1], Integer.parseInt(splitString[2]));
				Manager.current.scheduler();
			} else if (splitString[0].equals("to")) {
				Manager.current.timeOut();
				Manager.current.scheduler();
			} else if (splitString[0].equals("req")) {
				int rid = Integer.parseInt(splitString[1].substring(1, 2));
				Manager.current.request(rid, Integer.parseInt(splitString[2]));
				Manager.current.scheduler();
			} else if (splitString[0].equals("de")) {
				Manager.current.destroy(splitString[1]);
				Manager.current.scheduler();
			} else if (splitString[0].equals("rel")) {
				int rid = Integer.parseInt(splitString[1].substring(1, 2));
				Manager.current.release(rid, Integer.parseInt(splitString[2]));
				Manager.current.scheduler();
			} else if (splitString[0].equals("")) {
				result.append("\n");
			} else if (splitString[0].equals("init")) {
				Manager.start();
				result.append("init ");
			} else {
				result.append("error ");
				System.out.println("\"" + s + "\" is an invalid command.");
			}
		} catch (Exception e) {
			result.append("error ");
			//e.printStackTrace();
			//System.out.println("error has occurred.");
			//e.printStackTrace();
		}
	}
}