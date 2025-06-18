import com.bmc.arsys.api.*;

public class ARSMethods {

    private final ARServerUser server;

    public ARSMethods(ARServerUser server) {
        this.server = server;
    }

    public String createIncident(String submitter, String status, String description, String information,
                                 String firstName, String lastName, String middleName,
                                 String castCompany, String category) throws ARException {

        String formName = "HPD:IncidentInterface_Create";
        Entry entry = new Entry();

        entry.put(Constants.AR_CORE_SUBMITTER, new Value(submitter));
        entry.put(Constants.AR_CORE_STATUS, new Value(status, DataType.ENUM));
        entry.put(1000000076, new Value("CREATE")); // action
        entry.put(1000000019, new Value(firstName)); // first name
        entry.put(1000000018, new Value(lastName)); // last name
        entry.put(1000000020, new Value(middleName)); // middle name
        entry.put(1000000082, new Value(castCompany)); // company
        entry.put(1000000163, new Value("3000", DataType.ENUM)); // impact
        entry.put(1000000162, new Value("3000", DataType.ENUM)); // urgency
        entry.put(1000000000, new Value(description)); // description
        entry.put(1000000151, new Value(information)); // detailed description
        entry.put(1000000215, new Value("10000", DataType.ENUM)); // reported source

        int choice = switch (category) {
            case "Инцидент" -> 0;
            case "Инцидент мониторинга" -> 2;
            case "Событие мониторинга" -> 3;
            case "Жалоба" -> 4;
            default -> 1; // по умолчанию
        };

        entry.put(1000000099, new Value(choice, DataType.ENUM)); // service type

        String entryIdOut = server.createEntry(formName, entry);
        Entry incEntry = server.getEntry(formName, entryIdOut, null);
        Value incVal = incEntry.get(1000000161); // Incident Number
        return incVal.toString();
    }
}
import com.bmc.arsys.api.*;

public class ARSApplication {

    private static final String SERVER = "iss2tstars01";
    private static final String NAME = "sanzharovaa";
    private static final String PASSWORD = "123";

    public static void main(String[] args) {
        try {
            ARServerUser server = connect(SERVER, NAME, PASSWORD);
            ARSMethods arsMethods = new ARSMethods(server);

            String incidentNumber = arsMethods.createIncident(
                    "7", "7", "desc", "info",
                    "AA", "SS", "AA", "lukoil", "Инцидент"
            );

            System.out.println("Создан инцидент с номером: " + incidentNumber);

        } catch (ARException e) {
            System.err.println("Ошибка при работе с AR System: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static ARServerUser connect(String serverName, String userName, String userPassword) throws ARException {
        ARServerUser server = new ARServerUser();
        server.setPort(35000);
        server.setServer(serverName);
        server.setUser(userName);
        server.setPassword(userPassword);
        server.verifyUser();
        return server;
    }
}
