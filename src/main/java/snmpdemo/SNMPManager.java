package snmpdemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeUtils;

public class SNMPManager {
	private String address;

	private Snmp snmp;
	@SuppressWarnings("rawtypes")
	private TransportMapping transport;

	public SNMPManager(String address) {
		super();
		this.address = address;
		try {
			start();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private void start() throws IOException {
		transport = new DefaultUdpTransportMapping();
		snmp = new Snmp(transport);

		transport.listen();
	}

	public void stop() throws IOException {
		transport.close();
	}

	public String getAsString(OID oid) throws IOException {
		ResponseEvent event = get(oid);
		PDU pdu = event.getResponse();
		return pdu.get(0).getVariable().toString();
	}

	public ResponseEvent get(OID oid) throws IOException {
		PDU pdu = new PDU();
		pdu.add(new VariableBinding(oid));
		pdu.setType(PDU.GET);
		ResponseEvent event = snmp.send(pdu, getTarget(), null);
		if (event != null) {
			return event;
		}
		throw new RuntimeException("GET timed out");
	}

	private Target getTarget() {
		Address targetAddress = GenericAddress.parse(address);
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString("public"));
		target.setAddress(targetAddress);
		target.setRetries(2);
		target.setTimeout(1500);
		target.setVersion(SnmpConstants.version2c);
		return target;
	}

	public Map<String, String> viewTree(OID oid) {
		Map<String, String> result = new TreeMap<String, String>();
		TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
		List<TreeEvent> events = treeUtils.getSubtree(getTarget(), oid);
		for (TreeEvent treeEvent : events) {
			VariableBinding[] vbs = treeEvent.getVariableBindings();
			for (VariableBinding vb : vbs) {
				result.put("." + vb.getOid().toString(), vb.getVariable().toString());
			}
		}

		return result;
	}
	
	public ResponseEvent set(OID oid, OctetString value) throws IOException {
		PDU pdu = new PDU();
		Variable var = value;
		VariableBinding vb = new VariableBinding(oid, var);
		pdu.add(vb);
		pdu.setType(PDU.SET);
		ResponseEvent event = snmp.set(pdu, getTarget());
		if (event != null) {
			return event;
		}
		throw new RuntimeException("SET timed out");
	}
	
	public List<List<String>> getTableAsStrings(OID[] oids) {
		TableUtils tUtils = new TableUtils(snmp, new DefaultPDUFactory());

		List<TableEvent> events = tUtils.getTable(getTarget(), oids, null, null);

		List<List<String>> list = new ArrayList<List<String>>();
		for (TableEvent event : events) {
			if (event.isError()) {
				throw new RuntimeException(event.getErrorMessage());
			}
			List<String> strList = new ArrayList<String>();
			list.add(strList);
			VariableBinding[] vbs = event.getColumns();
			for (VariableBinding vb : vbs) {
				strList.add(vb.getVariable().toString());
			}
		}
		return list;
	}
}
