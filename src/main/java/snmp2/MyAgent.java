package snmp2;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.Target;
import org.snmp4j.agent.BaseAgent;
import org.snmp4j.agent.CommandProcessor;
import org.snmp4j.agent.DuplicateRegistrationException;
import org.snmp4j.agent.MOGroup;
import org.snmp4j.agent.ManagedObject;
import org.snmp4j.agent.mo.DefaultMOMutableRow2PC;
import org.snmp4j.agent.mo.MOTable;
import org.snmp4j.agent.mo.snmp.RowStatus;
import org.snmp4j.agent.mo.snmp.SnmpCommunityMIB;
import org.snmp4j.agent.mo.snmp.SnmpNotificationMIB;
import org.snmp4j.agent.mo.snmp.SnmpTargetMIB;
import org.snmp4j.agent.mo.snmp.StorageType;
import org.snmp4j.agent.mo.snmp.VacmMIB;
import org.snmp4j.agent.mo.snmp.SnmpCommunityMIB.SnmpCommunityEntryRow;
import org.snmp4j.agent.security.MutableVACM;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModel;
import org.snmp4j.security.USM;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeUtils;

public class MyAgent extends BaseAgent {

	private String managerAddress;
	private String address;
	private final String communityName = "public";
	private final String securityName = "spublic";
	private final String defaultContextName = "public";
	private final String groupName = "v1v2group";
	private final String readView = "fullReadView";
	private final String writeView = "fullWriteView";
	private final String noticeView = "fullNotifyView";

	protected MyAgent(String address) throws IOException {
		super(new File("conf.agent"), new File("bootCounter.agent"),
				new CommandProcessor(new OctetString(MPv3.createLocalEngineID())));
		this.address = address;

		start();
	}

	@Override
	protected void registerManagedObjects() {
	}

	public void registerManagedObject(ManagedObject managedObject) throws DuplicateRegistrationException {
		server.register(managedObject, null);
	}

	public void registerManagedObject(ManagedObject managedObject, OctetString contText)
			throws DuplicateRegistrationException {
		server.register(managedObject, contText);
	}

	@Override
	protected void unregisterManagedObjects() {
	}

	public void unregisterManagedObject(MOGroup moGroup) {
		moGroup.unregisterMOs(server, getContext(moGroup));
	}

	@Override
	protected void addUsmUser(USM usm) {
	}

	@Override
	protected void addNotificationTargets(SnmpTargetMIB targetMIB, SnmpNotificationMIB notificationMIB) {
	}

	@Override
	protected void addViews(VacmMIB vacmMIB) {
		vacmMIB.addGroup(SecurityModel.SECURITY_MODEL_SNMPv2c, new OctetString(securityName),
				new OctetString(groupName), StorageType.nonVolatile);
		vacmMIB.addAccess(new OctetString(groupName), new OctetString(communityName), SecurityModel.SECURITY_MODEL_ANY,
				SecurityLevel.NOAUTH_NOPRIV, MutableVACM.VACM_MATCH_EXACT, new OctetString(readView),
				new OctetString(writeView), new OctetString(noticeView), StorageType.nonVolatile);
		vacmMIB.addViewTreeFamily(new OctetString(readView), new OID("1.3"), new OctetString(),
				VacmMIB.VACM_VIEW_INCLUDED, StorageType.nonVolatile);
		vacmMIB.addViewTreeFamily(new OctetString(writeView), new OID("1.3"), new OctetString(),
				VacmMIB.VACM_VIEW_INCLUDED, StorageType.nonVolatile);
	}

	@Override
	protected void addCommunities(SnmpCommunityMIB communityMIB) {
		Variable[] vars = new Variable[] { new OctetString(communityName), new OctetString(securityName),
				getAgent().getContextEngineID(), new OctetString(defaultContextName), new OctetString(),
				new Integer32(StorageType.nonVolatile), new Integer32(RowStatus.active) };
		SnmpCommunityEntryRow row = communityMIB.getSnmpCommunityEntry()
				.createRow(new OctetString("random").toSubIndex(true), vars);
		communityMIB.getSnmpCommunityEntry().addRow(row);
	}

	@Override
	protected void initTransportMappings() throws IOException {
		transportMappings = new DefaultUdpTransportMapping[] {
				new DefaultUdpTransportMapping(new UdpAddress(address)) };
	}

	public void start() throws IOException {
		init();
		addShutdownHook();
		getServer().addContext(new OctetString(defaultContextName));
		finishInit();
		run();
		sendColdStartNotification();
		System.out.println("init agent");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean addRowToTable(MOTable motb, OID oid, Variable[] variables) {
		return motb.addRow(new DefaultMOMutableRow2PC(oid, variables));
	}

	@SuppressWarnings("rawtypes")
	public boolean setTableValue(MOTable motb, OID oid, OctetString value) {
		VariableBinding vb = new VariableBinding(oid, value);
		return motb.setValue(vb);
	}

	private Target getTarget() {
		Address targetAddress = GenericAddress.parse(address);
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString(communityName));
		target.setAddress(targetAddress);
		target.setRetries(2);
		target.setTimeout(1500);
		target.setVersion(SnmpConstants.version2c);
		return target;
	}

	public ResponseEvent get(OID oid) throws IOException {
		PDU pdu = new PDU();
		pdu.add(new VariableBinding(oid));
		pdu.setType(PDU.GET);
		ResponseEvent event = getSession().send(pdu, getTarget(), null);
		if (event != null) {
			return event;
		}
		throw new RuntimeException("GET timed out");
	}

	public ResponseEvent set(OID oid, OctetString value) throws IOException {
		PDU pdu = new PDU();
		Variable var = value;
		VariableBinding vb = new VariableBinding(oid, var);
		pdu.add(vb);
		pdu.setType(PDU.SET);
		ResponseEvent event = getSession().set(pdu, getTarget());
		if (event != null) {
			return event;
		}
		throw new RuntimeException("SET timed out");
	}

	public ResponseEvent getNext(OID oid) throws IOException {
		PDU pdu = new PDU();
		pdu.add(new VariableBinding(oid));
		pdu.setType(PDU.GETNEXT);
		ResponseEvent event = getSession().getNext(pdu, getTarget());
		if (event != null) {
			return event;
		}
		throw new RuntimeException("GETNEXT timed out");
	}

	public ResponseEvent getBulk(OID oid, int maxRepetition) throws IOException {
		PDU pdu = new PDU();
		pdu.add(new VariableBinding(oid));
		pdu.setType(PDU.GETBULK);
		pdu.setMaxRepetitions(maxRepetition);
		pdu.setNonRepeaters(0);
		ResponseEvent event = getSession().getBulk(pdu, getTarget());
		if (event != null) {
			return event;
		}
		throw new RuntimeException("GETBULK timed out");
	}

	public ResponseEvent inform(OID oid) throws IOException {
		PDU pdu = new PDU();
		pdu.add(new VariableBinding(oid));
		pdu.setType(PDU.INFORM);
		ResponseEvent event = getSession().inform(pdu, getTarget());
		if (event != null) {
			return event;
		}
		throw new RuntimeException("INFORM timed out");
	}

	public Map<String, String> getAsMap(OID oid) {
		Map<String, String> result = new TreeMap<String, String>();
		TreeUtils treeUtils = new TreeUtils(getSession(), new DefaultPDUFactory());
		List<TreeEvent> events = treeUtils.getSubtree(getTarget(), oid);
		for (TreeEvent treeEvent : events) {
			VariableBinding[] vbs = treeEvent.getVariableBindings();
			for (VariableBinding vb : vbs) {
				result.put("." + vb.getOid().toString(), vb.getVariable().toString());
			}
		}

		return result;
	}

	public void sendV1Trap() throws IOException {
		if (managerAddress == null || managerAddress.isEmpty()) {
			return;
		}
		CommunityTarget comtarget = new CommunityTarget();
		comtarget.setCommunity(new OctetString(communityName));
		comtarget.setVersion(SnmpConstants.version1);
		comtarget.setAddress(new UdpAddress(managerAddress));
		comtarget.setRetries(2);
		comtarget.setTimeout(1000);

		PDUv1 pdu = new PDUv1();
		pdu.setType(PDU.V1TRAP);
		pdu.setEnterprise(new OID("1.3.6.1.2.1.1.6"));
		pdu.setGenericTrap(PDUv1.ENTERPRISE_SPECIFIC);
		pdu.setSpecificTrap(1);
		pdu.setAgentAddress(new IpAddress("127.0.0.1"));
		pdu.setTimestamp(48 * 60 * 60 * 100);
		pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.2.2.3"), new OctetString("some error occured!")));

		getSession().trap(pdu, comtarget);
	}

	public void sendV2Trap() throws IOException {
		CommunityTarget communityTarget = getManager();
		if (communityTarget == null) {
			return;
		}
		
		PDU pdu = new PDU();
		pdu.add(new VariableBinding(SnmpConstants.sysUpTime, new OctetString(new Date().toString())));
		pdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID("1.3.6.1.2.1.1.6")));
		pdu.add(new VariableBinding(SnmpConstants.snmpTrapAddress, new IpAddress("127.0.0.1")));

		// pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.6"), new
		// OctetString("Major")));
		pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.2.2.3"), new OctetString("some error occured!")));
		pdu.setType(PDU.NOTIFICATION);

		getSession().send(pdu, communityTarget);
	}

	public void inform() throws IOException {
		CommunityTarget communityTarget = getManager();
		if (communityTarget == null) {
			return;
		}
		
		PDU pdu = new PDU();
		pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.2.2.3"), new OctetString("some error occured!")));
		pdu.add(new VariableBinding(SnmpConstants.sysUpTime, new OctetString(new Date().toString())));

		pdu.setType(PDU.INFORM);
		
		//PDU res = getSession().inform(pdu, communityTarget).getResponse();
		ResponseEvent res = getSession().inform(pdu, communityTarget);
		System.out.println(res.getResponse());
	}

	public void setManagerAddress(String managerAddress) {
		this.managerAddress = managerAddress;
	}

	public CommunityTarget getManager() {
		if (managerAddress == null || managerAddress.isEmpty()) {
			return null;
		}
		CommunityTarget comtarget = new CommunityTarget();
		comtarget.setCommunity(new OctetString(communityName));
		comtarget.setVersion(SnmpConstants.version2c);
		comtarget.setAddress(new UdpAddress(managerAddress));
		comtarget.setRetries(2);
		comtarget.setTimeout(1000);
		return comtarget;
	}
}
