package snmpdemo;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.snmp4j.agent.mo.MOAccessImpl;
import org.snmp4j.agent.mo.MOScalar;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.Gauge32;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.SMIConstants;
import org.snmp4j.smi.Variable;

public class MainApp {

	public static void main(String[] args) {
		try {
//        SNMPManager client = new SNMPManager("udp:127.0.0.1/161");
//        String sysDescr = client.getAsString(new OID(".1.3.6.1.2.1.1.1.0"));
//        System.out.println(sysDescr);

			String port = "2001";
			Agent agent = new Agent("0.0.0.0/" + port);
			agent.start();
//			agent.unregisterManagedObject(agent.getSnmpv2MIB());

			MOTableBuilder builder = new MOTableBuilder(new OID(".1.3.6.1.2.1.2.2.1"))
					.addColumnType(SMIConstants.SYNTAX_INTEGER, MOAccessImpl.ACCESS_READ_WRITE)
					.addColumnType(SMIConstants.SYNTAX_OCTET_STRING, MOAccessImpl.ACCESS_READ_WRITE)
					.addColumnType(SMIConstants.SYNTAX_INTEGER, MOAccessImpl.ACCESS_READ_WRITE)
					.addColumnType(SMIConstants.SYNTAX_INTEGER, MOAccessImpl.ACCESS_READ_WRITE)
					.addColumnType(SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_WRITE)
					.addColumnType(SMIConstants.SYNTAX_OCTET_STRING, MOAccessImpl.ACCESS_READ_WRITE)
					.addColumnType(SMIConstants.SYNTAX_INTEGER, MOAccessImpl.ACCESS_READ_WRITE)
					.addColumnType(SMIConstants.SYNTAX_INTEGER, MOAccessImpl.ACCESS_READ_WRITE)
					// Row 0
					.addRowValue(new Integer32(1)).addRowValue(new OctetString("col 2")).addRowValue(new Integer32(24))
					.addRowValue(new Integer32(500)).addRowValue(new Gauge32(10000000))
					.addRowValue(new OctetString("00:00:00:00:01")).addRowValue(new Integer32(53))
					.addRowValue(new Integer32(69))
					// Row 1
					.addRowValue(new Integer32(2)).addRowValue(new OctetString("alcso at col 2"))
					.addRowValue(new Integer32(45)).addRowValue(new Integer32(1000)).addRowValue(new Gauge32(20000000))
					.addRowValue(new OctetString("00:00:00:00:02")).addRowValue(new Integer32(35))
					.addRowValue(new Integer32(96))
					// Row 2
					.addRowValue(new Integer32(3)).addRowValue(new OctetString("2 - 3")).addRowValue(new Integer32(66))
					.addRowValue(new Integer32(1500)).addRowValue(new Gauge32(30000000))
					.addRowValue(new OctetString("00:00:00:00:03")).addRowValue(new Integer32(353))
					.addRowValue(new Integer32(696));

			agent.registerManagedObject(builder.build());

			OID testoid = new OID(".1.3.6.1.2.1.2.2.2.1.2");
			agent.registerManagedObject(new MOScalar<Variable>(testoid, MOAccessImpl.ACCESS_READ_WRITE,
					new OctetString("test string")));
			
			SNMPManager mnger = new SNMPManager("udp:127.0.0.1/" + port);
			String tmpAdd = ".1.3.6.1.2.1.2.2.1";

			System.out.println(mnger.getAsString(testoid));
			
			Map<String, String> m = mnger.viewTree(new OID(".1.3.6.1.2.1.2.2"));
			MainApp.printMap(m);
			

			System.out.println("---------------");
			System.out.println(mnger.getAsString(new OID(tmpAdd + ".2.2")));
			System.out.println("---------------");
			ResponseEvent res = mnger.set(testoid, new OctetString("hmmm"));
			System.out.println(res.getResponse());
			System.out.println("." + res.getResponse().get(0).getOid() + " " + res.getResponse().get(0).getVariable());
			System.out.println("---------------");
			//Thread.sleep(500);
			//MainApp.printMap(mnger.viewTree(new OID(".1.3.6.1.2.1.2.2")));
			System.out.println(mnger.getAsString(testoid));
			
			System.out.println(mnger.set(new OID(tmpAdd + ".1.1"), new OctetString("5454")).getResponse());
			
//			for (int i = 1; i <= 8; i++) {
//				System.out.println(client.getTableAsStrings(new OID[] { new OID(tmpAdd + "." + i) }));
//			}
//
//			System.out.println(client.getTableAsStrings(
//					new OID[] { new OID(tmpAdd + ".3"), new OID(tmpAdd + ".5"), new OID(tmpAdd + ".7") }));
//
//			System.out.println(client.getTableAsStrings(
//					new OID[] { new OID(tmpAdd + ".3"), new OID(tmpAdd + ".5"), new OID(tmpAdd + ".7") }));

			agent.stop();
			mnger.stop();
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
