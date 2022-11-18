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
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TriviaApplicationTest {

  @Test
  void readHtml() throws IOException {
    Files.find(Paths.get("."), 1, (p, a) -> p.getFileName().toString().endsWith(".html")).forEach(htmlPath -> {
      try {
        List<TriviaQuestion> triviaQuestions = HtmlReader.readQuestions(Files.newInputStream(htmlPath));
        assertNotEquals(0, triviaQuestions.size());
        Path path = Paths.get("target", htmlPath.getFileName().toString() + ".md");
        MarkdownReader.writeQuestions(Files.newOutputStream(path), triviaQuestions);

        ByteArrayOutputStream b1 = new ByteArrayOutputStream();
        MarkdownReader.writeQuestions(b1, triviaQuestions);
        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        MarkdownReader.writeQuestions(b2, MarkdownReader.readQuestions(Files.newInputStream(path)));
        assertArrayEquals(b1.toByteArray(), b2.toByteArray());
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    });
  }
}
