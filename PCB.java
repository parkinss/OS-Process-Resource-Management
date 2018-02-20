package os;
import java.util.*;

public class PCB {

	public String pid; //stores name of process
	private int statusType; //0 for running, 1 for ready, 2 for blocked, 3 for destroyed
	private PCB parent;
	private LinkedList<PCB> children;
	private int priority;
	private LinkedList<RCB> otherResources;

	public PCB(String pid, int priority) {
		this.pid = pid;
		statusType = 1;
		children = new LinkedList<PCB>();
		this.priority = priority;
		otherResources = new LinkedList<RCB>();
	}

	public void create(String pid, int priority) throws Exception {
		
		if (Manager.getPCB(pid) != null) {
			throw new ProcessExistsException(); //process aleady exists, can't create same name
		} else if (priority < 0 || priority > 2) { //invalid priority, error!
			throw new Exception();
		}
		PCB p = new PCB(pid, priority);
		p.parent = this;
		children.add(p);
		Manager.readyList.get(priority).add(p);
	}

	public class ProcessExistsException extends Exception {
		public ProcessExistsException() {
			super();
		}
		public ProcessExistsException(String message) {
			super(message);
		}
	}

	public void destroy(String pid) throws Exception {
		if (!this.pid.equals(pid) && !isDescendant(pid)) {
			throw new NotDescendantException();
		}
		killTree(Manager.getPCB(pid));
		//scheduler();
	}

	public void killTree(PCB p) throws Exception {
		for (int i=0; i < p.children.size(); i++) {
			killTree(p.children.get(i));
		}
		for (int i=0; i < p.otherResources.size(); i++) {
			p.release(p.otherResources.get(i).rid, p.otherResources.get(i).numAllocated(p));
		}
		if (p.statusType < 2) {
			Manager.readyList.get(p.priority).remove(p);
		}
		p.statusType = 3;

	}

	public class NotDescendantException extends Exception {
		public NotDescendantException() {
			super();
		}
		public NotDescendantException(String message) {
			super(message);
		}
	}

	public void request(int rid, int numUnits) throws Exception {
		if (rid < 1 || rid > 4) { //check if resource id is valid
			throw new Exception();
		}
		RCB resource = Manager.getRCB(rid);
		if (numUnits + resource.numAllocated(this) > resource.totalUnits || numUnits < 1) { //check if numUnits requested is valid
			throw new Exception();
		} else if (resource.isFree(numUnits) && resource.waitingList.size() == 0) { //if not enough units are free or another process is
			if (resource.numAllocated(this) == 0) {                               //waiting on them become blocked process
				otherResources.add(resource);
			}
			resource.allocate(this, numUnits);
		} else {
			statusType = 2;
			Manager.readyList.get(this.priority).remove(this);
			resource.waitingList.add(new RCB.Pair(this, numUnits));
		}
	}

	public void release(int rid, int numUnits) throws Exception {
		if (rid < 1 || rid > 4) {
			throw new Exception();
		} 
		RCB resource = Manager.getRCB(rid);
		if (numUnits > resource.numAllocated(this) || numUnits < 0) {
			throw new Exception();
		}
		resource.deallocate(this, numUnits); //free up resource units
		while (resource.waitingList.size() > 0) { //give resources to waiting processes
			RCB.Pair firstPair = resource.waitingList.get(0);
			if (resource.isFree(firstPair.amountUsed)) {
				Manager.readyList.get(firstPair.process.priority).add(firstPair.process);
				resource.waitingList.remove(0);
				if (resource.numAllocated(firstPair.process) == 0) { //in case process already holding onto a resource
					firstPair.process.otherResources.add(resource);
				}
				resource.allocate(firstPair.process, firstPair.amountUsed);
				firstPair.process.statusType = 1;
			} else {
				break;
			}
		}
		
	}

	public void scheduler() {
		//debugger();
		PCB p = highestPriority();
		if (this.priority < p.priority || this.statusType != 0 || this.statusType == 3) {
			p.statusType = 0;
			Manager.current = p;
		}
		IO.result.append(Manager.current.pid + " ");
	}

	public PCB highestPriority() {
		for (int i=2; i>=0; i--) {
			if (Manager.readyList.get(i).size() > 0) {
				return Manager.readyList.get(i).get(0);
			}
		}
		return null;
	}

	public void timeOut() {
		PCB current = Manager.current;
		Manager.readyList.get(current.priority).remove(current);
		Manager.readyList.get(current.priority).add(current);
		current.statusType = 1;
		//scheduler();
	}

	public boolean isDescendant(String pid) { //returns true if the current process is an ancestor of the specified process
		if (children.size() == 0) {
			return false;
		} else {
			for (int i = 0; i < children.size(); i++) {
				if (children.get(i).pid.equals(pid) || children.get(i).isDescendant(pid)) {
					return true;
				}
			}
		}
		return false;
	}

	public void debugger() {
		System.out.println("---READYLIST---");
		for (int i=2; i>=0; i--) {
			System.out.print(i + ":");
			for (int j=0; j<Manager.readyList.get(i).size(); j++) {
				System.out.print(" " + Manager.readyList.get(i).get(j).pid + "(");
				for (int z=0; z < Manager.readyList.get(i).get(j).otherResources.size(); z++){
					System.out.print(Manager.readyList.get(i).get(j).otherResources.get(z).ridString + " ");
				}
				System.out.print(")");

			}
			System.out.println();
		}
		System.out.print("---RESOURCELIST---");
		for (int i=0; i<4; i++) {
			System.out.print("\nR"+ Integer.toString(i+1) + ": ALLOCATED: ");
			for (int j=0; j<Manager.resourceList.get(i).allocatedList.size(); j++) {
				System.out.print(Manager.resourceList.get(i).allocatedList.get(j).process.pid);
				System.out.print("(" + Manager.resourceList.get(i).allocatedList.get(j).amountUsed + ")");
			}
			System.out.print(" --- WAITING: ");
			for (int j=0; j<Manager.resourceList.get(i).waitingList.size(); j++) {
				System.out.print(Manager.resourceList.get(i).waitingList.get(j).process.pid);
				System.out.print("(" + Manager.resourceList.get(i).waitingList.get(j).amountUsed + ")");

			}
		}
		System.out.println();
	}

}