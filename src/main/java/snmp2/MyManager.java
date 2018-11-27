package snmp2;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.CommunityTarget;
import org.snmp4j.MessageDispatcher;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.MessageException;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.agent.mo.MOTable;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.mp.StatusInformation;
import org.snmp4j.security.Priv3DES;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

public class MyManager implements CommandResponder {

	private List<MyAgent> myAgent;
	private String address;

	@SuppressWarnings("rawtypes")
	private TransportMapping transport = null;
	private ThreadPool threadPool = null;

	public MyManager(List<MyAgent> myAgent, String address) throws IOException {
		this.myAgent = myAgent;
		this.address = address;
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

	public void stop() throws IOException {
		for (MyAgent ma : myAgent) {
			ma.stop();
		}
		if (transport != null) {
			transport.close();
		}
		if (threadPool != null) {
			threadPool.stop();
		}
	}

	@SuppressWarnings({ "unchecked" })
	public synchronized void listen() throws IOException {
		threadPool = ThreadPool.create("PoolName", 10);
		MessageDispatcher mtDispatcher = new MultiThreadedMessageDispatcher(threadPool, new MessageDispatcherImpl());

		mtDispatcher.addMessageProcessingModel(new MPv1());
		mtDispatcher.addMessageProcessingModel(new MPv2c());

		SecurityProtocols.getInstance().addDefaultProtocols();
		SecurityProtocols.getInstance().addPrivacyProtocol(new Priv3DES());

		// Create Target
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString("public"));

		transport = new DefaultUdpTransportMapping(new UdpAddress(address));

		Snmp snmp = new Snmp(mtDispatcher, transport);
		snmp.addCommandResponder(this);

		transport.listen();
	}

	public void processPdu(CommandResponderEvent event) {
		PDU pdu = event.getPDU();
		if (pdu != null) {
			System.out.println(pdu);
			if (pdu.getType() == PDU.INFORM) {
				pdu.setErrorIndex(0);
				pdu.setErrorStatus(0);
				pdu.setType(PDU.RESPONSE);

				try {
					event.getMessageDispatcher().returnResponsePdu(event.getMessageProcessingModel(),
							event.getSecurityModel(), event.getSecurityName(), event.getSecurityLevel(), pdu,
							event.getMaxSizeResponsePDU(), event.getStateReference(), new StatusInformation());
				} catch (MessageException e) {
					e.printStackTrace();
				}
			}
		}

	}
}
