public static boolean aggregateAnswer(String input, String chatId) {
    List<String> answers = userStateManager.getUserAnswersList(chatId);
    JSONObject result = new JSONObject();

    result.put("От кого", chatId);

    String registeredTo = answers.size() >= 1 ? answers.get(0) : "не указано";
    result.put("На кого зарегистрирована заявка", registeredTo);

    String issue = answers.size() >= 2 ? answers.get(1) : "не указано";
    result.put("Происшествие", issue);

    StringBuilder prettyOutput = new StringBuilder("Сводка заявки:\n");
    prettyOutput.append("От кого: ").append(chatId).append("\n");
    prettyOutput.append("На кого зарегистрирована заявка: ").append(registeredTo).append("\n");
    prettyOutput.append("Происшествие: ").append(issue);

    userStateManager.sendText(chatId, prettyOutput.toString());
    log.info("Ответ сформирован и отправлен: {}", result.toString());

    return true;
}
