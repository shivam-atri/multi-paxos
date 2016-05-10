package paxosProject;
import java.util.ArrayList;
import java.util.Map;
import paxosProject.Nodes.Acceptor;
import paxosProject.Nodes.Client;
import paxosProject.Nodes.Proposer;
import paxosProject.network.NodeIdentifier;

public class Test {


	public static void main(String []args) throws Exception {
		Test test = new Test();
		//test.simpleTest(args[0]);
		test.simpleTest2(args[0]);
	}

	private void simpleTest(String configFile) throws Exception {
		Configuration.initConfiguration(configFile);
		Configuration.showNodeConfig();
		Configuration.addActiveLogger("NettyNetwork", SimpleLogger.INFO);
		ArrayList<Client> clients = new ArrayList<>();
		ArrayList<Proposer> proposers = new ArrayList<>();
		ArrayList<Acceptor> acceptors = new ArrayList<>();
		for(Map.Entry<Integer, NodeIdentifier> entry : Configuration.clientIDs.entrySet()) {
			int leaderid = 1;
			Client c = new Client(entry.getValue(), leaderid);
			clients.add(c);
		}
		for(Map.Entry<Integer, NodeIdentifier> entry : Configuration.acceptorIDs.entrySet()) {
			Acceptor a = new Acceptor(entry.getValue());
			acceptors.add(a);
		}
		for(Map.Entry<Integer, NodeIdentifier> entry : Configuration.proposerIDs.entrySet()) {
			Proposer p;
			if(entry.getKey()==1) {
				 p = new Proposer(entry.getValue(), true, entry.getKey());
			} else {
				 p = new Proposer(entry.getValue(), false, entry.getKey());
			}
			proposers.add(p);
		}
		Thread.sleep(4000);
		int key = 10;
		int value = 555;
		int limit = 14;
		try {
			while(key < limit) {
				Thread.sleep(2000);
				clients.get(0).put(key++, value++);
			}
			Thread.sleep(2000);
		} catch (Exception ex) {
			System.out.println("Who woke the test thread?");
		}
		}

	private void simpleTest2(String configFile) throws Exception {
		Configuration.initConfiguration(configFile);
		Configuration.showNodeConfig();
		Configuration.addActiveLogger("NettyNetwork", SimpleLogger.INFO);
		ArrayList<Client> clients = new ArrayList<>();
		ArrayList<Proposer> proposers = new ArrayList<>();
		ArrayList<Acceptor> acceptors = new ArrayList<>();
		for(Map.Entry<Integer, NodeIdentifier> entry : Configuration.clientIDs.entrySet()) {
			int leaderid = 1;
			Client c = new Client(entry.getValue(), leaderid);
			clients.add(c);
		}
		for(Map.Entry<Integer, NodeIdentifier> entry : Configuration.acceptorIDs.entrySet()) {
			Acceptor a = new Acceptor(entry.getValue());
			acceptors.add(a);
		}
		for(Map.Entry<Integer, NodeIdentifier> entry : Configuration.proposerIDs.entrySet()) {
			Proposer p;
			if(entry.getKey()==1) {
				p = new Proposer(entry.getValue(), true, entry.getKey());
				p.set_fail_reply_HeartBeat();
			} else {
				p = new Proposer(entry.getValue(), false, entry.getKey());
			}
			proposers.add(p);
		}
		Thread.sleep(4000);
		int key = 50;
		int value = 555;
		int limit = 57;
		try {
			while(key < limit) {
				Thread.sleep(5000);
				clients.get(0).put(key++, value++);
			}
			Thread.sleep(2000);
		} catch (Exception ex) {
			System.out.println("Who woke the test thread?");
		}
	}
}
