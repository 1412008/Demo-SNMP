package snmpdemo;

import java.io.File;
import java.io.IOException;

import org.snmp4j.agent.BaseAgent;
import org.snmp4j.agent.CommandProcessor;
import org.snmp4j.agent.DuplicateRegistrationException;
import org.snmp4j.agent.MOGroup;
import org.snmp4j.agent.ManagedObject;
import org.snmp4j.agent.mo.snmp.RowStatus;
import org.snmp4j.agent.mo.snmp.SnmpCommunityMIB;
import org.snmp4j.agent.mo.snmp.SnmpCommunityMIB.SnmpCommunityEntryRow;
import org.snmp4j.agent.mo.snmp.SnmpNotificationMIB;
import org.snmp4j.agent.mo.snmp.SnmpTargetMIB;
import org.snmp4j.agent.mo.snmp.StorageType;
import org.snmp4j.agent.mo.snmp.VacmMIB;
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
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class Agent extends BaseAgent {

	private String address;

	private final String communityName = "public";
	private final String securityName = "spublic";
	private final String defaultContextName = "public";
	private final String groupName = "v1v2group";

	public Agent(String address) {
		super(new File("conf.agent"), new File("bootCounter.agent"),
				new CommandProcessor(new OctetString(MPv3.createLocalEngineID())));
		OctetString tmp = new OctetString(MPv3.createLocalEngineID());
		System.out.println(tmp);
		this.address = address;
	}

	public void start() throws IOException {
		init();
		addShutdownHook();
		getServer().addContext(new OctetString(defaultContextName));
		finishInit();
		run();
		sendColdStartNotification();
	}

	@Override
	protected void initTransportMappings() throws IOException {
		transportMappings = new DefaultUdpTransportMapping[] {
				new DefaultUdpTransportMapping(new UdpAddress(address)) };
	}

	@Override
	protected void addCommunities(SnmpCommunityMIB comMIB) {
		Variable[] com2sec = new Variable[] { new OctetString(communityName), new OctetString(securityName),
				getAgent().getContextEngineID(), new OctetString(defaultContextName), new OctetString(),
				new Integer32(StorageType.nonVolatile), new Integer32(RowStatus.active) };
		SnmpCommunityEntryRow row = comMIB.getSnmpCommunityEntry()
				.createRow(new OctetString("public2public").toSubIndex(true), com2sec);
		comMIB.getSnmpCommunityEntry().addRow(row);
	}

	@Override
	protected void addNotificationTargets(SnmpTargetMIB target, SnmpNotificationMIB noti) {
	}

	@Override
	protected void addUsmUser(USM usm) {
	}

	@Override
	protected void addViews(VacmMIB vacmMIB) {
		vacmMIB.addGroup(SecurityModel.SECURITY_MODEL_SNMPv2c, new OctetString(securityName),
				new OctetString(groupName), StorageType.nonVolatile);
		vacmMIB.addAccess(new OctetString(groupName), new OctetString(communityName), SecurityModel.SECURITY_MODEL_ANY,
				SecurityLevel.NOAUTH_NOPRIV, MutableVACM.VACM_MATCH_EXACT, new OctetString("fullReadView"),
				new OctetString("fullWriteView"), new OctetString("fullNotifyView"), StorageType.nonVolatile);
		vacmMIB.addViewTreeFamily(new OctetString("fullReadView"), new OID("1.3"), new OctetString(),
				VacmMIB.VACM_VIEW_INCLUDED, StorageType.nonVolatile);
	}

	@Override
	protected void registerManagedObjects() {
	}

	public void registerManagedObject(ManagedObject mo) {
		try {
			server.register(mo, null);
		} catch (DuplicateRegistrationException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	protected void unregisterManagedObjects() {
	}

	public void unregisterManagedObject(MOGroup moGroup) {
		moGroup.unregisterMOs(server, getContext(moGroup));
	}

}
