package snmp2;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.snmp4j.PDU;
import org.snmp4j.agent.ManagedObject;
import org.snmp4j.agent.mo.DefaultMOMutableRow2PC;
import org.snmp4j.agent.mo.MOChangeEvent;
import org.snmp4j.agent.mo.MOChangeListener;
import org.snmp4j.agent.mo.MOTable;
import org.snmp4j.agent.mo.MOTableRow;
import org.snmp4j.agent.mo.MOTableRowEvent;
import org.snmp4j.agent.mo.MOTableRowListener;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

public class MyMain {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		try {
			String port = "2001";
			MyManager mng = new MyManager("127.0.0.1/" + port);
			MyAgent agent = new MyAgent("0.0.0.0/" + port);
			agent.unregisterManagedObject(agent.getSnmpv2MIB());

			String oid = "1.3.6.1.2.1.2.2";
			AgentHelper.createMOs1(agent, oid + ".1", oid + ".2");

			System.out.println(mng.getAsString(new OID(oid + ".1.1.0")));
			System.out.println(mng.getAsString(new OID(oid + ".2.2.0")));

			System.out.println("-------------------------------------");

			mng.setMyAgent(agent);
			System.out.println(mng.getAsString(new OID(oid + ".1.1.0")));
			mng.set(new OID(oid + ".1.1.0"), new OctetString("hello world"));
			System.out.println(mng.getAsString(new OID(oid + ".1.1.0")));

			// Map<String, String> map = mng.viewTree(new OID(oid));
			// printMap(map);

			System.out.println("-------------------------------------");
			String rootOid = "1.3.6.1.2.1.2.2.3";
			AgentHelper.createMOs2(agent, rootOid);

			@SuppressWarnings("rawtypes")
			MOTable mo = (MOTable) agent.getServer().getManagedObject(new OID(oid + ".3"), null);
			
			System.out.println(mng.setTableValue(mo, new OID(oid + ".3.1.2"), new OctetString("he he")));
			Variable[] variables = new OctetString[] { new OctetString("ss"), new OctetString("ss2"),
					new OctetString("ss3") };
			mng.addRowToTable(mo, new OID("2.1"), variables);
			// System.out.println(mo);
			// PDU pdu = mng.set(new OID(oid + ".3.2.1.1"), new
			// OctetString("hhh")).getResponse();
			// System.out.println(pdu);

			Map<String, String> map2 = mng.viewTree(new OID(oid));

			printMap(map2);

			agent.stop();
			mng.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void printMap(Map<String, String> m) {
		Iterator<Entry<String, String>> iter = m.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			System.out.println(entry.getKey() + " " + entry.getValue());
		}
	}
}
