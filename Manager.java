package os;
import java.util.*;
import java.io.*;

public class Manager {
	public static List<RCB> resourceList; //store resource blocks here
	public static List<LinkedList<PCB>> readyList; //stores processes currently ready
	public static FileReader fileReader;
	public static PCB current;

	public static void main(String[] args) {
		
		start();
		try {

			if (args.length == 1) {
				fileReader = new FileReader(args[0]);
				IO.readFile();
			} else {
				IO.readInput();
			}
 		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-2);
		}
	}

	public static void start() {
		resourceList = new ArrayList<RCB>();
		readyList = new ArrayList<LinkedList<PCB>>();

		for (int i=0; i<4; i++) {
			resourceList.add(new RCB(i+1));
		}

		for (int i=0; i<3; i++) {
			readyList.add(new LinkedList<PCB>());
		}	
		PCB init = new PCB("init", 0);
		readyList.get(0).add(init);
		current = init;
	}

	public static RCB getRCB(int rid) {
		return resourceList.get(rid - 1);
	}

	public static PCB getPCB(String pid) {

		for (int i = 0; i < readyList.size(); i++) { //check readyList for process
			for (int j = 0; j < readyList.get(i).size(); j++) {
				if (readyList.get(i).get(j).pid.equals(pid)) {
					return readyList.get(i).get(j);
				}
			}
		}
		for (int i = 0; i < resourceList.size(); i++) { //check resource waiting lists for process if process is blocked
			for (int j = 0; j < resourceList.get(i).waitingList.size(); j++) {
				if (resourceList.get(i).waitingList.get(j).process.pid.equals(pid)) {
					return resourceList.get(i).waitingList.get(j).process;
				}
			}
		}

		return null; //process doesn't exist!
	}
}