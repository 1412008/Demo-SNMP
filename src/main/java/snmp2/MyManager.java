package snmp2;

import java.io.IOException;
import java.util.Map;

import org.snmp4j.PDU;
import org.snmp4j.agent.mo.MOTable;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

public class MyManager {
	
	private MyAgent myAgent;

	public MyAgent getMyAgent() {
		return myAgent;
	}

	public void setMyAgent(MyAgent myAgent) {
		this.myAgent = myAgent;
	}

	public MyManager(String address) throws IOException {
		//this.address = address;
		//start();
	}

	//@SuppressWarnings("unchecked")
	//private void start() throws IOException {
		//transport = new DefaultUdpTransportMapping();
		//snmp = new Snmp(transport);
		//transport.listen();
	//}

	public void stop() throws IOException {
		//transport.close();
	}
	
	public String getAsString(OID oid) throws IOException {
		ResponseEvent event = myAgent.get(oid);
		PDU pdu = event.getResponse();
		// System.out.println(pdu);
		return pdu.get(0).getOid() + " - " + pdu.get(0).getVariable().toString();
	}

	public Map<String, String> getAsMap(OID oid) {
		return myAgent.getAsMap(oid);
	}

	public ResponseEvent set(OID oid, OctetString value) throws IOException {
		return myAgent.set(oid, value);
	}

	@SuppressWarnings("rawtypes")
	public boolean setTableValue(MOTable motb, OID oid, OctetString value) {
		return myAgent.setTableValue(motb, oid, value);

	}
	
	@SuppressWarnings("rawtypes")
	public boolean addRowToTable(MOTable motb, OID oid, Variable[] variables) {
		return myAgent.addRowToTable(motb, oid, variables);
	}

	public ResponseEvent getNext(OID oid) throws IOException {
		return myAgent.getNext(oid);
	}
	
	public ResponseEvent getBulk(OID oid, int maxRepetition) throws IOException {
		return myAgent.getBulk(oid, maxRepetition);
	}
	
	public ResponseEvent inform(OID oid) throws IOException {
		return myAgent.inform(oid);
	}
}
