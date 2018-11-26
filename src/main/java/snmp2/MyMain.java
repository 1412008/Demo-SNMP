package snmp2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
			MyAgent agent = new MyAgent("127.0.0.1/" + port);
			agent.unregisterManagedObject(agent.getSnmpv2MIB());
			
			List<MyAgent> list = new ArrayList<MyAgent>();
			list.add(agent);
			
			MyManager mng = new MyManager(list);

			String oid = "1.3.6.1.2.1.2.2";
			AgentHelper.createMOs1(agent, oid + ".1", oid + ".2");

			System.out.println(mng.getAsString(new OID(oid + ".1.1.0"), 0));
			System.out.println(mng.getAsString(new OID(oid + ".2.2.0"), 0));

//			System.out.println("-------------------------------------");
//			System.out.println(mng.getAsString(new OID(oid + ".1.1.0")));
//			mng.set(new OID(oid + ".1.1.0"), new OctetString("hello world"));
//			System.out.println(mng.getAsString(new OID(oid + ".1.1.0")));

			// Map<String, String> map = mng.viewTree(new OID(oid));
			// printMap(map);

			System.out.println("-------------------------------------");
			String rootOid = "1.3.6.1.2.1.2.2.3";
			AgentHelper.createMOs2(agent, rootOid);

			@SuppressWarnings("rawtypes")
			MOTable mo = (MOTable) agent.getServer().getManagedObject(new OID(oid + ".3"), null);
			
			System.out.println(mng.setTableValue(mo, new OID(oid + ".3.1.2"), new OctetString("he he"), 0));
			Variable[] variables = new OctetString[] { new OctetString("ss"), new OctetString("ss2"),
					new OctetString("ss3") };
			mng.addRowToTable(mo, new OID("2.1"), variables, 0);
			// System.out.println(mo);
			// PDU pdu = mng.set(new OID(oid + ".3.2.1.1"), new
			// OctetString("hhh")).getResponse();
			// System.out.println(pdu);

			Map<String, String> map2 = mng.getAsMap(new OID(oid), 0);

			printMap(map2);

			System.out.println("-------------------------------------");
			System.out.println(mng.getNext(new OID(oid + ".3.1.1"), 0).getResponse());
			System.out.println("-------------------------------------");
			System.out.println(mng.getBulk(new OID(oid + ".3"), 3, 0).getResponse());
			System.out.println("-------------------------------------");
			//System.out.println(mng.inform(new OID(oid + ".1.1.0")).getResponse());
			
			mng.listen();
			agent.sendV1Trap();
			Thread.sleep(1000);
			agent.sendV1Trap();
			Thread.sleep(2000);
			agent.sendV1Trap();
			
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
