/*
 * Copyright 2022 Aleksei Balan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ab;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MarkdownReader {

  public static final Pattern BREAK_PATTERN = Pattern.compile("\\*\\*\\*+\\n+");
  public static final String BREAK_STRING = "***";
  public static final Pattern QUESTION_PATTERN = Pattern.compile(
      "(?<question>.+)\\n\\n(?<choices>([*+-]\\s[^\\n]+\\n)+)\\n((?<answer>.*)\\n\\n)?", Pattern.DOTALL);
  public static final Pattern CHOICES_PATTERN = Pattern.compile(
      "([*+-])\\s([^\\n]+)\\n", Pattern.DOTALL);

  public static List<TriviaQuestion> readQuestions(InputStream inputStream) {
    String inputString = new Scanner(inputStream).useDelimiter("\\A").next();
    String[] split = BREAK_PATTERN.split(inputString);
    return Arrays.stream(split).map(s -> {
      Matcher questionMatcher = QUESTION_PATTERN.matcher(s);
      if (!questionMatcher.matches()) throw new IllegalStateException();
      TriviaQuestion triviaQuestion = new TriviaQuestion();
      triviaQuestion.question = questionMatcher.group("question");
      triviaQuestion.answer = questionMatcher.group("answer");
      Matcher choicesMatcher = CHOICES_PATTERN.matcher(questionMatcher.group("choices"));
      triviaQuestion.choices = new ArrayList<>();
      triviaQuestion.correct = new ArrayList<>();
      while (choicesMatcher.find()) {
        triviaQuestion.choices.add(choicesMatcher.group(2));
        String correctChar = choicesMatcher.group(1);
        switch (correctChar) {
          case "+":
            triviaQuestion.multipleAnswers = true;
            // fall through
          case "-":
          case "*":
            triviaQuestion.correct.add(!"-".equals(correctChar));
            break;
        }
      }
      return triviaQuestion;
    }).collect(Collectors.toList());
  }

  public static void writeQuestions(OutputStream outputStream, List<TriviaQuestion> triviaQuestions) {
    PrintWriter printWriter = new PrintWriter(outputStream);
    for (int triviaQuestionIndex = 0; triviaQuestionIndex < triviaQuestions.size(); triviaQuestionIndex++) {
      if (triviaQuestionIndex != 0) {
        printWriter.print(BREAK_STRING);
        printWriter.print("\n\n");
      }
      TriviaQuestion triviaQuestion = triviaQuestions.get(triviaQuestionIndex);
      printWriter.print(triviaQuestion.question);
      printWriter.print("\n\n");
      char correctChar = triviaQuestion.multipleAnswers ? '+' : '*';
      for (int i = 0; i < triviaQuestion.choices.size(); i++) {
        printWriter.print(triviaQuestion.correct.get(i) ? correctChar : '-');
        printWriter.print(' ');
        printWriter.print(triviaQuestion.choices.get(i));
        printWriter.print('\n');
      }
      printWriter.print('\n');
      if (triviaQuestion.answer != null) {
        printWriter.print(triviaQuestion.answer);
        printWriter.print("\n\n");
      }
    }
    printWriter.flush();
  }
}
