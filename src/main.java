import java.io.IOException;
import java.util.Scanner;

import javax.management.modelmbean.RequiredModelMBean;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class main {

	private static String ipAddress = "127.0.0.1";
	private static String port = "161";
	private static String community = "public";

	// OID of MIB RFC 1213; Scalar Object =
	// .iso.org.dod.internet.mgmt.mib-2.system.sysDescr
	private static String oidValue = ".1.3.6.1.2.1.1.1";

	private static String instance = "0";

	private static Integer requestId = 1;

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		System.out.println("Olá!");

		Scanner ss = new Scanner(System.in);

		// *** IP
		System.out.println("Digite o IP (default 127.0.0.1):");
		String ip = ss.nextLine();
		if (!ip.isEmpty())
			ipAddress = ip;

		// *** Comunnity
		System.out.println("Digite a comunidade (default public):");
		String cm = ss.nextLine();
		if (!cm.isEmpty())
			community = cm;

		// *** Init

		int opt = 2;
		// // *** Opt
		// System.out.println("Informe a opção desejada");
		// String cm = ss.nextLine();
		// if (!cm.isEmpty())
		// community = cm;

		String oid, inst;
		switch (opt) {
		case 1: // GET

			System.out.println("Informe o OID: (default: " + oidValue + "):");
			oid = ss.nextLine();
			if (!oid.isEmpty())
				oidValue = oid;

			System.out.println("Informe a instância (default: " + instance
					+ "):");
			inst = ss.nextLine();
			if (!inst.isEmpty())
				instance = inst;

			snmpGet();

			break;

		case 2: // GETNEXT

			System.out.println("Informe o OID: (default: " + oidValue + "):");
			oid = ss.nextLine();
			if (!oid.isEmpty())
				oidValue = oid;

			System.out.println("Informe a instância (default: " + instance
					+ "):");
			inst = ss.nextLine();
			if (!inst.isEmpty())
				instance = inst;

			snmpGetNext();

			break;

		default:
			break;
		}

	}

	private static void snmpGet() throws IOException {

		// Create TransportMapping and Listen
		TransportMapping transport = new DefaultUdpTransportMapping();
		transport.listen();

		// Create Target Address object
		CommunityTarget comtarget = new CommunityTarget();
		comtarget.setCommunity(new OctetString(community));
		comtarget.setVersion(SnmpConstants.version2c);
		comtarget.setAddress(new UdpAddress(ipAddress + "/" + port));
		comtarget.setRetries(2);
		comtarget.setTimeout(1500);

		// Create Snmp object for sending data to Agent
		Snmp snmp = new Snmp(transport);

		// Create the PDU object
		PDU pdu = new PDU();
		pdu.add(new VariableBinding(new OID(oidValue + "." + instance)));
		pdu.setType(PDU.GET);
		pdu.setRequestID(new Integer32(requestId++));

		System.out.println("Sending Request to Agent...");
		ResponseEvent response = snmp.get(pdu, comtarget);

		// Process Agent Response
		if (response != null) {
			System.out.println("Got Response from Agent");
			PDU responsePDU = response.getResponse();

			if (responsePDU != null) {
				int errorStatus = responsePDU.getErrorStatus();
				int errorIndex = responsePDU.getErrorIndex();
				String errorStatusText = responsePDU.getErrorStatusText();

				if (errorStatus == PDU.noError) {
					System.out.println("Snmp Get Response = "
							+ responsePDU.getVariableBindings());
				} else {
					System.out.println("Error: Request Failed");
					System.out.println("Error Status = " + errorStatus);
					System.out.println("Error Index = " + errorIndex);
					System.out
							.println("Error Status Text = " + errorStatusText);
				}
			} else {
				System.out.println("Error: Response PDU is null");
			}
		} else {
			System.out.println("Error: Agent Timeout... ");
		}

		snmp.close();
	}

	private static void snmpGetNext() throws IOException {

		// Create TransportMapping and Listen
		TransportMapping transport = new DefaultUdpTransportMapping();
		transport.listen();

		// Create Target Address object
		CommunityTarget comtarget = new CommunityTarget();
		comtarget.setCommunity(new OctetString(community));
		comtarget.setVersion(SnmpConstants.version2c);
		comtarget.setAddress(new UdpAddress(ipAddress + "/" + port));
		comtarget.setRetries(2);
		comtarget.setTimeout(1500);

		// Create Snmp object for sending data to Agent
		Snmp snmp = new Snmp(transport);

		// Create the PDU object
		PDU pdu = new PDU();
		pdu.add(new VariableBinding(new OID(oidValue + "." + instance)));
		pdu.setType(PDU.GETNEXT);
		pdu.setRequestID(new Integer32(requestId++));

		System.out.println("Sending Request to Agent...");
		ResponseEvent response = snmp.get(pdu, comtarget);

		// Process Agent Response
		if (response != null) {
			System.out
					.println("\nResponse:\nGot GetNext Response from Agent...");
			PDU responsePDU = response.getResponse();

			if (responsePDU != null) {
				int errorStatus = responsePDU.getErrorStatus();
				int errorIndex = responsePDU.getErrorIndex();
				String errorStatusText = responsePDU.getErrorStatusText();

				if (errorStatus == PDU.noError) {
					System.out
							.println("Snmp GetNext Response for sysObjectID = "
									+ responsePDU.getVariableBindings());
				} else {
					System.out.println("Error: Request Failed");
					System.out.println("Error Status = " + errorStatus);
					System.out.println("Error Index = " + errorIndex);
					System.out
							.println("Error Status Text = " + errorStatusText);
				}
			} else {
				System.out.println("Error: GetNextResponse PDU is null");
			}
		} else {
			System.out.println("Error: Agent Timeout... ");
		}
		snmp.close();
	}
}
