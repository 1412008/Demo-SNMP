package snmp2;

import org.snmp4j.agent.DuplicateRegistrationException;
import org.snmp4j.agent.mo.MOAccessImpl;
import org.snmp4j.agent.mo.MOChangeEvent;
import org.snmp4j.agent.mo.MOChangeListener;
import org.snmp4j.agent.mo.MOScalar;
import org.snmp4j.agent.mo.MOTable;
import org.snmp4j.agent.mo.MOTableRow;
import org.snmp4j.agent.mo.MOTableRowEvent;
import org.snmp4j.agent.mo.MOTableRowListener;
import org.snmp4j.smi.Gauge32;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.SMIConstants;
import org.snmp4j.smi.Variable;

public class AgentHelper {

	public static void createMOs1(MyAgent myAgent, String oid1, String oid2) throws DuplicateRegistrationException {
		MOScalar<Variable> mo1 = new MOScalar<Variable>(new OID(oid1 + ".1.0"), MOAccessImpl.ACCESS_READ_WRITE,
				new OctetString("test string"));
		mo1.addMOChangeListener(new MOChangeListener() {
			
			public void beforePrepareMOChange(MOChangeEvent changeEvent) {
				System.out.println("1 " + changeEvent);				
			}
			
			public void beforeMOChange(MOChangeEvent changeEvent) {
				System.out.println("3 " + changeEvent);
				
			}
			
			public void afterPrepareMOChange(MOChangeEvent changeEvent) {
				System.out.println("2 " + changeEvent);
				
			}
			
			public void afterMOChange(MOChangeEvent changeEvent) {
				System.out.println("4 " + changeEvent);
				
			}
		});
		
		myAgent.registerManagedObject(mo1);
		myAgent.registerManagedObject(new MOScalar<Variable>(new OID("1.3.6.1.2.1.2.2.4.0"), MOAccessImpl.ACCESS_READ_WRITE,
				new OctetString("ok")));
		myAgent.registerManagedObject(new MOScalar<Variable>(new OID(oid1 + ".2.0"), MOAccessImpl.ACCESS_READ_WRITE,
				new OctetString("2018")));
		myAgent.registerManagedObject(new MOScalar<Variable>(new OID(oid1 + ".3.0"), MOAccessImpl.ACCESS_READ_WRITE,
				new OctetString("192.168.1.1")));
		
		myAgent.registerManagedObject(new MOScalar<Variable>(new OID(oid2 + ".1.0"), MOAccessImpl.ACCESS_READ_WRITE,
				new OctetString("Lorem lorem")));
		myAgent.registerManagedObject(new MOScalar<Variable>(new OID(oid2 + ".2.0"), MOAccessImpl.ACCESS_READ_WRITE,
				new OctetString("11/26/2018")));
		myAgent.registerManagedObject(new MOScalar<Variable>(new OID(oid2 + ".3.0"), MOAccessImpl.ACCESS_READ_WRITE,
				new OctetString("AC:T5:KB:E3:Y5:CS")));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void createMOs2(MyAgent myAgent, String rootOid) throws DuplicateRegistrationException {		
		MOTableBuilder builder = new MOTableBuilder(new OID(rootOid))
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
		
		MOTable motb = builder.build();
		motb.addMOTableRowListener(new MOTableRowListener<MOTableRow>() {
			public void rowChanged(MOTableRowEvent<MOTableRow> event) {
				System.out.println("row changed");
			}
		});
		
		myAgent.registerManagedObject(motb);
	}

}
