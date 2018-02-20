package os;
import java.util.*;

public class RCB { //Resource Control Block

	public static class Pair{
		PCB process;
		int amountUsed;

		public Pair(PCB p, int n) {
			process = p;
			amountUsed = n;
		}
	}

	int rid; //1, 2, .. , i
	String ridString;
	int totalUnits;
	int usedUnits;
	LinkedList<Pair> allocatedList;
	LinkedList<Pair> waitingList;

	public RCB(int id) {
		rid = id;
		ridString = "R" + Integer.toString(id);
		totalUnits = id;
		usedUnits = 0;
		allocatedList = new LinkedList<Pair>();
		waitingList = new LinkedList<Pair>();
	}

	public int numAllocated(PCB process) {
		for (int x = 0; x < allocatedList.size(); x++) {
			if (allocatedList.get(x).process == process) {
				return allocatedList.get(x).amountUsed;
			}
		}
		return 0;
	}

	public void allocate(PCB process, int numUnits) {
		usedUnits += numUnits;

		if (numAllocated(process) == 0) {
			allocatedList.add(new Pair(process, numUnits));
		} else {
			for (int i = 0; i < allocatedList.size(); i++) {
				if (allocatedList.get(i).process == process) {
					allocatedList.get(i).amountUsed += numUnits;
				}
			}
		}
	}

	public void deallocate(PCB process, int numUnits) {	
		usedUnits -= numUnits;
		for (int i = 0; i < allocatedList.size(); i++) {
			if (allocatedList.get(i).process == process) {
				if (allocatedList.get(i).amountUsed == numUnits) {
					allocatedList.remove(i);
				} else {
					allocatedList.get(i).amountUsed -= numUnits;
				}
			}
		}
	}

	public Pair RemovePair(PCB process, LinkedList<Pair> list) { //returns pair from waiting
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).process == process) {
				Pair toReturn = list.get(i);
				list.remove(i);
				return toReturn;
			}
		}
		throw new NoSuchElementException();
	}

	public boolean isFree(int numUnits) {
		return totalUnits-usedUnits >= numUnits;
	}

	public class MaxResourceException extends Exception {
		public MaxResourceException() {
			super();
		}
		public MaxResourceException(String message) {
			super(message);
		}
	}

}