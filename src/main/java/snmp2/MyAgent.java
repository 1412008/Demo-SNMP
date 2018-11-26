package snmp2;

import java.io.File;
import java.io.IOException;

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
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModel;
import org.snmp4j.security.USM;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class MyAgent extends BaseAgent {

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
	}

	public void sendTrap(PDU pdu, Target target) throws IOException {
		this.getSession().trap((PDUv1) pdu, target);
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
}
