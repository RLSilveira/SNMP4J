import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.Vector;

import javax.management.modelmbean.RequiredModelMBean;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
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
		// *** Opt
		System.out.println("Informe a opção desejada:");
		System.out.println(" 1 - GET");
		System.out.println(" 2 - GETNEXT");
		System.out.println(" 3 - SET");
		System.out.println(" 4 - GETBULK");
		System.out.println(" 5 - WALK");
		System.out.println(" 6 - GETTABLE");
		//System.out.println(" 7 - GETDELTA");
		
		opt = Integer.parseInt(ss.nextLine());

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

			instance = null;
			System.out.println("Informe a instância (default: null):");
			inst = ss.nextLine();
			if (!inst.isEmpty())
				instance = inst;

			snmpGetNext();

			break;

		case 3: // SET

			System.out.println("Informe o OID: (default: " + oidValue + "):");
			oid = ss.nextLine();
			if (!oid.isEmpty())
				oidValue = oid;

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

			System.out.println("Informe os OIDs (já com a instancia e separados por ';'):");
			
			oidValue = ".1.3.6.1.2.1.1.1.0;.1.3.6.1.2.1.1.2.0;.1.3.6.1.2.1.1.3.0";
			System.out.println("(default: .1.3.6.1.2.1.1.1.0;.1.3.6.1.2.1.1.2.0;.1.3.6.1.2.1.1.3.0)");
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

			oidValue = ".1.3.6.1.2.1.2";
			System.out.println("Informe o OID: (default: " + oidValue + "):");
			oid = ss.nextLine();
			if (!oid.isEmpty())
				oidValue = oid;

			instance = "2";
			System.out.println("Informe a instância (default: " + instance
					+ "):");
			inst = ss.nextLine();
			if (!inst.isEmpty())
				instance = inst;

			snmpWalk();

			break;
			
		case 6: // GETTABLE

			oidValue = ".1.3.6.1.2.1.2";
			System.out.println("Informe o OID: (default: " + oidValue + "):");
			oid = ss.nextLine();
			if (!oid.isEmpty())
				oidValue = oid;

			instance = "2";
			System.out.println("Informe a instância (default: " + instance
					+ "):");
			inst = ss.nextLine();
			if (!inst.isEmpty())
				instance = inst;

			snmpGetTable();

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
		ArrayList<VariableBinding> vbs = new ArrayList<VariableBinding>(oids.length);
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
					
					Vector<? extends VariableBinding> vbs2 = responsePDU.getVariableBindings();
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

	private static void snmpWalk()
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

		
// #####
		Map<String, String> result = new TreeMap<String, String>();
        
        TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
        List<TreeEvent> events = treeUtils.getSubtree(comtarget, new OID(oidValue));
        if (events == null || events.size() == 0) {
            System.out.println("Error: Unable to read table...");
            //return result;
        }
 
        for (TreeEvent event : events) {
            if (event == null) {
                continue;
            }
            if (event.isError()) {
                System.out.println("Error: table OID [" + oidValue + "] " + event.getErrorMessage());
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
                 
                result.put("." + varBinding.getOid().toString(), varBinding.getVariable().toString());
            }
 
        }
        
        for (Map.Entry<String, String> entry : result.entrySet()) {
//            if (entry.getKey().startsWith(".1.3.6.1.2.1.2.2.1.2.")) {
//                System.out.println("ifDescr" + entry.getKey().replace(".1.3.6.1.2.1.2.2.1.2", "") + ": " + entry.getValue());
//            }
//            if (entry.getKey().startsWith(".1.3.6.1.2.1.2.2.1.3.")) {
//                System.out.println("ifType" + entry.getKey().replace(".1.3.6.1.2.1.2.2.1.3", "") + ": " + entry.getValue());
//            }
        	if (entry.getKey().startsWith(oidValue + "." + instance))
        		System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        
		snmp.close();
		
	}

	private static void snmpGetTable()throws IOException {

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
        List<TreeEvent> events = treeUtils.getSubtree(comtarget, new OID(oidValue));
        if (events == null || events.size() == 0) {
            System.out.println("Error: Unable to read table...");
            //return result;
        }
 
        for (TreeEvent event : events) {
            if (event == null) {
                continue;
            }
            if (event.isError()) {
                System.out.println("Error: table OID [" + oidValue + "] " + event.getErrorMessage());
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
                 
                result.put("." + varBinding.getOid().toString(), varBinding.getVariable().toString());
            }
 
        }
        
        String cab = "";
        String row = "";
        
        int colsOk = 0;
        int cols = 0;
        int iCol = 0;
        for (Map.Entry<String, String> entry : result.entrySet()) {
        	// get num cols
        	if (colsOk == 0){
        		String[] x = entry.getKey().split(".");
        		//if (x.length == 0) continue;
        		int c = Integer.parseInt(x[x.length-1]);
        		if (c > cols){
        			cols = c;
        			cab = cab + "\t" + c;
        		}
        		else {
        			colsOk = 1;
        			System.out.println(cab);
        		}
        	}
        	
    		// primeira coluna (oid)
        	if (iCol == 0){
        		row = entry.getKey();
        	}
        	
        	// get valores
        	row += "\t" + entry.getValue();
        	
        	// print e zera para proxima linha
        	if (iCol % cols == 0){
        		System.out.println(row);
        		iCol = 0;
        	}
        }
        
		snmp.close();
		
	}
}
