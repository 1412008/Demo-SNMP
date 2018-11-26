package snmp2;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.snmp4j.PDU;
import org.snmp4j.agent.mo.MOTable;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

public class MyManager {
	
	private List<MyAgent> myAgent;

	public MyManager(List<MyAgent> myAgent) throws IOException {
		this.myAgent  = myAgent;
	}
	
	public String getAsString(OID oid, int agent_id) throws IOException {
		ResponseEvent event = myAgent.get(agent_id).get(oid);
		PDU pdu = event.getResponse();
		return pdu.get(0).getOid() + " - " + pdu.get(0).getVariable().toString();
	}

	public Map<String, String> getAsMap(OID oid, int agent_id) {
		return myAgent.get(agent_id).getAsMap(oid);
	}

	public ResponseEvent set(OID oid, OctetString value, int agent_id) throws IOException {
		return myAgent.get(agent_id).set(oid, value);
	}

	@SuppressWarnings("rawtypes")
	public boolean setTableValue(MOTable motb, OID oid, OctetString value, int agent_id) {
		return myAgent.get(agent_id).setTableValue(motb, oid, value);

	}
	
	@SuppressWarnings("rawtypes")
	public boolean addRowToTable(MOTable motb, OID oid, Variable[] variables, int agent_id) {
		return myAgent.get(agent_id).addRowToTable(motb, oid, variables);
	}

	public ResponseEvent getNext(OID oid, int agent_id) throws IOException {
		return myAgent.get(agent_id).getNext(oid);
	}
	
	public ResponseEvent getBulk(OID oid, int maxRepetition, int agent_id) throws IOException {
		return myAgent.get(agent_id).getBulk(oid, maxRepetition);
	}
	
	public ResponseEvent inform(OID oid, int agent_id) throws IOException {
		return myAgent.get(agent_id).inform(oid);
	}

	public void stop() {
		for (MyAgent ma : myAgent) {
			ma.stop();
		}
	}
}
