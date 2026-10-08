/*
 * Copyright (c) 2026 Yuji Imagawa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package net.bis5.mattermost.model.serialize;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.bis5.mattermost.model.Bot;
import net.bis5.mattermost.model.Config;
import net.bis5.mattermost.model.config.EmailSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test cases for {@link MattermostPropertyNamingStrategy}.
 */
class MattermostPropertyNamingStrategyTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper()
        .setPropertyNamingStrategy(new MattermostPropertyNamingStrategy());
  }

  @Test
  void serializeModelAsSnakeCase() throws JsonProcessingException {
    Bot bot = new Bot();
    bot.setUserId("user-id");
    bot.setCreateAt(1234L);
    bot.setDisplayName("display name");

    JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(bot));

    assertEquals("user-id", json.get("user_id").asText());
    assertEquals(1234L, json.get("create_at").asLong());
    assertEquals("display name", json.get("display_name").asText());
    assertFalse(json.has("createAt"));
    assertFalse(json.has("CreateAt"));
  }

  @Test
  void deserializeModelFromSnakeCase() throws JsonProcessingException {
    Bot bot = objectMapper.readValue(
        "{\"user_id\":\"user-id\",\"create_at\":1234,\"display_name\":\"display name\"}",
        Bot.class);

    assertEquals("user-id", bot.getUserId());
    assertEquals(1234L, bot.getCreateAt());
    assertEquals("display name", bot.getDisplayName());
  }

  @Test
  void serializeConfigAsUpperCamelCase() throws JsonProcessingException {
    EmailSettings emailSettings = new EmailSettings();
    emailSettings.setFeedbackName("feedback");
    Config config = new Config();
    config.setEmailSettings(emailSettings);

    JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(config));

    assertTrue(json.has("EmailSettings"));
    assertFalse(json.has("email_settings"));
    JsonNode emailSettingsJson = json.get("EmailSettings");
    assertEquals("feedback", emailSettingsJson.get("FeedbackName").asText());
    assertFalse(emailSettingsJson.has("feedback_name"));
  }

  @Test
  void deserializeConfigFromUpperCamelCase() throws JsonProcessingException {
    Config config = objectMapper.readValue(
        "{\"EmailSettings\":{\"FeedbackName\":\"feedback\"}}", Config.class);

    assertEquals("feedback", config.getEmailSettings().getFeedbackName());
  }
}
