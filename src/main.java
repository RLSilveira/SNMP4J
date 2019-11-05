import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeUtils;

public class main {

	private static String ipAddress = "127.0.0.1";
	private static String port = "161";
	private static String community = "public";

	// OID of MIB RFC 1213; Scalar Object =
	// .iso.org.dod.internet.mgmt.mib-2.system.sysDescr
	private static String oidValue = ".1.3.6.1.2.1.1.1";

	private static String instance = null;

	private static Integer requestId = 1;

	/**
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws IOException,
			InterruptedException {
		// TODO Auto-generated method stub

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

		int opt = -1;
		// *** Opt
		System.out.println("Informe a opção desejada:");
		System.out.println(" 1 - GET");
		System.out.println(" 2 - GETNEXT");
		System.out.println(" 3 - SET");
		System.out.println(" 4 - GETBULK");
		System.out.println(" 5 - WALK");
		System.out.println(" 6 - GETTABLE");
		System.out.println(" 7 - GETDELTA");

		opt = Integer.parseInt(ss.nextLine());

		String oid, inst;
		switch (opt) {
		case 1: // GET

			System.out.println("Informe o OID: (default: " + oidValue + "):");
			oid = ss.nextLine();
			if (!oid.isEmpty())
				oidValue = oid;

			instance = "0";
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

			System.out.println("Informe a instância (default: null):");
			inst = ss.nextLine();
			if (!inst.isEmpty())
				instance = inst;

			snmpGetNext();

			break;

		case 3: // SET

			oidValue = ".1.3.6.1.2.1.1.5";
			System.out.println("Informe o OID: (default: " + oidValue + "):");
			oid = ss.nextLine();
			if (!oid.isEmpty())
				oidValue = oid;

			instance = "0";
			System.out.println("Informe a instância (default: " + instance
					+ "):");
			inst = ss.nextLine();
			if (!inst.isEmpty())
				instance = inst;

			String sValue;
			System.out.println("Informe o novo valor:");
			sValue = ss.nextLine();

			snmpSet(sValue);

			break;

		case 4: // GETBULK

			System.out
					.println("Informe os OIDs (já com a instancia e separados por ';'):");

			oidValue = ".1.3.6.1.2.1.1.1.0;.1.3.6.1.2.1.1.2.0;.1.3.6.1.2.1.1.3.0";
			System.out.println("(default: " + oidValue + ")");
			oid = ss.nextLine();
			if (!oid.isEmpty())
				oidValue = oid;

			int iNonRepeaters = 1;
			System.out.println("Informe o parametro non-repeaters:");
			iNonRepeaters = Integer.parseInt(ss.nextLine());

			int iMaxRepetitions = 1;
			System.out.println("Informe o parametro max-repetitions:");
			iMaxRepetitions = Integer.parseInt(ss.nextLine());

			snmpGetBulk(iNonRepeaters, iMaxRepetitions);

			break;

		case 5: // WALK

			oidValue = ".1.3.6.1.2.1.2.2";
			System.out.println("Informe o OID: (default: " + oidValue + "):");
			oid = ss.nextLine();
			if (!oid.isEmpty())
				oidValue = oid;

			snmpWalk();

			break;

		case 6: // GETTABLE

			oidValue = ".1.3.6.1.2.1.2.2";
			System.out.println("Informe o OID: (default: " + oidValue + "):");
			oid = ss.nextLine();
			if (!oid.isEmpty())
				oidValue = oid;

			snmpGetTable();

			break;

		case 7: // DELTA

			int n = 5;
			System.out.println("Informe o número de requisições: (default: "
					+ n + "):");
			String nn = ss.nextLine();
			if (!nn.isEmpty())
				n = Integer.parseInt(nn);

			int m = 1500;
			System.out
					.println("Informe o intervalo de tempo(ms) entre as requisições: (default: "
							+ m + "ms):");
			String mm = ss.nextLine();
			if (!mm.isEmpty())
				m = Integer.parseInt(mm);

			oidValue = ".1.3.6.1.2.1.4.3";
			System.out.println("Informe o OID: (default: " + oidValue + "):");
			oid = ss.nextLine();
			if (!oid.isEmpty())
				oidValue = oid;

			instance = "0";
			System.out.println("Informe a instância (default: " + instance
					+ "):");
			inst = ss.nextLine();
			if (!inst.isEmpty())
				instance = inst;

			snmpGetDelta(n, m);

			break;

		default:
			System.out.println("Opt inválida!");
			break;
		}

		System.out.println("Fim");
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
		pdu.add(new VariableBinding(new OID(oidValue
				+ (instance == null ? "" : "." + instance))));
		pdu.setType(PDU.GETNEXT);
		pdu.setRequestID(new Integer32(requestId++));

		System.out.println("Sending Request to Agent...");
		ResponseEvent response = snmp.getNext(pdu, comtarget);

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

	private static void snmpSet(String value) throws IOException {

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

		// Setting the Oid and Value for sysContact variable
		OID oid = new OID(new OID(oidValue + "." + instance));
		Variable var = new OctetString(value);
		VariableBinding varBind = new VariableBinding(oid, var);
		pdu.add(varBind);

		pdu.setType(PDU.SET);
		pdu.setRequestID(new Integer32(requestId++));

		System.out.println("Sending Request to Agent...");
		ResponseEvent response = snmp.set(pdu, comtarget);

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

	private static void snmpGetBulk(int nonRepeaters, int maxRepetitions)
			throws IOException {

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
		String[] oids = oidValue.split(";");
		ArrayList<VariableBinding> vbs = new ArrayList<VariableBinding>(
				oids.length);
		for (String oid : oids) {
			// do something interesting here
			vbs.add(new VariableBinding(new OID(oid)));
		}

		PDU pdu = new PDU();
		pdu.setType(PDU.GETBULK);
		pdu.addAll(vbs);
		pdu.setNonRepeaters(nonRepeaters);
		pdu.setMaxRepetitions(maxRepetitions);
		pdu.setRequestID(new Integer32(requestId++));

		System.out.println("Sending Request to Agent...");
		ResponseEvent response = snmp.getBulk(pdu, comtarget);

		// Process Agent Response
		if (response != null) {
			System.out.println("Got Response from Agent");
			PDU responsePDU = response.getResponse();

			if (responsePDU != null) {
				int errorStatus = responsePDU.getErrorStatus();
				int errorIndex = responsePDU.getErrorIndex();
				String errorStatusText = responsePDU.getErrorStatusText();

				if (errorStatus == PDU.noError) {

					System.out.println("Snmp GetBulk Response: ok");

					Vector<? extends VariableBinding> vbs2 = responsePDU
							.getVariableBindings();
					for (VariableBinding vb : vbs2) {

						System.out.println(vb.getVariable());

					}
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

	private static void snmpWalk() throws IOException {

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

		// #####
		Map<String, String> result = new TreeMap<String, String>();

		TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
		List<TreeEvent> events = treeUtils.getSubtree(comtarget, new OID(
				oidValue));
		if (events == null || events.size() == 0) {
			System.out.println("Error: Unable to read table...");
			// return result;
		}

		for (TreeEvent event : events) {
			if (event == null) {
				continue;
			}
			if (event.isError()) {
				System.out.println("Error: table OID [" + oidValue + "] "
						+ event.getErrorMessage());
				continue;
			}

			VariableBinding[] varBindings = event.getVariableBindings();
			if (varBindings == null || varBindings.length == 0) {
				continue;
			}
			for (VariableBinding varBinding : varBindings) {
				if (varBinding == null) {
					continue;
				}

				result.put("." + varBinding.getOid().toString(), varBinding
						.getVariable().toString());
			}

		}

		for (Map.Entry<String, String> entry : result.entrySet()) {
			// print somente sub-arvore 
			if (entry.getKey().startsWith(oidValue)) {
				System.out.println(entry.getKey() + ": " + entry.getValue());
			}
		}

		snmp.close();

	}

	private static void snmpGetTable() throws IOException {

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

		// #####
		Map<String, SortedMap<String, String>> result = new LinkedHashMap<String, SortedMap<String, String>>();

		TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
		List<TreeEvent> events = treeUtils.getSubtree(comtarget, new OID(
				oidValue));
		if (events == null || events.size() == 0) {
			System.out.println("Error: Unable to read table...");
			// return result;
		}

		for (TreeEvent event : events) {
			if (event == null) {
				continue;
			}
			if (event.isError()) {
				System.out.println("Error: table OID [" + oidValue + "] "
						+ event.getErrorMessage());
				continue;
			}

			VariableBinding[] varBindings = event.getVariableBindings();
			if (varBindings == null || varBindings.length == 0) {
				continue;
			}
			for (VariableBinding varBinding : varBindings) {
				if (varBinding == null) {
					continue;
				}

				Map<String, String> current;

				String keyAndIndex = "." + varBinding.getOid().toString();
				String key = keyAndIndex.substring(0,
						keyAndIndex.lastIndexOf("."));
				String _instance = keyAndIndex.replace(key, "");

				if (!result.containsKey(key)) {
					// nova linha
					result.put(key, new TreeMap<String, String>());
				}

				current = result.get(key);

				// nova coluna para a linha
				current.put(_instance, varBinding.getVariable().toString());

			}

		}

		for (Map.Entry<String, SortedMap<String, String>> entryRow : result
				.entrySet()) {

			System.out.print(entryRow.getKey());

			for (Map.Entry<String, String> entryCol : entryRow.getValue()
					.entrySet()) {

				System.out.print("\t|" + entryCol.getValue());

			}

			System.out.print("\n");

		}

		snmp.close();

	}

	private static void snmpGetDelta(int N, int M) throws IOException,
			InterruptedException {

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

		String resps[] = new String[N];
		for (int n = 0; n < N; n++) {

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

						resps[n] = responsePDU.getVariableBindings()
								.firstElement().toString();

					} else {
						System.out.println("Error: Request Failed");
						System.out.println("Error Status = " + errorStatus);
						System.out.println("Error Index = " + errorIndex);
						System.out.println("Error Status Text = "
								+ errorStatusText);
					}
				} else {
					System.out.println("Error: Response PDU is null");
				}
			} else {
				System.out.println("Error: Agent Timeout... ");
			}

			Thread.sleep(M);
		}

		snmp.close();

		// print delta
		int v1 = Integer.parseInt(resps[0].split("=")[1].trim()), v2;
		for (int i = 1; i < N; i++) {
			v2 = Integer.parseInt(resps[i].split("=")[1].trim());
			System.out.printf("delta entre %d e %d é %d\n", v1, v2, v2 - v1);

			v1 = v2;
		}

	}
}
