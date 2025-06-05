public class ARSConnector {
    private ARServerUser server;


    public ARSConnector() {
        server = new ARServerUser();
    }

    // Connect the current user to the server.
    public void connect(String serverName,String userName, String userPassword) throws ARException {

        //   System.out.println();
        //    System.out.println("Connecting to AR Server...");

        server.setServer(serverName);
        server.setUser(userName);
        server.setPassword(userPassword);
        server.verifyUser();
        //    System.out.println("Connected to AR Server " + server.getServer());
    }


    // Create an entry in a form using the given field values.
//    public String createRequest (String submitter, String status, String shortDesc,String summary,String company, String locationCompany,String lastName, String firstName, String middleName){
//        String formName = "SRM:Request";
//        String entryIdOut= "";
//        Value vs = new Value(status, DataType.ENUM);
//       // System.out.println("vs: " + vs.getCurrencyValue());
//
//        try {
//            Entry entry = new Entry();
//            entry.put(Constants.AR_CORE_SUBMITTER, new Value(submitter));
//            //entry.put(Constants.AR_CORE_STATUS, vs);
//            entry.put(Constants.AR_CORE_SHORT_DESCRIPTION, new Value(shortDesc));
//            entry.put(301244700,new Value(summary)); //summary
//            entry.put(1000000082,new Value(company)); //company
//            entry.put(1000000001,new Value(locationCompany)); //location company
//            entry.put(1000000018,new Value(lastName)); //lastName
//            entry.put(1000000019,new Value(firstName)); //firsName
//            entry.put(300810110,new Value(middleName)); //middleName
//
//
//            entryIdOut = server.createEntry(formName, entry);
//
//            System.out.println();
//            System.out.println("Entry created. The id # is " + entryIdOut);
//        }
//        catch (ARException e) {
//          ARExceptionHandler(e, "Cannot create the entry." );
//        }
//        return entryIdOut;
//    }

    // Create an entry in a form using the given field values.
    public String createIncident (String submitter, String status, String description, String information, String firstName, String lastName, String middleName, String castCompany, String category, String service, String supportGroup, String filePath) throws ARException {
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



        entry.put(1000000163, new Value("3000",DataType.ENUM)); //impact
        entry.put(1000000162, new Value("3000",DataType.ENUM)); //urgency
        entry.put(1000000000, new Value(description)); //description
        entry.put(1000000151, new Value(information)); //detailed description
        entry.put(1000000215, new Value("10000",DataType.ENUM)); //reported source

        int choice = 1;

        if(!category.equals("n/a")) {
            switch (category) {

                case "Инцидент" :{
                    choice = 0; break;
                }
                case "Инцидент мониторинга" :{
                    choice = 2; break;
                }
                case "Событие мониторинга" :{
                    choice = 3; break;
                }
                case "Жалоба" :{
                    choice = 4; break;
                }
                default:{

                }
            }
        }
        entry.put(1000000099, new Value(choice, DataType.ENUM)); //service type

        if(!service.equals("n/a")) {
            //entry.put(303497300, new Value(service)); //service  ///todo необходима функция правильного определения услуги пользователя сейчас ставит что попало.
            entry.put(303519300, new Value(service));

        }
        if(!supportGroup.equals("n/a")){
            //  entry.put(1000000217, new Value(supportGroup)); //support group
            ARSSupportGroup sg = getSupportGroup(supportGroup);
            entry.put(1000000079, new Value(sg.getGroupId())); //support group
        }

        if(filePath!=null) {
            try {

                String fileName = filePath.substring(filePath.lastIndexOf("\\") + 1);

                entry.put(301399000, new Value("Public")); //z1D_View_Access
                entry.put(301398900, new Value("No"));//z1D_Secure_Log
                entry.put(301398800, new Value("Вложение от пользователя")); //z1D_Details
                entry.put(301398600, new Value("Customer Communication")); //z1D_Activity_Type
                entry.put(301329900, new Value("Вложение получено через бот"));//z1D_WorklogDetails

                AttachmentValue atValue1 = new AttachmentValue(fileName, filePath);
                // AttachmentValue atValue2 = new AttachmentValue("arserver.config.txt","C:\\Program Files\\vkbot_service\\bin\\arserver.config.txt");
                //AttachmentValue atValue3 = new AttachmentValue("getXLS.log","C:\\Program Files\\vkbot_service\\bin\\getXLS.log");

                entry.put(1000005791, new Value(atValue1));
                //entry.put(700000002,new Value(atValue2));
                //entry.put(700000003,new Value(atValue3));
                //server.createEntry(formName, attachEntry);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        entryIdOut = server.createEntry(formName, entry);

        Entry incEntry = server.getEntry("HPD:IncidentInterface_Create", entryIdOut, null);
        Value incVal = incEntry.get(1000000161);
        incNumder = incVal.toString();

//        try {
//            Entry attachEntry = new Entry();
//            attachEntry.put(1000000161,incVal);
//            attachEntry.put(1000000076,new Value("ATTACHMENT"));
//
//            AttachmentValue atValue1 = new AttachmentValue("email.log","C:\\Program Files\\vkbot_service\\bin\\email.log");
////            AttachmentValue atValue2 = new AttachmentValue("C:\\Program Files\\vkbot_service\\bin\\arserver.config.txt");
////            AttachmentValue atValue3 = new AttachmentValue("C:\\Program Files\\vkbot_service\\bin\\getXLS.log");
//
//            attachEntry.put(1000005791,new Value(atValue1));
////            attachEntry.put(700000002,new Value(atValue2));
////            attachEntry.put(700000003,new Value(atValue3));
//            server.createEntry(formName, attachEntry);
//        }
//        catch (IOException e) {
//          throw new RuntimeException(e);
//        }


//            System.out.println();
//            System.out.println("Entry created. The id # is " + entryIdOut);
//        }
//        catch (ARException e) {
//            ARExceptionHandler(e, "Cannot create the entry." );
//        }
        return incNumder;
    }

    public String createIncidentEx (String submitter, String status, String description, String information, String contactLoginID, String personInatnceID,String category, String service, String supportGroup, String filePath) throws ARException {
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
        //entry.put(1000000019, new Value(firstName)); //first_name
        // entry.put(1000000018, new Value(lastName)); //last_name
        // entry.put(1000000020, new Value(middleName)); //middle name

        entry.put(300621300, new Value(personInatnceID)); //iniciator (Person Instance ID) instance ID

        if(contactLoginID != null){
            entry.put(304309530, new Value(contactLoginID)); //contact (Contact Login Id) login ID
        }

        //entry.put(1000000082, new Value(castCompany)); //company
//        entry.put(1000000099, new Value("2",DataType.ENUM)); //service type

        entry.put(1000000163, new Value("3000",DataType.ENUM)); //impact
        entry.put(1000000162, new Value("3000",DataType.ENUM)); //urgency
        entry.put(1000000000, new Value(description)); //description
        entry.put(1000000151, new Value(information)); //detailed description
        entry.put(1000000215, new Value("10000",DataType.ENUM)); //reported source

        int choice = 1;
        if(!category.equals("n/a")) {

            switch (category) {

                case "Инцидент" :{
                    choice = 0; break;
                }
                case "Инцидент мониторинга" :{
                    choice = 2; break;
                }
                case "Событие мониторинга" :{
                    choice = 3; break;
                }
                case "Жалоба" :{
                    choice = 4; break;
                }
                default:{
                }
            }

        }

        entry.put(1000000099, new Value(choice, DataType.ENUM)); //service type

        if(!service.equals("n/a")) {
            //entry.put(303497300, new Value(service)); //service
            entry.put(303519300, new Value(service));
        }
        if(!supportGroup.equals("n/a")){
            //entry.put(1000000217, new Value(supportGroup)); //support group

            ARSSupportGroup sg = getSupportGroup(supportGroup);
            entry.put(1000000079, new Value(sg.getGroupId())); //support group
        }

        if(filePath!=null) {
            try {

                String fileName = filePath.substring(filePath.lastIndexOf("\\") + 1);
                entry.put(301399000, new Value("Public")); //z1D_View_Access
                entry.put(301398900, new Value("No"));//z1D_Secure_Log
                entry.put(301398800, new Value("Вложение от пользователя")); //z1D_Details
                entry.put(301398600, new Value("Customer Communication")); //z1D_Activity_Type
                entry.put(301329900, new Value("Вложение получено через бот"));//z1D_WorklogDetails

                AttachmentValue atValue1 = new AttachmentValue(fileName, filePath);
                //AttachmentValue atValue2 = new AttachmentValue("arserver.config.txt","C:\\Program Files\\vkbot_service\\bin\\arserver.config.txt");
                //AttachmentValue atValue3 = new AttachmentValue("getXLS.log", "C:\\Program Files\\vkbot_service\\bin\\getXLS.log");

                entry.put(1000005791, new Value(atValue1));
                //entry.put(700000002,new Value(atValue2));
                //entry.put(700000003, new Value(atValue3));

                //server.createEntry(formName, attachEntry);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        entryIdOut = server.createEntry(formName, entry);
        Entry incEntry = server.getEntry("HPD:IncidentInterface_Create", entryIdOut, null);

        Value incVal = incEntry.get(1000000161);

//        try {
//            Entry attachEntry = new Entry();
//            attachEntry.put(1000000161,incVal);
//            attachEntry.put(1000000076,new Value("ATTACHMENT"));
//
//            AttachmentValue atValue1 = new AttachmentValue("email.log","C:\\Program Files\\vkbot_service\\bin\\email.log");
//            //AttachmentValue atValue2 = new AttachmentValue("C:\\Program Files\\vkbot_service\\bin\\arserver.config.txt");
//            //AttachmentValue atValue3 = new AttachmentValue("C:\\Program Files\\vkbot_service\\bin\\getXLS.log");
//
//            attachEntry.put(1000005791,new Value(atValue1));
//            //attachEntry.put(700000002,new Value(atValue2));
//            //attachEntry.put(700000003,new Value(atValue3));
//            server.createEntry(formName, attachEntry);
//        }
//        catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        incNumder = incVal.toString();

        System.out.println();
        System.out.println("Entry created. The id # is " + entryIdOut);
//        }
//        catch (ARException e) {
//            ARExceptionHandler(e, "Cannot create the entry." );
//        }
        return incNumder;
    }


    public void closeIncident(String number, String resolution, String assigne, String assigneLogin) throws ARException {
        String formName = "HPD:IncidentInterface";
        String qualStr = "'Incident Number' = \""+number+"\"";

        System.out.println("Query: " + qualStr);

        List <Field> fields = server.getListFieldObjects(formName);
        QualifierInfo qual = server.parseQualification(qualStr, fields, null, Constants.AR_QUALCONTEXT_DEFAULT);

        //1000000829 //number
        //301244700  //summary;
        //7          //status
        //1000000048 //email
        //1000000151 // detailed
        //3          //date submit
        //1000000150 //status reason
        //1000000156 //resolution

        int[] fieldIds = {1000000161,7,1000000150,1000000156,1000000218,4};
        OutputInteger nMatches = new OutputInteger();
        List<SortInfo> sortOrder = new ArrayList<>();
        sortOrder.add(new SortInfo(1000000161,Constants.AR_SORT_ASCENDING));
        // Retrieve entries from the form using the given qualification.
        List<Entry> entryList = server.getListEntryObjects(formName, qual, 0, Constants.AR_NO_MAX_LIST_RETRIEVE, sortOrder, fieldIds, true, nMatches);
        System.out.println("Close inc matches: "+nMatches.intValue());
        if( nMatches.intValue() == 1){

            // Entry entry = server.getEntry(formName, entryId, null);
            Entry entry = entryList.get(0);

            entry.put(1000000156, new Value(resolution)); // resolution
            entry.put(1000000218, new Value(assigne)); // assigne
            entry.put(4, new Value(assigneLogin)); // assigne login
            server.setEntry(formName, entry.getEntryId(), entry, null, 0);

            //закрытие в два приема
            entry.put(7, new Value(4)); // status
            entry.put(1000000150, new Value(17000)); //status reason
            server.setEntry(formName, entry.getEntryId(), entry, null, 0);

            System.out.println("Close inc susses "+nMatches.intValue());
        }
    }

    // Create an entry in a form using the given field values.
    public String createChange (String submitter, String status, String shortDesc){
        String formName = "CHG:Infrastructure Change";
        String entryIdOut= "";
        try {
            Entry entry = new Entry();
            entry.put(Constants.AR_CORE_SUBMITTER, new Value(submitter));
            entry.put(Constants.AR_CORE_STATUS, new Value(status, DataType.ENUM));
            entry.put(Constants.AR_CORE_SHORT_DESCRIPTION, new Value(shortDesc));
            entryIdOut = server.createEntry(formName, entry);
            System.out.println();
            System.out.println("Entry created. The id # is " + entryIdOut);
        }
        catch (ARException e) {
            ARExceptionHandler(e, "Cannot create the entry." );
        }
        return entryIdOut;
    }

    public String getRequestNumber(String qualStr) throws ARException {
        String requestNumder = "";
        System.out.println();
        //  try {

        // Entry entry = server.getEntry("HPD:IncidentInterface", incidentNumber, null);
        // Retrieve the detail info of all fields from the form.

        List <Field> fields = server.getListFieldObjects("HPD:IncidentInterface");
        // Create the search qualifier.
        QualifierInfo qual = server.parseQualification(qualStr, fields, null, Constants.AR_QUALCONTEXT_DEFAULT);

        int[] fieldIds = {1000000161,301572100};
        OutputInteger nMatches = new OutputInteger();
        List<SortInfo> sortOrder = new ArrayList<>();
        sortOrder.add(new SortInfo(1000000161,Constants.AR_SORT_DESCENDING));
        // Retrieve entries from the form using the given qualification.
        List<Entry> entryList = server.getListEntryObjects("HPD:IncidentInterface", qual, 0, Constants.AR_NO_MAX_LIST_RETRIEVE, sortOrder, fieldIds, true, nMatches);

        System.out.println ("Query returned " + nMatches +  " matches.");
        System.out.println ("nMatches.intValue " + nMatches.intValue());
        if( nMatches.intValue() == 1){
            //requestNumder = entryList.get(0).get(301572100).toString();

            requestNumder = entryList.get(0).toString();
            System.out.println ("requestNumder: " + requestNumder);

            requestNumder = entryList.get(0).get(301572100).toString();
            System.out.println ("requestNumder: " + requestNumder);
        }
        // }
        //  catch( ARException e ){
        //     ARExceptionHandler (e, "Problem while querying by entry ID.");
        //  }
        return requestNumder;
    }

    public ArrayList<ARSRequest> getOpenRequest(String qualStr) throws ARException {

        ArrayList<ARSRequest> requests = new ArrayList<>();
        List <Field> fields = server.getListFieldObjects("SRM:RequestInterface");
        QualifierInfo qual = server.parseQualification(qualStr, fields, null, Constants.AR_QUALCONTEXT_DEFAULT);
        System.out.println("qual: " + qualStr);

        //1000000829 //number
        //301244700  //summary;
        //7          //status
        //1000000048 //email
        //1000000151 // detailed
        //3          //date submit

        int[] fieldIds = {1000000829,1000000048,7,301244700,1000000151,3};
        OutputInteger nMatches = new OutputInteger();
        List<SortInfo> sortOrder = new ArrayList<>();
        sortOrder.add(new SortInfo(1000000829,Constants.AR_SORT_ASCENDING));
        // Retrieve entries from the form using the given qualification.
        List<Entry> entryList = server.getListEntryObjects("SRM:RequestInterface", qual, 0, Constants.AR_NO_MAX_LIST_RETRIEVE, sortOrder, fieldIds, true, nMatches);

        System.out.println ("Query returned " + nMatches +  " matches.");
        System.out.println ("nMatches.intValue " + nMatches.intValue());
        if( nMatches.intValue() > 0){

            System.out.println("Request Id         " + "Short Description" );
            for (Entry entry : entryList) {
                System.out.println(entry.getEntryId() + "     " + entry.get(Constants.AR_CORE_SHORT_DESCRIPTION));
                ARSRequest req = new ARSRequest();

                req.setNumber(entry.get(1000000829).toString());
                req.setStatus(reqStatusTransform(entry.get(7).toString()));
                req.setDescription(entry.get(301244700).toString());
                req.setInformation(entry.get(1000000151).toString());


                Timestamp ts = (Timestamp) entry.get(3).getValue();

                SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                String formatted = formatDate.format(ts.toDate());
                System.out.println(formatted);
                req.setSubmitDate(formatted);
                requests.add(req);

            }

        }
        return requests;
    }


    public ArrayList<ARSIncident> getIncidents(String qualStr) throws ARException {

        ArrayList<ARSIncident> requests = new ArrayList<>();
        String formName = "HPD:IncidentInteface";

        List <Field> fields = server.getListFieldObjects(formName);
        QualifierInfo qual = server.parseQualification(qualStr, fields, null, Constants.AR_QUALCONTEXT_DEFAULT);

        //1000000161 //number
        //301244700  //summary;
        //7          //status
        //1000000048 //email
        //1000000151 // detailed
        //3          //date submit

        int[] fieldIds = {1000000161,1000000048,7,301244700,1000000151,3};
        OutputInteger nMatches = new OutputInteger();
        List<SortInfo> sortOrder = new ArrayList<>();
        sortOrder.add(new SortInfo(1000000161,Constants.AR_SORT_ASCENDING));
        // Retrieve entries from the form using the given qualification.
        List<Entry> entryList = server.getListEntryObjects(formName, qual, 0, Constants.AR_NO_MAX_LIST_RETRIEVE, sortOrder, fieldIds, true, nMatches);

        System.out.println ("Query returned " + nMatches +  " matches.");
        System.out.println ("nMatches.intValue " + nMatches.intValue());
        if( nMatches.intValue() > 0){

            System.out.println("Request Id         " + "Short Description" );
            for (Entry entry : entryList) {
                System.out.println(entry.getEntryId() + "     " + entry.get(Constants.AR_CORE_SHORT_DESCRIPTION));
                ARSIncident inc = new ARSIncident();

                inc.setNumder(entry.get(1000000161).toString());
                inc.setCompany(reqStatusTransform(entry.get(7).toString()));
                inc.setOrg(entry.get(3).toString());
                inc.setDep(entry.get(3).toString());
                inc.setDescription(entry.get(301244700).toString());
                inc.setInformation(entry.get(1000000151).toString());


                Value atValue1 = entry.get(10010546);
                try {

                    AttachmentValue atValue = new AttachmentValue (atValue1.toString());

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                requests.add(inc);

            }

        }
        return requests;
    }

    public void modifyIncident(String entryId, String category, String service, String supportGroup) throws ARException {
        String formName = "HPD:IncidentInterface";

        Entry entry = server.getEntry(formName, entryId, null);

        entry.put(3454, new Value(category));
        entry.put(56546456, new Value(service));
        entry.put(54654645, new Value(supportGroup));

        server.setEntry(formName, entryId, entry, null, 0);
        System.out.println();
        System.out.println("Entry #" + entryId + " modified successfully.");

    }


    public ARSUser getUserInfo(String email) throws ARException {
        //String qualStr = "'Internet E-mail'=\""+email+"\"";

        String qualStr = "'Internet E-mail'=\""+email+"\" AND 'Profile Status' = \"Enabled\"";
        System.out.println("Qual: " + qualStr);

        return getUser(qualStr);
    }

    public ARSUser getUserInfo(String company, String lastName,String firstName, String middleName) throws ARException {


        String qualStr ="'Last Name' = \""+lastName+"\" AND 'First Name' = \""+firstName+"\"  AND 'Middle Initial' = \""+middleName+"\" AND 'Company' = \""+company.replaceAll("\"","\"\"")+"\"";

        System.out.println("Qual: " + qualStr);

        return getUser(qualStr);
    }


    private ARSUser getUser(String qualStr) throws ARException {
        ARSUser user = new ARSUser();

        List <Field> fields = server.getListFieldObjects("CTM:People");
        // Create the search qualifier.
        QualifierInfo qual = server.parseQualification(qualStr, fields, null, Constants.AR_QUALCONTEXT_DEFAULT);

        int[] fieldIds = {1000000048,1,4,179,1000000054,1000000018,1000000019,1000000020,1000000023,1000000001,1000000010,200000006,1000000056,1000000035};
        OutputInteger nMatches = new OutputInteger();
        List<SortInfo> sortOrder = new ArrayList<>();
        sortOrder.add(new SortInfo(1000000048,Constants.AR_SORT_DESCENDING));
        // Retrieve entries from the form using the given qualification.
        List<Entry> entryList = server.getListEntryObjects("CTM:People", qual, 0, Constants.AR_NO_MAX_LIST_RETRIEVE, sortOrder, fieldIds, true, nMatches);

        System.out.println ("Query returned " + nMatches +  " matches.");
        System.out.println ("nMatches.intValue " + nMatches.intValue());
        if( nMatches.intValue() == 1){
            //requestNumder = entryList.get(0).get(301572100).toString();
            user.setPersonID(entryList.get(0).get(1).toString());
            user.setCorporateID(entryList.get(0).get(1000000054).toString());
            user.setRemedyLoginID(entryList.get(0).get(4).toString());
            user.setInstanceID(entryList.get(0).get(179).toString());
            user.setLastName(entryList.get(0).get(1000000018).toString());
            user.setFirsName(entryList.get(0).get(1000000019).toString());
            user.setMiddleName(entryList.get(0).get(1000000020).toString());
            user.setJobTitle(entryList.get(0).get(1000000023).toString());
            user.setEmail(entryList.get(0).get(1000000048).toString());

            user.setCompany(entryList.get(0).get(1000000001).toString());
            user.setOrganization(entryList.get(0).get(1000000010).toString());
            user.setDepartment(entryList.get(0).get(200000006).toString());

            user.setPhone(entryList.get(0).get(1000000056).toString());
            user.setPlace(entryList.get(0).get(1000000035).toString());

            user.setValid(true);
        }

        return user;



    }


    public ARSService getService(String cod, String company) throws ARException {

        ARSService srv = new ARSService();

        String qualStr ="'Status'=\"Enabled\" AND 'Support Group Name'=\""+cod+"\"";

        System.out.println("Qual: " + qualStr);

        List <Field> fields = server.getListFieldObjects("CTM:Support Group");
        // Create the search qualifier.
        QualifierInfo qual = server.parseQualification(qualStr, fields, null, Constants.AR_QUALCONTEXT_DEFAULT);

        int[] fieldIds = {1000000015,1,179};
        OutputInteger nMatches = new OutputInteger();
        List<SortInfo> sortOrder = new ArrayList<>();
        sortOrder.add(new SortInfo(1000000015,Constants.AR_SORT_DESCENDING));
        // Retrieve entries from the form using the given qualification.
        List<Entry> entryList = server.getListEntryObjects("CTM:Support Group", qual, 0, Constants.AR_NO_MAX_LIST_RETRIEVE, sortOrder, fieldIds, true, nMatches);

        System.out.println ("Query returned " + nMatches +  " matches.");
        System.out.println ("nMatches.intValue " + nMatches.intValue());
        if( nMatches.intValue() == 1){
            //requestNumder = entryList.get(0).get(301572100).toString();
            srv.setName(entryList.get(0).get(1).toString());
            srv.setInstanceID(entryList.get(0).get(179).toString());
        }
        return srv;
    }

    public ARSSupportGroup getSupportGroup(String name) throws ARException {

        ARSSupportGroup sg = new ARSSupportGroup();

        String qualStr ="'Status'=\"Enabled\" AND 'Support Group Name'=\""+name.replaceAll("\"","\"\"")+"\"";

        System.out.println("Qual: " + qualStr);

        List <Field> fields = server.getListFieldObjects("CTM:Support Group");
        // Create the search qualifier.
        QualifierInfo qual = server.parseQualification(qualStr, fields, null, Constants.AR_QUALCONTEXT_DEFAULT);

        int[] fieldIds = {1000000015,1,179};
        OutputInteger nMatches = new OutputInteger();
        List<SortInfo> sortOrder = new ArrayList<>();
        sortOrder.add(new SortInfo(1000000015,Constants.AR_SORT_DESCENDING));
        // Retrieve entries from the form using the given qualification.
        List<Entry> entryList = server.getListEntryObjects("CTM:Support Group", qual, 0, Constants.AR_NO_MAX_LIST_RETRIEVE, sortOrder, fieldIds, true, nMatches);

        System.out.println ("Query returned " + nMatches +  " matches.");
        System.out.println ("nMatches.intValue " + nMatches.intValue());
        if( nMatches.intValue() == 1){
            //requestNumder = entryList.get(0).get(301572100).toString();
            sg.setGroupId(entryList.get(0).get(1).toString());
            sg.setInstanceId(entryList.get(0).get(179).toString());
            sg.setGroupName(entryList.get(0).get(1000000015).toString());
        }

        return sg;
    }

    // Modify the short description field on the specified entry.
    public void modifyEntry(String formName,String entryId) throws ARException {
        // try {
        Entry entry = server.getEntry(formName, entryId, null);
        entry.put(Constants.AR_CORE_SHORT_DESCRIPTION, new Value("Modified by JavaAPITest"));
        server.setEntry(formName, entryId, entry, null, 0);
        System.out.println();
        System.out.println("Entry #" + entryId + " modified successfully.");
        // }
        // catch(ARException e) {
        //     ARExceptionHandler(e,"Cannot modify the entry. ");
        // }
    }

    // Retrive an entry by its entry ID and print out the number of
    // fields in the entry. For each field in the entry, print out the
    // value, and the field info (name, id and the type).
    public void queryEntrysByID(String formName, String entryId) throws ARException {
        System.out.println();
        System.out.println("Retrieving entry with entry ID#" + entryId);
        //try {
        Entry entry = server.getEntry(formName, entryId, null);
        if( entry  == null ){
            System.out.println("No data found for ID#" + entryId);
            return;
        } else
            System.out.println("Number of fields: " + entry.size());

        // Retrieve all properties of fields in the entry.
        Set<Integer> fieldIds = entry.keySet();
        for (Integer fieldId : fieldIds){
            Field field = server.getField(formName, fieldId);
            Value val = entry.get(fieldId);
            // Output field's name, value, ID, and type.
            System.out.print(field.getName());
            System.out.print(": " + val);
            System.out.print(" , ID: " + field.getFieldID());
            System.out.print(" , Field type: " + field.getDataType());
            // Handle DateTime value.
            if ( field instanceof DateTimeField ){
                System.out.print(", DateTime value: ");
                Timestamp callDateTimeTS = (Timestamp)val.getValue();
                if (callDateTimeTS != null)
                    System.out.print(callDateTimeTS.toDate());
            }
            //System.out.println();
        }
//        }
//        catch( ARException e ){
//            ARExceptionHandler (e, "Problem while querying by entry ID.");
//        }
    }

    // Retrieve entries from the form using the given qualification. With
    // the returned entry set, print out the ID of each entry and the
    // contents in its shortDescription field.
    public void queryEntrysByQual(String formName, String qualStr) {
        System.out.println();
        System.out.println ("Retrieving entryies with qualification " + qualStr);
        try {
            // Retrieve the detail info of all fields from the form.
            List <Field> fields = server.getListFieldObjects(formName);
            // Create the search qualifier.
            QualifierInfo qual = server.parseQualification(qualStr, fields, null, Constants.AR_QUALCONTEXT_DEFAULT);

            int[] fieldIds = {2, 7, 8};
            OutputInteger nMatches = new OutputInteger();
            List<SortInfo> sortOrder = new ArrayList<>();
            sortOrder.add(new SortInfo(2,Constants.AR_SORT_DESCENDING));

            // Retrieve entries from the form using the given qualification.
            List<Entry> entryList = server.getListEntryObjects(formName, qual, 0, Constants.AR_NO_MAX_LIST_RETRIEVE, sortOrder, fieldIds, true, nMatches);

            System.out.println ("Query returned " + nMatches +  " matches.");
            if( nMatches.intValue() > 0){
                // Print out the matches.
                System.out.println("Request Id         " + "Short Description" );
                for (Entry entry : entryList) {
                    System.out.println(entry.getEntryId() + "     " + entry.get(Constants.AR_CORE_SHORT_DESCRIPTION));
                }
            }
        }
        catch( ARException e ) {
            ARExceptionHandler(e, "Problem while querying by qualifier. ");
        }
    }

    public void ARExceptionHandler(ARException e, String errMessage){
        System.out.println(errMessage);
        printStatusList(server.getLastStatus());
        System.out.print("Stack Trace:");
        //e.printStackTrace();
    }

    public void printStatusList(List<StatusInfo> statusList) {
        if (statusList == null || statusList.isEmpty()) {
            System.out.println("Status List is empty.");
            return;
        }
        System.out.print("Message type: ");
        switch(statusList.get(0).getMessageType()){
            case Constants.AR_RETURN_OK:
                System.out.println("Note");
                break;
            case Constants.AR_RETURN_WARNING:
                System.out.println("Warning");
                break;
            case Constants.AR_RETURN_ERROR:
                System.out.println("Error");
                break;
            case Constants.AR_RETURN_FATAL:
                System.out.println("Fatal Error");
                break;
            default:
                System.out.println("Unknown (" + statusList.get(0).getMessageType() + ")");
                break;
        }
        System.out.println("Status List:");
        for (StatusInfo statusInfo : statusList) {
            System.out.println(statusInfo.getMessageText());
            System.out.println(statusInfo.getAppendedText());
        }
    }

    public void cleanup() {
        server.logout();
        System.out.println();
        System.out.println("User logged out.");
    }

    private String reqStatusTransform( String stat){
        String status;
        switch(stat){
            case "1000":{
                status = "Новый";
                break;
            }
            case "1200":{
                status = "В корзине";
                break;
            }
            case "1500":{
                status = "На рассмотрении";
                break;
            }
            case "1800":{
                status = "Сохранен";
                break;
            }
            case "2000":{
                status = "В ожидании";
                break;
            }
            case "3000":{
                status = "Согласуется";
                break;
            }
            case "4000":{
                status = "Инициируется";
                break;
            }
            case "5000":{
                status = "Выполняется";
                break;
            }
            case "6000":{
                status = "Завершено";
                break;
            }
            case "7000":{
                status = "Отклонен";
                break;
            }
            case "8000":{
                status = "Отменен";
                break;
            }
            case "9000":{
                status = "Закрыт";
                break;
            }

            default:{
                status = "Новый";
            }

        }

        return status;
    }
}
