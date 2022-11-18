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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HtmlReader {

  public static final Pattern BODY_PATTERN = Pattern.compile(
      ".*?<div class=\"question_info\">(?<body>.*)</div>\\s+<div class=\"col-sm-12 well\">\\s+" +
      "<h4 class=\"sm\" style=\"color:#333;\">\\d+/\\d+ Questions right</h4>.*?", Pattern.DOTALL);
  public static final Pattern BREAK_PATTERN = Pattern.compile("</div>\\s+<div class=\"question_info\">");
  public static final String BREAK_STRING = "***";
  public static final Pattern QUESTION_PATTERN = Pattern.compile(
      ".*<small>- Multiple (Choice|Answer)</small>.*<div class=\"question_detail\">(?<question>.+)" +
      "<div\\s+class=\"pull-left\" style=\"margin-top: 10px;\">.*" +
      "<b>Your answer\\s*</b><br>\\s*<div style=\"font-size: 15px;\">(?<choices>.*)</div>\\s*" +
      "<b>Correct Answer</b><br>\\s*<div style=\"font-size: 15px;\">(?<correct>.*)</div>\\s*" +
      "<b>Explanation</b><br>\\s*<div style=\"font-size: 15px;\">(?<answer>.*)</div>\\s*" +
      "(<p><b>Points : </b>[^<>]*</p>\\s*)?</div>\\s*", Pattern.DOTALL);
//      "(?<question>.+)\\n\\n(?<choices>([*+-]\\s[^\\n]+\\n)+)\\n((?<answer>.*)\\n\\n)?", Pattern.DOTALL);
  public static final Pattern CHOICES_PATTERN = Pattern.compile(
      "<input type=\"([^\"]+)[^>]+>&nbsp;([^<]+)", Pattern.DOTALL);

  public static List<TriviaQuestion> readQuestions(InputStream inputStream) {
    String inputString = new Scanner(inputStream).useDelimiter("\\A").next();
    Matcher body = BODY_PATTERN.matcher(inputString);
    if (!body.matches()) throw new IllegalStateException();
    inputString = body.group("body");

    String[] split = BREAK_PATTERN.split(inputString);
    return Arrays.stream(split).map(s -> {
      Matcher questionMatcher = QUESTION_PATTERN.matcher(s);
      if (!questionMatcher.matches()) throw new IllegalStateException();
      TriviaQuestion triviaQuestion = new TriviaQuestion();
      triviaQuestion.question = questionMatcher.group("question").trim();
      triviaQuestion.answer = questionMatcher.group("answer").trim();
      String choices = questionMatcher.group("choices");
      Matcher choicesMatcher = CHOICES_PATTERN.matcher(choices);
      triviaQuestion.choices = new ArrayList<>();
      triviaQuestion.correct = new ArrayList<>();
      while (choicesMatcher.find()) {
        String group1 = choicesMatcher.group(1);
        switch (group1) {
          case "checkbox":
            triviaQuestion.multipleAnswers = true;
            // fall through
          case "radio":
            break;
          default:
            throw new IllegalStateException();
        }
        triviaQuestion.choices.add(choicesMatcher.group(2).trim());
        triviaQuestion.correct.add(false);
      }
      return triviaQuestion;
    }).filter(q -> q.choices.size() > 0).collect(Collectors.toList());
  }

}
