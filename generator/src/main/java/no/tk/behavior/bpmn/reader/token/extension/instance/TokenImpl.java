/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package no.tk.behavior.bpmn.reader.token.extension.instance;

import static no.tk.behavior.bpmn.reader.token.extension.TokenBpmnModelConstants.TOKEN_BPMN_ATTRIBUTE_SHOULD_EXIST;
import static no.tk.behavior.bpmn.reader.token.extension.TokenBpmnModelConstants.TOKEN_BPMN_ELEMENT_TOKEN;
import static no.tk.behavior.bpmn.reader.token.extension.TokenBpmnModelConstants.TOKEN_BPMN_NS;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

import org.camunda.bpm.model.bpmn.impl.instance.ArtifactImpl;
import org.camunda.bpm.model.bpmn.instance.Artifact;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.attribute.Attribute;

/**
 * The BPMN dataObject element
 *
 * @author Dario Campagna
 */
public class TokenImpl extends ArtifactImpl implements Token {

  protected static Attribute<Boolean> shouldExistAttribute;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Token.class,
            TOKEN_BPMN_ELEMENT_TOKEN)
        .namespaceUri(TOKEN_BPMN_NS)
        .extendsType(Artifact.class)
        .instanceProvider((ModelTypeInstanceProvider<Token>) TokenImpl::new);

    shouldExistAttribute = typeBuilder.booleanAttribute(TOKEN_BPMN_ATTRIBUTE_SHOULD_EXIST)
        .defaultValue(true)
        .namespace(TOKEN_BPMN_NS)
        .build();

    typeBuilder.build();
  }

  public TokenImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  @Override
  public boolean shouldExist() {
    return shouldExistAttribute.getValue(this);
  }

  @Override
  public void setShouldExist(boolean shouldExist) {
    shouldExistAttribute.setValue(this, shouldExist);
  }
}
