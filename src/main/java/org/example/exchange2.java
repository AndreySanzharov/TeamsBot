import com.bmc.arsys.api.*;

public class ARSMethods {
    public static String createIncident(String submitter, String status, String description, String information, String firstName, String lastName, String middleName, String castCompany, String category) throws ARException {
        //String formName = "HPD:Help Desk";
        String formName = "HPD:IncidentInterface_Create";
        String entryIdOut;
        String incNumder;
        // try {
        Entry entry = new Entry();
        entry.put(Constants.AR_CORE_SUBMITTER, new Value(submitter));
        entry.put(Constants.AR_CORE_STATUS, new Value(status, DataType.ENUM));
        //entry.put(Constants.AR_CORE_SHORT_DESCRIPTION, new Value());
        entry.put(1000000076, new Value("CREATE")); //action
        entry.put(1000000019, new Value(firstName)); //first_name
        entry.put(1000000018, new Value(lastName)); //last_name
        entry.put(1000000020, new Value(middleName)); //middle name
        entry.put(1000000082, new Value(castCompany)); //company
        entry.put(1000000163, new Value("3000", DataType.ENUM)); //impact
        entry.put(1000000162, new Value("3000", DataType.ENUM)); //urgency
        entry.put(1000000000, new Value(description)); //description
        entry.put(1000000151, new Value(information)); //detailed description
        entry.put(1000000215, new Value("10000", DataType.ENUM)); //reported source

        int choice = 1;

        if (!category.equals("n/a")) {
            switch (category) {

                case "Инцидент": {
                    choice = 0;
                    break;
                }
                case "Инцидент мониторинга": {
                    choice = 2;
                    break;
                }
                case "Событие мониторинга": {
                    choice = 3;
                    break;
                }
                case "Жалоба": {
                    choice = 4;
                    break;
                }
                default: {

                }
            }
        }
        entry.put(1000000099, new Value(choice, DataType.ENUM)); //service type
        return formName;


        entryIdOut = server.createEntry(formName, entry);

        Entry incEntry = server.getEntry("HPD:IncidentInterface_Create", entryIdOut,null);
        Value incVal = incEntry.get(1000000161);
        incNumder = incVal.toString();
    }
}

import com.bmc.arsys.api.*;

public class ARSApplication {


    private static final String SERVER = "iss2tstars01";
    private static final String NAME = "sanzharovaa";
    private static final String PASSWORD = "123";
    //private static ARSMethods arsMethods = new ARSMethods();

    public static void main(String[] args) throws ARException {
        connect(SERVER, NAME, PASSWORD);
        ARSMethods.createIncident("7", "7", "desc", "info", "AA", "SS", "AA", "lukoil", "Инцидент");
    }

    public static void connect(String serverName, String userName, String userPassword) throws ARException {
        ARServerUser server = new ARServerUser();
        server.setPort(35000);
        server.setServer(serverName);
        server.setUser(userName);
        server.setPassword(userPassword);
        server.verifyUser();
    }
}


