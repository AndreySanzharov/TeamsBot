public class Main {
    public static void main(String[] args) {
        try {
            ARSConnector connector = new ARSConnector();

            // Подключение к серверу
            String serverName = "your-ar-server-hostname";
            String userName = "Demo"; // замените на вашего пользователя
            String password = "password"; // замените на актуальный пароль

            connector.connect(serverName, userName, password);

            // Создание заявки
            String submitter = userName;
            String status = "New"; // или "Assigned", "In Progress" и т.д.
            String description = "Тестовая заявка с описанием проблемы";
            String information = "Дополнительная информация по заявке";

            // Используем метод createIncident (с именем)
            String firstName = "Иван";
            String lastName = "Иванов";
            String middleName = "Иванович";
            String castCompany = "ACME Corp";
            String category = "Инцидент"; // или другой из перечисленных
            String service = "Email Service";
            String supportGroup = "IT Support Group";
            String filePath = "C:\\temp\\example.txt"; // если нет файла, можно передать null

            String incidentNumber = connector.createIncident(
                submitter, status, description, information,
                firstName, lastName, middleName,
                castCompany, category, service, supportGroup,
                filePath
            );

            System.out.println("Инцидент создан: " + incidentNumber);

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
