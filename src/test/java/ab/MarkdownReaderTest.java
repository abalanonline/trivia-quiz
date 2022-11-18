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

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class MarkdownReaderTest {

  @Test
  void readQuestions() {
    String resourceString = new Scanner(getClass().getResourceAsStream("/startrek.md")).useDelimiter("\\A").next();
    List<TriviaQuestion> startrek = MarkdownReader.readQuestions(getClass().getResourceAsStream("/startrek.md"));
    assertEquals(4, startrek.size());
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    MarkdownReader.writeQuestions(outputStream, startrek);
    assertEquals(resourceString, new String(outputStream.toByteArray()));
  }
}
