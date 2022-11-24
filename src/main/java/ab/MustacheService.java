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

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MustacheService {

  public static final String RESOURCE_TEMPLATE = "templates/%s.mustache";

  private final MustacheFactory mustacheFactory;
  private final Map<String, Mustache> templates = new HashMap<>();

  public MustacheService() {
    this(new DefaultMustacheFactory());
  }

  public MustacheService(MustacheFactory mustacheFactory) {
    this.mustacheFactory = mustacheFactory;
  }

  private String applyObject(String template, Object object) {
    Mustache mustache = templates.computeIfAbsent(template,
        templateName -> mustacheFactory.compile(String.format(RESOURCE_TEMPLATE, templateName)));
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try {
      mustache.execute(new OutputStreamWriter(outputStream), object).flush();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
  }

  public String apply(String template, String... keysValues) {
    Map<String, String> scopes = new HashMap<>();
    for (int i = 1; i < keysValues.length; i += 2) {
      scopes.put(keysValues[i - 1], keysValues[i]);
    }
    return applyObject(template, scopes);
  }

  public String apply(String template, String[][] values, String... keys) {
    List<Map<String, String>> scopes = new ArrayList<>();
    for (String[] value : values) {
      Map<String, String> map = new HashMap<>();
      for (int i = 0; i < keys.length; i++) {
        map.put(keys[i], value[i]);
      }
      scopes.add(map);
    }
    return applyObject(template, Collections.singletonMap("items", scopes));
  }

}
