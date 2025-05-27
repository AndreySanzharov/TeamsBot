package org.example.JsonHandlers;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class QuestionnaireApp {
    private static String currentQuestion = ""; // Переменная для хранения актуального вопроса

    public static void main(String[] args) {
        try {
            // Загружаем JSON-файл
            JSONObject jsonRoot = loadJson("src/main/java/org/example/JsonHandlers/questions.json");

            // Запускаем процесс опроса
            runQuestionnaire(jsonRoot);
        } catch (FileNotFoundException e) {
            System.err.println("Ошибка: JSON-файл не найден!");
        }
    }

    /**
     * Загружает JSON-файл в JSONObject.
     */
    private static JSONObject loadJson(String filePath) throws FileNotFoundException {
        return new JSONObject(new JSONTokener(new FileInputStream(filePath)));
    }

    /**
     * Основной метод обработки вопросов и ответов.
     */
    private static void runQuestionnaire(JSONObject currentNode) {
        Scanner scanner = new Scanner(System.in);

        while (currentNode != null) {
            // Получаем первый ключ (вопрос)
            String question = currentNode.keys().next();
            setCurrentQuestion(question); // Сохраняем текущий вопрос
            System.out.println(currentQuestion);

            JSONObject possibleAnswers = currentNode.getJSONObject(question);
            printPossibleAnswers(possibleAnswers); // Выводим возможные ответы

            String userAnswer;
            while (true) {
                userAnswer = scanner.nextLine().trim().toLowerCase();

                if (possibleAnswers.has(userAnswer)) {
                    break; // Пользователь ввел корректный ответ
                }
                System.out.println("Ответ не распознан. Попробуйте еще раз.");
            }

            Object next = possibleAnswers.get(userAnswer);

            // Если это строка — значит, это конечный ответ
            if (next instanceof String) {
                System.out.println(next);
                return;
            }

            // Если это объект JSON, продолжаем опрос
            currentNode = (JSONObject) next;
        }
    }

    /**
     * Метод для сохранения актуального вопроса в переменную.
     */
    private static void setCurrentQuestion(String question) {
        currentQuestion = question;
    }

    /**
     * Метод для вывода возможных ответов на текущий вопрос.
     */
    private static void printPossibleAnswers(JSONObject possibleAnswers) {
        System.out.println("Возможные ответы: " + String.join(" \\ ", possibleAnswers.keySet()));
    }
}
